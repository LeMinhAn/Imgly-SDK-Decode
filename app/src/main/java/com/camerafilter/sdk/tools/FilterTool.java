package com.camerafilter.sdk.tools;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.ViewGroup;

import com.camerafilter.sdk.operator.AbstractOperation;
import com.camerafilter.sdk.operator.FilterOperation;
import com.camerafilter.ui.panels.FilterToolPanel;
import com.camerafilter.sdk.views.EditorPreview;

/**
 * Created by Le Minh An on 9/26/2016.
 */
public class FilterTool extends AbstractTool {

    private FilterOperation filterOperation;

    @Override
    public AbstractOperation getOperation() {
        return filterOperation;
    }

    @Override
    public boolean isRevertible() {
        return true;
    }

    public final SaveState saveState = new SaveState();
    public class SaveState {
        @Nullable
        ImageFilterInterface filter = null;
    }

    @Override
    protected void saveState() {
        saveState.filter = filterOperation.getFilter();
    }

    @Override
    protected void revertState() {
        if (saveState.filter != null) {
            setFilter(saveState.filter);
        }
    }

    public FilterTool(@StringRes int name, @DrawableRes int drawableId) {
        super(name, drawableId, FilterToolPanel.class);
    }

    public FilterTool(@StringRes int name, @DrawableRes int drawableId, @NonNull Class<? extends AbstractToolPanel> panelClass) {
        super(name, drawableId, panelClass);
    }

    @Override
    public View attachPanel(@NonNull ViewGroup parentView, @NonNull EditorPreview preview) {
        View view = super.attachPanel(parentView, preview);

        this.filterOperation = getOperator().getFilterOperation();
        saveState();
        return view;
    }

    public void setFilter(ImageFilterInterface filter) {
        filterOperation.setFilter(filter);
    }

    public ImageFilterInterface getFilter() {
        return filterOperation.getFilter();
    }

    public void setIntensity(float intensity) {
        filterOperation.setIntensity(intensity);
    }

    public float getIntensity() {
        return filterOperation.getIntensity();
    }

}

