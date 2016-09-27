package com.camerafilter.sdk.configuration;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.camerafilter.ui.adapter.DataSourceListAdapter;

import java.util.UUID;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class TextStickerConfig implements AbstractConfig.StickerConfigInterface {

    private final String identifierId = UUID.randomUUID().toString();
    private String text;
    private AbstractConfig.FontConfigInterface font;
    private int color;
    private int backgroundColor;
    private Paint.Align align;

    public TextStickerConfig(String name, Paint.Align align, AbstractConfig.FontConfigInterface font, int color, int backgroundColor) {
        this.text = name;
        this.color = color;
        this.font = font;
        this.backgroundColor = backgroundColor;
        this.align = align;
    }

    @NonNull
    @Override
    public STICKER_TYPE getType() {
        return STICKER_TYPE.TEXT;
    }

    @Override
    public int getStickerId() {
        return -1;
    }

    /**
     * Get the Text-Sticker Text
     *
     * @return text string
     */
    public String getText() {
        return text;
    }

    /**
     * Set Text-Sticker text
     *
     * @param text text string
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Get the Text-Sticker Font
     *
     * @return font config model
     */
    public AbstractConfig.FontConfigInterface getFont() {
        return font;
    }

    /**
     * Set the Text-Sticker font
     *
     * @param font font config object
     */
    public void setFont(AbstractConfig.FontConfigInterface font) {
        this.font = font;
    }

    /**
     * Get the text align of the Text-Sticker
     *
     * @return align
     * @deprecated is not implemented
     */
    public Paint.Align getAlign() {
        return align;
    }

    /**
     * Get the text align
     *
     * @param align the align of the Text-Sticker
     * @deprecated is not implemented
     */
    public void setAlign(Paint.Align align) {
        this.align = align;
    }

    /**
     * Get the font Typeface
     *
     * @return the Typeface
     */
    @Nullable
    public Typeface getTypeface() {
        if (font == null) {
            return null;
        }
        return font.getTypeface();
    }

    /**
     * Get the Foreground Color
     *
     * @return 32bit rgba color value
     */
    public int getColor() {
        return color;
    }

    /**
     * Set foreground Color
     *
     * @param color 32bit rgba color value
     */
    public void setColor(int color) {
        this.color = color;
    }

    /**
     * Get the Background Color
     *
     * @return 32bit rgba color value
     */
    public int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Set background Color
     *
     * @param color 32bit rgba color value
     */
    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
    }

    /**
     * Set Text-Sticker text and align
     *
     * @param text  text string
     * @param align text align
     * @deprecated not implemented
     */
    public void setText(String text, Paint.Align align) {
        this.text = text;
        this.align = align;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TextStickerConfig that = (TextStickerConfig) o;

        return identifierId.equals(that.identifierId);

    }

    @Override
    public int hashCode() {
        return identifierId.hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return "TextStickerConfig{" +
                "text='" + text + '\'' +
                ", font=" + font +
                ", color=" + color +
                ", backgroundColor=" + backgroundColor +
                ", align=" + align +
                ", identifierId='" + identifierId + '\'' +
                '}';
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public void setDirtyFlag(boolean isDirty) {

    }

    @Nullable
    @Override
    public String getName() {
        return null;
    }

    @Override
    public int getLayout() {
        return 0;
    }

    @Override
    public int getVerticalLayout() {
        return 0;
    }


    @NonNull
    @Override
    public DataSourceListAdapter.DataSourceViewHolder<AbstractConfig.BindData> createViewHolder(View view, boolean useVerticalLayout) {
        return new DataSourceListAdapter.DataSourceViewHolder<AbstractConfig.BindData>(view) {
            @Override
            protected void bind(AbstractConfig.BindData bindData) {
            }

            @Override
            public void setSelectedState(boolean selected) {
            }
        };
    }

    @Nullable
    @Override
    public AbstractConfig.BindData generateBindData() {
        return null;
    }

    @Nullable
    @Override
    public AbstractConfig.BindData generateBindDataAsync() {
        return null;
    }

    @Override
    public boolean isSelectable() {
        return false;
    }
}
