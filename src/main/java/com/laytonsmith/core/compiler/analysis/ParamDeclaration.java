package com.laytonsmith.core.compiler.analysis;

import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;

/**
 * Represents a parameter declaration in a scope graph.
 * @author P.J.S. Kools
 */
public class ParamDeclaration extends Declaration {

	/**
	 * Creates a new {@link ParamDeclaration} in the {@link Namespace#IVARIABLE} namespace.
	 * @param identifier - The parameter name.
	 * @param type - The parameter {@link CClassType}.
	 * @param t - The parameter target.
	 */
	public ParamDeclaration(String identifier, CClassType type, Target t) {
		super(Namespace.IVARIABLE, identifier, type, t);
	}
}
