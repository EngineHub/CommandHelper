package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.core.constructs.Auto;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CREGenericConstraintException;

/**
 * A TypeConstraint is a value that has a ClassType boundary.
 */
public abstract class BoundaryConstraint extends Constraint {
	protected CClassType bound;

	protected BoundaryConstraint(Target t, String typename, CClassType bound) {
		super(t, typename);
		if(bound == Auto.TYPE) {
			throw new CREGenericConstraintException("Cannot use auto types on " + getConstraintName()
					+ " constraints", t);
		}
		this.bound = bound;
	}

}
