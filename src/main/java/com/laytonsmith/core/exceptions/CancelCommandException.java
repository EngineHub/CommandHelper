package com.laytonsmith.core.exceptions;

import com.laytonsmith.core.constructs.Target;

/**
 *
 *
 */
public class CancelCommandException extends ProgramFlowManipulationException {

	String message;

	public CancelCommandException(String message, Target t) {
		super(t);
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}

}
