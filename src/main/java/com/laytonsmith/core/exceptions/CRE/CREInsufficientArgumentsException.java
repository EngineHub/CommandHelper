package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.GenericParameters;

/**
 *
 */
@typeof("ms.lang.InsufficientArgumentsException")
public class CREInsufficientArgumentsException extends CREException {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get(CREInsufficientArgumentsException.class);

	public CREInsufficientArgumentsException(String msg, Target t) {
		super(msg, t);
	}

	public CREInsufficientArgumentsException(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	@Override
	public String docs() {
		return "Some var arg functions may require at least a certain number of"
				+ " arguments to be passed to the function";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_1;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return super.getSuperclasses();
	}

	@Override
	public CClassType[] getInterfaces() {
		return super.getInterfaces();
	}

	@Override
	public GenericParameters getGenericParameters() {
		return null;
	}
}
