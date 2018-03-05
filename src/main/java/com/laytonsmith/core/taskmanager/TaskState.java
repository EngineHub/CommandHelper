package com.laytonsmith.core.taskmanager;

/**
 * A TaskState is the state in which a task currently is in.
 */
public enum TaskState {

	/**
	 * The task has been registered, but not yet determined to be either IDLE or RUNNING. This should immediately be
	 * followed by another state.
	 */
	REGISTERED(false),
	/**
	 * The task is registered, but not currently active
	 */
	IDLE(false),
	/**
	 * The task is currently running
	 */
	RUNNING(false),
	/**
	 * The task completed normally, and will be automatically removed from the task list.
	 */
	FINISHED(true),
	/**
	 * The task was killed by some abnormal means. This does not include exceptions, but rather say, being killed by the
	 * task manager.
	 */
	KILLED(true),;

	private final boolean finalized;

	private TaskState(boolean finalized) {
		this.finalized = finalized;
	}

	/**
	 * Returns true if this state means the task is finalized, that is, it can probably be removed from lists of
	 * "active" tasks.
	 */
	public boolean isFinalized() {
		return this.finalized;
	}
}
