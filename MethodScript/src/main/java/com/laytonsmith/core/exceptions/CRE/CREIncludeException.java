package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.Target;

/**
 * 
 */
@typeof("IncludeException")
public class CREIncludeException extends CREException {
	public CREIncludeException(String msg, Target t) {
		super(msg, t);
	}

	public CREIncludeException(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	@Override
	public String docs() {
		return "This exception is thrown if there is a problem with an include. This"
			+ " is thrown if there is a compile error in the included script.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}
}
