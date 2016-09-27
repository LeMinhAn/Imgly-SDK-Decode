package com.camerafilter.sdk.cropper.cropwindow.handle;

import android.graphics.Rect;
import android.support.annotation.NonNull;

import com.camerafilter.sdk.cropper.cropwindow.edge.Edge;
import com.camerafilter.sdk.cropper.util.AspectRatioUtil;

/**
 * Created by Le Minh An on 9/20/2016.
 */
class HorizontalHandleHelper extends HandleHelper {

    // Member Variables ////////////////////////////////////////////////////////

    private final Edge mEdge;

    // Constructor /////////////////////////////////////////////////////////////

    HorizontalHandleHelper(Edge edge) {
        super(edge, null);
        mEdge = edge;
    }

    // HandleHelper Methods ////////////////////////////////////////////////////

    @Override
    void updateCropWindow(float x,
                          float y,
                          float targetAspectRatio,
                          @NonNull Rect imageRect,
                          float snapRadius) {

        // Adjust this Edge accordingly.
        mEdge.adjustCoordinate(x, y, imageRect, snapRadius, targetAspectRatio);

        float left = Edge.LEFT.getCoordinate();
        float top = Edge.TOP.getCoordinate();
        float right = Edge.RIGHT.getCoordinate();
        float bottom = Edge.BOTTOM.getCoordinate();

        // After this Edge is moved, our crop window is now out of proportion.
        final float targetWidth = AspectRatioUtil.calculateWidth(top, bottom, targetAspectRatio);
        final float currentWidth = right - left;

        // Adjust the crop window so that it maintains the given aspect ratio by
        // moving the adjacent edges symmetrically in or out.
        final float difference = targetWidth - currentWidth;
        final float halfDifference = difference / 2;
        left -= halfDifference;
        right += halfDifference;

        Edge.LEFT.setCoordinate(left);
        Edge.RIGHT.setCoordinate(right);

        // $ if we have gone out of bounds on the sides, and fix.
        if (Edge.LEFT.isOutsideMargin(imageRect, snapRadius) && !mEdge.isNewRectangleOutOfBounds(Edge.LEFT,
                imageRect,
                targetAspectRatio)) {
            final float offset = Edge.LEFT.snapToRect(imageRect);
            Edge.RIGHT.offset(-offset);
            mEdge.adjustCoordinate(targetAspectRatio);

        }
        if (Edge.RIGHT.isOutsideMargin(imageRect, snapRadius) && !mEdge.isNewRectangleOutOfBounds(Edge.RIGHT,
                imageRect,
                targetAspectRatio)) {
            final float offset = Edge.RIGHT.snapToRect(imageRect);
            Edge.LEFT.offset(-offset);
            mEdge.adjustCoordinate(targetAspectRatio);
        }
    }
}