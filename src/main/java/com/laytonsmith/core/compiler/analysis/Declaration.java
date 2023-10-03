package com.laytonsmith.core.compiler.analysis;

import com.laytonsmith.core.NodeModifiers;
import com.laytonsmith.core.constructs.LeftHandSideType;
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
	private final LeftHandSideType type;
	private final Target target;
	private final NodeModifiers modifiers;

	/**
	 * Creates a new {@link Declaration}.
	 * @param namespace - The {@link Namespace} of the declaration.
	 * @param identifier - The idenfifier (e.g. variable or procedure name).
	 * @param type - The {@link LeftHandSideType} of this declaration.
	 * @param modifiers The element modifiers.
	 * @param t - The target of the declaration.
	 */
	public Declaration(Namespace namespace, String identifier, LeftHandSideType type, NodeModifiers modifiers, Target t) {
		this.namespace = namespace;
		this.identifier = identifier;
		this.type = type;
		this.modifiers = modifiers;
		this.target = t;
	}

	public Namespace getNamespace() {
		return this.namespace;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	public LeftHandSideType getType() {
		return this.type;
	}

	public Target getTarget() {
		return this.target;
	}

	public NodeModifiers getNodeModifiers() {
		return this.modifiers;
	}
}
