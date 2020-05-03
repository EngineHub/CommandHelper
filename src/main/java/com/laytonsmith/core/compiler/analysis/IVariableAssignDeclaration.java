package com.laytonsmith.core.compiler.analysis;

import com.laytonsmith.core.constructs.Target;

/**
 * Represents a reference to an ivariable assign in a scope graph.
 * @author P.J.S. Kools
 */
public class IVariableAssignDeclaration extends Declaration {

	/**
	 * Creates a new {@link IVariableAssignDeclaration} in the {@link Namespace#IVARIABLE_ASSIGN} scope.
	 * @param identifier - The variable name.
	 * @param t - The target of the declaration.
	 */
	public IVariableAssignDeclaration(String identifier, Target t) {
		super(Namespace.IVARIABLE_ASSIGN, identifier, null, t);
	}
}
