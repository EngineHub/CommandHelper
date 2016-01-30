package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Common.Annotations.ForceImplementation;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.Target;

/**
 * 
 */
@typeof("Throwable")
public class CREThrowable extends AbstractCREException {
	@ForceImplementation
	public CREThrowable(String msg, Target t) {
		super(msg, t);
	}

	@ForceImplementation
	public CREThrowable(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	@Override
	public String docs() {
		return "All throwable types must extend this method. Otherwise, they will not be allowed to be thrown"
				+ " by the in script system.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}

}
