package com.camerafilter.ui.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;

import com.camerafilter.sdk.brush.drawer.PaintChunkDrawer;
import com.camerafilter.sdk.brush.models.PaintChunk;
import com.camerafilter.sdk.brush.models.Painting;

/**
 * Created by Le Minh An on 9/26/2016.
 */
public class BrushToolPreviewView extends android.support.v7.widget.RecyclerView {

    private Painting painting;
    private PaintChunkDrawer drawer;

    private float size = 10;
    private int color = Color.TRANSPARENT;
    private float hardness = 1;

    private int width = 1;
    private int height = 1;

    public BrushToolPreviewView(Context context) {
        this(context, null);
    }

    public BrushToolPreviewView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BrushToolPreviewView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        painting = new Painting();

        setWillNotDraw(false);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        recreatePainting(width, height);
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public void setHardness(float hardness) {
        this.hardness = hardness;
    }

    public void update(){
        recreatePainting(width, height);
        postInvalidate();
    }

    private void recreatePainting(int w, int h) {
        int padding = (int) Math.ceil(size + getResources().getDisplayMetrics().density * 10);

        final int startX = padding;
        final int startY = padding;
        final int endX = w - padding;
        final int endY = h - padding;
        final int rangeX = endX - startX;

        painting.size = size;
        painting.color = color;
        painting.hardness = hardness;

        painting.getPaintChunks().clear();

        PaintChunk chunk = painting.startPaintChunk();
        painting.addPoint(startX, startY);
        painting.addPoint(startX + rangeX / 4, endY);
        painting.addPoint(startX + rangeX / 2, startY);
        painting.addPoint(endX, endY);
        painting.finalizePaintChunk();

        drawer = new PaintChunkDrawer(chunk);
        setLayerType(LAYER_TYPE_SOFTWARE, drawer.getLayerPaint());
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        if (drawer != null) {
            drawer.drawPath(c, 0);
        }

    }
}

