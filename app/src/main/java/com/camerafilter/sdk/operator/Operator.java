package com.camerafilter.sdk.operator;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.camerafilter.sdk.configuration.ImageLoadOperation;
import com.camerafilter.sdk.filter.NoneImageFilter;
import com.camerafilter.sdk.views.EditorPreview;

import java.util.Collection;
import java.util.TreeSet;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class Operator extends TreeSet<AbstractOperation> {

    public enum Priority {
        Load,
        Enhancement,
        Effect,
        ColorMatrix,
        Focus,
        Brush,
        Sticker,
        Crop,
        Orientation,
        Show,
        Save
    }

    @NonNull
    private final CropOperation cropOperation;
    @NonNull
    private final FocusOperation focusOperation;
    @NonNull
    private final FilterOperation filterOperation;
    @NonNull
    private final RotateOperation rotateOperation;
    @NonNull
    private final StickerOperation stickerOperation;
    private ImageLoadOperation imageLoadOperation;
    private ImageShowOperation imageShowOperation;
    private ImageSaveOperation imageSaveOperation;
    @NonNull
    private final ColorMatrixOperation colorMatrixOperation;

    private volatile boolean duringOperation = false;
    private volatile boolean postInvalidate  = false;
    private volatile boolean postExportInvalidate = false;

    private final EditorPreview editorPreview;

    private boolean isPreviewMode = true;

    protected void invalidate() {
        runPreviewOperations();
    }
    protected boolean isInPreviewMode() {
        return isPreviewMode;
    }

    protected Operator(EditorPreview editorPreview) {

        colorMatrixOperation = new ColorMatrixOperation();
        focusOperation       = new FocusOperation();
        cropOperation        = new CropOperation();
        filterOperation      = new FilterOperation(new NoneImageFilter());
        rotateOperation      = new RotateOperation();
        stickerOperation     = new StickerOperation();

        this.editorPreview = editorPreview;
    }

    /**
     * Get the image save Operation.
     * @return Image save operation.
     */
    public ImageSaveOperation getImageSaveOperation() {
        if(imageSaveOperation == null) {
            imageSaveOperation = new ImageSaveOperation();
            bindOperation(imageSaveOperation);
        }
        return imageSaveOperation;
    }

    /**
     * Get the color matrix Operation.
     * @return Color matrix Operation.
     */
    @NonNull
    public ColorMatrixOperation getColorMatrixOperation() {
        bindOperation(colorMatrixOperation);
        return colorMatrixOperation;
    }

    /**
     * Get the blur Operation.
     * @return Blur Operation.
     */
    @NonNull
    public FocusOperation getFocusOperation() {
        bindOperation(focusOperation);
        return focusOperation;
    }

    /**
     * Get the crop Operation.
     * @return Crop Operation.
     */
    @NonNull
    public CropOperation getCropOperation() {
        bindOperation(cropOperation);
        return cropOperation;
    }

    /**
     * Get the filter Operation.
     * @return Filter Operation.
     */
    @NonNull
    public FilterOperation getFilterOperation() {
        bindOperation(filterOperation);
        return filterOperation;
    }

    /**
     * Get the rotation Operation.
     * @return Rotation Operation.
     */
    @NonNull
    public RotateOperation getRotateOperation() {
        bindOperation(rotateOperation);
        return rotateOperation;
    }

    /**
     * Get the sticker Operation.
     * @return Sticker Operation.
     */
    @NonNull
    public StickerOperation getStickerOperation() {
        bindOperation(stickerOperation);
        return stickerOperation;
    }

    /**
     * Get the image show Operation.
     * @return Image show Operation.
     */
    public ImageShowOperation getImageShowOperation() {
        if (imageShowOperation == null) {
            imageShowOperation = new ImageShowOperation();
            bindOperation(imageShowOperation);
        }
        return imageShowOperation;
    }

    /**
     * Get the image load Operation.
     * @return Image load operation
     */
    public ImageLoadOperation getImageLoadOperation() {
        if (imageLoadOperation == null) {
            imageLoadOperation = new ImageLoadOperation();
            bindOperation(imageLoadOperation);
        }
        return imageLoadOperation;
    }


    @SuppressWarnings("deprecation")
    private void bindOperation(@NonNull AbstractOperation operation) {
        remove(operation);
        add(operation);
    }


    protected boolean readyToRun() {
        return imageLoadOperation != null && contains(imageLoadOperation) && imageLoadOperation.isReady();
    }

    /**
     * Request a fast Preview rendering.
     */
    public synchronized void runPreviewOperations() {
        if (readyToRun()) {
            if (!duringOperation) {
                isPreviewMode = true;
                invalidateOperations();
            } else {
                postInvalidate = true;
            }
        }
    }

    /**
     * Request a export rendering with full resolution.
     */
    public synchronized void runExportOperations() {
        if (readyToRun()) {
            if (!duringOperation) {
                isPreviewMode = false;
                invalidateOperations();
            } else {
                postExportInvalidate = true;
            }
        }
    }

    @Nullable
    protected AbstractOperation.SourceHolder getSourceBitmapHolder(AbstractOperation operation) {
        AbstractOperation preOperation = operation;
        while (preOperation != null) {
            preOperation = lower(preOperation);
            if (preOperation != null && preOperation.hasBitmapHolder()) {
                return (AbstractOperation.SourceHolder) preOperation.getResultBitmapHolder();
            }
        }
        return null;
    }

    protected void invalidateOperations() {
        BackgroundRunner runner = new BackgroundRunner();
        runner.start();
    }

    @Deprecated
    @Override
    public boolean add(@NonNull AbstractOperation operation) {
        operation.setCallback(this);
        if (operation instanceof AbstractEditorOperation) {

            AbstractEditorOperation canvasOperation = (AbstractEditorOperation) operation;
            canvasOperation.init(editorPreview);

        }

        return super.add(operation);
    }

    @Deprecated
    @Override
    public boolean remove(Object object) {
        return super.remove(object);
    }

    @Deprecated
    @Override
    public boolean addAll(Collection<? extends AbstractOperation> collection) {
        return super.addAll(collection);
    }

    @Deprecated
    @Override
    public AbstractOperation ceiling(AbstractOperation abstractOperation) {
        return super.ceiling(abstractOperation);
    }

    @Deprecated
    @Override
    public boolean removeAll(Collection<?> collection) {
        return super.removeAll(collection);
    }

    private class BackgroundRunner extends Thread implements Runnable, AbstractOperation.OperationDoneListener{

        private AbstractOperation currentOperation;

        @NonNull
        private final Handler mainHandler;

        private volatile boolean blocking = true;

        public BackgroundRunner() {
            mainHandler = new Handler(Looper.getMainLooper());
            currentOperation = first();
            setPriority(MIN_PRIORITY);
        }

        @Override
        public final void run() {
            duringOperation = true;
            while ((duringOperation || postInvalidate || postExportInvalidate) && !interrupted() && !isInterrupted()) {
                if (!duringOperation) {
                    isPreviewMode = postInvalidate;
                }
                postInvalidate = false;
                duringOperation = true;

                while (currentOperation != null && !interrupted() && !isInterrupted()) {
                    final AbstractOperation.MODE mode = currentOperation.getOperationMode();

                    if (mode == AbstractOperation.MODE.BACKGROUND_THREAD) {
                        currentOperation.doOperation(this);
                    } else {
                        blocking = mode == AbstractOperation.MODE.BLOCKING_MAIN_THREAD;

                        //noinspection deprecation
                        mainHandler.post(currentOperation.getOperationRunnable(this));

                        while (blocking && !interrupted() && !isInterrupted()) try {
                            Thread.sleep(1);
                        } catch (InterruptedException ignored) {}
                    }

                    currentOperation = higher(currentOperation);
                }

                duringOperation = false;
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignored) {}
            }
        }

        @Override
        public void operationDone(AbstractOperation operation) {
            blocking = false;
        }
    }

}
