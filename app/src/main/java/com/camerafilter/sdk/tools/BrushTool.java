package com.camerafilter.sdk.tools;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.ViewGroup;

import com.camerafilter.R;
import com.camerafilter.sdk.brush.models.Painting;
import com.camerafilter.sdk.configuration.PhotoEditorSdkConfig;
import com.camerafilter.sdk.operator.AbstractOperation;
import com.camerafilter.sdk.operator.StickerOperation;
import com.camerafilter.ui.panels.AdjustToolPanel;
import com.camerafilter.sdk.views.EditorPreview;

import java.util.List;

/**
 * Created by Le Minh An on 9/26/2016.
 */
public class BrushTool extends AbstractTool {

    StickerOperation operation;

    public enum COLOR_TYPE {
        BRUSH_COLOR
    }

    @Nullable
    @Override
    public AbstractOperation getOperation() {
        return operation;
    }

    @Override
    public boolean isRevertible() {
        return false;
    }


    @Override
    protected void saveState() {

    }

    @Override
    protected void revertState() {

    }

    public int getBrushColor() {
        return getPainting().color;
    }

    public void setBrushColor(int brushColor) {
        getPainting().color = brushColor;
    }

    public float getBrushSize() {
        return getPainting().size;
    }

    public void setBrushSize(float brushSize) {
        getPainting().size = brushSize;
    }

    public float getBrushHardness() {
        return getPainting().hardness;
    }

    public void setBrushHardness(float brushHardness) {
        getPainting().hardness = brushHardness;
    }

    public Painting getPainting() {
        return getEditorPreview().getPainting();
    }

    public BrushTool(@StringRes int name, @DrawableRes int drawableId) {
        this(name, drawableId, AdjustToolPanel.class);
    }

    public BrushTool(@StringRes int name, @DrawableRes int drawableId, @NonNull Class<? extends AbstractToolPanel> panelClass) {
        super(name, drawableId, panelClass);
    }

    public void openColorSelection(COLOR_TYPE type, int currentColor, AbstractColorTool.OnColorSelected<COLOR_TYPE> listener){
        getEditorPreview().dispatchEnterToolMode(new ColorSelection(type, currentColor,  listener), false);
    }

    public void goBackwards() {
        getPainting().goBackwards();
    }

    @Override
    public View attachPanel(@NonNull ViewGroup parentView, @NonNull EditorPreview preview) {
        View view = super.attachPanel(parentView, preview);

        operation = getOperator().getStickerOperation();

        preview.enableBrushMode(true);
        saveState();
        return view;
    }

    @Override
    public void detachPanel(boolean revertChanges) {
        super.detachPanel(revertChanges);
        EditorPreview preview = getEditorPreview();
        preview.enableBrushMode(false);
    }

    private static class ColorSelection extends AbstractColorTool<COLOR_TYPE> {
        ColorSelection(COLOR_TYPE type, int color, OnColorSelected<COLOR_TYPE> listener) {
            super(R.string.imgly_tool_name_text_color, type, color, listener);
        }

        @NonNull
        @Override
        public List<? extends ColorConfigInterface> getColorList() {
            return PhotoEditorSdkConfig.getBrushColors();
        }
    }

}

