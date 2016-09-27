package com.camerafilter.sdk.views;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import com.camerafilter.acs.Cam;
import com.camerafilter.acs.CamView;
import com.camerafilter.sdk.configuration.AbstractConfig;
import com.camerafilter.sdk.gles.DefaultConfigChooser;
import com.camerafilter.sdk.gles.DefaultContextFactory;
import com.camerafilter.sdk.gles.GlTextureView;
import com.camerafilter.sdk.renderer.PreviewRenderer;

import static android.opengl.GLES20.GL_MAX_RENDERBUFFER_SIZE;
import static android.opengl.GLES20.GL_MAX_TEXTURE_SIZE;
import static android.opengl.GLES20.glGetIntegerv;

/**
 * Created by Le Minh An on 9/24/2016.
 */
public class GlCameraPreview extends GlTextureView implements CamView.Preview, PreviewRenderer.RendererCallback {

    private final Cam camera = Cam.getInstance();

    private int maxTextureSize;
    private int maxRenderBufferSize;

    private boolean isInitialized = false;

    @NonNull
    protected final PreviewRenderer renderer;

    final boolean faceMirror = true;
    public GlCameraPreview(@NonNull final Context context) {
        this(context, null);
    }

    public GlCameraPreview(@NonNull final Context context, final AttributeSet attrs) {
        super(context, attrs);

        setEGLConfigChooser(new DefaultConfigChooser(false, 2));
        setEGLContextFactory(new DefaultContextFactory(2));

        renderer = new PreviewRenderer(context, this);

        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }


    public AbstractConfig.ImageFilterInterface getFilter() {
        return renderer.getFilter();
    }

    public synchronized void setFilter(final AbstractConfig.ImageFilterInterface shader) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                renderer.setFilter(shader);
            }
        });
    }

    @Override
    public void onSurfaceChanged(int width, int height) {

    }

    @Override
    public synchronized void startPreview() {
        if (!isInitialized) return;
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (maxTextureSize == 0) {

                    final int[] args = new int[1];
                    glGetIntegerv(GL_MAX_TEXTURE_SIZE, args, 0);
                    maxTextureSize = args[0];

                    glGetIntegerv(GL_MAX_RENDERBUFFER_SIZE, args, 0);
                    maxRenderBufferSize = args[0];

                    camera.setPreviewSize(maxTextureSize, maxRenderBufferSize);
                }

                startPreviewAfterTextureSizeAvailable();
            }
        });
    }

    @Override
    public synchronized void onStopPreview() {

    }

    public synchronized void onRendererInitialized() {
        isInitialized = true;
        startPreview();
    }

    private synchronized void startPreviewAfterTextureSizeAvailable() {
        post(new Runnable() {
            @Override
            public void run() {
                requestLayout();
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        renderer.onStartPreview(camera, faceMirror);
                    }
                });
            }
        });

    }

    public synchronized void onStartPreviewFinished() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                camera.startPreview();
            }
        });
    }
}


