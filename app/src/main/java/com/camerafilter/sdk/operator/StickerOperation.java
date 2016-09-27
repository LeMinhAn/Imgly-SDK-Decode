package com.camerafilter.sdk.operator;

import android.support.annotation.NonNull;

import com.camerafilter.sdk.views.LayerContainerView;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class StickerOperation extends AbstractEditorOperation {

    @NonNull
    @Override
    protected Operator.Priority getPriority() {
        return Operator.Priority.Sticker;
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
        if (!isInPreviewMode()) {
            LayerContainerView stickerHolder = getStickerStage();

            ResultHolder result = getResultBitmapHolder();
            SourceHolder source = getSourceBitmapHolder();

            if (source != null && source.getFullPreview() != null) {
                result.setFullResult(stickerHolder.drawToBitmap(source.getFullPreview(), 0, 0));
            }
        }
        return true;
    }


}

