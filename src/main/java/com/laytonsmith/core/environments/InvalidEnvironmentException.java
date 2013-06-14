package com.laytonsmith.core.environments;

/**
 *
 */
public class InvalidEnvironmentException extends RuntimeException {

	/**
	 * Creates a new instance of
	 * <code>InvalidEnvironmentException</code> without detail message.
	 */
	public InvalidEnvironmentException() {
	}

	/**
	 * Constructs an instance of
	 * <code>InvalidEnvironmentException</code> with the specified detail
	 * message.
	 *
	 * @param msg the detail message.
	 */
	public InvalidEnvironmentException(String msg) {
		super(msg);
	}
}
