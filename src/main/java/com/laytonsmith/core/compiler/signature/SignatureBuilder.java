package com.laytonsmith.core.compiler.signature;

import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;

/**
 * A builder for {@link FunctionSignatures}.
 * @author P.J.S. Kools
 */
public class SignatureBuilder {

	private final FunctionSignatures signatures = new FunctionSignatures();
	private FunctionSignature signature;

	/**
	 * Creates a new {@link SignatureBuilder}, initialized with a {@link FunctionSignature} with the given return type.
	 * @param returnType - The return type for the first {@link FunctionSignature}.
	 */
	public SignatureBuilder(CClassType returnType) {
		this.signature = new FunctionSignature(new ReturnType(returnType));
	}

	/**
	 * Creates a new {@link SignatureBuilder}, initialized with a {@link FunctionSignature} with the given
	 * generic return type.
	 * Generic return types are in format 'genericIdentifier extends returnType' or 'genericIdentifier', where the
	 * latter implies 'genericIdentifier extends mixed'.
	 * @param genericTypeName - The generic type name, used for matching with other generic types in the same signature.
	 * @param returnType - The parent {@link CClassType} of the return type for the first {@link FunctionSignature}.
	 */
	public SignatureBuilder(String genericTypeName, CClassType returnType) {
		this.signature = new FunctionSignature(new ReturnType(genericTypeName, returnType));
	}

	/**
	 * Adds a normal function parameter. Parameters should be added from left to right.
	 * @param paramType - The {@link CClassType} of the parameter.
	 * @param paramName - The name of the parameter.
	 * @param isOptional - Whether the parameter is optional or not.
	 * @return This {@link SignatureBuilder}, for chaining builder methods.
	 */
	public SignatureBuilder param(CClassType paramType, String paramName, boolean isOptional) {
		this.signature.addParam(new Param(null, paramType, paramName, false, isOptional));
		return this;
	}

	/**
	 * Adds a normal non-optional function parameter. Parameters should be added from left to right.
	 * @param paramType - The {@link CClassType} of the parameter.
	 * @param paramName - The name of the parameter.
	 * @return This {@link SignatureBuilder}, for chaining builder methods.
	 */
	public SignatureBuilder param(CClassType paramType, String paramName) {
		return this.param(paramType, paramName, false);
	}

	/**
	 * Adds a variable function parameter (varparam). Parameters should be added from left to right.
	 * @param paramType - The {@link CClassType} of the parameter (the type in 'paramType paramName...').
	 * @param paramName - The name of the parameter.
	 * @return This {@link SignatureBuilder}, for chaining builder methods.
	 */
	public SignatureBuilder varParam(CClassType paramType, String paramName) {
		this.signature.addParam(new Param(null, paramType, paramName, true, false));
		return this;
	}

	/**
	 * Adds a generic function parameter (in format 'genericTypeName extends genericTypeParent paramName').
	 * Parameters should be added from left to right.
	 * @param genericTypeName - The generic type name, used for matching with other generic types in the same signature.
	 * @param genericTypeParent - The parent {@link CClassType} of the parameter.
	 * This should be {@link Mixed} if the 'genericTypeName paramName' format is provided.
	 * @param paramName - The name of the parameter.
	 * @param isOptional - Whether the parameter is optional or not.
	 * @return This {@link SignatureBuilder}, for chaining builder methods.
	 */
	public SignatureBuilder genericParam(
			String genericTypeName, CClassType genericTypeParent, String paramName, boolean isOptional) {
		this.signature.addParam(new Param(genericTypeName, genericTypeParent, paramName, false, isOptional));
		return this;
	}

	/**
	 * Adds a generic non-optional function parameter (in format 'genericTypeName extends genericTypeParent paramName').
	 * Parameters should be added from left to right.
	 * @param genericTypeName - The generic type name, used for matching with other generic types in the same signature.
	 * @param genericTypeParent - The parent {@link CClassType} of the parameter.
	 * This should be {@link Mixed} if the 'genericTypeName paramName' format is provided.
	 * @param paramName - The name of the parameter.
	 * @return This {@link SignatureBuilder}, for chaining builder methods.
	 */
	public SignatureBuilder genericParam(String genericTypeName, CClassType genericTypeParent, String paramName) {
		return this.genericParam(genericTypeName, genericTypeParent, paramName, false);
	}

	/**
	 * Adds a non-optional generic variable function parameter
	 * (in format 'genericTypeName extends genericTypeParent paramName...').
	 * Parameters should be added from left to right.
	 * @param genericTypeName - The generic type name, used for matching with other generic types in the same signature.
	 * @param genericTypeParent - The parent {@link CClassType} of the parameter.
	 * This should be {@link Mixed} if the 'genericTypeName paramName...' format is provided.
	 * @param paramName - The name of the parameter.
	 * @return This {@link SignatureBuilder}, for chaining builder methods.
	 */
	public SignatureBuilder genericVarParam(
			String genericTypeName, CClassType genericTypeParent, String paramName) {
		this.signature.addParam(new Param(genericTypeName, genericTypeParent, paramName, true, false));
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

	/**
	 * Finalizes the last function signature and starts a new function signature with the given generic return type.
	 * Generic return types are in format 'genericIdentifier extends returnType' or 'genericIdentifier', where the
	 * latter implies 'genericIdentifier extends mixed'.
	 * @param genericTypeName - The generic type name, used for matching with other generic types in the same signature.
	 * @param returnType - The parent {@link CClassType} of the return type.
	 * @return This {@link SignatureBuilder}, for chaining builder methods.
	 */
	public SignatureBuilder newGenericSignature(String genericTypeName, CClassType returnType) {
		return this.newSignature(new ReturnType(genericTypeName, returnType));
	}

	/**
	 * Finalizes the last function signature and starts a new function signature with the given return type.
	 * @param returnType - The return type of the new function signature.
	 * @return This {@link SignatureBuilder}, for chaining builder methods.
	 */
	public SignatureBuilder newSignature(CClassType returnType) {
		return this.newSignature(new ReturnType(returnType));
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
