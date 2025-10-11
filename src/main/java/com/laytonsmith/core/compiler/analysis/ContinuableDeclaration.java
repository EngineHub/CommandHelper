package com.laytonsmith.core.compiler.analysis;

import com.laytonsmith.core.NodeModifiers;
import com.laytonsmith.core.constructs.Target;

/**
 * Represents a continuable (for(), while(), etc) declaration in a scope graph.
 * @author P.J.S. Kools
 */
public class ContinuableDeclaration extends Declaration {

	/**
	 * Creates a new {@link ContinuableDeclaration} in the {@link Namespace#CONTINUABLE} namespace.
	 * @param modifiers The node modifiers.
	 * @param t The continuable target.
	 */
	public ContinuableDeclaration(NodeModifiers modifiers, Target t) {
		super(Namespace.CONTINUABLE, null, null, modifiers, t);
	}
}
