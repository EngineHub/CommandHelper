package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.MSVersion;

/**
 *
 */
@typeof("ms.lang.number")
public abstract class CNumber extends CPrimitive {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get(CNumber.class);

	public CNumber(String value, ConstructType type, Target t) {
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
		return "A number is any double or integer number.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_0_1;
	}

	public abstract double getNumber();

	@Override
	public boolean getBooleanValue(Target t) {
		return getNumber() != 0.0;
	}
}
