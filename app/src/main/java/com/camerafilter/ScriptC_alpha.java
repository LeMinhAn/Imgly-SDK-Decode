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
public class ScriptC_alpha extends ScriptC {
    private static final String __rs_resource_name = "alpha";
    private Element __ALLOCATION;
    private Element __U8;
    private Element __U8_4;
    private FieldPacker __rs_fp_ALLOCATION;
    private FieldPacker __rs_fp_U8;
    private static final int mExportVarIdx_rsAllocationIn = 0;
    private Allocation mExportVar_rsAllocationIn;
    private static final int mExportVarIdx_rsAllocationOut = 1;
    private Allocation mExportVar_rsAllocationOut;
    private static final int mExportVarIdx_alpha = 2;
    private short mExportVar_alpha;
    private static final int mExportForEachIdx_setImageAlpha = 1;

    public ScriptC_alpha(RenderScript rs) {
        this(rs, rs.getApplicationContext().getResources(), rs.getApplicationContext().getResources().getIdentifier("alpha", "raw", rs.getApplicationContext().getPackageName()));
    }

    public ScriptC_alpha(RenderScript rs, Resources resources, int id) {
        super(rs, resources, id);
        this.__ALLOCATION = Element.ALLOCATION(rs);
        this.mExportVar_alpha = 0;
        this.__U8 = Element.U8(rs);
        this.__U8_4 = Element.U8_4(rs);
    }

    public synchronized void set_rsAllocationIn(Allocation v) {
        this.setVar(0, v);
        this.mExportVar_rsAllocationIn = v;
    }

    public Allocation get_rsAllocationIn() {
        return this.mExportVar_rsAllocationIn;
    }

    public FieldID getFieldID_rsAllocationIn() {
        return this.createFieldID(0, (Element)null);
    }

    public synchronized void set_rsAllocationOut(Allocation v) {
        this.setVar(1, v);
        this.mExportVar_rsAllocationOut = v;
    }

    public Allocation get_rsAllocationOut() {
        return this.mExportVar_rsAllocationOut;
    }

    public FieldID getFieldID_rsAllocationOut() {
        return this.createFieldID(1, (Element)null);
    }

    public synchronized void set_alpha(short v) {
        if(this.__rs_fp_U8 != null) {
            this.__rs_fp_U8.reset();
        } else {
            this.__rs_fp_U8 = new FieldPacker(1);
        }

        this.__rs_fp_U8.addU8(v);
        this.setVar(2, this.__rs_fp_U8);
        this.mExportVar_alpha = v;
    }

    public short get_alpha() {
        return this.mExportVar_alpha;
    }

    public FieldID getFieldID_alpha() {
        return this.createFieldID(2, (Element)null);
    }

    public KernelID getKernelID_setImageAlpha() {
        return this.createKernelID(1, 26, (Element)null, (Element)null);
    }

    public void forEach_setImageAlpha(Allocation aout) {
        this.forEach_setImageAlpha(aout, (LaunchOptions)null);
    }

    public void forEach_setImageAlpha(Allocation aout, LaunchOptions sc) {
        if(!aout.getType().getElement().isCompatible(this.__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        } else {
            this.forEach(1, (Allocation)null, aout, (FieldPacker)null, sc);
        }
    }
}

