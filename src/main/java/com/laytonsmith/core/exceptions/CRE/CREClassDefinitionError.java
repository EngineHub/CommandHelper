package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;

/**
 * This is thrown if a class definition is incorrect. Normally this should be a compiler error, but in dynamic code
 * cases, it may be a runtime error.
 */
@typeof("ms.lang.ClassDefinitionError")
public class CREClassDefinitionError extends CREError {
	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get("ms.lang.ClassDefinitionError");

	public CREClassDefinitionError(String msg, Target t) {
		super(msg, t);
	}

	public CREClassDefinitionError(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	@Override
	public String docs() {
		return "This is thrown if a class definition is incorrect. Normally this should be a compiler error,"
				+ " but in dynamic code"
				+ " cases, it may be a runtime error.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_4;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return super.getSuperclasses();
	}

	@Override
	public CClassType[] getInterfaces() {
		return super.getInterfaces();
	}
}
