package com.camerafilter.sdk.brush.models;

import android.support.annotation.NonNull;

import com.camerafilter.utils.CallSet;

import java.util.ArrayList;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class PaintChunk {
    private static long brushChunkCounter = 0;

    public final ArrayList<PaintKeyPoint> points;
    public final Brush brush;

    private final long runtimeBrushId = brushChunkCounter++;

    private boolean isFinished;
    private CallbackHandler callbackHandler;

    PaintChunk(Brush brush) {
        this.brush = brush;
        this.points = new ArrayList<>();
        this.callbackHandler = new CallbackHandler();
    }

    void addKeyPoint(@NonNull PaintKeyPoint point) {
        points.add(point);
        callbackHandler.brushChunkChanged(this);
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void finishChunk() {
        if (!isFinished) {
            this.callbackHandler.brushChunkFinished(this);
            isFinished = true;
        }
    }

    public long getRuntimeUniqId() {
        return runtimeBrushId;
    }

    public void addCallback(Callback callback) {
        this.callbackHandler.add(callback);
    }

    public void removeCallback(Callback callback) {
        this.callbackHandler.remove(callback);
    }

    public interface Callback {
        void brushChunkChanged(PaintChunk chunk);

        void brushChunkFinished(PaintChunk chunk);
    }

    private static final class CallbackHandler extends CallSet<Callback> {
        private void brushChunkChanged(@NonNull PaintChunk chunk) {
            for (Callback callback : getSet()) {
                callback.brushChunkChanged(chunk);
            }
        }

        private void brushChunkFinished(@NonNull PaintChunk chunk) {
            for (Callback callback : getSet()) {
                callback.brushChunkFinished(chunk);
            }
        }
    }
}
