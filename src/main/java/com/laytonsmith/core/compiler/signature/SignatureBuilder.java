package com.laytonsmith.core.compiler.signature;

import com.laytonsmith.core.compiler.signature.FunctionSignatures.MatchType;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.generics.GenericDeclaration;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;

/**
 * A builder for {@link FunctionSignatures}.
 * @author P.J.S. Kools
 */
public class SignatureBuilder {

	private final FunctionSignatures signatures;
	private FunctionSignature signature;

	public static SignatureBuilder withNoneReturnType() {
		return new SignatureBuilder((LeftHandSideType) null);
	}

	/**
	 * Creates a new {@link SignatureBuilder}, initialized with a {@link FunctionSignature} with the given return type.
	 * When determining the return type for given argument types, all matching signatures will be used.
	 * @param returnType - The return type for the first {@link FunctionSignature}.
	 */
	public SignatureBuilder(CClassType returnType) {
		this(returnType.asLeftHandSideType());
	}

	/**
	 * Creates a new {@link SignatureBuilder}, initialized with a {@link FunctionSignature} with the given return type.
	 * When determining the return type for given argument types, all matching signatures will be used.
	 * @param returnType - The return type for the first {@link FunctionSignature}.
	 */
	public SignatureBuilder(LeftHandSideType returnType) {
		this(returnType, null, MatchType.MATCH_ALL);
	}

	/**
	 * Creates a new {@link SignatureBuilder}, initialized with a {@link FunctionSignature} with the given return type.
	 * @param returnType - The return type for the first {@link FunctionSignature}.
	 * @param matchType - The {@link MatchType} used for determining the return type for given argument types.
	 */
	public SignatureBuilder(CClassType returnType, MatchType matchType) {
		this(returnType.asLeftHandSideType(), matchType);
	}

	/**
	 * Creates a new {@link SignatureBuilder}, initialized with a {@link FunctionSignature} with the given return type.
	 * @param returnType - The return type for the first {@link FunctionSignature}.
	 * @param matchType - The {@link MatchType} used for determining the return type for given argument types.
	 */
	public SignatureBuilder(LeftHandSideType returnType, MatchType matchType) {
		this(returnType, null, matchType);
	}

	/**
	 * Creates a new {@link SignatureBuilder}, initialized with a {@link FunctionSignature} with the given return type.
	 * When determining the return type for given argument types, all matching signatures will be used.
	 * @param returnType - The return type for the first {@link FunctionSignature}.
	 * @param returnValDesc - The return value description.
	 */
	public SignatureBuilder(CClassType returnType, String returnValDesc) {
		this(returnType.asLeftHandSideType(), returnValDesc);
	}

	/**
	 * Creates a new {@link SignatureBuilder}, initialized with a {@link FunctionSignature} with the given return type.
	 * When determining the return type for given argument types, all matching signatures will be used.
	 * @param returnType - The return type for the first {@link FunctionSignature}.
	 * @param returnValDesc - The return value description.
	 */
	public SignatureBuilder(LeftHandSideType returnType, String returnValDesc) {
		this(returnType, returnValDesc, MatchType.MATCH_ALL);
	}

	/**
	 * Creates a new {@link SignatureBuilder}, initialized with a {@link FunctionSignature} with the given return type.
	 * @param returnType - The return type for the first {@link FunctionSignature}.
	 * @param returnValDesc - The return value description.
	 * @param matchType - The {@link MatchType} used for determining the return type for given argument types.
	 */
	public SignatureBuilder(CClassType returnType, String returnValDesc, MatchType matchType) {
		this(returnType.asLeftHandSideType(), returnValDesc, matchType);
	}

	/**
	 * Creates a new {@link SignatureBuilder}, initialized with a {@link FunctionSignature} with the given return type.
	 * @param returnType - The return type for the first {@link FunctionSignature}.
	 * @param returnValDesc - The return value description.
	 * @param matchType - The {@link MatchType} used for determining the return type for given argument types.
	 */
	public SignatureBuilder(LeftHandSideType returnType, String returnValDesc, MatchType matchType) {
		this.signatures = new FunctionSignatures(matchType);
		this.signature = new FunctionSignature(new ReturnType(returnType, returnValDesc));
	}

	/**
	 * Adds a normal function parameter. Parameters should be added from left to right.
	 * @param paramType - The {@link CClassType} of the parameter.
	 * @param paramName - The name of the parameter.
	 * @param paramDesc - The description of the parameter.
	 * @param isOptional - Whether the parameter is optional or not.
	 * @return This {@link SignatureBuilder}, for chaining builder methods.
	 */
	public SignatureBuilder param(LeftHandSideType paramType, String paramName, String paramDesc, boolean isOptional) {
		this.signature.addParam(new Param(paramType, paramName, paramDesc, false, isOptional));
		return this;
	}

	/**
	 * Adds a normal non-optional function parameter. Parameters should be added from left to right.
	 * @param paramType - The {@link CClassType} of the parameter.
	 * @param paramName - The name of the parameter.
	 * @param paramDesc - The description of the parameter.
	 * @return This {@link SignatureBuilder}, for chaining builder methods.
	 */
	public SignatureBuilder param(LeftHandSideType paramType, String paramName, String paramDesc) {
		return this.param(paramType, paramName, paramDesc, false);
	}

