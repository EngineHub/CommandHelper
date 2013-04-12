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

	public int getMajor() {
		return this.version.getMajor();
	}

	public int getMinor() {
		return this.version.getMinor();
	}

	public int getSupplemental() {
		return this.version.getSupplemental();
	}
	
}
