package com.laytonsmith.core.compiler.analysis;

import com.laytonsmith.core.constructs.Target;

/**
 * Represents a reference in a scope graph.
 * @author P.J.S. Kools
 */
public class Reference {

	private final Namespace namespace;
	private final String identifier;
	private final Target target;

	/**
	 * Creates a new {@link Reference}.
	 * @param namespace - The {@link Namespace} of the reference.
	 * @param identifier - The idenfifier (e.g. variable or procedure name).
	 * @param t - The target of the declaration.
	 */
	public Reference(Namespace namespace, String identifier, Target t) {
		this.namespace = namespace;
		this.identifier = identifier;
		this.target = t;
	}

	public Namespace getNamespace() {
		return this.namespace;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	public Target getTarget() {
		return this.target;
	}
}
