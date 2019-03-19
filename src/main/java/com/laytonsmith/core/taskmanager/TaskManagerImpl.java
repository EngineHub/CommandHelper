package com.laytonsmith.core.taskmanager;

import java.util.ArrayList;
import java.util.List;

/**
 * A TaskManager is an object that is aware of, and can control various tasks in MethodScript.
 */
public class TaskManagerImpl implements TaskManager {

	/**
	 * An internal list of the tasks.
	 */
	private final List<TaskHandler> tasks;

	/**
	 * Creates a new TaskManager
	 */
	public TaskManagerImpl() {
		tasks = new ArrayList<>();
	}

	/**
	 * Returns a list of existing tasks
	 *
	 * @return
	 */
	@Override
	public synchronized List<TaskHandler> getTasks() {
		return new ArrayList<>(tasks);
	}

	/**
	 * Gets a task, given the task type and id
	 *
	 * @param type
	 * @param id
	 * @return
	 */
	@Override
	public synchronized TaskHandler getTask(TaskType type, int id) {
		for(TaskHandler task : getTasks()) {
			if(task.getType().equals(type) && task.getID() == id) {
				return task;
			}
		}
		return null;
	}

	/**
	 * Gets a task, given a string representation of the task type, and id.
	 *
	 * @param type
	 * @param id
	 * @return
	 */
	@Override
	public synchronized TaskHandler getTask(String type, int id) {
		for(TaskHandler task : getTasks()) {
			if(task.getType().name().equals(type) && task.getID() == id) {
				return task;
			}
		}
		return null;
	}

	/**
	 * Attempts to kill the given task. If the task isn't registered, already dead, or in the process of dying, nothing
	 * happens.
	 *
	 * @param id
	 * @param type
	 */
	@Override
	public void killTask(TaskType type, int id) {
		TaskHandler task = getTask(type, id);
		if(task != null) {
			task.kill();
		}
	}

	/**
	 * Attempts to kill the given task. If the task isn't registered, already dead, or in the process of dying, nothing
	 * happens.
	 *
	 * @param type A string representation of the task type
	 * @param id
	 */
	@Override
	public void killTask(String type, int id) {
		TaskHandler task = getTask(type, id);
		if(task != null) {
			task.kill();
		}
	}

	/**
	 * Adds a task to the list. Once the task is finalized, it is automatically removed from this list.
	 *
	 * @param task
	 */
	@Override
	public synchronized void addTask(final TaskHandler task) {
		task.addStateChangeListener(new TaskStateChangeListener() {

			@Override
			public void taskStateChanged(TaskState from, TaskHandler task) {
				if(task.getState().isFinalized()) {
					tasks.remove(task);
				}
			}
		});
		tasks.add(task);
	}

}
