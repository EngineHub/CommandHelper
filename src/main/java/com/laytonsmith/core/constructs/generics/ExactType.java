package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;

import java.util.EnumSet;

public class ExactType extends Constraint {

	private final CClassType type;

	public ExactType(Target t, CClassType type) {
		super(t, type.getFQCN().getFQCN());
		this.type = type;
	}

	@Override
	public EnumSet<ConstraintLocation> validLocations() {
		return EnumSet.of(ConstraintLocation.LHS, ConstraintLocation.RHS);
	}

	@Override
	public String getConstraintName() {
		return type.getFQCN().getFQCN();
	}

	public CClassType getType() {
		return type;
	}

}
