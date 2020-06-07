package com.laytonsmith.core.compiler.analysis;

import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;

/**
 * Represents a declaration in a scope graph.
 * @author P.J.S. Kools
 */
public class Declaration {

//	public static final Declaration ARGUMENTS_VAR =
//			new Declaration(Namespace.IVARIABLE, "@arguments", CArray.TYPE, Target.UNKNOWN);

	private final Namespace namespace;
	private final String identifier;
	private final CClassType type;
	private final Target target;

	/**
	 * Creates a new {@link Declaration}.
	 * @param namespace - The {@link Namespace} of the declaration.
	 * @param identifier - The idenfifier (e.g. variable or procedure name).
	 * @param type - The {@link CClassType} of this declaration.
	 * @param t - The target of the declaration.
	 */
	public Declaration(Namespace namespace, String identifier, CClassType type, Target t) {
		this.namespace = namespace;
		this.identifier = identifier;
		this.type = type;
		this.target = t;
	}

	public Namespace getNamespace() {
		return this.namespace;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	public CClassType getType() {
		return this.type;
	}

	public Target getTarget() {
		return this.target;
	}
}
