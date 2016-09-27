package com.camerafilter.sdk.cropper.cropwindow.handle;

import android.graphics.Rect;
import android.support.annotation.NonNull;

import com.camerafilter.sdk.cropper.cropwindow.edge.Edge;
import com.camerafilter.sdk.cropper.util.AspectRatioUtil;

/**
 * Created by Le Minh An on 9/20/2016.
 */
class VerticalHandleHelper extends HandleHelper {

    // Member Variables ////////////////////////////////////////////////////////

    private final Edge mEdge;

    // Constructor /////////////////////////////////////////////////////////////

    VerticalHandleHelper(Edge edge) {
        super(null, edge);
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
        final float targetHeight = AspectRatioUtil.calculateHeight(left, right, targetAspectRatio);
        final float currentHeight = bottom - top;

        // Adjust the crop window so that it maintains the given aspect ratio by
        // moving the adjacent edges symmetrically in or out.
        final float difference = targetHeight - currentHeight;
        final float halfDifference = difference / 2;
        top -= halfDifference;
        bottom += halfDifference;

        Edge.TOP.setCoordinate(top);
        Edge.BOTTOM.setCoordinate(bottom);

        // $ if we have gone out of bounds on the top or bottom, and fix.
        if (Edge.TOP.isOutsideMargin(imageRect, snapRadius) && !mEdge.isNewRectangleOutOfBounds(Edge.TOP,
                imageRect,
                targetAspectRatio)) {
            final float offset = Edge.TOP.snapToRect(imageRect);
            Edge.BOTTOM.offset(-offset);
            mEdge.adjustCoordinate(targetAspectRatio);
        }
        if (Edge.BOTTOM.isOutsideMargin(imageRect, snapRadius) && !mEdge.isNewRectangleOutOfBounds(Edge.BOTTOM,
                imageRect,
                targetAspectRatio)) {
            final float offset = Edge.BOTTOM.snapToRect(imageRect);
            Edge.TOP.offset(-offset);
            mEdge.adjustCoordinate(targetAspectRatio);
        }
    }
}
