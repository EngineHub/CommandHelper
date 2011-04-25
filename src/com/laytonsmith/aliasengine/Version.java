/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A version is formatted as such: 1.2.10 beta-1 where 1 is the major version,
 * 2 is the minor version, 10 is the supplemental version, and beta-1 is the tag.
 * When comparing two versions, the tag is not considered.
 * @author Layton
 */
public class Version implements Comparable<Version>{
    
    private int major;
    private int minor;
    private int supplemental;
    private String tag;
    
    /**
     * Creates a new Version object from a string version number. The tag is
     * optional, but all other parameters are required. If left off, each version
     * part is set to 0.
     * @param version 
     */
    public Version(String version){
        Pattern p = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)\\s+(.*)?");
        Matcher m = p.matcher(version);
        if(m.find()){
            major = Integer.parseInt(m.group(1)==null?"0":m.group(1));
            minor = Integer.parseInt(m.group(2)==null?"0":m.group(2));
            supplemental = Integer.parseInt(m.group(3)==null?"0":m.group(3));
            tag = m.group(4)==null?"":m.group(4);
        } else {
            major = minor = supplemental = 0;
            tag = "";
        }
    }
    
    public Version(int major, int minor, int supplemental, String tag){
        this.major = major;
        this.minor = minor;
        this.supplemental = supplemental;
        this.tag = tag;
    }
    
    public Version(int major, int minor, int supplemental){
        this(major, minor, supplemental, "");
    }
    
    @Override
    public String toString(){
        return (major + "." + minor + "." + supplemental + " " + tag).trim();
    }

    public int compareTo(Version o) {
        if(major < o.major){
            return -1;
        } else if(major > o.major){
            return 1;
        } else {
            if(minor < o.minor){
                return -1;
            } else if(minor > o.minor){
                return 1;
            } else {
                if(supplemental < o.supplemental){
                    return -1;
                } else if(supplemental > o.supplemental){
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Version){
            Version v = (Version) obj;
            if(major == v.major && minor == v.minor && supplemental == v.supplemental){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.major;
        hash = 97 * hash + this.minor;
        hash = 97 * hash + this.supplemental;
        return hash;
    }
    
    
}
