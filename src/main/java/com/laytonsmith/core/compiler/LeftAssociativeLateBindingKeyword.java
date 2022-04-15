package com.laytonsmith.core.compiler;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.exceptions.ConfigCompileException;

/**
 *
 */
public abstract class LeftAssociativeLateBindingKeyword extends LateBindingKeyword {

	@Override
	public final Associativity getAssociativity() {
		return Associativity.LEFT;
	}

	protected abstract ParseTree process(ParseTree leftHandNode) throws ConfigCompileException;

	@Override
	public final ParseTree processLeftAssociative(ParseTree leftHandNode) throws ConfigCompileException {
		return process(leftHandNode);
	}

	@Override
	public final ParseTree processRightAssociative(ParseTree rightHandNode) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final ParseTree processBothAssociative(ParseTree leftHandNode, ParseTree rightHandNode) {
		throw new UnsupportedOperationException();
	}

}
