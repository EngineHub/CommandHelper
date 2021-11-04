package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREGenericConstraintException;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

/**
 * A Constraints object contains the full list of Constraints for a given type parameter.
 */
public class Constraints extends AbstractList<Constraint> {

	private final List<Constraint> list;
	private final String typename;

	/**
	 * Constructs a new constraint object. Note that if this is being used on the LHS, no validation is done
	 * @param constraints
	 */
	public Constraints(Target t, ConstraintLocation location, Constraint... constraints) {
		this.list = Arrays.asList(constraints);
		if(location == ConstraintLocation.RHS) {
			throw new CREGenericConstraintException("Constraints cannot be used on the RHS", t);
		}
		if(location == ConstraintLocation.DEFINITION) {
			typename = ConstraintValidator.ValidateDefinition(this.list);
		} else {
			typename = "?";
		}
	}

	@Override
	public Constraint get(int index) {
		return list.get(index);
	}

	@Override
	public int size() {
		return list.size();
	}

	/**
	 * Returns the name of the type. T for instance, or ? if this is a wildcard (defined on LHS).
	 * @return
	 */
	public String getTypeName() {
		return typename;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		boolean doComma = false;
		for(Constraint c : list) {
			if(doComma) {
				b.append(" & ");
			}
			doComma = true;
			b.append(c.toString());
		}
		return b.toString();
	}

	/**
	 * Validates that the given set of Constraints is within the bounds of these contraints. This can be used to validate
	 * LHS against the class definition. Use {@link #withinBounds(CClassType, LeftHandGenericUse)} to validate RHS
	 * against LHS.
	 * @param lhs The other, presumably subtype to compare against.
	 * @throws CREGenericConstraintException
	 */
	public boolean withinBounds(Constraints lhs, List<String> errors, Environment env) {
		for(Constraint t : list) {
			boolean oneIsTrue = false;
			for(Constraint c : lhs) {
				Boolean res = t.isWithinConstraint(c, env);
				if(res != null) {
					if(res) {
						oneIsTrue = true;
					} else {
						errors.add("The LHS constraint " + c + " does not"
								+ " suit the constraint defined on the class " + t);
					}
				}
			}
			if(!oneIsTrue) {
				errors.add("The class defines the constraint " + t + ", but no constraints defined on the LHS suit"
						+ " this constraint.");
			}
		}
		if(errors.size() > 0) {
			return false;
		}
		return true;
	}

	/**
	 * Validates that this concrete type (and perhaps the concrete type's generics) fit within the boundary
	 * specified in the LHS constraint. This is used to validate the RHS against the LHS. Use
	 * {@link #withinBounds(Constraints, List, Environment)} to validate the LHS against the definition.
	 * @param rhsType
	 * @param rhsGenerics
	 * @return
	 */
	public boolean withinBounds(CClassType rhsType, LeftHandGenericUse rhsGenerics) {
		return false;
	}

	/**
	 * Given that this is the constraints on the LHS, returns the ExactType value that should be used on the RHS if
	 * the diamond operator was used. Not all Constraints support this, so this might throw an exception.
	 * @return
	 */
	public ExactType convertFromDiamond(Target t) throws CREGenericConstraintException {
		// Diamond operator can currently only be used in simple cases, though we anyways check for definitely
		// wrong cases.
		ExactType type = null;
		for(Constraint c : list) {
			ExactType newType = c.convertFromDiamond(t);
			if(type == null) {
				type = newType;
			} else {
				throw new CREGenericConstraintException("Cannot infer generic type from LHS, please explicitely define"
						+ " the RHS generic parameters.", t);
			}
		}
	}
}
