package com.laytonsmith.core.taskmanager;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class keeps track of all of the tasks currently running or on standby in the system.
 * All currently executing code will be listed in the task manager, however some tasks may automatically
 * unregister themselves once they finish, and so will no longer show up in the list of tasks. There is
 * only one task manager for the whole system, so there is only a static access method to retrieve the
 * task manager. All critical TaskManager methods are threadsafe, unless otherwise noted.
 */
public class TaskManager {
	/**
	 * The instance of this task manager.
	 */
	private static volatile TaskManager instance = null;
	
	private final Set<Task> registeredTasks = Collections.synchronizedSet(new TreeSet<Task>());
	
	/**
	 * Only this class can instantiate.
	 */
	private TaskManager(){
		
	}
	
	public static TaskManager getTaskManager(){
		if(instance == null){
			synchronized(TaskManager.class){
				if(instance == null){
					instance = new TaskManager();
				}
			}
		}
		return instance;
	}
	
	/**
	 * Adds a new task to the list of registered tasks.
	 * @param t 
	 */
	public void register(Task t){
		registeredTasks.add(t);
	}
	
	/**
	 * Removes a task from the list of registered tasks.
	 * @param t 
	 */
	public void unregister(Task t){
		registeredTasks.remove(t);
	}
	
	/**
	 * Given a task id, returns the task for that id.
	 * @param id
	 * @return 
	 */
	public Task getTaskById(String id){
		synchronized(registeredTasks){
			for(Task t : registeredTasks){
				if(t.getId().equals(id)){
					return t;
				}
			}
			return null;
		}
	}
	
	/**
	 * Returns a copy of the internal task list, which may be iterated
	 * over by external code, independent of other threads.
	 * @return 
	 */
	public Set<Task> getTaskList(){
		return new TreeSet<Task>(registeredTasks);
	}
	
	/**
	 * Returns a formatted task list, suitable for outputting to standard out, or other
	 * monospaced formats.
	 * @return 
	 */
	public String taskListing(){
		StringBuilder b = new StringBuilder();
		String header = "%4s%50s%50s";
		String format = "%4d%50s%50s";
		b.append(String.format(header, "ID", "Description", "Source"));
		synchronized(registeredTasks){
			for(Task t : registeredTasks){
				b.append(String.format(format, t.getNumericId(), t.toString(), t.getSource().toString()));
			}
		}
		return b.toString();
	}
}
