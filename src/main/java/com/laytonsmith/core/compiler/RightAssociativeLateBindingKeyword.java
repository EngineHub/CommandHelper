package com.laytonsmith.core.compiler;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.exceptions.ConfigCompileException;

/**
 *
 */
public abstract class RightAssociativeLateBindingKeyword extends LateBindingKeyword {

	@Override
	public final LateBindingKeyword.Associativity getAssociativity() {
		return LateBindingKeyword.Associativity.RIGHT;
	}

	protected abstract ParseTree process(ParseTree leftHandNode) throws ConfigCompileException;

	@Override
	public final ParseTree processLeftAssociative(ParseTree leftHandNode) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final ParseTree processRightAssociative(ParseTree rightHandNode) throws ConfigCompileException {
		return process(rightHandNode);
	}

	@Override
	public final ParseTree processBothAssociative(ParseTree leftHandNode, ParseTree rightHandNode) {
		throw new UnsupportedOperationException();
	}
}
