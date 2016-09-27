package com.camerafilter.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.camerafilter.ImgLySdk;
import com.camerafilter.R;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class RelativeBlurLayout extends RelativeLayout {
    final static int SAVE_LAYER_FLAGS = Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG;

    private static final String TAG = "RelativeBlurLayout";
    private static RenderScript rs;
    static {
        try {
            rs = ImgLySdk.getAppRsContext();
        } catch (Exception ignored) {}
    }
    private final boolean updateOnInvalidate;
    private final int[] sourceScreenPos = new int[2];
    private final int blurDownSample;

    @NonNull
    private final Paint paint;
    @NonNull
    private final Rect source;
    @NonNull
    private final Rect destination;
    private final float borderRadius;

    private View blurSourceView;
    private float blurRadius;
    private Bitmap blurBitmap;
    private boolean isSourceRoot = false;
    private Canvas canvas;
    private Drawable background;

    public RelativeBlurLayout(@NonNull final Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RelativeBlurLayout, 0, 0);

        blurRadius = a.getDimension(R.styleable.RelativeBlurLayout_blurRadius, 10);
        if (blurRadius > 25) {
            blurRadius = 25;
        }

        blurDownSample = Math.round(a.getInteger(R.styleable.RelativeBlurLayout_blurDownSample, 6) * 2);
        updateOnInvalidate = a.getBoolean(R.styleable.RelativeBlurLayout_blurUpdateOnInvalidate, false);
        final boolean updateOnStart = !isInEditMode() && a.getBoolean(R.styleable.RelativeBlurLayout_blurUpdateAtStart, true);

        TypedArray borderRadiusConfig = context.getTheme().obtainStyledAttributes(attrs, R.styleable.BorderRadius, 0, 0);
        borderRadius = borderRadiusConfig.getDimension(R.styleable.BorderRadius_borderRadius, 0);

        isSourceRoot = a.getBoolean(R.styleable.RelativeBlurLayout_blurRootSource, false);
        final int sourceResId = a.getResourceId(R.styleable.RelativeBlurLayout_blurSource, R.color.transparent);

        paint = new Paint();
        paint.setFilterBitmap(true);

        source = new Rect();
        destination = new Rect();

        setWillNotDraw(false);

        if (!isSourceRoot && sourceResId == R.color.transparent) {
            throw new RuntimeException("You must be define a BlurSource");
        }
        if (!isInEditMode()) {
            post(new Runnable() {
                @Override
                public void run() {
                    try {

                        if (isSourceRoot) {
                            blurSourceView = (View) getRootView().findViewById(android.R.id.content).getParent();
                        } else {
                            blurSourceView = getRootView().findViewById(sourceResId);
                        }

                        blurSourceView.getLocationOnScreen(sourceScreenPos);

                    } catch (Exception ignore) {
                    }

                    if (updateOnStart) {
                        updateBlur();
                    }
                }
            });
        }
        a.recycle();
    }

    @NonNull
    private static Bitmap renderBlur(@NonNull RenderScript rs, @NonNull Bitmap source, float blurRadius) {
        try {
            final Allocation input = Allocation.createFromBitmap(rs, source);
            final Allocation output = Allocation.createTyped(rs, input.getType());
            final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            //final ScriptIntrinsicConvolve5x5 script = ScriptIntrinsicConvolve5x5.create(rs, Element.U8_4(rs));
            //final ScriptIntrinsicConvolve5x5 script = ScriptIntrinsicConvolve5x5.create(rs, Element.RGBA_8888(rs));
            script.setRadius(blurRadius);
            script.setInput(input);
            script.forEach(output);
            output.copyTo(source);

        } catch (Exception ignored) {
            //shit happens, so no blurring
        }
        return source;
    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility == VISIBLE) {
            updateBlur();
        }
        super.setVisibility(visibility);
    }

    @Override
    public void setY(float y) {
        super.setY(y);
    }

    @Override
    public void setTranslationX(float translationX) {
        super.setTranslationX(translationX);
        invalidate();
    }

    @Override
    public void setTranslationY(float translationY) {
        super.setTranslationY(translationY);
        invalidate();
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {

        if (borderRadius != 0) {
            Path clipPath = new Path();
            clipPath.addRoundRect(new RectF(0, 0, canvas.getWidth(), canvas.getHeight()), borderRadius, borderRadius, Path.Direction.CW);
            try {
                canvas.clipPath(clipPath);
            } catch (Exception ignored) {
                // Workaround for
                // java.lang.UnsupportedOperationException at android.view.GLES20Canvas.clipPath(GLES20Canvas.java:435)
                // Version is Available since API 1!
            }
        }

        if (blurBitmap != null && !blurBitmap.isRecycled()) {
            int[] screenPos = new int[2];
            this.getLocationOnScreen(screenPos);

            screenPos[0] -= sourceScreenPos[0];
            screenPos[1] -= sourceScreenPos[1];

            source.set(
                    Math.round(screenPos[0] / (float) blurDownSample),
                    Math.round(screenPos[1] / (float) blurDownSample),
                    Math.round((screenPos[0] + canvas.getWidth()) / (float) blurDownSample),
                    Math.round((screenPos[1] + canvas.getHeight()) / (float) blurDownSample)
            );

            destination.set(
                    0,
                    0,
                    Math.max(1, Math.min((blurBitmap.getWidth() - source.left) * blurDownSample, canvas.getWidth())),
                    Math.max(1, Math.min((blurBitmap.getHeight() - source.top) * blurDownSample, canvas.getHeight()))
            );

            canvas.drawBitmap(blurBitmap, source, destination, paint);
        }


        if (background == null) {
            background = getBackground();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                setBackground(null);
            } else {
                //noinspection deprecation
                setBackgroundDrawable(null);
            }
        }
        if (background != null) {
            background.draw(canvas);
        }

        super.dispatchDraw(canvas);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateBlur();
    }

    @Override
    protected void onDetachedFromWindow() {
        //blurSourceView = null;
        super.onDetachedFromWindow();
    }

    public void setBlurSourceView(@NonNull View blurSourceView) {
        this.blurSourceView = blurSourceView;
        blurSourceView.getLocationOnScreen(sourceScreenPos);

    }

    public void updateBlur(@NonNull View blurSourceView) {
        setBlurSourceView(blurSourceView);
        updateBlur();
    }

    public void updateBlur() {
        int originalVisibility = getVisibility();
        super.setVisibility(View.INVISIBLE);

        if (blurSourceView != null) {
            Bitmap sourceBitmap = drawViewToBitmap(blurSourceView, blurDownSample);
            new BlurTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, sourceBitmap);
        }
        super.setVisibility(originalVisibility);
    }

    public Bitmap drawViewToBitmap(@NonNull View view, int downSampling) {
        final float scale = 1f / downSampling;
        final int viewWidth = view.getWidth();
        final int viewHeight = view.getHeight();
        final int bmpWidth = Math.round(viewWidth * scale);
        final int bmpHeight = Math.round(viewHeight * scale);

        if (bmpHeight < 1) { // Prevent errors of Paused Activity while Creating snapshot.
            return Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8);
        } else {

            Bitmap destination = blurBitmap;
            if (destination == null || destination.getWidth() != bmpWidth || destination.getHeight() != bmpHeight) {
                destination = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
                canvas = new Canvas(destination);

                if (downSampling > 1) {
                    canvas.scale(scale, scale);
                }
            }

            canvas.drawColor(Color.BLACK);

            if (view instanceof ViewGroup) {
                invalidateRecursive((ViewGroup) view);
            }
            try {
                view.draw(canvas);
            } catch (Exception ignored) {
                // This fix a strange NullPointerException in android.view.View.draw on some older devices
            }
            view.destroyDrawingCache();

            return destination;
        }
    }

    public void invalidateRecursive(@NonNull ViewGroup layout) {
        final int count = layout.getChildCount();
        View child;
        for (int i = 0; i < count; i++) {
            child = layout.getChildAt(i);
            if (child instanceof ViewGroup)
                invalidateRecursive((ViewGroup) child);
            else
                child.invalidate();
        }
    }

    private class BlurTask extends AsyncTask<Bitmap, Void, Bitmap> {

        @Nullable
        Context context;

        @NonNull
        @Override
        protected Bitmap doInBackground(Bitmap... bitmaps) {
            return renderBlur(rs, bitmaps[0], blurRadius);
        }

        @Override
        protected void onPreExecute() {
            context = getContext();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            blurBitmap = bitmap;
            invalidate();
            context = null;
        }
    }

}

