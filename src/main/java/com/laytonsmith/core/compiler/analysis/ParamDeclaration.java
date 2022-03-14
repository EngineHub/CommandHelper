package com.laytonsmith.core.compiler.analysis;

import com.laytonsmith.core.NodeModifiers;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;

/**
 * Represents a parameter declaration in a scope graph.
 * @author P.J.S. Kools
 */
public class ParamDeclaration extends Declaration {

	/**
	 * Creates a new {@link ParamDeclaration} in the {@link Namespace#IVARIABLE} namespace.
	 * @param identifier The parameter name.
	 * @param type The parameter {@link CClassType}.
	 * @param modifiers The node modifiers.
	 * @param t The parameter target.
	 */
	public ParamDeclaration(String identifier, CClassType type, NodeModifiers modifiers, Target t) {
		super(Namespace.IVARIABLE, identifier, type, modifiers, t);
	}
}
