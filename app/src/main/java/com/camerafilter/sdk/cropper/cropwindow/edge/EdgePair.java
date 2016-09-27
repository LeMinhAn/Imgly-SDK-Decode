package com.camerafilter.sdk.cropper.cropwindow.edge;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class EdgePair {
    // Member Variables ////////////////////////////////////////////////////////

    public Edge primary;
    public Edge secondary;

    // Constructor /////////////////////////////////////////////////////////////

    public EdgePair(Edge edge1, Edge edge2) {
        primary = edge1;
        secondary = edge2;
    }
}
