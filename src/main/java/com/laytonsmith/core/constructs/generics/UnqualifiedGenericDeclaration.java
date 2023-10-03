package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;

import java.util.Arrays;
import java.util.List;

/**
 * The UnqualifiedGenericDeclaration class, like the other Unqualified classes, is a simple wrapper around the
 * unqualified class names and other relevant information which is eventually qualified in a second pass, once the
 * relevant types have all had a chance to be defined.
 */
public class UnqualifiedGenericDeclaration {

	private final List<UnqualifiedConstraints> constraints;
	private final Target target;

	/**
	 * Constructs a new UnqualifiedGenericDeclaration.
	 * @param t The code target where this is being defined.
	 * @param constraints
	 */
	public UnqualifiedGenericDeclaration(Target t, UnqualifiedConstraints... constraints) {
		this.constraints = Arrays.asList(constraints);
		this.target = t;
	}

	public GenericDeclaration qualify(Environment env) throws ClassNotFoundException {
		Constraints[] c = new Constraints[constraints.size()];
		for(int i = 0; i < c.length; i++) {
			c[i] = constraints.get(i).qualify(ConstraintLocation.DEFINITION, null, env);
		}
		return new GenericDeclaration(target, c);
	}
}
