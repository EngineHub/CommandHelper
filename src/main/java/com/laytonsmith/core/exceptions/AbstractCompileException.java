package com.laytonsmith.core.exceptions;

/**
 * This abstract {@link Exception} should be used as super class for exceptions that are thrown to indicate that
 * compilation has failed.
 */
@SuppressWarnings("serial")
public abstract class AbstractCompileException extends Exception {

	public AbstractCompileException() {
	}

	public AbstractCompileException(String message) {
		super(message);
	}

	public AbstractCompileException(String message, Throwable cause) {
		super(message, cause);
	}

	public AbstractCompileException(Throwable cause) {
		super(cause);
	}
}
