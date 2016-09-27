package com.camerafilter.sdk.operator;

import android.support.annotation.NonNull;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class ImageShowOperation extends AbstractEditorOperation {

    @NonNull
    @Override
    protected Operator.Priority getPriority() {
        return Operator.Priority.Show;
    }

    @Override
    protected String getIdentifier() {
        return getClass().getName();
    }

    @NonNull
    @Override
    public MODE getOperationMode() {
        return MODE.BACKGROUND_THREAD;
    }

    @Override
    protected boolean doOperation() {

        if (isInPreviewMode()) {
            final SourceHolder source = getSourceBitmapHolder();
            final ResultHolder result = getResultBitmapHolder();

            result.disableInvalidatable();

            if (source != null) {
                if (source.hasFullPreview()) {
                    result.setFullResult(source.getFullPreview());
                }
                if (source.hasSharpPreview()) {
                    result.setSharpRegionResult(source.getSharpPreview(), source.getSharpRect());
                }
                if (source.hasBlurPreview()) {
                    result.setBlurResult(source.getBlurPreview());
                }

                EditorProtectedAccessor.setResultBitmap(getEditor(), result);

            }
        }
        return true;
    }
}

