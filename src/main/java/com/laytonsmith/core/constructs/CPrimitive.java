package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.natives.interfaces.ValueType;

/**
 *
 */
@typeof("ms.lang.primitive")
public abstract class CPrimitive extends Construct implements ValueType {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get("ms.lang.primitive");

	public CPrimitive(String value, ConstructType type, Target t) {
		super(value, type, t);
	}

	@Override
	public CClassType[] getSuperclasses() {
		// Implemented in the Runner
		throw new UnsupportedOperationException();
	}

	@Override
	public CClassType[] getInterfaces() {
		// Implemented in the Runner
		throw new UnsupportedOperationException();
	}

	@Override
	public String docs() {
		return "A primitive is any non-object and non-array data type. All primitives are pass by value.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_0_1;
	}

}
