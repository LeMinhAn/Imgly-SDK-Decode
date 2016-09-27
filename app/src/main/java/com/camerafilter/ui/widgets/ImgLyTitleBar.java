package com.camerafilter.ui.widgets;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.camerafilter.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Le Minh An on 9/24/2016.
 */
public class ImgLyTitleBar extends RelativeLayout {
    private static final int ANIMATION_DURATION = 500;
    @NonNull
    private final ViewGroup titleContainer;
    @NonNull
    private final List<TextView> textViews;

    private boolean initialSet;

    public ImgLyTitleBar(Context context) {
        this(context, null);
    }

    public ImgLyTitleBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImgLyTitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.imgly_widget_actionbar, this);
        titleContainer = (ViewGroup) findViewById(R.id.actionBarTitleBox);
        TextView textView = addNewTextView();
        textView.setText("");
        textView.setVisibility(View.INVISIBLE);
        textViews = new ArrayList<>();
        textViews.add(textView);
        initialSet = true;
    }

    @NonNull
    private TextView addNewTextView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        TextView view = (TextView) inflater.inflate(R.layout.imgly_widget_actionbar_title, titleContainer, false);
        titleContainer.addView(view, 0);
        return view;
    }

    public void setTitle(@StringRes int strRes, boolean leave) {
        setTitle(getResources().getString(strRes), leave);
    }

    public void setTitle(final CharSequence str, boolean leave) {
        final TextView oldView = textViews.get(textViews.size() - 1);
        if (initialSet) {
            initialSet = false;
            oldView.setText(str);
            oldView.setVisibility(View.VISIBLE);
        } else {
            final TextView newView = addNewTextView();
            textViews.add(newView);
            newView.setText(str);
            newView.setAlpha(0f);
            newView.setVisibility(View.VISIBLE);

            float height = getHeight();
            AnimatorSet set = new AnimatorSet();

            if (leave) {
                set.playTogether(
                        ObjectAnimator.ofFloat(oldView, "alpha", 1f, 0f),
                        ObjectAnimator.ofFloat(oldView, "translationY", 0f, height / 2f),
                        ObjectAnimator.ofFloat(newView, "alpha", 0f, 1f),
                        ObjectAnimator.ofFloat(newView, "translationY", height / -2f, 0f)
                );
            } else {
                set.playTogether(
                        ObjectAnimator.ofFloat(oldView, "alpha", 1f, 0f),
                        ObjectAnimator.ofFloat(oldView, "translationY", 0f, height / -2f),
                        ObjectAnimator.ofFloat(newView, "alpha", 0f, 1f),
                        ObjectAnimator.ofFloat(newView, "translationY", height / 2f, 0f)
                );
            }
            set.addListener(new Animator.AnimatorListener() {
                @Override public void onAnimationStart (Animator animation) {}
                @Override public void onAnimationCancel(Animator animation) {}
                @Override public void onAnimationRepeat(Animator animation) {}

                @Override
                public void onAnimationEnd(Animator animator) {
                    textViews.remove(oldView);
                    titleContainer.removeView(oldView);
                }


            });
            set.setInterpolator(new AccelerateDecelerateInterpolator());
            set.setDuration(ANIMATION_DURATION);
            set.start();
        }
    }
}

