package com.laytonsmith.core.taskmanager;

import com.laytonsmith.core.constructs.Target;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A Task is a single task running on the system. There are various methods
 * for a particular task, from simply obtaining information about the task, to
 * controlling various aspects of the task.
 */
public class Task implements Comparable<Task>{
	
	private final String creationThreadId;
	private Thread runningThread = null;
	private final String id;
	private final int numericId;
	private boolean activated = false;
	private final Target source;
	private String description = null;
	private boolean interrupted = false;
	private boolean interruptedGracefully = false;
	private final TaskType type;
	
	private static final AtomicInteger counter = new AtomicInteger(0);
	/**
	 * This creates a new Task. The id must be totally unique for all
	 * tasks, though if you send null for the id, a guaranteed unique id
	 * will be provided for the task.
	 * @param id The unique id for this task, possibly null, in which case
	 * one will be assigned for you.
	 * @param source The location that this task was registered, possibly
	 * null if inapplicable.
	 */
	public Task(String id, Target source, TaskType type){
		if(id == null){
			UUID uuid = UUID.randomUUID();
			this.id = uuid.toString();
		} else {
			this.id = id;
		}
		this.source = source;
		this.creationThreadId = Thread.currentThread().getName();
		this.numericId = counter.incrementAndGet();
		this.type = type;
	}
	
	/**
	 * Returns the id of the thread that was running when this task
	 * was created.
	 * @return 
	 */
	public String getCreationThreadId(){
		return creationThreadId;
	}
	
	/**
	 * Sets the thread that is running this task. Should be set by the
	 * task when it starts up.
	 * @param t
	 * @return 
	 */
	public synchronized Task setRunningThread(Thread t){
		this.runningThread = t;
		return this;
	}
	
	/**
	 * Returns a reference to the thread that is currently running
	 * this task, or null if isActivated would return false.
	 * @return 
	 */
	public synchronized Thread getRunningThread(){
		return runningThread;
	}
	
	/**
	 * Returns the id for this task
	 * @return 
	 */
	public String getId(){
		return id;
	}
	
	/**
	 * Returns the numeric id that was automatically generated for this task.
	 * @return 
	 */
	public int getNumericId(){
		return numericId;
	}
	
	/**
	 * Should be set by the task when it starts up and finishes.
	 * @param activated
	 * @return 
	 */
	public synchronized Task setActivated(boolean activated){
		this.activated = activated;
		return this;
	}
	
	/**
	 * Returns true if this task is currently activated; that is, it is running.
	 * @return 
	 */
	public synchronized boolean isActivated(){
		return activated;
	}
	
	/**
	 * Returns where the task was defined in source, or possibly null
	 * if not applicable.
	 * @return 
	 */
	public Target getSource(){
		return source;
	}
	
	/**
	 * Sets the description, which is used in the toString of this object.
	 * @param description
	 * @return 
	 */
	public synchronized Task setDescription(String description){
		this.description = description;
		return this;
	}

	/**
	 * Returns a string representation of this object. If setDescription
	 * has been called with a non-null value, that is returned, otherwise,
	 * it is set to a default value.
	 * @return 
	 */
	@Override
	public String toString() {
		if(description == null){
			return id + ":" + source;
		} else {
			return description;
		}
	}
	
	/**
	 * Convenience method that sets all the correct parameters
	 * when this task is started. It should only be called from the
	 * thread that will actually be running the task.
	 * @return 
	 */
	public synchronized Task start(){
		setActivated(true);
		setRunningThread(Thread.currentThread());
		interrupted = false;
		interruptedGracefully = false;
		return this;
	}
	
	/**
	 * Convenience method that sets all the correct parameters
	 * when this task is stopped.
	 * @return 
	 */
	public synchronized Task stop(){
		setActivated(false);
		setRunningThread(null);
		interrupted = false;
		interruptedGracefully = false;
		return this;
	}
	
	/**
	 * Sets the interrupt flag for this task. This can be used to prematurely end
	 * a task. All tasks should monitor the status of this flag, and shut down as quickly
	 * as possible once they detect this flag is true, even if it may cause
	 * system instability. If the task is not running, calling
	 * this will have no effect.
	 */
	public synchronized void interrupt(){
		if(isActivated()){
			interrupted = true;
		}
	}
	
	/**
	 * If this is true, the task has been signalled to stop by an external
	 * source. The task should terminate as soon as possible, even if this means
	 * leaving the system in an unstable state.
	 * @return 
	 */
	public synchronized boolean isInterrupted(){
		return interrupted;
	}
	
	/**
	 * Sets the interrupt flag for this task. This can be used to prematurely end
	 * a task. All tasks should monitor the status of this flag, and shut down as quickly
	 * as possible once they detect this flag is true, although they should do any cleanup
	 * required to ensure continued system stability. If the task is not running, calling
	 * this will have no effect.
	 */
	public synchronized void interruptGracefully(){
		if(isActivated()){
			interruptedGracefully = true;
		}
	}
	
	/**
	 * If this is true, the task has been signalled to stop by an external
	 * source. The task should terminate as soon as possible, however, the task
	 * will first do whatever is necessary to shutdown without causing any system instability.
	 * @return 
	 */
	public synchronized boolean isInterruptedGracefully(){
		return interruptedGracefully;
	}

	public int compareTo(Task o) {
		return (this.numericId < o.numericId?1:-1);
	}
	
	public TaskType getType(){
		return type;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + (this.id != null ? this.id.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Task other = (Task) obj;
		if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
			return false;
		}
		return true;
	}
}
