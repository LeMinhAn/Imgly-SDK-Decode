package com.camerafilter.sdk.gles;

import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

import static javax.microedition.khronos.egl.EGL10.EGL_ALPHA_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_BLUE_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_DEPTH_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_GREEN_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_NONE;
import static javax.microedition.khronos.egl.EGL10.EGL_RED_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_RENDERABLE_TYPE;
import static javax.microedition.khronos.egl.EGL10.EGL_STENCIL_SIZE;

/**
 * Created by Le Minh An on 9/24/2016.
 */
public class DefaultConfigChooser implements GLSurfaceView.EGLConfigChooser {

    @NonNull
    private final int[] mConfigSpec;
    private final int mRedSize;
    private final int mGreenSize;
    private final int mBlueSize;
    private final int mAlphaSize;
    private final int mDepthSize;
    private final int mStencilSize;

    private static final boolean USE_RGB_888 = false; //Build.VERSION.SDK_INT >= 17;

    public DefaultConfigChooser(final boolean withDepthBuffer, final int version) {
        this(
                USE_RGB_888 ? 8 : 5,
                USE_RGB_888 ? 8 : 6,
                USE_RGB_888 ? 8 : 5,
                0,
                withDepthBuffer ? 16 : 0,
                0,
                version
        );
    }

    public DefaultConfigChooser(
            final int redSize,
            final int greenSize,
            final int blueSize,
            final int alphaSize,
            final int depthSize,
            final int stencilSize,
            final int version) {
        mConfigSpec = filterConfigSpec(new int[]{
                EGL_RED_SIZE, redSize,
                EGL_GREEN_SIZE, greenSize,
                EGL_BLUE_SIZE, blueSize,
                EGL_ALPHA_SIZE, alphaSize,
                EGL_DEPTH_SIZE, depthSize,
                EGL_STENCIL_SIZE, stencilSize,
                EGL_NONE
        }, version);
        mRedSize = redSize;
        mGreenSize = greenSize;
        mBlueSize = blueSize;
        mAlphaSize = alphaSize;
        mDepthSize = depthSize;
        mStencilSize = stencilSize;
    }

    private static final int EGL_OPENGL_ES2_BIT = 4;

    @NonNull
    private int[] filterConfigSpec(@NonNull final int[] configSpec, final int version) {
        if (version != 2) {
            return configSpec;
        }
		/*
		 * We know none of the subclasses define EGL_RENDERABLE_TYPE.
		 * And we know the configSpec is well formed.
		 */
        final int len = configSpec.length;
        final int[] newConfigSpec = new int[len + 2];
        System.arraycopy(configSpec, 0, newConfigSpec, 0, len - 1);
        newConfigSpec[len - 1] = EGL_RENDERABLE_TYPE;
        newConfigSpec[len] = EGL_OPENGL_ES2_BIT;
        newConfigSpec[len + 1] = EGL_NONE;
        return newConfigSpec;
    }

    @Nullable
    @Override
    public EGLConfig chooseConfig(@NonNull final EGL10 egl, final EGLDisplay display) {
        final int[] num_config = new int[1];
        if (!egl.eglChooseConfig(display, mConfigSpec, null, 0, num_config)) {
            throw new IllegalArgumentException("eglChooseConfig failed");
        }
        final int config_size = num_config[0];
        if (config_size <= 0) {
            throw new IllegalArgumentException("No configs match configSpec");
        }

        final EGLConfig[] configs = new EGLConfig[config_size];
        if (!egl.eglChooseConfig(display, mConfigSpec, configs, config_size, num_config)) {
            throw new IllegalArgumentException("eglChooseConfig#2 failed");
        }
        final EGLConfig config = chooseConfig(egl, display, configs);
        if (config == null) {
            throw new IllegalArgumentException("No config chosen");
        }
        return config;
    }

    @Nullable
    EGLConfig chooseConfig(@NonNull final EGL10 egl, final EGLDisplay display, @NonNull final EGLConfig[] configs) {
        for (final EGLConfig config : configs) {
            final int d = findConfigAttribute(egl, display, config, EGL_DEPTH_SIZE, 0);
            final int s = findConfigAttribute(egl, display, config, EGL_STENCIL_SIZE, 0);
            if ((d >= mDepthSize) && (s >= mStencilSize)) {
                final int r = findConfigAttribute(egl, display, config, EGL_RED_SIZE, 0);
                final int g = findConfigAttribute(egl, display, config, EGL_GREEN_SIZE, 0);
                final int b = findConfigAttribute(egl, display, config, EGL_BLUE_SIZE, 0);
                final int a = findConfigAttribute(egl, display, config, EGL_ALPHA_SIZE, 0);
                if ((r == mRedSize) && (g == mGreenSize) && (b == mBlueSize) && (a == mAlphaSize)) {
                    return config;
                }
            }
        }
        return null;
    }

    private int findConfigAttribute(@NonNull final EGL10 egl, final EGLDisplay display, final EGLConfig config, final int attribute, final int defaultValue) {
        final int[] value = new int[1];
        if (egl.eglGetConfigAttrib(display, config, attribute, value)) {
            return value[0];
        }
        return defaultValue;
    }
}

