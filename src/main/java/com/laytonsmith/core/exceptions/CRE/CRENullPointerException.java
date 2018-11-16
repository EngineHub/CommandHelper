package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;

/**
 *
 */
@typeof("ms.lang.NullPointerException")
public class CRENullPointerException extends CREException {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get("ms.lang.NullPointerException");

	public CRENullPointerException(String msg, Target t) {
		super(msg, t);
	}

	public CRENullPointerException(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	@Override
	public String docs() {
		return "If a null is sent, but not expected, this exception is thrown. Additionally, this is thrown"
				+ " if null is dereferenced.";
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
