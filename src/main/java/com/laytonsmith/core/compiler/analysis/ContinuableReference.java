package com.laytonsmith.core.compiler.analysis;

import com.laytonsmith.core.constructs.Target;

/**
 * Represents a reference to a continuable (by the continue()) function in a scope graph.
 * @author P.J.S. Kools
 */
public class ContinuableReference extends Reference {

	/**
	 * Creates a new {@link ContinuableReference} in the {@link Namespace#CONTINUABLE} scope.
	 * @param t - The target of the reference.
	 */
	public ContinuableReference(Target t) {
		super(Namespace.CONTINUABLE, null, t);
	}
}
