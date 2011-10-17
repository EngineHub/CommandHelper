/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.events;

/**
 *
 * @author layton
 */
public class EventHandler {
    private static int EventID = 0;
    public static int GetUniqueID(){
        return ++EventID;
    }
}
