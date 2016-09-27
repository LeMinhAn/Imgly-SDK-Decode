package com.camerafilter.sdk.brush.drawer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.NonNull;

import com.camerafilter.sdk.brush.BrushHistoryCache;
import com.camerafilter.sdk.brush.models.PaintChunk;
import com.camerafilter.sdk.brush.models.Painting;

import java.util.ArrayList;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class PaintingDrawer {
    final Painting painting;

    public PaintingDrawer(@NonNull Painting painting) {
        this.painting = painting;
    }

    public void draw(@NonNull Canvas canvas, boolean useCache) {
        draw(canvas, useCache, 1);
    }

    public void draw(@NonNull Canvas canvas, boolean useCache, float resolutionScale) {
        ArrayList<PaintChunk> paintChunks = painting.getPaintChunks();
        if (paintChunks.size() >= 1) {
            PaintChunk lastChunk = paintChunks.get(paintChunks.size() - 1);

            if (useCache && BrushHistoryCache.hasCache(lastChunk)) {
                Bitmap bitmap = BrushHistoryCache.getCache(lastChunk);
                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, 0, 0, null);
                } // TODO: else Prevent delete in time of hasCache to getCache;
            } else for (PaintChunk chunk : painting.getPaintChunks()) {
                PaintChunkDrawer drawer = new PaintChunkDrawer(chunk, resolutionScale);
                drawer.drawPaintedLayer(canvas);
            }
        }
    }
}

