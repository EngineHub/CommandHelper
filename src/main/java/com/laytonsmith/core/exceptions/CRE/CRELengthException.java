package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.Target;

/**
 * 
 */
@typeof("LengthException")
public class CRELengthException extends CREException {
	public CRELengthException(String msg, Target t) {
		super(msg, t);
	}

	public CRELengthException(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	@Override
	public String docs() {
		return "This exception is thrown if a function expected the length of"
			+ " something to be a particular value, but it was not.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}
}
