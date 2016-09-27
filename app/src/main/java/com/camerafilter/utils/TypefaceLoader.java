package com.camerafilter.utils;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.util.LruCache;

import com.camerafilter.ImgLySdk;

import java.io.File;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class TypefaceLoader {
    private static final LruCache<String, Typeface> sTypefaceCache = new LruCache<>(12);

    public static Typeface getTypeface(@NonNull File typefaceFile) {
        String typefaceName = typefaceFile.getName();
        try {
            Typeface typeface = sTypefaceCache.get(typefaceName);
            if (typeface == null) {
                typeface = Typeface.createFromFile(typefaceFile);
                // Cache the Typeface object
                sTypefaceCache.put(typefaceName, typeface);
            }
            return typeface;
        } catch(Exception e) {
            return null;
        }
    }

    public static Typeface getTypeface(@NonNull String typefaceAssetsPath) {

        String[] split = typefaceAssetsPath.split("/");

        String typefaceName = split[split.length - 1];
        //try {
        Typeface typeface = sTypefaceCache.get(typefaceName);
        if (typeface == null) {
            typeface = Typeface.createFromAsset(ImgLySdk.getAppContext().getAssets(), typefaceAssetsPath);
            // Cache the Typeface object
            sTypefaceCache.put(typefaceName, typeface);
        }
        return typeface;
        //} catch(Exception e) {
        //    return null;
        //}
    }
}
