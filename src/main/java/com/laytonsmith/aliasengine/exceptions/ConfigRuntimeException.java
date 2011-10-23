/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine.exceptions;

import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import java.io.File;


/**
 *
 * @author layton
 */
public class ConfigRuntimeException extends RuntimeException {

    /**
     * Creates a new instance of <code>ConfigRuntimeException</code> without detail message.
     */
    protected ConfigRuntimeException() {
    }
    
    private ExceptionType ex;
    private int line_num;
    private File file;
    /**
     * Creates a new ConfigRuntimeException. If ex is not null, this exception can be caught
     * by user level code. Otherwise, it will be ignored by the try() function.
     * @param msg The message to be displayed
     * @param ex The type of exception this is, as seen by user level code
     * @param line_num The line this exception is being thrown from
     * @param file The file this code resides in
     */
    public ConfigRuntimeException(String msg, ExceptionType ex, int line_num, File file){
        this(msg, ex, line_num, file, null);
    }
    
    public ConfigRuntimeException(String msg, ExceptionType ex, int line_num, File file, Throwable cause){
        super(msg, cause);
        this.ex = ex;
        this.line_num = line_num;
        this.file = file;
    }
    
//    public ConfigRuntimeException(String msg, ExceptionType ex, int line_num){
//        this(msg, ex, line_num, null);
//    }
//    public ConfigRuntimeException(String msg, int line_num){
//        this(msg, null, line_num, null);
//    }
    
    /**
     * Creates an uncatchable exception (by user level code)
     * @param msg
     * @param line_num
     * @param file 
     */
    public ConfigRuntimeException(String msg, int line_num, File file){
        this(msg, null, line_num, file);
    }

    /**
     * Constructs an instance of <code>ConfigRuntimeException</code> with the specified detail message.
     * @param msg the detail message.
     */
    private ConfigRuntimeException(String msg) {
        super(msg);
    }
    
    public ExceptionType getExceptionType(){
        return this.ex;
    }
    
    public int getLineNum(){
        return this.line_num;
    }
    
    public File getFile(){
        return this.file;
    }
    
    public String getSimpleFile(){
        if(this.file != null){
            return this.file.getName();
        } else {
            return null;
        }
    }
}
