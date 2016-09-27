package com.camerafilter.sdk.brush;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class BitmapLayer extends Canvas {
    private Bitmap bitmap;
    public final int width;
    public final int height;

    public BitmapLayer(int width, int height, Bitmap.Config config) {
        super();

        this.width = width;
        this.height = height;

        bitmap = Bitmap.createBitmap(width, height, config);

        setBitmap(bitmap);
    }

    public BitmapLayer(float width, float height, Bitmap.Config config){
        this((int) Math.ceil(width), (int) Math.ceil(height), config);
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void clear() {
        drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }

    @Override
    protected void finalize() throws Throwable {
        bitmap = null;
        super.finalize();
    }

    public static final class ConcurrentLayer {
        private final Lock lock;
        private BitmapLayer layer;
        public final int width;
        public final int height;

        public ConcurrentLayer(int width, int height, Bitmap.Config config) {
            this.width = width;
            this.height = height;

            lock = new ReentrantLock();
            layer = new BitmapLayer(width, height, config);
        }

        @WorkerThread
        public BitmapLayer lock() {
            lock.lock();
            return layer;
        }

        @WorkerThread @MainThread
        public BitmapLayer getLockedLayer() {
            return layer;
        }

        @WorkerThread @MainThread
        public void unlock() {
            lock.unlock();
        }
    }
}

