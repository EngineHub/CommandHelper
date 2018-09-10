package com.laytonsmith.core.taskmanager;

import com.methodscript.PureUtilities.ClassLoading.ClassDiscovery;
import com.methodscript.PureUtilities.Version;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Documentation;
import java.net.URL;

/**
 *
 */
public enum CoreTaskType implements TaskType {
	TIMEOUT("Timeout", "A timeout is a closure that runs at some scheduled time in the future, only once.", CHVersion.V3_3_1),
	INTERVAL("Interval", "An interval is a closure that is run repeatedly, with a given delay between runs.", CHVersion.V3_3_1),
	CRON("Cron Job", "A cron job is like an interval, except it runs at some interval based on wall time, not an interval"
			+ " between runs.", CHVersion.V3_3_1),
	EXECUTION("Execution Task", "An execution task is a way for tasks to be scheduled to be run as soon as possible, though at"
			+ " some deferred time.", CHVersion.V3_3_1),
	THREAD("Thread", "A thread is a separate, user defined task that runs asyncronously from the main thread. The user controls all"
			+ " aspects about this task.", CHVersion.V3_3_1),
	ASYNC_TASK("Async Task", "An async task is some task that various individual functions use to do some processing off of the main"
			+ " thread. Usually, once the processing is complete, the task will return control to the main thread, and execute"
			+ " some user defined callback.", CHVersion.V3_3_1),;

	private final String displayName;
	private final String docs;
	private final Version version;

	private CoreTaskType(String displayName, String docs, Version version) {
		this.displayName = displayName;
		this.docs = docs;
		this.version = version;
	}

	@Override
	public String displayName() {
		return displayName;
	}

	@Override
	public URL getSourceJar() {
		return ClassDiscovery.GetClassContainer(this.getClass());
	}

	@Override
	public Class<? extends Documentation>[] seeAlso() {
		return new Class[0];
	}

	@Override
	public String getName() {
		return displayName;
	}

	@Override
	public String docs() {
		return docs;
	}

	@Override
	public Version since() {
		return version;
	}
}
