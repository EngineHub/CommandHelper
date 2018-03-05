package com.laytonsmith.core.taskmanager;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.annotations.taskhandler;
import com.laytonsmith.core.constructs.Target;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A TaskHandler is an object that actually manages the various task types. It knows several things, including how to
 * get display information about a particular task, as well as how to manipulate the task (and if the task can be
 * manipulated at all).
 */
public abstract class TaskHandler {

	private final Set<TaskStateChangeListener> stateChangeListeners = new HashSet<>();
	private final taskhandler annotation;
	private TaskState state = TaskState.REGISTERED;
	private TaskType type;
	private int id;
	private Target target;

	protected TaskHandler(TaskType type, int id, Target target) {
		this.annotation = this.getClass().getAnnotation(taskhandler.class);
		if (this.annotation == null) {
			throw new RuntimeException("All instances of TaskHandler must be tagged with the @taskhandler");
		}
		this.type = type;
		this.id = id;
		this.target = target;
	}

	/**
	 * Adds a state change listener to the task manager. When a task's state changes, it will be sent to all listeners.
	 *
	 * @param listener
	 */
	public void addStateChangeListener(TaskStateChangeListener listener) {
		stateChangeListeners.add(listener);
	}

	/**
	 * Removes a state change listener.
	 *
	 * @param listener
	 */
	public void removeStateChangeListener(TaskStateChangeListener listener) {
		stateChangeListeners.remove(listener);
	}

	public synchronized void changeState(TaskState changeTo) {
		TaskState old = this.getState();
		this.state = changeTo;
		for (TaskStateChangeListener listener : stateChangeListeners) {
			listener.taskStateChanged(old, this);
		}
	}

	/**
	 * Returns the current state of the task.
	 *
	 * @return
	 */
	public final TaskState getState() {
		return this.state;
	}

	/**
	 * Returns a list of properties
	 *
	 * @return
	 */
	public final String[] getProperties() {
		return annotation.properties();
	}

	/**
	 * Returns a map of properties and their data.
	 *
	 * @return
	 */
	public final Map<String, Object> getPropertyData() {
		Map<String, Object> data = new HashMap<>();
		for (String prop : getProperties()) {
			data.put(prop, ReflectionUtils.get(this.getClass(), this, "get" + prop));
		}
		return data;
	}

	/**
	 * Returns the id of the task. This is not necessarily unique across all tasks, but should be unique across each
	 * task type. Another id, based on the task
	 *
	 * @return
	 */
	public final int getID() {
		return id;
	}

	/**
	 * Gets the TaskType of this task.
	 *
	 * @return
	 */
	public final TaskType getType() {
		return type;
	}

	/**
	 * Returns the code target for where this task was defined at.
	 *
	 * @return
	 */
	public final Target getDefinedAt() {
		return target;
	}

	/**
	 * Attempts to kill the task.
	 */
	public abstract void kill();
}
