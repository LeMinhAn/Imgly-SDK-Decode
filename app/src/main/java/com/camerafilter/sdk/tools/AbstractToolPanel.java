package com.camerafilter.sdk.tools;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.camerafilter.utils.SetHardwareAnimatedViews;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public abstract class AbstractToolPanel {
    protected static final int ANIMATION_DURATION = 500;

    protected ViewGroup parentView;
    protected ToolView toolView;
    protected abstract @LayoutRes
    int getLayoutResource();

    private AbstractTool tool;

    public AbstractToolPanel() {}

    private boolean isActivated = false;

    protected final void init(AbstractTool tool){
        this.tool = tool;
    }

    public final View attach(@NonNull ViewGroup parentView) {
        isActivated = true;
        this.parentView = parentView;
        if (toolView == null) {
            Context context = parentView.getContext();

            this.toolView = new ToolView(this, context, getLayoutResource());

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );

            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

            toolView.setLayoutParams(params);
        }

        if (toolView.getParent() instanceof ViewGroup) {
            ((ViewGroup) toolView.getParent()).removeView(toolView);
        }
        toolView.setClickable(true);
        toolView.setFocusable(true);
        parentView.addView(toolView);
        toolView.setVisibility(View.INVISIBLE);
        return toolView;
    }

    public void refresh() {

    }

    public final void detach(final boolean revertChanges) {
        isActivated = false;
        if (parentView != null) {
            toolView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (parentView != null && !isActivated) {
                        parentView.removeView(toolView);
                    }
                }
            }, onBeforeDetach(toolView, revertChanges));
        }
    }

    public boolean isAttached() {
        return isActivated;
    }

    private void callAttached(final Context context, @NonNull final View panelView) {
        panelView.post(new Runnable() {
            @Override
            public void run() {
                panelView.setVisibility(View.VISIBLE);
                onAttached(context, panelView, tool);
                refresh();
            }
        });
    }

    /**
     * Called before the View will detached.
     * It is possible that this method will not call. Use #onDetached() instated to clear critical allocations.
     * @return a delay in ms. This will give time to Animate.
     */
    protected int onBeforeDetach(@NonNull View panelView, boolean revertChanges) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(panelView, "translationY", panelView.getTranslationY(), panelView.getHeight())
        );

        animatorSet.addListener(new SetHardwareAnimatedViews(panelView));
        animatorSet.setDuration(ANIMATION_DURATION);
        animatorSet.start();

        return ANIMATION_DURATION;
    }

    /**
     * Called on atteched the View.
     * @param context the view context
     * @param panelView the tool panel
     * @param tool the tool
     */
    protected void onAttached(Context context, @NonNull View panelView, AbstractTool tool) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(panelView, "translationY", panelView.getHeight(), 0f)
        );

        animatorSet.addListener(new SetHardwareAnimatedViews(panelView));
        animatorSet.setDuration(ANIMATION_DURATION);
        animatorSet.start();
    }

    /**
     * Called after View would detached.
     */
    protected abstract void onDetached();

    @SuppressLint("ViewConstructor")
    public static class ToolView extends RelativeLayout {
        final private AbstractToolPanel abstractToolPanel;

        public ToolView(AbstractToolPanel abstractToolPanel, Context context, @LayoutRes int layoutRes) {
            super(context);
            this.abstractToolPanel = abstractToolPanel;
            setClipToPadding(false);
            setClipChildren(false);
            inflate(context, layoutRes, this);
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            post(new Runnable() {
                @Override
                public void run() {
                    abstractToolPanel.callAttached(getContext(), ToolView.this);
                }
            });
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            abstractToolPanel.onDetached();
        }
    }
}

