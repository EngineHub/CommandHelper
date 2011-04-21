/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine;

/**
 * An internal exception is thrown when an unexpected error occurs
 * in the actual plugin itself, not the user scripts. If this exception
 * is thrown, the plugin is effectively disabled.
 * @author Layton
 */
public class InternalException extends RuntimeException {

    /**
     * Creates a new instance of <code>InternalException</code> without detail message.
     */
    public InternalException() {
    }

    /**
     * Constructs an instance of <code>InternalException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public InternalException(String msg) {
        super(msg);
    }
}
