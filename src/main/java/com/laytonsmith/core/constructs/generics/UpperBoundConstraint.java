package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;

import java.util.EnumSet;

/**
 * An UpperBoundConstraint is defined with the extends keyword, such as <code>T extends Number</code>.
 * In this case, Number is the upper bound.
 */
public class UpperBoundConstraint extends BoundaryConstraint {

	/**
	 * Constructs a new upper bound constraint.
	 * @param t Code target
	 * @param typename The name of this parameter, may be ? for LHS constraints
	 * @param upperBound The concrete upper bound
	 * @param genericParameters LHS generics that the upper bound type may provide. May be null if none were provided.
	 */
	public UpperBoundConstraint(Target t, String typename, CClassType upperBound, LeftHandGenericUse genericParameters) {
		super(t, typename, upperBound, genericParameters);
	}

	public CClassType getUpperBound() {
		return this.bound;
	}

	@Override
	public String toString() {
		return getTypeName() + " extends " + getUpperBound()
				+ (genericParameters == null ? "" : "<" + genericParameters + ">");
	}

	@Override
	public EnumSet<ConstraintLocation> validLocations() {
		return EnumSet.allOf(ConstraintLocation.class);
	}

	@Override
	public String getConstraintName() {
		return "upper bound";
	}

	@Override
	protected boolean isConcreteClassWithinConstraint(CClassType type, Environment env) {
		return type.isInstanceOf(bound, genericParameters, env);
	}

	@Override
	protected ConstraintToConstraintValidator getConstraintToConstraintValidator(Environment env) {
		return new ConstraintToConstraintValidator() {
			@Override
			public Boolean isWithinBounds(ConstructorConstraint lhs) {
				return null;
			}

			@Override
			public Boolean isWithinBounds(ExactType lhs) {
				return isWithinConstraint(lhs.getType(), lhs.getGenericParameters());
			}

			@Override
			public Boolean isWithinBounds(LowerBoundConstraint lhs) {
				return lhs.getLowerBound().isInstanceOf(UpperBoundConstraint.this.getUpperBound(), genericParameters, env);
			}

			@Override
			public Boolean isWithinBounds(UpperBoundConstraint lhs) {
				return lhs.getUpperBound().isInstanceOf(UpperBoundConstraint.this.getUpperBound(), genericParameters, env);
			}

			@Override
			public Boolean isWithinBounds(UnboundedConstraint lhs) {
				return true;
			}
		};
	}
}
