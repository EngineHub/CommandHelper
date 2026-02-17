package com.laytonsmith.core.compiler.analysis;

import com.laytonsmith.core.constructs.Target;

/**
 * Represents a reference to a breakable (by the break() function) in a scope graph.
 * @author P.J.S. Kools
 */
public class BreakableReference extends Reference {

	/**
	 * Creates a new {@link BreakableReference} in the {@link Namespace#BREAKABLE} scope.
	 * @param t - The target of the reference.
	 */
	public BreakableReference(Target t) {
		super(Namespace.BREAKABLE, null, t);
	}
}
