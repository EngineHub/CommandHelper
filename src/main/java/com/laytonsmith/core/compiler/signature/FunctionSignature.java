package com.laytonsmith.core.compiler.signature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.core.constructs.InstanceofUtil;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.GenericDeclaration;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.environments.Environment;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single function, procedure or closure signature.
 * @author P.J.S. Kools
 */
public class FunctionSignature {

	private final ReturnType returnType;
	private final List<Param> params;
	private final List<Throws> throwsList;
	private GenericDeclaration genericDeclaration;
	private String genericDeclarationDocs;
	private boolean noneIsAllowed;

	/**
	 * Creates a new {@link FunctionSignature} with the given properties.
	 * @param returnType - The function return type.
	 * @param params - The function parameters.
	 * @param throwsList - The list of possibly thrown exceptions by the function.
	 * @param methodGenericDeclaration The GenericDeclaration for the method. For signatures that do not have
	 * generics, this may be null. GenericDeclarations are what are verified against when type parameters are
	 * passed to a given function.
	 * @param noneIsAllowed If the none type is allowed. If so, none is treated as auto.
	 */
	public FunctionSignature(ReturnType returnType, List<Param> params, List<Throws> throwsList,
			GenericDeclaration methodGenericDeclaration, boolean noneIsAllowed) {
		this.returnType = returnType;
		this.params = params;
		this.throwsList = throwsList;
		this.genericDeclaration = methodGenericDeclaration;
		this.noneIsAllowed = noneIsAllowed;
	}

	/**
	 * Creates a new {@link FunctionSignature} with the given return type and no parameters,
	 * generic declaration, or possibly thrown exceptions.
	 * @param returnType - The function return type.
	 */
	public FunctionSignature(ReturnType returnType) {
		this(returnType, new ArrayList<>(), new ArrayList<>(), null, false);
	}

	protected void addParam(Param param) {
		this.params.add(param);
	}

	protected void addThrows(Throws throwsObj) {
		this.throwsList.add(throwsObj);
	}

	protected void setGenericDeclaration(GenericDeclaration methodGenericDeclaration, String docs) {
		this.genericDeclaration = methodGenericDeclaration;
		this.genericDeclarationDocs = docs;
	}

