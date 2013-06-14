package com.laytonsmith.persistance;

/**
 *
 */
public class ReadOnlyException extends Exception {

    /**
     * Creates a new instance of
     * <code>ReadOnlyException</code> without detail message.
     */
    public ReadOnlyException() {
    }

    /**
     * Constructs an instance of
     * <code>ReadOnlyException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public ReadOnlyException(String msg) {
        super(msg);
    }
}
