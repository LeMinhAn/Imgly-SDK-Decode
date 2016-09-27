package com.camerafilter.utils;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntRange;

/**
 * Created by Le Minh An on 9/26/2016.
 */
public class TimeOut<T extends Enum> {

    private volatile TimerThread timerThread;
    private volatile long releaseTime;

    private CallbackHandler callbackHandler;
    private Handler mainLoopHandler;

    private T identifier;

    public TimeOut(T identifier) {
        this.identifier = identifier;
        this.callbackHandler = new CallbackHandler();
        this.mainLoopHandler = new Handler(Looper.getMainLooper());
    }

    public TimeOut<T> setTimeOut(@IntRange(from = 10) int timeoutMilliseconds) {
        releaseTime = System.currentTimeMillis() + timeoutMilliseconds;
        startTimerThread();
        return this;
    }

    private void startTimerThread() {
        if (timerThread == null) {
            timerThread = new TimerThread();
            timerThread.start();
        }
    }

    private synchronized void onTimeOut() {
        mainLoopHandler.post(new Runnable() {
            @Override
            public void run() {
                timerThread = null;

                // Check again, because the timer possible restarted meanwhile
                if (waitForTimeout()) {
                    startTimerThread();
                } else {
                    callbackHandler.onTimeOut(identifier);
                }
            }
        });
    }

    private synchronized boolean waitForTimeout() {
        return releaseTime > System.currentTimeMillis();
    }

    public TimeOut<T> addCallback(Callback<T> callback) {
        callbackHandler.add(callback);
        return this;
    }

    public interface Callback<T extends Enum> {
        void onTimeOut(T identifier);
    }

    private class CallbackHandler extends CallSet<Callback<T>> {
        void onTimeOut(T identifier) {
            for (Callback<T> callback : getSet()) callback.onTimeOut(identifier);
        }
    }

    private class TimerThread extends Thread implements Runnable {
        @Override
        public void run() {
            super.run();

            while (waitForTimeout()) try {
                sleep(1);
            } catch (InterruptedException ignored) {
            }

            onTimeOut();
        }
    }
}

