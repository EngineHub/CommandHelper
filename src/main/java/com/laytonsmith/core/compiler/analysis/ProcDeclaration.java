package com.laytonsmith.core.compiler.analysis;

import com.laytonsmith.core.NodeModifiers;
import java.util.HashSet;
import java.util.Set;

import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a procedure declaration in a scope graph.
 * @author P.J.S. Kools
 */
public class ProcDeclaration extends Declaration {

	private final Set<Reference> requiredRefs = new HashSet<>();
	private final List<ParamDeclaration> parameters;


	/**
	 * Creates a new {@link ProcDeclaration} in the {@link Namespace#PROCEDURE} namespace.
	 * @param identifier - The procedure name.
	 * @param type - The procedure return type.
	 * @param parameters
	 * @param modifiers Node modifiers for this Procedure.
	 * @param t - The procedure target.
	 */
	public ProcDeclaration(String identifier, CClassType type, List<ParamDeclaration> parameters, NodeModifiers modifiers, Target t) {
		super(Namespace.PROCEDURE, identifier, type, modifiers, t);
		this.parameters = parameters;
	}

	/**
	 * Adds a required reference to this procedure declaration.Required references have to be resolved before this
 declaration may safely be used.
	 * @param ref - The required reference.
	 * @return
	 * @returns {@code true} if this set did not already contain the reference, {@code false} otherwise.
	 */
	public boolean addRequiredReference(Reference ref) {
		return this.requiredRefs.add(ref);
	}

	/**
	 * Gets the required references of this procedure declaration. Required references have to be resolved before this
	 * declaration may safely be used.
	 * @return The {@link Set} containing all {@link Reference}s.
	 */
	public Set<Reference> getRequiredRefs() {
		return this.requiredRefs;
	}

	/**
	 * Return the parameters for this proc.
	 * @return
	 */
	public List<ParamDeclaration> getParameters() {
		return  new ArrayList<>(this.parameters);
	}
}
