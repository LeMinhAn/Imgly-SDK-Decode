package com.camerafilter.ui.activity;

import android.app.Activity;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.camerafilter.sdk.configuration.AbstractConfig;
import com.camerafilter.sdk.configuration.PhotoEditorSdkConfig;
import com.camerafilter.sdk.filter.NoneImageFilter;

import java.io.File;
import java.util.List;

/**
 * Created by Le Minh An on 9/24/2016.
 */
public class PhotoEditorIntent extends ImgLyIntent {

    private static final Class activityClass = PhotoEditorActivity.class;

    public PhotoEditorIntent(android.content.Intent intent, Activity activity) {
        super(intent, activity);
    }

    public PhotoEditorIntent(Activity activity) {
        super(activity, activityClass);
    }

    /**
     * Set part of the Source image. It will be opend by the editor.
     * @param path the absolute image path.
     * @return this intent.
     */
    @NonNull
    public PhotoEditorIntent setSourceImagePath(String path) {
        putExtra(ImgLyIntent.Extra.SOURCE_FILE.name(), path);
        return this;
    }

    public String getSourceImage() {
        return getStringExtra(Extra.SOURCE_FILE.name());
    }

    /**
     * Set the Export directory save path of the result image.
     * @param path the absolutely save directory
     * @return this intent.
     */
    @NonNull
    public PhotoEditorIntent setExportDir(String path) {
        putExtra(Extra.EXPORT_PATH.name(), path);
        return this;
    }

    /**
     * Set the Export directory save path of the result image.
     * @param directory A specific system directory
     * @param folderName A folder name will saved in the system directory.
     * @return this intent.
     */
    @NonNull
    public PhotoEditorIntent setExportDir(@NonNull Directory directory, @NonNull String folderName) {
        File mMediaFolder = new File(Environment.getExternalStoragePublicDirectory(directory.dir), folderName);

        putExtra(Extra.EXPORT_PATH.name(), mMediaFolder.getAbsolutePath());
        return this;
    }

    /**
     * Enable or disable source image destroy after save.
     * @param destroy if true the source image will be destroyes after save.
     * @return this intent.
     */
    @NonNull
    public PhotoEditorIntent destroySourceAfterSave(boolean destroy) {
        putExtra(Extra.DESTROY_SOURCE.name(), destroy);
        return this;
    }

    /**
     * Set the image save name prefix.
     * @param prefix
     * @return this intent.
     */
    @NonNull
    public PhotoEditorIntent setExportPrefix(String prefix) {
        putExtra(Extra.EXPORT_PREFIX.name(), prefix);
        return this;
    }

    /**
     * Set a preselected filter
     * @param filter the ImageFilter that will be preselected.
     * @return this intent.
     */
    @NonNull
    public PhotoEditorIntent setFilter(@Nullable AbstractConfig.ImageFilterInterface filter) {
        List<AbstractConfig.ImageFilterInterface> list = PhotoEditorSdkConfig.getFilterConfig();
        int i = (filter != null) ? list.indexOf(filter) : 0;
        if (filter instanceof NoneImageFilter) {
            i = 0;
        } else if (i < 0) {
            list.add(1, filter);
            i = list.indexOf(filter);
        }
        this.putExtra(Extra.COLOR_FILTER.name(), i);

        return this;
    }

    public AbstractConfig.ImageFilterInterface getFilter() {
        List<AbstractConfig.ImageFilterInterface> list = PhotoEditorSdkConfig.getFilterConfig();
        int id = this.getIntExtra(Extra.COLOR_FILTER.name(), 0);
        if (id < 0) {
            id = 0;
        }
        return list.get(id);
    }

    /**
     * Set teh save quality of the jpeg. Default value ist 80
     * @param quality Image save Quality. A value of 0 is poor but has a small file size and a value of 100 is excellent but have a large file size.
     * @return this intent.
     */
    @NonNull
    public PhotoEditorIntent setJpegQuality(int quality) {
        this.putExtra(Extra.JPEG_QUALITY.name(), quality);
        return this;
    }

    protected boolean getDestroySourceAfterSave(){
        return getBooleanExtra(Extra.DESTROY_SOURCE.name(), false);
    }

    protected int getJpegQuality() {
        return this.getIntExtra(Extra.JPEG_QUALITY.name(), 100);
    }

    protected String getExportPath() {
        String string = getStringExtra(Extra.EXPORT_PATH.name());
        if (string == null) {
            string = Environment.getExternalStoragePublicDirectory(Directory.DCIM.dir).getAbsolutePath();
        }
        return string;
    }

    protected String getExportPrefix() {
        String string = getStringExtra(Extra.EXPORT_PREFIX.name());
        if (string == null) {
            string = "image_";
        }
        return string;
    }
}

