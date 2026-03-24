package com.laytonsmith.core.compiler;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;

/**
 *
 */
public abstract class LeftAssociativeLateBindingKeyword extends LateBindingKeyword {

	@Override
	public final Associativity getAssociativity() {
		return Associativity.LEFT;
	}

	protected abstract ParseTree process(Environment env, Target t, FileOptions fileOptions,
			ParseTree leftHandNode) throws ConfigCompileException;

	@Override
	public final ParseTree processLeftAssociative(Environment env, Target t, FileOptions fileOptions,
			ParseTree leftHandNode) throws ConfigCompileException {
		return process(env, t, fileOptions, leftHandNode);
	}

	@Override
	public final ParseTree processRightAssociative(Environment env, Target t, FileOptions fileOptions,
			ParseTree rightHandNode) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final ParseTree processBothAssociative(Environment env, Target t, FileOptions fileOptions,
			ParseTree leftHandNode, ParseTree rightHandNode) {
		throw new UnsupportedOperationException();
	}

}
