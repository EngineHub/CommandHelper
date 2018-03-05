package com.laytonsmith.abstraction;

public class MCCommandException extends RuntimeException {

	/**
	 * Creates a new instance of <code>MCCommandException</code> without detail message.
	 */
	public MCCommandException() {
	}

	/**
	 * Constructs an instance of <code>MCCommandException</code> with the specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public MCCommandException(String msg) {
		super(msg);
	}
}
