package com.laytonsmith.core.constructs.generics.constraints;

import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.ConstraintLocation;
import com.laytonsmith.core.constructs.generics.ConstraintToConstraintValidator;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREGenericConstraintException;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.EnumSet;

/**
 * A LowerBoundConstraint is defined with the super keyword, such as <code>T super number</code>.
 * In this case, number is the lower bound.
 */
public class LowerBoundConstraint extends BoundaryConstraint {

	/**
	 * Constructs a new upper bound constraint.
	 * @param t Code target
	 * @param typename The name of this parameter, may be ? for LHS constraints
	 * @param lowerBound The concrete lower bound
	 */
	public LowerBoundConstraint(Target t, String typename, LeftHandSideType lowerBound) {
		super(t, typename, lowerBound);
		if(lowerBound.equals(Mixed.TYPE.asLeftHandSideType())) {
			throw new CREGenericConstraintException("Cannot create a lower bound on mixed", t);
		}
	}

	@Override
	protected boolean isConcreteClassWithinConstraint(LeftHandSideType type, Environment env) {
		return this.bound.doesExtend(type, env);
	}

	public LeftHandSideType getLowerBound() {
		return this.bound;
	}

	@Override
	public String toSimpleString() {
		return getTypeName() + " super " + getLowerBound().getSimpleName();
	}

	@Override
	public String toString() {
		return getTypeName() + " super " + getLowerBound();
	}

	@Override
	public EnumSet<ConstraintLocation> validLocations() {
		return EnumSet.of(ConstraintLocation.LHS);
	}

	@Override
	public String getConstraintName() {
		return "lower bound";
	}

	@Override
	protected ConstraintToConstraintValidator getConstraintToConstraintValidator(Environment env) {
		return new ConstraintToConstraintValidator() {
			@Override
			public Boolean isWithinBounds(ConstructorConstraint lhs) {
				return null;
			}

			@Override
			public Boolean isWithinBounds(ExactTypeConstraint lhs) {
				return isWithinConstraint(lhs.getType(), env);
			}

			@Override
			public Boolean isWithinBounds(LowerBoundConstraint lhs) {
				return isWithinConstraint(lhs.getLowerBound(), env);
			}

			@Override
			public Boolean isWithinBounds(UpperBoundConstraint lhs) {
				return false;
			}

			@Override
			public Boolean isWithinBounds(UnboundedConstraint lhs) {
				throw new Error("Unexpected constraint combination.");
			}

			@Override
			public Boolean isWithinBounds(VariadicTypeConstraint lhs) {
				throw new Error("Unexpected constraint combination.");
			}
		};
	}

	@Override
	public ExactTypeConstraint convertFromDiamond(Target t) {
		return new ExactTypeConstraint(t, this.bound);
	}

	@Override
	public boolean supportsTypeUnions() {
		return true;
	}

}
