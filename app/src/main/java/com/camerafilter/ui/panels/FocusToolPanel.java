package com.camerafilter.ui.panels;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.camerafilter.sdk.configuration.DataSourceInterface;
import com.camerafilter.R;
import com.camerafilter.sdk.configuration.AbstractConfig;
import com.camerafilter.sdk.tools.AbstractTool;
import com.camerafilter.sdk.tools.AbstractToolPanel;
import com.camerafilter.sdk.tools.FocusTool;
import com.camerafilter.ui.adapter.DataSourceListAdapter;
import com.camerafilter.ui.widgets.HorizontalListView;
import com.camerafilter.ui.widgets.ImgLyFloatSlider;

import java.util.ArrayList;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class FocusToolPanel extends AbstractToolPanel implements ImgLyFloatSlider.OnSeekBarChangeListener, DataSourceListAdapter.OnItemClickListener<FocusToolPanel.FocusOption> {

    private static final int LAYOUT = R.layout.imgly_panel_tool_focus;

    private static final float MIN_VALUE = 0f;
    private static final float MAX_VALUE = 1f;

    private static final int VALUE_STEPS = 200;

    private ImgLyFloatSlider seekBar;

    private FocusTool focusTool;

    @Override
    protected int getLayoutResource() {
        return LAYOUT;
    }

    @Override
    protected void onAttached(Context context, @NonNull View panelView, AbstractTool tool) {
        super.onAttached(context, panelView, tool);

        focusTool = (FocusTool) tool;

        seekBar = (ImgLyFloatSlider) panelView.findViewById(R.id.seekBar);

        if (focusTool.getFocusMode() == FocusTool.MODE.NO_FOCUS) {
            seekBar.setAlpha(0f);
            seekBar.post(new Runnable() {
                @Override
                public void run() {
                    seekBar.setTranslationY(seekBar.getHeight());
                }
            });
        }

        seekBar.setMin(MIN_VALUE);
        seekBar.setMax(MAX_VALUE);
        seekBar.setSteps(VALUE_STEPS);
        seekBar.setValue(focusTool.getIntensity());
        seekBar.setOnSeekBarChangeListener(this);

        HorizontalListView listView = (HorizontalListView) panelView.findViewById(R.id.optionList);

        DataSourceListAdapter listAdapter = new DataSourceListAdapter(context);

        ArrayList<FocusOption> configs = new ArrayList<>();
        configs.add(new FocusOption(FocusTool.MODE.NO_FOCUS));
        configs.add(new FocusOption(FocusTool.MODE.LINEAR));
        configs.add(new FocusOption(FocusTool.MODE.RADIAL));

        listAdapter.setData(configs);
        listAdapter.setOnItemClickListener(this);
        listAdapter.setSelection(configs.get(0));
        listView.setAdapter(listAdapter);

        setSeekBarVisibility(FocusTool.MODE.NO_FOCUS != focusTool.getFocusMode(), true);
    }


    @Override
    protected void onDetached() {
        seekBar.setOnSeekBarChangeListener(null);
    }

    @Override
    public void onItemClick(@NonNull FocusOption entity) {
        focusTool.setFocusMode(entity.mode);

        setSeekBarVisibility(!entity.mode.equals(FocusTool.MODE.NO_FOCUS), false);
    }


    private void setSeekBarVisibility(boolean barVisible, boolean delay) {
        AnimatorSet animator = new AnimatorSet();
        animator.playTogether(
                ObjectAnimator.ofFloat(seekBar, "alpha",        seekBar.getAlpha(), barVisible ? 1f : 0f),
                ObjectAnimator.ofFloat(seekBar, "translationY", seekBar.getTranslationY(), barVisible ? 0f : seekBar.getHeight())
        );
        if (delay) animator.setStartDelay(ANIMATION_DURATION);
        animator.setDuration(ANIMATION_DURATION);
        animator.start();
    }

    @Override
    public void onProgressChanged(ImgLyFloatSlider seekBar, float value, boolean fromUser) {
        focusTool.setIntensity(value);
    }

    @Override
    public void onStartTrackingTouch(ImgLyFloatSlider seekBar) {

    }

    @Override
    public void onStopTrackingTouch(ImgLyFloatSlider seekBar) {

    }

    static class FocusOption extends AbstractConfig implements DataSourceInterface<AbstractConfig.BindData> {

        @NonNull
        private final FocusTool.MODE mode;

        FocusOption(@NonNull FocusTool.MODE mode) {
            super(getNameRes(mode));
            this.mode = mode;
        }

        static int getNameRes(@NonNull FocusTool.MODE mode) {
            switch (mode) {
                case NO_FOCUS: return R.string.imgly_focus_name_none;
                case RADIAL:   return R.string.imgly_focus_name_radial;
                case LINEAR: default: return R.string.imgly_focus_name_linear;
            }
        }

        @Override
        public boolean hasStaticThumbnail() {
            return true;
        }

        @Override
        public int getThumbnailResId() {
            switch (mode) {
                case NO_FOCUS: return R.drawable.imgly_icon_option_focus_off;
                case RADIAL:   return R.drawable.imgly_icon_option_focus_radial;
                case LINEAR: default: return R.drawable.imgly_icon_option_focus_linear;
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

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FocusOption that = (FocusOption) o;

            return mode == that.mode;
        }

        @Override
        public int hashCode() {
            return mode.hashCode();
        }
    }
}
