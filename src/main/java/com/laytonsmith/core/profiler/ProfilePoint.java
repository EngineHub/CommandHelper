package com.laytonsmith.core.profiler;

import com.laytonsmith.core.LogLevel;

/**
 *
 *
 */
public class ProfilePoint implements Comparable<ProfilePoint> {

	private final String name;
	private String message;
	boolean gcRun;
	private final Profiler parent;
	private LogLevel granularity;

	public ProfilePoint(String name, Profiler parent) {
		this.name = name;
		gcRun = false;
		this.parent = parent;
		this.message = null;
	}

	@Override
	public String toString() {
		return name;
	}

	void garbageCollectorRun() {
		gcRun = true;
	}

	boolean wasGCd() {
		return gcRun;
	}

	public void stop() {
		parent.stop(this);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String newMessage) {
		message = newMessage;
	}

	/**
	 * This is an arbitrary comparison, for the sake of fast tree searches.
	 *
	 * @param o
	 * @return
	 */
	@Override
	public int compareTo(ProfilePoint o) {
		return o.name.compareTo(name);
	}

	/**
	 * Package private.
	 *
	 * @param granularity
	 */
	void setGranularity(LogLevel granularity) {
		this.granularity = granularity;
	}

	/**
	 * Returns the log level at which this profile point was registered.
	 *
	 * @return
	 */
	public LogLevel getGranularity() {
		return this.granularity;
	}

}
