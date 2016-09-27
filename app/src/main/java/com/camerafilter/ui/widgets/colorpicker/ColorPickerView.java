package com.camerafilter.ui.widgets.colorpicker;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.camerafilter.R;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class ColorPickerView extends LinearLayout implements HueView.OnHueChangedListener, AlphaView.OnAlphaChangedListener, SelectView.OnOqaqueColorChangedListener {

    @NonNull
    private final HueView hueView;
    @NonNull
    private final AlphaView alphaView;
    @NonNull
    private final SelectView selectView;

    private OnColorChanged listener;

    public ColorPickerView(Context context) {
        this(context, null);
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.imgly_widget_color_picker, this);

        hueView    = (HueView)    findViewById(R.id.color_picker_hue);
        alphaView  = (AlphaView)  findViewById(R.id.color_picker_alpha);
        selectView = (SelectView) findViewById(R.id.color_picker_select);

        hueView.setListener(this);
        alphaView.setListener(this);
        selectView.setListener(this);
    }

    public void setListener(OnColorChanged listener) {
        this.listener = listener;
    }

    public void setSelectedColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hueView.setHueSelection(hsv[0]);
        selectView.setColor(color);
        alphaView.setColor(color);
        alphaView.setAlphaSelection(255, false);
    }

    private void dispatchListener() {
        if (listener != null) {
            int opaqueColor = selectView.getColorSelection();
            int color = Color.argb(alphaView.getAlphaSelection(), Color.red(opaqueColor), Color.green(opaqueColor), Color.blue(opaqueColor));
            listener.onColorPickerSelection(color);
        }
    }

    @Override
    public void onHueChanged(float hue) {
        selectView.setHue(hue, true);
    }

    @Override
    public void onAlphaChanged(int alpha) {
        dispatchListener();
    }

    @Override
    public void onOpaqueColorChanged(int color) {
        alphaView.setColor(color);
        dispatchListener();
    }

    public interface OnColorChanged {
        void onColorPickerSelection(int color);
    }
}

