package com.camerafilter.sdk.tools;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.camerafilter.R;
import com.camerafilter.sdk.operator.AbstractOperation;
import com.camerafilter.ui.panels.ColorOptionToolPanel;

import java.util.List;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public abstract class AbstractColorTool<T> extends AbstractTool {

    private final OnColorSelected<T> listener;
    private final T type;
    private int color;

    private final SaveState saveState = new SaveState();
    private class SaveState {
        int color = 0;
    }

    @Override
    protected void saveState() {
        saveState.color = color;
    }

    @Override
    protected void revertState() {
        setColor(saveState.color);
    }

    AbstractColorTool(@StringRes int name, T type, int color, OnColorSelected<T> listener) {
        super(name, R.drawable.imgly_icon_tool_text, ColorOptionToolPanel.class);
        this.listener = listener;
        this.type  = type;
        this.color = color;
        saveState();
    }

    public void setColor(int color) {
        this.color = color;
        listener.setColor(color, type);
    }

    public int getColor() {
        return color;
    }

    @NonNull
    public abstract List<? extends ColorConfigInterface> getColorList();

    @Override
    public void detachPanel(boolean revertChanges) {
        super.detachPanel(revertChanges);
    }

    @Nullable
    @Override
    public AbstractOperation getOperation() {
        return null;
    }

    @Override
    public boolean isRevertible() {
        return true;
    }

    public interface OnColorSelected<T> {
        void setColor(int color, T type);
    }
}

