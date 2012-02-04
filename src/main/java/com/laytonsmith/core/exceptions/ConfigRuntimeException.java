/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.core.exceptions;

import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.abstraction.MCChatColor;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.io.File;
import java.util.logging.Level;


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
    
    /**
     * If a exception bubbles all the way up to the top, this should be called first,
     * to see what reaction the plugin should take.
     * @param e
     * @return 
     */
    public static Reaction HandleUncaughtException(ConfigRuntimeException e){
        //For now just return DEFAULT, but eventually, we will also see what the
        //bound events do for it
        return Reaction.REPORT;
    }
    
    /**
     * If the Reaction returned by HandleUncaughtException is to report the exception,
     * this function should be used to standardize the report format.
     * @param e 
     */
    public static void DoReport(ConfigRuntimeException e){
        DoReport(e, null);
    }
    
    /**
     * If the Reaction returned by HandleUncaughtException is to report the exception,
     * this function should be used to standardize the report format. If the error message
     * wouldn't be very useful by itself, or if a hint is desired, an optional message
     * may be provided (null otherwise)
     * @param e
     * @param optionalMessage 
     */
    public static void DoReport(ConfigRuntimeException e, String optionalMessage){
        String formatted = optionalMessage==null?"":"; " + optionalMessage;
        System.out.println(TermColors.RED + e.getMessage() + formatted 
                + TermColors.WHITE + " :: " + TermColors.GREEN 
                + e.getExceptionType() + TermColors.WHITE + ":" 
                + TermColors.YELLOW + e.getFile() + TermColors.WHITE + ":" 
                + TermColors.CYAN + e.getLineNum() + TermColors.reset());
        if(e != null && e.env != null && e.env.GetPlayer() != null){
            e.env.GetPlayer().sendMessage(MCChatColor.RED.toString() + e.getMessage() + formatted + " :: " + e.getExceptionType() + ":" + e.getSimpleFile() + ":" + e.getLineNum());
        }
    }
    
    /**
     * Shorthand for DoWarning(exception, null, true);
     * @param e 
     */
    public static void DoWarning(Exception e){
        DoWarning(e, null, true);
    }
    
    /**
     * Shorthand for DoWarning(null, message, true);
     * @param optionalMessage 
     */
    public static void DoWarning(String optionalMessage){
        DoWarning(null, optionalMessage, true);
    }
    
    /**
     * To standardize the warning messages displayed, this function should
     * be used. It checks the preference setting for warnings to see if
     * the warning should be shown to begin with, if checkPref is true. The exception
     * is simply used to get an error message, and is otherwise unused. If the exception
     * is a ConfigRuntimeException, it is displayed specially (including line number
     * and file)
     * @param e
     * @param optionalMessage 
     * @throws NullPointerException If both the exception and message are null (or empty)
     */
    public static void DoWarning(Exception e, String optionalMessage, boolean checkPrefs){
        if(e == null && (optionalMessage == null || optionalMessage.isEmpty())){
            throw new NullPointerException("Both the exception and the message cannot be empty");
        }
        if(!checkPrefs || (Boolean)Static.getPreferences().getPreference("show-warnings")){
            String exceptionMessage = "";
            if(e instanceof ConfigRuntimeException){
                ConfigRuntimeException cre = (ConfigRuntimeException)e;
                exceptionMessage = MCChatColor.YELLOW + cre.getMessage() 
                + MCChatColor.WHITE + " :: " + MCChatColor.GREEN 
                + cre.getExceptionType() + MCChatColor.WHITE + ":" 
                + MCChatColor.YELLOW + cre.getFile() + MCChatColor.WHITE + ":" 
                + MCChatColor.AQUA + cre.getLineNum();
            } else if(e != null){
                exceptionMessage = MCChatColor.YELLOW + e.getMessage();
            }
            String message = exceptionMessage + MCChatColor.WHITE + optionalMessage;
            Static.getLogger().log(Level.WARNING, Static.MCToANSIColors(message) + TermColors.reset());
            //Warnings are not shown to players ever
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
