package com.laytonsmith.core.taskmanager;

import com.laytonsmith.annotations.taskhandler;
import com.laytonsmith.core.constructs.Target;

/**
 *
 */
@taskhandler(properties = {})
public class TimeoutTaskHandler extends TaskHandler {

	private final Runnable killTaskRunnable;

	public TimeoutTaskHandler(int id, Target t, Runnable killTaskRunnable) {
		super(CoreTaskType.TIMEOUT, id, t);
		this.killTaskRunnable = killTaskRunnable;
	}

	@Override
	public void kill() {
		killTaskRunnable.run();
	}

}
