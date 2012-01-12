/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.puls3.abstraction;

/**
 *
 * @author layton
 */
public class MCCommandException extends RuntimeException {

    /**
     * Creates a new instance of <code>MCCommandException</code> without detail message.
     */
    public MCCommandException() {
    }

    /**
     * Constructs an instance of <code>MCCommandException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public MCCommandException(String msg) {
        super(msg);
    }
}
