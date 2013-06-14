package com.laytonsmith.core;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

/**
 * The Threader class maintains a pool of threads that are required by the
 * userland code, so that it can be throttled if necessary.
 *
 */
public class Threader {

	private static Threader instance;
	private static int threadCount = 0;

	/**
	 * Returns an instance of a Threader. Each threader could be configured
	 * seperately, but for the time being, it simply returns the Threader
	 * singleton.
	 *
	 * @return The threader instance
	 */
	public static Threader GetThreader() {
		if (instance == null) {
			instance = new Threader();
		}
		return instance;
	}
	ExecutorService execService;

	private Threader() {
		execService = Executors.newCachedThreadPool(new ThreadFactory() {
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, "commandhelper-userland-" + (++threadCount));
				t.setDaemon(false);
				return t;
			}
		});		
	}

	/**
	 * Submits a value-returning task for execution and returns a Future
	 * representing the pending results of the task. The Future's
	 * <tt>get</tt> method will return the task's result upon successful
	 * completion.
	 *
	 * <p> If you would like to immediately block waiting for a task, you
	 * can use constructions of the form <tt>result =
	 * exec.submit(aCallable).get();</tt>
	 *
	 * <p> Note: The {@link Executors} class includes a set of methods that
	 * can convert some other common closure-like objects, for example,
	 * {@link java.security.PrivilegedAction} to {@link Callable} form so
	 * they can be submitted.
	 *
	 * @param task the task to submit
	 * @return a Future representing pending completion of the task
	 * @throws RejectedExecutionException if the task cannot be scheduled
	 * for execution
	 * @throws NullPointerException if the task is null
	 */
	public <T> Future<T> submit(Callable<T> callable) {
		return execService.submit(callable);
	}

	/**
	 * Submits a Runnable task for execution and returns a Future
	 * representing that task. The Future's <tt>get</tt> method will return
	 * the given result upon successful completion.
	 *
	 * @param task the task to submit
	 * @param result the result to return
	 * @return a Future representing pending completion of the task
	 * @throws RejectedExecutionException if the task cannot be scheduled
	 * for execution
	 * @throws NullPointerException if the task is null
	 */
	public <T> Future<T> submit(Runnable task, T result) {
		return execService.submit(task, result);
	}

	/**
	 * Submits a Runnable task for execution and returns a Future
	 * representing that task. The Future's <tt>get</tt> method will return
	 * <tt>null</tt> upon <em>successful</em> completion.
	 *
	 * @param task the task to submit
	 * @return a Future representing pending completion of the task
	 * @throws RejectedExecutionException if the task cannot be scheduled
	 * for execution
	 * @throws NullPointerException if the task is null
	 */
	public Future<?> submit(Runnable task){
		return execService.submit(task);
	}
}
