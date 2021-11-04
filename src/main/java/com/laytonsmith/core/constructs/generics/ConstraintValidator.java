package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CREGenericConstraintException;

import java.util.ArrayList;
import java.util.List;

public class ConstraintValidator {

	private ConstraintValidator(){}

	/**
	 * Validates and returns the typename for a set of constraints.
	 * @param constraints The constraint(s) to validate
	 * @return The typename, for instance <code>T</code> in <code>T extends Number</code>.
	 */
	public static String ValidateDefinition(List<Constraint> constraints)
			throws CREGenericConstraintException {
		for(Constraint c : constraints) {
			if(!c.validLocations().contains(ConstraintLocation.DEFINITION)) {
				throw new CREGenericConstraintException("The " + c.getConstraintName() + " constraint type cannot be"
						+ " used at the location of the " + ConstraintLocation.DEFINITION.getLocationName(),
						c.getTarget());
			}
			if(c.isWildcard()) {
				throw new CREGenericConstraintException("Constraints cannot use wildcards at the definition site.",
						c.getTarget());
			}
		}
		if(constraints.size() == 1) {
			// Only 1 constraint is always valid
			return constraints.get(0).getTypeName();
		}
		throw new CREGenericConstraintException("Multiple constraints are not yet supported.",
				constraints.get(0).getTarget());
	}

	/**
	 * Validates the LHS of a definition. This should be called with null if no generic parameters were defined, as that
	 * is not always allowed, depending on the ClassType, and this case is accounted for.
	 * @param t
	 * @param type
	 * @param genericParameters
	 */
	public static void ValidateLHS(Target t, CClassType type, LeftHandGenericUse genericParameters) {
		ValidateLHS(t, type, genericParameters == null ? null : genericParameters.getConstraints());
	}

	/**
	 * Validates the LHS of a definition. This should be called with null if no generic parameters were defined, as that
	 * 	 * is not always allowed, depending on the ClassType, and this case is accounted for.
	 * @param t
	 * @param type
	 * @param c
	 * @throws CREGenericConstraintException
	 */
	public static void ValidateLHS(Target t, CClassType type, List<Constraints> c)
			throws CREGenericConstraintException {
		GenericDeclaration dec = type.getGenericDeclaration();
		if(dec == null) {
			// Nothing to validate here
			if(c != null) {
				// However, they provided something anyways...
				throw new CREGenericConstraintException(type.getFQCN().getFQCN() + " does not define generic parameters,"
						+ " but they were provided anyways", t);
			}
			return;
		}
		List<Constraints> declarationConstraints = dec.getConstraints();
		ValidateLHStoLHS(t, c, declarationConstraints);
	}

	public static void ValidateLHStoLHS(Target t, List<Constraints> checkIfTheseConstraints, List<Constraints> areWithinBoundsOfThese)
			throws CREGenericConstraintException {
		if(checkIfTheseConstraints.size() != areWithinBoundsOfThese.size()) {
			throw new CREGenericConstraintException("Expected " + areWithinBoundsOfThese.size() + " parameter(s), but found"
					+ " " + checkIfTheseConstraints.size(), t);
		}
		for(int i = 0; i < areWithinBoundsOfThese.size(); i++) {
			Constraints definition = areWithinBoundsOfThese.get(i);
			Constraints lhs = checkIfTheseConstraints.get(i);
			// Check that the LHS fits the bounds of the definition
			List<String> errors = new ArrayList<>();
			if(!definition.withinBounds(lhs, errors)) {
				throw new CREGenericConstraintException("The constraint " + lhs.toString() + " does not fit within the"
						+ " bounds " + definition.toString() + ": "
						+ StringUtils.Join(errors, "\n"), t);
			}
		}
	}
}
