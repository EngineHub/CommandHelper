package com.laytonsmith.PureUtilities;

import java.util.HashSet;
import java.util.Set;

/**
 * A daemon manager allows for a layer on top of Java threads to determine if there are any threads open that are
 * actually doing any processing. This allows for a thread to wait for all spinoff threads before exiting, whether or
 * not those threads are daemon threads or not. All the methods in this class are threadsafe.
 */
public class DaemonManager {

	private final Object lock = new Object();
	private final Set<Thread> threads = new HashSet<>();
	private int count = 0;

	/**
	 * Sets a thread to "daemon" mode, meaning it is currently active. Null may be sent, in which case the current
	 * thread is used. You should always put a deactivateThread call for every activateThread call.
	 *
	 * @param t The thread to activate
	 */
	public void activateThread(Thread t) {
		synchronized(lock) {
			if(t != null) {
				threads.add(t);
			} else {
				threads.add(Thread.currentThread());
			}
			++count;
		}
	}

	/**
	 * Sets a thread to "non daemon" mode, meaning it is currently inactive. If null, the current thread is used.
	 *
	 * @param t The thread to deactivate
	 */
	public void deactivateThread(Thread t) {
		synchronized(lock) {
			if(t != null) {
				threads.remove(t);
			} else {
				threads.remove(Thread.currentThread());
			}
			--count;
			lock.notify();
		}
	}

	/**
	 * Returns an array of all active threads.
	 *
	 * @return
	 */
	public Thread[] getActiveThreads() {
		synchronized(lock) {
			return threads.toArray(new Thread[threads.size()]);
		}
	}

	/**
	 * Waits for all threads to finish, then returns.
	 *
	 * @throws InterruptedException
	 */
	public void waitForThreads() throws InterruptedException {
		synchronized(lock) {
			while(count > 0) {
				lock.wait();
			}
		}
	}

}
