package com.camerafilter.sdk.tools;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.ViewGroup;

import com.camerafilter.sdk.operator.AbstractOperation;
import com.camerafilter.sdk.operator.ColorMatrixOperation;
import com.camerafilter.ui.panels.AdjustToolPanel;
import com.camerafilter.sdk.views.EditorPreview;

/**
 * Created by Le Minh An on 9/26/2016.
 */
public class ColorMatrixTool extends AbstractTool {

    private ColorMatrixOperation operation;

    @Override
    public AbstractOperation getOperation() {
        return operation;
    }

    @Override
    public boolean isRevertible() {
        return true;
    }

    @NonNull
    public SaveState saveState = new SaveState();
    public class SaveState {
        float contrast = 0;
        float brightness = 0;
        float saturation = 0;
    }

    @Override
    protected void saveState() {
        saveState = new SaveState();
        saveState.contrast   = operation.getContrast();
        saveState.brightness = operation.getBrightness();
        saveState.saturation = operation.getSaturation();
    }

    @Override
    protected void revertState() {
        operation.setContrast(saveState.contrast);
        operation.setBrightness(saveState.brightness);
        operation.setSaturation(saveState.saturation);
        invalidate();
    }

    public ColorMatrixTool(@StringRes int name, @DrawableRes int drawableId) {
        super(name, drawableId, AdjustToolPanel.class);
    }

    public ColorMatrixTool(@StringRes int name, @DrawableRes int drawableId, @NonNull Class<? extends AbstractToolPanel> panelClass) {
        super(name, drawableId, panelClass);
    }

    @Override
    public View attachPanel(@NonNull ViewGroup parentView, @NonNull EditorPreview preview) {
        View view = super.attachPanel(parentView, preview);

        this.operation = getOperator().getColorMatrixOperation();
        saveState();
        return view;
    }

    @Override
    public void detachPanel(boolean revertChanges) {
        super.detachPanel(revertChanges);
    }

    public void invalidate() {
        operation.doOperation(new AbstractOperation.OperationDoneListener() {
            @Override
            public void operationDone(AbstractOperation operation) {

            }
        });
    }

    public float getSaturation(){
        return operation.getSaturation();
    }

    public void setSaturation(float saturation) {
        // TODO: Refactor
        operation.setSaturation(saturation);
        invalidate();
    }

    public float getBrightness() {
        return operation.getBrightness();
    }

    public void setBrightness(float brightness) {
        operation.setBrightness(brightness);
        invalidate();
    }

    public float getContrast() {
        return operation.getContrast();
    }

    public void setContrast(float contrast) {
        operation.setContrast(contrast);
        invalidate();
    }
}

