package com.laytonsmith.core.exceptions;

import com.laytonsmith.core.StepAction;

/**
 * Thrown by the iterative interpreter when a {@link StepAction.FlowControl} action
 * propagates to the top of the stack without being handled by any frame. The top-level
 * caller (e.g., {@code Script.run()}) catches this and dispatches based on the action
 * type, matching the behavior of the old exception-based system.
 */
public class UnhandledFlowControlException extends RuntimeException {

	private final StepAction.FlowControlAction action;

	public UnhandledFlowControlException(StepAction.FlowControlAction action) {
		this.action = action;
	}

	public StepAction.FlowControlAction getAction() {
		return action;
	}

	@Override
	public Throwable fillInStackTrace() {
		return this;
	}
}
