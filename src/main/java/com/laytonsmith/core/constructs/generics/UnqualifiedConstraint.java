package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.constructs.generics.constraints.Constraint;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;

/**
 * The UnqualifiedConstraint class, like the other Unqualified classes, is a simple wrapper around the unqualified class
 * names and other relevant information which is eventually qualified in a second pass, once the relevant types have all
 * had a chance to be defined.
 */
public class UnqualifiedConstraint {

	private final String constraint;
	private final ConstraintLocation location;
	private final Target constraintTarget;
	private final FileOptions fileOptions;

	/**
	 * Constructs a new UnqualifiedConstraint.
	 * @param fileOptions Used to determine the suppression instructions for warnings.
	 * @param constraint The constraint itself.
	 * @param location The location of the definition.
	 * @param constraintTarget The code target where the constraint is being defined.
	 */
	public UnqualifiedConstraint(FileOptions fileOptions, String constraint, ConstraintLocation location,
			Target constraintTarget) {
		this.constraint = constraint;
		this.location = location;
		this.constraintTarget = constraintTarget;
		this.fileOptions = fileOptions;
	}

	/**
	 * Qualifies the value and returns a Constraint object. This has the potential to throw Exceptions.
	 * @param env The environment.
	 * @param declarationConstraints The Constraints object which governs this Constraint. This should be
	 * null if location is {@link ConstraintLocation#DEFINITION}.
	 * @return A new Constraint object, which works with full types.
	 */
	public Constraint qualify(Environment env, Constraints declarationConstraints) {
		return Constraints.GetConstraint(fileOptions, constraint, constraintTarget, location, declarationConstraints, env);
	}
}
