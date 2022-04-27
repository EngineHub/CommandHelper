package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Either;
import com.laytonsmith.PureUtilities.ObjectHelpers;
import com.laytonsmith.PureUtilities.ObjectHelpers.StandardField;
import com.laytonsmith.PureUtilities.Pair;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREGenericConstraintException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents the generic parameters on the LHS. In general, this can contain several types of constraints,
 * including the ExactType constraint. Unlike the RHS, this information is not required to be kept around post
 * compilation, since the RHS is typechecked against this information, and then once confirmed to be correct, is no
 * longer needed for dynamic use of the reified type. The RHS can in general contain LHS information though,
 * particularly when the class specified on the RHS itself contains generics, in which case, the information within
 * those parameters will be LHS information.
 */
public class LeftHandGenericUse {

	@StandardField
	private final List<LeftHandGenericUseParameter> parameters;

	private static void ValidateCount(int inputParameters, CClassType forType, Target t) {
		if(forType.getGenericDeclaration().getParameterCount() != inputParameters) {
			throw new CREGenericConstraintException(
					forType.getSimpleName() + " expects "
					+ StringUtils.PluralTemplateHelper(forType.getGenericDeclaration().getParameterCount(),
							"%d parameter", "%d parameters")
					+ " but "
					+ StringUtils.PluralTemplateHelper(inputParameters, "only %1 was found.", "%d were found."), t);
		}
	}

	/**
	 * Constructs a new LeftHandGenericUse object when all parameters are regular Constraints.
	 *
	 * @param forType The type that is attached to this LHGU, for validation.
	 * @param t The code target.
	 * @param env The environment.
	 * @param constraints The list of Constraints. Each one represents one parameter.
	 */
	public LeftHandGenericUse(CClassType forType, Target t, Environment env, Constraints... constraints) {
		ValidateCount(constraints.length, forType, t);
		ConstraintValidator.ValidateLHS(t, forType, Arrays.asList(constraints), env);
		parameters = new ArrayList<>();
		for(Constraints c : constraints) {
			parameters.add(new LeftHandGenericUseParameter(Either.left(c)));
		}
	}

	/**
	 * Constructs a new LeftHandGenericUse object when all parameters are typenames.
	 *
	 * @param forType The type that is attached to this LHGU, for validation.
	 * @param t The code target.
	 * @param env The environment.
	 * @param typenames The list of typenames.
	 */
	public LeftHandGenericUse(CClassType forType, Target t, Environment env, Pair<String, Constraints>... typenames) {
		ValidateCount(typenames.length, forType, t);
		ConstraintValidator.ValidateLHS(t, forType, Arrays.asList(typenames).stream()
				.map(item -> item.getValue()).collect(Collectors.toList()), env);
		parameters = new ArrayList<>();
		for(Pair<String, Constraints> typename : typenames) {
			parameters.add(new LeftHandGenericUseParameter(Either.right(typename)));
		}
	}

	/**
	 * Constructs a new LeftHandGenericUse object for the given mix of Constraints objects and String typenames. If all
	 * values are the same type, use of the other constructors is cleaner and preferred.
	 *
	 * @param forType The type that is attached to this LHGU, for validation.
	 * @param t The code target.
	 * @param env The environment.
	 * @param parameters The list of parameters.
	 */
	public LeftHandGenericUse(CClassType forType, Target t, Environment env,
			Either<Constraints, Pair<String, Constraints>>... parameters) {
		ValidateCount(parameters.length, forType, t);
		List<Constraints> constraints = new ArrayList<>();
		for(Either<Constraints, Pair<String, Constraints>> param : parameters) {
			Constraints c;
			if(param.hasLeft()) {
				c = param.getLeft().get();
			} else {
				c = param.getRight().get().getValue();
			}
			constraints.add(c);
		}
		ConstraintValidator.ValidateLHS(t, forType, constraints, env);
		this.parameters = new ArrayList<>();
		for(Either<Constraints, Pair<String, Constraints>> param : parameters) {
			this.parameters.add(new LeftHandGenericUseParameter(param));
		}
	}

	/**
	 * Constructs a new LeftHandGenericUse object with the premade parameter objects.
	 *
	 * @param forType The type that is attached to this LHGU, for validation.
	 * @param t The code target.
	 * @param env The environment.
	 * @param parameters The list of parameters.
	 */
	public LeftHandGenericUse(CClassType forType, Target t, Environment env, LeftHandGenericUseParameter... parameters) {
		this(forType, t, env, Arrays.asList(parameters).stream().map(item -> item.getValue())
				.collect(Collectors.toList()).toArray(Either[]::new));
	}

