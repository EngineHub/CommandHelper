package com.laytonsmith.core.compiler.analysis;

import com.laytonsmith.core.constructs.Target;

/**
 * Represents an ivariable assign declaration in a scope graph. This can either be a variable declaration or an
 * ivariable reference (assign).
 * @author P.J.S. Kools
 */
public class IVariableAssignDeclaration extends Declaration {

	/**
	 * Creates a new {@link IVariableAssignDeclaration} in the {@link Namespace#IVARIABLE_ASSIGN} namespace.
	 * @param identifier - The variable name.
	 * @param t - The target of the declaration.
	 */
	public IVariableAssignDeclaration(String identifier, Target t) {
		super(Namespace.IVARIABLE_ASSIGN, identifier, null, t);
	}
}
