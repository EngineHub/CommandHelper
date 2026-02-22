package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.PureUtilities.Common.Annotations.AggressiveDeprecation;

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

	/** @deprecated Use {@link #getBooleanValue(Target, Environment)} instead. */
	@AggressiveDeprecation(deprecationDate = "2022-04-06", removalVersion = "3.3.7", deprecationVersion = "3.3.6")
	@Deprecated
	@Override
	public boolean getBooleanValue(Target t) {
		return getBooleanValue(t, null);
	}

	@Override
	public boolean getBooleanValue(Target t, Environment env) {
		return getNumber() != 0.0;
	}

	@Override
	public GenericParameters getGenericParameters() {
		return null;
	}
}
