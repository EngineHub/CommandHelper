package com.laytonsmith.PureUtilities;

import com.laytonsmith.PureUtilities.Common.StreamUtils;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Creates a new queue
 *
 */
public class RunnableQueue {

	private ExecutorService service;
	private static int threadCount = 0;
	private ThreadFactory threadFactory;

	public RunnableQueue(String threadPrefix) {
		this(threadPrefix, null);
	}

	public RunnableQueue(final String threadPrefix, final Thread.UncaughtExceptionHandler exceptionHandler) {
		if(threadPrefix == null) {
			throw new NullPointerException();
		}
		threadFactory = new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, threadPrefix + "-" + (++threadCount));
				t.setDaemon(false);
				if(exceptionHandler != null) {
					t.setUncaughtExceptionHandler(exceptionHandler);
				} else {
					t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

						@Override
						public void uncaughtException(Thread t, Throwable e) {
							StreamUtils.GetSystemErr().println("The thread " + t.getName() + " threw an exception, and it was not handled.");
							e.printStackTrace(StreamUtils.GetSystemErr());
						}
					});
				}
				return t;
			}
		};
	}

	private void activate() {
		if(service == null) {
			service = Executors.newSingleThreadExecutor(threadFactory);
		}
	}

	/**
	 * Schedules a runnable to run whenever the queue pump can get to it. Returns immediately.
	 *
	 * @param r
	 */
	public void invokeLater(final DaemonManager dm, final Runnable r) {
		if(dm != null) {
			dm.activateThread(null);
		}
		activate();
		service.submit(new Runnable() {

			@Override
			public void run() {
				try {
					r.run();
				} finally {
					if(dm != null) {
						dm.deactivateThread(null);
					}
				}
			}
		});
	}

	public <T> T invokeAndWait(Callable<T> callable) throws InterruptedException, ExecutionException {
		activate();
		return service.submit(callable).get();
	}

	public void shutdown() {
		activate();
		service.shutdownNow();
	}

}
