package com.camerafilter.sdk.brush.models;

import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class Brush {
    public final float radius;
    public final float hardness;
    public final float stepSize;
    public final int color;

    Brush(
            @FloatRange(from = 1.0, to = 400.0) float radius,
            @FloatRange(from = 0.0, to = 1.0) float hardness,
            @ColorInt int color
    ) {
        this.color = color;
        this.radius = radius < 1 ? 1 : radius;
        this.hardness = hardness;

        this.stepSize = Math.max(radius / 10, 1);
    }
}
