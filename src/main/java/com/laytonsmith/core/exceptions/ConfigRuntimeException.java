/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.core.exceptions;

import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.abstraction.MCChatColor;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;


/**
 *
 * @author layton
 */
public class ConfigRuntimeException extends RuntimeException {

    Stack<Target> stackTraceTrail = new Stack<Target>();

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

    public void setFile(File f) {
        if(file == null){
            file = f;
        }
    }

    public void setLineNum(int line_num) {
        if(this.line_num == -1){
            this.line_num = line_num;
        }
    }
    
    public void setColumn(int column){
        if(this.column == -1){
            this.column = column;
        }
    }
    
    public void addStackTraceTrail(Target t){
        //TODO: Add better stack traces to stuff that could bubble up
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
        //For now just return the default, but eventually, we will also see what the
        //bound events do for it
        return Reaction.REPORT;
    }
    
    /**
     * If there's nothing special you want to do with the exception, you can send it
     * here, and it will take the default action for an uncaught exception.
     * @param e
     * @param r 
     */
    public static void React(ConfigRuntimeException e){
        React(e, HandleUncaughtException(e), null);
    }
    
//    /**
//     * If there's nothing special you want to do with the exception, you can send it
//     * here, and it will take the default action for an uncaught exception.
//     * @param e
//     * @param r 
//     */
//    public static void React(ConfigRuntimeException e, Reaction r) {
//        React(e, r, null);
//    }
    
    /**
     * If there's nothing special you want to do with the exception, you can send it
     * here, and it will take the default action for an uncaught exception.
     * @param e
     * @param r 
     */
    public static void React(ConfigRuntimeException e, String optionalMessage){
        React(e, HandleUncaughtException(e), optionalMessage);
    }
    
    /**
     * If there's nothing special you want to do with the exception, you can send it
     * here, and it will take the default action for an uncaught exception.
     * @param e
     * @param r 
     */
    private static void React(ConfigRuntimeException e, Reaction r, String optionalMessage){        
        if(r == Reaction.IGNORE){
            //Welp, you heard the man.
            CHLog.Log(CHLog.Tags.RUNTIME, CHLog.Level.DEBUG, "An exception bubbled to the top, but was instructed by an event handler to not cause output.", e.getTarget());
        } else if(r == ConfigRuntimeException.Reaction.REPORT){
            ConfigRuntimeException.DoReport(e, optionalMessage);
        } else if(r == ConfigRuntimeException.Reaction.FATAL){
            ConfigRuntimeException.DoReport(e, optionalMessage);
            //Well, here goes nothing
            ConfigRuntimeException.DoReport(e, optionalMessage);
            throw e;
        }
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
    public static void DoReport(String message, String exceptionType, String file, String simpleFile, String line_num, String optionalMessage, MCPlayer player){
        String type = exceptionType;
        if(exceptionType == null){
            type = "FATAL";
        }
        String formatted = optionalMessage==null?"":"; " + optionalMessage;
        String plain = message + formatted + " :: " + type + ":" 
                + file + ":" + line_num;
        Target t;
        int ll = Integer.parseInt(line_num);
        File ff = file!=null?new File(file):null;
        int cc = 0;
        if(ll == 0 && ff == null && cc == 0){
            t = Target.UNKNOWN;
        } else {
            t = new Target(ll, ff, cc);
        }        
        CHLog.Log(exceptionType.equals("COMPILE ERROR")?CHLog.Tags.COMPILER:CHLog.Tags.RUNTIME, CHLog.Level.ERROR, plain, t);
        System.out.println(TermColors.RED + message + formatted 
                + TermColors.WHITE + " :: " + TermColors.GREEN 
                + type + TermColors.WHITE + ":" 
                + TermColors.YELLOW + file + TermColors.WHITE + ":" 
                + TermColors.CYAN + line_num + TermColors.reset());
        if(player != null){
            player.sendMessage(MCChatColor.RED.toString() + message + formatted
                    + MCChatColor.WHITE + " :: " + MCChatColor.GREEN
                    + type + MCChatColor.WHITE + ":" 
                    + MCChatColor.YELLOW + simpleFile + MCChatColor.WHITE + ":" 
                    + MCChatColor.AQUA + line_num);
        }
    }
    
    public static void DoReport(ConfigRuntimeException e, String optionalMessage){
        MCPlayer p = null;
        if(e.getEnv() != null && e.getEnv().GetPlayer() != null){
            p = e.getEnv().GetPlayer();
        }        
        DoReport(e.getMessage(), e.getExceptionType()!=null?e.getExceptionType().toString():"FatalRuntimeException", e.getFile()==null?null:e.getFile().getPath(), e.getSimpleFile(), Integer.toString(e.getLineNum()), optionalMessage, p);
    }
    
    public static void DoReport(ConfigCompileException e, String optionalMessage, MCPlayer player){
        DoReport(e.getMessage(), "COMPILE ERROR", e.getFile()==null?null:e.getFile().getPath(), e.getSimpleFile(), e.getLineNum(), optionalMessage, player);
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
        if(!checkPrefs || Prefs.ShowWarnings()){
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
    private int line_num = -1;
    private File file;
    private int column = -1;
    private Env env;
    private Target target;
    /**
     * Creates a new ConfigRuntimeException. If ex is not null, this exception can be caught
     * by user level code. Otherwise, it will be ignored by the try() function.
     * @param msg The message to be displayed
     * @param ex The type of exception this is, as seen by user level code
     * @param line_num The line this exception is being thrown from
     * @param file The file this code resides in
     */
    public ConfigRuntimeException(String msg, ExceptionType ex, Target t){
        this(msg, ex, t, null);
    }
    
    public ConfigRuntimeException(String msg, ExceptionType ex, Target t, Throwable cause){
        super(msg, cause);
        this.ex = ex;
        this.line_num = t.line();
        this.file = t.file();
        this.column = t.col();
        this.target = t;
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
    public ConfigRuntimeException(String msg, Target t){
        this(msg, null, t);
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
    
    public int getCol(){
        return this.column;
    }
    
    public Target getTarget(){
        return this.target;
    }
    
    public String getSimpleFile(){
        if(this.file != null){
            return this.file.getName();
        } else {
            return null;
        }
    }
}
