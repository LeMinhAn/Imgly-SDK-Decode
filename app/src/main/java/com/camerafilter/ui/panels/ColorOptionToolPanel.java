package com.camerafilter.ui.panels;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.camerafilter.R;
import com.camerafilter.sdk.configuration.AbstractConfig;
import com.camerafilter.sdk.tools.AbstractColorTool;
import com.camerafilter.sdk.tools.AbstractTool;
import com.camerafilter.sdk.tools.AbstractToolPanel;
import com.camerafilter.ui.adapter.DataSourceListAdapter;
import com.camerafilter.ui.widgets.HorizontalListView;
import com.camerafilter.ui.widgets.colorpicker.ColorPickerView;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class ColorOptionToolPanel extends AbstractToolPanel implements DataSourceListAdapter.OnItemClickListener<AbstractConfig.ColorConfigInterface>, ColorPickerView.OnColorChanged {

    private static final int LAYOUT = R.layout.imgly_panel_tool_color;

    private AbstractColorTool<?> colorTool;

    private ColorPickerView colorPicker;

    @Nullable
    private AbstractConfig.ColorConfigInterface currentColorConfig = null;

    private boolean colorPickerIsVisible = false;

    private int currentColor = 0;

    private DataSourceListAdapter listAdapter;

    @Override
    protected int getLayoutResource() {
        return LAYOUT;
    }

    @Override
    protected void onAttached(Context context, @NonNull View panelView, AbstractTool tool) {
        super.onAttached(context, panelView, tool);

        HorizontalListView listView = (HorizontalListView) panelView.findViewById(R.id.optionList);

        colorPicker = (ColorPickerView) panelView.findViewById(R.id.colorPicker);
        colorPicker.setTranslationY(colorPicker.getHeight());
        colorPicker.setVisibility(View.GONE);

        colorPicker.setListener(this);

        this.colorTool = (AbstractColorTool<?>) tool;

        listAdapter = new DataSourceListAdapter(context);
        listAdapter.setData(colorTool.getColorList());
        listAdapter.setOnItemClickListener(this);

        currentColor = colorTool.getColor();
        setSelection();

        listView.setAdapter(listAdapter);
    }

    private void setSelection() {
        boolean hasSelection = false;
        for (AbstractConfig.ColorConfigInterface colorConfig : colorTool.getColorList()) {
            if (colorConfig.getColor() == currentColor) {
                hasSelection = true;
                listAdapter.setSelection(colorConfig);
                break;
            }
        }
        if (!hasSelection) {
            listAdapter.setSelection(null);
        }
    }

    @Override
    protected int onBeforeDetach(View panelView, boolean revertChanges) {
        int time = super.onBeforeDetach(panelView, revertChanges);

        return time;
    }

    @Override
    protected void onDetached() {

    }

    private void toggleColorPicker() {

        colorPickerIsVisible = !colorPickerIsVisible;

        AnimatorSet animatorSet = new AnimatorSet();

        if (colorPickerIsVisible) {
            animatorSet.playTogether(
                    ObjectAnimator.ofFloat(colorPicker, "translationY", colorPicker.getTranslationY(), 0f)
            );
        } else {
            animatorSet.playTogether(
                    ObjectAnimator.ofFloat(colorPicker, "translationY", colorPicker.getTranslationY(), colorPicker.getHeight())
            );
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    colorPicker.setVisibility(View.GONE);
                }
            });
        }
        colorPicker.setVisibility(View.VISIBLE);
        animatorSet.setDuration(ANIMATION_DURATION);
        animatorSet.start();
    }

    @Override
    public void onItemClick(@NonNull AbstractConfig.ColorConfigInterface entity) {
        if (entity.equals(currentColorConfig)) {
            toggleColorPicker();
        } else {
            currentColorConfig = entity;
            currentColor = entity.getColor();
            colorTool.setColor(currentColor);
            colorPicker.setSelectedColor(currentColor);
        }
    }

    @Override
    public void onColorPickerSelection(int color) {
        colorTool.setColor(color);
        currentColor = color;
    }
}
