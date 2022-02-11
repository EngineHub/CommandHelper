package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;

import java.util.Arrays;
import java.util.List;

public class UnqualifiedLeftHandGenericUse {
	private final List<UnqualifiedConstraints> constraints;
	private final Target target;

	public UnqualifiedLeftHandGenericUse(Target t, UnqualifiedConstraints... constraints) {
		this.target = t;
		this.constraints = Arrays.asList(constraints);
	}

	public LeftHandGenericUse qualify(CClassType forType, Environment env) throws ClassNotFoundException {
		Constraints[] c = new Constraints[constraints.size()];
		for(int i = 0; i < c.length; i++) {
			c[i] = constraints.get(i).qualify(ConstraintLocation.LHS, target, env);
		}
		return new LeftHandGenericUse(forType, target, env, c);
	}
}
