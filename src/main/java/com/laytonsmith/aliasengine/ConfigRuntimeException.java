/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine;

/**
 *
 * @author layton
 */
public class ConfigRuntimeException extends RuntimeException {

    /**
     * Creates a new instance of <code>ConfigRuntimeException</code> without detail message.
     */
    public ConfigRuntimeException() {
    }


    /**
     * Constructs an instance of <code>ConfigRuntimeException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ConfigRuntimeException(String msg) {
        super(msg);
    }
}
