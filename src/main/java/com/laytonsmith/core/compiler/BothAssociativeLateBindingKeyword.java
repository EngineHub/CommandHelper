package com.laytonsmith.core.compiler;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigCompileException;

/**
 *
 */
public abstract class BothAssociativeLateBindingKeyword extends LateBindingKeyword {
	@Override
	public final LateBindingKeyword.Associativity getAssociativity() {
		return LateBindingKeyword.Associativity.BOTH;
	}

	protected abstract ParseTree process(Target t, FileOptions fileOptions, ParseTree leftHandNode, ParseTree rightHandNode) throws ConfigCompileException;

	@Override
	public final ParseTree processLeftAssociative(Target t, FileOptions fileOptions, ParseTree leftHandNode) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final ParseTree processRightAssociative(Target t, FileOptions fileOptions, ParseTree rightHandNode) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final ParseTree processBothAssociative(Target t, FileOptions fileOptions, ParseTree leftHandNode, ParseTree rightHandNode) throws ConfigCompileException {
		return process(t, fileOptions, leftHandNode, rightHandNode);
	}
}
