package com.laytonsmith.core.environments;

import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.StackTraceFrame;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.List;
import java.util.Map;

/**
 * Provides read-only access to the interpreter's state when paused at a breakpoint or
 * step condition. Two implementations exist:
 * <ul>
 *   <li><b>Async mode</b>: Backed by a frozen {@link com.laytonsmith.core.Script.DebugSnapshot
 *       DebugSnapshot} — the interpreter has returned and the state is a copy.</li>
 *   <li><b>Sync mode</b>: Backed by the live interpreter state — the interpreter thread
 *       is blocked, and reads go directly to the live stack and environment.</li>
 * </ul>
 */
public interface PausedState {

	/**
	 * Returns the source location where execution paused.
	 */
	Target getPauseTarget();

	/**
	 * Returns the environment at the point of the pause.
	 */
	Environment getEnvironment();

	/**
	 * Returns the user-visible call stack (proc/closure/include frames) at the point
	 * of the pause. Ordered from innermost (most recent) to outermost.
	 */
	List<StackTraceFrame> getCallStack();

	/**
	 * Returns the user-visible call depth at the point of the pause. This is the
	 * count of proc/closure/include frames, not the raw eval stack size.
	 */
	int getUserCallDepth();

	/**
	 * Returns the variables visible at the point of the pause as a name-to-value map.
	 */
	Map<String, Mixed> getVariables();

	/**
	 * Returns true if this pause was caused by an exception breakpoint.
	 */
	boolean isExceptionPause();

	/**
	 * If this is an exception pause, returns the exception. Otherwise returns null.
	 */
	ConfigRuntimeException getPauseException();
}
