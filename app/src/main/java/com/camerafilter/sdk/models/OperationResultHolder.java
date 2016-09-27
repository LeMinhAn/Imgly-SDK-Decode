package com.camerafilter.sdk.models;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.annotation.Nullable;

import com.camerafilter.sdk.operator.AbstractOperation;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class OperationResultHolder implements AbstractOperation.SourceHolder, AbstractOperation.ResultHolder {

    private int fullPreviewWidth;
    private int fullPreviewHeight;
    @Nullable
    private Bitmap result;

    public OperationResultHolder() {}

    @Override
    public boolean needRenderFullResult() {
        return true;
    }

    @Override public void disableInvalidatable() {}

    @Override public void invalidateAll() {}

    @Override public void invalidateFullPreview() {}

    @Override
    public void invalidateSharpPreview() {}

    @Override
    public void setSharpRegionResult(Bitmap sharpRegion, Rect sharpRect) {}

    @Override
    public void setFullResult(@Nullable Bitmap fullPreview) {
        this.result = fullPreview;
        if (fullPreview != null) {
            fullPreviewWidth  = fullPreview.getWidth();
            fullPreviewHeight = fullPreview.getHeight();
        }
    }

    @Override
    public void setBlurResult(Bitmap blurPreview) {

    }

    @Override
    public boolean needRenderSharpResult() {
        return false;
    }

    @Nullable
    @Override
    public Rect getSharpRect() {
        return null;
    }

    @Override
    public boolean hasFullPreview() {
        return result != null;
    }

    @Override
    public boolean hasBlurPreview() {
        return false;
    }

    @Override
    public boolean hasSharpPreview() {
        return false;
    }

    @Nullable
    @Override
    public Bitmap getFullPreview() {
        return result;
    }

    @Nullable
    @Override
    public Bitmap getBlurPreview() {
        return null;
    }

    @Nullable
    @Override
    public Bitmap getSharpPreview() {
        return null;
    }

    @Override
    public int getFullWidth() {
        return fullPreviewWidth;
    }

    @Override
    public int getFullHeight() {
        return fullPreviewHeight;
    }

    @Override
    public void recycle() {

    }

    @Nullable
    @Override
    public String getIdentifier() {
        return null;
    }
}

