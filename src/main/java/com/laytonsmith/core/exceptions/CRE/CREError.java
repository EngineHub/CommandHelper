package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.Target;

/**
 * 
 */
@typeof("Error")
public class CREError extends CREThrowable {
	public CREError(String msg, Target t) {
		super(msg, t);
	}

	public CREError(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	@Override
	public String docs() {
		return "Indicates a serious error occurred. It is not recommended to catch Error directly, but instead catch a specific subtype.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}
}
