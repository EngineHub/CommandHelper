package com.laytonsmith.core.compiler.signature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;

/**
 * Represents a collection of signatures for a single function, procedure or closure.
 * @author P.J.S. Kools
 */
public final class FunctionSignatures {

	private final List<FunctionSignature> signatures = new ArrayList<>();
	private MatchType matchType;

	protected FunctionSignatures(MatchType matchType) {
		this.matchType = matchType;
	}

	protected void addSignature(FunctionSignature signature) {
		this.signatures.add(signature);
	}

	/**
	 * Gets all signatures in this {@link FunctionSignatures}.
	 * @return A {@link List} containing all signatures.
	 */
	public List<FunctionSignature> getSignatures() {
		return Collections.unmodifiableList(this.signatures);
	}

	/**
	 * Gets the parameter types string of all signatures.
	 * @return The string in format "(firstArgType, secondArgType, ...)|(...)|...".
	 */
	public String getSignaturesParamTypesString() {
		return StringUtils.Join(this.signatures, " | ", null, null, null,
				(FunctionSignature signature) -> signature.getParamTypesString());
	}

	/**
	 * Gets the return {@link CClassType} based on this {@link FunctionSignatures}.
	 * If none of the signatures match, a compile error is generated.
	 * If multiple signatures match, then the most specific shared type is returned.
	 * @param t - The code target, used for setting the code target in thrown exceptions.
	 * @param argTypes - The types of the passed arguments.
	 * @param argTargets - The {@link Target}s belonging to the argTypes (in the same order).
	 * @param env - The {@link Environment}, used for instanceof checks on types.
	 * @param exceptions - A set to which all type errors will be added.
	 * @return The return type.
	 */
	public CClassType getReturnType(Target t, List<CClassType> argTypes,
			List<Target> argTargets, Environment env, Set<ConfigCompileException> exceptions) {

		// List all matching signatures, or return the return type of the first match when MatchType MATCH_FIRST is set.
		List<FunctionSignature> matches = new ArrayList<>();
		for(FunctionSignature signature : this.getSignatures()) {
			if(signature.matches(argTypes, env, false)) {
				if(this.matchType == MatchType.MATCH_FIRST) {
					return signature.getReturnType().getType();
				}
				matches.add(signature);
			}
		}

		// Select the return type based on the matches.
		switch(matches.size()) {
			case 0: {
				// No matches. Generate a compile error and return AUTO to prevent further typechecking errors.
				String argTypesStr = "(" + StringUtils.Join(
						argTypes, ", ", null, null, null, (CClassType type) -> type.getSimpleName()) + ")";
				exceptions.add(new ConfigCompileException("Arguments " + argTypesStr
						+ " do not match required " + this.getSignaturesParamTypesString() + ".", t));
				return CClassType.AUTO;
			}
			case 1: {
				// Exactly one signature matches, so return the return type.
				return matches.get(0).getReturnType().getType();
			}
			default: {
				// TODO - Ideally, we'd either return a multi-type or the most specific super type of the signatures.

				// Return the return type of all matching signatures if they are the same.
				CClassType type = matches.get(0).getReturnType().getType();
				for(FunctionSignature match : matches) {
					if(!match.getReturnType().getType().equals(type)) {
						return CClassType.AUTO;
					}
				}
				return type;
			}
		}
	}

	/**
	 * Represents how {@link FunctionSignature}s within {@link FunctionSignatures} should be matched with given
	 * argument types.
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
