package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.natives.interfaces.ObjectType;

/**
 *
 *
 */
public class CBareString extends CString {

    public CBareString(String value, Target t) {
	super(value, t);
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
	return new CClassType[]{CClassType.build("string")};
    }

    @Override
    public CClassType[] getInterfaces() {
	return new CClassType[]{};
    }

}
