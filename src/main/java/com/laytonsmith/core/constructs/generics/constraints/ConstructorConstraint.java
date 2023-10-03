package com.laytonsmith.core.constructs.generics.constraints;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.ConstraintLocation;
import com.laytonsmith.core.constructs.generics.ConstraintToConstraintValidator;
import com.laytonsmith.core.constructs.generics.ConstraintValidator;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREGenericConstraintException;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * A ConstructorConstraint is defined with <code>new T()</code>, optionally providing types such as
 * <code>new T(A, B, C)</code> where <code>A, B, C</code> are concrete types, and <code>T</code> is the type variable.
 * This requires that the class provided by the use-site contains a constructor of the specified type.
 */
public class ConstructorConstraint extends Constraint {

	private final List<LeftHandSideType> argTypes;

	public ConstructorConstraint(Target t, String typename, List<LeftHandSideType> argTypes) {
		super(t, typename);
		ConstraintValidator.ValidateTypename(typename, t);
		this.argTypes = argTypes;
	}

	public List<LeftHandSideType> getArgTypes() {
		return new ArrayList<>(this.argTypes);
	}

	@Override
	public String toSimpleString() {
		return "new " + getTypeName() + "("
				+ StringUtils.Join(argTypes,
						", ",
						", ",
						", ",
						"",
						item -> item.getSimpleName())
				+ ")";
	}

	@Override
	public String toString() {
		return "new " + getTypeName() + "("
				+ StringUtils.Join(argTypes, ", ", item -> item.val())
				+ ")";
	}

	@Override
	public EnumSet<ConstraintLocation> validLocations() {
		return EnumSet.allOf(ConstraintLocation.class);
	}

	@Override
	public String getConstraintName() {
		return "constructor constraint";
	}

	@Override
	public boolean isWithinConstraint(LeftHandSideType type, Environment env) {
		// TODO: Nothing has constructors yet, this is always false
		return false;
	}

	@Override
	protected ConstraintToConstraintValidator getConstraintToConstraintValidator(Environment env) {
		return new ConstraintToConstraintValidator() {
			@Override
			public Boolean isWithinBounds(ConstructorConstraint lhs) {
				return ConstructorConstraint.this.argTypes.equals(lhs.argTypes);
			}

			@Override
			public Boolean isWithinBounds(ExactTypeConstraint lhs) {
				return ConstructorConstraint.this.isWithinConstraint(lhs.getType(), env);
			}

			@Override
			public Boolean isWithinBounds(LowerBoundConstraint lhs) {
				return null;
			}

			@Override
			public Boolean isWithinBounds(UpperBoundConstraint lhs) {
				return null;
			}

			@Override
			public Boolean isWithinBounds(UnboundedConstraint lhs) {
				return false;
			}

			@Override
			public Boolean isWithinBounds(VariadicTypeConstraint lhs) {
				return null;
			}
		};
	}

	@Override
	public ExactTypeConstraint convertFromDiamond(Target t) {
		throw new CREGenericConstraintException("Cannot infer generic parameter from new constraint.", t);
	}

	@Override
	public boolean supportsTypeUnions() {
		return false;
	}

	@Override
	public ExactTypeConstraint convertFromNull(Target t) throws CREGenericConstraintException {
		throw new CREGenericConstraintException("Generic type required, auto type cannot be used.", t);
	}
}
