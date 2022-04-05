package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Pair;
import com.laytonsmith.core.constructs.Auto;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREGenericConstraintException;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

public final class ConstraintValidator {

	private ConstraintValidator() {
	}

	/**
	 * Validates and returns the typename for a set of constraints.
	 *
	 * @param constraints The constraint(s) to validate
	 * @return The typename, for instance <code>T</code> in <code>T extends Number</code>.
	 */
	public static String ValidateDefinition(SortedSet<Constraint> constraints, Target t)
			throws CREGenericConstraintException {
		String typename = null;
		for(Constraint c : constraints) {
			if(typename == null) {
				typename = c.getTypeName();
			} else if(!typename.equals(c.getTypeName())) {
				throw new CREGenericConstraintException("Multiple constraints in the same parameter must be named"
						+ " with the same type name.", t);
			}
			if(!c.validLocations().contains(ConstraintLocation.DEFINITION)) {
				throw new CREGenericConstraintException("The " + c.getConstraintName() + " constraint type cannot be"
						+ " used at the location of the " + ConstraintLocation.DEFINITION.getLocationName(),
						c.getTarget());
			}
			if(c.isWildcard()) {
				throw new CREGenericConstraintException("Constraints cannot use wildcards at the definition site.",
						c.getTarget());
			}
			for(Constraint cc : constraints) {
				// Check for duplicate constraints
				if(c == cc) {
					continue;
				}
				if(c.equals(cc)) {
					throw new CREGenericConstraintException("Duplicate constraint found. One constraint"
							+ " defined at " + c.getTarget() + ", the other constraint at " + cc.getTarget(), t);
				}
			}
		}
		if(constraints.size() == 1) {
			// Only 1 constraint is always valid
			return typename;
		}
		// TODO: Need to write the constraint error solver.
		// This will require additional work to ensure that for instance, a type does not have an impossible
		// upper and lower bound, among others.
		throw new CREGenericConstraintException("Multiple constraints are not yet supported.", t);
	}

	/**
	 * Validates the RHS against the LHS of a definition. This should be called with null if no generic parameters were
	 * defined, as that is not always allowed, depending on the ClassType, and this case is accounted for. It is assumed
	 * that the LHS fits the constraints defined in the constraint definition.
	 *
	 * @param t
	 * @param type
	 * @param genericParameters
	 */
	public static void ValidateLHS(Target t, CClassType type, LeftHandGenericUse genericParameters, Environment env) {
		ValidateLHS(t, type, genericParameters == null ? null : genericParameters.getConstraints(), env);
	}

