package com.laytonsmith.core.compiler.analysis;

import com.laytonsmith.core.constructs.Target;

/**
 * Represents a reference to a returnable in a scope graph.
 * @author P.J.S. Kools
 */
public class ReturnableReference extends Reference {

	/**
	 * Creates a new {@link ReturnableReference} in the {@link Namespace#RETURNABLE} scope.
	 * @param t - The target of the reference.
	 */
	public ReturnableReference(Target t) {
		super(Namespace.RETURNABLE, null, t);
	}
}
