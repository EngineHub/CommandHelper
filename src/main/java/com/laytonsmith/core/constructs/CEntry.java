package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.natives.interfaces.ObjectType;

/**
 *
 *
 */
public class CEntry extends Construct {

    Construct ckey;
    Construct construct;

    public CEntry(String value, Target t) {
	super(value, ConstructType.ENTRY, t);
	throw new UnsupportedOperationException("CEntry Constructs cannot use this constructor");
    }

    public CEntry(Construct key, Construct value, Target t) {
	super(key.val() + ":(CEntry)", ConstructType.ENTRY, t);
	this.ckey = key;
	this.construct = value;
    }

    @Override
    public String val() {
	return construct.val();
    }

    public Construct construct() {
	return this.construct;
    }

    @Override
    public boolean isDynamic() {
	return false;
    }

    @Override
    public Version since() {
	return super.since();
    }

    @Override
    public String docs() {
	return super.docs();
    }

    @Override
    public CClassType[] getSuperclasses() {
	return new CClassType[]{CClassType.MIXED};
    }

    @Override
    public CClassType[] getInterfaces() {
	return new CClassType[]{};
    }

}
