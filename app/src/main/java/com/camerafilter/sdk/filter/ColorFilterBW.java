package com.camerafilter.sdk.filter;

import com.camerafilter.R;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class ColorFilterBW extends LutColorFilter {

    public ColorFilterBW() {
        super(R.string.imgly_color_filter_name_bw, R.drawable.imgly_filter_preview_photo, R.drawable.imgly_lut_identity);
    }
}