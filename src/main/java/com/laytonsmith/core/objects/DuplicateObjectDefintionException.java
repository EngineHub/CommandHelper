package com.laytonsmith.core.objects;

/**
 * Thrown if an ObjectDefintion is attempted to be redefined. An ObjectDefinition is uniquely identified by its fully
 * qualified class name, as a String.
 */
public class DuplicateObjectDefintionException extends Exception {
	private final boolean wasCopy;
	public DuplicateObjectDefintionException(boolean isCopy) {
		this(null, null, isCopy);
	}

	public DuplicateObjectDefintionException(String message, boolean isCopy) {
		this(message, null, isCopy);
	}

	public DuplicateObjectDefintionException(String message, Throwable cause, boolean isCopy) {
		super(message, cause);
		this.wasCopy = isCopy;
	}

	/**
	 * If the duplicate was an exact copy, then this may assist in diagnosing the problem. It is still not allowed
	 * to create duplicate classes, even if they are identical, but this flag indicates if that actually is the case.
	 * @return
	 */
	public boolean wasCopy() {
		return this.wasCopy;
	}
}
