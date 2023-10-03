package com.laytonsmith.core.constructs.generics.constraints;

import com.laytonsmith.core.constructs.Auto;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.ConstraintLocation;
import com.laytonsmith.core.constructs.generics.ConstraintToConstraintValidator;
import com.laytonsmith.core.constructs.generics.ConstraintValidator;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREGenericConstraintException;
import java.util.EnumSet;
import java.util.Set;

/**
 *
 */
public class VariadicTypeConstraint extends Constraint {

	public VariadicTypeConstraint(Target t, String typename) {
		super(t, typename);
		ConstraintValidator.ValidateTypename(typename, t);
	}

	@Override
	public Set<ConstraintLocation> validLocations() {
		return EnumSet.of(ConstraintLocation.DEFINITION);
	}

	@Override
	public String getConstraintName() {
		return "variadic generic type";
	}

	@Override
	public boolean isWithinConstraint(LeftHandSideType type, Environment env) {
		return true;
	}

	@Override
	public boolean supportsTypeUnions() {
		return true;
	}

	@Override
	protected ConstraintToConstraintValidator getConstraintToConstraintValidator(Environment env) {
		return new ConstraintToConstraintValidator() {
			@Override
			public Boolean isWithinBounds(ConstructorConstraint lhs) {
				return false;
			}

			@Override
			public Boolean isWithinBounds(ExactTypeConstraint lhs) {
				return true;
			}

			@Override
			public Boolean isWithinBounds(LowerBoundConstraint lhs) {
				return false;
			}

			@Override
			public Boolean isWithinBounds(UpperBoundConstraint lhs) {
				return false;
			}

			@Override
			public Boolean isWithinBounds(UnboundedConstraint lhs) {
				return false;
			}

			@Override
			public Boolean isWithinBounds(VariadicTypeConstraint lhs) {
				return true;
			}
		};
	}

	@Override
	public ExactTypeConstraint convertFromDiamond(Target t) throws CREGenericConstraintException {
		return new ExactTypeConstraint(t, Auto.LHSTYPE);
	}

	@Override
	public String toSimpleString() {
		return getTypeName() + "...";
	}

	@Override
	public ExactTypeConstraint convertFromNull(Target t) throws CREGenericConstraintException {
		return new ExactTypeConstraint(t, (LeftHandSideType) Auto.LHSTYPE.asVariadicType(null));
	}

}
