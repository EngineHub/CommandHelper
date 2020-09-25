package com.laytonsmith.core.compiler.signature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.InstanceofUtil;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 * Represents a single function, procedure or closure signature.
 * @author P.J.S. Kools
 */
public class FunctionSignature {

	private final ReturnType returnType;
	private final List<Param> params;
	private final List<Throws> throwsList;

	/**
	 * Creates a new {@link FunctionSignature} with the given properties.
	 * @param returnType - The function return type.
	 * @param params - The function parameters.
	 * @param throwsList - The list of possibly thrown exceptions by the function.
	 */
	public FunctionSignature(ReturnType returnType, List<Param> params, List<Throws> throwsList) {
		this.returnType = returnType;
		this.params = params;
		this.throwsList = throwsList;
	}

	/**
	 * Creates a new {@link FunctionSignature} with the given return type and no parameters
	 * and possibly thrown exceptions.
	 * @param returnType - The function return type.
	 */
	public FunctionSignature(ReturnType returnType) {
		this(returnType, new ArrayList<>(), new ArrayList<>());
	}

	protected void addParam(Param param) {
		this.params.add(param);
	}

	protected void addThrows(Throws throwsObj) {
		this.throwsList.add(throwsObj);
	}

	/**
	 * Gets the function's return type.
	 * @return The return type.
	 */
	public ReturnType getReturnType() {
		return this.returnType;
	}

	/**
	 * Gets the function's parameters.
	 * @return The parameters.
	 */
	public List<Param> getParams() {
		return Collections.unmodifiableList(this.params);
	}

	/**
	 * Gets the function's possibly thrown exceptions.
	 * @return The possibly thrown exceptions.
	 */
	public List<Throws> getThrows() {
		return Collections.unmodifiableList(this.throwsList);
	}

	/**
	 * Matches the given argument types against this signature.
	 * @param argTypes - The argument types.
	 * @param env - The environment.
	 * @param allowUnmatchedArgs - Whether or not to allow more arguments than the signature prescribes.
	 * This should be {@code false} for normal functions, and {@code true} for procedures and closures.
	 * @return {@code true} if the arguments match the signature, {@code false} otherwise.
	 */
	public boolean matches(List<CClassType> argTypes, Environment env, boolean allowUnmatchedArgs) {
		int argIndex = 0;
		Stack<Integer> numArgMatchStack = new Stack<>();
		matchLoop:
		for(int paramIndex = 0; paramIndex < this.params.size(); paramIndex++) {
			Param param = this.params.get(paramIndex);

			// Match parameter.
			if(!param.isVarParam()) {

				// Match normal or optional parameter.
				if(argIndex < argTypes.size()
						&& InstanceofUtil.isInstanceof(argTypes.get(argIndex), param.getType(), env)) {

					// Keep track of the optional parameter match.
					if(param.isOptional()) {
						numArgMatchStack.push(1);
					}

					// Normal param match, continue with next param.
					argIndex++;
					continue;
				} else if(param.isOptional()) {

					// Mark as optional parameter match and continue with the next param.
					numArgMatchStack.push(0);
					continue;
				}
			} else {

				// Match as many arguments as possible with this varparam.
				int numMatches = 0;
				while(argIndex < argTypes.size()
						&& InstanceofUtil.isInstanceof(argTypes.get(argIndex), param.getType(), env)) {
					argIndex++;
					numMatches++;
				}
				numArgMatchStack.push(numMatches);
				continue;
			}

			// Param didn't match. Make the last varparam or optional match with matches match one less term.
			paramIndex--; // The current parameter doesn't have a match.
			while(!numArgMatchStack.empty()) {
				while(!this.params.get(paramIndex).isVarParam() && !this.params.get(paramIndex).isOptional()) {

					// Undo normal parameter match.
					paramIndex--;
					argIndex--;
				}
				int numArgMatches = numArgMatchStack.pop();
				if(numArgMatches > 0) {

					// Undo last argument match of the varparam.
					numArgMatchStack.push(numArgMatches - 1);
					argIndex--;

					// Retry matching.
					continue matchLoop;
				}
			}
			return false; // No varparams with matches remaining in the signature, so it's no match.
		}

		// All parameters are matched. Return true only if all arguments were also matched, or if this is not required.
		return allowUnmatchedArgs || argIndex >= argTypes.size();
	}

	/**
	 * Gets the parameter types string.
	 * @return The string in format "([firstArgType], secondArgType..., ...)".
	 */
	public String getParamTypesString() {
		return "(" + StringUtils.Join(this.params, ", ", null, null, null, (Param param) -> {
			String ret = param.getType().getSimpleName();
			if(param.getGenericIdentifier() != null) {
				ret = param.getGenericIdentifier();
				if(param.getType() != Mixed.TYPE) {
					ret += " extends " + ret;
				}
			}
			if(param.isVarParam()) {
				ret += "...";
			}
			if(param.isOptional()) {
				ret = "[" + ret + "]";
			}
			return ret;
		}) + ")";
	}
}
