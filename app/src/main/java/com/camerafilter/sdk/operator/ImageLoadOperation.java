package com.camerafilter.sdk.operator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.camerafilter.sdk.models.OperationCacheHolder;
import com.camerafilter.utils.BitmapFactoryUtils;

import java.io.IOException;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class ImageLoadOperation extends AbstractEditorOperation {

    private String sourceImagePath;

    private int previewWidth  = -1;
    private int previewHeight = -1;

    private int imageWidth = -1;
    private int imageHeight= -1;

    private int imageAngle = 0;

    @Nullable
    private Rect sharpRect = null;

    private static String currentSourceImagePath;
    @Nullable
    private static Bitmap thumbnail;

    @Nullable
    public static Bitmap getThumbnailBitmap(int size) {
        if (currentSourceImagePath != null && (thumbnail == null)) {
            thumbnail = BitmapFactoryUtils.decodeFile(currentSourceImagePath, size, true, true);
            if (thumbnail != null) {
                int width  = thumbnail.getWidth() - thumbnail.getWidth() % 16;
                int height = Math.round(thumbnail.getHeight() * (width / (float) thumbnail.getWidth()));
                thumbnail  = Bitmap.createScaledBitmap(thumbnail, width, height, true);
            }

        }
        return thumbnail;
    }

    /**
     * Set Image source path
     * @param imagePath path of the image.
     */
    public void setSourceImagePath(String imagePath) {
        thumbnail = null;
        currentSourceImagePath = imagePath;
        this.sourceImagePath = imagePath;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(imagePath, options);

        imageAngle = BitmapFactoryUtils.getImageRotation(imagePath);

        boolean isRotated = imageAngle % 180 != 0;

        imageWidth  = isRotated ? options.outHeight : options.outWidth;
        imageHeight = isRotated ? options.outWidth  : options.outHeight;

        if (isReady()) {
            OperationCacheHolder.releaseAll();
            invalidateState();
            invalidateCache();
        }
    }

    @NonNull
    @Override
    protected Operator.Priority getPriority() {
        return Operator.Priority.Load;
    }

    @Override
    protected String getIdentifier() {
        return ImageLoadOperation.class.getName();
    }

    /**
     * Set preview window size, to save Memory an Speedup preview by load a low resolution preview.
     * @param width width in Pixels
     * @param height height in Pixel
     */
    public void setPreviewSize(int width, int height) {
        this.previewWidth  = width;
        this.previewHeight = height;
        invalidateState();
    }

    protected boolean isReady() {
        return previewHeight > 0 && previewWidth > 0 && sourceImagePath != null;
    }

    /**
     * Get the real source image Exif rotation
     * @return rotation in degree
     */
    public int getImageAngle() {
        return 0; //imageAngle;
    }

    /**
     * Get the real source image width
     * @return width in Pixels
     */
    public int getImageWidth() {
        return imageWidth;
    }

    /**
     * Get the real source image height
     * @return height in Pixel
     */
    public int getImageHeight() {
        return imageHeight;
    }

    /**
     * Load a sharp image preview
     * @param sharpRect the zoom region rect
     */
    public void setSharpRect(@Nullable Rect sharpRect) {
        if (sharpRect == null || (!sharpRect.equals(this.sharpRect) && (sharpRect.width() > 10 && sharpRect.height() > 10))) {
            if (this.sharpRect != null || sharpRect == null || (sharpRect.width() == imageWidth && sharpRect.height() == imageHeight)) {
                this.sharpRect = sharpRect;
                invalidateState();
            } else {
                this.sharpRect = sharpRect;
                invalidateSharp();
            }
        }
    }


    private Bitmap loadSharpRegion() {
        boolean hasSharpRect = false; //sharpRect != null && (sharpRect.width() != imageWidth || sharpRect.height() != imageHeight);
        if (hasSharpRect && sourceImagePath != null) {
            try {
                int inSampleSize = sharpRect.height() / previewHeight;
                if (inSampleSize < 1) {
                    inSampleSize = 1;
                }
                BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(sourceImagePath, false);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = inSampleSize;

                Rect imageCropRect = new Rect(sharpRect.left / inSampleSize, sharpRect.top / inSampleSize, sharpRect.right / inSampleSize, sharpRect.bottom / inSampleSize);

                return decoder.decodeRegion(imageCropRect, options);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @NonNull
    @Override
    public MODE getOperationMode() {
        return MODE.BACKGROUND_THREAD;
    }

    @Override
    protected boolean doOperation() {
        ResultHolder result = getResultBitmapHolder();
        if (isInPreviewMode() && isReady()) {
            boolean hasSharpRect = false; // sharpRect != null && (sharpRect.width() != imageWidth || sharpRect.height() != imageHeight);

            int size = Math.max(previewWidth, previewHeight);

            if (result.needRenderFullResult()) {
                Bitmap fullPreview = BitmapFactoryUtils.decodeFile(sourceImagePath, size / (hasSharpRect ? 8 : 1), false, true);

                //bring image to a 16 dividable
                int width  = (this.previewWidth / this.previewHeight < fullPreview.getWidth() / fullPreview.getWidth())
                        ? this.previewWidth
                        : this.previewWidth * this.previewHeight / fullPreview.getWidth();

                if (width % 16 != 0) {
                    width = width + (16 - width % 16);
                }

                if (width != fullPreview.getWidth()) {
                    fullPreview = Bitmap.createScaledBitmap(
                            fullPreview,
                            width,
                            (int) Math.round(fullPreview.getHeight() * (width / (double) fullPreview.getWidth())),
                            true
                    );
                }

                result.setFullResult(fullPreview);
            }

            if (result.needRenderSharpResult()) {
                result.setSharpRegionResult(loadSharpRegion(), sharpRect);
            }

        } else if (!isInPreviewMode()) {
            Bitmap fullBitmap = BitmapFactoryUtils.decodeFile(sourceImagePath, Integer.MAX_VALUE, false, true);
            result.setFullResult(fullBitmap);
        }

        return true;
    }

}

