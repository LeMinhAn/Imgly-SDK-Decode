package com.camerafilter.sdk.brush.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.view.View;

import com.camerafilter.sdk.brush.BitmapLayer;
import com.camerafilter.sdk.brush.drawer.PaintChunkDrawer;
import com.camerafilter.sdk.brush.models.PaintChunk;
import com.camerafilter.utils.ThreadUtils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class BrushChunkPreviewLayer extends View implements PaintChunk.Callback {

    private static long LAYER_CREATE_COUNT = 0;

    public PaintChunk chunk;

    private long layerId = LAYER_CREATE_COUNT++;

    private volatile AtomicBoolean isAttached = new AtomicBoolean(false);
    private volatile float cachedLength = -1;
    private volatile float previewLength = -1;


    private ThreadUtils.WorkerThreadRunnable backgroundRunnable;
    private PaintChunkDrawer previewDrawer;
    private BitmapLayer.ConcurrentLayer bitmapLayer;

    @MainThread
    public BrushChunkPreviewLayer(Context context, PaintChunk chunk) {
        super(context);
        this.chunk = chunk;

        setWillNotDraw(false);
    }

    @Override
    @MainThread
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.bitmapLayer = new BitmapLayer.ConcurrentLayer(w, h, Bitmap.Config.ARGB_8888);
        previewLength = 0;
        cachedLength = 0;
    }

    @Override
    @MainThread
    protected void onDraw(Canvas canvas) {
        if (isAttached.get() && bitmapLayer != null) {
            super.onDraw(canvas);
            try {
                BitmapLayer layer = bitmapLayer.lock();
                Bitmap bitmap = layer.getBitmap();
                if (isAttached.get()) {
                    canvas.drawBitmap(bitmap, 0, 0, null);
                }
            } finally {
                bitmapLayer.unlock();
            }
            if (isAttached.get()) {
                previewLength = previewDrawer.drawPath(canvas, cachedLength);
                if (backgroundRunnable != null) {
                    // This is a serial background execute for our minimal SDK Android Version and above!
                    ThreadUtils.replaceTaskOnWorkerGroup("DrawChunk_" + layerId, ThreadUtils.PRIORITY.NORM_PRIORITY, backgroundRunnable);
                }
            }
        }
    }

    @Override
    @MainThread
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttached.compareAndSet(false, true);

        this.chunk.addCallback(this);

        this.previewDrawer = new PaintChunkDrawer(chunk);

        setLayerType(LAYER_TYPE_HARDWARE, getLayerPaint());

        backgroundRunnable = new ThreadUtils.WorkerThreadRunnable() {
            // Use own instance to avoid thread conflicts
            private PaintChunkDrawer cacheDrawer = new PaintChunkDrawer(chunk);

            @Override
            @WorkerThread
            public void run() {
                if (isAttached.get() && bitmapLayer != null) {
                    try {
                        cachedLength = cacheDrawer.drawPath(bitmapLayer.lock(), cachedLength);
                    } finally {
                        bitmapLayer.unlock();
                    }
                }
            }
        };
    }

    @WorkerThread
    @MainThread
    Paint getLayerPaint() {
        return previewDrawer.getLayerPaint();
    }

    @Override
    @MainThread
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        chunk.removeCallback(this);
        isAttached.compareAndSet(true, false);
        backgroundRunnable = null;
    }

    @Nullable
    @WorkerThread
    Bitmap getResult() {
        if (bitmapLayer == null) return null;

        try {
            BitmapLayer layer = bitmapLayer.lock();
            if (cachedLength != previewLength) {
                if (layer != null) {
                    PaintChunkDrawer resultDrawer = new PaintChunkDrawer(chunk);
                    cachedLength = resultDrawer.drawPath(layer, previewLength);
                }

                chunk.finishChunk();
            }

            return layer != null ? layer.getBitmap() : null;
        } finally {
            bitmapLayer.unlock();
        }
    }

    @Override
    public void brushChunkChanged(PaintChunk chunk) {
        postInvalidate();
    }

    @Override
    public void brushChunkFinished(PaintChunk chunk) {

    }
}

