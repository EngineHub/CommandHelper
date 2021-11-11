package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;

import java.util.EnumSet;
import java.util.Objects;

public class ExactType extends Constraint {

	private final CClassType type;
	private final LeftHandGenericUse genericParameters;
	private final GenericDeclaration genericDeclaration;

	/**
	 * Constructs a new unbounded wildcard instance of ExactType.
	 * @param typeDeclaration As an unbounded wildcard, it simply inherits the restrictions from the class definition.
	 * @param t The code target
	 * @return
	 */
	public static ExactType AsUnboundedWildcard(GenericDeclaration typeDeclaration, Target t) {
		return new ExactType(typeDeclaration, t);
	}

	private ExactType(GenericDeclaration typeDeclaration, Target t) {
		super(t, "?");
		type = null;
		genericParameters = null;
		this.genericDeclaration = typeDeclaration;
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
		this.genericDeclaration = null;
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
			for(Constraints c : this.genericDeclaration.getConstraints()) {
				if(!c.withinBounds(type, generics, env)) {
					return false;
				}
			}
			return true;
		}
		return type.getNakedType().equals(this.type.getNakedType())
				&& (this.genericParameters == null || this.genericParameters.equals(generics));
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
				return false;
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
	 * @return
	 */
	public CClassType getType() {
		return type;
	}

	@Override
	public String toString() {
		return type == null ? "?" : type.getFQCN().getFQCN();
	}

	public LeftHandGenericUse getGenericParameters() {
		return genericParameters;
	}
}
