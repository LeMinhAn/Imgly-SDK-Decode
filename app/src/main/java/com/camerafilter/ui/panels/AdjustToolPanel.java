package com.camerafilter.ui.panels;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.camerafilter.ImgLySdk;
import com.camerafilter.R;
import com.camerafilter.sdk.configuration.AbstractConfig;
import com.camerafilter.sdk.configuration.DataSourceInterface;
import com.camerafilter.sdk.tools.AbstractTool;
import com.camerafilter.sdk.tools.AbstractToolPanel;
import com.camerafilter.sdk.tools.ColorMatrixTool;
import com.camerafilter.ui.adapter.DataSourceListAdapter;
import com.camerafilter.ui.widgets.HorizontalListView;
import com.camerafilter.ui.widgets.ImgLyFloatSlider;

import java.util.ArrayList;

/**
 * Created by Le Minh An on 9/26/2016.
 */
public class AdjustToolPanel extends AbstractToolPanel implements ImgLyFloatSlider.OnSeekBarChangeListener, DataSourceListAdapter.OnItemClickListener<AdjustToolPanel.AdjustOption> {

    private enum MODE {
        NONE(-1f, 1f, 2000),
        BRIGHTNESS(-1f, 1f, 2000),
        CONTRAST(-1f, 2f, 4000),
        SATURATION(0f, 2f, 2000);

        private final float MIN_VALUE;
        private final float MAX_VALUE;
        private final int VALUE_STEPS;

        MODE(float MIN_VALUE, float MAX_VALUE, int VALUE_STEPS){
            this.MIN_VALUE = MIN_VALUE;
            this.MAX_VALUE = MAX_VALUE;
            this.VALUE_STEPS = VALUE_STEPS;
        }
    }

    private static final int LAYOUT = R.layout.imgly_panel_tool_adjust;

    private ImgLyFloatSlider seekBar;

    private ColorMatrixTool colorMatrixTool;

    @NonNull
    private MODE currentSeekMode = MODE.NONE;

    @Override
    protected int getLayoutResource() {
        return LAYOUT;
    }


    @Override
    protected void onAttached(Context context, @NonNull View panelView, AbstractTool tool) {
        super.onAttached(context, panelView, tool);
        ImgLySdk.getAnalyticsPlugin().changeScreen("AdjustTool");
        colorMatrixTool = (ColorMatrixTool) tool;


        seekBar  = (ImgLyFloatSlider)  panelView.findViewById(R.id.seekBar);
        seekBar.setAlpha(0f);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.post(new Runnable() {
            @Override
            public void run() {
                seekBar.setAlpha(0f);
                seekBar.setTranslationY(seekBar.getHeight());
            }
        });

        HorizontalListView listView = (HorizontalListView) panelView.findViewById(R.id.optionList);

        DataSourceListAdapter listAdapter = new DataSourceListAdapter(context);

        ArrayList<AbstractConfig> configs = new ArrayList<>();
        configs.add(new AdjustOption(MODE.BRIGHTNESS));
        configs.add(new AdjustOption(MODE.CONTRAST));
        configs.add(new AdjustOption(MODE.SATURATION));

        listAdapter.setData(configs);
        listAdapter.setOnItemClickListener(this);
        listView.setAdapter(listAdapter);

    }

    @Override
    protected void onDetached() {
        seekBar.setOnSeekBarChangeListener(null);
    }

    @Override
    public void onItemClick(@NonNull AdjustOption entity) {
        final boolean barVisible;
        currentSeekMode = entity.mode;

        final float value;

        switch (currentSeekMode) {
            case BRIGHTNESS:
                barVisible = true;
                value = colorMatrixTool.getBrightness();
                break;
            case CONTRAST:
                barVisible = true;
                value = colorMatrixTool.getContrast();
                break;
            case SATURATION:
                barVisible = true;
                value = colorMatrixTool.getSaturation();
                break;
            case NONE: default:
                barVisible = false;
                value = 0;
                break;
        }

        // Convert old Progress to start Animation with the new Max and Min
        float currentProgress = seekBar.getPercentageProgress();
        seekBar.setSteps(currentSeekMode.VALUE_STEPS);
        seekBar.setMin(currentSeekMode.MIN_VALUE);
        seekBar.setMax(currentSeekMode.MAX_VALUE);
        seekBar.setPercentageProgress(currentProgress);

        AnimatorSet animator = new AnimatorSet();
        animator.playTogether(
                ObjectAnimator.ofFloat(seekBar, "value",        seekBar.getValue(), value),
                ObjectAnimator.ofFloat(seekBar, "alpha",        seekBar.getAlpha(), barVisible ? 1f : 0f),
                ObjectAnimator.ofFloat(seekBar, "translationY", seekBar.getTranslationY(), barVisible ? 0f : seekBar.getHeight())
        );

        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(ANIMATION_DURATION);
        animator.start();
    }

    @Override
    public void onProgressChanged(ImgLyFloatSlider seekBar, float value, boolean fromUser) {
        if (fromUser) {
            switch (currentSeekMode) {
                case BRIGHTNESS:
                    colorMatrixTool.setBrightness(value);
                    break;
                case CONTRAST:
                    colorMatrixTool.setContrast(value);
                    break;
                case SATURATION:
                    colorMatrixTool.setSaturation(value);
                    break;
            }
        }
    }

    @Override
    public void onStartTrackingTouch(ImgLyFloatSlider seekBar) {

    }

    @Override
    public void onStopTrackingTouch(ImgLyFloatSlider seekBar) {

    }

    protected static class AdjustOption extends AbstractConfig implements DataSourceInterface<AbstractConfig.BindData> {

        @NonNull
        private final MODE mode;

        public AdjustOption(@NonNull MODE mode) {
            super(getNameRes(mode));
            this.mode = mode;
        }

        public static int getNameRes(@NonNull MODE mode) {
            switch (mode) {
                case BRIGHTNESS: return R.string.imgly_tool_name_adjust_brightness;
                case CONTRAST: return R.string.imgly_tool_name_adjust_contrast;
                case SATURATION: default: return R.string.imgly_tool_name_adjust_saturation;
            }
        }

        @Override
        public boolean hasStaticThumbnail() {
            return true;
        }

        @Override
        public int getThumbnailResId() {
            switch (mode) {
                case BRIGHTNESS: return R.drawable.imgly_icon_option_brightness;
                case CONTRAST: return R.drawable.imgly_icon_option_contrast;
                case SATURATION: default: return R.drawable.imgly_icon_option_saturation;
            }
        }

        @Override
        public int getLayout() {
            return R.layout.imgly_list_item_option_selecable;
        }

        @Override
        public boolean isSelectable() {
            return true;
        }
    }
}
