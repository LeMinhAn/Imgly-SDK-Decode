package com.camerafilter;

import android.content.res.Resources;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.FieldPacker;
import android.renderscript.RSRuntimeException;
import android.renderscript.RenderScript;
import android.renderscript.ScriptC;
import android.renderscript.Type;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public class ScriptC_translate_3d_lut extends ScriptC {
    private static final String __rs_resource_name = "translate_3d_lut";
    private Element __ALLOCATION;
    private Element __U8_4;
    private FieldPacker __rs_fp_ALLOCATION;
    private static final int mExportVarIdx_rsAllocationIn = 0;
    private Allocation mExportVar_rsAllocationIn;
    private static final int mExportVarIdx_rsAllocationOut = 1;
    private Allocation mExportVar_rsAllocationOut;
    private static final int mExportForEachIdx_root = 0;

    public ScriptC_translate_3d_lut(RenderScript rs) {
        this(rs, rs.getApplicationContext().getResources(), rs.getApplicationContext().getResources().getIdentifier("translate_3d_lut", "raw", rs.getApplicationContext().getPackageName()));
    }

    public ScriptC_translate_3d_lut(RenderScript rs, Resources resources, int id) {
        super(rs, resources, id);
        this.__ALLOCATION = Element.ALLOCATION(rs);
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

    public KernelID getKernelID_root() {
        return this.createKernelID(0, 27, (Element)null, (Element)null);
    }

    public void forEach_root(Allocation ain, Allocation aout) {
        this.forEach_root(ain, aout, (LaunchOptions)null);
    }

    public void forEach_root(Allocation ain, Allocation aout, LaunchOptions sc) {
        if(!ain.getType().getElement().isCompatible(this.__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        } else if(!aout.getType().getElement().isCompatible(this.__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        } else {
            Type t0 = ain.getType();
            Type t1 = aout.getType();
            if(t0.getCount() == t1.getCount() && t0.getX() == t1.getX() && t0.getY() == t1.getY() && t0.getZ() == t1.getZ() && t0.hasFaces() == t1.hasFaces() && t0.hasMipmaps() == t1.hasMipmaps()) {
                this.forEach(0, ain, aout, (FieldPacker)null, sc);
            } else {
                throw new RSRuntimeException("Dimension mismatch between parameters ain and aout!");
            }
        }
    }
}
