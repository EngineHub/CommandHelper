package com.laytonsmith.core.compiler.analysis;

import com.laytonsmith.core.NodeModifiers;
import com.laytonsmith.core.constructs.Target;

/**
 * Represents a continuable (for(), while(), etc) bound declaration in a scope graph.
 * This indicates a boundary where lookup for a continuable should stop.
 * @author P.J.S. Kools
 */
public class ContinuableBoundDeclaration extends Declaration {

	/**
	 * Creates a new {@link ContinuableBoundDeclaration} in the {@link Namespace#CONTINUABLE} namespace.
	 * @param modifiers The node modifiers.
	 * @param t The continuable target.
	 */
	public ContinuableBoundDeclaration(NodeModifiers modifiers, Target t) {
		super(Namespace.CONTINUABLE, null, null, modifiers, t);
	}
}
