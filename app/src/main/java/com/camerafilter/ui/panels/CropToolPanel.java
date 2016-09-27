package com.camerafilter.ui.panels;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.camerafilter.R;
import com.camerafilter.sdk.configuration.AbstractConfig;
import com.camerafilter.sdk.configuration.PhotoEditorSdkConfig;
import com.camerafilter.sdk.tools.AbstractTool;
import com.camerafilter.sdk.tools.AbstractToolPanel;
import com.camerafilter.sdk.tools.CropTool;
import com.camerafilter.ui.adapter.DataSourceListAdapter;
import com.camerafilter.ui.widgets.HorizontalListView;

/**
 * Created by Le Minh An on 9/26/2016.
 */
public class
CropToolPanel extends AbstractToolPanel implements DataSourceListAdapter.OnItemClickListener<AbstractConfig.AspectConfigInterface> {

    private static final int LAYOUT = R.layout.imgly_panel_tool_crop;

    private CropTool cropTool;

    @Override
    protected int getLayoutResource() {
        return LAYOUT;
    }

    @Override
    protected void onAttached(Context context, @NonNull View panelView, AbstractTool tool) {
        super.onAttached(context, panelView, tool);

        HorizontalListView listView = (HorizontalListView) panelView.findViewById(R.id.optionList);

        this.cropTool = (CropTool) tool;

        DataSourceListAdapter listAdapter = new DataSourceListAdapter(context);
        listAdapter.setData(PhotoEditorSdkConfig.getCropConfig());
        listAdapter.setSelection(cropTool.getAspectConfig());
        listAdapter.setOnItemClickListener(this);
        listView.setAdapter(listAdapter);
    }

    @Override
    protected void onDetached() {

    }

    @Override
    public void onItemClick(AbstractConfig.AspectConfigInterface entity) {
        cropTool.setAspectConfig(entity);
    }

}
