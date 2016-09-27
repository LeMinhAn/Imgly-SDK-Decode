package com.camerafilter.ui.widgets;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.camerafilter.ui.utilities.OrientationSensor;
import com.camerafilter.utils.SetHardwareAnimatedViews;

/**
 * Created by Le Minh An on 9/26/2016.
 */
public class AutoRotateLayout extends RelativeLayout implements OrientationSensor.OrientationListener {

    private static final int ANIMATION_DURATION = 400;

    public AutoRotateLayout(Context context) {
        super(context);
    }

    public AutoRotateLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoRotateLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!isInEditMode()) {
            OrientationSensor.getInstance().removeListener(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        setRotation(OrientationSensor.getScreenOrientation().getRotation());
        if (!isInEditMode()) {
            OrientationSensor.getInstance().addListener(this);
        }
    }

    /**
     * Would be set by the OrientationSensor Event.
     * @param screenOrientation Orientation set by Sensor.
     */
    @Override
    public void onOrientationChange(@NonNull OrientationSensor.ScreenOrientation screenOrientation) {
        final int rotation = screenOrientation.getRotation();

        boolean normalOrientation = rotation % 180 == 0;

        final float currentRotation     = getRotation() % 360;
        final float destinationRotation = (Math.abs(currentRotation - rotation) <= 180) ? rotation : ((currentRotation > rotation) ? 360 + rotation : rotation - 360);

        float scale = normalOrientation ? 1f : ((float)(getHeight() / (double) getWidth()));

        if (getRotation() != rotation) {
            AnimatorSet set = new AnimatorSet();
            set.playTogether(
                    ObjectAnimator.ofFloat(this, "rotation", currentRotation, destinationRotation)
                    //,ObjectAnimator.ofFloat(this, "scaleX",   getScaleX(), scale)
                    //,ObjectAnimator.ofFloat(this, "scaleY",   getScaleY(), scale)
            );
            set.addListener(new SetHardwareAnimatedViews(this));
            set.setDuration(ANIMATION_DURATION);
            set.start();
            //animate().rotation(destinationRotation).setDuration(ANIMATION_DURATION).start();
        }

    }
}
