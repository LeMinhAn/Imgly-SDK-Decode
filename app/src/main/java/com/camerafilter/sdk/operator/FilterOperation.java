package com.camerafilter.sdk.operator;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.camerafilter.sdk.configuration.AbstractConfig;
import com.camerafilter.sdk.filter.ImageFilter;
import com.camerafilter.sdk.filter.NoneImageFilter;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class FilterOperation extends AbstractOperation {

    private float intensity = 1;

    private AbstractConfig.ImageFilterInterface filter;

    public FilterOperation(ImageFilter filter) {
        super();
        setFilter(filter);
    }

    /**
     * Get the current filter.
     * @return current filter.
     */
    public AbstractConfig.ImageFilterInterface getFilter(){
        return filter;
    }

    /**
     * Set a image filter.
     * @param filter filter.
     */
    public void setFilter(AbstractConfig.ImageFilterInterface filter) {
        this.filter = filter;
        invalidateState();
    }

    /**
     * Set filter intensity.
     * @param intensity intensity from 0.0 - 1.0.
     */
    public void setIntensity(float intensity) {
        this.intensity = intensity > 0 ? intensity < 1 ? intensity : 1 : 0;

        invalidateState();
    }

    public float getIntensity() {
        return intensity;
    }

    @NonNull
    @Override
    protected Operator.Priority getPriority() {
        return Operator.Priority.Effect;
    }

    @Override
    protected String getIdentifier() {
        return this.getClass().getName();
    }

    @NonNull
    @Override
    public MODE getOperationMode() {
        return MODE.BACKGROUND_THREAD;
    }

    @Override
    protected boolean doOperation() {
        AbstractConfig.ImageFilterInterface filter = getFilter();
        if (!(filter instanceof NoneImageFilter)) {
            ResultHolder result = getResultBitmapHolder();
            SourceHolder source = getSourceBitmapHolder();

            if (result.needRenderFullResult()) {
                Bitmap sourceBitmap = source.getFullPreview();

                final Bitmap resultBitmap;
                if (filter.hasIntensityConfig()) {
                    resultBitmap = filter.renderImage(sourceBitmap, intensity);
                } else {
                    resultBitmap = filter.renderImage(sourceBitmap);
                }

                result.setFullResult(resultBitmap);
            }

            if (result.needRenderSharpResult() && source.hasSharpPreview()) {
                Bitmap sourceBitmap = source.getSharpPreview();
                final Bitmap resultBitmap;
                if (filter.hasIntensityConfig()) {
                    resultBitmap = filter.renderImage(sourceBitmap, intensity);
                } else {
                    resultBitmap = filter.renderImage(sourceBitmap);
                }
                result.setSharpRegionResult(resultBitmap, source.getSharpRect());
            }
        } else {
            ResultHolder result = getResultBitmapHolder();
            SourceHolder source = getSourceBitmapHolder();

            if (result.needRenderFullResult()) {
                result.setFullResult(source.getFullPreview());
            }

            if (result.needRenderSharpResult() && source.hasSharpPreview()) {
                result.setSharpRegionResult(source.getSharpPreview(), source.getSharpRect());
            }
        }

        return true;
    }


}

