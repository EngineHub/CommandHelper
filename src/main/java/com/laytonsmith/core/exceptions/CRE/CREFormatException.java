package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.Target;

/**
 * 
 */
@typeof("FormatException")
public class CREFormatException extends CREException {
	public CREFormatException(String msg, Target t) {
		super(msg, t);
	}

	public CREFormatException(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	@Override
	public String docs() {
		return "This exception is thrown if a function expected a string to be"
			+ " formatted in a particular way, but it could not interpret the given"
			+ " value.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}
}
