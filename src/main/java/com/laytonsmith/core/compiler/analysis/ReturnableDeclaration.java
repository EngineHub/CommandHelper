package com.laytonsmith.core.compiler.analysis;

import com.laytonsmith.core.NodeModifiers;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;

/**
 * Represents a returnable declaration in a scope graph.
 * @author P.J.S. Kools
 */
public class ReturnableDeclaration extends Declaration {

	/**
	 * Creates a new {@link ReturnableDeclaration} in the {@link Namespace#RETURNABLE} namespace.
	 * @param type The expected return {@link LeftHandSideType}.
	 * @param modifiers The node modifiers.
	 * @param t The returnable target.
	 */
	public ReturnableDeclaration(LeftHandSideType type, NodeModifiers modifiers, Target t) {
		super(Namespace.RETURNABLE, null, type, modifiers, t);
	}
}
