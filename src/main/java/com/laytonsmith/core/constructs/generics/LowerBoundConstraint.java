package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CREGenericConstraintException;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.EnumSet;

/**
 * A LowerBoundConstraint is defined with the super keyword, such as <code>T super Number</code>.
 * In this case, Number is the lower bound.
 */
public class LowerBoundConstraint extends BoundaryConstraint {

	public LowerBoundConstraint(Target t, String typename, CClassType lowerBound) {
		super(t, typename, lowerBound);
		if(lowerBound.equals(Mixed.TYPE)) {
			throw new CREGenericConstraintException("Cannot create a lower bound on mixed", t);
		}
	}

	public CClassType getLowerBound() {
		return this.bound;
	}

	@Override
	public String toString() {
		return getTypeName() + " super " + getLowerBound().getFQCN().getFQCN();
	}

	@Override
	public EnumSet<ConstraintLocation> validLocations() {
		return EnumSet.of(ConstraintLocation.LHS);
	}

	@Override
	public String getConstraintName() {
		return "lower bound";
	}
}
