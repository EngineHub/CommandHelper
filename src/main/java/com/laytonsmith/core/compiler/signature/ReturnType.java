package com.laytonsmith.core.compiler.signature;

import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.LeftHandSideType;

/**
 * Represents the return type of a function, closure or procedure.
 * @author P.J.S. Kools
 */
public class ReturnType {

	private final LeftHandSideType type;
	private final String valDesc;

	/**
	 * Creates a new {@link ReturnType} with the given type.
	 * @param type - The type that will be returned.
	 * @param valDesc - The return value description.
	 */
	public ReturnType(LeftHandSideType type, String valDesc) {
		this.type = type;
		this.valDesc = valDesc;
	}

	/**
	 * Gets the return type.
	 * @return The {@link CClassType}.
	 */
	public LeftHandSideType getType() {
		return this.type;
	}

	/**
	 * Gets the description of the return value.
	 * @return The description.
	 */
	public String getValDesc() {
		return this.valDesc;
	}
}
