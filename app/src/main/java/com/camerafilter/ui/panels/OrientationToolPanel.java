package com.camerafilter.ui.panels;

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
import com.camerafilter.sdk.tools.OrientationTool;
import com.camerafilter.ui.adapter.DataSourceListAdapter;
import com.camerafilter.ui.widgets.HorizontalListView;

import java.util.ArrayList;

/**
 * Created by Le Minh An on 9/26/2016.
 */
public class OrientationToolPanel extends AbstractToolPanel implements DataSourceListAdapter.OnItemClickListener<OrientationToolPanel.OrientationOption> {

    private static final int LAYOUT = R.layout.imgly_panel_tool_rotate;

    private OrientationTool orientationTool;

    private enum OPTION {
        ROTATE_CW,
        ROTATE_CCW,
        FLIP_V,
        FLIP_H
    }

    @Override
    protected int getLayoutResource() {
        return LAYOUT;
    }

    @Override
    protected void onAttached(Context context, @NonNull View panelView, AbstractTool tool) {
        super.onAttached(context, panelView, tool);
        ImgLySdk.getAnalyticsPlugin().changeScreen("RotationTool");

        this.orientationTool = (OrientationTool) tool;

        HorizontalListView listView = (HorizontalListView) panelView.findViewById(R.id.optionList);

        DataSourceListAdapter listAdapter = new DataSourceListAdapter(context);

        ArrayList<AbstractConfig> configs = new ArrayList<>();
        configs.add(new OrientationOption(OPTION.ROTATE_CCW));
        configs.add(new OrientationOption(OPTION.ROTATE_CW));
        configs.add(new Divider());
        configs.add(new OrientationOption(OPTION.FLIP_H));
        configs.add(new OrientationOption(OPTION.FLIP_V));

        listAdapter.setData(configs);
        listAdapter.setOnItemClickListener(this);
        listView.setAdapter(listAdapter);


    }

    @Override
    protected void onDetached() {

    }

    public void rotateCW() {
        orientationTool.rotateCW();
        ImgLySdk.getAnalyticsPlugin().sendEvent("ImageEdit", "Rotate", "CW");
    }

    public void rotateCCW() {
        orientationTool.rotateCCW();
        ImgLySdk.getAnalyticsPlugin().sendEvent("ImageEdit", "Rotate", "CCW");
    }

    public void toogleFlipHorizontal() {
        orientationTool.toogleFlipHorizontal();
        ImgLySdk.getAnalyticsPlugin().sendEvent("ImageEdit", "Rotate", "Flip horizontal");
    }

    public void toogleFlipVertical() {
        orientationTool.toogleFlipVertical();
        ImgLySdk.getAnalyticsPlugin().sendEvent("ImageEdit", "Rotate", "Flip vertical");
    }

    @Override
    public void onItemClick(@NonNull OrientationOption entity) {
        switch (entity.option) {
            case ROTATE_CW: rotateCW(); break;
            case ROTATE_CCW: rotateCCW(); break;
            case FLIP_V: toogleFlipVertical(); break;
            case FLIP_H: toogleFlipHorizontal(); break;
        }
    }

    protected static class OrientationOption extends AbstractConfig implements DataSourceInterface<AbstractConfig.BindData> {

        @NonNull
        private final OPTION option;

        public OrientationOption(@NonNull OPTION option) {
            super(getNameRes(option));
            this.option = option;
        }

        public static int getNameRes(@NonNull OPTION option) {
            switch (option) {
                case ROTATE_CW: return R.string.imgly_orientation_name_cw;
                case ROTATE_CCW: return R.string.imgly_orientation_name_ccw;
                case FLIP_V: return R.string.imgly_orientation_name_flip_v;
                case FLIP_H: default: return R.string.imgly_orientation_name_flip_h;
            }
        }

        @Override
        public boolean hasStaticThumbnail() {
            return true;
        }

        @Override
        public int getThumbnailResId() {
            switch (option) {
                case ROTATE_CW: return R.drawable.imgly_icon_option_orientation_rotate_l;
                case ROTATE_CCW: return R.drawable.imgly_icon_option_orientation_rotate_r;
                case FLIP_V: return R.drawable.imgly_icon_option_orientation_flip_v;
                case FLIP_H: default: return R.drawable.imgly_icon_option_orientation_flip_h;
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
