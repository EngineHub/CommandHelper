package com.laytonsmith.core.compiler.signature;

import com.laytonsmith.core.constructs.CClassType;

/**
 * Represents the return type of a function, closure or procedure.
 * @author P.J.S. Kools
 */
public class ReturnType {

	private final CClassType type;

	/**
	 * Creates a new {@link ReturnType} with the given type.
	 * @param type - The type that will be returned.
	 */
	public ReturnType(CClassType type) {
		this.type = type;
	}

	/**
	 * Gets the return type.
	 * @return The {@link CClassType}.
	 */
	public CClassType getType() {
		return this.type;
	}
}
