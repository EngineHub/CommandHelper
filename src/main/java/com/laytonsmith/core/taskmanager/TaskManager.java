package com.laytonsmith.core.taskmanager;

import java.util.List;

/**
 * A TaskManager is an object that is aware of, and can control various tasks in MethodScript.
 */
public interface TaskManager {

	/**
	 * Adds a task to the list. Once the task is finalized, it is automatically removed from this list.
	 *
	 * @param task
	 */
	void addTask(final TaskHandler task);

	/**
	 * Gets a task, given the task type and id
	 *
	 * @param type
	 * @param id
	 * @return
	 */
	TaskHandler getTask(TaskType type, int id);

	/**
	 * Gets a task, given a string representation of the task type, and id.
	 *
	 * @param type
	 * @param id
	 * @return
	 */
	TaskHandler getTask(String type, int id);

	/**
	 * Returns a list of existing tasks
	 *
	 * @return
	 */
	List<TaskHandler> getTasks();

	/**
	 * Attempts to kill the given task. If the task isn't registered, already dead, or in the process of dying, nothing
	 * happens.
	 *
	 * @param id
	 * @param type
	 */
	void killTask(TaskType type, int id);

	/**
	 * Attempts to kill the given task. If the task isn't registered, already dead, or in the process of dying, nothing
	 * happens.
	 *
	 * @param type A string representation of the task type
	 * @param id
	 */
	void killTask(String type, int id);

}
