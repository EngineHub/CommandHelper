package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.Target;

/**
 * 
 */
@typeof("CastException")
public class CRECastException extends CREException {
	public CRECastException(String msg, Target t) {
		super(msg, t);
	}

	public CRECastException(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	@Override
	public String docs() {
		return "This exception is thrown if a value cannot be cast into an"
			+ " appropriate type. Functions that require a numeric value, for"
			+ " instance, would throw this if the string \"hi\" were passed in.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}
}
