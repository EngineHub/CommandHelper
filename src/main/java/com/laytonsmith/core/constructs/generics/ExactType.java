package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.PureUtilities.ObjectHelpers;
import com.laytonsmith.PureUtilities.ObjectHelpers.StandardField;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;

import java.util.EnumSet;
import java.util.Objects;

public class ExactType extends Constraint {

	@StandardField
	private final CClassType type;
	@StandardField
	private final LeftHandGenericUse genericParameters;

	/**
	 * Constructs a new unbounded wildcard instance of ExactType.
	 * @param t The code target
	 * @return
	 */
	public static ExactType AsUnboundedWildcard(Target t) {
		return new ExactType(t);
	}

	private ExactType(Target t) {
		super(t, "?");
		type = null;
		genericParameters = null;
	}

	/**
	 *
	 * @param t
	 * @param type
	 * @param genericParameters
	 */
	public ExactType(Target t, CClassType type, LeftHandGenericUse genericParameters) {
		super(t, type.getFQCN().getFQCN());
		Objects.requireNonNull(type);
		this.type = type;
		this.genericParameters = genericParameters;
	}

	@Override
	public EnumSet<ConstraintLocation> validLocations() {
		return EnumSet.of(ConstraintLocation.LHS, ConstraintLocation.RHS);
	}

	@Override
	public String getConstraintName() {
		return type == null ? "?" : type.getFQCN().getFQCN();
	}

	@Override
	public boolean isWithinConstraint(CClassType type, LeftHandGenericUse generics, Environment env) {
		if(this.type == null) {
			for(Constraints c : type.getGenericDeclaration().getConstraints()) {
				if(!c.withinBounds(type, generics, env)) {
					return false;
				}
			}
			return true;
		}
		return type.getNakedType(env).equals(this.type.getNakedType(env))
				&& (this.genericParameters == null || this.genericParameters.isWithinBounds(env, generics));
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
	 * Returns the exact ClassType for this constraint. Note that in the case of a wildcard "?", an ExactType constraint
	 * will be used, however, this value will be null.
	 */
	public CClassType getType() {
		return type;
	}

	@Override
	public String toSimpleString() {
		return type == null ? "?" : type.getFQCN().getSimpleName()
				+ (genericParameters == null ? "" : "<" + genericParameters.toSimpleString() + ">");
	}


	@Override
	public String toString() {
		return type == null ? "?" : type.getFQCN().getFQCN()
				+ (genericParameters == null ? "" : "<" + genericParameters.toString() + ">");
	}

	public LeftHandGenericUse getGenericParameters() {
		return genericParameters;
	}

	@Override
	public boolean equals(Object that) {
		return ObjectHelpers.DoEquals(this, that);
	}

	@Override
	public int hashCode() {
		return ObjectHelpers.DoHashCode(this);
	}
}
