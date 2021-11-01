package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CREGenericConstraintException;

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

	public static void ValidateLHS(Target t, CClassType type, List<Constraints> c)
			throws CREGenericConstraintException {
		GenericDeclaration dec = type.getGenericDeclaration();
		if(dec.getParameterCount() != c.size()) {
			throw new CREGenericConstraintException(type.getFQCN().getFQCN() + " defines " + dec.getParameterCount()
					+ " type parameter(s), but only found " + c.size(), t);
		}

		List<Constraints> declarationConstraints = dec.getConstraints();
		for(int i = 0; i < declarationConstraints.size(); i++) {
			Constraints definition = declarationConstraints.get(i);
			Constraints lhs = c.get(i);
			// Check that the LHS fits the bounds of the definition
		}
	}
}
