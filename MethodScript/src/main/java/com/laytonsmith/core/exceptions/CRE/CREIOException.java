package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.Target;

/**
 * 
 */
@typeof("IOException")
public class CREIOException extends CREException {
	public CREIOException(String msg, Target t) {
		super(msg, t);
	}

	public CREIOException(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	@Override
	public String docs() {
		return "This exception is thrown if a file cannot be read or written to.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}
}
