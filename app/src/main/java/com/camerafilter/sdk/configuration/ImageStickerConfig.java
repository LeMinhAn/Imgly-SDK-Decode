package com.camerafilter.sdk.configuration;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;

import com.camerafilter.R;
import com.camerafilter.utils.BitmapFactoryUtils;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class ImageStickerConfig extends AbstractConfig implements AbstractConfig.StickerConfigInterface {
    private final @DrawableRes
    @RawRes
    int stickerId;

    private final boolean isSvg;

    public ImageStickerConfig(@StringRes int name, @DrawableRes @RawRes int drawableId, @DrawableRes @RawRes int stickerId) {
        super(name, drawableId);
        this.stickerId  = stickerId;
        this.isSvg      = BitmapFactoryUtils.checkIsSvgResource(stickerId);
    }

    /**
     * Get sticker drawable resource it, it can be a drawable or raw
     * @return the drawable resource id;
     */
    public @DrawableRes @RawRes int getStickerId() {
        return stickerId;
    }

    /*
     * Check if the Sticker is a SVG sticker
     * @return true if it is a SVG sticker
     */
    /*public boolean isSvg() {
        return isSvg;
    }*/

    @NonNull
    @Override
    public STICKER_TYPE getType() {
        return STICKER_TYPE.IMAGE;
    }

    @Override
    public int getLayout() {
        return R.layout.imgly_list_item_sticker;
    }

    @Override
    public boolean isSelectable() {
        return false;
    }
}
