package com.laytonsmith.core.exceptions;

import com.laytonsmith.core.constructs.Target;

/**
 * Thrown by constructs like die() to cancel the current command execution.
 */
public class CancelCommandException extends RuntimeException {

	private final Target t;
	String message;

	public CancelCommandException(String message, Target t) {
		this.t = t;
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}

	/**
	 * Returns the code target at which this cancel was triggered.
	 *
	 * @return
	 */
	public Target getTarget() {
		return t;
	}

	@Override
	public Throwable fillInStackTrace() {
		return this;
	}

}
