package com.laytonsmith.core.exceptions;

/**
 * This error should be thrown to indicate that the error is not with the user's
 * scripts, but that the error is in MethodScript itself. These errors are described
 * differently to the user, and are caught by the highest level handlers.
 */
public class NonScriptError extends Error {

	public NonScriptError(String message) {
		super(message);
	}

	public NonScriptError(String message, Throwable cause) {
		super(message, cause);
	}
	
}
