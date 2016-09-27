package com.camerafilter.sdk.cropper.cropwindow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.camerafilter.sdk.cropper.cropwindow.edge.Edge;
import com.camerafilter.sdk.cropper.cropwindow.handle.Handle;
import com.camerafilter.sdk.cropper.util.AspectRatioUtil;
import com.camerafilter.sdk.cropper.util.HandleUtil;
import com.camerafilter.sdk.cropper.util.ImageViewUtil;
import com.camerafilter.sdk.cropper.util.PaintUtil;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class CropOverlayView extends View {

    private static final Rect EMPTY_RECT = new Rect();

    private static final boolean DEFAULT_FIXED_ASPECT_RATIO = false;
    private static final float DEFAULT_ASPECT_RATIO = 1;
    private static final int DEFAULT_GUIDELINES = 1;

    private static final int SNAP_RADIUS_DP = 6;
    private static final float DEFAULT_SHOW_GUIDELINES_LIMIT = 100;

    // Gets default values from PaintUtil, sets a bunch of values such that the
    // corners will draw correctly
    private static final float DEFAULT_CORNER_THICKNESS_DP = PaintUtil.getCornerThickness();
    private static final float DEFAULT_LINE_THICKNESS_DP   = PaintUtil.getLineThickness();
    private static final float DEFAULT_CORNER_OFFSET_DP    = (DEFAULT_CORNER_THICKNESS_DP / 2) - (DEFAULT_LINE_THICKNESS_DP / 2);
    private static final float DEFAULT_CORNER_EXTENSION_DP = (DEFAULT_CORNER_THICKNESS_DP / 2) + (DEFAULT_CORNER_OFFSET_DP);
    private static final float DEFAULT_CORNER_LENGTH_DP    = 20;

    // mGuidelines enumerations
    private static final int GUIDELINES_OFF = 0;
    private static final int GUIDELINES_ON_TOUCH = 1;
    private static final int GUIDELINES_ON = 2;

    // Member Variables ////////////////////////////////////////////////////////

    // The Paint used to draw the white rectangle around the crop area.
    private Paint mBorderPaint;

    // The Paint used to draw the guidelines within the crop area when pressed.
    private Paint mGuidelinePaint;

    // The Paint used to draw the corners of the Border
    private Paint mCornerPaint;

    // The Paint used to darken the surrounding areas outside the crop area.
    private Paint mBackgroundPaint;

    // The bounding box around the Bitmap that we are cropping.
    private Rect imageRect;

    private Rect canvasRect;

    // The radius of the touch zone (in pixels) around a given Handle.
    private float mHandleRadius;

    // An edge of the crop window will snap to the corresponding edge of a
    // specified bounding box when the crop window edge is less than or equal to
    // this distance (in pixels) away from the bounding box edge.
    private float mSnapRadius;

    // Holds the x and y offset between the exact touch location and the exact
    // handle location that is activated. There may be an offset because we
    // allow for some leeway (specified by mHandleRadius) in activating a
    // handle. However, we want to maintain these offset values while the handle
    // is being dragged so that the handle doesn't jump.
    @Nullable
    private Pair<Float, Float> mTouchOffset;

    // The Handle that is currently pressed; null if no Handle is pressed.
    @Nullable
    private Handle mPressedHandle;

    // Flag indicating if the crop area should always be a certain aspect ratio
    // (indicated by mTargetAspectRatio).
    private boolean isAspectRatioFix = DEFAULT_FIXED_ASPECT_RATIO;

    // The aspect ratio that the crop area should maintain; this variable is
    // only used when mMaintainAspectRatio is true.
    private float mTargetAspectRatio = DEFAULT_ASPECT_RATIO;

    // Instance variables for customizable attributes
    private int mGuidelines = DEFAULT_GUIDELINES;

    // Whether the Crop View has been initialized for the first time
    private boolean initializedCropWindow = false;

    // Instance variables for the corner values
    private float mCornerExtension;
    private float mCornerOffset;
    private float mCornerLength;

    public CropOverlayView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public CropOverlayView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);
        // Initialize the crop window here because we need the size of the view
        // to have been determined.
        canvasRect = new Rect(0,0, width, height);
        initCropWindow(imageRect);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {

        super.onDraw(canvas);

        if (imageRect != null) {
            // Draw translucent background for the cropped area.
            drawBackground(canvas, imageRect);
        }

        if (showGuidelines()) {
            // Determines whether guidelines should be drawn or not
            if (mGuidelines == GUIDELINES_ON) {
                drawRuleOfThirdsGuidelines(canvas);
            } else if (mGuidelines == GUIDELINES_ON_TOUCH) {
                // Draw only when resizing
                if (mPressedHandle != null)
                    drawRuleOfThirdsGuidelines(canvas);
            } else if (mGuidelines == GUIDELINES_OFF) {
                // Do nothing
            }
        }

        // Draws the main crop window border.
        canvas.drawRect(Edge.LEFT.getCoordinate(),
                Edge.TOP.getCoordinate(),
                Edge.RIGHT.getCoordinate(),
                Edge.BOTTOM.getCoordinate(),
                mBorderPaint);

        drawCorners(canvas);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {

        // If this View is not enabled, don't allow for touch interactions.
        if (!isEnabled()) {
            return false;
        }

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                onActionDown(event.getX(), event.getY());
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                onActionUp();
                return true;

            case MotionEvent.ACTION_MOVE:
                onActionMove(event.getX(), event.getY());
                getParent().requestDisallowInterceptTouchEvent(true);
                return true;

            default:
                return false;
        }
    }




    public void setImageRect(Rect imageRect) {
        this.imageRect = imageRect;
        initCropWindow(this.imageRect);
    }

    public Rect getImageRect(){
        return this.imageRect;
    }


    @NonNull
    public RectF getCropRectState() {
        return new RectF(Edge.LEFT.getCoordinate(), Edge.TOP.getCoordinate(), Edge.RIGHT.getCoordinate(), Edge.BOTTOM.getCoordinate());
    }

    public void restoreCropRectState(@Nullable RectF rect) {
        if (rect != null) {
            Edge.LEFT.setCoordinate(rect.left);
            Edge.TOP.setCoordinate(rect.top);
            Edge.RIGHT.setCoordinate(rect.right);
            Edge.BOTTOM.setCoordinate(rect.bottom);
        }
    }

    @NonNull
    public Rect getCropRect(int imageWidth, int imageHeight) {

        int width  = getMeasuredWidth();
        int height = getMeasuredHeight();

        if(getParent() != null) {
            View parent = (View) getParent();
            if (width <= 0) {
                width = parent.getWidth();
            }
            if (height <= 0) {
                height = parent.getHeight();
            }
        }

        final Rect displayedImageRect = ImageViewUtil.getBitmapRectCenterInside(imageWidth, imageHeight, width, height);

        // Get the scale factor between the actual Bitmap dimensions and the
        // displayed dimensions for width.
        final float scaleFactorWidth    = (float) imageWidth / displayedImageRect.width();

        // Get the scale factor between the actual Bitmap dimensions and the
        // displayed dimensions for height.
        final float scaleFactorHeight    = (float) imageHeight / displayedImageRect.height();

        // Get crop window position relative to the displayed image.
        final float cropWindowX      = Edge.LEFT.getCoordinate() - displayedImageRect.left;
        final float cropWindowY      = Edge.TOP.getCoordinate()  - displayedImageRect.top;
        final float cropWindowWidth  = Edge.getWidth();
        final float cropWindowHeight = Edge.getHeight();
        // Scale the crop window position to the actual size of the Bitmap.
        final float actualCropX      = cropWindowX * scaleFactorWidth;
        final float actualCropY      = cropWindowY * scaleFactorHeight;
        final float actualCropWidth  = cropWindowWidth * scaleFactorWidth;
        final float actualCropHeight = cropWindowHeight * scaleFactorHeight;

        return new Rect(
                (int) (actualCropX),
                (int) (actualCropY),
                (int) (actualCropWidth  + actualCropX),
                (int) (actualCropHeight + actualCropY)
        );
    }

    public void resetImageRect() {
        setImageRect(EMPTY_RECT);
    }

    public void resetCropOverlayView() {

        if (initializedCropWindow) {
            initCropWindow(imageRect);
            invalidate();
        }
    }

    /**
     * Sets the guidelines for the CropOverlayView to be either on, off, or to
     * show when resizing the application.
     *
     * @param guidelines Integer that signals whether the guidelines should be
     *            on, off, or only showing when resizing.
     */
    public void setGuidelines(int guidelines)
    {
        if (guidelines < 0 || guidelines > 2)
            throw new IllegalArgumentException("Guideline value must be set between 0 and 2. See documentation.");
        else {
            mGuidelines = guidelines;

            if (initializedCropWindow) {
                initCropWindow(imageRect);
                invalidate();
            }
        }
    }

    /**
     * Sets whether the aspect ratio is fixed or not; true fixes the aspect
     * ratio, while false allows it to be changed.
     *
     * @param fixAspectRatio Boolean that signals whether the aspect ratio
     *            should be maintained.
     */
    public void setFixedAspectRatio(boolean fixAspectRatio){
        if (isAspectRatioFix == fixAspectRatio) {
            return;
        }
        isAspectRatioFix = fixAspectRatio;

        if (initializedCropWindow) {
            if (canvasRect != null) {
                initCropWindow(getCropRect(canvasRect.width(), canvasRect.height()));
                invalidate();
            }
        }
    }


    public void setAspectRatio(float aspectRatio) {
        if (mTargetAspectRatio == aspectRatio) {
            return;
        }
        if (aspectRatio <= 0)
            throw new IllegalArgumentException("Cannot set aspect ratio value to a number less than or equal to 0.");
        else {
            mTargetAspectRatio = aspectRatio;

            if (initializedCropWindow) {
                initCropWindow(imageRect);
                invalidate();
            }
        }
    }

    private void init(@NonNull Context context) {

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        mHandleRadius = HandleUtil.getTargetRadius(context);

        mCornerPaint     = PaintUtil.newCornerPaint(context);
        mBorderPaint     = PaintUtil.newBorderPaint(context);
        mGuidelinePaint  = PaintUtil.newGuidelinePaint();
        mBackgroundPaint = PaintUtil.newBackgroundPaint(context);

        mSnapRadius      = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, SNAP_RADIUS_DP,              displayMetrics);
        mCornerOffset    = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_CORNER_OFFSET_DP,    displayMetrics);
        mCornerLength    = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_CORNER_LENGTH_DP,    displayMetrics);
        mCornerExtension = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_CORNER_EXTENSION_DP, displayMetrics);

        // Sets guidelines to default until specified otherwise
        mGuidelines = DEFAULT_GUIDELINES;
    }

    /**
     * Set the initial crop window size and position. This is dependent on the
     * size and position of the image being cropped.
     *
     * @param bitmapRect the bounding box around the image being cropped
     */
    private void initCropWindow(@NonNull Rect bitmapRect) {

        if (!initializedCropWindow) {
            initializedCropWindow = true;
        }

        if (isAspectRatioFix) {

            // If the image aspect ratio is wider than the crop aspect ratio,
            // then the image height is the determining initial length. Else,
            // vice-versa.
            if (AspectRatioUtil.calculateAspectRatio(bitmapRect) > mTargetAspectRatio) {

                Edge.TOP.setCoordinate(bitmapRect.top);
                Edge.BOTTOM.setCoordinate(bitmapRect.bottom);

                final float centerX = getWidth() / 2f;

                // Limits the aspect ratio to no less than 40 wide or 40 tall
                final float cropWidth = Math.max(Edge.MIN_CROP_LENGTH_PX,
                        AspectRatioUtil.calculateWidth(Edge.TOP.getCoordinate(),
                                Edge.BOTTOM.getCoordinate(),
                                mTargetAspectRatio));

                // Create new TargetAspectRatio if the original one does not fit
                // the screen
                if (cropWidth == Edge.MIN_CROP_LENGTH_PX)
                    mTargetAspectRatio = (Edge.MIN_CROP_LENGTH_PX) / (Edge.BOTTOM.getCoordinate() - Edge.TOP.getCoordinate());

                final float halfCropWidth = cropWidth / 2f;
                Edge.LEFT.setCoordinate(centerX - halfCropWidth);
                Edge.RIGHT.setCoordinate(centerX + halfCropWidth);

            } else {

                Edge.LEFT.setCoordinate(bitmapRect.left);
                Edge.RIGHT.setCoordinate(bitmapRect.right);

                final float centerY = getHeight() / 2f;

                // Limits the aspect ratio to no less than 40 wide or 40 tall
                final float cropHeight = Math.max(Edge.MIN_CROP_LENGTH_PX,
                        AspectRatioUtil.calculateHeight(Edge.LEFT.getCoordinate(),
                                Edge.RIGHT.getCoordinate(),
                                mTargetAspectRatio));

                // Create new TargetAspectRatio if the original one does not fit
                // the screen
                if (cropHeight == Edge.MIN_CROP_LENGTH_PX)
                    mTargetAspectRatio = (Edge.RIGHT.getCoordinate() - Edge.LEFT.getCoordinate()) / Edge.MIN_CROP_LENGTH_PX;

                final float halfCropHeight = cropHeight / 2f;
                Edge.TOP.setCoordinate(centerY - halfCropHeight);
                Edge.BOTTOM.setCoordinate(centerY + halfCropHeight);
            }

        } else { // ... do not fix aspect ratio...

            // Initialize crop window to have 10% padding w/ respect to image.
            final float horizontalPadding = 0.0f;// * bitmapRect.width();
            final float verticalPadding = 0.0f;// * bitmapRect.height();

            if (!isInEditMode()) {
                Edge.LEFT.setCoordinate(bitmapRect.left + horizontalPadding);
                Edge.TOP.setCoordinate(bitmapRect.top + verticalPadding);
                Edge.RIGHT.setCoordinate(bitmapRect.right - horizontalPadding);
                Edge.BOTTOM.setCoordinate(bitmapRect.bottom - verticalPadding);
            }
        }
    }

    /**
     * Indicates whether the crop window is small enough that the guidelines
     * should be shown. Public because this function is also used to determine
     * if the center handle should be focused.
     *
     * @return boolean Whether the guidelines should be shown or not
     */
    public static boolean showGuidelines() {
        return !((Math.abs(Edge.LEFT.getCoordinate() - Edge.RIGHT.getCoordinate()) < DEFAULT_SHOW_GUIDELINES_LIMIT)
                || (Math.abs(Edge.TOP.getCoordinate() - Edge.BOTTOM.getCoordinate()) < DEFAULT_SHOW_GUIDELINES_LIMIT));
    }

    private void drawRuleOfThirdsGuidelines(@NonNull Canvas canvas) {

        final float left = Edge.LEFT.getCoordinate();
        final float top = Edge.TOP.getCoordinate();
        final float right = Edge.RIGHT.getCoordinate();
        final float bottom = Edge.BOTTOM.getCoordinate();

        // Draw vertical guidelines.
        final float oneThirdCropWidth = Edge.getWidth() / 3;

        final float x1 = left + oneThirdCropWidth;
        canvas.drawLine(x1, top, x1, bottom, mGuidelinePaint);
        final float x2 = right - oneThirdCropWidth;
        canvas.drawLine(x2, top, x2, bottom, mGuidelinePaint);

        // Draw horizontal guidelines.
        final float oneThirdCropHeight = Edge.getHeight() / 3;

        final float y1 = top + oneThirdCropHeight;
        canvas.drawLine(left, y1, right, y1, mGuidelinePaint);
        final float y2 = bottom - oneThirdCropHeight;
        canvas.drawLine(left, y2, right, y2, mGuidelinePaint);
    }

    private void drawBackground(@NonNull Canvas canvas, @NonNull Rect bitmapRect) {

        final float left = Edge.LEFT.getCoordinate();
        final float top = Edge.TOP.getCoordinate();
        final float right = Edge.RIGHT.getCoordinate();
        final float bottom = Edge.BOTTOM.getCoordinate();

        /*-
          -------------------------------------
          |                top                |
          -------------------------------------
          |      |                    |       |
          |      |                    |       |
          | left |                    | right |
          |      |                    |       |
          |      |                    |       |
          -------------------------------------
          |              bottom               |
          -------------------------------------
         */

        // Draw "top", "bottom", "left", then "right" quadrants.
        canvas.drawRect(bitmapRect.left, bitmapRect.top, bitmapRect.right, top, mBackgroundPaint);
        canvas.drawRect(bitmapRect.left, bottom, bitmapRect.right, bitmapRect.bottom, mBackgroundPaint);
        canvas.drawRect(bitmapRect.left, top, left, bottom, mBackgroundPaint);
        canvas.drawRect(right, top, bitmapRect.right, bottom, mBackgroundPaint);
    }

    private void drawCorners(@NonNull Canvas canvas) {

        final float left = Edge.LEFT.getCoordinate();
        final float top = Edge.TOP.getCoordinate();
        final float right = Edge.RIGHT.getCoordinate();
        final float bottom = Edge.BOTTOM.getCoordinate();

        // Draws the corner lines

        // Top left
        canvas.drawLine(left - mCornerOffset,
                top - mCornerExtension,
                left - mCornerOffset,
                top + mCornerLength,
                mCornerPaint);
        canvas.drawLine(left, top - mCornerOffset, left + mCornerLength, top - mCornerOffset, mCornerPaint);

        // Top right
        canvas.drawLine(right + mCornerOffset,
                top - mCornerExtension,
                right + mCornerOffset,
                top + mCornerLength,
                mCornerPaint);
        canvas.drawLine(right, top - mCornerOffset, right - mCornerLength, top - mCornerOffset, mCornerPaint);

        // Bottom left
        canvas.drawLine(left - mCornerOffset,
                bottom + mCornerExtension,
                left - mCornerOffset,
                bottom - mCornerLength,
                mCornerPaint);
        canvas.drawLine(left,
                bottom + mCornerOffset,
                left + mCornerLength,
                bottom + mCornerOffset,
                mCornerPaint);

        // Bottom left
        canvas.drawLine(right + mCornerOffset,
                bottom + mCornerExtension,
                right + mCornerOffset,
                bottom - mCornerLength,
                mCornerPaint);
        canvas.drawLine(right,
                bottom + mCornerOffset,
                right - mCornerLength,
                bottom + mCornerOffset,
                mCornerPaint);

    }

    /**
     * Handles a {@link MotionEvent#ACTION_DOWN} event.
     *
     * @param x the x-coordinate of the down action
     * @param y the y-coordinate of the down action
     */
    private void onActionDown(float x, float y) {

        final float left = Edge.LEFT.getCoordinate();
        final float top = Edge.TOP.getCoordinate();
        final float right = Edge.RIGHT.getCoordinate();
        final float bottom = Edge.BOTTOM.getCoordinate();

        mPressedHandle = HandleUtil.getPressedHandle(x, y, left, top, right, bottom, mHandleRadius);

        if (mPressedHandle == null)
            return;

        // Calculate the offset of the touch point from the precise location
        // of the handle. Save these values in a member variable since we want
        // to maintain this offset as we drag the handle.
        mTouchOffset = HandleUtil.getOffset(mPressedHandle, x, y, left, top, right, bottom);

        invalidate();
    }

    /**
     * Handles a {@link MotionEvent#ACTION_UP} or
     * {@link MotionEvent#ACTION_CANCEL} event.
     */
    private void onActionUp() {

        if (mPressedHandle == null)
            return;

        mPressedHandle = null;

        invalidate();
    }

    /**
     * Handles a {@link MotionEvent#ACTION_MOVE} event.
     *
     * @param x the x-coordinate of the move event
     * @param y the y-coordinate of the move event
     */
    private void onActionMove(float x, float y) {

        if (mPressedHandle == null)
            return;

        // Adjust the coordinates for the finger position's offset (i.e. the
        // distance from the initial touch to the precise handle location).
        // We want to maintain the initial touch's distance to the pressed
        // handle so that the crop window size does not "jump".
        x += mTouchOffset.first;
        y += mTouchOffset.second;

        // Calculate the new crop window size/position.
        if (isAspectRatioFix) {
            mPressedHandle.updateCropWindow(x, y, mTargetAspectRatio, imageRect, mSnapRadius);
        } else {
            mPressedHandle.updateCropWindow(x, y, imageRect, mSnapRadius);
        }
        invalidate();
    }

    public void enableEditorMode(boolean enable) {
        setVisibility(enable ? View.VISIBLE : View.INVISIBLE);
        setEnabled(enable);
    }

    public void setSize(int width, int height) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        layoutParams.width  = width;
        layoutParams.height = height;
        setLayoutParams(layoutParams);
    }
}

