package com.laytonsmith.core.compiler.signature;

import com.laytonsmith.core.constructs.CClassType;

/**
 * Represents the return type of a function, closure or procedure.
 * @author P.J.S. Kools
 */
public class ReturnType {

	private final CClassType type;
	private final String genericIdentifier;

	/**
	 * Creates a new {@link ReturnType} with the given properties.
	 * If geneticIdentifier is non-null, then the return type is 'geneticIdentifier extends type'.
	 * @param genericIdentifier - The generic identifier, which can be used to link generic types.
	 * @param type - The (parent) type that will be returned.
	 */
	public ReturnType(String genericIdentifier, CClassType type) {
		this.genericIdentifier = genericIdentifier;
		this.type = type;
	}

	/**
	 * Creates a new {@link ReturnType} with the given type.
	 * @param type - The type that will be returned.
	 */
	public ReturnType(CClassType type) {
		this(null, type);
	}

	/**
	 * Gets the {@link CClassType} return type.
	 * If this {@link ReturnType} is generic, then the type in 'genericIdentifier extends type' is returned.
	 * @return The type.
	 */
	public CClassType getType() {
		return this.type;
	}

	/**
	 * Gets the generic identifier.
	 * @return The generic identifier in 'genericIdentifier extends type',
	 * or {@code null} if the return type is not generic.
	 */
	public String getGenericIdentifier() {
		return this.genericIdentifier;
	}
}
