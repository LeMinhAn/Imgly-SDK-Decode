package com.camerafilter;

import android.content.res.Resources;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.FieldPacker;
import android.renderscript.RSRuntimeException;
import android.renderscript.RenderScript;
import android.renderscript.ScriptC;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class ScriptC_split extends ScriptC {
    private static final String __rs_resource_name = "split";
    private Element __ALLOCATION;
    private Element __U8_4;
    private FieldPacker __rs_fp_ALLOCATION;
    private static final int mExportVarIdx_rsAllocationRGB = 0;
    private Allocation mExportVar_rsAllocationRGB;
    private static final int mExportVarIdx_rsAllocationAlpha = 1;
    private Allocation mExportVar_rsAllocationAlpha;
    private static final int mExportForEachIdx_splitLayer = 1;
    private static final int mExportForEachIdx_combineLayer = 2;

    public ScriptC_split(RenderScript rs) {
        this(rs, rs.getApplicationContext().getResources(), rs.getApplicationContext().getResources().getIdentifier("split", "raw", rs.getApplicationContext().getPackageName()));
    }

    public ScriptC_split(RenderScript rs, Resources resources, int id) {
        super(rs, resources, id);
        this.__ALLOCATION = Element.ALLOCATION(rs);
        this.__U8_4 = Element.U8_4(rs);
    }

    public synchronized void set_rsAllocationRGB(Allocation v) {
        this.setVar(0, v);
        this.mExportVar_rsAllocationRGB = v;
    }

    public Allocation get_rsAllocationRGB() {
        return this.mExportVar_rsAllocationRGB;
    }

    public FieldID getFieldID_rsAllocationRGB() {
        return this.createFieldID(0, (Element)null);
    }

    public synchronized void set_rsAllocationAlpha(Allocation v) {
        this.setVar(1, v);
        this.mExportVar_rsAllocationAlpha = v;
    }

    public Allocation get_rsAllocationAlpha() {
        return this.mExportVar_rsAllocationAlpha;
    }

    public FieldID getFieldID_rsAllocationAlpha() {
        return this.createFieldID(1, (Element)null);
    }

    public KernelID getKernelID_splitLayer() {
        return this.createKernelID(1, 26, (Element)null, (Element)null);
    }

    public void forEach_splitLayer(Allocation aout) {
        this.forEach_splitLayer(aout, (LaunchOptions)null);
    }

    public void forEach_splitLayer(Allocation aout, LaunchOptions sc) {
        if(!aout.getType().getElement().isCompatible(this.__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        } else {
            this.forEach(1, (Allocation)null, aout, (FieldPacker)null, sc);
        }
    }

    public KernelID getKernelID_combineLayer() {
        return this.createKernelID(2, 26, (Element)null, (Element)null);
    }

    public void forEach_combineLayer(Allocation aout) {
        this.forEach_combineLayer(aout, (LaunchOptions)null);
    }

    public void forEach_combineLayer(Allocation aout, LaunchOptions sc) {
        if(!aout.getType().getElement().isCompatible(this.__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        } else {
            this.forEach(2, (Allocation)null, aout, (FieldPacker)null, sc);
        }
    }
}
