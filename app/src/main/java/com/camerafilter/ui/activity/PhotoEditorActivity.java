package com.camerafilter.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.camerafilter.ImgLySdk;
import com.camerafilter.ui.utilities.PermissionRequest;
import com.camerafilter.R;
import com.camerafilter.sdk.configuration.AbstractConfig;
import com.camerafilter.sdk.configuration.PhotoEditorSdkConfig;
import com.camerafilter.sdk.tools.AbstractTool;
import com.camerafilter.ui.adapter.DataSourceListAdapter;
import com.camerafilter.ui.utilities.OrientationSensor;
import com.camerafilter.ui.widgets.ConfirmPopupView;
import com.camerafilter.ui.widgets.HorizontalListView;
import com.camerafilter.ui.widgets.ImgLyTitleBar;
import com.camerafilter.sdk.views.EditorPreview;

import java.io.File;
import java.util.ArrayList;

public class PhotoEditorActivity extends Activity implements DataSourceListAdapter.OnItemClickListener<AbstractConfig.ToolConfigInterface>, EditorPreview.OnImageReadyCallback, EditorPreview.PanelBindCallback {

    public HorizontalListView toolListView;
    public EditorPreview editorPreviewView;
    public RelativeLayout toolViewContainer;
    public Button cancelButton;
    public Button acceptButton;
    public ImgLyTitleBar actionBar;

    private PhotoEditorIntent intent;

    private class ToolHistory {
        final AbstractConfig.ToolConfigInterface tool;
        final View toolView;

        public ToolHistory(AbstractConfig.ToolConfigInterface tool, View toolView) {
            this.tool = tool;
            this.toolView = toolView;
        }
    }

    private final ArrayList<ToolHistory> toolHistory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imgly_activity_photo_editor);

        ImgLySdk.getAnalyticsPlugin().changeScreen("PhotoEditor");

        initViews();

        intent = new PhotoEditorIntent(getIntent(), this);

        String imagePath = intent.getSourceImage();

        AbstractConfig.ImageFilterInterface filter = intent.getFilter();

        editorPreviewView.setPanelBindCallback(this);
        editorPreviewView.setSourceImagePath(imagePath);
        editorPreviewView.getOperator().getFilterOperation().setFilter(filter);
        editorPreviewView.invalidateOperations();


        DataSourceListAdapter toolListAdapter = new DataSourceListAdapter(this);
        toolListAdapter.setData(PhotoEditorSdkConfig.getTools());
        toolListAdapter.setOnItemClickListener(this);
        toolListView.setAdapter(toolListAdapter);

        toolHistory.clear();
    }

    private void initViews() {
        editorPreviewView = (EditorPreview) findViewById(R.id.editorImageView);
        toolViewContainer = (RelativeLayout) findViewById(R.id.toolPanelContainer);
        toolListView = (HorizontalListView) findViewById(R.id.toolList);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        acceptButton = (Button) findViewById(R.id.acceptButton);
        actionBar = (ImgLyTitleBar) findViewById(R.id.imglyActionBar);

        actionBar.setTitle(R.string.imgly_photo_editor_title, false);

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAccept();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCancel();
            }
        });
    }


    @Override
    public void onItemClick(@NonNull AbstractConfig.ToolConfigInterface tool) {
        enterToolMode(tool, false);
    }

    public void onAccept() {
        if (getCurrentTool() != null) {
            leaveToolMode(false);
        }else{
            onSave();
        }
    }

    public void onCancel() {
        if (getCurrentTool() != null) {
            leaveToolMode(true);
        } else {

            new ConfirmPopupView().setListener(new ConfirmPopupView.Listener() {
                @Override
                public void onConfirmPopupResult(boolean isPositive) {
                    if (isPositive) {
                        PhotoEditorActivity activity = PhotoEditorActivity.this;
                        activity.setResult(RESULT_CANCELED);
                        activity.finish();
                    }
                }
            }).blurSource(findViewById(R.id.rootView)).show(this);
        }

    }

    @Nullable
    public ToolHistory getCurrentTool() {
        return toolHistory.size() > 0 ? toolHistory.get(toolHistory.size() - 1) : null;
    }

    public void onSave() {
        final String filePrefix = intent.getExportPrefix();

        File mMediaFolder = new File(intent.getExportPath());

        if (!mMediaFolder.exists()) {
            mMediaFolder.mkdirs();
        }

        (findViewById(R.id.progressView)).setVisibility(View.VISIBLE);

        String filePath = mMediaFolder.getAbsolutePath() + "/" + filePrefix + System.currentTimeMillis() + ".jpg";
        editorPreviewView.saveFinalImage(filePath, this);
        ImgLySdk.getAnalyticsPlugin().sendEvent("Interface", "Save image");
    }

    @Override
    public void enterToolMode(@NonNull AbstractConfig.ToolConfigInterface tool, boolean replace) {
        ToolHistory history = getCurrentTool();
        if (history == null || !tool.equals(history.tool)) {

            final AbstractTool currentTool = ((AbstractTool) tool);

            final View toolView = currentTool.attachPanel(toolViewContainer, editorPreviewView);

            if (replace) {
                final ToolHistory detachTool = getCurrentTool();

                if (detachTool != null) {
                    toolHistory.remove(detachTool);
                    detachTool.tool.detachPanel(false);
                }

                toolHistory.add(toolHistory.size(), new ToolHistory(currentTool, toolView));
            } else {
                toolHistory.add(new ToolHistory(currentTool, toolView));
            }

            cancelButton.setVisibility(currentTool.isRevertible() ? View.VISIBLE : View.GONE);
            actionBar.setTitle(currentTool.getTitle(), false);
        } else {
            history.tool.refreshPanel();
        }
    }

    @Override
    public void leaveToolMode(boolean revertChanges) {

        //ANIMATION
        if (toolHistory.size() <= 1) {
            toolListView.setVisibility(View.VISIBLE);
        }

        final ToolHistory detachTool = getCurrentTool();

        if (detachTool != null) {
            if (revertChanges) {
                detachTool.tool.revertChanges();
            }

            toolHistory.remove(detachTool);
            detachTool.tool.detachPanel(revertChanges);

            cancelButton.setVisibility(View.VISIBLE);
        }

        if (getCurrentTool() == null) {
            actionBar.setTitle(R.string.imgly_photo_editor_title, true);
        } else {
            actionBar.setTitle(getCurrentTool().tool.getTitle(), true);
        }

    }

    @Override
    public void onBackPressed() {
        onCancel();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
        OrientationSensor.getInstance().start(PhotoEditorSdkConfig.getEditorScreenRotationMode());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onPause() {
        super.onPause();
        OrientationSensor.getInstance().stop();
    }

    @Override
    public void onImageReadyCallback(String path) {
        Intent result = new Intent();
        result.putExtra(CameraPreviewActivity.RESULT_IMAGE_PATH, path);
        setResult(RESULT_OK, result);
        finish();
    }
}
