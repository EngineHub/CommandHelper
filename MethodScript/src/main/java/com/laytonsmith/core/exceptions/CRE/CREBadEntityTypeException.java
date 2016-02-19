package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.Target;

/**
 * 
 */
@typeof("BadEntityTypeException")
public class CREBadEntityTypeException extends CREException {
	public CREBadEntityTypeException(String msg, Target t) {
		super(msg, t);
	}

	public CREBadEntityTypeException(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	@Override
	public String docs() {
		return "Thrown if an entity has the wrong type.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}
}
