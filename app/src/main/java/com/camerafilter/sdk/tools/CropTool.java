package com.camerafilter.sdk.tools;

import android.graphics.RectF;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.ViewGroup;

import com.camerafilter.sdk.configuration.AbstractConfig;
import com.camerafilter.sdk.operator.AbstractOperation;
import com.camerafilter.sdk.operator.CropOperation;
import com.camerafilter.ui.panels.CropToolPanel;
import com.camerafilter.sdk.views.EditorPreview;

/**
 * Created by Le Minh An on 9/26/2016.
 */
public class CropTool extends AbstractTool {

    private CropOperation cropOperation;

    @Override
    public AbstractOperation getOperation() {
        return cropOperation;
    }

    @Override
    public boolean isRevertible() {
        return true;
    }

    public final SaveState saveState = new SaveState();
    public class SaveState {
        @Nullable
        RectF cropRect = null;
    }


    @Override
    protected void saveState() {
        EditorPreview editorPreview = getEditorPreview();
        saveState.cropRect = editorPreview.getCropRectState();
    }

    @Override
    protected void revertState() {
        EditorPreview editorPreview = getEditorPreview();
        editorPreview.restoreCropRectState(saveState.cropRect);
    }

    public CropTool(@StringRes int name, @DrawableRes int drawableId) {
        super(name, drawableId, CropToolPanel.class);
    }

    public CropTool(@StringRes int name, @DrawableRes int drawableId, @NonNull Class<? extends AbstractToolPanel> panelClass) {
        super(name, drawableId, panelClass);
    }

    public AspectConfigInterface getAspectConfig() {
        return getEditorPreview().getCurrentRotationBasedAspect();
    }

    public void setAspectConfig(AbstractConfig.AspectConfigInterface aspect) {
        EditorPreview editorPreview = getEditorPreview();
        editorPreview.setAspectRatio(aspect);
    }

    @Override
    public View attachPanel(@NonNull ViewGroup parentView, @NonNull EditorPreview preview) {
        View view = super.attachPanel(parentView, preview);

        this.cropOperation = getOperator().getCropOperation();
        preview.enableCropMode(true);
        saveState();
        return view;
    }

    @Override
    public void detachPanel(boolean revertChanges) {
        super.detachPanel(revertChanges);
        EditorPreview preview = getEditorPreview();
        preview.enableCropMode(false);
    }

}
