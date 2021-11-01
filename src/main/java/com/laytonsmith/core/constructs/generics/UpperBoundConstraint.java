package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;

import java.util.EnumSet;

/**
 * An UpperBoundConstraint is defined with the extends keyword, such as <code>T extends Number</code>.
 * In this case, Number is the upper bound.
 */
public class UpperBoundConstraint extends BoundaryConstraint {

	public UpperBoundConstraint(Target t, String typename, CClassType upperBound) {
		super(t, typename, upperBound);
	}

	public CClassType getUpperBound() {
		return this.bound;
	}

	@Override
	public String toString() {
		return getTypeName() + " extends " + getUpperBound().getFQCN().getFQCN();
	}

	@Override
	public EnumSet<ConstraintLocation> validLocations() {
		return EnumSet.allOf(ConstraintLocation.class);
	}

	@Override
	public String getConstraintName() {
		return "upper bound";
	}
}
