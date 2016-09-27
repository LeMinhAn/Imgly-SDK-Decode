package com.camerafilter.ui.panels;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.camerafilter.ImgLySdk;
import com.camerafilter.R;
import com.camerafilter.sdk.configuration.AbstractConfig;
import com.camerafilter.sdk.configuration.ImageStickerConfig;
import com.camerafilter.sdk.configuration.PhotoEditorSdkConfig;
import com.camerafilter.sdk.configuration.TextStickerConfig;
import com.camerafilter.sdk.tools.AbstractTool;
import com.camerafilter.sdk.tools.AbstractToolPanel;
import com.camerafilter.sdk.tools.StickerTool;
import com.camerafilter.ui.adapter.DataSourceListAdapter;
import com.camerafilter.ui.widgets.HorizontalListView;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class StickerToolPanel extends AbstractToolPanel implements DataSourceListAdapter.OnItemClickListener<AbstractConfig.StickerConfigInterface> {

    private static final int LAYOUT = R.layout.imgly_panel_tool_sticker;

    private StickerTool stickerTool;

    @Override
    protected int getLayoutResource() {
        return LAYOUT;
    }

    @Override
    protected void onAttached(Context context, @NonNull View panelView, AbstractTool tool) {
        super.onAttached(context, panelView, tool);
        ImgLySdk.getAnalyticsPlugin().changeScreen("StickerTool");

        HorizontalListView listView = (HorizontalListView) panelView.findViewById(R.id.optionList);

        this.stickerTool = (StickerTool) tool;

        DataSourceListAdapter listAdapter = new DataSourceListAdapter(context);
        listAdapter.setData(PhotoEditorSdkConfig.getStickerConfig());
        listAdapter.setOnItemClickListener(this);
        listView.setAdapter(listAdapter);
    }

    @Override
    protected void onDetached() {

    }

    @Override
    public void onItemClick(@NonNull AbstractConfig.StickerConfigInterface entity) {
        ImgLySdk.getAnalyticsPlugin().sendEvent("StickerEdit", "Sticker add", entity.getName());
        if (entity instanceof ImageStickerConfig) {
            stickerTool.addSticker((ImageStickerConfig) entity);
        } else {
            stickerTool.addSticker((TextStickerConfig) entity);
        }
    }
}
