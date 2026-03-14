package com.laytonsmith.core.environments;

/**
 * Callback interface for debugger events. Implementations control what happens when
 * the interpreter pauses (e.g., blocking on a latch for cmdline mode, or returning
 * control to the host application in embedded mode).
 */
public interface DebugListener {

	/**
	 * Called when the interpreter has paused at a breakpoint or step condition.
	 * The paused state provides access to the frozen (async) or live (sync)
	 * execution state for variable inspection and stack traces.
	 *
	 * <p>For cmdline mode, this method typically blocks until the DAP server
	 * sends a continue/step command. For embedded mode, this method returns
	 * immediately and the state is stored for later resumption.</p>
	 *
	 * @param state The paused execution state
	 */
	void onPaused(PausedState state);

	/**
	 * Called when the interpreter resumes execution after being paused.
	 */
	void onResumed();

	/**
	 * Called when script execution completes (normally or via exception)
	 * while a debugger is attached.
	 */
	void onCompleted();

	/**
	 * Called when a new MethodScript thread becomes active in the debugger.
	 * Default implementation does nothing.
	 *
	 * @param dapThreadId The DAP thread ID assigned to this thread
	 * @param name The display name of the thread
	 */
	default void onThreadStarted(int dapThreadId, String name) {
		// no-op by default
	}

	/**
	 * Called when a MethodScript thread exits. Default implementation does nothing.
	 *
	 * @param dapThreadId The DAP thread ID of the exiting thread
	 */
	default void onThreadExited(int dapThreadId) {
		// no-op by default
	}

	/**
	 * Called when a log point breakpoint is hit. The message has already been
	 * interpolated (expressions in {@code {braces}} evaluated).
	 * Default implementation does nothing.
	 *
	 * @param message The interpolated log message
	 */
	default void onLogPoint(String message) {
		// no-op by default
	}
}