	/**
	 * Adds a normal function parameter. Parameters should be added from left to right.
	 * @param paramType - The {@link CClassType} of the parameter.
	 * @param paramName - The name of the parameter.
	 * @param paramDesc - The description of the parameter.
	 * @param isOptional - Whether the parameter is optional or not.
	 * @return This {@link SignatureBuilder}, for chaining builder methods.
	 */
	public SignatureBuilder param(CClassType paramType, String paramName, String paramDesc, boolean isOptional) {
		this.signature.addParam(new Param(paramType == null ? null : paramType.asLeftHandSideType(), paramName, paramDesc, false, isOptional));
		return this;
	}

	/**
	 * Adds a normal non-optional function parameter. Parameters should be added from left to right.
	 * @param paramType - The {@link CClassType} of the parameter.
	 * @param paramName - The name of the parameter.
	 * @param paramDesc - The description of the parameter.
	 * @return This {@link SignatureBuilder}, for chaining builder methods.
	 */
	public SignatureBuilder param(CClassType paramType, String paramName, String paramDesc) {
		return this.param(paramType, paramName, paramDesc, false);
	}

	/**
	 * Adds a variadic function parameter (varparam). Parameters should be added from left to right.
	 * @param paramType - The {@link CClassType} of the parameter (the type in 'paramType paramName...').
	 * @param paramName - The name of the parameter.
	 * @param paramDesc - The description of the parameter.
	 * @return This {@link SignatureBuilder}, for chaining builder methods.
	 */
	public SignatureBuilder varParam(CClassType paramType, String paramName, String paramDesc) {
		return varParam(paramType.asLeftHandSideType(), paramName, paramDesc);
	}

	/**
	 * Adds a variadic function parameter (varparam). Parameters should be added from left to right.
	 * @param paramType - The {@link CClassType} of the parameter (the type in 'paramType paramName...').
	 * @param paramName - The name of the parameter.
	 * @param paramDesc - The description of the parameter.
	 * @return This {@link SignatureBuilder}, for chaining builder methods.
	 */
	public SignatureBuilder varParam(LeftHandSideType paramType, String paramName, String paramDesc) {
		this.signature.addParam(new Param(paramType, paramName, paramDesc, true, false));
		return this;
	}

	/**
	 * Adds a possibly thrown exception to the function signature.
	 * @param exception - The class representing the possibly thrown exception.
	 * @param when - The condition under which the exception can be thrown.
	 * @return This {@link SignatureBuilder}, for chaining builder methods.
	 */
	public SignatureBuilder throwsEx(Class<? extends CREThrowable> exception, String when) {
		this.signature.addThrows(new Throws(exception, when));
		return this;
	}

	public SignatureBuilder setGenericDeclaration(GenericDeclaration genericDeclaration, String docs) {
		this.signature.setGenericDeclaration(genericDeclaration, docs);
		return this;
	}

	/**
	 * Sets whether or not values of the {@code none} type are allowed to be passed in here. In general, this
	 * should be false, as code such as {@code add(die(), die())} is almost certainly an error, however, branch
	 * type functions, such as if, loops, and even less direct ones such as dor should accept.
	 * @param allowed
	 * @return
	 */
	public SignatureBuilder setNoneIsAllowed(boolean allowed) {
		this.signature.setNoneIsAllowed(allowed);
		return this;
	}



	/**
	 * Finalizes the last function signature and starts a new function signature with the given return type.
	 * @param returnType - The return type of the new function signature.
	 * @return This {@link SignatureBuilder}, for chaining builder methods.
	 */
	public SignatureBuilder newSignature(CClassType returnType) {
		return newSignature(returnType.asLeftHandSideType());
	}

	/**
	 * Finalizes the last function signature and starts a new function signature with the given return type.
	 * @param returnType - The return type of the new function signature.
	 * @return This {@link SignatureBuilder}, for chaining builder methods.
	 */
	public SignatureBuilder newSignature(LeftHandSideType returnType) {
		return this.newSignature(returnType, null);
	}

	/**
	 * Finalizes the last function signature and starts a new function signature with the given return type.
	 * @param returnType - The return type of the new function signature.
	 * @param returnValDesc - The return value description.
	 * @return This {@link SignatureBuilder}, for chaining builder methods.
	 */
	public SignatureBuilder newSignature(CClassType returnType, String returnValDesc) {
		return newSignature(returnType.asLeftHandSideType(), returnValDesc);
	}

	/**
	 * Finalizes the last function signature and starts a new function signature with the given return type.
	 * @param returnType - The return type of the new function signature.
	 * @param returnValDesc - The return value description.
	 * @return This {@link SignatureBuilder}, for chaining builder methods.
	 */
	public SignatureBuilder newSignature(LeftHandSideType returnType, String returnValDesc) {
		return this.newSignature(new ReturnType(returnType, returnValDesc));
	}

	private SignatureBuilder newSignature(ReturnType returnType) {

		// Add current signature.
		this.signatures.addSignature(this.signature);

		// Create new signature.
		this.signature = new FunctionSignature(returnType);

		return this;
	}

	/**
	 * Builds a {@link FunctionSignatures} from the information provided to this builder.
	 * @return The resulting {@link FunctionSignatures}.
	 */
	public FunctionSignatures build() {

		// Add current signature.
		this.signatures.addSignature(this.signature);

		// Return the signatures.
		return this.signatures;
	}
}
