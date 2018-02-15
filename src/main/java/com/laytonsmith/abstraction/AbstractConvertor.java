package com.laytonsmith.abstraction;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractConvertor implements Convertor{

	private final List<Runnable> shutdownHooks = new ArrayList<>();

	@Override
	public void addShutdownHook(Runnable r) {
		shutdownHooks.add(r);
	}

	@Override
	public void runShutdownHooks() {
		// Fire off the shutdown event, before we shut down all the internal hooks
		EventUtils.TriggerListener(Driver.SHUTDOWN, "shutdown", new BindableEvent() {

			@Override
			public Object _GetObject() {
				return new Object();
			}
		});
		Iterator<Runnable> iter = shutdownHooks.iterator();
		while(iter.hasNext()){
			iter.next().run();
			iter.remove();
		}
	}

	/**
	 * Runs the task either now or later. In the case of a default Convertor,
	 * it just runs the task now.
	 * @param dm
	 * @param r 
	 */
	@Override
	public void runOnMainThreadLater(DaemonManager dm, Runnable r) {
		r.run();
	}

	@Override
	public <T> T runOnMainThreadAndWait(Callable<T> callable) throws Exception{
		return (T) callable.call();
	}

	@Override
	public MCWorldCreator getWorldCreator(String worldName) {
		throw new UnsupportedOperationException("Not supported.");
	}
	
	@Override
	public MCCommand getNewCommand(String name) {
		throw new UnsupportedOperationException("Not supported in this implementation.");
	}
	
	@Override
	public MCCommandSender GetCorrectSender(MCCommandSender unspecific) {
		throw new UnsupportedOperationException("Not supported in this implementation.");
	}

	private final Map<Integer, Task> tasks = new HashMap<>();
	private final AtomicInteger taskIDs = new AtomicInteger(0);

	@Override
	public void ClearAllRunnables() {
		synchronized(tasks){
			for(Task task : tasks.values()){
				task.unregister();
			}
			tasks.clear();
		}
	}

	@Override
	public void ClearFutureRunnable(int id) {
		synchronized(tasks){
			if(tasks.containsKey(id)){
				tasks.get(id).unregister();
				tasks.remove(id);
			}
		}
	}

	@Override
	public int SetFutureRepeater(DaemonManager dm, long ms, long initialDelay, final Runnable r) {
		int id = taskIDs.getAndIncrement();
		Task t = new Task(id, dm, true, initialDelay, ms, new Runnable() {

			@Override
			public void run() {
				triggerRunnable(r);
			}
		});
		synchronized(tasks){
			tasks.put(id, t);
			t.register();
		}
		return id;
	}

	@Override
	public int SetFutureRunnable(DaemonManager dm, long ms, final Runnable r) {
		int id = taskIDs.getAndIncrement();
		Task t = new Task(id, dm, false, ms, 0, new Runnable() {

			@Override
			public void run() {
				triggerRunnable(r);
			}
		});
		synchronized(tasks){
			tasks.put(id, t);
			t.register();
		}
		return id;
	}

	/**
	 * A subclass may need to do special handling for the actual trigger of a scheduled
	 * task, though not need to do anything special for the scheduling itself. In this
	 * case, subclasses may override this method, and whenever a scheduled task is
	 * intended to be run, it will be passed to this method instead. By default, the
	 * runnable is simply run.
	 * @param r
	 */
	protected synchronized void triggerRunnable(Runnable r){
		r.run();
	}

	private class Task {
		/**
		 * The task id
		 */
		private final int id;
		/**
		 * The DaemonManager
		 */
		private final DaemonManager dm;
		/**
		 * True if this is an interval, false otherwise.
		 */
		private final boolean repeater;
		/**
		 * The initial delay. For timeouts, this is just the delay.
		 */
		private final long initialDelay;
		/**
		 * The delay between triggers. For intervals, this is ignored.
		 */
		private final long interval;
		/**
		 * The task itself.
		 */
		private final Runnable task;

		private Timer timer;

		public Task(int id, DaemonManager dm, boolean repeater, long initialDelay, long interval, Runnable task){
			this.id = id;
			this.dm = dm;
			this.repeater = repeater;
			this.initialDelay = initialDelay;
			if(repeater){
				this.interval = interval;
			} else {
				this.interval = Long.MAX_VALUE;
			}
			this.task = task;
		}

		public void register(){
			timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					task.run();
					if(!repeater){
						unregister();
						synchronized(tasks){
							tasks.remove(id);
						}
					}
				}
			}, initialDelay, interval);
			if(dm != null){
				dm.activateThread(null);
			}
		}

		public void unregister(){
			timer.cancel();
			if(dm != null){
				dm.deactivateThread(null);
			}
		}

		public int getId(){
			return id;
		}
	}

}
