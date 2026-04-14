package com.laytonsmith.tools.debugger;

/**
 * Specifies the security mode for the DAP debug server.
 */
public enum DebugSecurity {
	/**
	 * No authentication. Suitable for localhost-only debugging.
	 */
	NONE,

	/**
	 * SSH-style keypair authentication with TLS transport encryption.
	 * Required for remote debugging.
	 */
	KEYPAIR
}
