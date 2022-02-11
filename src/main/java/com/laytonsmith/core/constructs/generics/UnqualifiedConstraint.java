package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;

public class UnqualifiedConstraint {
	private final String constraint;
	private final ConstraintLocation location;
	private final Target constraintTarget;

	public UnqualifiedConstraint(String constraint, ConstraintLocation location, Target constraintTarget) {
		this.constraint = constraint;
		this.location = location;
		this.constraintTarget = constraintTarget;
	}

	public Constraint qualify(Environment env) throws ClassNotFoundException {
		return Constraints.GetConstraint(constraint, constraintTarget, location, env);
	}
}
