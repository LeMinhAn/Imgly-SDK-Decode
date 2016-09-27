package com.camerafilter.sdk.brush.drawer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.camerafilter.sdk.brush.models.PaintChunk;
import com.camerafilter.sdk.brush.models.PaintKeyPoint;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class PaintChunkDrawer {
    private static final int SMOOTH_VAL = 3;

    private float[] pos = new float[]{0, 0};
    private float[] tan = new float[]{0, 0};

    private PaintChunk chunk;
    private BrushDrawer brushDrawer;

    private Path path;
    private PathMeasure pathMeasure;

    private int pathKeyPointCount;

    public PaintChunkDrawer(PaintChunk chunk) {
        this(chunk, 1);
    }

    public PaintChunkDrawer(PaintChunk chunk, float resolutionScale) {
        setChunk(chunk, resolutionScale);
    }

    public void setChunk(PaintChunk chunk) {
        setChunk(chunk, 1);
    }

    public void setChunk(PaintChunk chunk, float resolutionScale) {
        this.chunk = chunk;
        this.path = new Path();
        this.brushDrawer = new BrushDrawer(chunk.brush, resolutionScale);
        this.pathMeasure = new PathMeasure();

        this.pathKeyPointCount = 0;
    }

    public void drawPaintedLayer(Canvas canvas) {
        canvas.saveLayer(getBounds(), getLayerPaint(), Canvas.ALL_SAVE_FLAG);
        drawPath(canvas, 0);
        canvas.restore();
    }

    public synchronized float drawPath(@NonNull Canvas canvas, float startLength) {
        updatePathMeasure();

        final float pathLength = getPathLength();

        float drawnLength = startLength;
        for (; drawnLength < pathLength; drawnLength += chunk.brush.stepSize) {
            pos = calculatePos(drawnLength);
            brushDrawer.draw(canvas, pos[0], pos[1]);
        }

        pathMeasure.setPath(null, false);
        return drawnLength;
    }

    private float[] calculatePos(float position) {
        pathMeasure.getPosTan(position, pos, tan);
        return pos;
    }

    public RectF getBounds() {
        RectF bounds = new RectF();
        updatePathMeasure();
        path.computeBounds(bounds, false);
        brushDrawer.correctBounds(bounds);
        return bounds;
    }

    private synchronized void updatePathMeasure() {

        for (int i = pathKeyPointCount, l = chunk.points.size() - 1; i <= l; i++) {
            PaintKeyPoint point = chunk.points.get(i);
            if (i == 0) {
                path.moveTo(point.x, point.y);
            } else {
                PaintKeyPoint beforeLastPoint = (i >= 2) ? chunk.points.get(i - 2) : null;
                PaintKeyPoint lastPoint = (i >= 1) ? chunk.points.get(i - 1) : null;
                PaintKeyPoint nextPoint = (i < l) ? chunk.points.get(i + 1) : null;

                final float pointDx;
                final float pointDy;

                if (nextPoint == null) {
                    pointDx = ((point.x - lastPoint.x) / SMOOTH_VAL);
                    pointDy = ((point.y - lastPoint.y) / SMOOTH_VAL);
                } else {
                    pointDx = ((nextPoint.x - lastPoint.x) / SMOOTH_VAL);
                    pointDy = ((nextPoint.y - lastPoint.y) / SMOOTH_VAL);
                }

                final float lastPointDx;
                final float lastPointDy;

                if (beforeLastPoint == null) {
                    lastPointDx = ((point.x - lastPoint.x) / SMOOTH_VAL);
                    lastPointDy = ((point.y - lastPoint.y) / SMOOTH_VAL);
                } else {
                    lastPointDx = ((point.x - beforeLastPoint.x) / SMOOTH_VAL);
                    lastPointDy = ((point.y - beforeLastPoint.y) / SMOOTH_VAL);
                }

                path.cubicTo(
                        lastPoint.x + lastPointDx,
                        lastPoint.y + lastPointDy,
                        point.x - pointDx,
                        point.y - pointDy,
                        point.x,
                        point.y
                );
            }

            pathKeyPointCount = i + 1;
        }
        pathMeasure.setPath(path, false);
    }

    public Paint getLayerPaint() {
        int color = chunk.brush.color;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColorFilter(new ColorMatrixColorFilter(new float[]{
                0, 0, 0, 0, Color.red(color),
                0, 0, 0, 0, Color.green(color),
                0, 0, 0, 0, Color.blue(color),
                0, 0, 0, 1, 0
        }));
        paint.setAlpha(Color.alpha(color));

        return paint;
    }

    private float getPathLength() {
        return pathMeasure.getLength();
    }
}
