package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.Target;

/**
 * 
 */
@typeof("InvalidWorldException")
public class CREInvalidWorldException extends CREException {
	public CREInvalidWorldException(String msg, Target t) {
		super(msg, t);
	}

	public CREInvalidWorldException(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	@Override
	public String docs() {
		return "If a function requests a world, and the world given doesn't exist,"
			+ " this is thrown";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}
}
