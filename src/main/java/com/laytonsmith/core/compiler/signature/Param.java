package com.laytonsmith.core.compiler.signature;

import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.LeftHandSideType;

/**
 * Represents a parameter for a function, closure or procedure.
 * @author P.J.S. Kools
 */
public class Param {

	private final LeftHandSideType type;
	private final String name;
	private final String desc;
	private final boolean isVarParam;
	private final boolean isOptional;

	/**
	 * Creates a new {@link Param} with the given properties.
	 * Parameters cannot be variadic and optional at the same time, as varparams already imply optionality.
	 * @param type - The (parent) type of the parameter.
	 * @param name - The name of the parameter.
	 * @param desc - The description of the parameter.
	 * @param isVarParam - {@code true} if the parameter is a varparam, meaning that it matches zero or more arguments
	 * of the given type, or one argument of type {@code array<type>}. {@code false} otherwise.
	 * Note that a varparam is only usable as type {@code array<type>}.
	 * @param isOptional - {@code true} if the parameter is optional, {@code false} otherwise.
	 */
	public Param(LeftHandSideType type, String name, String desc, boolean isVarParam, boolean isOptional) {
		assert !isVarParam || !isOptional : "A parameter cannot be variadic and optional at the same time.";
		this.type = type;
		this.name = name;
		this.desc = desc;
		this.isVarParam = isVarParam;
		this.isOptional = isOptional;
	}

	/**
	 * Creates a new non-varparam {@link Param} with the given properties.
	 * @param type - The type of the parameter.
	 * @param name - The name of the parameter.
	 * @param desc - The description of the parameter.
	 */
	public Param(LeftHandSideType type, String name, String desc) {
		this(type, name, desc, false, false);
	}

	/**
	 * Gets the parameter {@link CClassType}.
	 * If this {@link Param} is a varparam, then the type in 'type name...' is returned (and not {@code array<type>}).
	 * @return The type.
	 */
	public LeftHandSideType getType() {
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
	 * Gets the description of the parameter.
	 * @return The description.
	 */
	public String getDesc() {
		return this.desc;
	}

	/**
	 * Gets whether the parameter is a varparam or not. Varparams accept 0 or more arguments of their type,
	 * or one argument of type {@code array<type>} and they become usable as {@code array<type>}.
	 * @return {@code true} if this parameter is a varparam, {@code false} otherwise.
	 */
	public boolean isVarParam() {
		return this.isVarParam;
	}

	/**
	 * Gets whether the parameter is optional or not.
	 * @return {@code true} if this parameter is optional, {@code false} otherwise.
	 */
	public boolean isOptional() {
		return this.isOptional;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		if(isOptional) {
			b.append("[");
		}
		b.append(getType() == null ? "any" : getType().val())
				.append(" @").append(getName());
		if(isOptional) {
			b.append("]");
		}
		return b.toString();
	}
}
