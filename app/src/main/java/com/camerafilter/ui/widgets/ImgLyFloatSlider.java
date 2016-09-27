package com.camerafilter.ui.widgets;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.camerafilter.R;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class ImgLyFloatSlider extends RelativeLayout implements SeekBar.OnSeekBarChangeListener {

    @NonNull
    private final SeekBar seekBar;

    private float minValue = 0;
    private float maxValue = 1;
    private int steps = 1000;

    private float value = 0f;

    private OnSeekBarChangeListener listener;

    public ImgLyFloatSlider(Context context) {
        this(context, null);
    }

    public ImgLyFloatSlider(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImgLyFloatSlider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View container = inflate(context, R.layout.imgly_widget_slider, this);

        seekBar = (SeekBar) container.findViewById(R.id.seekBarSlider);
        seekBar.setOnSeekBarChangeListener(this);
    }

    public static int convertFromRange(float value, float minValue, float maxValue, int steps){

        float rangeValue = (Math.min(Math.max(value, minValue), maxValue) - minValue) / (maxValue - minValue); // Hold in Range an convert t 0.0 - 1.0 range

        return Math.round((rangeValue) * (steps));
    }

    public static float convertToRange(int value, float minValue, float maxValue, int steps){

        float rangeValue = Math.min(Math.max(value, 0), steps) / (float) steps; // Hold in Range an slide to Zero

        return (rangeValue * (maxValue - minValue)) + minValue;
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        this.listener = listener;
    }

    public float getMax() {
        return maxValue;
    }

    public float getMin() {
        return minValue;
    }

    public int getSteps() {
        return steps;
    }

    public synchronized void setMax(float max) {
        this.maxValue = max;
        postInvalidateConfig();
    }

    public synchronized void setMin(float min) {
        this.minValue = min;
        postInvalidateConfig();
    }

    public void setSteps(int steps) {
        this.steps = steps;
        postInvalidateConfig();
    }

    public void setValue(float value) {
        this.value = value;
        postInvalidateConfig();
    }

    public float getValue() {
        return value;
    }

    /**
     * Get the Progress in percent.
     * @return get progress in percent from 0.0 to 1.0
     */
    public float getPercentageProgress() {
        return seekBar.getProgress() / (float) calculateSeekBarMax();
    }

    /**
     * Set the Progress in percent.
     * @param percent progress in percent from 0.0 to 1.0
     */
    public void setPercentageProgress(float percent) {
        setValue(convertToRange(
                Math.round(calculateSeekBarMax() * percent),
                minValue,
                maxValue,
                steps
        ));
    }

    private int calculateSeekBarMax() {
        return convertFromRange(maxValue, minValue, maxValue, steps);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        event.offsetLocation(-seekBar.getX(), 0);

        seekBar.onTouchEvent(event);

        return true;
    }

    private void invalidateConfig(float value, float  minValue, float maxValue, int steps) {
        seekBar.setMax(calculateSeekBarMax());
        seekBar.setProgress(convertFromRange(value, minValue, maxValue, steps));
    }

    private PostInvalidation postInvalidation = null;

    private void postInvalidateConfig() {
        if (postInvalidation == null) {
            postInvalidation = new PostInvalidation();
            post(postInvalidation);
        }
        postInvalidation.postValue = value;
        postInvalidation.postSteps = steps;
        postInvalidation.postMinValue = minValue;
        postInvalidation.postMaxValue = maxValue;
    }

    private class PostInvalidation implements Runnable {

        // This Prevents wrong invalidation states

        private float postValue;
        private float postMinValue;
        private float postMaxValue;
        private int postSteps;

        @Override
        public void run() {
            postInvalidation = null;
            invalidateConfig(postValue, postMinValue, postMaxValue, postSteps);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        float progressConversion = convertToRange(progress, minValue, maxValue, steps);
        if (fromUser) {
            value = progressConversion;
        }
        if (listener != null) {
            listener.onProgressChanged(this, progressConversion, fromUser);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (listener != null) {
            listener.onStartTrackingTouch(this);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (listener != null) {
            listener.onStopTrackingTouch(this);
        }
    }

    /**
     * A callback that notifies clients when the progress level has been
     * changed. This includes changes that were initiated by the user through a
     * touch gesture or arrow key/trackball as well as changes that were initiated
     * programmatically.
     */
    public interface OnSeekBarChangeListener {

        /**
         * Notification that the progress level has changed. Clients can use the fromUser parameter
         * to distinguish user-initiated changes from those that occurred programmatically.
         *
         * @param seekBar The SeekBar whose progress has changed
         * @param value The current progress level. This will be in the range min..max where max
         *        was set by {@link #setMax(float)} and min was set by {@link #setMin(float)}. (The default value for max is 1.)
         * @param fromUser True if the progress change was initiated by the user.
         */
        void onProgressChanged(ImgLyFloatSlider seekBar, float value, boolean fromUser);

        /**
         * Notification that the user has started a touch gesture. Clients may want to use this
         * to disable advancing the seekbar.
         * @param seekBar The SeekBar in which the touch gesture began
         */
        void onStartTrackingTouch(ImgLyFloatSlider seekBar);

        /**
         * Notification that the user has finished a touch gesture. Clients may want to use this
         * to re-enable advancing the seekbar.
         * @param seekBar The SeekBar in which the touch gesture began
         */
        void onStopTrackingTouch(ImgLyFloatSlider seekBar);
    }
}
