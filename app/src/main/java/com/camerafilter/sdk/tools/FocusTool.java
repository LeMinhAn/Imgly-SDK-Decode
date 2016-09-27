package com.camerafilter.sdk.tools;

import android.graphics.RectF;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.ViewGroup;

import com.camerafilter.sdk.operator.AbstractOperation;
import com.camerafilter.sdk.operator.FocusOperation;
import com.camerafilter.ui.panels.FocusToolPanel;
import com.camerafilter.sdk.views.EditorPreview;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class FocusTool extends AbstractTool {

    public enum MODE {
        NO_FOCUS,
        RADIAL,
        LINEAR
    }

    private FocusOperation focusOperation;
    @Nullable
    private MODE focusType = null;

    @Override
    public AbstractOperation getOperation() {
        return focusOperation;
    }

    @Override
    public boolean isRevertible() {
        return true;
    }

    public final SaveState saveState = new SaveState();
    public class SaveState {
        @Nullable
        RectF blurRect  = null;
        float intensity = 0;
    }

    /**
     * Set blur intensity.
     * @param intensity intensity from 0.0 - 1.0.
     */
    public void setIntensity(float intensity) {
        focusOperation.setIntensity(intensity);
    }

    /**
     * Get blur intensity.
     * @return intensity from 0.0 - 1.0.
     */
    public float getIntensity() {
        return focusOperation.getIntensity();
    }

    @Override
    protected void saveState() {
        EditorPreview editorPreview = getEditorPreview();
        saveState.intensity = focusOperation.getIntensity();
        //TODO: saveState.blurRect = editorPreview.getCropRectState();
    }

    @Override
    protected void revertState() {
        EditorPreview editorPreview = getEditorPreview();
        focusOperation.setIntensity(saveState.intensity);
        //TODO: editorPreview.restoreCropRectState(saveState.blurRect);
    }

    public FocusTool(@StringRes int name, @DrawableRes int drawableId) {
        super(name, drawableId, FocusToolPanel.class);
    }

    public FocusTool(@StringRes int name, @DrawableRes int drawableId, @NonNull Class<? extends AbstractToolPanel> panelClass) {
        super(name, drawableId, panelClass);
    }

    @Nullable
    public MODE getFocusMode() {
        return focusType;
    }

    public void setFocusMode(MODE type){
        focusType = type;
        focusOperation.setFocusMode(type);
        getEditorPreview().setFocusType(type);
    }

    @Override
    public View attachPanel(@NonNull ViewGroup parentView, @NonNull EditorPreview preview) {
        View view = super.attachPanel(parentView, preview);

        this.focusOperation = getOperator().getFocusOperation();
        preview.enableFocusMode(true);
        saveState();
        if (focusType == null){
            setFocusMode(MODE.NO_FOCUS);
        }
        return view;
    }

    @Override
    public void detachPanel(boolean revertChanges) {
        super.detachPanel(revertChanges);
        EditorPreview preview = getEditorPreview();
        preview.enableFocusMode(false);
    }

}
