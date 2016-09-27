package com.camerafilter.sdk.cropper.util;

import android.graphics.Rect;
import android.support.annotation.NonNull;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class AspectRatioUtil {
    /**
     * Calculates the aspect ratio given a rectangle.
     */
    public static float calculateAspectRatio(float left, float top, float right, float bottom) {

        final float width = right - left;
        final float height = bottom - top;
        final float aspectRatio = width / height;

        return aspectRatio;
    }

    /**
     * Calculates the aspect ratio given a rectangle.
     */
    public static float calculateAspectRatio(@NonNull Rect rect) {

        final float aspectRatio = (float) rect.width() / (float) rect.height();

        return aspectRatio;
    }

    /**
     * Calculates the x-coordinate of the left edge given the other sides of the
     * rectangle and an aspect ratio.
     */
    public static float calculateLeft(float top, float right, float bottom, float targetAspectRatio) {

        final float height = bottom - top;
        // targetAspectRatio = width / height
        // width = targetAspectRatio * height
        // right - left = targetAspectRatio * height
        final float left = right - (targetAspectRatio * height);

        return left;
    }

    /**
     * Calculates the y-coordinate of the top edge given the other sides of the
     * rectangle and an aspect ratio.
     */
    public static float calculateTop(float left, float right, float bottom, float targetAspectRatio) {

        final float width = right - left;
        // targetAspectRatio = width / height
        // width = targetAspectRatio * height
        // height = width / targetAspectRatio
        // bottom - top = width / targetAspectRatio
        final float top = bottom - (width / targetAspectRatio);

        return top;
    }

    /**
     * Calculates the x-coordinate of the right edge given the other sides of
     * the rectangle and an aspect ratio.
     */
    public static float calculateRight(float left, float top, float bottom, float targetAspectRatio) {

        final float height = bottom - top;
        // targetAspectRatio = width / height
        // width = targetAspectRatio * height
        // right - left = targetAspectRatio * height
        final float right = (targetAspectRatio * height) + left;

        return right;
    }

    /**
     * Calculates the y-coordinate of the bottom edge given the other sides of
     * the rectangle and an aspect ratio.
     */
    public static float calculateBottom(float left, float top, float right, float targetAspectRatio) {

        final float width = right - left;
        // targetAspectRatio = width / height
        // width = targetAspectRatio * height
        // height = width / targetAspectRatio
        // bottom - top = width / targetAspectRatio
        final float bottom = (width / targetAspectRatio) + top;

        return bottom;
    }

    /**
     * Calculates the width of a rectangle given the top and bottom edges and an
     * aspect ratio.
     */
    public static float calculateWidth(float top, float bottom, float targetAspectRatio) {

        final float height = bottom - top;
        final float width = targetAspectRatio * height;

        return width;
    }

    /**
     * Calculates the height of a rectangle given the left and right edges and
     * an aspect ratio.
     */
    public static float calculateHeight(float left, float right, float targetAspectRatio) {

        final float width = right - left;
        final float height = width / targetAspectRatio;

        return height;
    }
}
