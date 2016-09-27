package com.camerafilter.ui.panels;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageButton;

import com.camerafilter.ImgLySdk;
import com.camerafilter.R;
import com.camerafilter.sdk.configuration.AbstractConfig;
import com.camerafilter.sdk.configuration.FontConfig;
import com.camerafilter.sdk.configuration.PhotoEditorSdkConfig;
import com.camerafilter.sdk.configuration.TextStickerConfig;
import com.camerafilter.sdk.tools.AbstractTool;
import com.camerafilter.sdk.tools.AbstractToolPanel;
import com.camerafilter.sdk.tools.TextTool;
import com.camerafilter.ui.adapter.DataSourceListAdapter;
import com.camerafilter.ui.widgets.HorizontalListView;
import com.camerafilter.ui.widgets.RelativeBlurLayout;
import com.camerafilter.ui.widgets.VerticalListView;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class TextFontOptionToolPanel extends AbstractToolPanel implements DataSourceListAdapter.OnItemClickListener<AbstractConfig.FontConfigInterface> {

    private static final int LAYOUT = R.layout.imgly_panel_tool_font;

    private TextTool.FontSelection fontTool;

    private RelativeBlurLayout fontPicker;
    private ImageButton actionButton;


    private DataSourceListAdapter listAdapter;
    private DataSourceListAdapter bigListAdapter;

    @Override
    protected int getLayoutResource() {
        return LAYOUT;
    }

    @Override
    protected void onAttached(Context context, @NonNull View panelView, AbstractTool tool) {
        super.onAttached(context, panelView, tool);
        ImgLySdk.getAnalyticsPlugin().changeScreen("FontTool");

        HorizontalListView listView = (HorizontalListView) panelView.findViewById(R.id.optionList);
        VerticalListView bigListView  = (VerticalListView) panelView.findViewById(R.id.bigFontList);

        fontPicker = (RelativeBlurLayout) panelView.findViewById(R.id.fontPicker);

        actionButton = (ImageButton) panelView.findViewById(R.id.openFontListButton);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFontPicker(true);
            }
        });

        this.fontTool = (TextTool.FontSelection) tool;

        bigListAdapter = new DataSourceListAdapter(context);
        listAdapter = new DataSourceListAdapter(context);

        listAdapter.setData(PhotoEditorSdkConfig.getFontConfig());
        bigListAdapter.setData(PhotoEditorSdkConfig.getFontConfig());

        listAdapter.setSelection(fontTool.getFontConfig());
        bigListAdapter.setSelection(fontTool.getFontConfig());

        listAdapter.setOnItemClickListener(this);
        bigListAdapter.setOnItemClickListener(new DataSourceListAdapter.OnItemClickListener<AbstractConfig.FontConfigInterface>() {
            @Override
            public void onItemClick(AbstractConfig.FontConfigInterface entity) {
                listAdapter.setSelection(entity);
                TextFontOptionToolPanel.this.onItemClick(entity);
            }
        });

        listAdapter.setUseVerticalLayout(false);
        bigListAdapter.setUseVerticalLayout(true);

        listView.setAdapter(listAdapter);
        bigListView.setAdapter(bigListAdapter);

        fontPicker.setAlpha(0f);
        fontPicker.setTranslationY(bigListView.getHeight() / 2);

        TextStickerConfig textStickerConfig = fontTool.getEditorPreview().getCurrentTextConfig();

        if (textStickerConfig != null) {
            FontConfig.currentPreviewText = textStickerConfig.getText();
        }

    }

    @Override
    protected int onBeforeDetach(View panelView, boolean revertChanges) {
        int time = super.onBeforeDetach(panelView, revertChanges);
        showFontPicker(false);
        return time;
    }



    private void showFontPicker(boolean open) {

        AnimatorSet animatorSet = new AnimatorSet();

        fontPicker.updateBlur();
        fontPicker.setVisibility(View.VISIBLE);

        if (open) {
            animatorSet.playTogether(
                    ObjectAnimator.ofFloat(fontPicker,  "alpha",         fontPicker.getAlpha(), 1f),
                    ObjectAnimator.ofFloat(fontPicker,  "translationY", fontPicker.getTranslationY(), 0f),

                    ObjectAnimator.ofFloat(actionButton, "alpha",        actionButton.getAlpha(), 0f),
                    ObjectAnimator.ofFloat(actionButton, "scaleX",       actionButton.getScaleX(), 20f),
                    ObjectAnimator.ofFloat(actionButton, "scaleY",       actionButton.getScaleY(), 20f),
                    ObjectAnimator.ofFloat(actionButton, "translationX", actionButton.getTranslationX(), -fontPicker.getWidth()  / 2)
            );
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override public void onAnimationStart(Animator animation)  {}
                @Override public void onAnimationCancel(Animator animation) {}
                @Override public void onAnimationRepeat(Animator animation) {}

                @Override
                public void onAnimationEnd(Animator animation) {
                    actionButton.setVisibility(View.INVISIBLE);
                }

            });
        } else {
            actionButton.setVisibility(View.VISIBLE);
            animatorSet.playTogether(
                    ObjectAnimator.ofFloat(fontPicker,  "alpha",        fontPicker.getAlpha(), 0f),
                    ObjectAnimator.ofFloat(fontPicker,  "translationY", fontPicker.getTranslationX(), fontPicker.getHeight()  / 2),

                    ObjectAnimator.ofFloat(actionButton, "alpha",        actionButton.getAlpha(), 1f),
                    ObjectAnimator.ofFloat(actionButton, "scaleX",       actionButton.getScaleX(), 1f),
                    ObjectAnimator.ofFloat(actionButton, "scaleY",       actionButton.getScaleY(), 1f),
                    ObjectAnimator.ofFloat(actionButton, "translationX", actionButton.getTranslationX(), 0f),
                    ObjectAnimator.ofFloat(actionButton, "translationY", actionButton.getTranslationY(), 0f)

            );
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override public void onAnimationStart(Animator animation)  {}
                @Override public void onAnimationCancel(Animator animation) {}
                @Override public void onAnimationRepeat(Animator animation) {}

                @Override
                public void onAnimationEnd(Animator animation) {
                    fontPicker.setVisibility(View.INVISIBLE);
                }

            });
        }

        animatorSet.setDuration(ANIMATION_DURATION);
        animatorSet.start();
    }

    @Override
    protected void onDetached() {

    }

    public void onItemClick(AbstractConfig.FontConfigInterface entity) {
        showFontPicker(false);
        bigListAdapter.setSelection(entity);
        fontTool.setFontConfig(entity);
    }
}
