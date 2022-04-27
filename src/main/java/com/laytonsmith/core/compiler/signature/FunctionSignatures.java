package com.laytonsmith.core.compiler.signature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.constructs.Auto;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.ConstraintValidator;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.util.Map;

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

	/**
	 * Gets the return {@link CClassType} based on this {@link FunctionSignatures}.If none of the signatures match, a
	 * compile error is generated.If multiple signatures match, then the most specific shared type is returned.
	 *
	 * @param node The function node. This is populated with the generic types if they aren't already there explicitly.
	 * The explicit nodes are pulled from here as well.
	 * @param t The code target, used for setting the code target in thrown exceptions.
	 * @param argTypes The types of the passed arguments.
	 * @param argTargets The {@link Target}s belonging to the argTypes (in the same order).
	 * @param inferredReturnType The inferred return type, which are used in case the function call does not
	 * have explicit type parameters. May be null.
	 * @param env The {@link Environment}, used for instanceof checks on types.
	 * @param exceptions A set to which all type errors will be added.
	 * @return The return type.
	 */
	public LeftHandSideType getReturnType(ParseTree node, Target t, List<LeftHandSideType> argTypes,
			List<Target> argTargets, LeftHandSideType inferredReturnType,
			Environment env, Set<ConfigCompileException> exceptions) {

		// List all matching signatures, or return the return type of the first match when MatchType MATCH_FIRST is set.
		GenericParameters generics = node.getNodeModifiers().getGenerics();
		List<FunctionSignature> matches = new ArrayList<>();
		/*
			TODO: This must be changed to take into account the fact that the runtime uses late binding. Consider a
			function defined with two signatures, `int func(array)` and `string func(ArrayAccess)`. At first blush,
			if the input argument is `ArrayAccess` we might assume that surely the second signature should match, and
			string returned, and not the first.

			But consider the caller code such as `ArrayAccess @a = array(); func(@a);`. In this case, at runtime, the
			type is array(), and so the second one which returns string is in fact the signature that should be
			used. Unfortunately, this code currently will select the ArrayAccess one, and will state that the
			return type will be int, which is not always the case, the correct return type in this case for ArrayAccess
			should be `int | string`, and not `string`. Additional parameters that definitely don't match can
			disambiguate, such as `func(ArrayAccess, string)` and `func(array, int)`. In this case, we use the second
			parameter to determine if the first or second signature should match, because `func(array, string)` always
			matches the first, and `func(array, int)` always matches the second.

			Null values must also be taken into account, but we can require an explicit cast for hardcoded nulls that
			are ambiguous.

			Signatures such as `int func(ArrayAccess, array)` and `int func(array, ArrayAccess)` are not allowed,
			because a call to `func(array, array)` would be completely ambiguous, and cannot be resolved even at
			runtime either. To determine this, we take the first argument in the first signature, and see that
			ArrayAccess is a superclass of array in the second signature, and so the second signature is so far
			eligible to be an overmatched signature. Then, we see that the second parameter of the first signature
			is array, which is not an superclass of ArrayAccess. However, it is a subclass of the overmatched signature.
			We end the search, and find there were no disambiguating parameters, and we had subclasses of overmatched
			signatures, and so we trigger a compile error, because the signatures are ambiguous.
		*/
		for(FunctionSignature signature : this.getSignatures()) {
			if(signature.matches(argTypes, generics, env, inferredReturnType, false)) {
				matches.add(signature);
				if(this.matchType == MatchType.MATCH_FIRST) {
					break;
				}
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
				FunctionSignature signature = matches.get(0);
				Map<String, LeftHandSideType> typeResolutions
						= signature.getTypeResolutions(t, argTypes, generics, inferredReturnType, env);
				if(generics == null && !typeResolutions.isEmpty()) {
					// Put them in the node
					GenericParameters.GenericParametersBuilder builder = GenericParameters.emptyBuilder(signature);
					for(Map.Entry<String, LeftHandSideType> entry : typeResolutions.entrySet()) {
						if(entry.getValue() == null) {
							builder.addParameter(null, null, env, t);
						} else {
							builder.addParameter(entry.getValue());
						}
					}
					generics = builder.build(t, env);
					node.getNodeModifiers().setGenerics(generics);
					// If this is wrong, this implies we did something wrong here, but just in case.
					ConstraintValidator.ValidateParametersToDeclaration(t, env, generics,
							signature.getGenericDeclaration(), inferredReturnType);
				}
				return LeftHandSideType.resolveTypeFromGenerics(t, env,
						signature.getReturnType().getType(), generics, signature.getGenericDeclaration(),
						typeResolutions);
			}
			default -> {
				LeftHandSideType[] types = new LeftHandSideType[matches.size()];
				for(int i = 0; i < matches.size(); i++) {
					LeftHandSideType retType = LeftHandSideType.resolveTypeFromGenerics(t, env,
						matches.get(i).getReturnType().getType(),
						generics,
						matches.get(0).getGenericDeclaration(), inferredReturnType);
					types[i] = retType;
				}
				LeftHandSideType ret = LeftHandSideType.fromTypeUnion(t, env, types);
				if(ret.isTypeUnion()) {
					// If multiple types match, and one or more of the arguments is auto, we return auto, rather than
					// the type union. This indicates that the user is content with runtime type verification, and so
					// we continue that up the chain. In non-strict mode, constants (and $vars) are declared as auto,
					// so that we get expected behavior with for instance `'123' > 10`, which causes this mode to be
					// triggered more often than in non-strict mode. For non-ambiguous signatures even with auto arguments,
					// this behavior is not triggered.
					for(LeftHandSideType argType : argTypes) {
						if(Auto.LHSTYPE.equals(argType)) {
							return Auto.LHSTYPE;
						}
					}
				}
				return ret;
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
