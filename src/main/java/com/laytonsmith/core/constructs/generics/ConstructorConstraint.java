package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * A ConstructorConstraint is defined with <code>new T()</code>, optionally providing
 * types such as <code>new T(A, B, C)</code> where <code>A, B, C</code> are concrete types, and
 * <code>T</code> is the type variable.
 * This requires that the class provided by the use-site contains a constructor of the
 * specified type.
 */
public class ConstructorConstraint extends Constraint {

	private final List<CClassType> types;

	public ConstructorConstraint(Target t, String typename, CClassType... types) {
		super(t, typename);
		this.types = Arrays.asList(types);
	}

	public List<CClassType> getTypes() {
		return new ArrayList<>(this.types);
	}

	@Override
	public String toString() {
		return "new " + getTypeName() + "("
				+ StringUtils.Join(types,
					", ",
					", ",
					", ",
					"",
					item -> item.getFQCN().getFQCN())
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
}
