package com.laytonsmith.abstraction;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 * 
 */
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
}
