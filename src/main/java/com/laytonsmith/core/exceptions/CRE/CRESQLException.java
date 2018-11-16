package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;

/**
 *
 */
@typeof("ms.lang.SQLException")
public class CRESQLException extends CREException {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get("ms.lang.SQLException");

	public CRESQLException(String msg, Target t) {
		super(msg, t);
	}

	public CRESQLException(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	@Override
	public String docs() {
		return "Thrown if an SQL related exception occurs.";
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
}
