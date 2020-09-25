package com.laytonsmith.core.compiler.signature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
}
