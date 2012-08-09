package com.laytonsmith.PureUtilities;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 *
 * @author lsmith
 */
public class ExecutionQueue {
	
	private ExecutorService service;
	private static int threadCount = 0;
	private Map<String, Deque<Runnable>> queues;
	private String defaultQueueName;
	
	/**
	 * Creates a new ExecutionQueue instance.
	 * @param threadPrefix The prefix to use when naming the threads
	 * @param defaultQueueName The name of the default queue
	 * @throws NullPointerException if either parameter is null
	 */
	public ExecutionQueue(final String threadPrefix, String defaultQueueName){
		if(threadPrefix == null || defaultQueueName == null){
			throw new NullPointerException();
		}
		service = Executors.newCachedThreadPool(new ThreadFactory() {

			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, threadPrefix + "-" + (++threadCount));
				t.setDaemon(false);
				return t;
			}
		});
		queues = new HashMap<String, Deque<Runnable>>();
		this.defaultQueueName = defaultQueueName;
	}
	
	/**
	 * Pushes a new runnable onto the end of the specified queue
	 * @param queue The named queue
	 * @param r 
	 */
	public void push(String queue, Runnable r){

	}
	
	/**
	 * Removes the last element added to the back of the queue
	 * @param queue 
	 */
	public void remove(String queue){
		
	}
	
	/**
	 * Pushes a new element to the front of the queue, barring other calls
	 * to pushFront, this runnable will go next.
	 * @param queue
	 * @param r 
	 */
	public void pushFront(String queue, Runnable r){
		
	}
	
	/**
	 * Removes the front element from the queue
	 * @param queue 
	 */
	public void removeFront(String queue){
		
	}
	
	/**
	 * Clears all elements from this queue
	 * @param queue 
	 */
	public void clear(String queue){
		
	}
	
	/**
	 * Returns true if this queue has elements on the queue,
	 * or is currently running one.
	 * @param queue
	 * @return 
	 */
	public boolean isRunning(String queue){
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Returns a list of active queues; that is, isRunning will
	 * return true for all these queues.
	 * @return 
	 */
	public List<String> activeQueues(){
		throw new UnsupportedOperationException();		
	}
	
	/**
	 * Sets up a queue initially
	 * @param queueName 
	 */
	private void prepareQueue(String queueName){
		
	}
	
	/**
	 * Destroys a no-longer-in-use queue
	 * @param queueName 
	 */
	private void destroyQueue(String queueName){
		
	}
	
	/**
	 * This method actually runs the queue management 
	 * @param queueName 
	 */
	private void pumpQueue(String queueName){
		
	}
	
	
}
