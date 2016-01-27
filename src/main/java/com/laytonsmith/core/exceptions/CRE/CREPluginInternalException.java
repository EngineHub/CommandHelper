package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.Target;

/**
 * 
 */
@typeof("PluginInternalException")
public class CREPluginInternalException extends CREException {
	public CREPluginInternalException(String msg, Target t) {
		super(msg, t);
	}

	public CREPluginInternalException(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	@Override
	public String docs() {
		return "This exception is thrown when a plugin is loaded, but a call to the"
			+ " plugin failed, usually for some reason specific to the plugin. Check"
			+ " the error message for more details about this error.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}
}
