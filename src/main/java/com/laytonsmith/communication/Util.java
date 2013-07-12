package com.laytonsmith.communication;

/**
 *
 * @author import
 */
public class Util {
    public static boolean isValidChannel(String chan) {
        if (chan == null) {
            return false;
        } else if (chan.contains("\0")) {
            return false;
        } else if (chan.isEmpty()){
            return false;
        }
        
        return true;
    }
    
    public static boolean isValidName(String name) {
        if (name == null) {
            return false;
        } else if (name.contains("\0")) {
            return false;
        } else if (name.isEmpty()){
            return false;
        }
        
        return true;
    }
}
