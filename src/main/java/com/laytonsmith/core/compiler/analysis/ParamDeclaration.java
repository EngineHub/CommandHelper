package com.laytonsmith.core.compiler.analysis;

import com.laytonsmith.core.NodeModifiers;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;

/**
 * Represents a parameter declaration in a scope graph.
 * @author P.J.S. Kools
 */
public class ParamDeclaration extends Declaration {

	private final ParseTree defaultValue;

	/**
	 * Creates a new {@link ParamDeclaration} in the {@link Namespace#IVARIABLE} namespace.
	 * @param identifier The parameter name.
	 * @param type The parameter {@link CClassType}.
	 * @param defaultValue The default value. If not set, this should be java null.
	 * @param modifiers The node modifiers.
	 * @param t The parameter target.
	 */
	public ParamDeclaration(String identifier, CClassType type, ParseTree defaultValue, NodeModifiers modifiers, Target t) {
		super(Namespace.IVARIABLE, identifier, type, modifiers, t);
		this.defaultValue = defaultValue;
	}

	/**
	 * Returns the default value of the parameter. This may be java null if there isn't one. Otherwise, this represents
	 * the ParseTree node which was set as the value.
	 * @return
	 */
	public ParseTree getDefaultValue() {
		return defaultValue;
	}
}
