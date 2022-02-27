package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;

import java.util.Arrays;
import java.util.List;

public class UnqualifiedGenericDeclaration {

	private final List<UnqualifiedConstraints> constraints;

	public UnqualifiedGenericDeclaration(Target t, UnqualifiedConstraints... constraints) {
		this.constraints = Arrays.asList(constraints);
	}

	public GenericDeclaration qualify(Target t, Environment env) throws ClassNotFoundException {
		Constraints[] c = new Constraints[constraints.size()];
		for(int i = 0; i < c.length; i++) {
			c[i] = constraints.get(i).qualify(ConstraintLocation.DEFINITION, t, env);
		}
		return new GenericDeclaration(t, c);
	}
}
