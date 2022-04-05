package com.laytonsmith.core.compiler.signature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Pair;
import com.laytonsmith.core.constructs.Auto;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.ConstraintValidator;
import com.laytonsmith.core.constructs.generics.GenericDeclaration;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.constructs.generics.LeftHandGenericUse;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;

/**
 * Represents a collection of signatures for a single function, procedure or closure.
 *
 * @author P.J.S. Kools
 */
public final class FunctionSignatures {

	private final List<FunctionSignature> signatures = new ArrayList<>();
	private final MatchType matchType;

	protected FunctionSignatures(MatchType matchType) {
		this.matchType = matchType;
	}

	protected void addSignature(FunctionSignature signature) {
		this.signatures.add(signature);
	}

	/**
	 * Gets all signatures in this {@link FunctionSignatures}.
	 *
	 * @return A {@link List} containing all signatures.
	 */
	public List<FunctionSignature> getSignatures() {
		return Collections.unmodifiableList(this.signatures);
	}

	/**
	 * Gets the parameter types string of all signatures.
	 *
	 * @return The string in format "(firstArgType, secondArgType, ...)|(...)|...".
	 */
	public String getSignaturesParamTypesString() {
		return StringUtils.Join(this.signatures, " | ", null, null, null,
				(FunctionSignature signature) -> signature.getParamTypesString());
	}

	private LeftHandSideType resolveTypeFromGenerics(Target t, Environment env, LeftHandSideType type,
			GenericParameters parameters, GenericDeclaration declaration) {
		if(type == null) {
			// Type is none, cannot have generics
			return type;
		}
		if(!type.isTypeName()) {
			return type;
		}
		// Validate the parameters against the declaration, and then return the type of the correct parameter
		ConstraintValidator.ValidateParametersToDeclaration(t, env, parameters, declaration);

		if(parameters == null) {
			// Return auto for no type. This already passed, since ValidateParametersToDeclaration would have
			// failed if this were null and auto wasn't sufficient due to the constraints.
			return Auto.LHSTYPE;
		}
		// It passes. Lookup the correct parameter based on the typename.
		String typename = type.getTypename();
		for(int i = 0; i < declaration.getParameterCount(); i++) {
			if(declaration.getConstraints().get(i).getTypeName().equals(typename)) {
				// Found it
				Pair<CClassType, LeftHandGenericUse> p = parameters.getParameters().get(i);
				return LeftHandSideType.fromCClassType(p.getKey(), p.getValue(), t);
			}
		}
		// Would be good to unit test for this, but this won't be able to happen generally in user
		// classes.
		throw new Error("Typename returned by native function is not in the GenericDeclaration!");
	}

	/**
	 * Gets the return {@link CClassType} based on this {@link FunctionSignatures}.If none of the signatures match, a
	 * compile error is generated. If multiple signatures match, then the most specific shared type is returned.
	 *
	 * @param t The code target, used for setting the code target in thrown exceptions.
	 * @param generics The generic parameters passed to the function.
	 * @param argTypes The types of the passed arguments.
	 * @param argTargets The {@link Target}s belonging to the argTypes (in the same order).
	 * @param env The {@link Environment}, used for instanceof checks on types.
	 * @param exceptions A set to which all type errors will be added.
	 * @return The return type.
	 */
	public LeftHandSideType getReturnType(Target t, GenericParameters generics, List<LeftHandSideType> argTypes,
			List<Target> argTargets, Environment env, Set<ConfigCompileException> exceptions) {

		// List all matching signatures, or return the return type of the first match when MatchType MATCH_FIRST is set.
		List<FunctionSignature> matches = new ArrayList<>();
		for(FunctionSignature signature : this.getSignatures()) {
			if(signature.matches(argTypes, env, false)) {
				if(this.matchType == MatchType.MATCH_FIRST) {
					return resolveTypeFromGenerics(t, env,
							signature.getReturnType().getType(), generics, signature.getGenericDeclaration());
				}
				matches.add(signature);
			}
		}

		// Select the return type based on the matches.
		switch(matches.size()) {
			case 0 -> {
				// No matches. Generate a compile error and return AUTO to prevent further typechecking errors.
				String argTypesStr = "(" + StringUtils.Join(argTypes, ", ",
						(LeftHandSideType type) -> (type == null ? "none" : type.getSimpleName())) + ")";
				exceptions.add(new ConfigCompileException("Arguments " + argTypesStr
						+ " do not match required " + this.getSignaturesParamTypesString() + ".", t));
				return CClassType.AUTO.asLeftHandSideType();
			}
			case 1 -> {
				// Exactly one signature matches, so return the return type.
				return resolveTypeFromGenerics(t, env, matches.get(0).getReturnType().getType(), generics,
						matches.get(0).getGenericDeclaration());
			}
			default -> {
				// TODO - Ideally, we'd either return a multi-type or the most specific super type of the signatures.
				// Return the return type of all matching signatures if they are the same.
				LeftHandSideType type = resolveTypeFromGenerics(t, env, matches.get(0).getReturnType().getType(),
					generics,
					matches.get(0).getGenericDeclaration());
				for(int i = 1; i < matches.size(); i++) {
					LeftHandSideType retType = resolveTypeFromGenerics(t, env,
						matches.get(i).getReturnType().getType(),
						generics,
						matches.get(0).getGenericDeclaration());
					if((retType == null ? retType != type : !retType.equals(type))) {
						return CClassType.AUTO.asLeftHandSideType();
					}
				}
				return type;
			}
		}
	}

	/**
	 * Represents how {@link FunctionSignature}s within {@link FunctionSignatures} should be matched with given argument
	 * types.
	 *
	 * @author P.J.S. Kools
	 */
	public static enum MatchType {

		/**
		 * Indicates that all signatures within a {@link FunctionSignatures} should be matched when possible.
		 */
		MATCH_ALL,
		/**
		 * Indicates that signatures within a {@link FunctionSignatures} should be matched from first to last,
		 * terminating as soon as a match is found.
		 */
		MATCH_FIRST;
	}
}
