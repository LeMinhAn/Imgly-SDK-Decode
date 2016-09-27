package com.camerafilter.ui.widgets;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.camerafilter.R;

/**
 * Created by Le Minh An on 9/24/2016.
 */
public class ConfirmPopupView extends DialogFragment {

    private Listener listener;

    @Nullable
    private View blurSource = null;

    @NonNull
    public ConfirmPopupView setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    @NonNull
    public ConfirmPopupView blurSource(View blurSource) {
        this.blurSource = blurSource;
        return this;
    }

    public int show(@NonNull Activity activity) {

        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        Fragment prev = activity.getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        return super.show(ft, "dialog");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.TransparentDialog);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.imgly_popup_confirm, container, false);

        v.findViewById(R.id.agreeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onConfirmPopupResult(true);
                }
                dismiss();
            }
        });

        v.findViewById(R.id.disagreeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onConfirmPopupResult(false);
                }
                dismiss();
            }
        });

        v.setAlpha(0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(v, "alpha", v.getAlpha(), 1f)
        );

        animatorSet.start();

        RelativeBlurLayout blurView = (RelativeBlurLayout) v.findViewById(R.id.blurView);
        if (blurView != null && blurSource != null) {
            blurView.updateBlur(blurSource);
        }

        return v;
    }

    public interface Listener {
        void onConfirmPopupResult(boolean accepted);
    }
}
