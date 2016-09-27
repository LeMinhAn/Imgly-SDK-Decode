package com.camerafilter.ui.panels;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.camerafilter.ImgLySdk;
import com.camerafilter.R;
import com.camerafilter.sdk.configuration.AbstractConfig;
import com.camerafilter.sdk.configuration.DataSourceInterface;
import com.camerafilter.sdk.configuration.Divider;
import com.camerafilter.sdk.tools.AbstractTool;
import com.camerafilter.sdk.tools.AbstractToolPanel;
import com.camerafilter.sdk.tools.StickerTool;
import com.camerafilter.ui.adapter.DataSourceListAdapter;
import com.camerafilter.ui.widgets.HorizontalListView;
import com.camerafilter.utils.SetHardwareAnimatedViews;

import java.util.ArrayList;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class StickerOptionToolPanel extends AbstractToolPanel implements DataSourceListAdapter.OnItemClickListener<StickerOptionToolPanel.StickerOption> {

    private static final int LAYOUT = R.layout.imgly_panel_tool_sticker_options;

    private enum OPTION {
        FLIP_H,
        FLIP_V,
        TO_FRONT,
        DELETE
    }

    private StickerTool.Options stickerTool;

    @Override
    protected int getLayoutResource() {
        return LAYOUT;
    }

    @Override
    protected void onAttached(Context context, @NonNull View panelView, AbstractTool tool) {

        panelView.setTranslationY(panelView.getHeight());
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(panelView, "translationY", panelView.getHeight(), 0f)
        );

        animatorSet.addListener(new SetHardwareAnimatedViews(panelView));
        animatorSet.setStartDelay(ANIMATION_DURATION / 2);
        animatorSet.setDuration(ANIMATION_DURATION);
        animatorSet.start();

        ImgLySdk.getAnalyticsPlugin().changeScreen("StickerOptionTool");

        this.stickerTool = (StickerTool.Options) tool;

        HorizontalListView listView = (HorizontalListView) panelView.findViewById(R.id.optionList);

        DataSourceListAdapter listAdapter = new DataSourceListAdapter(context);

        ArrayList<AbstractConfig> configs = new ArrayList<>();
        configs.add(new StickerOption(OPTION.FLIP_H));
        configs.add(new StickerOption(OPTION.FLIP_V));
        configs.add(new Divider());
        configs.add(new StickerOption(OPTION.TO_FRONT));
        configs.add(new StickerOption(OPTION.DELETE));

        listAdapter.setData(configs);
        listAdapter.setOnItemClickListener(this);
        listView.setAdapter(listAdapter);
    }

    @Override
    protected void onDetached() {

    }

    @Override
    public void onItemClick(@NonNull StickerOption entity) {
        switch (entity.option) {
            case DELETE:   stickerTool.deleteSticker();       break;
            case FLIP_V:   stickerTool.flipSticker(true);     break;
            case FLIP_H:   stickerTool.flipSticker(false);    break;
            case TO_FRONT: stickerTool.bringStickerToFront(); break;
        }
    }

    protected static class StickerOption extends AbstractConfig implements DataSourceInterface<AbstractConfig.BindData> {

        @NonNull
        private final OPTION option;

        public StickerOption(@NonNull OPTION option) {
            super(getNameRes(option));
            this.option = option;
        }

        public static int getNameRes(@NonNull OPTION option) {
            switch (option) {
                case FLIP_V: return R.string.imgly_sticker_option_flip_v;
                case FLIP_H: return R.string.imgly_sticker_option_flip_h;
                case DELETE: return R.string.imgly_sticker_option_delete;
                case TO_FRONT: default: return R.string.imgly_sticker_option_to_front;
            }
        }

        @Override
        public boolean hasStaticThumbnail() {
            return true;
        }

        @Override
        public int getThumbnailResId() {
            switch (option) {
                case FLIP_V: return R.drawable.imgly_icon_option_orientation_flip_v;
                case FLIP_H: return R.drawable.imgly_icon_option_orientation_flip_h;
                case DELETE: return R.drawable.imgly_icon_option_delete;
                case TO_FRONT: default: return R.drawable.imgly_icon_option_bringtofront;
            }
        }

        @Override
        public int getLayout() {
            return R.layout.imgly_list_item_option;
        }

        @Override
        public boolean isSelectable() {
            return false;
        }
    }
}
