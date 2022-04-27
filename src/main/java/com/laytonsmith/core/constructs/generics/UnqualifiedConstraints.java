package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.core.constructs.generics.constraints.Constraint;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;

import java.util.Arrays;
import java.util.List;

/**
 * The UnqualifiedConstraints class, like the other Unqualified classes, is a simple wrapper around the unqualified
 * class names and other relevant information which is eventually qualified in a second pass, once the relevant types
 * have all had a chance to be defined.
 */
public class UnqualifiedConstraints {

	private final List<UnqualifiedConstraint> unorderedConstraints;
	private final Target definitionTarget;
	/**
	 * Constructs a new constraint object.Note that if this is being used on the LHS, no validation is done
	 *
	 * @param t The code target.
	 * @param constraints The constraints. This is an unordered list, but they will be normalized into their natural
	 * order.
	 */
	public UnqualifiedConstraints(Target t, UnqualifiedConstraint... constraints) {
		this.unorderedConstraints = Arrays.asList(constraints);
		this.definitionTarget = t;
	}

	/**
	 * Qualifies the value and returns a Constraints object. This has the potential to throw Exceptions.
	 * @param location The location that this Constraints object is being used.
	 * @param declarationConstraints The Constraints object which governs this Constraint. This should be
	 * null if location is {@link ConstraintLocation#DEFINITION}.
	 * @param env The environment.
	 * @return A new Constraints object, which works with full types.
	 */
	public Constraints qualify(ConstraintLocation location, Constraints declarationConstraints,
			Environment env) {
		Constraint[] constraints = new Constraint[unorderedConstraints.size()];
		for(int i = 0; i < constraints.length; i++) {
			constraints[i] = unorderedConstraints.get(i).qualify(env, declarationConstraints);
		}
		return new Constraints(definitionTarget, location, constraints);
	}

}
