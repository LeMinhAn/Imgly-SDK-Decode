package com.camerafilter.ui.widgets.colorpicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import com.camerafilter.utils.ThreadUtils;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class HueView extends AbstractSliderView {

    final int MAX_HUE = 360;

    @NonNull
    private final Paint paint;
    private Shader shader;

    private float hueSelection = 0;

    private OnHueChangedListener listener;

    public HueView(Context context) {
        this(context, null);
    }

    public HueView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HueView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        setWillNotDraw(false);
    }

    public void setListener(OnHueChangedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onProgressChange(float progress) {
        setHueSelection(MAX_HUE - progress * MAX_HUE, true);
    }

    private void createResources() {
        float[] stops = new float[MAX_HUE + 1];
        int[]  colors = new int[stops.length];

        for (int hue = 0; hue <= MAX_HUE; hue++) {
            stops [hue] = hue / (float) MAX_HUE;
            colors[hue] = Color.HSVToColor(new float[]{MAX_HUE - hue, 1, 1});
        }

        //noinspection SuspiciousNameCombination
        shader = new LinearGradient(colorRange.top, colorRange.left, colorRange.top, colorRange.bottom,  colors, stops, Shader.TileMode.CLAMP);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        createResources();
    }

    private void setHueSelection(float hue, boolean triggerListener) {

        this.hueSelection = hue;

        if (ThreadUtils.thisIsUiThread()) {
            invalidate();
        } else {
            postInvalidate();
        }

        if (listener != null && triggerListener) {
            listener.onHueChanged(this.hueSelection);
        }
    }

    public void setHueSelection(float hue) {
        setHueSelection(hue, false);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        stage.set(0, 0, getWidth(), getHeight());

        paint.setShader(shader);
        canvas.drawRoundRect(colorRange, density * 2f, density * 2f, paint);

        drawFinder(canvas, (MAX_HUE - hueSelection) / (float) MAX_HUE);
    }

    public interface OnHueChangedListener {
        void onHueChanged(float hue);
    }
}

