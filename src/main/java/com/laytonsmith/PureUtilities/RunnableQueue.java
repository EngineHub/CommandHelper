
package com.laytonsmith.PureUtilities;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Creates a new queue
 * @author Layton
 */
public class RunnableQueue {
	private ExecutorService service;
	private static int threadCount = 0;
	
	public RunnableQueue(String threadPrefix){
		this(threadPrefix, null);
	}
	
	public RunnableQueue(final String threadPrefix, final Thread.UncaughtExceptionHandler exceptionHandler){
		if(threadPrefix == null){
			throw new NullPointerException();
		}
		ThreadFactory threadFactory = new ThreadFactory() {

			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, threadPrefix + "-" + (++threadCount));
				t.setDaemon(false);
				if(exceptionHandler != null){
					t.setUncaughtExceptionHandler(exceptionHandler);
				} else {
					t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

						public void uncaughtException(Thread t, Throwable e) {
							System.err.println("The thread " + t.getName() + " threw an exception, and it was not handled.");
							e.printStackTrace(System.err);
						}
					});
				}				
				return t;
			}
		};
		service = Executors.newSingleThreadExecutor(threadFactory);
	}
	
	/**
	 * Schedules a runnable to run whenever the queue pump can get to it. Returns
	 * immediately.
	 * @param r 
	 */
	public void invokeLater(Runnable r){
		service.submit(r);
	}
	
	public <T> T invokeAndWait(Callable<T> callable) throws InterruptedException, ExecutionException{
		return service.submit(callable).get();
	}

	public void shutdown() {
		service.shutdownNow();
	}

}