	protected void setNoneIsAllowed(boolean noneIsAllowed) {
		this.noneIsAllowed = noneIsAllowed;
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
	 * Returns the generic declaration for this method. This declaration is validated against for type parameters
	 * sent in, either at compile time (if Static Analysis is enabled) or at runtime, and thus does can be
	 * relied on to be correct within the function exec method.
	 * @return The definition, or null, if the function does not have one.
	 */
	public GenericDeclaration getGenericDeclaration() {
		return this.genericDeclaration;
	}

	/**
	 * Returns the docs specified in the builder. May be null.
	 * @return
	 */
	public String getGenericDeclarationDocs() {
		return this.genericDeclarationDocs;
	}

	/**
	 * Given an argument set, which may contain typenames, resolves all of them based on the actual arg types that
	 * were passed in, comparing them to the generic parameters.These are the same values sent to {@link #matches}.
	 * @param t
	 * @param argTypes The argument types.
	 * @param generics The generic parameters passed to the function. These are necessary to resolve type names.
	 * @param inferredReturnType The inferred types of the function call. This may be null, but is used to
	 * infer types of generic typenames.
	 * @param env The environment.
	 * @return A Map containing the appropriate mappings from typename to type, or an empty map if there are no
	 * typename parameters.
	 */
	public Map<String, LeftHandSideType> getTypeResolutions(Target t, List<LeftHandSideType> argTypes, GenericParameters generics,
			LeftHandSideType inferredReturnType, Environment env) {
		// In general, parameter types have precedence over return type, so if the return type is a typename, we go
		// ahead and set it, but then overwrite it with the parameter of the same type.
		Map<String, LeftHandSideType> ret = new HashMap<>();
		if(returnType.getType() != null && returnType.getType().isTypeName()) {
			ret.put(returnType.getType().getTypename(), inferredReturnType);
		}
		// TODO: Would be nice to figure out how to not basically copy paste this
		int argIndex = 0;
		Stack<Integer> numArgMatchStack = new Stack<>();
		matchLoop:
		for(int paramIndex = 0; paramIndex < this.params.size(); paramIndex++) {
			Param param = this.params.get(paramIndex);

			LeftHandSideType argType = null;
			if(argIndex < argTypes.size()) {
				argType = LeftHandSideType.resolveTypeFromGenerics(t, env, argTypes.get(argIndex),
					generics, genericDeclaration, (LeftHandSideType) null);
			}
			Map<String, LeftHandSideType> inferredTypes = new HashMap<>();
			if(param.getType() != null && param.getType().isTypeName()) {
				inferredTypes.put(param.getType().getTypename(), argType);
			}
			LeftHandSideType paramType = LeftHandSideType.resolveTypeFromGenerics(t, env, param.getType(),
					generics, genericDeclaration, inferredTypes);
			// Match parameter.
			if(!param.isVarParam()) {

				// Match normal or optional parameter.
				if(argIndex < argTypes.size()
						&& ((argType == null && noneIsAllowed)
						|| (paramType == null
							|| InstanceofUtil.isAssignableTo(argType, paramType, env)))) {

					// Keep track of the optional parameter match.
					if(param.isOptional()) {
						numArgMatchStack.push(1);
					}

					// Normal param match, continue with next param.
					if(param.getType() != null && param.getType().isTypeName()) {
						ret.put(param.getType().getTypename(), argType);
					}
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
						&& (
						(argType == null && noneIsAllowed)
						|| InstanceofUtil.isInstanceof(argType, paramType, env))) {
					if(param.getType() != null && param.getType().isTypeName()) {
						ret.put(param.getType().getTypename(), argType);
					}
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
		}
		return ret;
	}

	/**
	 * Matches the given argument types against this signature.
	 * @param argTypes The argument types.
	 * @param generics The generic parameters passed to the function. These are necessary to resolve type names.
	 * @param env The environment.
	 * @param inferredReturnType The inferred types of the function call. This may be null, but is used to
	 * infer types of generic typenames.
	 * @param allowUnmatchedArgs Whether or not to allow more arguments than the signature prescribes.
	 * This should be {@code false} for normal functions, and {@code true} for procedures and closures.
	 * @return {@code true} if the arguments match the signature, {@code false} otherwise.
	 */
	public boolean matches(List<LeftHandSideType> argTypes, GenericParameters generics, Environment env,
			LeftHandSideType inferredReturnType, boolean allowUnmatchedArgs) {
		int argIndex = 0;
		Stack<Integer> numArgMatchStack = new Stack<>();
		Target t = Target.UNKNOWN;
		matchLoop:
		for(int paramIndex = 0; paramIndex < this.params.size(); paramIndex++) {
			Param param = this.params.get(paramIndex);

			LeftHandSideType argType = null;
			if(argIndex < argTypes.size()) {
				argType = LeftHandSideType.resolveTypeFromGenerics(t, env, argTypes.get(argIndex),
					generics, genericDeclaration, (LeftHandSideType) null);
			}
			Map<String, LeftHandSideType> inferredTypes = new HashMap<>();
			if(param.getType() != null && param.getType().isTypeName()) {
				inferredTypes.put(param.getType().getTypename(), argType);
			}
			if(generics != null && genericDeclaration != null) {
				if(generics.getParameters().size() != genericDeclaration.getParameterCount()) {
					// Different parameter counts for the generics, so no match on this signature.
					return false;
				}
			}
			LeftHandSideType paramType = LeftHandSideType.resolveTypeFromGenerics(t, env, param.getType(),
					generics, genericDeclaration, inferredTypes);
			// Match parameter.
			if(!param.isVarParam()) {

				// Match normal or optional parameter.
				if(argIndex < argTypes.size()
						&& ((argType == null && noneIsAllowed)
						|| (paramType == null
							|| InstanceofUtil.isAssignableTo(argType, paramType, env)))) {

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
						&& (
						(argType == null && noneIsAllowed)
						|| InstanceofUtil.isInstanceof(argType, paramType, env))) {
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
			String ret = (param.getType() == null ? "any" : param.getType().getSimpleName());
			if(param.isVarParam()) {
				ret += "...";
			}
			if(param.isOptional()) {
				ret = "[" + ret + "]";
			}
			return ret;
		}) + ")";
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(returnType.getType() == null ? "none" : returnType.getType().val());
		b.append("(");
		b.append(StringUtils.Join(params, ", "));
		b.append(")");
		return b.toString();
	}


}
