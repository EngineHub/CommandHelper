package com.laytonsmith.core.objects;

import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigCompileException;

/**
 * Thrown if an ObjectDefintion is attempted to be redefined. An ObjectDefinition is uniquely identified by its fully
 * qualified class name, as a String.
 */
public class DuplicateObjectDefintionException extends ConfigCompileException {
	private final boolean wasCopy;
	public DuplicateObjectDefintionException(Target t, boolean isCopy) {
		this(null, t, isCopy);
	}

	public DuplicateObjectDefintionException(String message, Target t, boolean isCopy) {
		super(message, t);
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
