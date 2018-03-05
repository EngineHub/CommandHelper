package com.laytonsmith.core.taskmanager;

/**
 * Fired when a task's state is changed
 */
public interface TaskStateChangeListener {

	/**
	 * Fired when a task's state has changed.
	 *
	 * @param from The old state
	 * @param task The TaskHandler for this task. The new state can be determined from this object.
	 */
	void taskStateChanged(TaskState from, TaskHandler task);

}
