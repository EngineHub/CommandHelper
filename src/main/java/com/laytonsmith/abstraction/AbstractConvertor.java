package com.laytonsmith.abstraction;

import com.laytonsmith.PureUtilities.DaemonManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 * @author lsmith
 */
public abstract class AbstractConvertor implements Convertor{
	
	private List<Runnable> shutdownHooks = new ArrayList<Runnable>();

	@Override
	public void addShutdownHook(Runnable r) {
		shutdownHooks.add(r);
	}

	@Override
	public void runShutdownHooks() {
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
