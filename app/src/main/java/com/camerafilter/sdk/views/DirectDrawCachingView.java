package com.camerafilter.sdk.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.view.TextureView;

import com.camerafilter.sdk.brush.BitmapLayer;
import com.camerafilter.utils.ThreadUtils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class DirectDrawCachingView extends TextureView implements TextureView.SurfaceTextureListener {
    private volatile boolean isAvailable = false;
    private volatile AtomicBoolean isLocked = new AtomicBoolean(false);
    private volatile Canvas lockedCanvas;
    private BitmapLayer.ConcurrentLayer bitmapLayer;


    public DirectDrawCachingView(Context context) {
        super(context);
        setLayerType(LAYER_TYPE_HARDWARE, null);
        setSurfaceTextureListener(this);
    }

    @Nullable
    @Override
    @WorkerThread
    public BitmapLayer lockCanvas() {

        if (bitmapLayer == null || !isAvailable) {
            return null;
        }

        while (!isLocked.compareAndSet(false, true)) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {
            }
        }

        //noinspection WrongThread
        lockedCanvas = super.lockCanvas();
        if (lockedCanvas == null || !isAvailable) {
            isLocked.compareAndSet(true, false);
            return null;
        } else {
            return bitmapLayer.lock();
        }
    }

    @WorkerThread
    public void unlockAndUpdate() {
        if (lockedCanvas != null) {
            if (bitmapLayer != null) {
                Bitmap bitmap = bitmapLayer.getLockedLayer().getBitmap();
                if (bitmap != null) {
                    lockedCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    lockedCanvas.drawBitmap(bitmap, 0, 0, null);
                }
                bitmapLayer.unlock();
            }
            //noinspection WrongThread
            super.unlockCanvasAndPost(lockedCanvas);
            isLocked.compareAndSet(true, false);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        createBitmapLayer(width, height);
        isAvailable = true;
    }

    private void createBitmapLayer(int width, int height) {
        boolean mustBeInvalidated = bitmapLayer != null;
        if (bitmapLayer == null || bitmapLayer.width != width || bitmapLayer.height != height) {
            BitmapLayer.ConcurrentLayer oldLayer = bitmapLayer;
            if (oldLayer != null) {
                // noinspection WrongThread
                oldLayer.lock();
            }
            bitmapLayer = new BitmapLayer.ConcurrentLayer(width, height, Bitmap.Config.ARGB_8888);
            if (oldLayer != null) {
                try {
                    Bitmap bitmap = oldLayer.getLockedLayer().getBitmap();
                    if (bitmap != null) {
                        // noinspection WrongThread
                        bitmapLayer.lock().drawBitmap(bitmap, 0, 0, null);
                    }
                } finally {
                    oldLayer.unlock();
                    bitmapLayer.unlock();
                }
            }
        }

        if (mustBeInvalidated) {
            ThreadUtils.addTaskToWorkerGroup("TextureViewInvalidate", ThreadUtils.PRIORITY.MAX_PRIORITY, new ThreadUtils.WorkerThreadRunnable() {
                @Override
                public void run() {
                    lockCanvas();
                    unlockAndUpdate();
                }
            });
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        createBitmapLayer(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        isAvailable = false;
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }
}

