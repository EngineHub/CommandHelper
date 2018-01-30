package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIndexOverflowException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.ArrayAccess;
import com.laytonsmith.core.natives.interfaces.ObjectType;
import java.util.Set;

/**
 *
 *
 */
@typeof("string")
public class CString extends CPrimitive implements Cloneable, ArrayAccess {

    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final CClassType TYPE = CClassType.get("string");

    public CString(String value, Target t) {
	super(value == null ? "" : value, ConstructType.STRING, t);
    }

    public CString(char value, Target t) {
	this(Character.toString(value), t);
    }

    public CString(CharSequence value, Target t) {
	this(value.toString(), t);
    }

    @Override
    public CString clone() throws CloneNotSupportedException {
	return this;
    }

    @Override
    public boolean isDynamic() {
	return false;
    }

    @Override
    public final Construct get(String index, Target t) {
	try {
	    int i = Integer.parseInt(index);
	    return get(i, t);
	} catch (NumberFormatException e) {
	    throw new CREFormatException("Expecting numerical index, but recieved " + index, t);
	}
    }

    @Override
    public long size() {
	return val().length();
    }

    @Override
    public boolean canBeAssociative() {
	return false;
    }

    @Override
    public Construct slice(int begin, int end, Target t) {
	if (begin >= end) {
	    return new CString("", t);
	}
	return new CString(this.val().substring(begin, end), t);
    }

    @Override
    public String getQuote() {
	return super.getQuote();
    }

    @Override
    public Construct get(int index, Target t) throws ConfigRuntimeException {
	return new CString(this.val().charAt(index), t);
    }

    @Override
    public boolean isAssociative() {
	return false;
    }

    @Override
    public Set<Construct> keySet() {
	throw new CREIndexOverflowException("Not supported.", Target.UNKNOWN);
    }

    @Override
    public final Construct get(Construct index, Target t) throws ConfigRuntimeException {
	int i = Static.getInt32(index, t);
	return get(i, t);
    }

    @Override
    public String docs() {
	return "A string is a value that contains character data. The character encoding is stored with the string as well.";
    }

    @Override
    public Version since() {
	return CHVersion.V3_0_1;
    }

    @Override
    public CClassType[] getSuperclasses() {
	return new CClassType[]{CPrimitive.TYPE};
    }

    @Override
    public CClassType[] getInterfaces() {
	return new CClassType[]{ArrayAccess.TYPE};
    }

    @Override
    public ObjectType getObjectType() {
	return ObjectType.CLASS;
    }

}