	public LeftHandGenericUse(CClassType forType, Target t, Environment env, List<LeftHandGenericUseParameter> parameters) {
		this(forType, t, env, parameters.toArray(LeftHandGenericUseParameter[]::new));
	}

	/**
	 * Constructs a new LeftHandGenericUse object. This should only be used for native object construction.
	 * @param forType The type that is attached to this LHGU, for validation.
	 * @param parameters The parameters.
	 * @return A new LeftHandGenericUse object.
	 */
	public static LeftHandGenericUse forNativeParameters(CClassType forType, LeftHandGenericUseParameter... parameters) {
		return new LeftHandGenericUse(forType, Target.UNKNOWN, null, parameters);
	}

	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object that) {
		return ObjectHelpers.DoEquals(this, that);
	}

	@Override
	public int hashCode() {
		return ObjectHelpers.DoHashCode(this);
	}

	public List<LeftHandGenericUseParameter> getParameters() {
		return new ArrayList<>(parameters);
	}

	public List<Constraints> getConstraints() {
		List<Constraints> constraints = new ArrayList<>();
		for(LeftHandGenericUseParameter param : parameters) {
			constraints.add(param.getConstraints());
		}
		return constraints;
	}

	/**
	 * Returns whether or not the parameter at the specified location is a typename.
	 * @param parameterPlace The location of the parameter, 0 indexed.
	 * @return True if the parameter at the location is a typename.
	 */
	public boolean isTypename(int parameterPlace) {
		return parameters.get(parameterPlace).getValue().hasRight();
	}

	/**
	 * Returns true if the LHGU has a typename parameter in any position.
	 * @return
	 */
	public boolean hasTypename() {
		for(LeftHandGenericUseParameter param : parameters) {
			if(param.getValue().hasRight()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Works like toString, but uses the class's simple name.
	 *
	 * @return
	 */
	public String toSimpleString() {
		StringBuilder b = new StringBuilder();
		boolean doComma = false;
		for(LeftHandGenericUseParameter cc : parameters) {
			if(doComma) {
				b.append(", ");
			}
			doComma = true;
			b.append(cc.toSimpleString());
		}
		return b.toString();
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		boolean doComma = false;
		for(LeftHandGenericUseParameter cc : parameters) {
			if(doComma) {
				b.append(", ");
			}
			doComma = true;
			b.append(cc.toString());
		}
		return b.toString();
	}

	/**
	 * Checks if the generics on the RHS are within bounds of this LHS generic definition.
	 *
	 * @param env The environment
	 * @param types The types, each argument corresponding to the Constraints objects, that is, each argument to the
	 * generic parameter set.
	 * @return True if all types are within bounds.
	 */
	public boolean isWithinBounds(Environment env, LeftHandSideType... types) {
		if(this.getConstraints().size() != types.length) {
			return false;
		}
		for(int i = 0; i < this.getConstraints().size(); i++) {
			Constraints lhs = this.parameters.get(i).getConstraints();
			LeftHandSideType type = types[i];
			if(!lhs.withinBounds(type, env)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param env The environment
	 * @param presumedSubtype The value to check if it is within the bounds represented by this object
	 * @return True if the input LHS type is a subtype of this LHS.
	 */
	public boolean isWithinBounds(Environment env, LeftHandGenericUse presumedSubtype) {
		if(presumedSubtype == null) {
			// We have generics, but they didn't provide any, so it doesn't match.
			return false;
		}
		List<Constraints> checkIfTheseConstraints = presumedSubtype.getConstraints();
		List<Constraints> areWithinBoundsOfThese = this.getConstraints();
		if(checkIfTheseConstraints.size() != areWithinBoundsOfThese.size()) {
			return false;
		}
		for(int i = 0; i < areWithinBoundsOfThese.size(); i++) {
			Constraints definition = areWithinBoundsOfThese.get(i);
			Constraints lhs = checkIfTheseConstraints.get(i);
			// Check that the LHS fits the bounds of the definition
			List<String> errors = new ArrayList<>();
			if(!definition.withinBounds(lhs, errors, env)) {
				return false;
			}
		}
		return true;
	}

}
