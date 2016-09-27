package com.camerafilter.sdk.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Xfermode;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.camerafilter.sdk.operator.AbstractOperation;
import com.camerafilter.sdk.tools.FocusTool;
import com.camerafilter.utils.ScaledMotionEventWrapper;
import com.camerafilter.utils.ThreadUtils;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class PicturePreviewView extends View {

    public enum PREVIEW_MODE {
        FOCUS,
        FULL_FIXED,
        PAN_AND_ZOOM
    }

    private final static float BLUR_FADE = 2 / 3f;

    private final static int INDICATOR_SHOW_TIME = 1000;
    private final static int INDICATOR_FADE_TIME = 500;

    private final ValueAnimator indicatorAnimation;

    @NonNull
    private Paint imagePaint;

    @NonNull
    private final Paint clearPaint;

    @NonNull
    private final Paint linePaint;
    private final Rect imageBlurRegion = new Rect();
    private final Rect imageCropRegion = new Rect();
    private final Rect imageDrawRegion = new Rect();
    private final RectF focusRect = new RectF();
    private final RectF focusInnerRect = new RectF();
    private final Xfermode xfermodeIn = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
    private final float density;
    private final Matrix matrix = new Matrix();
    private float imageScale = 1;
    private float translationX = 0;
    private float translationY = 0;
    private int paddingLeft = 0;
    private int paddingTop = 0;
    private int paddingRight = 0;
    private int paddingBottom = 0;
    private AbstractOperation.SourceHolder bitmapHolder;

    private Rect imageStageRegion = new Rect();
    private int imageWidth = -1;
    private int imageHeight = -1;
    private float startFocusRadius = 0;
    private float startFocusAngle = 0;
    private float startFocusX = 0;
    private float startFocusY = 0;
    private float currentFocusRadius = -1;
    private float currentFocusAngle = 0;
    private float currentFocusX = 0;
    private float currentFocusY = 0;
    private float indicatorAlpha = 0f;
    @NonNull
    private FocusTool.MODE focusType = FocusTool.MODE.NO_FOCUS;
    private PREVIEW_MODE previewMode = PREVIEW_MODE.FULL_FIXED;

    public PicturePreviewView(Context context) {
        this(context, null);
    }

    public PicturePreviewView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PicturePreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(LAYER_TYPE_HARDWARE, null);

        density = getResources().getDisplayMetrics().density;

        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(0xFFFFFFFF);
        linePaint.setAntiAlias(true);

        clearPaint = new Paint();
        /*clearPaint.setColor(Color.TRANSPARENT);
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        clearPaint.setAntiAlias(true);
        clearPaint.setStyle(Paint.Style.FILL);*/

        imagePaint = new Paint();
        imagePaint.setFilterBitmap(true);

        indicatorAnimation = ValueAnimator.ofFloat(1f, 0f);
        indicatorAnimation.setDuration(INDICATOR_FADE_TIME);
        indicatorAnimation.setStartDelay(INDICATOR_SHOW_TIME);

        indicatorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                indicatorAlpha = (Float) animation.getAnimatedValue();
                invalidate();
            }
        });

        this.setBackgroundColor(Color.TRANSPARENT);

        setWillNotDraw(false);

    }

    public void setPreviewMode(PREVIEW_MODE previewMode) {
        if (previewMode != this.previewMode) {
            this.previewMode = previewMode;
            invalidate();
        }
    }

    public void setFocusType(@NonNull FocusTool.MODE focusType) {
        if (!focusType.equals(this.focusType)) {
            this.focusType = focusType;
            if (previewMode.equals(PREVIEW_MODE.FOCUS)) {
                currentFocusRadius = Math.min(imageDrawRegion.width(), imageDrawRegion.height()) / 2;
                currentFocusX = getWidth() / 2;
                currentFocusY = getHeight() / 2;
                currentFocusAngle = 0;
                showIndicator(false);
            }
            invalidate();
        }
    }

    public void showIndicator(boolean stay) {
        if (stay) {
            indicatorAnimation.cancel();
            indicatorAlpha = 1f;
        } else {
            indicatorAlpha = 1f;
            indicatorAnimation.cancel();
            indicatorAnimation.start();
        }
    }

    public Paint getImagePaint() {
        return imagePaint;
    }

    /**
     * Set a Paint object that will be used for a fast preview of ColorMatrix manipulations.
     *
     * @param imagePaint the current paint object
     */
    public void setImagePaint(Paint imagePaint) {

        this.imagePaint = imagePaint;

        invalidate();
    }

    /**
     * Update the preview result.
     *
     * @param bitmapHolder the bitmapHolder contains teh image to display.
     * @param fullWidth    full source image width without downscaling
     * @param fullHeight   full source image height without downscaling
     */
    public synchronized void setImage(AbstractOperation.SourceHolder bitmapHolder, int fullWidth, int fullHeight) {
        this.imageWidth = fullWidth;
        this.imageHeight = fullHeight;
        this.bitmapHolder = bitmapHolder;

        invalidate();
    }


    private void convertCropSource(@NonNull Rect crop, @NonNull Rect stage, int canvasWidth, int canvasHeight) {

        float cropScale = crop.width() / (float) stage.width();

        crop.set(
                crop.left + Math.round((paddingLeft - stage.left) * cropScale),
                crop.top + Math.round((paddingTop - stage.top) * cropScale),
                crop.right - Math.round((paddingRight - (canvasWidth - stage.right)) * cropScale),
                crop.bottom - Math.round((paddingBottom - (canvasHeight - stage.bottom)) * cropScale)
        );
    }

    @Override
    public boolean onTouchEvent(MotionEvent rawEvent) {

        ScaledMotionEventWrapper event = new ScaledMotionEventWrapper(rawEvent, imageScale, translationX, translationY);


        if (previewMode.equals(PREVIEW_MODE.FOCUS)) {

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    showIndicator(true);
                    break;
                case MotionEvent.ACTION_UP:
                    showIndicator(false);
                    break;
            }

            if (event.isCheckpoint()) {
                startFocusRadius = getFocusRadius();
                startFocusAngle = currentFocusAngle;
                startFocusX = currentFocusX;
                startFocusY = currentFocusY;
            } else {
                ScaledMotionEventWrapper.TransformDiff diff = event.getTransformDifference();
                currentFocusRadius = startFocusRadius + diff.distanceDiff;
                currentFocusAngle = startFocusAngle + diff.angleDiff;
                currentFocusX = startFocusX + diff.xDiff;
                currentFocusY = startFocusY + diff.yDiff;

                if (imageDrawRegion.left > currentFocusX) {
                    startFocusX += imageDrawRegion.left - currentFocusX;
                    currentFocusX = imageDrawRegion.left;
                }
                if (imageDrawRegion.right < currentFocusX) {
                    startFocusX += imageDrawRegion.right - currentFocusX;
                    currentFocusX = imageDrawRegion.right;
                }
                if (imageDrawRegion.top > currentFocusY) {
                    startFocusY += imageDrawRegion.top - currentFocusY;
                    currentFocusY = imageDrawRegion.top;
                }
                if (imageDrawRegion.bottom < currentFocusY) {
                    startFocusY += imageDrawRegion.bottom - currentFocusY;
                    currentFocusY = imageDrawRegion.bottom;
                }

            }

            invalidate();

            return true;
        } else {
            return super.onTouchEvent(rawEvent);
        }
    }

    @Override
    public float getTranslationX() {
        return translationX;
    }

    @Override
    public void setTranslationX(float translationX) {
        this.translationX = translationX;
        invalidate();
    }

    @Override
    public float getTranslationY() {
        return translationY;
    }

    @Override
    public void setTranslationY(float translationY) {
        this.translationY = translationY;
        invalidate();
    }

    @Override
    public void invalidate() {
        if (ThreadUtils.thisIsUiThread()) {
            super.invalidate();
        } else {
            postInvalidate();
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        drawToCanvas(canvas, imageScale, translationX, translationY);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        this.paddingLeft = left;
        this.paddingTop = top;
        this.paddingRight = right;
        this.paddingBottom = bottom;
        invalidate();
    }

    @Override
    public int getPaddingTop() {
        return paddingTop;
    }

    @Override
    public int getPaddingBottom() {
        return paddingBottom;
    }

    @Override
    public int getPaddingLeft() {
        return paddingLeft;
    }

    @Override
    public int getPaddingRight() {
        return paddingRight;
    }

    private void drawToCanvas(@NonNull Canvas canvas, float canvasScale, float x, float y) {
        if (bitmapHolder != null) {
            final int canvasWidth = canvas.getWidth();
            final int canvasHeight = canvas.getHeight();

            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            imageDrawRegion.set(
                    (paddingLeft > imageStageRegion.left) ? paddingLeft : imageStageRegion.left,
                    (paddingTop > imageStageRegion.top) ? paddingTop : imageStageRegion.top,

                    (paddingRight > (canvasWidth - imageStageRegion.right)) ? canvasWidth - paddingRight : imageStageRegion.right,
                    (paddingBottom > (canvasHeight - imageStageRegion.bottom)) ? canvasHeight - paddingBottom : imageStageRegion.bottom
            );

            canvas.scale(canvasScale, canvasScale);
            canvas.translate(x, y);

            if (bitmapHolder.hasFullPreview()) {
                assert bitmapHolder.getFullPreview() != null;

                if (!bitmapHolder.hasBlurPreview() || focusType.equals(FocusTool.MODE.NO_FOCUS)) {

                    this.imageCropRegion.set(0, 0, bitmapHolder.getFullWidth(), bitmapHolder.getFullHeight());
                    convertCropSource(imageCropRegion, imageStageRegion, canvasWidth, canvasHeight);
                    canvas.drawBitmap(bitmapHolder.getFullPreview(), imageCropRegion, imageDrawRegion, imagePaint);

                } else if (bitmapHolder.hasBlurPreview()) {
                    assert bitmapHolder.getBlurPreview() != null;

                    renderFocus(focusType, bitmapHolder.getFullPreview(), bitmapHolder.getBlurPreview(), canvas, imagePaint, true);

                    clearEmptyRegion(canvas, imageDrawRegion);

                    drawIndicator(canvas, canvasScale);

                }
            }

            if (bitmapHolder.hasSharpPreview()) {
                float scale = imageStageRegion.width() / (float) imageWidth;

                Rect rect = bitmapHolder.getSharpRect();
                if (rect != null) {
                    Bitmap sharpBitmap = bitmapHolder.getSharpPreview();

                    Rect sourceRect = new Rect(0, 0, sharpBitmap.getWidth(), sharpBitmap.getHeight());
                    Rect destRect = new Rect(
                            (int) (rect.left * scale) + imageStageRegion.left,
                            (int) (rect.top * scale) + imageStageRegion.top,
                            (int) (rect.right * scale) + imageStageRegion.left,
                            (int) (rect.bottom * scale) + imageStageRegion.top
                    );

                    canvas.drawBitmap(sharpBitmap, sourceRect, destRect, imagePaint);
                }
            }
        }
    }

    public synchronized void renderFocus(@NonNull FocusTool.MODE type,
                                         @NonNull Bitmap sourceBitmap,
                                         @NonNull Bitmap blurBitmap,
                                         @NonNull Canvas canvas,
                                         @NonNull Paint sourceImagePaint,
                                         boolean renderFullStage) {

        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        if (canvasWidth <= -1 || canvasHeight <= -1) {
            return;
        }

        this.imageBlurRegion.set(0, 0, blurBitmap.getWidth(), blurBitmap.getHeight());
        this.imageCropRegion.set(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight());

        final Rect sourceDestination;

        if (!renderFullStage) {
            sourceDestination = imageCropRegion;
        } else {
            sourceDestination = imageDrawRegion;
            convertCropSource(imageCropRegion, imageStageRegion, canvasWidth, canvasHeight);
            convertCropSource(imageBlurRegion, imageStageRegion, canvasWidth, canvasHeight);
        }

        canvas.drawBitmap(sourceBitmap, imageCropRegion, sourceDestination, imagePaint);

        this.imageBlurRegion.set(0, 0, blurBitmap.getWidth(), blurBitmap.getHeight());
        this.imageCropRegion.set(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight());

        final Rect destination;
        final float posScale;
        final float posXDiff;
        final float posYDiff;
        if (!renderFullStage) {
            posScale = imageCropRegion.width() / (float) imageStageRegion.width();
            posXDiff = imageStageRegion.left * -posScale;
            posYDiff = imageStageRegion.top * -posScale;
            destination = imageCropRegion;
        } else {
            posScale = 1;
            posXDiff = 0;
            posYDiff = 0;
            destination = imageDrawRegion;
            convertCropSource(imageCropRegion, imageStageRegion, canvas.getWidth(), canvas.getHeight());
            convertCropSource(imageBlurRegion, imageStageRegion, canvas.getWidth(), canvas.getHeight());
        }

        final float focusFieldRadius = getFocusRadius() * posScale;

        sourceImagePaint.setStyle(Paint.Style.FILL);
        if (type == FocusTool.MODE.RADIAL) {

            focusRect.set(
                    currentFocusX * posScale - focusFieldRadius + posXDiff,
                    currentFocusY * posScale - focusFieldRadius + posYDiff,
                    currentFocusX * posScale + focusFieldRadius + posXDiff,
                    currentFocusY * posScale + focusFieldRadius + posYDiff
            );

            sourceImagePaint.setShader(
                    new RadialGradient(
                            focusRect.centerX(),
                            focusRect.centerY(),
                            focusFieldRadius,
                            new int[]{0xFFFFFFFF, 0x00FFFFFF},
                            new float[]{BLUR_FADE, 1},
                            Shader.TileMode.CLAMP
                    )
            );
            canvas.saveLayer(new RectF(0, 0, canvas.getWidth(), canvas.getHeight()), null, Canvas.ALL_SAVE_FLAG);
            canvas.drawBitmap(blurBitmap, imageBlurRegion, destination, imagePaint);
            sourceImagePaint.setXfermode(xfermodeIn);
            canvas.drawOval(focusRect, sourceImagePaint);
            canvas.restore();
        } else {

            float[] gradientPoints = translatePoints(
                    currentFocusX * posScale + posXDiff,
                    currentFocusY * posScale + posYDiff,
                    currentFocusAngle,
                    new float[]{
                            (currentFocusX * posScale) + posXDiff,
                            (currentFocusY * posScale) - focusFieldRadius + posYDiff,
                            (currentFocusX * posScale) + posXDiff,
                            (currentFocusY * posScale) + focusFieldRadius + posYDiff,

                            (currentFocusX - getWidth() * 10) * posScale + posXDiff,
                            (currentFocusY * posScale) + posYDiff,
                            (currentFocusX + getWidth() * 10) * posScale + posXDiff,
                            (currentFocusY * posScale) + posYDiff
                    }
            );

            sourceImagePaint.setShader(
                    new LinearGradient(
                            gradientPoints[0],
                            gradientPoints[1],
                            gradientPoints[2],
                            gradientPoints[3],
                            new int[]{0x00FFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0x00FFFFFF},
                            new float[]{0, 1 - BLUR_FADE, BLUR_FADE, 1},
                            Shader.TileMode.CLAMP
                    )
            );

            canvas.saveLayer(new RectF(0, 0, canvas.getWidth(), canvas.getHeight()), null, Canvas.ALL_SAVE_FLAG);
            canvas.drawBitmap(blurBitmap, imageBlurRegion, destination, imagePaint);
            sourceImagePaint.setXfermode(xfermodeIn);
            sourceImagePaint.setStrokeWidth(focusFieldRadius * 2);
            canvas.drawLine(
                    gradientPoints[4],
                    gradientPoints[5],
                    gradientPoints[6],
                    gradientPoints[7],
                    sourceImagePaint
            );
            sourceImagePaint.setStrokeWidth(1);
            canvas.restore();
        }

        sourceImagePaint.setXfermode(null);
        sourceImagePaint.setShader(null);
    }

    private void clearEmptyRegion(@NonNull Canvas canvas, @NonNull Rect leavedRegion) {
        //L, T, R, B
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        canvas.drawRect(0, 0, canvasWidth, leavedRegion.top, clearPaint);
        canvas.drawRect(0, 0, leavedRegion.left, canvasHeight, clearPaint);
        canvas.drawRect(0, leavedRegion.bottom, canvasWidth, canvasHeight, clearPaint);
        canvas.drawRect(leavedRegion.right, 0, canvasWidth, canvasHeight, clearPaint);
    }

    private void drawIndicator(@NonNull Canvas canvas, float scale) {
        if (previewMode.equals(PREVIEW_MODE.FOCUS) && indicatorAlpha > 0) {
            linePaint.setStrokeWidth((2 * density) / scale);
            linePaint.setAlpha(Math.round(255 * indicatorAlpha));
            if (focusType.equals(FocusTool.MODE.RADIAL)) {
                focusInnerRect.set(
                        focusRect.centerX() - (focusRect.width() * BLUR_FADE) / 2,
                        focusRect.centerY() - (focusRect.height() * BLUR_FADE) / 2,
                        focusRect.centerX() + (focusRect.width() * BLUR_FADE) / 2,
                        focusRect.centerY() + (focusRect.height() * BLUR_FADE) / 2
                );

                canvas.drawOval(focusInnerRect, linePaint);
            } else {
                final float focusFieldRadius = getFocusRadius();
                final float length = Math.max(getWidth(), getHeight());
                float[] linePoints = translatePoints(
                        currentFocusX,
                        currentFocusY,
                        currentFocusAngle,
                        new float[]{
                                (currentFocusX - length * 5),
                                (currentFocusY - focusFieldRadius * BLUR_FADE),
                                (currentFocusX + length * 5),
                                (currentFocusY - focusFieldRadius * BLUR_FADE),

                                (currentFocusX - length * 5),
                                (currentFocusY + focusFieldRadius * BLUR_FADE),
                                (currentFocusX + length * 5),
                                (currentFocusY + focusFieldRadius * BLUR_FADE)
                        }
                );

                canvas.drawLine(linePoints[0], linePoints[1], linePoints[2], linePoints[3], linePaint);
                canvas.drawLine(linePoints[4], linePoints[5], linePoints[6], linePoints[7], linePaint);

                RectF mid = new RectF(
                        currentFocusX - 20,
                        currentFocusY - 20,
                        currentFocusX + 20,
                        currentFocusY + 20
                );
                canvas.drawOval(mid, linePaint);
            }
        }
    }

    private float[] translatePoints(float x, float y, float angle, float[] points) {
        matrix.reset();
        matrix.preRotate(angle, x, y);
        matrix.mapPoints(points);
        return points;
    }

    private float getFocusRadius() {
        int minWidth = getWidth() / 20;
        return currentFocusRadius < minWidth ? minWidth : currentFocusRadius;
    }

    public float getScale() {
        return this.imageScale;
    }

    public void setScale(float scale) {
        this.imageScale = scale;
        invalidate();
    }

    /**
     * Set the image draw region.
     *
     * @param drawRegion draw region and aspect
     */
    public void setImageRect(Rect drawRegion) {
        this.imageStageRegion = drawRegion;
        invalidate();
    }
}
