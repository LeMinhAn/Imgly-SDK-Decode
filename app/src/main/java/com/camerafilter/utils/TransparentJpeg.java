package com.camerafilter.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;

import com.camerafilter.ImgLySdk;
import com.camerafilter.ScriptC_split;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class TransparentJpeg {
    private static final String RGB_FILE_SUFFIX = ".jrgb";
    private static final String ALPHA_FILE_SUFFIX = ".jalpha";

    private static RenderScript rs = ImgLySdk.getAppRsContext();
    private static ScriptC_split split = new ScriptC_split(rs);

    private TransparentJpeg() {
    }

    /**
     * Save ARGB Bitmap in two JPEG files
     * @param dir save directory
     * @param filename the filename prefix
     * @param bitmap the bitmap that should be saved
     * @return true if saving is successful, false if the files can not be written
     */
    public static boolean saveTransparentJpeg(File dir, String filename, Bitmap bitmap) {
        OutputStream rgbStream = null;
        OutputStream alphaStream = null;

        try {
            File fileNameRGB = new File(dir, filename + RGB_FILE_SUFFIX);
            File fileNameAlpha = new File(dir, filename + ALPHA_FILE_SUFFIX);

            Bitmap rgbBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Bitmap alphaBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

            Allocation in = Allocation.createFromBitmap(rs, bitmap);
            Allocation rgbOut = Allocation.createFromBitmap(rs, rgbBitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
            Allocation alphaOut = Allocation.createFromBitmap(rs, alphaBitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);

            split.set_rsAllocationRGB(rgbOut);
            split.set_rsAllocationAlpha(alphaOut);
            split.forEach_splitLayer(in);

            rgbOut.copyTo(rgbBitmap);
            rgbStream = new BufferedOutputStream(new FileOutputStream(fileNameRGB));
            rgbBitmap.compress(Bitmap.CompressFormat.JPEG, 40, rgbStream);

            split.set_rsAllocationRGB(null); // Release Memory

            alphaOut.copyTo(alphaBitmap);
            alphaStream = new BufferedOutputStream(new FileOutputStream(fileNameAlpha));
            alphaBitmap.compress(Bitmap.CompressFormat.JPEG, 70, alphaStream);

            split.set_rsAllocationAlpha(null); // Release Memory

        } catch (IOException ignored) {
            return false;
        } finally {

            if (rgbStream != null) try {
                rgbStream.close();
            } catch (IOException ignored) {
            }

            if (alphaStream != null) try {
                alphaStream.close();
            } catch (IOException ignored) {
            }
        }
        return true;
    }

    /**
     * Delete the RGB and ALPHA Jpeg.
     * @param dir save directory
     * @param filename the filename prefix
     */
    public static void deleteTransparentJpeg(File dir, String filename) {
        File fileNameRGB = new File(dir, filename + RGB_FILE_SUFFIX);
        File fileNameAlpha = new File(dir, filename + ALPHA_FILE_SUFFIX);

        if (fileNameRGB.exists()) {
            //noinspection ResultOfMethodCallIgnored
            fileNameRGB.delete();
        }


        if (fileNameAlpha.exists()) {
            //noinspection ResultOfMethodCallIgnored
            fileNameAlpha.delete();
        }
    }

    /**
     * Check if the RGB and ALPHA channel's exists
     * @param dir save directory
     * @param filename the filename prefix
     * @param cleanupIfNot delete the second channel if the other not present
     * @return true, if the RGB and ALPHA channel is present.
     */
    public static boolean combinationExists(File dir, String filename, boolean cleanupIfNot) {
        File fileNameRGB = new File(dir, filename + RGB_FILE_SUFFIX);
        File fileNameAlpha = new File(dir, filename + ALPHA_FILE_SUFFIX);

        boolean combinationExist = true;
        if (!fileNameRGB.exists()) {
            if (cleanupIfNot) {

                //noinspection ResultOfMethodCallIgnored
                fileNameRGB.delete();
            }
            combinationExist = false;
        }

        if (!fileNameAlpha.exists()) {
            if (cleanupIfNot) {
                //noinspection ResultOfMethodCallIgnored
                fileNameAlpha.delete();
            }
            combinationExist = false;
        }

        return combinationExist;
    }

    /**
     * Load the RGB and ALPHA channel and combine them into one Bitmap.
     * @param dir save directory
     * @param filename the filename prefix
     * @return the resulting bitmap in ARGB_8888
     */
    public static Bitmap loadTransparentJpeg(File dir, String filename) {
        Bitmap bitmap = null;
        FileInputStream rgbStream = null;
        FileInputStream alphaStream = null;

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();

            File fileNameRGB = new File(dir, filename + RGB_FILE_SUFFIX);
            File fileNameAlpha = new File(dir, filename + ALPHA_FILE_SUFFIX);

            rgbStream = new FileInputStream(fileNameRGB);
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inMutable = false;
            Bitmap rgbBitmap = BitmapFactory.decodeStream(rgbStream, null, options);

            alphaStream = new FileInputStream(fileNameAlpha);
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inMutable = false;
            Bitmap alphaBitmap = BitmapFactory.decodeStream(alphaStream, null, options);

            bitmap = Bitmap.createBitmap(rgbBitmap.getWidth(), rgbBitmap.getHeight(), Bitmap.Config.ARGB_8888);

            Allocation out = Allocation.createFromBitmap(rs, bitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
            Allocation rgbIn = Allocation.createFromBitmap(rs, rgbBitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
            Allocation alphaIn = Allocation.createFromBitmap(rs, alphaBitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);

            split.set_rsAllocationRGB(rgbIn); // in == out to save Memory
            split.set_rsAllocationAlpha(alphaIn);
            split.forEach_combineLayer(out);

            split.set_rsAllocationRGB(null); // Release Memory
            split.set_rsAllocationAlpha(null); // Release Memory

            out.copyTo(bitmap);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            if (rgbStream != null) try {
                rgbStream.close();
            } catch (IOException ignored) {
            }

            if (alphaStream != null) try {
                alphaStream.close();
            } catch (IOException ignored) {
            }
        }

        return bitmap;
    }
}
