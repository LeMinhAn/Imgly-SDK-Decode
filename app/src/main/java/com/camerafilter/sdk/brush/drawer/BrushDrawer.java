package com.camerafilter.sdk.brush.drawer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

import com.camerafilter.sdk.brush.models.Brush;

/**
 * Created by Le Minh An on 9/20/2016.
 */
class BrushDrawer {

    private final float xOffset;
    private final float yOffset;

    private final int stepAlpha;
    private final Brush brush;
    private final float resolutionScale;
    private Bitmap brushBitmap;
    private Paint brushPaint;
    private Matrix matrix;

    BrushDrawer(Brush brush) {
        this(brush, 1);
    }

    BrushDrawer(Brush brush, float resolutionScale) {

        this.brush = brush;

        this.resolutionScale = resolutionScale;

        this.matrix = new Matrix();

        this.brushBitmap = createBrushBitmap();
        this.xOffset = brush.radius;
        this.yOffset = brush.radius;

        float fadeLength = 1 + ((brush.radius) / (brush.stepSize * 2)) * (1 - brush.hardness);
        this.stepAlpha = (int) Math.ceil(255 / fadeLength);

        this.brushPaint = new Paint();
        this.brushPaint.setAntiAlias(true);
        this.brushPaint.setFilterBitmap(true);
        this.brushPaint.setAlpha(stepAlpha);

    }

    private Bitmap createBrushBitmap() {

        Bitmap bitmap = Bitmap.createBitmap((int) Math.ceil(brush.radius * 2 * resolutionScale), (int) Math.ceil(brush.radius * 2 * resolutionScale), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        paint.setShader(new RadialGradient(
                brush.radius * resolutionScale,
                brush.radius * resolutionScale,
                brush.radius * resolutionScale,
                new int[]{0xFFFFFFFF, 0x00FFFFFF},
                new float[]{brush.hardness, 1},
                Shader.TileMode.CLAMP
        ));

        canvas.drawCircle(brush.radius * resolutionScale, brush.radius * resolutionScale, brush.radius * resolutionScale, paint);

        return bitmap.copy(Bitmap.Config.ARGB_8888, false);
    }

    void draw(final Canvas canvas, final float x, final float y) {
        if (canvas != null) {

            if (resolutionScale == 1) {
                canvas.drawBitmap(brushBitmap, x - xOffset, y - yOffset, brushPaint);
            } else {
                matrix.reset();
                matrix.preScale(1 / resolutionScale, 1 / resolutionScale);
                matrix.postTranslate(x - xOffset , y - yOffset);

                canvas.drawBitmap(brushBitmap, matrix, brushPaint);
            }

        }
    }

    void correctBounds(RectF bounds) {
        bounds.set(
                bounds.left - (xOffset) - 1,
                bounds.top - (yOffset) - 1,
                bounds.right + (brush.radius * 2 - xOffset) + 1,
                bounds.bottom + (brush.radius * 2 - yOffset) + 1
        );
    }

}
