package com.camerafilter.sdk.operator;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.camerafilter.sdk.views.EditorPreview;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class ImageSaveOperation extends AbstractEditorOperation {

    @Nullable
    private String outputPath = null;

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    @Nullable
    public String getOutputPath() {
        return outputPath;
    }

    @NonNull
    @Override
    protected Operator.Priority getPriority() {
        return Operator.Priority.Save;
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
        SourceHolder source = getSourceBitmapHolder();
        try {
            if (!isInPreviewMode()) {
                OutputStream stream = new FileOutputStream(outputPath);
                source.getFullPreview().compress(Bitmap.CompressFormat.JPEG, 80, stream);

                runOnUiThread(new UiRunnable() {
                    @Override
                    protected final void run(@NonNull EditorPreview editor) {
                        editor.onFinalResultSaved();
                    }
                });
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return true;
    }
}

