package com.camerafilter.sdk.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.camerafilter.sdk.brush.drawer.PaintingDrawer;
import com.camerafilter.sdk.brush.models.Painting;
import com.camerafilter.sdk.brush.views.PaintPreview;
import com.camerafilter.sdk.cropper.util.ImageViewUtil;
import com.camerafilter.utils.ScaledMotionEventWrapper;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class PaintLayer extends FrameLayout implements LayerContainerView.Layer {

    private Painting paintingStore = null;
    private float brushScale = 1f;
    private float brushTranslationX = 0f;
    private float brushTranslationY = 0f;

    private Rect drawRegion = new Rect();
    private final Paint clearPaint;

    private int paddingLeft = 0;
    private int paddingTop = 0;
    private int paddingRight = 0;
    private int paddingBottom = 0;

    private final Matrix drawLayerMatrix = new Matrix();

    public PaintLayer(Context context) {
        this(context, null);
    }

    public PaintLayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaintLayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        paintingStore = new Painting();

        addView(new PaintPreview(getContext(), paintingStore), new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        enableEditorMode(false);

        setPivotX(0);
        setPivotY(0);

        clearPaint = new Paint();
        clearPaint.setColor(Color.TRANSPARENT);
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        clearPaint.setAntiAlias(true);
        clearPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public boolean onTouchEvent(MotionEvent rawEvent) {
        if (!isEnabled()) return super.onTouchEvent(rawEvent);

        ScaledMotionEventWrapper event = new ScaledMotionEventWrapper(rawEvent, 1, 0, 0);

        if (event.getPointerCount() == 1) {
            if (event.isCheckpoint()) {
                paintingStore.startPaintChunk();
            }
            paintingStore.addPoint(event.getX(0), event.getY(0));

            if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                paintingStore.finalizePaintChunk();
            }
        }

        return true;
    }

    public void enableEditorMode(boolean enable){
        setEnabled(enable);
    }

    @Override
    public void setTranslationX(float translationX) {
        this.brushTranslationX = translationX;
        super.setTranslationX(this.brushTranslationX  * brushScale);
    }

    @Override
    public void setTranslationY(float translationY) {
        this.brushTranslationY = translationY;
        super.setTranslationY(this.brushTranslationY * brushScale);
    }

    public void setScale(float scale) {
        this.brushScale = scale;
        super.setScaleX(this.brushScale);
        super.setScaleY(this.brushScale);
        super.setTranslationX(this.brushTranslationX * brushScale);
        super.setTranslationY(this.brushTranslationY * brushScale);
    }

    @Override
    public void rescaleCache(float scaleDown) {

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);
        super.dispatchDraw(canvas);
        clearEmptyRegion(canvas);
        canvas.restore();
    }

    public void setDrawRegion(Rect drawRegion) {
        this.drawRegion = drawRegion;
        invalidate();
    }

    public void drawLayerScaledToCanvas(@NonNull Canvas canvas, int x, int y) {
        Rect imageRect = ImageViewUtil.getBitmapRectCenterInside(canvas.getWidth(), canvas.getHeight(), getWidth(), getHeight());

        final float scale = Math.min(canvas.getWidth() / (float) imageRect.width(), canvas.getHeight() / (float) imageRect.height());

        x -= imageRect.left;
        y -= imageRect.top;

        drawLayerMatrix.postTranslate(x, y);
        drawLayerMatrix.postScale(scale, scale);

        canvas.save();
        canvas.setMatrix(drawLayerMatrix);

        new PaintingDrawer(paintingStore).draw(canvas, false, scale);

        canvas.restore();
    }

    public Painting getPainting() {
        return paintingStore;
    }

    /*
    TODO: implement empty region cleanup
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        clearEmptyRegion(canvas, drawRegion);
    }*/

    private void clearEmptyRegion(@NonNull Canvas canvas) {
        //L, T, R, B
        int canvasWidth  = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        Rect leavedRegion = new Rect(
                paddingLeft,
                paddingTop,
                canvasWidth  - paddingRight,
                canvasHeight - paddingBottom
        );

        canvas.drawRect(0,                  0,                   canvasWidth,       leavedRegion.top, clearPaint);
        canvas.drawRect(0,                  0,                   leavedRegion.left, canvasHeight,     clearPaint);
        canvas.drawRect(0,                  leavedRegion.bottom, canvasWidth,       canvasHeight,     clearPaint);
        canvas.drawRect(leavedRegion.right, 0,                   canvasWidth,       canvasHeight,     clearPaint);
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
    public int getPaddingLeft() {
        return paddingLeft;
    }

    @Override
    public int getPaddingRight() {
        return paddingRight;
    }

    @Override
    public int getPaddingBottom() {
        return paddingBottom;
    }

}

