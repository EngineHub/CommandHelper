package com.laytonsmith.core;

/**
 *
 * @author layton
 */
public enum CHVersion {
    V0_0_0("0.0.0"), //Unreleased version
    V3_0_1("3.0.1"),
    V3_0_2("3.0.2"),
    V3_1_0("3.1.0"), 
    V3_1_2("3.1.2"), 
    V3_1_3("3.1.3"),
    V3_2_0("3.2.0"), 
    V3_3_0("3.3.0"), 
    V3_3_1("3.3.1");
    final Version version;
    private CHVersion(String version){
        this.version = new Version(version);
    }
    
    public Version getVersion(){
        return this.version;
    }
    
    public String getVersionString(){
        return this.version.toString();
    }
    
    public String toString(){
        return this.version.toString();
    }
}
