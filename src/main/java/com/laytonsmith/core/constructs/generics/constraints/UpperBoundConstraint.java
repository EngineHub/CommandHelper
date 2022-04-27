package com.laytonsmith.core.constructs.generics.constraints;

import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.ConstraintLocation;
import com.laytonsmith.core.constructs.generics.ConstraintToConstraintValidator;
import com.laytonsmith.core.constructs.generics.ConstraintValidator;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREGenericConstraintException;
import com.laytonsmith.core.objects.ObjectModifier;

import java.util.EnumSet;
import java.util.Set;

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
	 */
	public UpperBoundConstraint(Target t, String typename, LeftHandSideType upperBound) {
		super(t, typename, upperBound);
		ConstraintValidator.ValidateTypename(typename, t);
		for(Set<ObjectModifier> upperBoundComponent : upperBound.getTypeObjectModifiers()) {
			if(upperBoundComponent.contains(ObjectModifier.FINAL)) {
				throw new CREGenericConstraintException(upperBound.val() + " is marked as final, and so"
						+ " cannot be used in an upper bound constraint.", t);
			}
		}
	}

	public LeftHandSideType getUpperBound() {
		return this.bound;
	}

	@Override
	public String toSimpleString() {
		return getTypeName() + " extends " + getUpperBound().getSimpleName();
	}

	@Override
	public String toString() {
		return getTypeName() + " extends " + getUpperBound();
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
	protected boolean isConcreteClassWithinConstraint(LeftHandSideType type, Environment env) {
		return type.doesExtend(bound, env);
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
				if(lhs.getType() == null) {
					// Wildcard
					return true;
				}
				return isWithinConstraint(lhs.getType(), env);
			}

			@Override
			public Boolean isWithinBounds(LowerBoundConstraint lhs) {
				return lhs.getLowerBound().doesExtend(UpperBoundConstraint.this.getUpperBound(), env);
			}

			@Override
			public Boolean isWithinBounds(UpperBoundConstraint lhs) {
				return lhs.getUpperBound().doesExtend(UpperBoundConstraint.this.getUpperBound(), env);
			}

			@Override
			public Boolean isWithinBounds(UnboundedConstraint lhs) {
				throw new Error("Unexpected constraint combination.");
			}
		};
	}

	@Override
	public ExactType convertFromDiamond(Target t) {
		return new ExactType(t, this.bound);
	}

	@Override
	public boolean supportsTypeUnions() {
		return true;
	}
}
