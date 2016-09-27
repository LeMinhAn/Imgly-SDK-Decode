package com.camerafilter.ui.panels;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.camerafilter.ImgLySdk;
import com.camerafilter.R;
import com.camerafilter.sdk.configuration.AbstractConfig;
import com.camerafilter.sdk.configuration.PhotoEditorSdkConfig;
import com.camerafilter.sdk.configuration.TextStickerConfig;
import com.camerafilter.sdk.tools.AbstractTool;
import com.camerafilter.sdk.tools.AbstractToolPanel;
import com.camerafilter.sdk.tools.StickerTool;
import com.camerafilter.sdk.tools.TextTool;
import com.camerafilter.ui.widgets.RelativeBlurLayout;
import com.camerafilter.utils.SetHardwareAnimatedViews;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class TextToolPanel extends AbstractToolPanel implements ViewTreeObserver.OnGlobalLayoutListener, TextView.OnEditorActionListener {

    private final int DEFAULT_COLOR    = 0xFFFFFFFF; //ARGB
    private final int DEFAULT_BG_COLOR = 0x00FFFFFF; //ARGB

    int previousHeightDifference = 0;

    private static final int LAYOUT = R.layout.imgly_panel_tool_text;

    private StickerTool stickerTool;

    private TextStickerConfig currentConfig;

    private View panelView;

    private int currentColor = DEFAULT_COLOR;
    private final int currentBackgroundColor = DEFAULT_BG_COLOR;

    private final AbstractConfig.FontConfigInterface currentFontConfig = PhotoEditorSdkConfig.getFontConfig().get(0);

    private final boolean isEdit = false;

    private RelativeBlurLayout blurView;
    private EditText editText;

    @Override
    protected int getLayoutResource() {
        return LAYOUT;
    }

    @Override
    protected void onAttached(Context context, @NonNull View panelView, AbstractTool tool) {
        ImgLySdk.getAnalyticsPlugin().changeScreen("TextTool");
        ImgLySdk.getAnalyticsPlugin().sendEvent("TextTool", "Open add text dialog");

        this.stickerTool = (TextTool) tool;

        this.panelView = panelView;

        editText = (EditText) panelView.findViewById(R.id.textInputField);
        blurView = (RelativeBlurLayout) panelView.findViewById(R.id.rootView);
        blurView.updateBlur();

        editText.setText("");

        editText.setOnEditorActionListener(this);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(blurView, "translationY", blurView.getHeight(), 0f)
        );

        animatorSet.addListener(new SetHardwareAnimatedViews(panelView));
        animatorSet.setDuration(ANIMATION_DURATION);
        animatorSet.addListener(new Animator.AnimatorListener() {
            boolean isCanceled = false;
            @Override public void onAnimationStart(Animator animation) {}
            @Override public void onAnimationCancel(Animator animation) {
                isCanceled = true;
            }
            @Override public void onAnimationRepeat(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isCanceled) {
                    switchKeyboardVisibility(true);
                }
            }


        });
        animatorSet.start();

        checkKeyboardHeight(true);
    }

    public void checkKeyboardHeight(final boolean check) {
        if(panelView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                panelView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
            if (check) {
                panelView.getViewTreeObserver().addOnGlobalLayoutListener(this);
            } else {
                View rootView = panelView.getRootView();
                View actionBar = rootView.findViewById(R.id.imglyActionBar);
                editText.setTranslationY(0);
                actionBar.setTranslationY(0);
            }
        }
    }

    public void switchKeyboardVisibility(boolean enable) {
        if (editText != null) {
            InputMethodManager imm = (InputMethodManager) ImgLySdk.getAppSystemService(Context.INPUT_METHOD_SERVICE);
            if (enable) {
                editText.requestFocusFromTouch();
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            } else {
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onGlobalLayout() {
        if (panelView != null) {
            Rect r = new Rect();

            panelView.getWindowVisibleDisplayFrame(r);

            View rootView  = panelView.getRootView();
            View actionBar = rootView.findViewById(R.id.imglyActionBar);

            int screenHeight = rootView.getHeight();
            int heightDifference = screenHeight - (r.bottom);

            previousHeightDifference = heightDifference;

            AnimatorSet animatorSet = new AnimatorSet();

            if (editText != null && actionBar != null) {
                animatorSet.playTogether(
                        ObjectAnimator.ofFloat(editText, "translationY", editText.getTranslationY(), -heightDifference / 2),
                        ObjectAnimator.ofFloat(actionBar, "translationY", actionBar.getTranslationY(), -heightDifference)
                );
            }

            animatorSet.start();
        }
    }

    @Override
    protected int onBeforeDetach(View panelView, boolean revertChanges) {

        if (blurView != null) {
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(
                    ObjectAnimator.ofFloat(blurView, "translationY", blurView.getTranslationY(), blurView.getHeight())
            );

            animatorSet.addListener(new SetHardwareAnimatedViews(panelView));
            animatorSet.setDuration(ANIMATION_DURATION);
            animatorSet.start();
        }

        checkKeyboardHeight(false);
        switchKeyboardVisibility(false);

        if (!revertChanges && editText != null) {
            onTextChanged(editText.getText().toString().trim(), Paint.Align.LEFT);
        }

        return ANIMATION_DURATION;
    }

    @Override
    protected void onDetached() {
        View rootView  = panelView != null ? panelView.getRootView() : null;
        View actionBar = rootView  != null ? rootView.findViewById(R.id.imglyActionBar) : null;
        if (actionBar != null) {
            actionBar.setTranslationY(0);
        }
    }

    public void onTextChanged(@NonNull String text, Paint.Align align) {
        if (!isEdit || currentConfig == null) {
            currentConfig = new TextStickerConfig(text, align, currentFontConfig, currentColor, currentBackgroundColor);

            this.stickerTool.addSticker(currentConfig);
        } else {
            currentConfig.setText(text, align);
            stickerTool.refreshConfig(currentConfig);

            ImgLySdk.getAnalyticsPlugin().sendEvent("TextEdit", "Open add text dialog", "Length: " + text.length());
        }
    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        stickerTool.getEditorPreview().dispatchLeaveToolMode(false);
        return true;
    }
}
