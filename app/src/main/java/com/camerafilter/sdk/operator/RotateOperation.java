package com.camerafilter.sdk.operator;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.annotation.NonNull;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class RotateOperation extends AbstractEditorOperation {

    private int rotation = 0;
    private boolean flipVertical   = false;
    private boolean flipHorizontal = false;


    @NonNull
    @Override
    protected Operator.Priority getPriority() {
        return Operator.Priority.Orientation;
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
        if (isInPreviewMode()) {
            getEditor().setImageRotation(rotation, flipHorizontal, flipVertical);
        } else {
            Matrix matrix = new Matrix();

            matrix.preRotate(rotation % 360);
            matrix.postScale(flipHorizontal ? -1 : 1, flipVertical ? -1 : 1);

            ResultHolder result = getResultBitmapHolder();
            SourceHolder source = getSourceBitmapHolder();

            result.setFullResult(Bitmap.createBitmap(source.getFullPreview(), 0, 0, source.getFullWidth(), source.getFullHeight(), matrix, false));
        }
        return true;
    }

    /**
     * Rotate Clockwise
     */
    public void rotateCW() {
        rotation += 90;
        rotation %= 360;

        setRotation(rotation);
    }

    /**
     * Rotate Counter Clockwise
     */
    public void rotateCCW() {
        rotation -= 90;
        if (rotation < 0) {
            rotation = 360 - 90;
        }

        setRotation(rotation);
    }

    /**
     * Set rotation in 90 Degree steps
     */
    public void setRotation(int rotation) {
        this.rotation = rotation - rotation % 90;
        invalidateState();
    }

    /**
     * Get the current rotation.
     * @return current rotation.
     */
    public int getRotation() {
        return rotation;
    }

    /**
     * Check if image flipped vertical.
     * @return true if flipped
     */
    public boolean isFlipVertical() {
        return flipVertical;
    }

    /**
     * Check if image flipped horizontal.
     * @return true if flipped
     */
    public boolean isFlipHorizontal() {
        return flipHorizontal;
    }

    /**
     * Set vertical flip
     * @param flipVertical true to set flip vertical if is not already flipped
     */
    public void setFlipVertical(boolean flipVertical) {
        this.flipVertical = flipVertical;
        invalidateState();
    }

    /**
     * Set horizontal flip
     * @param flipHorizontal true to set flip vertical if is not already flipped
     */
    public void setFlipHorizontal(boolean flipHorizontal) {
        this.flipHorizontal = flipHorizontal;
        invalidateState();
    }


}

