package com.laytonsmith.core.webserver;

/**
 *
 */
public class InvalidVerbException extends Exception {

	/**
	 * Creates a new instance of <code>InvalidVerbException</code> without detail message.
	 */
	public InvalidVerbException() {
	}

	/**
	 * Constructs an instance of <code>InvalidVerbException</code> with the specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public InvalidVerbException(String msg) {
		super(msg);
	}
}
