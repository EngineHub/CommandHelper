package com.laytonsmith.core.taskmanager;

import com.laytonsmith.core.Documentation;

/**
 * A TaskType is a type of task that can be plugged into the task manager.
 */
public interface TaskType extends Documentation {

	/**
	 * Returns the display name of the task type
	 * @return
	 */
	String displayName();

	/**
	 * Returns the enum name of the task type.
	 * @return
	 */
	String name();

}
