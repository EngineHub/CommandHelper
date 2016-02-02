package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.Target;

/**
 * 
 */
@typeof("IndexOverflowException")
public class CREIndexOverflowException extends CREException {
	public CREIndexOverflowException(String msg, Target t) {
		super(msg, t);
	}

	public CREIndexOverflowException(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	@Override
	public String docs() {
		return "This exception is thrown if a value is requested from an array that"
			+ " is above the highest index of the array, or a negative number.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}
}
