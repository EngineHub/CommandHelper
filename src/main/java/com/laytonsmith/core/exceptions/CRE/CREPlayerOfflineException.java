package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.Target;

/**
 * 
 */
@typeof("PlayerOfflineException")
public class CREPlayerOfflineException extends CREException {
	public CREPlayerOfflineException(String msg, Target t) {
		super(msg, t);
	}

	public CREPlayerOfflineException(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	@Override
	public String docs() {
		return "This exception is thrown if a function expected an online player, but"
			+ " that player was offline, or the command is being run from somewhere"
			+ " not in game, and the function was trying to use the current player.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}
}
