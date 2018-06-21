package com.laytonsmith.PureUtilities;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;

/**
 * This class provides a framework of simple hooks to run critical code on the main thread for code that is running off
 * the main thread.
 *
 * The main operations for code that uses this class are invokeNow and invokeLater, and the main operations for code
 * that implements this class are the abstract methods, and the configuration methods.
 *
 *
 */
public abstract class ThreadPump {

	private final int minHoldTime;
	private final int maxHoldTime;
	private final int waitTime;
	private final Queue<Runnable> eventPump;
	private int startStack;
	private boolean pumpStarted;
	private final String threadName;

	private final Object waitForStopLock;
	private final Object waitForTaskLock;

	private long lockTime;
	private long idleTime;

	/**
	 * Creates a new ThreadPump object. All times are in milliseconds.
	 *
	 * @param minHoldTime The minimum amount of time to wait for the next operation before giving up control of the main
	 * thread again. This should be set to 0 if there is no or negligible penalty for regaining the main thread once
	 * given up.
	 * @param maxHoldTime The max time that the main thread should be monopolized before it is given up. This is useful
	 * to prevent starvation.
	 * @param waitTime The amount of time to wait before resuming if the maxHoldTime was met, and thread control was
	 * returned.
	 */
	protected ThreadPump(int minHoldTime, int maxHoldTime, int waitTime, String threadName) {
		eventPump = new LinkedList<Runnable>();
		startStack = 0;
		waitForStopLock = new Object();
		waitForTaskLock = new Object();
		pumpStarted = false;
		this.minHoldTime = minHoldTime;
		this.maxHoldTime = maxHoldTime;
		this.waitTime = waitTime;
		this.threadName = threadName;
	}

	/**
	 * Starts a transaction. This resets all the mechanisms for wait times. For each start() call, there must be exactly
	 * one stop() call, however, they may be called multiple times, and only when the last stop() is called will it
	 * actually take effect.
	 */
	public synchronized void start() {
		startStack++;
	}

	/**
	 * Stops a transaction, and immediately returns control to the main thread, if this is the last stop() method to be
	 * called.
	 */
	public synchronized void stop() {
		startStack--;
		if(startStack < 0) {
			throw new RuntimeException("stop() called too many times!");
		}
	}

	/**
	 * Stops a transaction, but waits for all tasks to complete before returning. This should ONLY be called if this is
	 * the top level stop, and an exception will be thrown if the start stack is not 1.
	 */
	public void waitForStop() throws InterruptedException {
		if(startStack != 1) {
			throw new RuntimeException("waitForStop called from an inner invocation");
		}
		synchronized(waitForStopLock) {
			waitForStopLock.wait();
		}
	}

	/**
	 * Runs a task on the main thread at some point. This returns immediately. Tasks queued up will be run in order, but
	 * at some indeterminate time in the future.
	 *
	 * @param runnable
	 */
	public void invokeLater(Runnable runnable) {
		eventPump.add(runnable);
		startPump();
	}

	/**
	 * Runs a task on the main thread and waits for it to complete. Tasks submitted are queued up in order, so the task
	 * may not get run immediately, and the task completion time is still dependant on the thread starvation parameters.
	 *
	 * @param callable
	 * @return
	 */
	public Object invokeNow(final Callable<?> callable) throws InterruptedException {
		final Object myLock = new Object();
		final Object[] ret = new Object[1];
		invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					ret[0] = callable.call();
					synchronized(myLock) {
						myLock.notifyAll();
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		synchronized(myLock) {
			myLock.wait();
		}
		return ret[0];
	}

	private void startPump() {
		if(!pumpStarted) {
			synchronized(this) {
				lockTime = System.currentTimeMillis();
				pumpStarted = true;
				new Thread(new Runnable() {
					@Override
					public void run() {
						doPump();
					}
				}, threadName).start();
			}
		}
		synchronized(waitForTaskLock) {
			waitForTaskLock.notifyAll();
		}
	}

	@SuppressWarnings("SleepWhileInLoop")
	private void doPump() {
		while(pumpStarted) {
			runOnMainThread(new Runnable() {

				@SuppressWarnings("NestedSynchronizedStatement")
				@Override
				public void run() {
					//This condition happens when we are done with the thread
					//AND there are no pending events.
					if(eventPump.isEmpty() && startStack == 0) {
						synchronized(ThreadPump.this) {
							pumpStarted = false;
							return;
						}
					}
					//This condition happens when we need to wait
					if(eventPump.isEmpty() && startStack > 0) {
						long now = System.currentTimeMillis();
						//Our wait time is the maximum time left before we reach
						//the min hold time.
						long waitTime = lockTime + minHoldTime - now;
						synchronized(waitForTaskLock) {
							try {
								waitForTaskLock.wait(waitTime);
								//Well, no tasks were given, so let's return control
								//until further notice.
								synchronized(ThreadPump.this) {
									pumpStarted = false;
									return;
								}
							} catch (InterruptedException ex) {
								//We were notified that a new task is present,
								//so we'll continue down to the next condition.
							}
						}
					}
					if(!eventPump.isEmpty()) {
						Runnable task = eventPump.poll();
						if(task != null) {
							task.run();
						}
						//Now check against the max time. If this is greater than that, let's
						//return control to the main thread for waitTime.
						long now = System.currentTimeMillis();
						if(now > lockTime + maxHoldTime) {
							idleTime = waitTime;
							return;
						}
					}
				}
			});
			if(idleTime > 0) {
				try {
					Thread.sleep(idleTime);
					idleTime = 0;
				} catch (InterruptedException ex) {
					//
				}
			}
		}
	}

	/**
	 * Runs a task on the main thread, immediately. This task will be an encapsulation of the "blocking calls" that this
	 * class provides.
	 *
	 * @param r
	 */
	protected abstract void runOnMainThread(Runnable r);

}
