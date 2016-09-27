package com.camerafilter.sdk.brush;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.camerafilter.ImgLySdk;
import com.camerafilter.sdk.brush.models.PaintChunk;
import com.camerafilter.utils.ThreadUtils;
import com.camerafilter.utils.TransparentJpeg;

import java.io.File;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class BrushHistoryCache {
    private static final String CACHE_SUB_DIR_NAME = "brush_history";
    private static final File cacheDir;

    static {
        cacheDir = ImgLySdk.getAppContext().getExternalCacheDir() != null ?
                new File(ImgLySdk.getAppContext().getExternalCacheDir(), CACHE_SUB_DIR_NAME) :
                new File(ImgLySdk.getAppContext().getCacheDir(), CACHE_SUB_DIR_NAME);

        //noinspection ResultOfMethodCallIgnored
        cacheDir.mkdirs();

        // Cleanup ghost files
        cleanupCacheFolder();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                cleanupCacheFolder();
            }
        }));

        cacheDir.deleteOnExit();
    }

    private static void cleanupCacheFolder() {
        cleanupPath(cacheDir);
    }

    private static void cleanupPath(File path) {
        if (path.isDirectory()) for(File file : path.listFiles()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        } else {
            //noinspection ResultOfMethodCallIgnored
            path.delete();
        }
    }

    public static void destroyCache(PaintChunk chunk) {
        TransparentJpeg.deleteTransparentJpeg(cacheDir, getCacheName(chunk));
    }

    public static boolean hasCache(PaintChunk chunk) {
        return TransparentJpeg.combinationExists(cacheDir, getCacheName(chunk), true);
    }

    private static String getCacheName(PaintChunk chunk) {
        return "BrushChunk_" + chunk.getRuntimeUniqId();
    }

    public static Bitmap getCache(PaintChunk chunk) {
        final String name = getCacheName(chunk);
        if (TransparentJpeg.combinationExists(cacheDir, name, false)) {
            return TransparentJpeg.loadTransparentJpeg(cacheDir, name);
        }
        return null;
    }

    @WorkerThread
    public static void saveCache(PaintChunk chunk, @Nullable final Bitmap bitmap) {

        final String cacheName = getCacheName(chunk);

        if (bitmap != null) {
            ThreadUtils.addTaskToWorkerGroup("SaveHistory", ThreadUtils.PRIORITY.MIN_PRIORITY, new ThreadUtils.WorkerThreadRunnable() {
                @Override
                public void run() {
                    TransparentJpeg.saveTransparentJpeg(cacheDir, cacheName, bitmap);
                }
            });
        }
    }
}
