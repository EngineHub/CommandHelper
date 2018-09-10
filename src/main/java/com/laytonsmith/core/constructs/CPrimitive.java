package com.laytonsmith.core.constructs;

import com.methodscript.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;

/**
 *
 */
@typeof("primitive")
public abstract class CPrimitive extends Construct {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get("primitive");

	public CPrimitive(String value, ConstructType type, Target t) {
		super(value, type, t);
	}

	@Override
	public CClassType[] getSuperclasses() {
		throw new UnsupportedOperationException();
	}

	@Override
	public CClassType[] getInterfaces() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String docs() {
		return "A primitive is any non-object and non-array data type. All primitives are pass by value.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_0_1;
	}

}
