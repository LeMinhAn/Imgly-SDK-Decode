package com.camerafilter.sdk.configuration;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.camerafilter.R;
import com.camerafilter.sdk.gles.Texture;
import com.camerafilter.sdk.operator.AbstractOperation;
import com.camerafilter.sdk.operator.Operator;
import com.camerafilter.ui.adapter.DataSourceListAdapter;
import com.camerafilter.sdk.views.EditorPreview;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class Divider extends AbstractConfig implements AbstractConfig.FontConfigInterface<AbstractConfig.BindData>, AbstractConfig.ToolConfigInterface, AbstractConfig.AspectConfigInterface, AbstractConfig.ImageFilterInterface, AbstractConfig.StickerConfigInterface {
    public Divider() {
        super("");
    }

    @Override
    public int getLayout() {
        return R.layout.imgly_list_item_devider;
    }


    @Override
    public float getAspect() {
        return 0;
    }

    @Override
    public boolean hasSpecificSize() {
        return false;
    }

    @Override
    public int getCropWidth() {
        return 0;
    }

    @Override
    public int getCropHeight() {
        return 0;
    }

    @Nullable
    @Override
    public Typeface getTypeface() {
        return null;
    }

    @Override
    public int getVerticalLayout() {
        return 0;
    }

    @Nullable
    @Override
    public Bitmap renderImage(Bitmap bitmap) {
        return null;
    }

    @Nullable
    @Override
    public Bitmap renderImage(Bitmap bitmap, float intensity) {
        return null;
    }

    @Override
    public void release() {

    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    @Override
    public void draw(Texture texture, float[] mvpMatrix, float[] stMatrix, float aspectRatio) {

    }

    @Override
    public boolean isClickable() {
        return false;
    }

    @Override
    public boolean hasIntensityConfig() {
        return false;
    }

    @Nullable
    @Override
    public STICKER_TYPE getType() {
        return null;
    }


    @Override
    public int getStickerId() {
        return 0;
    }

    @Nullable
    @Override
    public AbstractOperation getOperation() {
        return null;
    }

    @Override
    public boolean isRevertible() {
        return false;
    }

    @Nullable
    @Override
    public Operator getOperator() {
        return null;
    }

    @Override
    public void revertChanges() {

    }

    @Nullable
    @Override
    public EditorPreview getEditorPreview() {
        return null;
    }

    @Override
    public void detachPanel(boolean revertChanges) {

    }

    @Override
    public void refreshPanel() {

    }

    @NonNull
    @Override
    public DataSourceListAdapter.DataSourceViewHolder createViewHolder(@NonNull View view, boolean useVerticalLayout) {
        return new DividerViewHolder(view);
    }

    private static class DividerViewHolder extends DataSourceListAdapter.DataSourceViewHolder {

        public DividerViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        protected void bind(Object o) {

        }

        @Override
        public void setSelectedState(boolean selected) {

        }

    }
}
