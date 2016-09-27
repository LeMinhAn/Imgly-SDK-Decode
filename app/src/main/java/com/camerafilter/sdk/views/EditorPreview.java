package com.camerafilter.sdk.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import com.camerafilter.R;
import com.camerafilter.sdk.brush.models.Painting;
import com.camerafilter.sdk.configuration.AbstractConfig;
import com.camerafilter.sdk.configuration.CropAspectConfig;
import com.camerafilter.sdk.configuration.ImageLoadOperation;
import com.camerafilter.sdk.configuration.ImageStickerConfig;
import com.camerafilter.sdk.configuration.PhotoEditorSdkConfig;
import com.camerafilter.sdk.configuration.TextStickerConfig;
import com.camerafilter.sdk.cropper.cropwindow.CropOverlayView;
import com.camerafilter.sdk.cropper.util.ImageViewUtil;
import com.camerafilter.sdk.operator.AbstractOperation;
import com.camerafilter.sdk.operator.Operator;
import com.camerafilter.sdk.tools.FocusTool;
import com.camerafilter.sdk.tools.StickerTool;
import com.camerafilter.sdk.tools.TextTool;
import com.camerafilter.ui.utilities.OrientationSensor;
import com.camerafilter.utils.SetHardwareAnimatedViews;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class EditorPreview extends FrameLayout implements OrientationSensor.OrientationListener, LayerContainerView.OnStickerSelectionCallback {

    enum MODE {
        NORMAL,
        CROP,
        BRUSH,
        FOCUS,
        STICKER
    }

    @NonNull
    private final CropOverlayView cropOverlayView;
    @NonNull
    private final LayerContainerView layerContainerView;
    @NonNull
    private final PicturePreviewView resultView;
    @NonNull
    private final TextTool.Options textOptionToolPanel;
    @NonNull
    private final StickerTool.Options stickerOptionToolPanel;
    @NonNull
    private final PaintLayer paintLayer;
    @NonNull
    private MODE currentMode = MODE.NORMAL;
    private Operator operator;
    private boolean initialized = false;
    private int imageSourceWidth = -1;
    private int imageSourceHeight = -1;
    private int imageSourceRotation = 0;
    private int imageRotation = 0;
    private int screenRotation = 0;
    private boolean imageFlipVertical = false;
    private boolean imageFlipHorizontal = false;
    private float panScale = 1f;
    private float scaleImage = 1f;
    private float scaleY = 1f;
    @Nullable
    private AbstractConfig.AspectConfigInterface currentAspectConfig = null;
    private int aspectRotation;
    private PanelBindCallback panelBindCallback;
    @Nullable
    private OnImageReadyCallback onResultImageReadyCallback;

    public EditorPreview(Context context) {
        this(context, null);
    }

    public EditorPreview(Context context, AttributeSet attrs) {
        super(context, attrs);

        final LayoutInflater inflater = LayoutInflater.from(context);
        final View v = inflater.inflate(R.layout.imgly_editor_preview_view, this, true);

        resultView = (PicturePreviewView) v.findViewById(R.id.imageResultView);
        cropOverlayView = (CropOverlayView) v.findViewById(R.id.cropOverlayView);
        layerContainerView = (LayerContainerView) v.findViewById(R.id.stickerHolderView);

        paintLayer = layerContainerView.getPaintLayer();

        layerContainerView.setTextStickerSelectionCallback(this);
        enableStickerSelection(true);

        textOptionToolPanel = new TextTool.Options();
        stickerOptionToolPanel = new StickerTool.Options();

        operator = getOperator();

        enableCropMode(false);
    }

    public void enableStickerSelection(boolean enable) {

        layerContainerView.enableSelectableMode(enable);
    }

    public void bringPaintLayerToFront() {
        layerContainerView.bringPaintLayerToFront();
    }

    public Operator getOperator() {
        if (operator == null) {
            operator = new $Operator(this);
        }
        return operator;
    }

    public void enableCropMode(final boolean enable) {

        if (enable) {
            currentMode = MODE.CROP;
            enableStickerSelection(false);
        } else if (currentMode == MODE.CROP) {
            currentMode = MODE.NORMAL;
            enableStickerSelection(true);
        }

        invalidateCrop(false);
        cropOverlayView.enableEditorMode(enable);
        cropOverlayView.setAlpha(enable ? 0f : 1f);
        post(new Runnable() {
            @Override
            public void run() {
                float alpha = 1f;
                float scale = enable ? 0.9f : 1f;
                AnimatorSet animator = new AnimatorSet();
                animator.addListener(new SetHardwareAnimatedViews(EditorPreview.this, cropOverlayView));
                animator.playTogether(
                        ObjectAnimator.ofFloat(EditorPreview.this, "panScale", getPanScale(), scale),
                        ObjectAnimator.ofFloat(cropOverlayView, "alpha", cropOverlayView.getAlpha(), alpha)
                );
                animator.setDuration(500);
                animator.start();
            }
        });

    }

    public void invalidateCrop(boolean instant) {

        AnimatorSet set = new AnimatorSet();

        final int[] padding = calculateImagePadding();

        final float holderScale;

        final float x;
        final float y;

        ImageLoadOperation operation = operator.getImageLoadOperation();
        operation.setSharpRect(getCropRect(imageSourceWidth, imageSourceHeight));

        if (currentMode == MODE.CROP || (resultView.getWidth() <= 10)) {
            x = 0;
            y = 0;
            holderScale = 1;
        } else {
            Rect image = getImageRect();
            Rect crop = getCropRect(image.width(), image.height());

            int stageWidth = resultView.getWidth();
            int stageHeight = resultView.getHeight();

            Rect holderRect = ImageViewUtil.getBitmapRectCenterInside(crop.width(), crop.height(), stageWidth, stageHeight);

            holderScale = Math.min(holderRect.width() / (float) crop.width(), holderRect.height() / (float) crop.height());

            x = -crop.left - (stageWidth - image.width()) / 2 + (holderRect.left / holderScale);
            y = -crop.top - (stageHeight - image.height()) / 2 + (holderRect.top / holderScale);
        }

        set.playTogether(
                ObjectAnimator.ofFloat(layerContainerView, "scale", layerContainerView.getScale(), holderScale),
                ObjectAnimator.ofFloat(layerContainerView, "translationX", layerContainerView.getTranslationX(), x),
                ObjectAnimator.ofFloat(layerContainerView, "translationY", layerContainerView.getTranslationY(), y),

                ObjectAnimator.ofFloat(resultView, "scale", resultView.getScale(), holderScale),
                ObjectAnimator.ofFloat(resultView, "translationX", resultView.getTranslationX(), x),
                ObjectAnimator.ofFloat(resultView, "translationY", resultView.getTranslationY(), y),

                createPaddingAnimator(resultView, padding[0], padding[1], padding[2], padding[3]),
                createPaddingAnimator(layerContainerView, padding[0], padding[1], padding[2], padding[3])
        );

        set.addListener(new SetHardwareAnimatedViews(resultView, layerContainerView));
        set.setInterpolator(new LinearInterpolator());
        set.setDuration(instant ? 0 : 400);
        set.start();
    }

    public float getPanScale() {
        return panScale;
    }

    public void setPanScale(float panScale) {
        this.panScale = panScale;
        super.setScaleX(scaleImage * panScale);
        super.setScaleY(scaleImage * panScale);
    }

    @NonNull
    private int[] calculateImagePadding() {
        return calculateImagePadding(resultView.getWidth(), resultView.getHeight());
    }

    public Rect getCropRect(int imageWidth, int imageHeight) {
        return cropOverlayView.getCropRect(imageWidth, imageHeight);
    }

    public Rect getImageRect() {
        return cropOverlayView.getImageRect();
    }

    private ValueAnimator createPaddingAnimator(@NonNull final View target, final int toLeftPadding, final int toTopPadding, final int toRightPadding, final int toBottomPadding) {

        final int[] fromPadding = new int[]{
                target.getPaddingLeft(),
                target.getPaddingTop(),
                target.getPaddingRight(),
                target.getPaddingBottom()
        };

        final int[] relativePadding = new int[]{
                toLeftPadding - fromPadding[0],
                toTopPadding - fromPadding[1],
                toRightPadding - fromPadding[2],
                toBottomPadding - fromPadding[3]
        };

        final ValueAnimator animation = ValueAnimator.ofFloat(0f, 1f);
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
                float progress = (Float) valueAnimator.getAnimatedValue();

                final int left = fromPadding[0] + (int) (relativePadding[0] * progress);
                final int top = fromPadding[1] + (int) (relativePadding[1] * progress);
                final int right = fromPadding[2] + (int) (relativePadding[2] * progress);
                final int bottom = fromPadding[3] + (int) (relativePadding[3] * progress);

                target.setPadding(left, top, right, bottom);
            }
        });
        animation.setInterpolator(new LinearInterpolator());
        return animation;
    }

    @NonNull
    private int[] calculateImagePadding(int w, int h) {

        final int[] imagePadding = new int[4];
        final int[] stickerPadding = new int[4];

        Rect cropRect = getCropRect(w, h);
        Rect imageRect = getImageRect();

        if (imageRect == null) {
            imageRect = new Rect();
        }
        if (cropRect == null || cropRect.left > w) {
            cropRect = imageRect;
        }

        switch (currentMode) {
            case CROP:
                imagePadding[0] = imageRect.left;
                imagePadding[1] = imageRect.top;
                imagePadding[2] = w - imageRect.right;
                imagePadding[3] = h - imageRect.bottom;
                stickerPadding[0] = imageRect.left;
                stickerPadding[1] = imageRect.top;
                stickerPadding[2] = w - imageRect.right;
                stickerPadding[3] = h - imageRect.bottom;
                break;
            default:
                imagePadding[0] = cropRect.left;
                imagePadding[1] = cropRect.top;
                imagePadding[2] = w - cropRect.right;
                imagePadding[3] = h - cropRect.bottom;
                stickerPadding[0] = imageRect.left;
                stickerPadding[1] = imageRect.top;
                stickerPadding[2] = w - imageRect.right;
                stickerPadding[3] = h - imageRect.bottom;
                break;
        }


        return new int[]{
                imagePadding[0],
                imagePadding[1],
                imagePadding[2],
                imagePadding[3],
                stickerPadding[0],
                stickerPadding[1],
                stickerPadding[2],
                stickerPadding[3]
        };
    }

    protected void setResultBitmap(AbstractOperation.ResultHolder result) {
        AbstractOperation.SourceHolder source = (AbstractOperation.SourceHolder) result;
        if (source.hasFullPreview()) {
            resultView.setImage(source, imageSourceWidth, imageSourceHeight);
        }
    }

    @NonNull
    protected LayerContainerView getStickerStage() {
        return layerContainerView;
    }

    public RectF getCropRectState() {
        return cropOverlayView.getCropRectState();
    }

    public void restoreCropRectState(RectF rect) {
        cropOverlayView.restoreCropRectState(rect);
    }

    private void setScreenRotation(int rotation) {
        this.screenRotation = rotation;
        setImageRotation(imageRotation, imageFlipHorizontal, imageFlipVertical);
    }

    @Override
    public void onTextStickerSelected(TextStickerConfig config, boolean isNew) {
        dispatchEnterToolMode(textOptionToolPanel, true);
    }

    public void dispatchEnterToolMode(AbstractConfig.ToolConfigInterface tool, boolean replace) {
        if (panelBindCallback != null) {
            panelBindCallback.enterToolMode(tool, replace);
        }
    }

    @Override
    public void onImageStickerSelected(ImageStickerConfig config, boolean isNew) {
        dispatchEnterToolMode(stickerOptionToolPanel, !isNew);
    }

    @Override
    public void onNoneStickerSelected() {
        if (textOptionToolPanel.isAttached() || stickerOptionToolPanel.isAttached()) {
            dispatchLeaveToolMode(false);
        }
    }

    public void dispatchLeaveToolMode(boolean revertChanges) {
        if (panelBindCallback != null) {
            panelBindCallback.leaveToolMode(revertChanges);
        }
    }

    public Painting getPainting() {
        return paintLayer.getPainting();
    }

    public void setImageRotation(int imageRotation, boolean flipHorizontal, boolean flipVertical) {
        int rotation = (imageRotation + screenRotation + imageSourceRotation) % 360;

        final float currentRotation = getRotation() % 360;
        final float destinationRotation = (Math.abs(currentRotation - rotation) <= 180) ? rotation : ((currentRotation > rotation) ? 360 + rotation : rotation - 360);

        final boolean normalImageOrientation = imageRotation % 180 == 0;
        final boolean normalOrientation = destinationRotation % 180 == 0;

        final Rect image = getImageRect();
        final Rect crop = getCropRect(image.width(), image.height());

        final Rect holderRect = ImageViewUtil.getBitmapRectCenterInside(crop.width(), crop.height(), normalOrientation ? resultView.getWidth() : resultView.getHeight(), normalOrientation ? resultView.getHeight() : resultView.getWidth());

        float scale = Math.max((float) holderRect.width() / (float) resultView.getWidth(), (float) holderRect.height() / (float) resultView.getHeight());

        final AnimatorSet set = new AnimatorSet();

        final Collection<Animator> animations = new HashSet<>();

        if (destinationRotation != currentRotation) {
            animations.add(ObjectAnimator.ofFloat(this, "rotation", currentRotation, destinationRotation));
            animations.add(ObjectAnimator.ofFloat(this, "imageScale", getImageScale(), scale));
        }

        if (imageFlipVertical != flipVertical || imageFlipHorizontal != flipHorizontal) {
            animations.add(ObjectAnimator.ofFloat(resultView, "scaleX", resultView.getScaleX(), 1 / 4f, 1f));
            animations.add(ObjectAnimator.ofFloat(resultView, "scaleY", resultView.getScaleY(), 1 / 4f, 1f));
        }

        if ((normalImageOrientation && imageFlipVertical != flipVertical) || (!normalImageOrientation && imageFlipHorizontal != flipHorizontal)) {
            // Flip around x-axis
            final int xFlip = (Math.signum(resultView.getRotationX()) == 0) ? 180 : 0;

            animations.add(ObjectAnimator.ofFloat(resultView, "rotationX", resultView.getRotationX(), xFlip));
            animations.add(ObjectAnimator.ofFloat(layerContainerView, "rotationX", layerContainerView.getRotationX(), xFlip));
            cropOverlayView.setRotationX(xFlip);
        } else if ((!normalImageOrientation && imageFlipVertical != flipVertical) || (normalImageOrientation && imageFlipHorizontal != flipHorizontal)) {
            // Flip around y-axis
            final int yFlip = (Math.signum(resultView.getRotationY()) == 0) ? 180 : 0;

            animations.add(ObjectAnimator.ofFloat(resultView, "rotationY", resultView.getRotationY(), yFlip));
            animations.add(ObjectAnimator.ofFloat(layerContainerView, "rotationY", layerContainerView.getRotationY(), yFlip));
            cropOverlayView.setRotationY(yFlip);
        }

        set.playTogether(animations);

        set.addListener(new SetHardwareAnimatedViews(this));

        post(new Runnable() {
            @Override
            public void run() {
                set.start();
                invalidateAspectRation();
            }
        });

        this.imageRotation = imageRotation;
        this.imageFlipVertical = flipVertical;
        this.imageFlipHorizontal = flipHorizontal;
    }

    public void setSourceImagePath(String imagePath) {
        ImageLoadOperation operation = operator.getImageLoadOperation();

        operation.setSourceImagePath(imagePath);

        imageSourceWidth = operation.getImageWidth();
        imageSourceHeight = operation.getImageHeight();
        imageSourceRotation = operation.getImageAngle();

        post(new Runnable() {
            @Override
            public void run() {
                setScreenRotation(EditorPreview.this.screenRotation);
            }
        });


        invalidate();
    }

    private Paint getPreviewPaint() {
        return resultView.getImagePaint();
    }

    private void setPreviewPaint(Paint paint) {
        resultView.setImagePaint(paint);
    }

    public void invalidateOperations() {
        if (initialized) {
            operator.runPreviewOperations();
        }
    }

    /**
     * Sets the aspect config values of the aspectRatio.
     *
     * @param aspect a Aspect config that specifies the new aspect ratio and resolution
     */
    public void setAspectRatio(AbstractConfig.AspectConfigInterface aspect) {
        this.currentAspectConfig = aspect;
        this.aspectRotation = imageRotation;
        invalidateAspectRation();

    }

    /**
     * Get the current config that best match currentAspect and rotation.
     * For ex. a 4 / 3 aspect will be converted to a 3 / 4 aspect when possible.
     *
     * @return a conversion of the current aspect based on the image rotation.
     */
    @Nullable
    public AbstractConfig.AspectConfigInterface getCurrentRotationBasedAspect() {
        if (currentAspectConfig == null) {
            final AbstractConfig.AspectConfigInterface forcedCrop;
            if (imageSourceWidth / (float) imageSourceHeight > 1) {
                forcedCrop = PhotoEditorSdkConfig.getForceLandscapeCrop();
            } else {
                forcedCrop = PhotoEditorSdkConfig.getForcePortraitCrop();
            }

            if (forcedCrop != null) {
                setAspectRatio(forcedCrop);
            } else {
                if (PhotoEditorSdkConfig.getCropConfig().size() == 0) {
                    PhotoEditorSdkConfig.getCropConfig().add(new CropAspectConfig(R.string.imgly_crop_name_custom, R.drawable.imgly_icon_option_crop_custom, CropAspectConfig.CUSTOM_ASPECT));
                }
                AbstractConfig.AspectConfigInterface startCrop = PhotoEditorSdkConfig.getCropConfig().get(0);
                for (AbstractConfig.AspectConfigInterface cropConfig : PhotoEditorSdkConfig.getCropConfig()) {
                    if (cropConfig.getAspect() == CropAspectConfig.CUSTOM_ASPECT) {
                        startCrop = cropConfig;
                        break;
                    }
                }
                setAspectRatio(startCrop);
            }
        }

        final AbstractConfig.AspectConfigInterface aspect;

        if (imageRotation % 180 == aspectRotation % 180) {
            aspect = currentAspectConfig;
        } else {
            AbstractConfig.AspectConfigInterface newAspect = null;
            float findAspectRation = (aspectRotation % 180 == 0) ? 1 / currentAspectConfig.getAspect() : currentAspectConfig.getAspect();
            float shortestEpsilon = 0.1f;
            for (AbstractConfig.AspectConfigInterface compareAspectConfig : PhotoEditorSdkConfig.getCropConfig()) {
                float compareAspect = compareAspectConfig.getAspect();
                if (compareAspect == CropAspectConfig.CUSTOM_ASPECT) {
                    newAspect = compareAspectConfig;
                } else if (Math.abs(findAspectRation - compareAspect) < shortestEpsilon
                        && currentAspectConfig.hasSpecificSize() == compareAspectConfig.hasSpecificSize()
                        && (!currentAspectConfig.hasSpecificSize()
                        || (currentAspectConfig.getCropWidth() == compareAspectConfig.getCropHeight()
                        && currentAspectConfig.getCropHeight() == compareAspectConfig.getCropWidth()
                )
                )
                        ) {
                    shortestEpsilon = Math.abs(findAspectRation - compareAspect);
                    newAspect = compareAspectConfig;
                }
            }

            if (newAspect != null) {
                aspect = newAspect;
            } else {
                AbstractConfig.AspectConfigInterface freeCrop = currentAspectConfig;
                for (AbstractConfig.AspectConfigInterface cropConfig : PhotoEditorSdkConfig.getCropConfig()) {
                    if (cropConfig.getAspect() == CropAspectConfig.CUSTOM_ASPECT) {
                        freeCrop = cropConfig;
                        break;
                    }
                }
                currentAspectConfig = freeCrop;
                aspect = currentAspectConfig;
            }
        }
        return aspect;
    }

    private void invalidateAspectRation() {
        AbstractConfig.AspectConfigInterface aspect = getCurrentRotationBasedAspect();

        final boolean normalImageOrientation = imageRotation % 180 == 0;
        float aspectRation = (normalImageOrientation) ? aspect.getAspect() : 1 / aspect.getAspect();

        if (aspect.getAspect() == CropAspectConfig.CUSTOM_ASPECT) {
            cropOverlayView.setFixedAspectRatio(false);
        } else {
            cropOverlayView.setAspectRatio(aspectRation);
            cropOverlayView.setFixedAspectRatio(true);
        }
        invalidateCrop(false);
    }

    @NonNull
    private PicturePreviewView getResultView() {
        return resultView;
    }

    public void setFocusType(FocusTool.MODE type) {
        resultView.setFocusType(type);
    }

    public void enableFocusMode(boolean enable) {

        if (enable) {
            currentMode = MODE.FOCUS;
            resultView.setPreviewMode(PicturePreviewView.PREVIEW_MODE.FOCUS);
            enableStickerSelection(false);
        } else if (currentMode == MODE.FOCUS) {
            currentMode = MODE.NORMAL;
            resultView.setPreviewMode(PicturePreviewView.PREVIEW_MODE.PAN_AND_ZOOM);
            enableStickerSelection(true);
        }
    }

    public void enableBrushMode(final boolean enable) {
        if (enable) {
            currentMode = MODE.BRUSH;
            enableStickerSelection(false);
        } else if (currentMode == MODE.BRUSH) {
            currentMode = MODE.NORMAL;
            enableStickerSelection(true);
        }

        paintLayer.enableEditorMode(enable);
        post(new Runnable() {
            @Override
            public void run() {
                float scale = enable ? 0.9f : 1f;
                AnimatorSet animator = new AnimatorSet();
                animator.addListener(new SetHardwareAnimatedViews(EditorPreview.this));
                animator.playTogether(
                        ObjectAnimator.ofFloat(EditorPreview.this, "panScale", getPanScale(), scale)
                );
                animator.setDuration(500);
                animator.start();
            }
        });
    }

    public float getImageScale() {
        return scaleImage;
    }

    public void setImageScale(float scaleImageScale) {
        this.scaleImage = scaleImageScale;
        super.setScaleX(scaleImageScale * panScale);
        super.setScaleY(scaleImageScale * panScale);
    }

    public void setPanX(int x) {
        this.setTranslationX(x);
    }

    public void setPanY(int y) {
        this.setTranslationY(y);
    }

    public void addSticker(ImageStickerConfig config) {
        layerContainerView.addStickerView(config);
    }

    public void addSticker(TextStickerConfig config) {
        layerContainerView.addStickerView(config);
    }

    public void refreshSticker(TextStickerConfig config) {
        layerContainerView.refreshStickerView(config);
    }

    @Nullable
    public TextStickerConfig getCurrentTextConfig() {
        AbstractConfig.StickerConfigInterface configInterface = layerContainerView.getCurrentStickerConfig();
        if (configInterface != null && configInterface.getType() == AbstractConfig.StickerConfigInterface.STICKER_TYPE.TEXT) {
            return (TextStickerConfig) layerContainerView.getCurrentStickerConfig();
        } else {
            return null;
        }
    }

    public void flipSticker(boolean vertical) {
        layerContainerView.flipSticker(vertical);
    }

    public void deleteSticker() {
        layerContainerView.deleteSticker();
        dispatchLeaveToolMode(false);
    }

    public void bringStickerToFront() {
        layerContainerView.bringStickerToFront();
    }

    public void leaveSticker() {
        layerContainerView.leaveSticker();
    }

    private boolean hasSourceImageSize() {
        return imageSourceWidth > 0 && imageSourceHeight > 0;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        initialized = true;
        operator.getImageLoadOperation().setPreviewSize(w, h);

        if (hasSourceImageSize()) {
            final Rect imageRect = ImageViewUtil.getBitmapRectCenterInside(imageSourceWidth, imageSourceHeight, w, h);

            resultView.setImageRect(imageRect);
            cropOverlayView.setImageRect(imageRect);

            setAspectRatio(getCurrentRotationBasedAspect());

            post(new Runnable() {
                @Override
                public void run() {
                    invalidateCrop(true);
                    setScreenRotation(OrientationSensor.getScreenOrientation().getRotation());
                }
            });
        } else {
            cropOverlayView.resetImageRect();
        }

        operator.getImageShowOperation();
        operator.runPreviewOperations();
    }

    public void onFinalResultSaved() {
        if (onResultImageReadyCallback != null) {
            String outputPath = operator.getImageSaveOperation().getOutputPath();

            onResultImageReadyCallback.onImageReadyCallback(outputPath);
            onResultImageReadyCallback = null;
        }
    }

    public void saveFinalImage(String exportPath, OnImageReadyCallback callback) {
        this.onResultImageReadyCallback = callback;
        operator.getImageSaveOperation().setOutputPath(exportPath);
        operator.runExportOperations();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!isInEditMode()) {
            OrientationSensor.getInstance().addListener(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!isInEditMode()) {
            OrientationSensor.getInstance().removeListener(this);
        }
    }

    public void setPanelBindCallback(PanelBindCallback panelBindCallback) {
        this.panelBindCallback = panelBindCallback;
    }

    @Override
    public void onOrientationChange(@NonNull OrientationSensor.ScreenOrientation screenOrientation) {
        setScreenRotation(screenOrientation.getRotation());
    }

    public interface OnImageReadyCallback {
        void onImageReadyCallback(String path);
    }

    public interface PanelBindCallback {
        void leaveToolMode(boolean revertChanges);

        void enterToolMode(AbstractConfig.ToolConfigInterface tool, boolean replace);
    }

    @Deprecated
    protected static class ProtectedAccessor {
        public static void setResultBitmap(@NonNull EditorPreview editor, AbstractOperation.ResultHolder result) {
            editor.setResultBitmap(result);
        }

        @NonNull
        public static LayerContainerView getStickerStage(@NonNull EditorPreview editor) {
            return editor.getStickerStage();
        }

        public static Paint getPreviewPaint(@NonNull EditorPreview editor) {
            return editor.getPreviewPaint();
        }

        public static void setPreviewPaint(@NonNull EditorPreview editor, Paint paint) {
            editor.setPreviewPaint(paint);
        }

        @NonNull
        public static PicturePreviewView getResultView(@NonNull EditorPreview editor) {
            return editor.getResultView();
        }
    }

    private final class $Operator extends Operator {
        private $Operator(EditorPreview editorPreview) {
            super(editorPreview);
        }
    }
}
