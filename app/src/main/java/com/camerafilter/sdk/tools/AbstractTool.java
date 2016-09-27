package com.camerafilter.sdk.tools;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.ViewGroup;

import com.camerafilter.ImgLySdk;
import com.camerafilter.R;
import com.camerafilter.sdk.configuration.AbstractConfig;
import com.camerafilter.sdk.operator.AbstractOperation;
import com.camerafilter.sdk.operator.Operator;
import com.camerafilter.sdk.views.EditorPreview;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public abstract class AbstractTool extends AbstractConfig implements AbstractConfig.ToolConfigInterface {

    @NonNull
    public final Class<? extends AbstractToolPanel> panelClass;

    private AbstractToolPanel panel;
    private EditorPreview editorPreview;

    @Nullable
    public abstract AbstractOperation getOperation();

    public abstract boolean isRevertible();

    protected void saveState() {
        ImgLySdk.getAnalyticsPlugin().sendEvent("Interface", "Save state");

    }
    protected void revertState() {
        ImgLySdk.getAnalyticsPlugin().sendEvent("Interface", "Revert state");
    }

    public AbstractTool(@StringRes int name, @DrawableRes int drawableId, @NonNull Class<? extends AbstractToolPanel> panelClass) {
        super(name, drawableId);
        this.panelClass = panelClass;
    }

    public void refreshPanel() {
        if (panel != null) {
            panel.refresh();
        }
    }

    public boolean isAttached() {
        return panel != null && panel.isAttached();
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    public View attachPanel(@NonNull ViewGroup parentView, @NonNull EditorPreview preview) {
        this.editorPreview = preview;
        if (panel == null) {
            try {
                panel = panelClass.newInstance();
                panel.init(this);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return panel.attach(parentView);
    }

    public int getVerticalLayout() {
        return 0;
    }

    public Operator getOperator() {
        return getEditorPreview().getOperator();
    }

    public void revertChanges() {
        revertState();
    }

    @NonNull
    public EditorPreview getEditorPreview() {
        return editorPreview;
    }

    public void detachPanel(boolean revertChanges) {
        panel.detach(revertChanges);
    }

    public static int convertFromRange(float value, float minValue, float maxValue, int steps){

        float rangeValue = (Math.min(Math.max(value, minValue), maxValue) - minValue) / (maxValue - minValue); // Hold in Range an convert t 0.0 - 1.0 range

        return Math.round((rangeValue) * (steps));
    }

    public static float convertToRange(int value, float minValue, float maxValue, int steps){

        float rangeValue = Math.min(Math.max(value, 0), steps) / (float) steps; // Hold in Range an slide to Zero

        return (rangeValue * (maxValue - minValue)) + minValue;
    }

    @Override
    public int getLayout() {
        return R.layout.imgly_list_item_tool;
    }
}
