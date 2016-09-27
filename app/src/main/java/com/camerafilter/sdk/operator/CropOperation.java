package com.camerafilter.sdk.operator;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.camerafilter.sdk.configuration.AbstractConfig;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class CropOperation extends AbstractEditorOperation {


    @NonNull
    @Override
    protected Operator.Priority getPriority() {
        return Operator.Priority.Crop;
    }

    @Override
    protected String getIdentifier() {
        return this.getClass().getName();
    }

    @NonNull
    @Override
    public MODE getOperationMode() {
        return isInPreviewMode() ? MODE.INSTANT_MAIN_THREAD : MODE.BACKGROUND_THREAD;
    }

    @Override
    protected boolean doOperation() {

        if (!isInPreviewMode()) {
            ResultHolder result = getResultBitmapHolder();
            SourceHolder source = getSourceBitmapHolder();

            Rect crop = getCropRect(source.getFullWidth(), source.getFullHeight());


            Bitmap croppedBitmap = Bitmap.createBitmap(
                    source.getFullPreview(),
                    crop.left,
                    crop.top,
                    crop.width(),
                    crop.height()
            );

            AbstractConfig.AspectConfigInterface aspect = getCropConfig();
            if (aspect != null && aspect.hasSpecificSize()) {
                croppedBitmap = Bitmap.createScaledBitmap(croppedBitmap, aspect.getCropWidth(), aspect.getCropHeight(), true);
            }

            result.setFullResult(croppedBitmap);
        }
        return true;

    }

    private Rect getCropRect(int width, int height) {
        return getEditor().getCropRect(width, height);
    }

    @Nullable
    private AbstractConfig.AspectConfigInterface getCropConfig() {
        return getEditor().getCurrentRotationBasedAspect();
    }

}