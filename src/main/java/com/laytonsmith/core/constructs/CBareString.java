package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;

/**
 *
 *
 */
// A bare string is just a string. In general, we don't expect this to ever be actually used at compile time, but
// in theory it could remain, in some places. Eventually, this class should just be deleted, and a flag added to CString
// to denote that a value was created as a bare string.
@typeof(value = "ms.lang.string")
public class CBareString extends CString {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CString.TYPE;

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
		return CClassType.EMPTY_CLASS_ARRAY;
	}

	@Override
	public CBareString duplicate() {
		return new CBareString(val(), getTarget());
	}
}
