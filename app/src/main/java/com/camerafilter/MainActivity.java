package com.camerafilter;

import android.app.Activity;
import android.os.Bundle;

import com.camerafilter.ui.activity.CameraPreviewIntent;
import com.camerafilter.ui.activity.PhotoEditorIntent;
import com.camerafilter.ui.utilities.PermissionRequest;

public class MainActivity extends Activity implements PermissionRequest.Response {
    private static final String FOLDER = "ImgLy";
    public static int CAMERA_PREVIEW_RESULT = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new CameraPreviewIntent(this)
                .setExportDir(CameraPreviewIntent.Directory.DCIM, FOLDER)
                .setExportPrefix("img_")
                .setEditorIntent(
                        new PhotoEditorIntent(this)
                                .setExportDir(PhotoEditorIntent.Directory.DCIM, FOLDER)
                                .setExportPrefix("result_")
                                .destroySourceAfterSave(true)
                )
                .startActivityForResult(CAMERA_PREVIEW_RESULT);
    }

    @Override
    public void permissionGranted() {

    }

    @Override
    public void permissionDenied() {
        finish();
        System.exit(0);
    }
}
