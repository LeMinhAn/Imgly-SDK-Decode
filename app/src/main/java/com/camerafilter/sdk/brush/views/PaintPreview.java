package com.camerafilter.sdk.brush.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.camerafilter.sdk.brush.BitmapLayer;
import com.camerafilter.sdk.brush.BrushHistoryCache;
import com.camerafilter.sdk.brush.drawer.PaintingDrawer;
import com.camerafilter.sdk.brush.models.PaintChunk;
import com.camerafilter.sdk.brush.models.Painting;
import com.camerafilter.utils.ThreadUtils;
import com.camerafilter.sdk.views.DirectDrawCachingView;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class PaintPreview extends FrameLayout implements Painting.Callback, PaintChunk.Callback {

    protected final Painting painting;
    private volatile AtomicBoolean duringLayerRemove = new AtomicBoolean(false);

    private LayoutParams layerParams;

    private DirectDrawCachingView drawCachingView;

    public PaintPreview(@NonNull Context context, @NonNull Painting painting) {
        this(context, null, painting);
    }

    public PaintPreview(@NonNull Context context, AttributeSet attrs, @NonNull Painting painting) {
        this(context, attrs, 0, painting);
    }

    public PaintPreview(Context context, AttributeSet attrs, int defStyleAttr, @NonNull Painting painting) {
        super(context, attrs, defStyleAttr);
        setLayerType(LAYER_TYPE_HARDWARE, null);
        this.painting = painting;

        this.layerParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        this.drawCachingView = new DirectDrawCachingView(getContext());

        addView(drawCachingView, layerParams);

        setWillNotDraw(true);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        painting.addCallback(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        painting.removeCallback(this);
    }

    @Override
    public void paintingHasChanged(@NonNull Painting painting) {
    }

    @Override
    public void paintingChunkCreate(@NonNull Painting painting, PaintChunk newChunk) {
        addLayer(newChunk);
    }

    @Override
    public void paintingChunkDestroy(@NonNull final Painting painting, PaintChunk removeChunk) {
        BrushHistoryCache.destroyCache(removeChunk);
        removeChunk.removeCallback(this);

        BrushChunkPreviewLayer removeLayer = searchLayerForChunk(removeChunk);
        if (removeLayer != null) {
            removeView(removeLayer);
        }

        ThreadUtils.addTaskToWorkerGroup("CombineLayer", ThreadUtils.PRIORITY.MAX_PRIORITY, new ThreadUtils.WorkerThreadRunnable() {
            @Override
            @WorkerThread
            public void run() {
                PaintingDrawer drawer = new PaintingDrawer(painting);
                try {
                    BitmapLayer layer = drawCachingView.lockCanvas();
                    if (layer != null) {
                        layer.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                        drawer.draw(layer, true);
                    }
                } finally {
                    drawCachingView.unlockAndUpdate();
                }
            }
        });

    }

    private void addLayer(PaintChunk chunk) {
        if (searchLayerForChunk(chunk) == null) {
            BrushChunkPreviewLayer layer = new BrushChunkPreviewLayer(getContext(), chunk);
            addView(layer, layerParams);

            chunk.addCallback(this);
        }
    }

    private synchronized BrushChunkPreviewLayer searchLayerForChunk(PaintChunk chunk) {
        BrushChunkPreviewLayer foundedLayer = null;
        for (BrushChunkPreviewLayer layer : getLayers()) {
            if (chunk.equals(layer.chunk)) {
                foundedLayer = layer;
                break;
            }
        }
        return foundedLayer;
    }

    private BrushChunkPreviewLayer[] getLayers() {
        int childCount = getChildCount();
        BrushChunkPreviewLayer[] layers = new BrushChunkPreviewLayer[childCount - 1];
        for (int i = 0, l = 0; i < childCount; i++) {
            View view = getChildAt(i);
            if (view instanceof BrushChunkPreviewLayer) {
                layers[l++] = (BrushChunkPreviewLayer) view;
            }
        }
        return layers;
    }

    @Override
    public void brushChunkChanged(PaintChunk chunk) {

    }

    @Override
    public void brushChunkFinished(PaintChunk chunk) {
        removeLayer(chunk);
    }

    // TODO: Make code beautiful
    private void removeLayer(final PaintChunk chunk) {

        chunk.removeCallback(this);

        final BrushChunkPreviewLayer removeLayer = searchLayerForChunk(chunk);

        ThreadUtils.addTaskToWorkerGroup("CombineLayer", ThreadUtils.PRIORITY.MAX_PRIORITY, new ThreadUtils.WorkerThreadRunnable() {
            @Override
            @WorkerThread
            public void run() {

                while (duringLayerRemove.get() && !duringLayerRemove.compareAndSet(false, true)) {
                    sleep(1);
                }

                if (removeLayer != null) {
                    Bitmap layerBitmap = removeLayer.getResult();
                    if (layerBitmap != null) {
                        try {
                            BitmapLayer layer = drawCachingView.lockCanvas();
                            if (layer != null) {
                                layer.drawBitmap(layerBitmap, 0, 0, removeLayer.getLayerPaint());
                                BrushHistoryCache.saveCache(chunk, layer.getBitmap().copy(Bitmap.Config.ARGB_8888, false));
                            }
                        } finally {
                            drawCachingView.unlockAndUpdate();
                        }

                    }
                    runOnUi(new ThreadUtils.MainThreadRunnable() {
                        @Override
                        @MainThread
                        public void run() {
                            removeView(removeLayer);
                            duringLayerRemove.compareAndSet(true, false);
                        }
                    });
                }
            }
        });
    }
}
