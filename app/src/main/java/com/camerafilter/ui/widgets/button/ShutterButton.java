package com.camerafilter.ui.widgets.button;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.camerafilter.R;

/**
 * Created by Le Minh An on 9/26/2016.
 */
public class ShutterButton extends Button implements View.OnClickListener{

    @NonNull
    final AnimationDrawable frameAnimation;
    OnClickListener listener;

    public ShutterButton(Context context) {
        this(context, null, 0);
    }

    public ShutterButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShutterButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setBackgroundResource(R.drawable.imgly_button_shutter_pressed_animation);
        frameAnimation = (AnimationDrawable) getBackground();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOnClickListener(OnClickListener listener) {
        super.setOnClickListener(this);
        this.listener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View v) {
        listener.onClick(v);
        playAnimation();
    }

    private void playAnimation(){
        post(new Runnable() {
            public void run() {
                if(frameAnimation.isRunning()){
                    frameAnimation.stop();
                }
                frameAnimation.start();
            }
        });
    }

}

