package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.Version;

/**
 *
 * @author layton
 */
public enum CHVersion implements Version {
    V0_0_0("0.0.0"), //Unreleased version
    V3_0_1("3.0.1"),
    V3_0_2("3.0.2"),
    V3_1_0("3.1.0"), 
    V3_1_2("3.1.2"), 
    V3_1_3("3.1.3"),
    V3_2_0("3.2.0"), 
    V3_3_0("3.3.0"), 
    V3_3_1("3.3.1");
    final SimpleVersion version;
	
	/**
	 * This points to the latest version in the series. This should normally only
	 * be used for things that report the <i>current</i> version, not things that are
	 * versioned. This is not an actual enum within the class, this is a static member of
	 * the class which points to the an actual enum.
	 */
	public static final CHVersion LATEST;
	
	static {
		//Dynamically determine the latest value.
		CHVersion latest = null;
		for(CHVersion v : CHVersion.values()){
			if(latest == null || v.gt(latest)){
				latest = v;
			}
		}
		LATEST = latest;
	}
    private CHVersion(String version){
        this.version = new SimpleVersion(version);
    }
    
    public String getVersionString(){
        return this.version.toString();
    }
    
    @Override
    public String toString(){
        return this.version.toString();
    }

	public int compareTo(Version o) {
		return this.version.compareTo(o);
	}

	@Override
	public int getMajor() {
		return this.version.getMajor();
	}

	@Override
	public int getMinor() {
		return this.version.getMinor();
	}

	@Override
	public int getSupplemental() {
		return this.version.getSupplemental();
	}

	@Override
	public boolean lt(Version other) {
		return this.version.lt(other);
	}

	@Override
	public boolean lte(Version other) {
		return this.version.lte(other);
	}

	@Override
	public boolean gt(Version other) {
		return this.version.gt(other);
	}

	@Override
	public boolean gte(Version other) {
		return this.version.gte(other);
	}
	
}
