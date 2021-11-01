package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.core.constructs.Target;

import java.util.EnumSet;

/**
 * An ExactConstraint is a generic constraint which is a simple type. For instance,
 * <code>class M&lt;T&gt;</code>, the constraint T is an ExactConstraint. Note that
 * ExactConstraint isn't used in use-time, it's for declare time.
 */
public class UnboundedConstraint extends Constraint {

	public UnboundedConstraint(Target t, String typename) {
		super(t, typename);
	}

	@Override
	public EnumSet<ConstraintLocation> validLocations() {
		return EnumSet.of(ConstraintLocation.DEFINITION);
	}

	@Override
	public String getConstraintName() {
		return "unbounded constraint";
	}

	@Override
	public String toString() {
		return getTypeName();
	}
}
