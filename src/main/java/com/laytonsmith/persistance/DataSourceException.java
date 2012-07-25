package com.laytonsmith.persistance;

/**
 *
 * @author lsmith
 */
public class DataSourceException extends Exception {

    /**
     * Constructs an instance of
     * <code>DataSourceException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public DataSourceException(String msg){
        super(msg);
    }
    
    public DataSourceException(String msg, Throwable reason) {
        super(msg, reason);
    }        
}
