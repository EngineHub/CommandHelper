package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;

import java.util.Arrays;
import java.util.List;

public class UnqualifiedConstraints {

	private final List<UnqualifiedConstraint> unorderedConstraints;

	/**
	 * Constructs a new constraint object. Note that if this is being used on the LHS, no validation is done
	 * @param constraints The constraints. This is an unordered list, but they will be normalized into their
	 *                    natural order.
	 */
	public UnqualifiedConstraints(Target t, ConstraintLocation location, UnqualifiedConstraint... constraints) {
		this.unorderedConstraints = Arrays.asList(constraints);
	}

	public Constraints qualify(ConstraintLocation location, Target t, Environment env) throws ClassNotFoundException {
		Constraint[] constraints = new Constraint[unorderedConstraints.size()];
		for(int i = 0; i < constraints.length; i++) {
			constraints[i] = unorderedConstraints.get(i).qualify(env);
		}
		return new Constraints(t, location, constraints);
	}

}
