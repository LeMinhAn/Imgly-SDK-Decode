package com.camerafilter.ui.panels;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.camerafilter.ImgLySdk;
import com.camerafilter.R;
import com.camerafilter.sdk.configuration.AbstractConfig;
import com.camerafilter.sdk.configuration.PhotoEditorSdkConfig;
import com.camerafilter.sdk.tools.AbstractTool;
import com.camerafilter.sdk.tools.AbstractToolPanel;
import com.camerafilter.sdk.tools.FilterTool;
import com.camerafilter.ui.adapter.DataSourceListAdapter;
import com.camerafilter.ui.widgets.HorizontalListView;
import com.camerafilter.ui.widgets.ImgLyFloatSlider;

/**
 * Created by Le Minh An on 9/26/2016.
 */
public class FilterToolPanel extends AbstractToolPanel implements DataSourceListAdapter.OnItemClickListener<AbstractConfig.ImageFilterInterface>, ImgLyFloatSlider.OnSeekBarChangeListener {

    private static final int INTENSITY_VALUE_STEPS = 255;
    private static final int LAYOUT = R.layout.imgly_panel_tool_filter;

    private ImgLyFloatSlider seekBar;

    private FilterTool filterTool;

    @Override
    protected int getLayoutResource() {
        return LAYOUT;
    }

    @Override
    protected void onAttached(Context context, @NonNull View panelView, AbstractTool tool) {
        super.onAttached(context, panelView, tool);
        ImgLySdk.getAnalyticsPlugin().changeScreen("FilterTool");

        HorizontalListView listView = (HorizontalListView) panelView.findViewById(R.id.optionList);

        seekBar = (ImgLyFloatSlider) panelView.findViewById(R.id.seekBar);

        this.filterTool = (FilterTool) tool;

        DataSourceListAdapter listAdapter = new DataSourceListAdapter(context);
        listAdapter.setData(PhotoEditorSdkConfig.getFilterConfig());
        listAdapter.setOnItemClickListener(this);
        listAdapter.setSelection(filterTool.getFilter());
        listView.setAdapter(listAdapter);

        seekBar.setMax(1);
        seekBar.setSteps(INTENSITY_VALUE_STEPS);
        seekBar.setValue(filterTool.getIntensity());
        seekBar.setOnSeekBarChangeListener(this);

        seekBar.setTranslationY(seekBar.getHeight());
        setSeekBarVisibility(filterTool.getFilter() != null && filterTool.getFilter().hasIntensityConfig(), true);
    }

    @Override
    protected void onDetached() {
    }

    private void setSeekBarVisibility(boolean barVisible, boolean delay) {
        AnimatorSet animator = new AnimatorSet();
        animator.playTogether(
                ObjectAnimator.ofFloat(seekBar, "alpha",        seekBar.getAlpha(), barVisible ? 1f : 0f),
                ObjectAnimator.ofFloat(seekBar, "translationY", seekBar.getTranslationY(), barVisible ? 0f : seekBar.getHeight())
        );
        if (delay) animator.setStartDelay(ANIMATION_DURATION);
        animator.start();
    }

    @Override
    public void onItemClick(@Nullable AbstractConfig.ImageFilterInterface entity) {
        filterTool.setFilter(entity);
        setSeekBarVisibility(entity != null && entity.hasIntensityConfig(), false);
    }

    @Override
    public void onProgressChanged(ImgLyFloatSlider seekBar, float value, boolean fromUser) {
        if (fromUser) {
            filterTool.setIntensity(value);
        }
    }

    @Override
    public void onStartTrackingTouch(ImgLyFloatSlider seekBar) {

    }

    @Override
    public void onStopTrackingTouch(ImgLyFloatSlider seekBar) {

    }
}
