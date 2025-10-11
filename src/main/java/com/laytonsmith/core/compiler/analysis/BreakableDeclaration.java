package com.laytonsmith.core.compiler.analysis;

import com.laytonsmith.core.NodeModifiers;
import com.laytonsmith.core.constructs.Target;

/**
 * Represents a breakable (for(), while(), switch(), etc) declaration in a scope graph.
 * @author P.J.S. Kools
 */
public class BreakableDeclaration extends Declaration {

	private final Scope parentScope;

	/**
	 * Creates a new {@link BreakableDeclaration} in the {@link Namespace#BREAKABLE} namespace.
	 * @param parentScope The parent scope of the breakable (that does not include this declaration).
	 * @param modifiers The node modifiers.
	 * @param t The breakable target.
	 */
	public BreakableDeclaration(Scope parentScope, NodeModifiers modifiers, Target t) {
		super(Namespace.BREAKABLE, null, null, modifiers, t);
		this.parentScope = parentScope;
	}

	public Scope getParentScope() {
		return this.parentScope;
	}
}
