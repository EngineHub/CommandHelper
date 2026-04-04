package com.laytonsmith.core.environments;

import com.laytonsmith.core.constructs.Target;

import java.io.File;
import java.util.concurrent.CountDownLatch;

/**
 * Per-thread debug state, extracted from DebugContext so that multiple threads can be
 * independently paused, stepped, and resumed. Each MethodScript thread gets its own
 * instance, stored in DebugContext's thread registry.
 */
public class ThreadDebugState {

	private volatile DebugContext.StepMode stepMode = DebugContext.StepMode.NONE;
	private volatile int stepReferenceDepth = 0;
	private volatile Target stepReferenceTarget = Target.UNKNOWN;
	private volatile int stepIntoTargetCol = -1;
	private volatile Target resumeTarget = Target.UNKNOWN;
	private volatile boolean paused = false;
	private volatile boolean skippingResume = false;
	private volatile CountDownLatch pauseLatch;

	// Breakpoint hit-count deduplication: when shouldPause checks a
	// hit-count breakpoint, we cache the file+line+col+result so that
	// subsequent AST nodes on the same source line reuse the cached result
	// (and don't re-increment the hit count). A "new visit" to the same
	// line is detected when the same first-node column fires again.
	private volatile File lastBpFile;
	private volatile int lastBpLine = -1;
	private volatile int lastBpCol = -1;
	private volatile boolean lastBpResult;

	public DebugContext.StepMode getStepMode() {
		return stepMode;
	}

	public void setStepMode(DebugContext.StepMode mode, int currentDepth, Target currentTarget,
			int targetCol) {
		this.stepMode = mode;
		this.stepReferenceDepth = currentDepth;
		this.stepReferenceTarget = currentTarget;
		this.stepIntoTargetCol = targetCol;
	}

	public int getStepIntoTargetCol() {
		return stepIntoTargetCol;
	}

	public int getStepReferenceDepth() {
		return stepReferenceDepth;
	}

	public Target getStepReferenceTarget() {
		return stepReferenceTarget;
	}

	public boolean isPaused() {
		return paused;
	}

	public void setPaused(boolean paused, Target pauseTarget) {
		this.paused = paused;
		if(paused && pauseTarget != null) {
			this.resumeTarget = pauseTarget;
			this.skippingResume = true;
		}
	}

	public Target getResumeTarget() {
		return resumeTarget;
	}

	public boolean isSkippingResume() {
		return skippingResume;
	}

	public void clearSkippingResume() {
		this.skippingResume = false;
	}

	/**
	 * Creates the latch that {@link #awaitResume()} will block on. Must be
	 * called before any listener notification that might trigger a
	 * {@link #resume()} call, to avoid the race where resume() fires before
	 * the latch exists.
	 */
	public void prepareAwait() {
		pauseLatch = new CountDownLatch(1);
	}

	/**
	 * Blocks the calling thread until {@link #resume()} is called.
	 * {@link #prepareAwait()} must have been called first.
	 */
	public void awaitResume() throws InterruptedException {
		pauseLatch.await();
	}

	/**
	 * Releases the thread blocked in {@link #awaitResume()}.
	 */
	public void resume() {
		CountDownLatch latch = pauseLatch;
		if(latch != null) {
			latch.countDown();
		}
	}

	/**
	 * Returns true if we already evaluated a hit-count breakpoint for this
	 * file+line on this thread and can reuse the cached result. A cache hit
	 * occurs when file+line match but col differs (a subsequent AST node on
	 * the same source line). When file+line+col all match, it means the first
	 * node is firing again (new loop iteration), so we return false to force
	 * a fresh evaluation.
	 */
	public boolean hasCachedBreakpointResult(File file, int line, int col) {
		return lastBpLine == line && file != null && file.equals(lastBpFile)
				&& lastBpCol != col;
	}

	/**
	 * Returns the cached breakpoint evaluation result.
	 */
	public boolean getCachedBreakpointResult() {
		return lastBpResult;
	}

	/**
	 * Caches the breakpoint evaluation result for the given file+line+col.
	 */
	public void cacheBreakpointResult(File file, int line, int col, boolean result) {
		this.lastBpFile = file;
		this.lastBpLine = line;
		this.lastBpCol = col;
		this.lastBpResult = result;
	}

	/**
	 * Clears the breakpoint evaluation cache. Called when execution moves
	 * to a different source line.
	 */
	public void clearBreakpointCache() {
		this.lastBpFile = null;
		this.lastBpLine = -1;
		this.lastBpCol = -1;
	}
}
