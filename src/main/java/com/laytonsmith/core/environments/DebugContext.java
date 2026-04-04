package com.laytonsmith.core.environments;

import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.EvalStack;
import com.laytonsmith.core.FlowFunction;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.StackFrame;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.StackTraceFrame;
import com.laytonsmith.core.exceptions.StackTraceManager;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An {@link Environment.EnvironmentImpl} that holds debugger state. When present in the
 * environment, the interpreter loop checks it at the top of each iteration to determine
 * whether execution should pause (breakpoint hit or step condition met).
 *
 * <p>Two threading modes are supported:</p>
 * <ul>
 *   <li><b>SYNCHRONOUS</b>: The interpreter thread blocks on a latch when paused.
 *       The Java call stack is preserved, and resume releases the latch. This is
 *       the default for command-line execution.</li>
 *   <li><b>ASYNCHRONOUS</b>: The interpreter freezes its state into a
 *       {@link com.laytonsmith.core.Script.DebugSnapshot DebugSnapshot}, calls
 *       {@link DebugListener#onPaused}, and returns. The caller decides whether
 *       to block or return control. This is the default for embedded mode where
 *       the main thread cannot be blocked.</li>
 * </ul>
 */
public class DebugContext implements Environment.EnvironmentImpl {

	/**
	 * Controls how the interpreter thread behaves when paused.
	 */
	public enum ThreadingMode {
		/** Block the interpreter thread in place. Java call stack preserved. */
		SYNCHRONOUS,
		/** Snapshot state and return control to the caller. */
		ASYNCHRONOUS
	}

	/**
	 * The step mode determines when the interpreter next pauses.
	 */
	public enum StepMode {
		/** Running freely - only pause on breakpoints. */
		NONE,
		/** Step Into - pause on the very next frame with a source location. */
		INTO,
		/** Step Over - pause on the next frame at the same or lower stack depth. */
		OVER,
		/** Step Out - pause when the stack depth drops below the reference depth. */
		OUT
	}

	/**
	 * Determines when exceptions cause the debugger to break.
	 */
	public enum ExceptionBreakMode {
		/** Never break on exceptions. */
		NONE,
		/** Break only on uncaught exceptions (no matching try/catch on the stack). */
		UNCAUGHT,
		/** Break on all thrown exceptions, whether caught or not. */
		ALL
	}

	private final Map<File, TreeMap<Integer, Breakpoint>> breakpoints = new HashMap<>();
	private ExceptionBreakMode exceptionBreakMode = ExceptionBreakMode.NONE;
	private boolean disconnected = false;
	private DebugListener listener;
	private ThreadingMode threadingMode;
	private volatile Thread mainThread;

	// Reconnect support: when the debugger disconnects with suspend mode,
	// execution threads block in shouldPause() until a new client connects.
	private volatile boolean awaitingReconnect = false;
	private final Object reconnectMonitor = new Object();

	// Suspend-on-start support: when true, execution threads block in
	// shouldPause() until the debugger connects and sends configurationDone.
	private volatile boolean awaitingInitialConnection = false;

	// Log point deduplication: tracks last fired log point to avoid firing
	// multiple times for different AST nodes on the same source line.
	private File lastLogPointFile;
	private int lastLogPointLine = -1;

	// Per-thread debug state (step mode, pause latch, etc.)
	private final ConcurrentHashMap<Thread, ThreadDebugState> threadStates = new ConcurrentHashMap<>();

	// Thread registry: Thread → DAP thread ID and Thread → name
	public static final int MAIN_THREAD_DAP_ID = 1;
	private final AtomicInteger nextDapThreadId = new AtomicInteger(MAIN_THREAD_DAP_ID + 1);
	private final ConcurrentHashMap<Thread, Integer> threadDapIds = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Thread, String> threadNames = new ConcurrentHashMap<>();

	/**
	 * Creates a new DebugContext with no breakpoints, the given listener, and
	 * the specified threading mode.
	 *
	 * @param listener The listener to notify when execution pauses or resumes.
	 *     Must not be null.
	 * @param threadingMode The threading mode (SYNCHRONOUS or ASYNCHRONOUS).
	 *     The caller must choose the appropriate mode for its execution context.
	 * @param mainThread The main execution thread. Non-main threads (e.g. from
	 *     x_new_thread) always use synchronous mode regardless of the configured
	 *     threading mode. Must not be null.
	 */
	public DebugContext(DebugListener listener, ThreadingMode threadingMode, Thread mainThread) {
		if(listener == null) {
			throw new IllegalArgumentException("DebugListener must not be null");
		}
		if(mainThread == null) {
			throw new IllegalArgumentException("mainThread must not be null");
		}
		this.listener = listener;
		this.threadingMode = threadingMode;
		this.mainThread = mainThread;
		threadDapIds.put(mainThread, MAIN_THREAD_DAP_ID);
		threadNames.put(mainThread, "main");
	}

	/**
	 * Adds a breakpoint. If a breakpoint at the same file+line exists, it is replaced.
	 *
	 * @param bp The breakpoint to add
	 */
	public void addBreakpoint(Breakpoint bp) {
		breakpoints.computeIfAbsent(bp.file(), k -> new TreeMap<>()).put(bp.line(), bp);
	}

	/**
	 * Removes a breakpoint. If the breakpoint doesn't exist, this is a no-op.
	 *
	 * @param bp The breakpoint to remove
	 */
	public void removeBreakpoint(Breakpoint bp) {
		TreeMap<Integer, Breakpoint> lines = breakpoints.get(bp.file());
		if(lines != null) {
			lines.remove(bp.line());
			if(lines.isEmpty()) {
				breakpoints.remove(bp.file());
			}
		}
	}

	/**
	 * Replaces all breakpoints for a given file with fully configured breakpoints
	 * (including conditions, hit counts, and log messages).
	 *
	 * @param file The file whose breakpoints to replace
	 * @param newBreakpoints The new breakpoints for this file
	 */
	public void setBreakpointsForFile(File file, java.util.List<Breakpoint> newBreakpoints) {
		TreeMap<Integer, Breakpoint> map = new TreeMap<>();
		for(Breakpoint bp : newBreakpoints) {
			map.put(bp.line(), bp);
		}
		if(map.isEmpty()) {
			breakpoints.remove(file);
		} else {
			breakpoints.put(file, map);
		}
	}

	/**
	 * Looks up the breakpoint at the given file and line, or returns null if none exists.
	 * O(log n) in the number of breakpoints for that file.
	 */
	public Breakpoint getBreakpoint(File file, int line) {
		TreeMap<Integer, Breakpoint> lines = breakpoints.get(file);
		if(lines == null) {
			return null;
		}
		return lines.get(line);
	}

	/**
	 * Clears all breakpoints.
	 */
	public void clearBreakpoints() {
		breakpoints.clear();
	}

	/**
	 * Gets or creates the {@link ThreadDebugState} for the calling thread.
	 */
	public ThreadDebugState getThreadState() {
		return threadStates.computeIfAbsent(Thread.currentThread(), k -> new ThreadDebugState());
	}

	/**
	 * Gets or creates the {@link ThreadDebugState} for the specified thread.
	 */
	public ThreadDebugState getThreadState(Thread t) {
		return threadStates.computeIfAbsent(t, k -> new ThreadDebugState());
	}

	/**
	 * Returns the current step mode for the calling thread.
	 */
	public StepMode getStepMode() {
		return getThreadState().getStepMode();
	}

	/**
	 * Sets the step mode and reference depth for the calling thread. Called when the user
	 * issues a step command (into/over/out) or continue.
	 * When targetCol >= 0, step-into will only enter the function at that column.
	 */
	public void setStepMode(int dapThreadId, StepMode mode, int currentDepth,
			Target currentTarget, int targetCol) {
		Thread t = getThreadByDapId(dapThreadId);
		if(t != null) {
			getThreadState(t).setStepMode(mode, currentDepth, currentTarget, targetCol);
		}
	}

	/**
	 * Returns true if the calling thread is currently in a paused state.
	 */
	public boolean isPaused() {
		return getThreadState().isPaused();
	}

	/**
	 * Sets the paused state for the calling thread. Called by the interpreter when it freezes.
	 * When pausing, also records the pause location so that on resume,
	 * the breakpoint check can be skipped until the interpreter moves
	 * past the pause point.
	 *
	 * @param paused true if pausing, false if resuming
	 * @param pauseTarget The source location where execution paused,
	 *     or null when resuming
	 */
	public void setPaused(boolean paused, Target pauseTarget) {
		getThreadState().setPaused(paused, pauseTarget);
	}

	/**
	 * Marks the debugger as disconnected. The interpreter will clear the
	 * DebugContext and run to completion.
	 */
	public void disconnect() {
		this.disconnected = true;
		synchronized(reconnectMonitor) {
			awaitingReconnect = false;
			awaitingInitialConnection = false;
			reconnectMonitor.notifyAll();
		}
		for(ThreadDebugState state : threadStates.values()) {
			state.setPaused(false, null);
			state.setStepMode(StepMode.NONE, 0, null, -1);
			state.resume();
		}
		threadStates.clear();
	}

	/**
	 * Enters reconnect-wait mode. Execution threads that call
	 * {@link #shouldPause} will block until {@link #reconnect()} is called.
	 * This is used when the debugger disconnects while suspend mode is active,
	 * so that execution pauses until a new client attaches.
	 */
	public void awaitReconnect() {
		synchronized(reconnectMonitor) {
			awaitingReconnect = true;
		}
		// Resume any threads that are paused on debug latches so they can
		// reach shouldPause() and block on the reconnect monitor instead.
		for(ThreadDebugState state : threadStates.values()) {
			state.setPaused(false, null);
			state.resume();
		}
	}

	/**
	 * Signals that a new debugger has connected. Unblocks any execution
	 * threads that are waiting in {@link #shouldPause} for a reconnection.
	 */
	public void reconnect() {
		synchronized(reconnectMonitor) {
			disconnected = false;
			awaitingReconnect = false;
			awaitingInitialConnection = false;
			reconnectMonitor.notifyAll();
		}
	}

	/**
	 * Returns true if execution threads are blocked waiting for a debugger
	 * to reconnect.
	 */
	public boolean isAwaitingReconnect() {
		return awaitingReconnect;
	}

	/**
	 * Sets the awaiting-initial-connection flag. When true, execution threads
	 * will block in {@link #shouldPause} until a debugger connects and calls
	 * {@link #reconnect()}. This is used for suspend-on-start in contexts
	 * where the calling thread cannot be blocked (e.g. Minecraft server startup).
	 */
	public void setAwaitingInitialConnection(boolean awaiting) {
		synchronized(reconnectMonitor) {
			awaitingInitialConnection = awaiting;
		}
	}

	/**
	 * Returns true if the debugger has been disconnected.
	 */
	public boolean isDisconnected() {
		return disconnected;
	}

	/**
	 * Returns the debug listener.
	 */
	public DebugListener getListener() {
		return listener;
	}

	/**
	 * Determines whether the interpreter should pause at the given source location
	 * and user-visible call depth. This is called at the top of each interpreter loop
	 * iteration.
	 *
	 * <p>The {@code userCallDepth} is <b>not</b> the raw eval stack size. It is the count
	 * of user-visible call boundaries on the stack - proc calls, closure calls, and includes.
	 * Internal functions (if, for, array_push, etc.) do not count. This matches the call
	 * stack a user would see in a debugger's stack trace panel.</p>
	 *
	 * @param source The source location of the current frame
	 * @param userCallDepth The user-visible call depth (proc/closure/include nesting)
	 * @param env The current environment, used for evaluating conditional breakpoints
	 * @return true if the interpreter should pause
	 */
	public boolean shouldPause(Target source, int userCallDepth, Environment env) {
		if(disconnected) {
			return false;
		}

		// If the debugger disconnected with suspend mode, or we're awaiting
		// the initial debugger connection (suspend-on-start), block until
		// a debugger connects. The thread will appear stopped at whatever
		// line it was executing when the disconnect happened.
		if(awaitingReconnect || awaitingInitialConnection) {
			synchronized(reconnectMonitor) {
				while(awaitingReconnect || awaitingInitialConnection) {
					try {
						reconnectMonitor.wait();
					} catch(InterruptedException e) {
						Thread.currentThread().interrupt();
						return false;
					}
				}
			}
			// A new debugger just connected - pause so it can inspect state.
			return true;
		}

		if(source == null || source == Target.UNKNOWN
				|| source.file() == null || source.line() <= 0) {
			return false;
		}

		ThreadDebugState state = getThreadState();

		// Check if we would normally pause here (breakpoint or step condition).
		boolean shouldStop = false;

		Breakpoint bp = getBreakpoint(source.file(), source.line());
		if(bp != null) {
			if(bp.isConditional()) {
				shouldStop = evaluateBreakpointCondition(bp, source, state, env);
			} else {
				shouldStop = true;
			}
			if(shouldStop && bp.isLogPoint()) {
				// Deduplicate: only fire once per visit to a source line.
				// Resets when execution moves to a different line.
				if(!source.file().equals(lastLogPointFile) || source.line() != lastLogPointLine) {
					lastLogPointFile = source.file();
					lastLogPointLine = source.line();
					String msg = interpolateLogMessage(bp.logMessage(), env);
					listener.onLogPoint(msg);
				}
				return false;
			}
		}

		// Clear log point dedup when we move to a different line
		if(lastLogPointLine != -1
				&& (!source.file().equals(lastLogPointFile) || source.line() != lastLogPointLine)) {
			lastLogPointFile = null;
			lastLogPointLine = -1;
		}

		if(!shouldStop) {
			switch(state.getStepMode()) {
				case NONE:
					break;
				case INTO:
					if(state.getStepIntoTargetCol() >= 0) {
						// Targeted step-into: only step into the function at the target column
						if(sameSourceLine(source, state.getStepReferenceTarget())) {
							// Still on the paused line, haven't entered anything yet
							break;
						}
						if(userCallDepth > state.getStepReferenceDepth()) {
							// Entered a function — check if it's the target
							StackTraceManager stm = env.getEnv(GlobalEnv.class)
									.peekStackTraceManager();
							if(stm != null) {
								List<StackTraceFrame> frames = stm.getCurrentStackTrace();
								if(!frames.isEmpty()) {
									Target callSite = frames.get(0).getCallSite();
									if(callSite.col() == state.getStepIntoTargetCol()) {
										shouldStop = true;
										break;
									}
								}
							}
							// Not the target — don't pause (step over this call)
						} else {
							// Moved to a different line at same/lower depth (safety fallback)
							shouldStop = true;
						}
					} else {
						shouldStop = !sameSourceLine(source, state.getStepReferenceTarget());
					}
					break;
				case OVER:
					shouldStop = userCallDepth <= state.getStepReferenceDepth()
							&& !sameSourceLine(source, state.getStepReferenceTarget());
					break;
				case OUT:
					shouldStop = userCallDepth < state.getStepReferenceDepth();
					break;
				default:
					break;
			}
		}

		// After resuming, suppress pauses while still at the resume source line.
		// This prevents breakpoints and step conditions from re-firing before
		// the interpreter has advanced. Once we move to a different source line,
		// the flag is unconditionally cleared so it doesn't interfere with later
		// pause checks (e.g. step-over returning from a deeper call).
		if(state.isSkippingResume()) {
			if(sameSourceLine(source, state.getResumeTarget())) {
				return false;
			}
			state.clearSkippingResume();
		}

		// When doing targeted step-into, suppress breakpoint re-fires on the
		// reference line. We may return to this line between stepping over
		// non-target functions and entering the target.
		if(shouldStop && state.getStepMode() == StepMode.INTO
				&& state.getStepIntoTargetCol() >= 0
				&& sameSourceLine(source, state.getStepReferenceTarget())
				&& userCallDepth <= state.getStepReferenceDepth()) {
			shouldStop = false;
		}

		return shouldStop;
	}

	/**
	 * Evaluates a conditional breakpoint's condition and hit count.
	 * Returns true if the breakpoint should cause a pause.
	 */
	private boolean evaluateBreakpointCondition(Breakpoint bp, Target source,
			ThreadDebugState state, Environment env) {
		// Check hit count first (cheap). Deduplicate per source line visit:
		// multiple AST nodes on the same line should only increment the hit
		// count once. We use the column to distinguish "same visit, different
		// node" (col differs → cache hit) from "new visit, same first node"
		// (col matches → cache miss, re-evaluate).
		if(bp.hitCountThreshold() > 0) {
			if(!state.hasCachedBreakpointResult(source.file(), source.line(), source.col())) {
				int count = bp.incrementHitCount();
				boolean hitReached = count >= bp.hitCountThreshold();
				state.cacheBreakpointResult(source.file(), source.line(), source.col(), hitReached);
				if(!hitReached) {
					return false;
				}
			} else if(!state.getCachedBreakpointResult()) {
				return false;
			}
		}
		// Evaluate condition expression
		ParseTree compiled = bp.compiledCondition();
		if(compiled != null && env != null) {
			try {
				Mixed result = MethodScriptCompiler.execute(compiled, env, null, null);
				while(result instanceof IVariable iv) {
					result = env.getEnv(GlobalEnv.class).GetVarList()
							.get(iv.getVariableName(), iv.getTarget(), env).ival();
				}
				return ArgumentValidation.getBooleanish(result, Target.UNKNOWN, env);
			} catch(Exception e) {
				// If condition evaluation fails, pause anyway so the user sees the error
				return true;
			}
		}
		return true;
	}

	/**
	 * Interpolates a DAP log message template. Expressions in {@code {braces}} are
	 * evaluated as MethodScript and their string value substituted in.
	 */
	private String interpolateLogMessage(String template, Environment env) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while(i < template.length()) {
			char c = template.charAt(i);
			if(c == '{') {
				int end = template.indexOf('}', i + 1);
				if(end == -1) {
					sb.append(c);
					i++;
				} else {
					String expr = template.substring(i + 1, end);
					try {
						Mixed result = MethodScriptCompiler.execute(
								MethodScriptCompiler.compile(
										MethodScriptCompiler.lex(expr, null, null, true),
										null, Environment.getDefaultEnvClasses()),
								env, null, null);
						while(result instanceof IVariable iv) {
							result = env.getEnv(GlobalEnv.class).GetVarList()
									.get(iv.getVariableName(), iv.getTarget(), env).ival();
						}
						sb.append(result.val());
					} catch(Exception e) {
						sb.append("{").append(expr).append("}");
					}
					i = end + 1;
				}
			} else {
				sb.append(c);
				i++;
			}
		}
		return sb.toString();
	}

	/**
	 * Sets the exception break mode.
	 *
	 * @param mode The new exception break mode
	 */
	public void setExceptionBreakMode(ExceptionBreakMode mode) {
		this.exceptionBreakMode = mode;
	}

	/**
	 * Returns the current exception break mode.
	 */
	public ExceptionBreakMode getExceptionBreakMode() {
		return exceptionBreakMode;
	}

	/**
	 * Determines whether the debugger should break on the given exception, based
	 * on the current {@link ExceptionBreakMode} and the eval stack state.
	 *
	 * <p>For {@link ExceptionBreakMode#ALL}, always returns true. For
	 * {@link ExceptionBreakMode#UNCAUGHT}, walks the eval stack looking for
	 * {@code try} frames whose catch type list would match the exception. If no
	 * matching catch is found, the exception is considered uncaught.</p>
	 *
	 * @param exception The exception that was thrown
	 * @param stack The current eval stack at the point of the throw
	 * @return true if the debugger should pause
	 */
	public boolean shouldBreakOnException(ConfigRuntimeException exception, EvalStack stack) {
		if(disconnected || exceptionBreakMode == ExceptionBreakMode.NONE) {
			return false;
		}
		if(exceptionBreakMode == ExceptionBreakMode.ALL) {
			return true;
		}
		// UNCAUGHT: walk the stack looking for a frame that would catch this exception
		for(StackFrame frame : stack) {
			if(frame.hasFlowFunction() && frame.hasBegun()) {
				@SuppressWarnings("unchecked")
				FlowFunction<Object> ff = (FlowFunction<Object>) frame.getFlowFunction();
				if(ff.wouldCatch(frame.getFunctionState(), exception)) {
					return false;
				}
			}
		}
		return true;
	}

	private static boolean sameSourceLine(Target a, Target b) {
		if(a == b) {
			return true;
		}
		if(a == null || b == null) {
			return false;
		}
		if(a.line() != b.line()) {
			return false;
		}
		if(a.file() == null || b.file() == null) {
			return a.file() == b.file();
		}
		return a.file().equals(b.file());
	}

	/**
	 * Returns the threading mode for this debug context.
	 */
	public ThreadingMode getThreadingMode() {
		return threadingMode;
	}

	/**
	 * Returns the main execution thread. This is the thread that uses the
	 * configured threading mode; all other threads use synchronous mode.
	 */
	public Thread getMainThread() {
		return mainThread;
	}

	/**
	 * Updates the main execution thread. This is needed in asynchronous mode
	 * when resuming from a snapshot creates a new thread that becomes the
	 * continuation of the main execution.
	 */
	public void setMainThread(Thread mainThread) {
		if(this.mainThread != null && this.mainThread != mainThread) {
			// Transfer debug state from old main thread to new one so that
			// step mode settings survive the thread switch in async mode.
			ThreadDebugState oldState = threadStates.remove(this.mainThread);
			if(oldState != null) {
				threadStates.put(mainThread, oldState);
			}
			threadDapIds.remove(this.mainThread);
			threadNames.remove(this.mainThread);
		}
		this.mainThread = mainThread;
		threadDapIds.put(mainThread, MAIN_THREAD_DAP_ID);
		threadNames.put(mainThread, "main");
	}

	/**
	 * Returns true if the calling thread should use synchronous debugging
	 * (block in place). This is true when:
	 * <ul>
	 * <li>The configured threading mode is SYNCHRONOUS, or</li>
	 * <li>The calling thread is not the main execution thread (e.g. a thread
	 *     spawned by x_new_thread, which can always safely block)</li>
	 * </ul>
	 */
	public boolean shouldUseSyncMode() {
		return threadingMode == ThreadingMode.SYNCHRONOUS
				|| Thread.currentThread() != mainThread;
	}

	/**
	 * Creates the latch that {@link #awaitResume()} will block on. Must be
	 * called before notifying the listener of a pause, so that a resume()
	 * arriving before awaitResume() is not lost.
	 */
	public void prepareAwait() {
		getThreadState().prepareAwait();
	}

	/**
	 * Blocks the calling thread until {@link #resume(int)} is called from the DAP thread.
	 * Used by the interpreter loop in synchronous mode to pause in place.
	 * {@link #prepareAwait()} must have been called first.
	 *
	 * @throws InterruptedException if the waiting thread is interrupted
	 */
	public void awaitResume() throws InterruptedException {
		getThreadState().awaitResume();
	}

	/**
	 * Releases the thread with the given DAP thread ID that is blocked in
	 * {@link #awaitResume()}. Called from the DAP server thread when a
	 * continue/step command is received.
	 *
	 * @param dapThreadId The DAP thread ID to resume
	 */
	public void resume(int dapThreadId) {
		Thread t = getThreadByDapId(dapThreadId);
		if(t != null) {
			getThreadState(t).resume();
		}
	}

	/**
	 * Releases the calling thread's latch. Used internally (e.g. during disconnect).
	 */
	public void resume() {
		getThreadState().resume();
	}

	// ---- Thread registry ----

	/**
	 * Registers a thread with the debug context, assigning it a DAP thread ID
	 * and capturing its name. The main thread is registered automatically at
	 * construction time; this is for background threads.
	 *
	 * @param t The thread to register
	 * @param displayName The user-visible name, or null to use the Java thread name
	 * @return The assigned DAP thread ID
	 */
	public int registerThread(Thread t, String displayName) {
		return threadDapIds.computeIfAbsent(t, k -> {
			threadNames.put(t, displayName != null ? displayName : t.getName());
			return nextDapThreadId.getAndIncrement();
		});
	}

	/**
	 * Registers a thread and transfers the {@link ThreadDebugState} from a
	 * source thread. This is used in embedded async mode where the main
	 * thread's step mode and other debug state need to carry over to a
	 * spawned execution thread without changing the main thread identity.
	 *
	 * @param t The thread to register
	 * @param displayName The user-visible name
	 * @param stateSource The thread whose debug state should be transferred
	 * @return The assigned DAP thread ID
	 */
	public int registerThread(Thread t, String displayName, Thread stateSource) {
		int dapId = registerThread(t, displayName);
		ThreadDebugState state = threadStates.remove(stateSource);
		if(state != null) {
			threadStates.put(t, state);
		}
		return dapId;
	}

	/**
	 * Unregisters a thread, removing its DAP ID, name, and debug state.
	 *
	 * @param t The thread to unregister
	 */
	public void unregisterThread(Thread t) {
		threadDapIds.remove(t);
		threadNames.remove(t);
		ThreadDebugState state = threadStates.remove(t);
		if(state != null) {
			state.resume();
		}
	}

	/**
	 * Returns the DAP thread ID for the given thread, or -1 if not registered.
	 */
	public int getDapThreadId(Thread t) {
		Integer id = threadDapIds.get(t);
		return id != null ? id : -1;
	}

	/**
	 * Returns the DAP thread ID for the calling thread, or -1 if not registered.
	 */
	public int getCurrentDapThreadId() {
		return getDapThreadId(Thread.currentThread());
	}

	/**
	 * Returns the thread associated with the given DAP thread ID, or null if not found.
	 */
	public Thread getThreadByDapId(int dapThreadId) {
		for(java.util.Map.Entry<Thread, Integer> entry : threadDapIds.entrySet()) {
			if(entry.getValue() == dapThreadId) {
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * Returns a snapshot of all registered threads as a map of DAP thread ID to thread name.
	 */
	public java.util.Map<Integer, String> getRegisteredThreads() {
		java.util.Map<Integer, String> result = new java.util.LinkedHashMap<>();
		for(java.util.Map.Entry<Thread, Integer> entry : threadDapIds.entrySet()) {
			String name = threadNames.getOrDefault(entry.getKey(), entry.getKey().getName());
			result.put(entry.getValue(), name);
		}
		return result;
	}

	/**
	 * Registers this DebugContext as a {@link com.laytonsmith.PureUtilities.DaemonManager.ThreadLifecycleListener}
	 * on the given DaemonManager, so that threads activated/deactivated through the
	 * DaemonManager are automatically registered/unregistered for debugging.
	 *
	 * @param dm The DaemonManager to listen to
	 */
	public void registerWithDaemonManager(com.laytonsmith.PureUtilities.DaemonManager dm) {
		dm.addThreadLifecycleListener(new com.laytonsmith.PureUtilities.DaemonManager.ThreadLifecycleListener() {
			@Override
			public void onActivated(Thread thread, String displayName) {
				int dapId = registerThread(thread, displayName);
				String name = displayName != null ? displayName : thread.getName();
				if(listener != null) {
					listener.onThreadStarted(dapId, name);
				}
			}

			@Override
			public void onDeactivated(Thread thread) {
				int dapId = getDapThreadId(thread);
				unregisterThread(thread);
				if(listener != null && dapId >= 0) {
					listener.onThreadExited(dapId);
				}
			}
		});
	}

	@Override
	public Environment.EnvironmentImpl clone() throws CloneNotSupportedException {
		// Debug context is shared across environment clones - all frames in a single
		// execution unit share the same debugger state.
		return this;
	}

	@Override
	public String toString() {
		return "DebugContext{threads=" + threadStates.size()
				+ ", breakpoints=" + breakpoints.size() + ", disconnected=" + disconnected + "}";
	}
}
