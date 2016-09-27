package com.camerafilter.ui.widgets.colorpicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.camerafilter.utils.ThreadUtils;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class SelectView extends View {

    @NonNull
    private final Paint paint;
    @NonNull
    private final RectF stage;
    private final float density;
    @NonNull
    private final RectF colorRange;

    private Shader saturationGradient;
    private Shader lightnessGradient;

    protected Bitmap finderBitmap;

    private float hue = 0;

    private float selectionX = 0;
    private float selectionY = 0;

    private OnOqaqueColorChangedListener listener;

    public SelectView(Context context) {
        this(context, null);
    }

    public SelectView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SelectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        stage = new RectF();
        paint = new Paint();

        colorRange = new RectF();
        density = getResources().getDisplayMetrics().density;

        setWillNotDraw(false);
    }

    public void setHue(float hue, boolean triggerListener) {
        this.hue = hue;
        createShader();

        dispatchListener(triggerListener);
        if (ThreadUtils.thisIsUiThread()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    public void setListener(OnOqaqueColorChangedListener listener) {
        this.listener = listener;
    }

    private void createShader() {
        int[] lightnessColors = new int[] {
                Color.HSVToColor(0,   new float[]{hue, 1, 1}),
                Color.HSVToColor(255, new float[]{hue, 1, 0})
        };
        int[] saturationColors = new int[] {
                Color.HSVToColor(255, new float[]{hue, 0, 1}),
                Color.HSVToColor(255, new float[]{hue, 1, 1})
        };
        saturationGradient = new LinearGradient(0, 0, stage.width(), 0, saturationColors, new float[]{0, 1}, Shader.TileMode.CLAMP);
        lightnessGradient = new LinearGradient(0, 0, 0, stage.height(), lightnessColors, new float[]{0, 1}, Shader.TileMode.CLAMP);
    }

    private void generateFinderBitmap() {

        float shadowBlur = 3 * density;
        float shadowY    = 2 * density;

        float radiusWidth = 9 * density;
        float strokeWidth = 2 * density;

        float width = (radiusWidth + shadowBlur) * 2;
        float height = width + shadowY * 2;

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xFFFFFFFF);
        paint.setStrokeWidth(strokeWidth);

        paint.setShadowLayer(shadowBlur, 0, shadowY, 0x7F000000);

        RectF rect = new RectF(shadowBlur, height / 2 - radiusWidth, width - shadowBlur, height / 2 + radiusWidth);

        finderBitmap = Bitmap.createBitmap((int) Math.ceil(width), (int) Math.ceil(height), Bitmap.Config.ARGB_8888);
        finderBitmap.eraseColor(Color.TRANSPARENT);
        Canvas canvas = new Canvas(finderBitmap);
        canvas.drawOval(rect, paint);
    }

    public void setColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);

        selectionX = hsv[1];
        selectionY = 1 - hsv[2];
        setHue(hsv[0], false);
    }

    private void dispatchListener(boolean triggerListener) {
        if (listener != null && triggerListener) {
            listener.onOpaqueColorChanged(getColorSelection());
        }
    }

    protected int getColorSelection(){
        return Color.HSVToColor(new float[]{hue, selectionX, 1 - selectionY});
    }

    protected void onProgressChange(float progressX, float progressY) {
        this.selectionX = progressX;
        this.selectionY = progressY;

        dispatchListener(true);

        if (ThreadUtils.thisIsUiThread()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();

        final float progressX = (x - colorRange.left) / colorRange.width();
        final float progressY = (y - colorRange.top)  / colorRange.height();

        onProgressChange(
                progressX < 0 ? 0 : (progressX > 1 ? 1 : progressX),
                progressY < 0 ? 0 : (progressY > 1 ? 1 : progressY)
        );

        return true;
    }

    protected void drawFinder(@NonNull Canvas canvas, float selectionX, float selectionY) {
        if (finderBitmap != null) {
            float x = colorRange.left + colorRange.width()  * selectionX - finderBitmap.getWidth()  / (float) 2;
            float y = colorRange.top  + colorRange.height() * selectionY - finderBitmap.getHeight() / (float) 2;

            canvas.drawBitmap(finderBitmap, x, y, paint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float left    = getPaddingLeft();
        float top     = getPaddingTop();
        float right   = w - getPaddingRight();
        float bottom  = h - getPaddingBottom();

        stage.set(left, top, right, bottom);

        colorRange.set(stage.left, stage.top, stage.right, stage.bottom);
        generateFinderBitmap();
        createShader();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        paint.setShader(saturationGradient);
        canvas.drawRoundRect(colorRange, density * 2f, density * 2f, paint);
        paint.setShader(lightnessGradient);
        canvas.drawRoundRect(colorRange, density * 2f, density * 2f, paint);

        drawFinder(canvas, selectionX, selectionY);
    }

    public interface OnOqaqueColorChangedListener {
        void onOpaqueColorChanged(int color);
    }
}

