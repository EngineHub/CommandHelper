package com.laytonsmith.abstraction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 */
public abstract class AbstractConvertor implements Convertor{
	
	private List<Runnable> shutdownHooks = new ArrayList<Runnable>();

	public void addShutdownHook(Runnable r) {
		shutdownHooks.add(r);
	}

	public void runShutdownHooks() {
		Iterator<Runnable> iter = shutdownHooks.iterator();
		while(iter.hasNext()){
			iter.next().run();
			iter.remove();
		}
	}
	
	//By default, we can just run on a new thread
	public void runOnMainThreadLater(Runnable r) {
		r.run();
	}

	public <T> T runOnMainThreadAndWait(Callable<T> callable) throws Exception{
		return (T) callable.call();
	}				

	public MCWorldCreator getWorldCreator(String worldName) {
		throw new UnsupportedOperationException("Not supported.");
	}
}
