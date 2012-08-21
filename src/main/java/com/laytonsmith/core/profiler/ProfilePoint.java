package com.laytonsmith.core.profiler;

/**
 *
 * @author Layton
 */
public class ProfilePoint implements Comparable<ProfilePoint> {
	private String name;
	boolean GCRun;
	private Profiler parent;

	public ProfilePoint(String name, Profiler parent) {
		this.name = name;
		GCRun = false;
		this.parent = parent;
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

	/**
	 * This is an arbitrary comparison, for the sake of fast tree searches.
	 *
	 * @param o
	 * @return
	 */
	public int compareTo(ProfilePoint o) {
		return o.name.compareTo(name);
	}
    
}
