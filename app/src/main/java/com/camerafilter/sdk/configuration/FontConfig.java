package com.camerafilter.sdk.configuration;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.TextView;

import com.camerafilter.ImgLySdk;
import com.camerafilter.R;
import com.camerafilter.ui.adapter.DataSourceListAdapter;
import com.camerafilter.utils.TypefaceLoader;

import java.io.File;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class FontConfig implements AbstractConfig.FontConfigInterface<FontConfig.FontBindData> {

    @Nullable
    private final String fontAssetsPath;
    @Nullable
    private final File fontFile;

    public static String currentPreviewText;

    private final String name;

    protected boolean isDirty;

    /**
     * Create Font Config
     * @param name Font name
     * @param fontAssetsPath font file in assets path.
     */
    public FontConfig(String name, String fontAssetsPath) {
        this.name = name;
        this.fontFile = null;
        this.fontAssetsPath = fontAssetsPath;
    }

    /**
     * Create Font Config
     * @param name Font name
     * @param fontFile font file path as file path
     */
    public FontConfig(String name, File fontFile) {
        this.name = name;
        this.fontFile = fontFile;
        this.fontAssetsPath = null;
    }

    /**
     * Create Font Config
     * @param name Font name
     * @param fontAssetsPath font file in assets path.
     */
    public FontConfig(@StringRes int name, String fontAssetsPath) {
        this.name = ImgLySdk.getAppResource().getString(name);
        this.fontFile = null;
        this.fontAssetsPath = fontAssetsPath;
    }

    /**
     * Create Font Config
     * @param name Font name
     * @param fontFile font file path as file path
     */
    public FontConfig(@StringRes int name, File fontFile) {
        this.name = ImgLySdk.getAppResource().getString(name);
        this.fontFile = fontFile;
        this.fontAssetsPath = null;
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    /**
     * Get typeface
     * @return get the typeface
     */
    @Nullable
    public Typeface getTypeface() {

        final Typeface typeface;
        if (fontAssetsPath != null) {
            typeface = TypefaceLoader.getTypeface(fontAssetsPath);
        } else {
            typeface = TypefaceLoader.getTypeface(fontFile);
        }

        return typeface;

    }

    @Override
    public boolean isDirty() {
        return isDirty;
    }

    @Override
    public void setDirtyFlag(boolean isDirty) {
        this.isDirty = isDirty;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getLayout() {
        return R.layout.imgly_list_item_font;
    }

    @Override
    public int getVerticalLayout() {
        return R.layout.imgly_list_item_font_big;
    }

    @NonNull
    @Override
    public FontBindData generateBindData() {
        return new FontBindData(this);
    }

    @Nullable
    @Override
    public FontBindData generateBindDataAsync() {
        return null;
    }

    public static class FontBindData extends AbstractConfig.BindData {
        final FontConfig data;

        public FontBindData(FontConfig data) {
            super(null, null);
            this.data = data;
        }
    }

    @NonNull
    @Override
    public DataSourceListAdapter.DataSourceViewHolder<FontBindData> createViewHolder(@NonNull View view, boolean useVerticalLayout) {
        return new FontViewHolder(view, useVerticalLayout);
    }

    protected static class FontViewHolder extends DataSourceListAdapter.DataSourceViewHolder<FontBindData> implements View.OnClickListener {

        public final View contentHolder;
        @NonNull
        public final TextView textView;
        @NonNull
        public final TextView labelView;

        private boolean useVerticalLayout = false;

        public FontViewHolder(@NonNull View v, boolean useVerticalLayout) {
            super(v);

            this.contentHolder = v.findViewById(R.id.contentHolder);
            this.textView  = (TextView) v.findViewById(R.id.text);
            this.labelView = (TextView) v.findViewById(R.id.label);

            this.useVerticalLayout = useVerticalLayout;

            this.contentHolder.setOnClickListener(this);


        }

        @Override
        public void setSelectedState(boolean selected) {
            contentHolder.setSelected(selected);
        }

        public void onClick(View v) {
            dispatchOnItemClick();
            dispatchSelection();
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void bind(@NonNull FontBindData bindData) {

            textView.setTypeface(bindData.data.getTypeface());
            if (useVerticalLayout) {
                textView.setText(currentPreviewText);
            } else {
                textView.setText("Ag");
            }

            labelView.setText(bindData.data.getName());
        }

    }
}
