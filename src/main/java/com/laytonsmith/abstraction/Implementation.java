/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

/**
 * This class dynamically detects the server version being run, using various
 * checks as needed.
 * @author layton
 */
public class Implementation {
    
    /**
     * These are all the supported server types
     */
    public static enum Type{
        BUKKIT,
        TEST,
        //GLOWSTONE,
        //SINGLE_PLAYER
    }
    
    private static Implementation.Type serverType = null;
    
    /**
     * Returns the server type currently running
     * @return 
     */
    public static Type GetServerType(){       
        if(serverType == null){
            throw new RuntimeException("Server type has not been set yet! Please call Implementation.setServerType with the appropriate implementation.");
        }
        return serverType;        
    }
    
    public static void setServerType(Implementation.Type type){
        if(serverType == null){
            serverType = type;
        } else {
            if(type != Type.TEST){ //This could potentially happen, but we don't care in the case that we
                //are testing, so don't error out here. (Failures may occur elsewhere though... :()
                throw new RuntimeException("Server type is already set! Cannot re-set!");
            }
        }
    }
}
