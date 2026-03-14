package com.laytonsmith.PureUtilities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A daemon manager allows for a layer on top of Java threads to determine if there are any threads open that are
 * actually doing any processing. This allows for a thread to wait for all spinoff threads before exiting, whether or
 * not those threads are daemon threads or not. All the methods in this class are threadsafe.
 */
public class DaemonManager {

	/**
	 * Listener interface for thread lifecycle events.
	 */
	public interface ThreadLifecycleListener {
		/**
		 * Called when a thread is activated (registered as active).
		 * @param t The thread that was activated
		 * @param displayName A user-facing display name for the thread, or null
		 *     if no explicit name was provided (in which case the thread's Java
		 *     name should be used as a fallback).
		 */
		void onActivated(Thread t, String displayName);

		/**
		 * Called when a thread is deactivated (no longer active).
		 * @param t The thread that was deactivated
		 */
		void onDeactivated(Thread t);
	}

	private final Object lock = new Object();
	private final Set<Thread> threads = new HashSet<>();
	private final List<ThreadLifecycleListener> listeners = new ArrayList<>();
	private int count = 0;

	/**
	 * Adds a listener that will be notified when threads are activated or deactivated.
	 * @param listener The listener to add
	 */
	public void addThreadLifecycleListener(ThreadLifecycleListener listener) {
		synchronized(lock) {
			listeners.add(listener);
		}
	}

	/**
	 * Removes a previously added lifecycle listener.
	 * @param listener The listener to remove
	 */
	public void removeThreadLifecycleListener(ThreadLifecycleListener listener) {
		synchronized(lock) {
			listeners.remove(listener);
		}
	}

	/**
	 * Sets a thread to "daemon" mode, meaning it is currently active. Null may be sent, in which case the current
	 * thread is used. You should always put a deactivateThread call for every activateThread call.
	 *
	 * @param t The thread to activate
	 */
	public void activateThread(Thread t) {
		activateThread(t, null);
	}

	/**
	 * Sets a thread to "daemon" mode with an explicit display name. Null may be sent for the thread,
	 * in which case the current thread is used.
	 *
	 * @param t The thread to activate
	 * @param displayName A user-facing name for the thread, or null to use the Java thread name
	 */
	public void activateThread(Thread t, String displayName) {
		Thread resolved;
		List<ThreadLifecycleListener> snapshot;
		synchronized(lock) {
			resolved = t != null ? t : Thread.currentThread();
			threads.add(resolved);
			++count;
			snapshot = new ArrayList<>(listeners);
		}
		for(ThreadLifecycleListener listener : snapshot) {
			listener.onActivated(resolved, displayName);
		}
	}

	/**
	 * Sets a thread to "non daemon" mode, meaning it is currently inactive. If null, the current thread is used.
	 *
	 * @param t The thread to deactivate
	 */
	public void deactivateThread(Thread t) {
		Thread resolved;
		List<ThreadLifecycleListener> snapshot;
		synchronized(lock) {
			resolved = t != null ? t : Thread.currentThread();
			threads.remove(resolved);
			--count;
			lock.notify();
			snapshot = new ArrayList<>(listeners);
		}
		for(ThreadLifecycleListener listener : snapshot) {
			listener.onDeactivated(resolved);
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
