package com.laytonsmith.core.compiler.signature;

import com.laytonsmith.core.constructs.CClassType;

/**
 * Represents a parameter for a function, closure or procedure.
 * @author P.J.S. Kools
 */
public class Param {

	private final CClassType type;
	private final String name;
	private final String genericIdentifier;
	private final boolean isVarParam;

	/**
	 * Creates a new {@link Param} with the given properties.
	 * If geneticIdentifier is non-null, then the parameter type is 'geneticIdentifier extends type'.
	 * @param genericIdentifier - The generic identifier, which can be used to link generic types.
	 * @param type - The (parent) type of the parameter.
	 * @param name - The name of the parameter.
	 * @param isVarParam - {@code true} if the parameter is a varparam, meaning that it matches zero or more arguments
	 * of the given type, or one argument of type {@code array<type>}. {@code false} otherwise.
	 * Note that a varparam is only usable as type {@code array<type>}.
	 */
	public Param(String genericIdentifier, CClassType type, String name, boolean isVarParam) {
		this.genericIdentifier = genericIdentifier;
		this.type = type;
		this.name = name;
		this.isVarParam = isVarParam;
	}

	/**
	 * Creates a new non-generic non-varparam {@link Param} with the given properties.
	 * @param type - The type of the parameter.
	 * @param name - The name of the parameter.
	 */
	public Param(CClassType type, String name) {
		this(null, type, name, false);
	}

	/**
	 * Gets the parameter {@link CClassType}.
	 * If this {@link Param} is generic, then the type in 'genericIdentifier extends type' is returned.
	 * If this {@link Param} is a varparam, then the type in 'type name...' is returned (and not {@code array<type>}).
	 * @return The type.
	 */
	public CClassType getType() {
		return this.type;
	}

	/**
	 * Gets the name of the parameter.
	 * @return The name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the generic identifier.
	 * @return The generic identifier in 'genericIdentifier extends type',
	 * or {@code null} if the parameter type is not generic.
	 */
	public String getGenericIdentifier() {
		return this.genericIdentifier;
	}

	/**
	 * Gets whether the parameter is a varparam or not. Varparams accept 0 or more arguments of their type,
	 * or one argument of type {@code array<type>} and they become usable as {@code array<type>}.
	 * @return {@code true} if this parameter is a varparam, {@code false} otherwise.
	 */
	public boolean isVarParam() {
		return this.isVarParam;
	}
}
