package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.taskmanager.CoreTaskType;
import com.laytonsmith.core.taskmanager.TaskHandler;
import com.laytonsmith.core.taskmanager.TaskManager;
import com.laytonsmith.core.taskmanager.TaskState;

/**
 *
 */
public class TaskHandling {

	public static String docs() {
		return "This class is used to manage various tasks throughout MethodScript. It is a task manager of sorts.";
	}

	@api
	@hide("Only timeouts are added currently, making it mostly useless")
	public static class tm_get_tasks extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			TaskManager tm = environment.getEnv(GlobalEnv.class).GetTaskManager();
			CArray ret = new CArray(t);
			for(TaskHandler task : tm.getTasks()) {
				CArray tt = CArray.GetAssociativeArray(t);
				tt.set("id", new CInt(task.getID(), t), t);
				tt.set("type", task.getType().name());
				tt.set("state", task.getState().name());
				tt.set("target", task.getDefinedAt().toString());
				CArray properties = CArray.GetAssociativeArray(t);
				for(String prop : task.getProperties()) {
					properties.set(prop, task.getPropertyData().get(prop).toString());
				}
				tt.set("properties", properties, t);
				ret.push(tt, t);
			}
			return ret;
		}

		@Override
		public String getName() {
			return "tm_get_tasks";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} Returns an array of currently running tasks. ---- Each task will be an associative array with the"
					+ " following properties:\n"
					+ "{| width=\"100%\" cellspacing=\"1\" cellpadding=\"1\" border=\"1\" class=\"wikitable\"\n"
					+ "|-\n"
					+ "! scope=\"col\" | Key\n"
					+ "! scope=\"col\" | Value\n"
					+ "|-\n"
					+ "| id\n"
					+ "| The id of the currently running task. This is not a unique id necessarily, across all tasks, but will be unique"
					+ " across all tasks of this task type.\n"
					+ "|-\n"
					+ "| type\n"
					+ "| The task type. This plus the id are the unique identifiers for a task. Extensions may add new task types, but the"
					+ " builtin tasks are: " + StringUtils.Join(CoreTaskType.values(), ", ", ", and ") + "\n"
					+ "|-\n"
					+ "| state\n"
					+ "| The state of the task. Will be either " + StringUtils.Join(TaskState.values(), ", ", ", or ") + "\n"
					+ "|-"
					+ "| target\n"
					+ "| Where the task was defined in code.\n"
					+ "|-\n"
					+ "| properties\n"
					+ "| An array of additional properties provided by the particular task type. May be empty (but not null).\n"
					+ "|}\n";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	@hide("This doesn't appear to work yet. It will be added once it does.")
	public static class tm_kill_task extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String type = args[0].val();
			int id = Static.getInt32(args[1], t);
			TaskManager tm = environment.getEnv(GlobalEnv.class).GetTaskManager();
			tm.killTask(type, id);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "tm_kill_task";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {taskType, id} Attempts to kill the specified task. The taskType and id will be listed with the task in the task manager."
					+ " If the task is already finished, doesn't exist, or already in the process of finishing, nothing happens.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}
}
