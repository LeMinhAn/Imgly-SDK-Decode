package com.camerafilter.ui.panels;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.view.View;

import com.camerafilter.ImgLySdk;
import com.camerafilter.R;
import com.camerafilter.sdk.configuration.AbstractConfig;
import com.camerafilter.sdk.configuration.DataSourceInterface;
import com.camerafilter.sdk.configuration.Divider;
import com.camerafilter.sdk.configuration.PhotoEditorSdkConfig;
import com.camerafilter.sdk.configuration.TextStickerConfig;
import com.camerafilter.sdk.tools.AbstractColorTool;
import com.camerafilter.sdk.tools.AbstractTool;
import com.camerafilter.sdk.tools.AbstractToolPanel;
import com.camerafilter.sdk.tools.TextTool;
import com.camerafilter.ui.adapter.DataSourceListAdapter;
import com.camerafilter.ui.widgets.HorizontalListView;
import com.camerafilter.utils.BitmapFactoryUtils;
import com.camerafilter.utils.SetHardwareAnimatedViews;

import java.util.ArrayList;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class TextOptionToolPanel extends AbstractToolPanel implements TextTool.FontSelection.OnFontSelected, DataSourceListAdapter.OnItemClickListener<TextOptionToolPanel.TextStickerOption>, AbstractColorTool.OnColorSelected<TextTool.COLOR_TYPE> {

    private final int DEFAULT_COLOR    = 0xFFFFFFFF; //ARGB
    private final int DEFAULT_BG_COLOR = 0x00FFFFFF; //ARGB

    private static final int LAYOUT = R.layout.imgly_panel_tool_text_option;

    private enum OPTION {
        FONT,
        COLOR,
        BG_COLOR,
        FLIP_H,
        FLIP_V,
        TO_FRONT,
        DELETE
    }

    private TextTool.Options textOptionTool;

    private int currentColor = DEFAULT_COLOR;
    private int currentBackgroundColor = DEFAULT_BG_COLOR;

    private AbstractConfig.FontConfigInterface currentFontConfig = PhotoEditorSdkConfig.getFontConfig().get(0);

    private TextStickerColorOption colorOption;
    private TextStickerColorOption backgroundColorOption;

    private DataSourceListAdapter listAdapter;

    @Override
    protected int getLayoutResource() {
        return LAYOUT;
    }

    @Override
    protected void onAttached(Context context, @NonNull View panelView, AbstractTool tool) {
        panelView.setTranslationY(panelView.getHeight());
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(panelView, "translationY", panelView.getHeight(), 0f)
        );

        animatorSet.addListener(new SetHardwareAnimatedViews(panelView));
        animatorSet.setStartDelay(ANIMATION_DURATION / 2);
        animatorSet.setDuration(ANIMATION_DURATION);
        animatorSet.start();

        ImgLySdk.getAnalyticsPlugin().changeScreen("TextTool");

        this.textOptionTool = (TextTool.Options) tool;

        HorizontalListView listView = (HorizontalListView) panelView.findViewById(R.id.optionList);

        listAdapter = new DataSourceListAdapter(context);

        colorOption = new TextStickerColorOption(OPTION.COLOR, currentColor);
        backgroundColorOption = new TextStickerColorOption(OPTION.BG_COLOR, currentBackgroundColor);

        ArrayList<AbstractConfig> configs = new ArrayList<>();
        configs.add(new TextStickerOption(OPTION.FONT));
        configs.add(colorOption);
        configs.add(backgroundColorOption);
        configs.add(new Divider());
        configs.add(new TextStickerOption(OPTION.FLIP_H));
        configs.add(new TextStickerOption(OPTION.FLIP_V));
        configs.add(new Divider());
        configs.add(new TextStickerOption(OPTION.TO_FRONT));
        configs.add(new TextStickerOption(OPTION.DELETE));

        listAdapter.setData(configs);
        listAdapter.setOnItemClickListener(this);
        listView.setAdapter(listAdapter);

    }

    @Override
    public void refresh() {
        TextStickerConfig config = textOptionTool.getEditorPreview().getCurrentTextConfig();
        if (config != null) {
            colorOption.setColor(config.getColor());
            backgroundColorOption.setColor(config.getBackgroundColor());
            listAdapter.invalidateItem(colorOption);
            listAdapter.invalidateItem(backgroundColorOption);
        }


    }

    @Override
    protected void onDetached() {

    }

    private void selectFont() {
        textOptionTool.openFontSelection(currentFontConfig, this);
    }

    private void selectColor() {
        textOptionTool.openColorSelection(TextTool.COLOR_TYPE.FOREGROUND, currentColor, this);
    }

    private void selectBackgroundColor() {
        textOptionTool.openColorSelection(TextTool.COLOR_TYPE.BACKGROUND, currentBackgroundColor, this);
    }

    @Override
    public void setFontConfig(AbstractConfig.FontConfigInterface fontConfig) {
        this.currentFontConfig = fontConfig;
        textOptionTool.setFontConfig(currentFontConfig);
    }

    @Override
    public void setColor(int color, @NonNull TextTool.COLOR_TYPE type) {

        switch (type){
            case FOREGROUND:
                currentColor = color;
                colorOption.setColor(color);
                listAdapter.invalidateItem(colorOption);
                break;

            case BACKGROUND:
                currentBackgroundColor = color;
                backgroundColorOption.setColor(color);
                listAdapter.invalidateItem(backgroundColorOption);
                break;
        }

        textOptionTool.setColor(currentColor, currentBackgroundColor);
    }


    @Override
    public void onItemClick(@NonNull TextStickerOption entity) {
        switch (entity.option) {
            case FONT:     selectFont();                         break;
            case COLOR:    selectColor();                        break;
            case BG_COLOR: selectBackgroundColor();              break;
            case DELETE:   textOptionTool.deleteSticker();       break;
            case FLIP_V:   textOptionTool.flipSticker(true);     break;
            case FLIP_H:   textOptionTool.flipSticker(false);    break;
            case TO_FRONT: textOptionTool.bringStickerToFront(); break;
        }
    }

    static class TextStickerOption extends AbstractConfig implements DataSourceInterface<AbstractConfig.BindData> {

        @NonNull
        final OPTION option;

        TextStickerOption(@NonNull OPTION option) {
            super(getNameRes(option));
            this.option = option;
        }

        static int getNameRes(@NonNull OPTION option) {
            switch (option) {
                case FONT:     return R.string.imgly_text_option_font;
                case COLOR:    return R.string.imgly_text_option_color;
                case BG_COLOR: return R.string.imgly_text_option_bg_color;

                case FLIP_V:   return R.string.imgly_sticker_option_flip_v;
                case FLIP_H:   return R.string.imgly_sticker_option_flip_h;
                case DELETE:   return R.string.imgly_sticker_option_delete;
                case TO_FRONT: default: return R.string.imgly_sticker_option_to_front;
            }
        }

        @Override
        public boolean isSelectable() {
            return false;
        }

        @Override
        public boolean hasStaticThumbnail() {
            return true;
        }

        @Override
        public Bitmap getThumbnailBitmap(int maxWidth) {
            return getThumbnailBitmap();
        }

        @Override
        public int getThumbnailResId() {
            switch (option) {
                case FONT:     return R.drawable.imgly_icon_option_font;
                case COLOR:    return R.drawable.imgly_icon_option_selected_color_bg;
                case BG_COLOR: return R.drawable.imgly_icon_option_selected_color_bg;

                case FLIP_V:   return R.drawable.imgly_icon_option_orientation_flip_v;
                case FLIP_H:   return R.drawable.imgly_icon_option_orientation_flip_h;
                case DELETE:   return R.drawable.imgly_icon_option_delete;
                case TO_FRONT: default: return R.drawable.imgly_icon_option_bringtofront;
            }
        }

        @Override
        public int getLayout() {
            return R.layout.imgly_list_item_option;
        }
    }

    private static class TextStickerColorOption extends TextStickerOption {

        TextStickerColorOption(@NonNull OPTION option, int color) {
            super(option);
            this.color = color;
        }

        @Override
        public boolean hasStaticThumbnail() {
            return false;
        }

        private int color;

        public void setColor(int color) {
            this.color = color;
        }

        @Override
        public Bitmap getThumbnailBitmap() {
            return getThumbnailBitmap(1);
        }

        private Bitmap colorOverlay;
        private Bitmap bitmap;
        private Bitmap result;
        private Paint paint;
        @Override
        public Bitmap getThumbnailBitmap(int maxWidth) {

            if (result == null) {
                Resources res = ImgLySdk.getAppResource();
                bitmap       = BitmapFactoryUtils.decodeResource(res, R.drawable.imgly_icon_option_selected_color_bg);
                colorOverlay = BitmapFactoryUtils.decodeResource(res, R.drawable.imgly_icon_option_selected_color);
                result       = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                paint = new Paint();
            }

            Canvas canvas = new Canvas(result);

            paint.setColorFilter(null);
            paint.setAlpha(255);

            canvas.drawBitmap(bitmap, 0, 0, paint);

            paint.setColorFilter(new LightingColorFilter(color, 1));
            paint.setAlpha(Color.alpha(color));
            canvas.drawBitmap(colorOverlay, 0,0, paint);

            return result;
        }
    }
}
