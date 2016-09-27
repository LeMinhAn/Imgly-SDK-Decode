package com.camerafilter.sdk.operator;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.renderscript.ScriptIntrinsicResize;
import android.renderscript.Type;
import android.support.annotation.NonNull;

import com.camerafilter.ImgLySdk;
import com.camerafilter.sdk.tools.FocusTool;
import com.camerafilter.sdk.views.PicturePreviewView;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class FocusOperation extends AbstractEditorOperation {

    private static final RenderScript rs = ImgLySdk.getAppRsContext();

    private float intensity = 0.5f;
    private ScriptIntrinsicBlur scriptBlur;
    private ScriptIntrinsicResize scriptResize;
    private FocusTool.MODE focusMode = FocusTool.MODE.NO_FOCUS;

    public FocusOperation() {
        super();
    }

    /**
     * Get blur intensity.
     *
     * @return intensity from 0.0 - 1.0.
     */
    public float getIntensity() {
        return intensity;
    }

    /**
     * Set blur intensity.
     *
     * @param intensity intensity from 0.0 - 1.0.
     */
    public void setIntensity(float intensity) {
        this.intensity = intensity;
        invalidateState();
    }

    /*public void setFastMode(boolean fastMode) {
        this.isFastMode = fastMode;
    }*/

    public FocusTool.MODE getFocusMode() {
        return focusMode;
    }

    public void setFocusMode(FocusTool.MODE focusMode) {
        this.focusMode = focusMode;
        invalidateState();
    }

    @NonNull
    @Override
    protected Operator.Priority getPriority() {
        return Operator.Priority.Focus;
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
        ResultHolder result = getResultBitmapHolder();
        SourceHolder source = getSourceBitmapHolder();

        if (isInPreviewMode()) {
            if (result.needRenderFullResult()) {
                result.setFullResult(source.getFullPreview());
            }
            if (intensity != 0) {
                Bitmap sourceBitmap = source.getFullPreview();
                Bitmap resultBitmap = renderBlur(sourceBitmap, true);
                result.setBlurResult(resultBitmap);
            }
        } else {
            if (intensity != 0 && focusMode != FocusTool.MODE.NO_FOCUS) {
                if (result.needRenderFullResult()) {
                    result.setFullResult(source.getFullPreview());
                }
                Bitmap sourceBitmap = source.getFullPreview();
                Bitmap blurBitmap = renderBlur(sourceBitmap, false);

                PicturePreviewView focusEditor = EditorProtectedAccessor.getFocusEditor(getEditor());

                Bitmap resultBitmap = Bitmap.createBitmap(sourceBitmap.getWidth(), sourceBitmap.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(resultBitmap);
                focusEditor.renderFocus(focusMode, sourceBitmap, blurBitmap, canvas, new Paint(), false);

                if (result.needRenderFullResult()) {
                    result.setFullResult(resultBitmap);
                }
            } else {
                if (source.hasFullPreview()) {
                    result.setFullResult(source.getFullPreview());
                }
                if (source.hasBlurPreview()) {
                    result.setBlurResult(source.getBlurPreview());
                }
                if (source.hasSharpPreview()) {
                    result.setFullResult(source.getSharpPreview());
                }
            }
        }

        return true;
    }

    public Bitmap renderBlur(@NonNull Bitmap bitmap, boolean fast) {

        float blurRadius = intensity * (Math.max(bitmap.getWidth(), bitmap.getHeight()) / (float) 50);
        if (blurRadius <= 0) {
            return bitmap;
        } else {

            try {
                ScriptIntrinsicBlur scriptBlur = getBlurScript();
                ScriptIntrinsicResize scriptResize = getResizeScript();

                final int sourceWidth = bitmap.getWidth();
                final int sourceHeight = bitmap.getHeight();

                final float blur = (blurRadius > 20) ? 20 : blurRadius;
                final float resize = (blurRadius / 20) + 1;

                int resWidth = resize < 1 ? sourceWidth : Math.round(sourceWidth / resize);
                int resHeight = resize < 1 ? sourceHeight : Math.round(sourceHeight / resize);

                resWidth += 16 - resWidth % 16; //TODO

                Bitmap outputBitmap = Bitmap.createBitmap(resWidth, resHeight, bitmap.getConfig());

                Allocation allocIn = Allocation.createFromBitmap(rs, bitmap);
                Allocation allocOut = Allocation.createFromBitmap(rs, outputBitmap);
                Allocation allocSized = Allocation.createTyped(rs, Type.createXY(rs, Element.RGBA_8888(rs), resWidth, resHeight));

                scriptResize.setInput(allocIn);
                scriptResize.forEach_bicubic(allocSized);

                scriptBlur.setRadius(blur);
                scriptBlur.setInput(allocSized);
                scriptBlur.forEach(allocOut);

                allocOut.copyTo(outputBitmap);

                allocIn.destroy();
                allocSized.destroy();
                allocOut.destroy();

                if (resize > 1 && !fast) {
                    Bitmap scaledOutput = Bitmap.createScaledBitmap(outputBitmap, sourceWidth, sourceHeight, true);
                    outputBitmap.recycle();
                    return scaledOutput;
                } else {
                    return outputBitmap;
                }
            } catch (Exception e) {
                return bitmap;
            }

        }
    }

    private ScriptIntrinsicBlur getBlurScript() {
        if (scriptBlur == null) {
            scriptBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        }
        return scriptBlur;
    }

    private ScriptIntrinsicResize getResizeScript() {
        if (scriptResize == null) {
            scriptResize = ScriptIntrinsicResize.create(rs);
        }
        return scriptResize;
    }


}


