package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.Target;

/**
 * 
 */
@typeof("IllegalArgumentException")
public class CREIllegalArgumentException extends CREException {
	public CREIllegalArgumentException(String msg, Target t) {
		super(msg, t);
	}

	public CREIllegalArgumentException(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	@Override
	public String docs() {
		return "Thrown if an argument was illegal in the given context.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}
}
