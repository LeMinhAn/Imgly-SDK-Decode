package com.camerafilter.sdk.tools;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.ViewGroup;

import com.camerafilter.sdk.operator.AbstractOperation;
import com.camerafilter.sdk.operator.RotateOperation;
import com.camerafilter.ui.panels.OrientationToolPanel;
import com.camerafilter.sdk.views.EditorPreview;

/**
 * Created by Le Minh An on 9/26/2016.
 */
public class OrientationTool extends AbstractTool {

    enum Rotation {
        ROTATION_0,
        ROTATION_90,
        ROTATION_180,
        ROTATION_270
    }

    private RotateOperation rotateOperation;


    public final SaveState saveState = new SaveState();
    public class SaveState {
        int rotation = 0;
        boolean flipVertical   = false;
        boolean flipHorizontal = false;
    }

    @Override
    public AbstractOperation getOperation() {
        return rotateOperation;
    }

    @Override
    public boolean isRevertible() {
        return true;
    }

    @Override
    protected void saveState() {
        saveState.rotation       = rotateOperation.getRotation();
        saveState.flipVertical   = rotateOperation.isFlipVertical();
        saveState.flipHorizontal = rotateOperation.isFlipHorizontal();
    }

    @Override
    protected void revertState() {
        rotateOperation.setRotation(saveState.rotation);
        rotateOperation.setFlipVertical(saveState.flipVertical);
        rotateOperation.setFlipHorizontal(saveState.flipHorizontal);
    }

    public OrientationTool(@StringRes int name, @DrawableRes int drawableId) {
        super(name, drawableId, OrientationToolPanel.class);
    }

    public OrientationTool(@StringRes int name, @DrawableRes int drawableId, @NonNull Class<? extends AbstractToolPanel> panelClass) {
        super(name, drawableId, panelClass);
    }

    @Override
    public View attachPanel(@NonNull ViewGroup parentView, @NonNull EditorPreview preview) {
        View view = super.attachPanel(parentView, preview);

        this.rotateOperation = getOperator().getRotateOperation();
        saveState();
        return view;
    }

    public void rotateCW() {
        rotateOperation.rotateCW();
    }

    public void rotateCCW() {
        rotateOperation.rotateCCW();
    }

    public void setRotation(@NonNull Rotation rotation) {
        rotateOperation.setRotation(rotation.ordinal() * 90);
    }

    public Rotation getRotation() {
        return Rotation.values()[(rotateOperation.getRotation() % 360) / 90];
    }

    public void toogleFlipHorizontal() {
        rotateOperation.setFlipHorizontal(!rotateOperation.isFlipHorizontal());
    }

    public void toogleFlipVertical() {
        rotateOperation.setFlipVertical(!rotateOperation.isFlipVertical());
    }

    public void setFlipHorizontal(boolean isFlipped) {
        rotateOperation.setFlipHorizontal(isFlipped);
    }

    public void setFlipVertical(boolean isFlipped) {
        rotateOperation.setFlipVertical(isFlipped);
    }

    public boolean isFlipedHorizontal(){
        return rotateOperation.isFlipHorizontal();
    }

    public boolean isFlipedVertical() {
        return rotateOperation.isFlipVertical();
    }

}

