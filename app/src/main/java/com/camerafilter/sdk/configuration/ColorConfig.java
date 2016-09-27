package com.camerafilter.sdk.configuration;

import android.graphics.Bitmap;
import android.support.annotation.StringRes;

import com.camerafilter.R;

/**
 * Created by Le Minh An on 9/26/2016.
 */
public class ColorConfig extends AbstractConfig implements AbstractConfig.ColorConfigInterface {
    private final int color;

    public ColorConfig(@StringRes int name, int color) {
        super(name);
        this.color = color ^ 0xFF000000;
    }

    @Override
    public int getLayout() {
        return R.layout.imgly_list_item_color;
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public boolean hasStaticThumbnail() {
        return false;
    }

    @Override
    public Bitmap getThumbnailBitmap() {
        return getThumbnailBitmap(1);
    }

    @Override
    public Bitmap getThumbnailBitmap(int maxWidth) {
        return Bitmap.createBitmap(new int[]{color, color}, 1, 1, Bitmap.Config.ARGB_8888);
    }

    @Override
    public boolean isSelectable() {
        return true;
    }
}
