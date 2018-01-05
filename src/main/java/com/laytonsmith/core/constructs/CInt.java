package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;

/**
 *
 *
 */
@typeof("int")
public class CInt extends CNumber implements Cloneable {

    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final CClassType TYPE = CClassType.get("int");

    public static final long serialVersionUID = 1L;
    final long val;

    public CInt(String value, Target t) {
	super(value, Construct.ConstructType.INT, t);
	try {
	    val = Long.parseLong(value);
	} catch (NumberFormatException e) {
	    throw new CREFormatException("Could not parse " + value + " as an integer", t);
	}
    }

    public CInt(long value, Target t) {
	super(Long.toString(value), Construct.ConstructType.INT, t);
	val = value;
    }

    public long getInt() {
	return val;
    }

    @Override
    public CInt clone() throws CloneNotSupportedException {
	return this;
    }

    @Override
    public boolean isDynamic() {
	return false;
    }

    @Override
    public String docs() {
	return "An integer is a discreet numerical value. All positive and negative counting numbers, as well as 0.";
    }

    @Override
    public Version since() {
	return CHVersion.V3_0_1;
    }

    @Override
    public CClassType[] getSuperclasses() {
	return new CClassType[]{CNumber.TYPE};
    }

    @Override
    public CClassType[] getInterfaces() {
	return new CClassType[]{};
    }

}
