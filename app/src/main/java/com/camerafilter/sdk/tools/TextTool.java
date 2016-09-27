package com.camerafilter.sdk.tools;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.ViewGroup;

import com.camerafilter.ImgLySdk;
import com.camerafilter.R;
import com.camerafilter.sdk.configuration.PhotoEditorSdkConfig;
import com.camerafilter.sdk.configuration.TextStickerConfig;
import com.camerafilter.ui.panels.TextFontOptionToolPanel;
import com.camerafilter.ui.panels.TextOptionToolPanel;
import com.camerafilter.ui.panels.TextToolPanel;
import com.camerafilter.sdk.views.EditorPreview;

import java.util.List;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class TextTool extends StickerTool {
    public TextTool(@StringRes int name, @DrawableRes int drawableId) {
        super(name, drawableId, TextToolPanel.class);
    }

    private TextTool(@StringRes int name, @NonNull Class<? extends AbstractToolPanel> panelClass) {
        super(name, R.drawable.imgly_icon_tool_text, panelClass);
    }

    @NonNull
    @Override
    public String getTitle() {
        return ImgLySdk.getAppResource().getString(R.string.imgly_tool_name_text_add);
    }

    @Override
    public boolean isRevertible() {
        return true;
    }

    public void setFontConfig(FontConfigInterface fontConfig) {
        TextStickerConfig config = getEditorPreview().getCurrentTextConfig();
        if (config != null) {
            config.setFont(fontConfig);
            refreshConfig(config);
        }
    }

    public void setColor(int color, int backgroundColor) {
        TextStickerConfig config = getEditorPreview().getCurrentTextConfig();
        if (config != null) {
            config.setColor(color);
            config.setBackgroundColor(backgroundColor);
            refreshConfig(config);
        }
    }

    @Override
    public View attachPanel(@NonNull ViewGroup parentView, @NonNull EditorPreview preview) {
        View view = super.attachPanel(parentView, preview);
        return view;
    }

    @Override
    public void detachPanel(boolean revertChanges) {
        super.detachPanel(revertChanges);

    }

    public void openFontSelection(FontConfigInterface currentFontSelection, FontSelection.OnFontSelected listener){
        getEditorPreview().dispatchEnterToolMode(new FontSelection(currentFontSelection, listener), false);
    }

    public void openColorSelection(COLOR_TYPE type, int currentColor, ColorSelection.OnColorSelected<COLOR_TYPE> listener){
        getEditorPreview().dispatchEnterToolMode(new ColorSelection(type, currentColor,  listener), false);
    }

    public static class Options extends TextTool {
        public Options() {
            super(R.string.imgly_tool_name_text_options, TextOptionToolPanel.class);
        }

        @Override
        public boolean isRevertible() {
            return false;
        }
    }

    public enum COLOR_TYPE {
        FOREGROUND,
        BACKGROUND
    }

    public static class ColorSelection extends AbstractColorTool<COLOR_TYPE> {
        ColorSelection(COLOR_TYPE type, int color, OnColorSelected<COLOR_TYPE> listener) {
            super(R.string.imgly_tool_name_text_color, type, color, listener);
        }

        @NonNull
        @Override
        public List<? extends ColorConfigInterface> getColorList() {
            return PhotoEditorSdkConfig.getTextColorConfig();
        }
    }

    public static class FontSelection extends TextTool {

        private final OnFontSelected listener;

        private final FontConfigInterface fontConfig;

        private FontSelection(FontConfigInterface currentFontConfig, OnFontSelected listener) {
            super(R.string.imgly_tool_name_text_font, TextFontOptionToolPanel.class);
            this.listener = listener;
            this.fontConfig = currentFontConfig;
            saveState();
        }

        private final SaveState saveState = new SaveState();
        private class SaveState {
            @Nullable
            FontConfigInterface fontConfig = null;
        }

        @Override
        protected void saveState() {
            saveState.fontConfig = fontConfig;
        }

        @Override
        protected void revertState() {
            setFontConfig(saveState.fontConfig);
        }


        @Override
        public boolean isRevertible() {
            return true;
        }

        public void setFontConfig(FontConfigInterface fontConfig) {
            listener.setFontConfig(fontConfig);
        }

        public FontConfigInterface getFontConfig() {
            return fontConfig;
        }

        public interface OnFontSelected {
            void setFontConfig(FontConfigInterface fontConfig);
        }
    }

}

