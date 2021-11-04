package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;

import java.util.EnumSet;

public class ExactType extends Constraint {

	private final CClassType type;
	private final LeftHandGenericUse genericParameters;

	/**
	 *
	 * @param t
	 * @param type
	 * @param genericParameters
	 */
	public ExactType(Target t, CClassType type, LeftHandGenericUse genericParameters) {
		super(t, type.getFQCN().getFQCN());
		this.type = type;
		this.genericParameters = genericParameters;
	}

	@Override
	public EnumSet<ConstraintLocation> validLocations() {
		return EnumSet.of(ConstraintLocation.LHS, ConstraintLocation.RHS);
	}

	@Override
	public String getConstraintName() {
		return type.getFQCN().getFQCN();
	}

	@Override
	public boolean isWithinConstraint(CClassType type, LeftHandGenericUse generics, Environment env) {
		return type.getNakedType().equals(this.type.getNakedType()) && this.genericParameters.equals(generics);
	}

	@Override
	protected ConstraintToConstraintValidator getConstraintToConstraintValidator(Environment env) {

	}

	@Override
	public ExactType convertFromDiamond(Target t) {
		return this;
	}

	public CClassType getType() {
		return type;
	}

	@Override
	public String toString() {
		return type.getFQCN().getFQCN();
	}

	public LeftHandGenericUse getGenericParameters() {
		return genericParameters;
	}
}
