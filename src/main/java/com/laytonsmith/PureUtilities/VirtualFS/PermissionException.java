package com.laytonsmith.PureUtilities.VirtualFS;

/**
 * Thrown if a function failed due to a permissions issue
 *
 */
public class PermissionException extends SecurityException {

	public PermissionException(String s) {
		super(s);
	}

	public PermissionException(Throwable cause) {
		super(cause);
	}

	public PermissionException(String message, Throwable cause) {
		super(message, cause);
	}

}
