package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;

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
		return new CClassType[]{CString.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return new CClassType[]{};
	}

}
