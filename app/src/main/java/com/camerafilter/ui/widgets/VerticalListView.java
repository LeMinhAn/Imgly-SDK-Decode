package com.camerafilter.ui.widgets;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class VerticalListView extends android.support.v7.widget.RecyclerView {
    public VerticalListView(Context context) {
        this(context, null);
    }

    public VerticalListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        setLayoutManager(layoutManager);
    }

}