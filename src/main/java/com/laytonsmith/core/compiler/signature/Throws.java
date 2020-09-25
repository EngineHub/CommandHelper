package com.laytonsmith.core.compiler.signature;

import com.laytonsmith.core.exceptions.CRE.CREThrowable;

/**
 * Represents an exception that is stated to be possibly thrown by a function.
 * @author P.J.S. Kools
 */
public class Throws {

	private final Class<? extends CREThrowable> exceptionClass;
	private final String thrownWhen;

	/**
	 * Creates a new {@link Throws} with the given properties.
	 * @param exception - The exception {@link Class} representing the exception that can be thrown.
	 * @param when - When the exception can be thrown.
	 */
	public Throws(Class<? extends CREThrowable> exception, String when) {
		this.exceptionClass = exception;
		this.thrownWhen = when;
	}

	/**
	 * Gets the exception class representing the exception that can be thrown.
	 * @return The exception {@link Class}.
	 */
	public Class<? extends CREThrowable> getExceptionClass() {
		return this.exceptionClass;
	}

	/**
	 * Gets when the exception can be thrown.
	 * @return When the exception can be thrown.
	 */
	public String getThrownWhen() {
		return this.thrownWhen;
	}
}
