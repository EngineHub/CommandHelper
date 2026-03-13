package com.laytonsmith.core.environments;

import com.laytonsmith.core.constructs.Target;

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
	private volatile Target resumeTarget = Target.UNKNOWN;
	private volatile boolean paused = false;
	private volatile boolean skippingResume = false;
	private volatile CountDownLatch pauseLatch;

	public DebugContext.StepMode getStepMode() {
		return stepMode;
	}

	public void setStepMode(DebugContext.StepMode mode, int currentDepth, Target currentTarget) {
		this.stepMode = mode;
		this.stepReferenceDepth = currentDepth;
		this.stepReferenceTarget = currentTarget;
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
	 * Blocks the calling thread until {@link #resume()} is called.
	 */
	public void awaitResume() throws InterruptedException {
		pauseLatch = new CountDownLatch(1);
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
}
