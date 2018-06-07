package com.laytonsmith.PureUtilities;

import com.laytonsmith.PureUtilities.Common.StreamUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class manages execution queues. A task added to a queue is guaranteed to be sequential with respect to other
 * tasks in that queue, but not necessarily with respect to other tasks on other queues. Tasks will block the queue.
 *
 */
public class ExecutionQueue {

	private ExecutorService service;
	private static int threadCount = 0;
	private Map<String, Deque<Runnable>> queues;
	private final Map<String, Object> locks;
	private Map<String, Boolean> runningQueues;
	private String defaultQueueName;
	private Thread.UncaughtExceptionHandler uncaughtExceptionHandler = null;
	private ThreadFactory threadFactory;

	public ExecutionQueue(String threadPrefix, String defaultQueueName) {
		this(threadPrefix, defaultQueueName, null);
	}

	/**
	 * Creates a new ExecutionQueue instance.
	 *
	 * @param threadPrefix The prefix to use when naming the threads
	 * @param defaultQueueName The name of the default queue
	 * @param exceptionHandler The uncaught exception handler for these queues
	 * @throws NullPointerException if either threadPrefix or defaultQueueName are null
	 */
	public ExecutionQueue(final String threadPrefix, String defaultQueueName, final Thread.UncaughtExceptionHandler exceptionHandler) {
		if(threadPrefix == null || defaultQueueName == null) {
			throw new NullPointerException();
		}
		uncaughtExceptionHandler = exceptionHandler;
		threadFactory = new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, threadPrefix + "-" + (++threadCount));
				t.setDaemon(false);
				return t;
			}
		};

		queues = new HashMap<String, Deque<Runnable>>();
		this.defaultQueueName = defaultQueueName;
		locks = new HashMap<String, Object>();
		runningQueues = new HashMap<String, Boolean>();
	}

	public final void setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler exceptionHandler) {
		this.uncaughtExceptionHandler = exceptionHandler;
	}

	/**
	 * Pushes a new runnable onto the end of the specified queue
	 *
	 * @param queue The named queue
	 * @param r
	 */
	public final void push(DaemonManager dm, String queue, Runnable r) {
		queue = prepareLock(queue);
		synchronized(locks.get(queue)) {
			Deque<Runnable> q = prepareQueue(queue);
			q.addLast(r);
			startQueue(dm, queue);
		}
	}

	/**
	 * Pushes a new element to the front of the queue, barring other calls to pushFront, this runnable will go next.
	 *
	 * @param queue
	 * @param r
	 */
	public final void pushFront(DaemonManager dm, String queue, Runnable r) {
		queue = prepareLock(queue);
		synchronized(locks.get(queue)) {
			Deque<Runnable> q = prepareQueue(queue);
			q.addFirst(r);
			startQueue(dm, queue);
		}
	}

	/**
	 * Removes the last element added to the back of the queue
	 *
	 * @param queue
	 */
	public final void remove(String queue) {
		queue = prepareLock(queue);
		synchronized(locks.get(queue)) {
			Deque<Runnable> q = prepareQueue(queue);
			try {
				q.removeLast();
			} catch (NoSuchElementException e) {
				//
			}
		}
	}

	/**
	 * Removes the front element from the queue
	 *
	 * @param queue
	 */
	public final void removeFront(String queue) {
		try {
			pop(queue);
		} catch (NoSuchElementException e) {
			//
		}
	}

	/**
	 * Clears all elements from this queue
	 *
	 * @param queue
	 */
	public final void clear(String queue) {
		queue = prepareLock(queue);
		synchronized(locks.get(queue)) {
			prepareQueue(queue).clear();
		}
	}

	/**
	 * Returns true if this queue has elements on the queue, or is currently running one.
	 *
	 * @param queue
	 * @return
	 */
	public final boolean isRunning(String queue) {
		queue = prepareLock(queue);
		synchronized(locks.get(queue)) {
			return runningQueues.containsKey(queue) && runningQueues.get(queue).equals(true);
		}
	}

	/**
	 * Returns a list of active queues; that is, isRunning will return true for all these queues.
	 *
	 * @return
	 */
	public final List<String> activeQueues() {
		List<String> q = new ArrayList<String>();
		for(String queue : queues.keySet()) {
			synchronized(locks.get(queue)) {
				if(queues.containsKey(queue) && !queues.get(queue).isEmpty()) {
					q.add(queue);
				}
			}
		}
		return q;
	}

	/**
	 * Sets up a queue initially
	 *
	 * @param queueName
	 */
	private Deque<Runnable> prepareQueue(String queueName) {
		if(!queues.containsKey(queueName)) {
			queues.put(queueName, new ArrayDeque<Runnable>());
		}
		return queues.get(queueName);
	}

	private String prepareLock(String queueName) {
		if(queueName == null) {
			queueName = defaultQueueName;
		}
		if(!locks.containsKey(queueName)) {
			locks.put(queueName, new Object());
		}
		return queueName;
	}

	/**
	 * Destroys a no-longer-in-use queue
	 *
	 * @param queueName
	 */
	private void destroyQueue(String queueName) {
		synchronized(locks.get(queueName)) {
			queues.remove(queueName);
		}
	}

	/**
	 * This method actually runs the queue management
	 *
	 * @param queueName
	 */
	private void pumpQueue(String queueName) {
		while(true) {
			Runnable r;
			synchronized(locks.get(queueName)) {

				r = pop(queueName);
			}
			r.run();
			synchronized(locks.get(queueName)) {
				if(queues.get(queueName).isEmpty()) {
					runningQueues.put(queueName, false);
					destroyQueue(queueName);
					break;
				}
			}
		}
	}

	private synchronized void startQueue(final DaemonManager dm, final String queue) {
		synchronized(locks.get(queue)) {
			if(!isRunning(queue)) {
				//We need to create a new thread
				runningQueues.put(queue, true);
				if(dm != null) {
					dm.activateThread(null);
				}
				if(service == null) {
					service = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
							50L, TimeUnit.MILLISECONDS,
							new SynchronousQueue<Runnable>(),
							threadFactory);
				}
				service.submit(new Runnable() {

					@Override
					public void run() {
						try {
							pumpQueue(queue);
						} catch (RuntimeException t) {
							if(uncaughtExceptionHandler != null) {
								uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), t);
							} else {
								StreamUtils.GetSystemErr().println("The queue \"" + queue + "\" threw an exception, and it was not handled.");
								t.printStackTrace(StreamUtils.GetSystemErr());
							}
						} finally {
							if(dm != null) {
								dm.deactivateThread(null);
							}
						}
					}
				});
			}
		}
	}

	private Runnable pop(String queue) throws NoSuchElementException {
		queue = prepareLock(queue);
		synchronized(locks.get(queue)) {
			Deque<Runnable> q = queues.get(queue);
			if(q == null) {
				throw new NoSuchElementException("The given queue does not exist.");
			}
			return q.removeFirst();
		}
	}

	/**
	 * Stops all executing tasks on a best effort basis.
	 */
	public synchronized void stopAllNow() {
		if(service != null) {
			service.shutdownNow();
			service = null;
		}
	}

	/**
	 * Attempts an orderly shutdown of all existing tasks.
	 */
	public synchronized void stopAll() {
		service.shutdown();
		service = null;
	}

}
