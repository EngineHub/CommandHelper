/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions.exceptions;

/**
 *
 * @author layton
 */
public class EventException extends Exception {

    /**
     * Creates a new instance of <code>EventException</code> without detail message.
     */
    public EventException() {
    }

    /**
     * Constructs an instance of <code>EventException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public EventException(String msg) {
        super(msg);
    }
}
