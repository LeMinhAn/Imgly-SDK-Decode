package com.camerafilter.ui.widgets.colorpicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import com.camerafilter.R;
import com.camerafilter.utils.BitmapFactoryUtils;
import com.camerafilter.utils.ThreadUtils;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class AlphaView extends AbstractSliderView {
    @NonNull
    private final Paint paint;

    private Shader shader;
    private Shader chessboardShader;

    private int color = 0;

    private int alphaSelection = 0;

    private OnAlphaChangedListener listener;

    public AlphaView(Context context) {
        this(context, null);
    }

    public AlphaView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlphaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();

        setWillNotDraw(false);
    }

    public void setListener(OnAlphaChangedListener listener) {
        this.listener = listener;
    }

    public void setAlphaSelection(int alpha, boolean triggerListener) {
        this.alphaSelection = (alpha > 255 ? alpha : (alpha < 0) ? 0 : alpha);
        if (ThreadUtils.thisIsUiThread()) {
            invalidate();
        } else {
            postInvalidate();
        }

        dispatchListener(triggerListener);
    }

    public int getAlphaSelection() {
        return alphaSelection;
    }

    private void dispatchListener(boolean triggerListener) {
        if (listener != null && triggerListener) {
            listener.onAlphaChanged(alphaSelection);
        }
    }

    @Override
    protected void onProgressChange(float progress) {
        setAlphaSelection(Math.round(255 * progress), true);
    }

    private void createResources() {
        int[] alphaColors = new int[]{
                Color.argb(0,   Color.red(color), Color.green(color), Color.blue(color)),
                Color.argb(255, Color.red(color), Color.green(color), Color.blue(color))
        };


        //noinspection SuspiciousNameCombination
        shader = new LinearGradient(colorRange.top, colorRange.left, colorRange.top, colorRange.bottom,  alphaColors, new float[]{0, 1}, Shader.TileMode.CLAMP);
        if (stage.width() > 0) {
            Bitmap chessboardBitmap = BitmapFactoryUtils.drawResource(getResources(), R.drawable.imgly_background_transparent_indentity, Math.round(40 * density), Math.round(40 * density));
            chessboardShader = new BitmapShader(
                    Bitmap.createScaledBitmap(chessboardBitmap, (int) (chessboardBitmap.getWidth() * (1/2.5f)), (int) (chessboardBitmap.getHeight() * (1/2.5f)), true),
                    Shader.TileMode.REPEAT,
                    Shader.TileMode.REPEAT);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        stage.set(0, 0, w, h);
        createResources();
    }

    public void setColor(int color) {
        this.color = color;
        createResources();
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        stage.set(0, 0, getWidth(), getHeight());

        if (chessboardShader != null) {
            paint.setShader(chessboardShader);
            canvas.drawRoundRect(colorRange, density * 2f, density * 2f, paint);
        }
        paint.setShader(shader);
        canvas.drawRoundRect(colorRange, density * 2f, density * 2f, paint);

        drawFinder(canvas, alphaSelection / (float) 255);
    }

    public interface OnAlphaChangedListener {
        void onAlphaChanged(int alpha);
    }
}
