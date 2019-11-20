
package com.laytonsmith.core.webserver;

/**
 *
 */
public class ServerAlreadyUpException extends RuntimeException {

	/**
	 * Creates a new instance of <code>ServerAlreadyUpException</code> without detail message.
	 */
	public ServerAlreadyUpException() {
	}

	/**
	 * Constructs an instance of <code>ServerAlreadyUpException</code> with the specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public ServerAlreadyUpException(String msg) {
		super(msg);
	}
}
