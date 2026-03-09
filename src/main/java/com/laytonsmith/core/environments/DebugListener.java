package com.laytonsmith.core.environments;

import com.laytonsmith.core.Script;

/**
 * Callback interface for debugger events. Implementations control what happens when
 * the interpreter pauses (e.g., blocking on a latch for cmdline mode, or returning
 * control to the host application in embedded mode).
 */
public interface DebugListener {

	/**
	 * Called when the interpreter has paused at a breakpoint or step condition.
	 * The snapshot contains the frozen execution state and can be used to inspect
	 * variables and stack frames.
	 *
	 * <p>For cmdline mode, this method typically blocks until the DAP server
	 * sends a continue/step command. For embedded mode, this method returns
	 * immediately and the snapshot is stored for later resumption.</p>
	 *
	 * @param snapshot The frozen execution state
	 */
	void onPaused(Script.DebugSnapshot snapshot);

	/**
	 * Called when the interpreter resumes execution after being paused.
	 */
	void onResumed();

	/**
	 * Called when script execution completes (normally or via exception)
	 * while a debugger is attached.
	 */
	void onCompleted();
}
