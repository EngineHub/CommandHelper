package com.laytonsmith.core;

import com.laytonsmith.core.StepAction.StepResult;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.environments.Environment;

/**
 * Interface for functions that need to control how their children are evaluated.
 * This replaces the old {@code execs()} mechanism. Instead of a function calling
 * {@code parent.eval()} recursively, the interpreter loop calls the FlowFunction methods
 * and the function returns {@link StepAction} values to direct evaluation.
 *
 * <p>Functions that don't need special child evaluation (the majority) don't implement
 * this interface — the interpreter loop evaluates all their children left-to-right,
 * then calls {@code exec()} with the results.</p>
 *
 * <p>Functions that DO need it (if, for, and, try, etc.) implement this interface
 * and are driven by the interpreter loop via begin/childCompleted/childInterrupted.</p>
 *
 * <p>Functions that execute Callables MUST use this mechanism, however,
 * in most cases, it is sufficient to implement {@link CallbackYield} instead, which
 * is a specialized overload of this class, which hides most of the complexity
 * in the case where the only complexity is calling Callables.</p>
 *
 * <p>The type parameter {@code S} is the per-call state type. Since function instances
 * are singletons, per-call mutable state cannot be stored on the function itself.
 * Instead, methods receive and return state via {@link StepAction.StepResult}.
 * The interpreter stores this state on the {@link StackFrame} as {@code Object} and
 * passes it back (with an unchecked cast) on subsequent calls. Functions that need
 * no per-call state should use {@code Void} and pass {@code null}.</p>
 */
public interface FlowFunction<S> {

	/**
	 * Called when this function frame is first entered. The function should return
	 * a {@link StepAction.StepResult} containing the first action (typically
	 * {@link StepAction.Evaluate}) and the initial per-call state.
	 *
	 * @param t The code target of the function call
	 * @param children The unevaluated child parse trees (same as what execs() received)
	 * @param env The current environment
	 * @return The first step action paired with initial state
	 */
	StepResult<S> begin(Target t, ParseTree[] children, Environment env);

	/**
	 * Called each time a child evaluation (requested via {@link StepAction.Evaluate})
	 * completes successfully. The function receives the result and its per-call state,
	 * and returns the next action paired with updated state.
	 *
	 * @param t The code target of the function call
	 * @param state The per-call state from the previous step
	 * @param result The result of the child evaluation
	 * @param env The current environment
	 * @return The next step action paired with updated state
	 */
	StepResult<S> childCompleted(Target t, S state, Mixed result, Environment env);

	/**
	 * Called when a child evaluation produced a {@link StepAction.FlowControl} action
	 * that is propagating up the stack. The function can choose to handle it (e.g.,
	 * a loop handling a break action) or let it propagate by returning {@code null}.
	 *
	 * <p>For example, {@code _for}'s implementation handles {@code BreakAction} by completing
	 * the loop, and handles {@code ContinueAction} by jumping to the increment step.
	 * {@code _try}'s implementation handles {@code ThrowAction} by switching to the catch branch.</p>
	 *
	 * <p>The default implementation returns {@code null}, propagating the action up.</p>
	 *
	 * @param t The code target of the function call
	 * @param state The per-call state from the previous step
	 * @param action The flow control action propagating through this frame
	 * @param env The current environment
	 * @return A {@link StepAction.StepResult} to handle it, or {@code null} to propagate
	 */
	default StepResult<S> childInterrupted(Target t, S state, StepAction.FlowControl action, Environment env) {
		return null;
	}

	/**
	 * Called when this function's frame is being removed from the stack, regardless of
	 * the reason (normal completion, flow control propagation, or exception). This is
	 * the FlowFunction equivalent of a {@code finally} block — use it to restore
	 * environment state that was modified in {@code begin()} (e.g., command sender,
	 * dynamic scripting mode, stack trace elements).
	 *
	 * <p>This is called exactly once per frame, after the final action has been determined
	 * but before the frame is actually popped. The default implementation is a no-op.</p>
	 *
	 * @param t The code target of the function call
	 * @param state The per-call state (may be null if begin() hasn't been called)
	 * @param env The current environment
	 */
	default void cleanup(Target t, S state, Environment env) {
	}

	/**
	 * Returns whether the given exception would be caught by this function,
	 * given the current per-call state. The default returns false. Override in
	 * exception-catching functions (e.g. try/catch) to inspect the state and
	 * determine if the exception matches a catch clause.
	 *
	 * <p>This is used by the debugger to determine if an exception is "uncaught"
	 * by inspecting the eval stack without actually propagating the exception.</p>
	 *
	 * @param state The per-call state from the stack frame
	 * @param exception The exception to check
	 * @return true if this function would catch the exception in its current state
	 */
	default boolean wouldCatch(S state, ConfigRuntimeException exception) {
		return false;
	}
}
