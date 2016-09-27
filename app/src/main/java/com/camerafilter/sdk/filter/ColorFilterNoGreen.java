package com.camerafilter.sdk.filter;

import com.camerafilter.R;

/**
 * Created by Le Minh An on 9/26/2016.
 */
public class ColorFilterNoGreen extends LutColorFilter {

    public ColorFilterNoGreen() {
        super(R.string.imgly_color_filter_name_nogreen, R.drawable.imgly_filter_preview_photo, R.drawable.imgly_lut_identity);
    }
}
