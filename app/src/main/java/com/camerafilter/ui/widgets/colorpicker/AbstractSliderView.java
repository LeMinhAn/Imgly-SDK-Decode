package com.camerafilter.ui.widgets.colorpicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public abstract class AbstractSliderView extends View {
    @NonNull
    protected final RectF stage;
    protected final float density;
    @NonNull
    protected final RectF colorRange;

    protected Bitmap finderBitmap;

    @NonNull
    private final Paint paint;

    public AbstractSliderView(Context context) {
        this(context, null);
    }

    public AbstractSliderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AbstractSliderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        paint.setFilterBitmap(true);

        stage = new RectF();
        colorRange = new RectF();

        density = getResources().getDisplayMetrics().density;
    }

    private void generateFinderBitmap() {

        float shadowBlur = 3 * density;
        float shadowY    = 2 * density;

        float lineWidth  = stage.width();
        float lineHeight = 3 * density;

        float width  = stage.width() + shadowBlur * 2;
        float height = lineHeight + (shadowBlur + shadowY) * 2;

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFFFFFFFF);

        paint.setShadowLayer(shadowBlur, 0, shadowY, 0x7F000000);

        RectF rect = new RectF(shadowBlur, (height - lineHeight) / 2, shadowBlur + lineWidth, (height + lineHeight) / 2);

        finderBitmap = Bitmap.createBitmap((int) Math.ceil(width), (int) Math.ceil(height), Bitmap.Config.ARGB_8888);
        finderBitmap.eraseColor(Color.TRANSPARENT);
        Canvas canvas = new Canvas(finderBitmap);
        canvas.drawRect(rect, paint);
    }

    protected abstract void onProgressChange(float progress);

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        final float y = event.getY();
        final float progress = (y - colorRange.top) / colorRange.height();
        onProgressChange(progress < 0 ? 0 : (progress > 1 ? 1 : progress));

        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);


        float left    = getPaddingLeft();
        float top     = getPaddingTop();
        float right   = w - getPaddingRight();
        float bottom  = h - getPaddingBottom();

        stage.set(left, top, right, bottom);

        float rangeWidth = 12 * density;
        colorRange.set(stage.left + ((stage.width() - rangeWidth) / 2), stage.top, stage.right - ((stage.width() - rangeWidth) / 2), stage.bottom);
        generateFinderBitmap();
    }

    protected void drawFinder(@NonNull Canvas canvas, float progress) {
        if (finderBitmap != null) {
            float y = colorRange.top + colorRange.height() * progress - finderBitmap.getHeight() / (float) 2;
            float x = colorRange.centerX() - (finderBitmap.getWidth() / 2);

            canvas.drawBitmap(finderBitmap, x, y, paint);
        }
    }
}

