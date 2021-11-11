package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Pair;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREGenericConstraintException;

import java.util.ArrayList;
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

	private final List<Pair<CClassType, LeftHandGenericUse>> types;

	public ConstructorConstraint(Target t, String typename, List<Pair<CClassType, LeftHandGenericUse>> types) {
		super(t, typename);
		this.types = types;
	}

	public List<Pair<CClassType, LeftHandGenericUse>> getTypes() {
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
					item -> item.getKey().getFQCN().getFQCN() + "<" + item.getValue().toString() + ">")
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
	public boolean isWithinConstraint(CClassType type, LeftHandGenericUse generics, Environment env) {
		// TODO: Nothing has constructors yet, this is always false
		return false;
	}

	@Override
	protected ConstraintToConstraintValidator getConstraintToConstraintValidator(Environment env) {
		return new ConstraintToConstraintValidator() {
			@Override
			public Boolean isWithinBounds(ConstructorConstraint lhs) {
				return ConstructorConstraint.this.types.equals(lhs.types);
			}

			@Override
			public Boolean isWithinBounds(ExactType lhs) {
				return ConstructorConstraint.this.isWithinConstraint(lhs.getType(), lhs.getGenericParameters(), env);
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
				return null;
			}
		};
	}

	@Override
	public ExactType convertFromDiamond(Target t) {
		throw new CREGenericConstraintException("Cannot infer generic parameter from new constraint.", t);
	}
}
