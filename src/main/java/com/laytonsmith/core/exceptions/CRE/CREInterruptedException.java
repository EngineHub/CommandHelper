package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;

/**
 *
 */
@typeof("ms.lang.InterruptedException")
public class CREInterruptedException extends CREException {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get(CREInterruptedException.class);

	public CREInterruptedException(String msg, Target t) {
		super(msg, t);
	}

	public CREInterruptedException(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	public CREInterruptedException(InterruptedException ex, Target t) {
		super("", t, ex);
	}

	@Override
	public String docs() {
		return "This exception is thrown if the given operation is interrupted.";
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
