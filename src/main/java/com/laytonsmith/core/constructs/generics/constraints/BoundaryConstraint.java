package com.laytonsmith.core.constructs.generics.constraints;

import com.laytonsmith.core.constructs.Auto;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.ConstraintValidator;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREGenericConstraintException;

/**
 * A TypeConstraint is a value that has a ClassType boundary.
 */
public abstract class BoundaryConstraint extends Constraint {
	/**
	 * The bound (either upper or lower). Will never be auto.
	 */
	protected LeftHandSideType bound;

	protected BoundaryConstraint(Target t, String typename, LeftHandSideType bound) {
		super(t, typename);
		ConstraintValidator.ValidateTypename(typename, t);
		if(Auto.LHSTYPE.equals(bound)) {
			throw new CREGenericConstraintException("Cannot use auto types on " + getConstraintName()
					+ " constraints", t);
		}
		this.bound = bound;
	}

	/**
	 * {@inheritDoc}
	 * @param type The concrete type to check
	 * @return
	 */
	@Override
	public boolean isWithinConstraint(LeftHandSideType type, Environment env) {
		return isConcreteClassWithinConstraint(type, env);
	}

	protected abstract boolean isConcreteClassWithinConstraint(LeftHandSideType type, Environment env);

	@Override
	public ExactTypeConstraint convertFromNull(Target t) throws CREGenericConstraintException {
		return new ExactTypeConstraint(t, Auto.LHSTYPE);
	}

}
