package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.Target;

/**
 * 
 */
@typeof("NullPointerException")
public class CRENullPointerException extends CREException {
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
		return CHVersion.V3_3_1;
	}
}
