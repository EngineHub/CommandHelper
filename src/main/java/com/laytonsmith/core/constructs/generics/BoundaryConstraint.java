package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.core.constructs.Auto;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREGenericConstraintException;

/**
 * A TypeConstraint is a value that has a ClassType boundary.
 */
public abstract class BoundaryConstraint extends Constraint {
	protected CClassType bound;
	protected LeftHandGenericUse genericParameters;

	protected BoundaryConstraint(Target t, String typename, CClassType bound, LeftHandGenericUse genericParameters) {
		super(t, typename);
		if(bound == Auto.TYPE) {
			throw new CREGenericConstraintException("Cannot use auto types on " + getConstraintName()
					+ " constraints", t);
		}
		this.bound = bound;
		this.genericParameters = genericParameters;
	}

	/**
	 * {@inheritDoc}
	 * @param type The concrete type to check
	 * @param generics Any LHS generics that were defined.
	 * @return
	 */
	@Override
	public boolean isWithinConstraint(CClassType type, LeftHandGenericUse generics, Environment env) {
		return isConcreteClassWithinConstraint(type, generics, env);
	}

	public LeftHandGenericUse getBoundaryGenerics() {
		return genericParameters;
	}

	protected abstract boolean isConcreteClassWithinConstraint(CClassType type, LeftHandGenericUse generics, Environment env);

}
