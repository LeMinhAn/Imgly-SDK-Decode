package com.camerafilter.sdk.operator;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.camerafilter.sdk.models.OperationCacheHolder;
import com.camerafilter.sdk.models.OperationResultHolder;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public abstract class AbstractOperation implements Comparable<AbstractOperation> {

    /**
     * Operation execution mode
     */
    public enum MODE {
        /**
         * Run in sequence on a Background thread.
         */
        BACKGROUND_THREAD,
        /**
         * Run in sequence and async on the main thread, the Background Thread do not wait for a result
         */
        INSTANT_MAIN_THREAD,
        /**
         * Run in sequence and async on the main thread, but the Background Thread wait until call of operationDone()
         */
        BLOCKING_MAIN_THREAD
    }

    protected abstract @NonNull
    Operator.Priority getPriority();
    protected abstract String getIdentifier();

    @Nullable
    private OperationDoneListener listener;

    private Operator operator;

    @NonNull
    private final OperationCacheHolder bitmapCacheHolder;

    @Nullable
    private OperationResultHolder resultHolder;

    AbstractOperation() {
        bitmapCacheHolder = new OperationCacheHolder(getIdentifier());
    }

    /**
     * Get the Image render Operator
     * @return Operator
     */
    protected Operator getOperator() {
        return operator;
    }

    /**
     * Return the current execution mode of this operator.
     * @see MODE
     * @return return a operation mode.
     * {@inheritDoc}
     */
    public abstract MODE getOperationMode();
    protected abstract boolean doOperation();

    /**
     * get the current render mode
     * @return true if the operation only must display a preview and false if a full resolution
     * result is requested.
     */
    protected boolean isInPreviewMode() {
        return operator.isInPreviewMode();
    }

    /**
     * Start the Operation
     * @param listener a callback for the Mode #MODE.BLOCKING_MAIN_THREAD, in other modes the callback will never fired
     * @return false if the operation not callable because the operation is already running.
     */
    public synchronized boolean doOperation(@Nullable OperationDoneListener listener) {
        MODE mode = getOperationMode();
        if (this.listener == null && (mode != MODE.BLOCKING_MAIN_THREAD || listener != null)) {
            this.listener = listener;
            if (mode == MODE.BLOCKING_MAIN_THREAD) {
                doOperation();
                listener.operationDone(this);
            } else {
                doOperation();
                this.listener = null;
            }

            return true;
        }
        return false;
    }

    /**
     * Get whether the Operator has a result
     * @return true if it has a Image result
     */
    public boolean hasBitmapHolder() {
        return isInPreviewMode() ? bitmapCacheHolder.hasFullPreview() : resultHolder != null;
    }

    /**
     * Get the result holder, this object holds the result.
     * @return the operation result holder.
     */
    @NonNull
    public ResultHolder getResultBitmapHolder() {
        if (isInPreviewMode()) {
            resultHolder = null;
            return bitmapCacheHolder;
        } else {
            if (resultHolder == null) {
                SourceHolder source = getSourceBitmapHolder();
                resultHolder = (source instanceof OperationResultHolder) ? (OperationResultHolder) source : null;
                if (resultHolder == null) {
                    resultHolder = new OperationResultHolder();
                }
            }
            return resultHolder;
        }
    }

    /**
     * Get the Source Bitmaps holder
     * @return result of the previous Operation
     */
    @Nullable
    SourceHolder getSourceBitmapHolder() {
        return operator.getSourceBitmapHolder(this);
    }

    void setCallback(Operator callback) {
        this.operator = callback;
    }

    /**
     * Must be called if the mode is #Mode.BLOCKING_MAIN_THREAD
     */
    protected synchronized void operationDone(){
        if (listener != null) {
            listener.operationDone(this);
        }
        listener = null;
    }

    /**
     * Invalidate the own cache. ATTENTION all followed caches are stay there.
     */
    protected void invalidateCache() {
        if (bitmapCacheHolder != null) {
            bitmapCacheHolder.invalidateAll();
        }
    }

    /**
     * Like #invalidateCache but invalidate only the sharp preview cache
     */
    synchronized void invalidateSharpCache() {
        if (bitmapCacheHolder != null) {
            bitmapCacheHolder.invalidateSharpPreview();
        }
    }

    /**
     * Invalidate the Sharp region
     */
    protected void invalidateSharp() {
        if (operator != null) {
            AbstractOperation operation = this;
            while (operation != null) {
                operation.invalidateSharpCache();
                operation = getOperator().higher(operation);
            }
        }
        invalidateOperator();
    }

    /**
     * Invalidate the own cache an all followed caches and call the Operator to start rendering.
     */
    protected void invalidateState() {
        if (operator != null) {
            AbstractOperation operation = this;
            while (operation != null) {
                operation.invalidateCache();
                operation = operator.higher(operation);
            }
        }
        invalidateOperator();
    }

    /**
     * Call the Operator to start rendering. Instant Operations can call this to keep the caches.
     */
    void invalidateOperator() {
        if (operator != null) {
            operator.invalidate();
        }
    }

    @Override
    public int compareTo(@NonNull AbstractOperation operation) {
        return getPriority().ordinal() - operation.getPriority().ordinal();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractOperation operation = (AbstractOperation) o;

        return (getPriority()   == operation.getPriority())
                && !(getIdentifier() != null ? !getIdentifier().equals(operation.getIdentifier()) : operation.getIdentifier() != null);

    }

    @Override
    public int hashCode() {
        int result = getPriority().hashCode();
        result = 31 * result + (getIdentifier() != null ? getIdentifier().hashCode() : 0);
        return result;
    }

    /**
     * Operation done callback.
     */
    public interface OperationDoneListener{
        void operationDone(AbstractOperation operation);
    }


    @NonNull
    @Override
    public String toString() {
        return "Operation{" +
                "id=" + getIdentifier() +
                '}';
    }

    @NonNull
    @Deprecated
    public final Runnable getOperationRunnable(final OperationDoneListener listener) {
        return new Runnable() {
            @Override
            public void run() {
                doOperation(listener);
            }
        };
    }

    protected interface Holder {
        /**
         * Free Memory
         */
        void recycle();

        /**
         * Get Identifier.
         * @return Operator identifier string.
         */
        @Nullable
        String getIdentifier();
    }

    public interface SourceHolder extends Holder {
        /**
         * Get the Sharp Region that need to bie preview in a higher resolution.
         * @return rect of the sharp preview
         */
        @Nullable
        Rect getSharpRect();

        /**
         * Has a source bitmap
         * @return should be true after load operation done.
         */
        boolean hasFullPreview();

        /**
         * Has a source bitmap
         * @return should be true after load operation done.
         */
        boolean hasBlurPreview();

        /**
         * Has a sharp region bitmap
         * @return true if sharp region source is present
         */
        boolean hasSharpPreview();

        /**
         * Get source
         * @return previews operation source image
         */
        @Nullable
        Bitmap getFullPreview();

        /**
         * Get source
         * @return previews operation source image
         */
        @Nullable
        Bitmap getBlurPreview();

        /**
         * Get sharp preview source.
         * @return previews operation sharp source bitmap.
         */
        @Nullable
        Bitmap getSharpPreview();

        /**
         * Get source image width.
         * @return source image width.
         */
        int getFullWidth();

        /**
         * Get source image height.
         * @return source image height.
         */
        int getFullHeight();
    }

    public interface ResultHolder extends Holder{
        /**
         * disable this state can be became invalid state.
         */
        void disableInvalidatable();

        void invalidateAll();
        void invalidateFullPreview();
        void invalidateSharpPreview();

        /**
         * Set the sharp region bitmap result and his region.
         * @param sharpRegion sharp region bitmap
         * @param sharpRect sharp region, should be the source region or a bigger region.
         */
        void setSharpRegionResult(Bitmap sharpRegion, Rect sharpRect);

        /**
         * Set the bitmap result.
         * @param fullPreview the full preview
         */
        void setFullResult(Bitmap fullPreview);

        /**
         * Set the bitmap blur result for the focus preview.
         * @param blurPreview the full preview
         */
        void setBlurResult(Bitmap blurPreview);

        /**
         * Check if this operation need to render sharp preview.
         * @return true if need to be render
         */
        boolean needRenderSharpResult();
        /**
         * Check if this operation need to render full region
         * @return true if need to be render
         */
        boolean needRenderFullResult();
    }
}
