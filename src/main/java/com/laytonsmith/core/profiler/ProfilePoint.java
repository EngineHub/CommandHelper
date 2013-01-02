package com.laytonsmith.core.profiler;

import com.laytonsmith.core.LogLevel;

/**
 *
 * @author Layton
 */
public class ProfilePoint implements Comparable<ProfilePoint> {
	private String name;
	private String message;
	boolean GCRun;
	private Profiler parent;
	private LogLevel granularity;

	public ProfilePoint(String name, Profiler parent) {
		this.name = name;
		GCRun = false;
		this.parent = parent;
		this.message = "";
	}

	@Override
	public String toString() {
		return name;
	}

	void garbageCollectorRun() {
		GCRun = true;
	}

	boolean wasGCd() {
		return GCRun;
	}
	
	public void stop(){
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
	public int compareTo(ProfilePoint o) {
		return o.name.compareTo(name);
	}

	/**
	 * Package private.
	 * @param granularity 
	 */
	void setGranularity(LogLevel granularity) {
		this.granularity = granularity;
	}
	
	/**
	 * Returns the log level at which this profile point was registered.
	 * @return 
	 */
	public LogLevel getGranularity(){
		return this.granularity;
	}
    
}
