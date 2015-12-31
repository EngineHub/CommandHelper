package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.Target;

/**
 * 
 */
@typeof("BindException")
public class CREBindException extends CREException {
	public CREBindException(String msg, Target t) {
		super(msg, t);
	}

	public CREBindException(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	@Override
	public String docs() {
		return "This exception is thrown if an error occurs when trying to bind() an"
			+ " event, or if a event framework related error occurs.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}
}
