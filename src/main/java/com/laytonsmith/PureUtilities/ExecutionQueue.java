package com.laytonsmith.PureUtilities;

import java.util.List;

/**
 *
 */
public interface ExecutionQueue {

	/**
	 * Returns a list of active queues; that is, isRunning will return true for all these queues.
	 *
	 * @return
	 */
	List<String> activeQueues();

	/**
	 * Clears all elements from this queue
	 *
	 * @param queue
	 */
	void clear(String queue);

	/**
	 * Returns true if this queue has elements on the queue, or is currently running one.
	 *
	 * @param queue
	 * @return
	 */
	boolean isRunning(String queue);

	/**
	 * Pushes a new runnable onto the end of the specified queue
	 *
	 * @param queue The named queue
	 * @param r
	 */
	void push(DaemonManager dm, String queue, Runnable r);

	/**
	 * Pushes a new element to the front of the queue, barring other calls to pushFront, this runnable will go next.
	 *
	 * @param queue
	 * @param r
	 */
	void pushFront(DaemonManager dm, String queue, Runnable r);

	/**
	 * Removes the last element added to the back of the queue
	 *
	 * @param queue
	 */
	void remove(String queue);

	/**
	 * Removes the front element from the queue
	 *
	 * @param queue
	 */
	void removeFront(String queue);

	void setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler exceptionHandler);

	/**
	 * Attempts an orderly shutdown of all existing tasks.
	 */
	void stopAll();

	/**
	 * Stops all executing tasks on a best effort basis.
	 */
	void stopAllNow();

}
