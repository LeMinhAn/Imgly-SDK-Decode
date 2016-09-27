package com.camerafilter.sdk.gles;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public interface Texture {
    int getTextureId();
    void release();

    int getTextureTarget();
}
