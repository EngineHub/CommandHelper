package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.Target;

/**
 * 
 */
@typeof("UnageableMobException")
public class CREUnageableMobException extends CREException {
	public CREUnageableMobException(String msg, Target t) {
		super(msg, t);
	}

	public CREUnageableMobException(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	@Override
	public String docs() {
		return "If an age function is called on an unageable mob, this "
			+ "exception is thrown.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}
}
