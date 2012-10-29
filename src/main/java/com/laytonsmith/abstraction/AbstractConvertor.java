package com.laytonsmith.abstraction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author lsmith
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
	
	
}
