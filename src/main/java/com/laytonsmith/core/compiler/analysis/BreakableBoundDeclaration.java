package com.laytonsmith.core.compiler.analysis;

import com.laytonsmith.core.NodeModifiers;
import com.laytonsmith.core.constructs.Target;

/**
 * Represents a breakable (for(), while(), switch(), etc) bound declaration in a scope graph.
 * This indicates a boundary where lookup for a breakable should stop.
 * @author P.J.S. Kools
 */
public class BreakableBoundDeclaration extends Declaration {

	/**
	 * Creates a new {@link BreakableBoundDeclaration} in the {@link Namespace#BREAKABLE} namespace.
	 * @param modifiers The node modifiers.
	 * @param t The breakable target.
	 */
	public BreakableBoundDeclaration(NodeModifiers modifiers, Target t) {
		super(Namespace.BREAKABLE, null, null, modifiers, t);
	}
}
