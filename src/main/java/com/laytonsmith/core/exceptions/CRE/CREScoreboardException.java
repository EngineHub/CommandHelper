package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.Target;

/**
 * 
 */
@typeof("ScoreboardException")
public class CREScoreboardException extends CREException {
	public CREScoreboardException(String msg, Target t) {
		super(msg, t);
	}

	public CREScoreboardException(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	@Override
	public String docs() {
		return "Thrown if a scoreboard error occurs, such as attempting to create a"
				+ " team or objective with a name that is already in use,"
				+ " or trying to access one that doesn't exist.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}
}
