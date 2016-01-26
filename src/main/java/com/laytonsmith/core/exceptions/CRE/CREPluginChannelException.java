package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.Target;

/**
 * 
 */
@typeof("PluginChannelException")
public class CREPluginChannelException extends CREException {
	public CREPluginChannelException(String msg, Target t) {
		super(msg, t);
	}

	public CREPluginChannelException(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	@Override
	public String docs() {
		return "Thrown if trying to register a plugin channel that is"
				+ " already registered, or unregister one that isn't registered.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}
}
