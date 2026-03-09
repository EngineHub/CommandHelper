package com.laytonsmith.core.environments;

import com.laytonsmith.core.constructs.Target;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * An {@link Environment.EnvironmentImpl} that holds debugger state. When present in the
 * environment, the interpreter loop checks it at the top of each iteration to determine
 * whether execution should pause (breakpoint hit or step condition met).
 *
 * <p>This class is the core of the asynchronous debugging model. When the interpreter
 * decides to pause, it freezes its state into a {@link DebugSnapshot}, calls
 * {@link DebugListener#onPaused(DebugSnapshot)}, and returns. The caller (cmdline or
 * Minecraft server) decides whether to block or return control. When the debugger sends
 * a continue/step command, the caller feeds the snapshot back to the interpreter to resume.</p>
 */
public class DebugContext implements Environment.EnvironmentImpl {

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

	private final Set<Breakpoint> breakpoints = new HashSet<>();
	private StepMode stepMode = StepMode.NONE;
	private int stepReferenceDepth = 0;
	private Target stepReferenceTarget = Target.UNKNOWN;
	private Target resumeTarget = Target.UNKNOWN;
	private boolean paused = false;
	private boolean disconnected = false;
	private boolean skippingResume = false;
	private DebugListener listener;

	/**
	 * Creates a new DebugContext with no breakpoints and the given listener.
	 *
	 * @param listener The listener to notify when execution pauses or resumes.
	 *     Must not be null.
	 */
	public DebugContext(DebugListener listener) {
		if(listener == null) {
			throw new IllegalArgumentException("DebugListener must not be null");
		}
		this.listener = listener;
	}

	/**
	 * Adds a breakpoint. If the breakpoint already exists, this is a no-op.
	 *
	 * @param bp The breakpoint to add
	 */
	public void addBreakpoint(Breakpoint bp) {
		breakpoints.add(bp);
	}

	/**
	 * Removes a breakpoint. If the breakpoint doesn't exist, this is a no-op.
	 *
	 * @param bp The breakpoint to remove
	 */
	public void removeBreakpoint(Breakpoint bp) {
		breakpoints.remove(bp);
	}

	/**
	 * Replaces all breakpoints for a given file. Used by DAP's setBreakpoints
	 * which sends the full set of breakpoints for a file at once.
	 *
	 * @param file The file whose breakpoints to replace
	 * @param lines The new set of line numbers
	 */
	public void setBreakpointsForFile(File file, Set<Integer> lines) {
		breakpoints.removeIf(bp -> bp.file().equals(file));
		for(int line : lines) {
			breakpoints.add(new Breakpoint(file, line));
		}
	}

	/**
	 * Returns an unmodifiable view of the current breakpoints.
	 */
	public Set<Breakpoint> getBreakpoints() {
		return Collections.unmodifiableSet(breakpoints);
	}

	/**
	 * Clears all breakpoints.
	 */
	public void clearBreakpoints() {
		breakpoints.clear();
	}

	/**
	 * Returns the current step mode.
	 */
	public StepMode getStepMode() {
		return stepMode;
	}

	/**
	 * Sets the step mode and reference depth. Called when the user issues a
	 * step command (into/over/out) or continue.
	 *
	 * @param mode The new step mode
	 * @param currentDepth The user-visible call depth at the time of the command
	 *     (count of proc/closure/include frames, not raw eval stack size)
	 * @param currentTarget The source target at the time of the command
	 */
	public void setStepMode(StepMode mode, int currentDepth, Target currentTarget) {
		this.stepMode = mode;
		this.stepReferenceDepth = currentDepth;
		this.stepReferenceTarget = currentTarget;
	}

	/**
	 * Returns the stack depth when the current step command was issued.
	 */
	public int getStepReferenceDepth() {
		return stepReferenceDepth;
	}

	/**
	 * Returns true if the debugger is currently in a paused state.
	 */
	public boolean isPaused() {
		return paused;
	}

	/**
	 * Sets the paused state. Called by the interpreter when it freezes.
	 * When pausing, also records the pause location so that on resume,
	 * the breakpoint check can be skipped until the interpreter moves
	 * past the pause point.
	 *
	 * @param paused true if pausing, false if resuming
	 * @param pauseTarget The source location where execution paused,
	 *     or null when resuming
	 */
	public void setPaused(boolean paused, Target pauseTarget) {
		this.paused = paused;
		if(paused && pauseTarget != null) {
			this.resumeTarget = pauseTarget;
			this.skippingResume = true;
		}
	}

	/**
	 * Marks the debugger as disconnected. The interpreter will clear the
	 * DebugContext and run to completion.
	 */
	public void disconnect() {
		this.disconnected = true;
		this.paused = false;
		this.stepMode = StepMode.NONE;
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
	 * @return true if the interpreter should pause
	 */
	public boolean shouldPause(Target source, int userCallDepth) {
		if(disconnected) {
			return false;
		}

		if(source == null || source == Target.UNKNOWN
				|| source.file() == null || source.line() <= 0) {
			return false;
		}

		// Check if we would normally pause here (breakpoint or step condition).
		boolean shouldStop = false;

		if(breakpoints.contains(new Breakpoint(source.file(), source.line()))) {
			shouldStop = true;
		}

		if(!shouldStop) {
			switch(stepMode) {
				case NONE:
					break;
				case INTO:
					shouldStop = !sameSourceLine(source, stepReferenceTarget);
					break;
				case OVER:
					shouldStop = userCallDepth <= stepReferenceDepth
							&& !sameSourceLine(source, stepReferenceTarget);
					break;
				case OUT:
					shouldStop = userCallDepth < stepReferenceDepth;
					break;
				default:
					break;
			}
		}

		// After resuming, suppress pauses while still at the resume source line.
		// This prevents breakpoints and step conditions from re-firing before
		// the interpreter has advanced. The flag is only cleared when we would
		// pause at a genuinely new location.
		if(shouldStop && skippingResume) {
			if(sameSourceLine(source, resumeTarget)) {
				return false;
			}
			skippingResume = false;
		}

		return shouldStop;
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

	@Override
	public Environment.EnvironmentImpl clone() throws CloneNotSupportedException {
		// Debug context is shared across environment clones - all frames in a single
		// execution unit share the same debugger state.
		return this;
	}

	@Override
	public String toString() {
		return "DebugContext{stepMode=" + stepMode + ", paused=" + paused
				+ ", breakpoints=" + breakpoints.size() + ", disconnected=" + disconnected + "}";
	}
}
