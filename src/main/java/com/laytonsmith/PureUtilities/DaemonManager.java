
package com.laytonsmith.PureUtilities;

import java.util.HashSet;
import java.util.Set;

/**
 * A daemon manager allows for a layer on top of Java threads to determine
 * if there are any threads open that are actually doing any processing.
 * This allows for a thread to wait for all spinoff threads before exiting, whether
 * or not those threads are daemon threads or not. All the methods in this class
 * are threadsafe.
 */
public class DaemonManager {
	private final Object lock = new Object();
	private final Set<Thread> threads = new HashSet<Thread>();
	private int count = 0;
	
	/**
	 * Sets a thread to "daemon" mode, meaning it is currently
	 * active. Null may be sent, in which case a simple count is kept,
	 * but otherwise isn't added to the active threads list. You should
	 * always put a deactivateThread call for every activateThread call.
	 * @param t The thread to activate
	 */
	public void activateThread(Thread t){
		synchronized(lock){
			if(t != null){
				threads.add(t);
			}
			++count;
		}
	}
	
	/**
	 * Sets a thread to "non daemon" mode, meaning it is currently
	 * inactive.
	 * @param t The thread to deactivate
	 */
	public void deactivateThread(Thread t){
		synchronized(lock){
			if(t != null){
				threads.remove(t);
			}
			--count;
			lock.notify();
		}
	}
	
	/**
	 * Returns an array of all active threads. Threads may have started
	 * themselves with null, so they won't be listed here. This is for informational
	 * purposes only.
	 * @return 
	 */
	public Thread[] getActiveThreads(){
		synchronized(lock){
			return threads.toArray(new Thread[threads.size()]);
		}
	}
	
	/**
	 * Waits for all threads to finish, then returns.
	 * @throws InterruptedException 
	 */
	public void waitForThreads() throws InterruptedException{
		synchronized(lock){
			while(count > 0){
				lock.wait();
			}
		}
	}
	
}
