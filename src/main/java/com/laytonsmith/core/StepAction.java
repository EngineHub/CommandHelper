package com.laytonsmith.core;

import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 * Represents an action that a function returns to the interpreter loop, telling it what to do next.
 * The interpreter loop understands three kinds of actions:
 * <ul>
 *   <li>{@link Evaluate} — evaluate a child node, then call back with the result</li>
 *   <li>{@link Complete} — this function is done, here's the result</li>
 *   <li>{@link FlowControl} — a control flow action is propagating up the stack</li>
 * </ul>
 *
 * <p>The interpreter loop does not know about specific flow control types (break, continue, return, etc.).
 * Those are defined by the functions that produce and consume them, via {@link FlowControlAction}.</p>
 */
public abstract class StepAction {

	private StepAction() {
	}

	/**
	 * Tells the interpreter loop to evaluate the given parse tree node. Once evaluation completes,
	 * the result is passed back to the current frame's {@link FlowFunction#childCompleted}.
	 *
	 * <p>If an environment is provided, the child frame will use that environment instead of
	 * inheriting the parent frame's environment. This is used by procedure calls, which evaluate
	 * their body in a cloned environment.</p>
	 */
	public static final class Evaluate extends StepAction {
		private final ParseTree node;
		private final Environment env;
		private final boolean keepIVariable;

		public Evaluate(ParseTree node) {
			this(node, null, false);
		}

		/**
		 * @param node The node to evaluate
		 * @param env The environment to evaluate in, or null to use the parent frame's environment
		 */
		public Evaluate(ParseTree node, Environment env) {
			this(node, env, false);
		}

		/**
		 * @param node The node to evaluate
		 * @param env The environment to evaluate in, or null to use the parent frame's environment
		 * @param keepIVariable If true, the result is returned as-is even if it's an IVariable.
		 *     If false (default), IVariables are resolved to their values before being passed
		 *     to childCompleted.
		 */
		public Evaluate(ParseTree node, Environment env, boolean keepIVariable) {
			this.node = node;
			this.env = env;
			this.keepIVariable = keepIVariable;
		}

		public ParseTree getNode() {
			return node;
		}

		/**
		 * Returns the environment to evaluate in, or null to use the parent frame's environment.
		 */
		public Environment getEnv() {
			return env;
		}

		/**
		 * Returns true if IVariable results should be kept as-is rather than resolved.
		 */
		public boolean keepIVariable() {
			return keepIVariable;
		}
	}

	/**
	 * Tells the interpreter loop that the current function is done, and provides its result value.
	 */
	public static final class Complete extends StepAction {
		private final Mixed result;

		public Complete(Mixed result) {
			this.result = result;
		}

		public Mixed getResult() {
			return result;
		}
	}

	/**
	 * Tells the interpreter loop that a control flow action is propagating up the stack.
	 * The loop will pass this to each frame's {@link FlowFunction#childInterrupted} as it
	 * unwinds, until a frame handles it or it reaches the top of the stack.
	 *
	 * <p>The interpreter loop does not inspect the {@link FlowControlAction} — specific flow control
	 * types (break, continue, return, throw, etc.) are defined alongside the functions that
	 * produce and consume them.</p>
	 */
	public static final class FlowControl extends StepAction {
		private final FlowControlAction action;

		public FlowControl(FlowControlAction action) {
			this.action = action;
		}

		public FlowControlAction getAction() {
			return action;
		}
	}

	/**
	 * Marker interface for control flow actions that propagate up the interpreter stack.
	 * Concrete implementations are defined alongside the functions that produce/consume them
	 * (e.g., BreakAction lives near _break in ControlFlow.java).
	 *
	 * <p>The interpreter loop treats all FlowControlActions generically — it does not know about
	 * specific types. This allows extensions to define custom control flow without modifying core.</p>
	 */
	public interface FlowControlAction {
		/**
		 * Returns the code location where this action originated.
		 */
		Target getTarget();
	}

	/**
	 * Pairs a {@link StepAction} with the flow function's per-call state. Returned by
	 * {@link FlowFunction} methods so the interpreter loop can store the state
	 * on the {@link StackFrame} without knowing its type.
	 *
	 * @param <S> The flow function's state type
	 */
	public static final class StepResult<S> {
		private final StepAction action;
		private final S state;

		public StepResult(StepAction action, S state) {
			this.action = action;
			this.state = state;
		}

		public StepAction getAction() {
			return action;
		}

		public S getState() {
			return state;
		}
	}
}
