/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.core.exceptions;

import com.laytonsmith.core.Env;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.sun.jndi.toolkit.ctx.Continuation;
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

    public void setEnv(Env env) {
        this.env = env;
    }
    
    /**
     * This returns the environment that was set when the exception was thrown.
     * It may be null, though that's due to an incomplete swapover, and should be
     * fixed.
     */
    public Env getEnv(){
        return this.env;
    }
    
    public static enum Reaction{
        /**
         * This exception should be ignored, because a handler dealt with it
         * as desired. The plugin is no longer responsible for dealing with this
         * exception
         */
        IGNORE,
        /**
         * No handler knew how to deal with this exception, or they chose not
         * to handle it. The plugin should handle it by using the default action
         * for an uncaught exception
         */
        REPORT,
        /**
         * A handler knew how to deal with this exception, and furthermore, it escalated
         * it to a more serious category. Though the behavior may be undefined, the
         * plugin should pass the exception up further.
         */
        FATAL
    }
    
    public static Reaction HandleUncaughtException(ConfigRuntimeException e){
        //For now just return DEFAULT, but eventually, we will also see what the
        //bound events do for it
        return Reaction.REPORT;
    }
    
    public static void DoReport(ConfigRuntimeException e){
        System.out.println(e.getMessage() + " :: " + e.getExceptionType() + ":" + e.getFile() + ":" + e.getLineNum());
        if(e != null){
            if(e.env.GetPlayer() != null){
                e.env.GetPlayer().sendMessage(e.getMessage() + " :: " + e.getExceptionType() + ":" + e.getSimpleFile() + ":" + e.getLineNum());
            }
        }
    }
    
    private ExceptionType ex;
    private int line_num;
    private File file;
    private Env env;
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
