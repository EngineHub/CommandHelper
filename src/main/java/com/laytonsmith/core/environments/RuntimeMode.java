package com.laytonsmith.core.environments;

import java.util.EnumSet;

/**
 * Represents the runtime mode(s) of the runtime environment.
 * @author P.J.S. Kools
 */
public enum RuntimeMode {

	/**
	 * Indicates that the runtime was started in interpreter mode. This mode can be combined with other modes.
	 */
	INTERPRETER,

	/**
	 * Indicates that the runtime was started in commandline mode. This mode can be combined with {@link #INTERPRETER}.
	 */
	CMDLINE,

	/**
	 * Indicates that the runtime was started, embedded in some environment other than {@link #CMDLINE}.
	 */
	EMBEDDED;

	/**
	 * Validates the given {@link EnumSet} of runtime modes. Throws an {@link Error} if it is invalid.
	 * A set of runtime modes is valid when it is non-null and it contains exactly one of
	 * {@link #CMDLINE} and {@link #EMBEDDED}.
	 */
	public static void validate(EnumSet<RuntimeMode> runtimeModes) {
		if(runtimeModes == null) {
			throw new Error("Runtime modes must not be null.");
		}
		if(runtimeModes.contains(CMDLINE) == runtimeModes.contains(EMBEDDED)) {
			throw new Error("Runtime modes have to contain exactly one of " + CMDLINE + " and " + EMBEDDED + ".");
		}
	}
}
