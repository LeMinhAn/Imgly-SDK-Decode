package com.camerafilter.sdk.gles;

import com.camerafilter.acs.Cam;

/**
 * Created by Le Minh An on 9/26/2016.
 */
public interface PreviewTextureInterface extends Texture {

    interface OnFrameAvailableListener {
        void onFrameAvailable(PreviewTextureInterface previewTexture);
    }

    void setOnFrameAvailableListener(final OnFrameAvailableListener l);

    void setup(Cam camera);

    void updateTexImage();

    void getTransformMatrix(float[] mtx);

}