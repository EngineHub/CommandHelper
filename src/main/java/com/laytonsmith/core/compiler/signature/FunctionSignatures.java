package com.laytonsmith.core.compiler.signature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.laytonsmith.PureUtilities.Common.StringUtils;

/**
 * Represents a collection of signatures for a single function, procedure or closure.
 * @author P.J.S. Kools
 */
public final class FunctionSignatures {

	private final List<FunctionSignature> signatures = new ArrayList<>();

	protected FunctionSignatures() {
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
}
