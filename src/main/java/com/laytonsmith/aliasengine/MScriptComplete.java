/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine;

/**
 *
 * @author Layton
 */
public interface MScriptComplete {
    /**
     * This function is called when the mscript has finished. Any output generated
     * by the script is sent here. If the script generated an error, null is sent.
     * If the script ran successfully, but did not return any output, an empty string
     * is sent.
     */
    public void done(String output);
}
