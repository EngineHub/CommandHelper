package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.Target;

/**
 * 
 */
@typeof("InsufficientArgumentsException")
public class CREInsufficientArgumentsException extends CREException {
	public CREInsufficientArgumentsException(String msg, Target t) {
		super(msg, t);
	}

	public CREInsufficientArgumentsException(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	@Override
	public String docs() {
		return "Some var arg functions may require at least a certain number of"
			+ " arguments to be passed to the function";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}
}
