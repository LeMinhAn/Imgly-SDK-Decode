package com.camerafilter.sdk.operator;

import android.graphics.Paint;

/**
 * Created by Le Minh An on 9/20/2016.
 */
abstract class AbstractPaintOperation extends AbstractEditorOperation {
    private Paint paint;

    Paint getPaint() {
        paint = EditorProtectedAccessor.getPreviewPaint(getEditor());
        return paint;
    }
    void refreshPaint() {
        EditorProtectedAccessor.setPreviewPaint(getEditor(), paint);
    }

}
