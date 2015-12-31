package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.Target;

/**
 * 
 */
@typeof("InvalidProcedureException")
public class CREInvalidProcedureException extends CREException {
	public CREInvalidProcedureException(String msg, Target t) {
		super(msg, t);
	}

	public CREInvalidProcedureException(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	@Override
	public String docs() {
		return "This exception is thrown if a procedure is used without being"
			+ " defined, or if a procedure name does not follow proper naming"
			+ " conventions.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}
}
