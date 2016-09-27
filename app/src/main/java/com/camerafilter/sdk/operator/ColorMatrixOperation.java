package com.camerafilter.sdk.operator;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.support.annotation.NonNull;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class ColorMatrixOperation extends AbstractPaintOperation {

    @NonNull
    @Override
    protected Operator.Priority getPriority() {
        return Operator.Priority.ColorMatrix;
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

    private float brightness = 0;
    private float saturation = 1;
    private float contrast   = 0f;

    @Override
    protected boolean doOperation() {

        Paint paint;
        if (isInPreviewMode()) {
            paint = getPaint();
        } else {
            paint = new Paint();
        }

        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.postConcat(getSaturationMatrix(saturation));
        colorMatrix.postConcat(getContrastMatrix(contrast));
        colorMatrix.postConcat(getBrightnessMatrix(brightness));


        ColorFilter filer = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(filer);

        if (isInPreviewMode()) {
            refreshPaint();
        } else {
            ResultHolder result = getResultBitmapHolder();
            SourceHolder source = getSourceBitmapHolder();

            if (result.needRenderFullResult()) {
                result.setFullResult(renderColorFilter(source.getFullPreview(), paint));
            }

            if (result.needRenderSharpResult() && source.hasSharpPreview()) {
                result.setSharpRegionResult(renderColorFilter(source.getSharpPreview(), paint), source.getSharpRect());
            }

        }
        return true;
    }

    @NonNull
    private Bitmap renderColorFilter(@NonNull Bitmap orgBitmap, Paint paint) {
        //Bitmap bitmap = Bitmap.createBitmap(orgBitmap.getWidth(), orgBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(orgBitmap);
        canvas.drawBitmap(orgBitmap, 0, 0, paint);
        return orgBitmap;
    }

    @NonNull
    protected ColorMatrix getSaturationMatrix(float saturation) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(saturation);
        return matrix;
    }

    @NonNull
    protected ColorMatrix getContrastMatrix(float contrast) {
        float scale = contrast + 1.f;
        float translate = (-.5f * scale + .5f) * 255.f;
        float[] array = new float[] {
                scale, 0, 0, 0, translate,
                0, scale, 0, 0, translate,
                0, 0, scale, 0, translate,
                0, 0, 0, 1, 0};
        return new ColorMatrix(array);
    }

    @NonNull
    protected ColorMatrix getBrightnessMatrix(float brightness) {
        float translate = brightness * 255.f;
        float[] array = new float[] {
                1, 0, 0, 0, translate,
                0, 1, 0, 0, translate,
                0, 0, 1, 0, translate,
                0, 0, 0, 1, 0 };
        return new ColorMatrix(array);
    }

    /**
     * Set image saturation.
     * @param saturation A value of 0 maps the color to gray-scale. 1 is identity. 1 &gt; intense the color.
     */
    public void setSaturation(float saturation){
        this.saturation = saturation;
    }

    /**
     * Get the current Saturation.
     * @return Current Saturation.
     */
    public float getSaturation() {
        return saturation;
    }

    /**
     * Get the current brightness
     * @return Current brightness
     */
    public float getBrightness() {
        return brightness;
    }

    /**
     * Set image brightness
     * @param brightness A value of 0 maps the color to black. 1 is identity. 2 is white.
     */
    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    /**
     * Get the current contrast
     * @return Current contrast
     */
    public float getContrast() {
        return contrast;
    }

    /**
     * Set the contrast.
     * @param contrast A value of 0 the image is totally gray. 1 is identity. 1 &gt; intense the contrast.
     */
    public void setContrast(float contrast) {
        this.contrast = contrast;
    }
}

