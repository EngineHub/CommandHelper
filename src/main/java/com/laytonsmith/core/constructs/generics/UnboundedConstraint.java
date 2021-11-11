package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREGenericConstraintException;
import com.laytonsmith.core.natives.interfaces.Mixed;

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
	public boolean isWithinConstraint(CClassType type, LeftHandGenericUse generics, Environment env) {
		return true;
	}

	@Override
	protected ConstraintToConstraintValidator getConstraintToConstraintValidator(Environment env) {
		return new ConstraintToConstraintValidator() {
			@Override
			public Boolean isWithinBounds(ConstructorConstraint lhs) {
				return true;
			}

			@Override
			public Boolean isWithinBounds(ExactType lhs) {
				return true;
			}

			@Override
			public Boolean isWithinBounds(LowerBoundConstraint lhs) {
				return true;
			}

			@Override
			public Boolean isWithinBounds(UpperBoundConstraint lhs) {
				return true;
			}

			@Override
			public Boolean isWithinBounds(UnboundedConstraint lhs) {
				return true;
			}
		};
	}

	@Override
	public ExactType convertFromDiamond(Target t) throws CREGenericConstraintException {
		return new ExactType(t, Mixed.TYPE, null);
	}

	@Override
	public String toString() {
		return getTypeName();
	}
}
