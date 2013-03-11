package com.laytonsmith.core.taskmanager;

import com.laytonsmith.annotations.documentation;
import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.constructs.CPrimitive;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.MEnum;

/**
 * This enum lists all the types of tasks that can be registered with
 * the task manager.
 */
@typename("TaskType")
public enum TaskType implements MEnum {
	@documentation(docs="This task is an event handler registered with bind().")
	EVENT_HANDLER,
	@documentation(docs="This task is a closure registered with set_timeout(). It runs once, then automatically"
			+ " unregisters from the list.")
	TIMEOUT,
	@documentation(docs="This task is a closure registered with set_interval(). It continues to run until it is"
			+ " stopped with clear_task().")
	INTERVAL,
	@documentation(docs="This is a command handler, registered in the special msa syntax files.")
	COMMAND,
	@documentation(docs="This is the task for the main code, which is run at startup. In most cases, this should not"
			+ " be running, except during startup only. Once the task completes, it unregisters from the list.")
	MAIN,
	@documentation(docs="This is a task launched from an interpreter. Once the script provided finishes, it unregisters"
			+ " from the list, however other standby tasks launched from it may remain.")
	INTERPRETER,
	;

	public Object value() {
		return this;
	}

	public String val() {
		return name();
	}

	public boolean isNull() {
		return false;
	}

	public String typeName() {
		return this.getClass().getAnnotation(typename.class).value();
	}

	public CPrimitive primitive(Target t) throws ConfigRuntimeException {
		throw new Error();
	}

	public boolean isImmutable() {
		return true;
	}
}
