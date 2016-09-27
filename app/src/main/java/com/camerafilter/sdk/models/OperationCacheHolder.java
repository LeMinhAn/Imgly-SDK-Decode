package com.camerafilter.sdk.models;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.LruCache;

import com.camerafilter.sdk.operator.AbstractOperation;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class OperationCacheHolder implements AbstractOperation.ResultHolder, AbstractOperation.SourceHolder {

    //private static final String OPERATION_CACHE_DIR_NAME = "OperationCache";
    private static final float MAX_MEMORY_PERCENTAGE = 0.75f;
    //private static final long  MIN_FREE_SPACE = 10 * 1024 * 1024;


    //private static final File cacheDir;
    @NonNull
    private static final LruCache<String, Bitmap> memoryCache;

    //private static final HashMap<String, Bitmap> memoryCache;
    @NonNull
    private static final HashSet<String> invalidateState;

    private boolean isInvalidatable = true;

    static {

        invalidateState = new HashSet<>();

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = (int) (maxMemory * MAX_MEMORY_PERCENTAGE);


        //memoryCache = new HashMap<>(); /*

        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, @NonNull Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.

                return bitmap.getByteCount() / 1024;
            }
        };//*/

    }

    public enum CACHE_TYPE {
        FULL_PREVIEW,
        BLUR_PREVIEW,
        SHARP_PREVIEW
    }

    @Nullable
    private Rect sharpRect;

    private final String identifier;

    private int fullPreviewWidth;
    private int fullPreviewHeight;

    public OperationCacheHolder(String identifier) {
        this.identifier = identifier;
    }

    ///****CACHE CODE START****///

    public static void releaseAll() {
        memoryCache.evictAll();
    }

    public String getIdentifier() {
        return identifier;
    }

    @NonNull
    protected String getCacheId(@NonNull CACHE_TYPE typeProperty){
        return md5(typeProperty.toString() + "_" + identifier);
    }

    protected synchronized void setCache(@NonNull CACHE_TYPE typeProperty, @Nullable Bitmap bitmap) {
        String id = getCacheId(typeProperty);
        if (bitmap != null) {
            memoryCache.put(id, bitmap);
            invalidateState.remove(id);
        } else {
            memoryCache.remove(id);
        }
    }

    protected Bitmap getCache(@NonNull CACHE_TYPE typeProperty) {
        String id = getCacheId(typeProperty);
        return memoryCache.get(id);
    }

    private Boolean hasCache(@NonNull CACHE_TYPE typeProperty) {
        String id = getCacheId(typeProperty);
        return memoryCache.get(id) != null;
    }

    protected void removeCache(@NonNull CACHE_TYPE typeProperty) {
        String id = getCacheId(typeProperty);
        memoryCache.remove(id);
    }

    public void disableInvalidatable(){
        isInvalidatable = false;
    }

    @NonNull
    private static String md5(@NonNull String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (byte aMessageDigest : messageDigest) {
                hexString.append(Integer.toHexString(0xFF & aMessageDigest));
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return s;

    }

    public void invalidateAll() {
        if (isInvalidatable) {
            for (CACHE_TYPE type : CACHE_TYPE.values()) {
                invalidateType(type);
            }
        }
    }

    public void invalidateType(@NonNull CACHE_TYPE type) {
        invalidateState.add(getCacheId(type));
    }

    public boolean isInvalid(@NonNull CACHE_TYPE type) {
        return invalidateState.contains(getCacheId(type));
    }

    ///****CACHE CODE END****///

    public void recycle() {
        sharpRect = null;
        for (CACHE_TYPE type : CACHE_TYPE.values()) if (isInvalid(type)) {
            removeCache(type);
        }
    }


    public void invalidateFullPreview() {
        if (isInvalidatable) invalidateType(CACHE_TYPE.FULL_PREVIEW);
    }

    public void invalidateSharpPreview() {
        if (isInvalidatable) invalidateType(CACHE_TYPE.SHARP_PREVIEW);
    }

    @Nullable
    public Rect getSharpRect() {
        return sharpRect;
    }

    public void setFullResult(@Nullable Bitmap fullPreview) {
        setCache(CACHE_TYPE.FULL_PREVIEW, fullPreview);
        if (fullPreview != null) {
            fullPreviewWidth = fullPreview.getWidth();
            fullPreviewHeight = fullPreview.getHeight();
        }
    }

    @Override
    public void setBlurResult(Bitmap blurPreview) {
        setCache(CACHE_TYPE.BLUR_PREVIEW, blurPreview);
    }

    public Bitmap getFullPreview() {
        return getCache(CACHE_TYPE.FULL_PREVIEW);
    }

    @Override
    public Bitmap getBlurPreview() {
        return getCache(CACHE_TYPE.BLUR_PREVIEW);
    }

    public boolean needRenderSharpResult() {
        return isInvalid(CACHE_TYPE.SHARP_PREVIEW);
    }

    public boolean needRenderFullResult() {
        return isInvalid(CACHE_TYPE.FULL_PREVIEW);
    }

    public boolean hasFullPreview() {
        return hasCache(CACHE_TYPE.FULL_PREVIEW);
    }

    @Override
    public boolean hasBlurPreview() {
        return hasCache(CACHE_TYPE.BLUR_PREVIEW);
    }

    public boolean hasSharpPreview() {
        return hasCache(CACHE_TYPE.SHARP_PREVIEW);
    }

    public Bitmap getSharpPreview() {
        return getCache(CACHE_TYPE.SHARP_PREVIEW);
    }

    public void setSharpRegionResult(Bitmap sharpRegion, Rect sharpRect) {
        this.sharpRect = sharpRect;
        setCache(CACHE_TYPE.SHARP_PREVIEW, sharpRegion);
    }

    public int getFullWidth(){
        return fullPreviewWidth;
    }

    public int getFullHeight(){
        return fullPreviewHeight;
    }


}