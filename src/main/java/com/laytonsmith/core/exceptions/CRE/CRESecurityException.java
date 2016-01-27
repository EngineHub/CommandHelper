package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.Target;

/**
 * 
 */
@typeof("SecurityException")
public class CRESecurityException extends CREException {
	public CRESecurityException(String msg, Target t) {
		super(msg, t);
	}

	public CRESecurityException(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	@Override
	public String docs() {
		return "This exception is thrown if a script tries to read or write to a"
			+ " location of the filesystem that is not allowed.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}
}
