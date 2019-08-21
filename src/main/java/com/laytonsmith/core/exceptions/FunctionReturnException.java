package com.laytonsmith.core.exceptions;

import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 *
 */
public class FunctionReturnException extends ProgramFlowManipulationException {

	Mixed ret;

	public FunctionReturnException(Mixed ret, Target t) {
		super(t);
		this.ret = ret;
	}

	public Mixed getReturn() {
		return ret;
	}
}
