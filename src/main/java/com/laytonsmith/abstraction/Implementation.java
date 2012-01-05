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
    public static enum Type{
        BUKKIT,
        GLOWSTONE,
        SINGLE_PLAYER
    }
    
    /**
     * Returns the server type currently running
     * @return 
     */
    public static Type GetServerType(){       
        return Type.BUKKIT;
    }
}
