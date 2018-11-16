package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;

/**
 *
 */
@typeof("ms.lang.InsufficientPermissionException")
public class CREInsufficientPermissionException extends CREException {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get("ms.lang.InsufficientPermissionException");

	public CREInsufficientPermissionException(String msg, Target t) {
		super(msg, t);
	}

	public CREInsufficientPermissionException(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	@Override
	public String docs() {
		return "This exception is thrown if the user running the command does not"
				+ " have permission to run the function";
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
