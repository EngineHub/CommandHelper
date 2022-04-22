package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.PureUtilities.ObjectHelpers;
import com.laytonsmith.PureUtilities.ObjectHelpers.StandardField;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;

import java.util.EnumSet;
import java.util.Objects;

public class ExactType extends Constraint {

	@StandardField
	private final LeftHandSideType type;


	/**
	 * Constructs a new unbounded wildcard instance of ExactType.
	 *
	 * @param t The code target
	 * @return
	 */
	public static ExactType AsUnboundedWildcard(Target t) {
		return new ExactType(t);
	}

	private ExactType(Target t) {
		super(t, "?");
		type = null;
	}

	/**
	 *
	 * @param t
	 * @param type
	 */
	public ExactType(Target t, LeftHandSideType type) {
		super(t, type.val());
		Objects.requireNonNull(type);
		this.type = type;
	}

	@Override
	public EnumSet<ConstraintLocation> validLocations() {
		return EnumSet.of(ConstraintLocation.LHS, ConstraintLocation.RHS);
	}

	@Override
	public String getConstraintName() {
		return type == null ? "?" : type.val();
	}

	@Override
	public boolean isWithinConstraint(LeftHandSideType type, Environment env) {
		if(this.type == null) {
			for(Constraints c : type.getGenericDeclaration().getConstraints()) {
				if(!c.withinBounds(type, generics, env)) {
					return false;
				}
			}
			return true;
		}
		// Loop through each type in the union, and see if at least one matches. If so, this passes.
		for(Pair<CClassType, LeftHandGenericUse> pair : this.type.getTypes()) {
			if(type.getNakedType(env).equals(this.type.getNakedType(env))
					&& (this.genericParameters == null || this.genericParameters.isWithinBounds(env, generics))) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected ConstraintToConstraintValidator getConstraintToConstraintValidator(Environment env) {
		return new ConstraintToConstraintValidator() {
			@Override
			public Boolean isWithinBounds(ConstructorConstraint lhs) {
				return false;
			}

			@Override
			public Boolean isWithinBounds(ExactType lhs) {
				return ExactType.this.equals(lhs);
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
				throw new Error("Unexpected constraint combination.");
			}
		};
	}

	@Override
	public ExactType convertFromDiamond(Target t) {
		return this;
	}

	/**
	 * Returns the exact type for this constraint.Note that in the case of a wildcard "?", an ExactType constraint will
	 * be used, however, this value will be null.
	 *
	 * @return The type, or null if it was defined as a wildcard.
	 */
	public LeftHandSideType getType() {
		return type;
	}

	@Override
	public String toSimpleString() {
		return type == null ? "?" : type.getSimpleName();
	}

	@Override
	public String toString() {
		return type == null ? "?" : type.val();
	}

	@Override
	public boolean equals(Object that) {
		return ObjectHelpers.DoEquals(this, that);
	}

	@Override
	public int hashCode() {
		return ObjectHelpers.DoHashCode(this);
	}

	@Override
	public boolean supportsTypeUnions() {
		return false;
	}
}
