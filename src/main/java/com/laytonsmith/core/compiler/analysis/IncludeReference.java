package com.laytonsmith.core.compiler.analysis;

import com.laytonsmith.core.constructs.Target;

/**
 * Represents a reference to an include in a scope graph.
 * @author P.J.S. Kools
 */
public class IncludeReference extends Reference {

	private final Scope inScope;
	private final Scope outScope;

	/**
	 * Creates a new {@link IncludeReference} in the {@link Namespace#INCLUDE} scope.
	 * Stores the scopes to which the include should be linked.
	 * This should be in order: inScope <- includeScopes <- outScope (arrows pointing from child to parent scopes).
	 * @param identifier - The include identifier (the path passed to include(), which should resolve to a file).
	 * @param inScope - The parent scope which the include should be linked to.
	 * @param outScope - The scope in which the include should be usable.
	 * @param t - The target of the reference.
	 */
	public IncludeReference(String identifier, Scope inScope, Scope outScope, Target t) {
		super(Namespace.INCLUDE, identifier, t);
		this.inScope = inScope;
		this.outScope = outScope;
	}

	public Scope getInScope() {
		return this.inScope;
	}

	public Scope getOutScope() {
		return this.outScope;
	}
}
