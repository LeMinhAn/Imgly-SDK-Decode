package com.camerafilter.sdk.brush.models;

import android.support.annotation.NonNull;
import android.util.Log;

import com.camerafilter.utils.CallSet;
import com.camerafilter.sdk.brush.BrushHistoryCache;
import com.camerafilter.sdk.configuration.PhotoEditorSdkConfig;

import java.util.ArrayList;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class Painting {
    public float hardness = .5f;
    public float size = 25;
    public int color;

    private ArrayList<PaintChunk> paintChunks;
    private PaintChunk currentChunk;

    private CallbackHandler callbackHandler = new CallbackHandler();

    public Painting() {
        paintChunks = new ArrayList<>();
        if (PhotoEditorSdkConfig.getBrushColors().size() > 0) {
            color = PhotoEditorSdkConfig.getBrushColors().get(0).getColor();
        }
    }

    public void addPoint(float x, float y) {
        if (currentChunk == null) {
            startPaintChunk();
        }
        currentChunk.addKeyPoint(new PaintKeyPoint(x, y));

        this.callbackHandler.paintingHasChanged(this);
    }

    public synchronized PaintChunk startPaintChunk() {
        if (finalizePaintChunk()) {
            Log.w("Brush", "Warning: old PaintChuck not finalized");
        }
        Brush brush = new Brush(size, hardness, color);
        currentChunk = new PaintChunk(brush);
        paintChunks.add(currentChunk);
        callbackHandler.paintingChunkCreate(this, currentChunk);
        return currentChunk;
    }

    public boolean finalizePaintChunk() {
        if (currentChunk != null) {
            currentChunk.finishChunk();
            currentChunk = null;
            return true;
        }
        return false;
    }

    public boolean goBackwards() {
        PaintChunk removedChunk = null;
        if (paintChunks.size() >= 1) {
            removedChunk = paintChunks.remove(paintChunks.size() - 1);
        }
        if (removedChunk != null && BrushHistoryCache.hasCache(removedChunk)) {
            callbackHandler.paintingChunkDestroy(this, removedChunk);
            return true;
        }
        return false;
    }

    @NonNull
    public ArrayList<PaintChunk> getPaintChunks() {
        return paintChunks;
    }

    public void addCallback(@NonNull Callback callback) {
        this.callbackHandler.add(callback);
    }

    public void removeCallback(@NonNull Callback callback) {
        this.callbackHandler.remove(callback);
    }

    public interface Callback {
        void paintingHasChanged(@NonNull Painting painting);

        void paintingChunkCreate(@NonNull Painting painting, PaintChunk newChunk);

        void paintingChunkDestroy(@NonNull Painting painting, PaintChunk removedChunk);
    }

    private static final class CallbackHandler extends CallSet<Callback> {
        private void paintingHasChanged(@NonNull Painting painting) {
            for (Callback callback : getSet()) {
                callback.paintingHasChanged(painting);
            }
        }

        private void paintingChunkCreate(@NonNull Painting painting, PaintChunk newChunk) {
            for (Callback callback : getSet()) {
                callback.paintingChunkCreate(painting, newChunk);
            }
        }

        private void paintingChunkDestroy(@NonNull Painting painting, PaintChunk removedChunk) {
            for (Callback callback : getSet()) {
                callback.paintingChunkDestroy(painting, removedChunk);
                callback.paintingHasChanged(painting);
            }
        }
    }
}
