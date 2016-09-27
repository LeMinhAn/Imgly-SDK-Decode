package com.camerafilter.sdk.operator;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.NonNull;

import com.camerafilter.sdk.views.EditorPreview;
import com.camerafilter.sdk.views.LayerContainerView;
import com.camerafilter.sdk.views.PicturePreviewView;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public abstract class AbstractEditorOperation extends AbstractOperation {
    private EditorPreview editor;

    EditorPreview getEditor() {
        return editor;
    }

    public void init(EditorPreview editorPreview) {
        editor = editorPreview;
    }

    /**
     * Run a part of the operation async on the next Main Thread frame.
     * @param runnable UiRunnable implementation
     */
    protected void runOnUiThread(UiRunnable runnable){
        if (editor != null) {
            editor.post(runnable);
        }
    }

    /**
     * Runnable to access Editor methods on main thread.
     */
    abstract class UiRunnable implements Runnable{

        @Override
        public final void run() {
            run(getEditor());
        }

        protected abstract void run(EditorPreview editor);
    }

    /**
     * Get the Sticker stage to add, edit or remove Sticker
     * @return sticker stage view
     */
    @NonNull
    public LayerContainerView getStickerStage() {
        return EditorProtectedAccessor.getStickerStage(getEditor());
    }


    /**
     * Helper class to access protected Editor methods.
     */
    @SuppressWarnings("deprecation")
    protected static class EditorProtectedAccessor extends EditorPreview {
        @Deprecated private EditorProtectedAccessor(Context context) { super(context); }

        public static void setResultBitmap(@NonNull EditorPreview editor, AbstractOperation.ResultHolder result) {
            EditorPreview.ProtectedAccessor.setResultBitmap(editor, result);
        }

        @NonNull
        public static LayerContainerView getStickerStage(@NonNull EditorPreview editor) {
            return EditorPreview.ProtectedAccessor.getStickerStage(editor);
        }

        public static Paint getPreviewPaint(@NonNull EditorPreview editorPreview){
            return EditorPreview.ProtectedAccessor.getPreviewPaint(editorPreview);
        }

        public static void setPreviewPaint(@NonNull EditorPreview editor, Paint paint) {
            EditorPreview.ProtectedAccessor.setPreviewPaint(editor, paint);
        }

        @NonNull
        public static PicturePreviewView getFocusEditor(@NonNull EditorPreview editor) {
            return EditorPreview.ProtectedAccessor.getResultView(editor);
        }
    }
}
