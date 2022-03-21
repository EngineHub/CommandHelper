package com.laytonsmith.core.compiler.signature;

import com.laytonsmith.core.compiler.signature.FunctionSignatures.MatchType;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;

/**
 * A builder for {@link FunctionSignatures}.
 * @author P.J.S. Kools
 */
public class SignatureBuilder {

	private final FunctionSignatures signatures;
	private FunctionSignature signature;

	/**
	 * Creates a new {@link SignatureBuilder}, initialized with a {@link FunctionSignature} with the given return type.
	 * When determining the return type for given argument types, all matching signatures will be used.
	 * @param returnType - The return type for the first {@link FunctionSignature}.
	 */
	public SignatureBuilder(CClassType returnType) {
		this(returnType, null, MatchType.MATCH_ALL);
	}

	/**
	 * Creates a new {@link SignatureBuilder}, initialized with a {@link FunctionSignature} with the given return type.
	 * @param returnType - The return type for the first {@link FunctionSignature}.
	 * @param matchType - The {@link MatchType} used for determining the return type for given argument types.
	 */
	public SignatureBuilder(CClassType returnType, MatchType matchType) {
		this(returnType, null, matchType);
	}

	/**
	 * Creates a new {@link SignatureBuilder}, initialized with a {@link FunctionSignature} with the given return type.
	 * When determining the return type for given argument types, all matching signatures will be used.
	 * @param returnType - The return type for the first {@link FunctionSignature}.
	 * @param returnValDesc - The return value description.
	 */
	public SignatureBuilder(CClassType returnType, String returnValDesc) {
		this(returnType, returnValDesc, MatchType.MATCH_ALL);
	}

	/**
	 * Creates a new {@link SignatureBuilder}, initialized with a {@link FunctionSignature} with the given return type.
	 * @param returnType - The return type for the first {@link FunctionSignature}.
	 * @param returnValDesc - The return value description.
	 * @param matchType - The {@link MatchType} used for determining the return type for given argument types.
	 */
	public SignatureBuilder(CClassType returnType, String returnValDesc, MatchType matchType) {
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
	public SignatureBuilder param(CClassType paramType, String paramName, String paramDesc, boolean isOptional) {
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
		return this.newSignature(returnType, null);
	}

	/**
	 * Finalizes the last function signature and starts a new function signature with the given return type.
	 * @param returnType - The return type of the new function signature.
	 * @param returnValDesc - The return value description.
	 * @return This {@link SignatureBuilder}, for chaining builder methods.
	 */
	public SignatureBuilder newSignature(CClassType returnType, String returnValDesc) {
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
