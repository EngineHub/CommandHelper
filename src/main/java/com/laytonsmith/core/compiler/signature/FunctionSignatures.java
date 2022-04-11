package com.laytonsmith.core.compiler.signature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.core.ParseTree;
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
					GenericParameters.GenericParametersBuilder builder = GenericParameters.emptyBuilder();
					for(Map.Entry<String, LeftHandSideType> entry : typeResolutions.entrySet()) {
						if(entry.getValue() == null) {
							builder.addParameter(null, null);
						} else {
							builder.addParameter(entry.getValue().asConcreteType(t), null);
						}
					}
					generics = builder.build();
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
				// TODO - Ideally, we'd either return a multi-type or the most specific super type of the signatures.
				// Return the return type of all matching signatures if they are the same.
				LeftHandSideType type = LeftHandSideType.resolveTypeFromGenerics(t, env, matches.get(0).getReturnType().getType(),
					generics,
					matches.get(0).getGenericDeclaration(), inferredReturnType);
				for(int i = 1; i < matches.size(); i++) {
					LeftHandSideType retType = LeftHandSideType.resolveTypeFromGenerics(t, env,
						matches.get(i).getReturnType().getType(),
						generics,
						matches.get(0).getGenericDeclaration(), inferredReturnType);
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
