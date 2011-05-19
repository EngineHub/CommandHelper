/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine;

/**
 *
 * @author Layton
 */
public interface MinescriptComplete {
    /**
     * This function is called when the minescript has finished. Any output generated
     * by the script is sent here.
     */
    public void done(String output);
}
