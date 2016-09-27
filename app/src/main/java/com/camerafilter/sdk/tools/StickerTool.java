package com.camerafilter.sdk.tools;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.ViewGroup;

import com.camerafilter.R;
import com.camerafilter.sdk.configuration.ImageStickerConfig;
import com.camerafilter.sdk.configuration.TextStickerConfig;
import com.camerafilter.sdk.operator.AbstractOperation;
import com.camerafilter.sdk.operator.StickerOperation;
import com.camerafilter.ui.panels.StickerOptionToolPanel;
import com.camerafilter.ui.panels.StickerToolPanel;
import com.camerafilter.sdk.views.EditorPreview;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class StickerTool extends AbstractTool {

    private StickerOperation stickerOperation;

    private static boolean stickerListMode = false;

    @Override
    public AbstractOperation getOperation() {
        return stickerOperation;
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

    public StickerTool(@StringRes int name, @DrawableRes int drawableId) {
        super(name, drawableId, StickerToolPanel.class);
    }

    public StickerTool(@StringRes int name, @DrawableRes int drawableId, @NonNull Class<? extends AbstractToolPanel> panelClass) {
        super(name, drawableId, panelClass);
    }

    @Override
    public View attachPanel(@NonNull ViewGroup parentView, @NonNull EditorPreview preview) {
        View view = super.attachPanel(parentView, preview);

        this.stickerOperation = getOperator().getStickerOperation();
        saveState();
        stickerListMode = true;

        return view;
    }

    @Override
    public void detachPanel(boolean revertChanges) {
        super.detachPanel(revertChanges);
        stickerListMode = false;
        if (!(this instanceof TextTool) || (this instanceof TextTool.Options)) {
            getEditorPreview().leaveSticker();
        }
    }

    public void addSticker(ImageStickerConfig config){
        EditorPreview preview = getEditorPreview();
        preview.addSticker(config);

    }

    public void addSticker(TextStickerConfig config){
        EditorPreview preview = getEditorPreview();
        preview.addSticker(config);
    }

    public void getCurrentTextConfig(TextStickerConfig config) {
        EditorPreview preview = getEditorPreview();
        preview.refreshSticker(config);
    }

    public void refreshConfig(TextStickerConfig config) {
        EditorPreview preview = getEditorPreview();
        preview.refreshSticker(config);
    }

    public void flipSticker(boolean vertical) {
        EditorPreview preview = getEditorPreview();
        preview.flipSticker(vertical);
    }

    public void deleteSticker() {
        EditorPreview preview = getEditorPreview();
        preview.deleteSticker();
    }

    public void bringStickerToFront() {
        EditorPreview preview = getEditorPreview();
        preview.bringStickerToFront();
    }

    public static class Options extends StickerTool {
        public Options() {
            super(R.string.imgly_tool_name_sticker_options, R.drawable.imgly_icon_tool_sticker, StickerOptionToolPanel.class);
        }

        @Override
        public View attachPanel(@NonNull ViewGroup parentView, @NonNull EditorPreview preview) {
            View view = super.attachPanel(parentView, preview);
            stickerListMode = true;
            return view;
        }

        @Override
        public void detachPanel(boolean revertChanges) {
            super.detachPanel(revertChanges);
        }
    }
}