	/**
	 * Validates the RHS against the LHS of a definition. This should be called with null if no generic parameters were
	 * defined, as that is not always allowed, depending on the ClassType, and this case is accounted for. It is assumed
	 * that the LHS fits the constraints defined in the constraint definition.
	 *
	 * @param t
	 * @param type
	 * @param c
	 * @throws CREGenericConstraintException
	 */
	public static void ValidateLHS(Target t, CClassType type, List<Constraints> c, Environment env)
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
		if(c == null) {
			// If nothing was passed in, then this was declared without parameters, and they would be inferred ones.
			// This is generally fine, except when they're specifically required due to the class definition requiring
			// them, such as with the ConstructorConstraint. Therefore, we simply loop through the parameters, and try to
			// infer them, and if they all pass, we're good.
			for(Constraints cc : declarationConstraints) {
				cc.convertFromDiamond(t);
			}
		} else {
			ValidateLHStoLHS(t, c, declarationConstraints, env);
		}
	}

	public static void ValidateRHStoLHS(Target t, CClassType rhs, LeftHandGenericUse lhsGenerics, Environment env) {
		List<Constraints> exactType = new ArrayList<>();
		exactType.add(new Constraints(t, ConstraintLocation.RHS, new ExactType(t, rhs, lhsGenerics)));
		ValidateLHStoLHS(t, exactType, lhsGenerics.getConstraints(), env);
	}

	/**
	 * Checks that the given constraints are within the bounds of the other constraints.
	 *
	 * @param t
	 * @param checkIfTheseConstraints These are the constraints to check to see if they are within the bounds of the
	 * other constraints.
	 * @param areWithinBoundsOfThese These are the constraints to check against. These can be thought of as the
	 * "definition" even though that's not the case using previously defined terminology.
	 * @param env
	 * @throws CREGenericConstraintException
	 */
	public static void ValidateLHStoLHS(Target t, List<Constraints> checkIfTheseConstraints, List<Constraints> areWithinBoundsOfThese, Environment env)
			throws CREGenericConstraintException {
		if((checkIfTheseConstraints == null || checkIfTheseConstraints.isEmpty())
				&& (areWithinBoundsOfThese == null || areWithinBoundsOfThese.isEmpty())) {
			// This is ok, nothing to validate on either side
			return;
		}
		if(areWithinBoundsOfThese != null && checkIfTheseConstraints == null) {
			throw new RuntimeException("Missing constraints.");
		}
		if(checkIfTheseConstraints.size() != areWithinBoundsOfThese.size()) {
			throw new CREGenericConstraintException("Expected " + areWithinBoundsOfThese.size() + " parameter(s), but found"
					+ " " + checkIfTheseConstraints.size(), t);
		}
		for(int i = 0; i < areWithinBoundsOfThese.size(); i++) {
			Constraints definition = areWithinBoundsOfThese.get(i);
			Constraints lhs = checkIfTheseConstraints.get(i);
			// Check that the LHS fits the bounds of the definition
			List<String> errors = new ArrayList<>();
			if(!definition.withinBounds(lhs, errors, env)) {
				throw new CREGenericConstraintException("The constraint " + lhs.toString() + " does not fit within the"
						+ " bounds " + definition.toString() + ": "
						+ StringUtils.Join(errors, "\n"), t);
			}
		}
	}

	/**
	 * Validates a parameter set against a given parameter declaration.This ensures that the parameters passed in
	 * conform in size and type to the given constraints of the definition.
	 *
	 * @param t The code target, used in case of failure to throw a CREGenericConstraintException.
	 * @param env The environment.
	 * @param parameters The parameters.
	 * @param declaration The declaration.
	 */
	public static void ValidateParametersToDeclaration(Target t, Environment env,
			GenericParameters parameters, GenericDeclaration declaration) {
		if(declaration == null) {
			if(parameters != null) {
				throw new CREGenericConstraintException("No generics are defined here, unexpected generic parameters"
						+ " provided.", t);
			} else {
				// No parameters, no declaration, nothing to validate.
				return;
			}
		}
		if(parameters == null) {
			// Everything is auto, though this doesn't inherently work everywhere, so just fill it in with auto
			// based on the parameter count, then do the validation normally.
			GenericParameters.GenericParametersBuilder builder = GenericParameters.emptyBuilder();
			for(int i = 0; i < declaration.getParameterCount(); i++) {
				builder.addParameter(Auto.TYPE, null);
			}
			parameters = builder.build();
		}

		if(parameters.getParameters().size() != declaration.getParameterCount()) {
			throw new CREGenericConstraintException(StringUtils.PluralTemplateHelper(declaration.getParameterCount(),
					"Expected %d generic parameter", "Expected %d generic parameters") + " to be provided, but"
					+ " instead "
					+ StringUtils.PluralTemplateHelper(parameters.getParameters().size(),
							"%d was found.", "%d were found."), t);
		}

		for(int i = 0; i < declaration.getParameterCount(); i++) {
			Constraints c = declaration.getConstraints().get(i);
			Pair<CClassType, LeftHandGenericUse> param = parameters.getParameters().get(i);
			if(!c.withinBounds(param.getKey(), param.getValue(), env)) {
				throw new CREGenericConstraintException("Generic parameter at location " + i
						+ " does not satisfy the constraints: " + c.toString(), t);
			}
		}

	}
}
