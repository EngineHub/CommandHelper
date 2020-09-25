package com.laytonsmith.core.compiler.signature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
}
