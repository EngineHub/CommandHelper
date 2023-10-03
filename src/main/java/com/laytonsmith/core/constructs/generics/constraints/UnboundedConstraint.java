package com.laytonsmith.core.constructs.generics.constraints;

import com.laytonsmith.core.constructs.Auto;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.ConstraintLocation;
import com.laytonsmith.core.constructs.generics.ConstraintToConstraintValidator;
import com.laytonsmith.core.constructs.generics.ConstraintValidator;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREGenericConstraintException;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.EnumSet;

/**
 * An UnboundedConstraint is a generic constraint which is a simple and single type. For instance,
 * <code>class M&lt;T&gt;</code>, the constraint T is an UnboundedConstraint. Note that
 * UnboundedConstraint isn't used in use-time, it's for declare time.
 */
public class UnboundedConstraint extends Constraint {

	public UnboundedConstraint(Target t, String typename) {
		super(t, typename);
		ConstraintValidator.ValidateTypename(typename, t);
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
	public boolean isWithinConstraint(LeftHandSideType type, Environment env) {
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
			public Boolean isWithinBounds(ExactTypeConstraint lhs) {
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

			@Override
			public Boolean isWithinBounds(VariadicTypeConstraint lhs) {
				throw new Error("Unexpected constraint combination.");
			}
		};
	}

	@Override
	public ExactTypeConstraint convertFromDiamond(Target t) throws CREGenericConstraintException {
		return new ExactTypeConstraint(t, Mixed.TYPE.asLeftHandSideType());
	}

	@Override
	public String toSimpleString() {
		return toString();
	}

	@Override
	public String toString() {
		return getTypeName();
	}

	@Override
	public boolean supportsTypeUnions() {
		return true;
	}

	@Override
	public ExactTypeConstraint convertFromNull(Target t) throws CREGenericConstraintException {
		return new ExactTypeConstraint(t, Auto.LHSTYPE);
	}
}
