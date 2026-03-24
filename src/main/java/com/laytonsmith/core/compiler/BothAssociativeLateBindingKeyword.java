package com.laytonsmith.core.compiler;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;

/**
 *
 */
public abstract class BothAssociativeLateBindingKeyword extends LateBindingKeyword {
	@Override
	public final LateBindingKeyword.Associativity getAssociativity() {
		return LateBindingKeyword.Associativity.BOTH;
	}

	protected abstract ParseTree process(Environment env, Target t, FileOptions fileOptions, ParseTree leftHandNode,
			ParseTree rightHandNode) throws ConfigCompileException;

	@Override
	public final ParseTree processLeftAssociative(Environment env, Target t, FileOptions fileOptions,
			ParseTree leftHandNode) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final ParseTree processRightAssociative(Environment env, Target t, FileOptions fileOptions,
			ParseTree rightHandNode) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final ParseTree processBothAssociative(Environment env, Target t, FileOptions fileOptions,
			ParseTree leftHandNode, ParseTree rightHandNode) throws ConfigCompileException {
		return process(env, t, fileOptions, leftHandNode, rightHandNode);
	}
}
