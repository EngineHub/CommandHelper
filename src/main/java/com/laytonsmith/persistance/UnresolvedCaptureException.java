package com.laytonsmith.persistance;

/**
 * This exception is thrown to indicate that when trying to retrieve
 * a value (or values), a capture was unresolved, thereby preventing
 * a connection to a data source.
 * @author lsmith
 */
public class UnresolvedCaptureException extends Exception {


    private String filter;
    /**
     * Constructs an instance of
     * <code>UnresolvedCaptureException</code> with the specified detail
     * message.
     *
     * @param msg the detail message.
     */
    public UnresolvedCaptureException(String filter) {
        super("Could not fully resolve the capture in the filter: " + filter);
        this.filter = filter;
    }
    
    public String getFilter(){
        return filter;
    }
}

