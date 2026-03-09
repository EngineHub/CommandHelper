package com.laytonsmith.core;

import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.AbstractFunction;
import com.laytonsmith.core.functions.ControlFlow;
import com.laytonsmith.core.natives.interfaces.Callable;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Base class for functions that need to call closures/callables without re-entering
 * {@code eval()}. Subclasses implement {@link #execWithYield} instead of {@code exec()}.
 * The callback-style exec builds a chain of deferred steps via a {@link Yield} object,
 * which this class then drives as a {@link FlowFunction}.
 *
 * <p>The interpreter loop sees this as a FlowFunction and drives it via
 * begin/childCompleted/childInterrupted. The subclass never deals with those
 * methods — it just uses the Yield API.</p>
 *
 * <p>Example (array_map):</p>
 * <pre>
 * protected void execCallback(Target t, Environment env, Mixed[] args, Yield yield) {
 *     CArray array = ArgumentValidation.getArray(args[0], t, env);
 *     CClosure closure = ArgumentValidation.getObject(args[1], t, CClosure.class);
 *     CArray newArray = new CArray(t, (int) array.size(env));
 *
 *     for(Mixed key : array.keySet(env)) {
 *         yield.call(closure, env, t, array.get(key, t, env))
 *              .then((result, y) -&gt; {
 *                  newArray.set(key, result, t, env);
 *              });
 *     }
 *     yield.done(() -&gt; newArray);
 * }
 * </pre>
 */
public abstract class CallbackYield extends AbstractFunction implements FlowFunction<CallbackYield.CallbackState> {

	/**
	 * Implement this instead of {@code exec()}. Use the {@link Yield} object to queue
	 * closure calls and set the final result.
	 *
	 * @param t The code target
	 * @param env The environment
	 * @param args The evaluated arguments (same as what exec() would receive)
	 * @param yield The yield object for queuing closure calls
	 */
	protected abstract void execWithYield(Target t, Environment env, Mixed[] args, Yield yield);

	/**
	 * Bridges the standard exec() interface to the callback mechanism. This is called by the
	 * interpreter loop's simple-exec path, but since CallbackYield is also a FlowFunction,
	 * the loop will use the FlowFunction path instead. This implementation exists only as a
	 * fallback for external callers that invoke exec() directly (e.g. compile-time optimization).
	 * In that case, closures are executed synchronously via executeCallable() as before.
	 */
	@Override
	public final Mixed exec(Target t, Environment env, GenericParameters generics, Mixed... args)
			throws ConfigRuntimeException {
		// Fallback: build the yield chain but execute closures synchronously.
		// This only runs when called outside the iterative interpreter loop.
		Yield yield = new Yield();
		execWithYield(t, env, args, yield);
		yield.executeSynchronously(env, t);
		return yield.getResult();
	}

	@Override
	public StepAction.StepResult<CallbackState> begin(Target t, ParseTree[] children, Environment env) {
		// The interpreter has already evaluated all children (args) before recognizing
		// this as a FlowFunction. But actually — since CallbackYield extends AbstractFunction
		// AND implements FlowFunction, the loop will see instanceof FlowFunction and route
		// to the FlowFunction path. We need to evaluate args ourselves.
		// Start by evaluating the first child.
		CallbackState state = new CallbackState();
		if(children.length > 0) {
			state.children = children;
			state.argIndex = 0;
			return new StepAction.StepResult<>(new StepAction.Evaluate(children[0]), state);
		}
		// No args — run the callback immediately
		return runCallback(t, env, new Mixed[0], state);
	}

	@Override
	public StepAction.StepResult<CallbackState> childCompleted(Target t, CallbackState state,
			Mixed result, Environment env) {
		// Phase 1: collecting args
		if(!state.yieldStarted) {
			state.addArg(result);
			state.argIndex++;
			if(state.argIndex < state.children.length) {
				return new StepAction.StepResult<>(
						new StepAction.Evaluate(state.children[state.argIndex]), state);
			}
			// All args collected — run the callback
			return runCallback(t, env, state.getArgs(), state);
		}

		// Phase 2: draining yield steps — a closure just completed
		YieldStep step = state.currentStep;
		if(step != null && step.callback != null) {
			step.callback.accept(result, state.yield);
		}
		return drainNext(t, state, env);
	}

	@Override
	public StepAction.StepResult<CallbackState> childInterrupted(Target t, CallbackState state,
			StepAction.FlowControl action, Environment env) {
		StepAction.FlowControlAction fca = action.getAction();
		// A return() inside a closure is how it produces its result.
		if(fca instanceof ControlFlow.ReturnAction ret) {
			YieldStep step = state.currentStep;
			cleanupCurrentStep(state, env);
			if(step != null && step.callback != null) {
				step.callback.accept(ret.getValue(), state.yield);
			}
			return drainNext(t, state, env);
		}

		cleanupCurrentStep(state, env);

		// break/continue cannot escape a closure — this is a script error.
		if(fca instanceof ControlFlow.BreakAction || fca instanceof ControlFlow.ContinueAction) {
			throw ConfigRuntimeException.CreateUncatchableException(
					"Loop manipulation operations (e.g. break() or continue()) cannot"
					+ " bubble up past closures.", fca.getTarget());
		}

		// ThrowAction and anything else — propagate
		return null;
	}

	@Override
	public void cleanup(Target t, CallbackState state, Environment env) {
		if(state != null && state.currentStep != null) {
			cleanupCurrentStep(state, env);
		}
	}

	private StepAction.StepResult<CallbackState> runCallback(Target t, Environment env,
			Mixed[] args, CallbackState state) {
		Yield yield = new Yield();
		state.yield = yield;
		state.yieldStarted = true;
		execWithYield(t, env, args, yield);
		return drainNext(t, state, env);
	}

	private StepAction.StepResult<CallbackState> drainNext(Target t, CallbackState state,
			Environment env) {
		Yield yield = state.yield;
		if(!yield.steps.isEmpty()) {
			YieldStep step = yield.steps.poll();
			state.currentStep = step;

			// Try stack-based execution first (closures, procedures)
			Callable.PreparedCallable prep = step.callable.prepareForStack(env, t, step.args);
			if(prep != null) {
				step.preparedEnv = prep.env();
				return new StepAction.StepResult<>(
						new StepAction.Evaluate(prep.node(), prep.env()), state);
			} else {
				// Sync-only Callable (e.g. CNativeClosure) — execute inline
				Mixed result = step.callable.executeCallable(env, t, step.args);
				if(step.callback != null) {
					step.callback.accept(result, yield);
				}
				return drainNext(t, state, env);
			}
		}

		// All steps drained
		return new StepAction.StepResult<>(
				new StepAction.Complete(yield.getResult()), state);
	}

	private void cleanupCurrentStep(CallbackState state, Environment env) {
		YieldStep step = state.currentStep;
		if(step != null) {
			if(step.preparedEnv != null) {
				// Pop the stack trace element that prepareExecution pushed
				step.preparedEnv.getEnv(GlobalEnv.class).GetStackTraceManager().popStackTraceElement();
				step.preparedEnv = null;
			}
			if(step.cleanupAction != null) {
				step.cleanupAction.run();
			}
		}
		state.currentStep = null;
	}

	/**
	 * Per-call state for the FlowFunction. Tracks argument collection and yield step draining.
	 */
	protected static class CallbackState {
		ParseTree[] children;
		int argIndex;
		private Mixed[] args;
		private int argCount;
		boolean yieldStarted;
		Yield yield;
		YieldStep currentStep;

		void addArg(Mixed arg) {
			if(args == null) {
				args = new Mixed[children.length];
			}
			args[argCount++] = arg;
		}

		Mixed[] getArgs() {
			if(args == null) {
				return new Mixed[0];
			}
			if(argCount < args.length) {
				Mixed[] trimmed = new Mixed[argCount];
				System.arraycopy(args, 0, trimmed, 0, argCount);
				return trimmed;
			}
			return args;
		}

		@Override
		public String toString() {
			if(!yieldStarted) {
				return "CallbackState{collecting args: " + argCount + "/" + (children != null ? children.length : 0) + "}";
			}
			return "CallbackState{draining yields: " + (yield != null ? yield.steps.size() : 0) + " remaining}";
		}
	}

	/**
	 * The object passed to {@link #execWithYield}. Functions use this to queue closure calls
	 * and declare the final result.
	 */
	public static class Yield {
		private final Queue<YieldStep> steps = new ArrayDeque<>();
		private Supplier<Mixed> resultSupplier = () -> CVoid.VOID;
		private boolean doneSet = false;

		/**
		 * Queue a closure/callable invocation.
		 *
		 * @param callable The closure or callable to invoke
		 * @param env The environment (unused for closures, which capture their own)
		 * @param t The target
		 * @param args The arguments to pass to the callable
		 * @return A {@link YieldStep} for chaining a {@code .then()} callback
		 */
		public YieldStep call(Callable callable, Environment env, Target t, Mixed... args) {
			YieldStep step = new YieldStep(callable, args);
			steps.add(step);
			return step;
		}

		/**
		 * Set the final result of this function via a supplier. The supplier is evaluated
		 * after all yield steps have completed. This must be called exactly once.
		 *
		 * @param resultSupplier A supplier that returns the result value
		 */
		public void done(Supplier<Mixed> resultSupplier) {
			this.resultSupplier = resultSupplier;
			this.doneSet = true;
		}

		Mixed getResult() {
			return resultSupplier.get();
		}

		/**
		 * Clears all remaining queued steps. Used for short-circuiting (e.g. array_every,
		 * array_some) where the final result is known before all steps have been processed.
		 */
		public void clear() {
			steps.clear();
		}

		/**
		 * Fallback for when CallbackYield functions are called outside the iterative
		 * interpreter (e.g. during compile-time optimization). Drains all steps synchronously
		 * by calling executeCallable directly.
		 */
		void executeSynchronously(Environment env, Target t) {
			while(!steps.isEmpty()) {
				YieldStep step = steps.poll();
				Mixed r = step.callable.executeCallable(env, t, step.args);
				if(step.callback != null) {
					step.callback.accept(r, this);
				}
			}
		}

		@Override
		public String toString() {
			return "Yield{steps=" + steps.size() + ", doneSet=" + doneSet + "}";
		}
	}

	/**
	 * A single queued closure call with an optional continuation.
	 */
	public static class YieldStep {
		final Callable callable;
		final Mixed[] args;
		BiConsumer<Mixed, Yield> callback;
		Runnable cleanupAction;
		Environment preparedEnv;

		YieldStep(Callable callable, Mixed[] args) {
			this.callable = callable;
			this.args = args;
		}

		/**
		 * Register a callback to run after the closure completes.
		 *
		 * @param callback Receives the closure's return value and the Yield object
		 *     (for queuing additional steps or calling done())
		 * @return This step, for fluent chaining
		 */
		public YieldStep then(BiConsumer<Mixed, Yield> callback) {
			this.callback = callback;
			return this;
		}

		/**
		 * Register a cleanup action that runs after this step completes, whether
		 * normally or due to an exception. This is analogous to a {@code finally} block.
		 *
		 * @param cleanup The cleanup action to run
		 * @return This step, for fluent chaining
		 */
		public YieldStep cleanup(Runnable cleanup) {
			this.cleanupAction = cleanup;
			return this;
		}

		@Override
		public String toString() {
			return "YieldStep{callable=" + callable.getClass().getSimpleName()
					+ ", args=" + Arrays.toString(args) + ", hasCallback=" + (callback != null) + "}";
		}
	}
}
