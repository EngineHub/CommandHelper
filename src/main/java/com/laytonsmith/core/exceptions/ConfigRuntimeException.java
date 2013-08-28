


package com.laytonsmith.core.exceptions;

import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.enums.MCChatColor;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author layton
 */
public class ConfigRuntimeException extends RuntimeException {

    List<StackTraceElement> stackTraceTrail = new ArrayList<StackTraceElement>();

    /**
     * Creates a new instance of <code>ConfigRuntimeException</code> without detail message.
     */
    protected ConfigRuntimeException() {
    }

    public void setEnv(Environment env) {
        this.env = env;
    }
    
    /**
     * This returns the environment that was set when the exception was thrown.
     * It may be null, though that's due to an incomplete swapover, and should be
     * fixed.
     */
    public Environment getEnv(){
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
    
    public void addStackTraceTrail(StackTraceElement t, Target nextTarget){
		if(t == null){
			stackTraceTrail.add(new StackTraceElement("<<main code>>", target));
		} else {
			stackTraceTrail.add(t);
			target = nextTarget;
		}
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
     * to see what reaction the plugin should take. Generally speaking, you'll want to
	 * use {@link #React} instead of this, though if you need to take custom action, you
	 * can determine the user's preferred reaction with this method.
     * @param e
     * @return 
     */
    public static Reaction HandleUncaughtException(ConfigRuntimeException e, Environment env){
        //If there is an exception handler, call it to see what it says.
		Reaction reaction = Reaction.REPORT;
		if(e.getExceptionType() == null){
			//Uncatchable, so return the default
			return reaction;
		}
		if(env.getEnv(GlobalEnv.class).GetExceptionHandler() != null){
			CClosure c = env.getEnv(GlobalEnv.class).GetExceptionHandler();
			CArray ex = ObjectGenerator.GetGenerator().exception(e, Target.UNKNOWN);
			Construct ret = new CNull();
			try{
				c.execute(new Construct[]{ex});
			} catch(FunctionReturnException retException){
				ret = retException.getReturn();
			}
			if(ret instanceof CNull || Prefs.ScreamErrors()){
				reaction = Reaction.REPORT;
			} else {
				if(Static.getBoolean(ret)){
					reaction = Reaction.IGNORE;
				} else {
					reaction = Reaction.FATAL;
				}
			}
		}
        return reaction;
    }
	
	/**
	 * Compile errors are always handled with the default mechanism, but
	 * to standardize error handling, this method must be used.
	 * @param e
	 * @param optionalMessage
	 * @param player 
	 */
	public static void React(ConfigCompileException e, String optionalMessage, MCPlayer player){
		DoReport(e, player);
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
    public static void React(ConfigRuntimeException e, Environment env){
        React(e, HandleUncaughtException(e, env));
    }
    
    /**
     * If there's nothing special you want to do with the exception, you can send it
     * here, and it will take the default action for an uncaught exception.
     * @param e
     * @param r 
     */
    private static void React(ConfigRuntimeException e, Reaction r){
		//This is the top of the stack chain, so finalize the stack trace at this point
		e.addStackTraceTrail(null, Target.UNKNOWN);
        if(r == Reaction.IGNORE){
            //Welp, you heard the man.
            CHLog.GetLogger().Log(CHLog.Tags.RUNTIME, LogLevel.DEBUG, "An exception bubbled to the top, but was instructed by an event handler to not cause output.", e.getTarget());
        } else if(r == ConfigRuntimeException.Reaction.REPORT){
            ConfigRuntimeException.DoReport(e);
        } else if(r == ConfigRuntimeException.Reaction.FATAL){
            ConfigRuntimeException.DoReport(e);
            //Well, here goes nothing
            ConfigRuntimeException.DoReport(e);
            throw e;
        }
    }
    
    /**
     * If the Reaction returned by HandleUncaughtException is to report the exception,
     * this function should be used to standardize the report format. If the error message
     * wouldn't be very useful by itself, or if a hint is desired, an optional message
     * may be provided (null otherwise).
     * @param e
     * @param optionalMessage 
     */
    private static void DoReport(String message, String exceptionType, List<StackTraceElement> stacktrace, MCPlayer currentPlayer){
        String type = exceptionType;
        if(exceptionType == null){
            type = "FATAL";
        }
		List<StackTraceElement> st = new ArrayList<StackTraceElement>(stacktrace);
		if(message == null){
			message = "";
		}
		if(!"".equals(message.trim())){
			message = ": " + message;
		}
		Target top = Target.UNKNOWN;
		StringBuilder log = new StringBuilder();
		StringBuilder console = new StringBuilder();
		StringBuilder player = new StringBuilder();
		log.append(type).append(message).append("\n");
		console.append(TermColors.RED).append(type).append(TermColors.WHITE).append(message).append("\n");
		player.append(MCChatColor.RED).append(type).append(MCChatColor.WHITE).append(message).append("\n");
		for(StackTraceElement e : st){
			Target t = e.getDefinedAt();
			if(top == Target.UNKNOWN){
				top = t;
			}
			String proc = e.getProcedureName();
			File file = t.file();
			int line = t.line();
			int column = t.col();
			String filepath;
			String simplepath;
			if(file == null){
				filepath = simplepath = "Unknown Source";
			} else {
				filepath = file.getPath();
				simplepath = file.getName();
			}
			
			log.append("\t").append(proc).append(":").append(filepath).append(":")
					.append(line)/*.append(".")
					.append(column)*/.append("\n");
			console.append("\t").append(TermColors.GREEN).append(proc)
					.append(TermColors.WHITE).append(":")
					.append(TermColors.YELLOW).append(filepath)
					.append(TermColors.WHITE).append(":")
					.append(TermColors.CYAN).append(line)/*.append(".").append(column)*/.append("\n");
			player.append("\t").append(MCChatColor.GREEN).append(proc)
					.append(MCChatColor.WHITE).append(":")
					.append(MCChatColor.YELLOW).append(simplepath)
					.append(MCChatColor.WHITE).append(":")
					.append(MCChatColor.AQUA).append(line)/*.append(".").append(column)*/.append("\n");
			
		}
		
		//Log
		//Don't log to screen though, since we're ALWAYS going to do that ourselves.
		CHLog.GetLogger().Log("COMPILE ERROR".equals(exceptionType)?CHLog.Tags.COMPILER:CHLog.Tags.RUNTIME, 
				LogLevel.ERROR, log.toString(), top, false);
		//Console
		System.out.println(console.toString() + TermColors.reset());
		//Player
		if(currentPlayer != null){
			currentPlayer.sendMessage(player.toString());
		}
    }
    
    private static void DoReport(ConfigRuntimeException e){
        MCPlayer p = null;
        if(e.getEnv() != null && e.getEnv().getEnv(CommandHelperEnvironment.class).GetPlayer() != null){
            p = e.getEnv().getEnv(CommandHelperEnvironment.class).GetPlayer();
        }
        DoReport(e.getMessage(), e.getExceptionType()!=null?e.getExceptionType().toString():"FatalRuntimeException", e.stackTraceTrail, p);
		if(e.getCause() != null && Prefs.DebugMode()){
			//This is more of a system level exception, so if debug mode is on, we also want to print this stack trace
			System.err.println("The previous MethodScript error had an attached cause:");
			e.getCause().printStackTrace(System.err);
		}
    }
    
    private static void DoReport(ConfigCompileException e, MCPlayer player){
		List<StackTraceElement> st = new ArrayList<StackTraceElement>();
		st.add(0, new StackTraceElement("", e.getTarget()));
        DoReport(e.getMessage(), "COMPILE ERROR", st, player);
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
			Target t = Target.UNKNOWN;
            if(e instanceof ConfigRuntimeException){
                ConfigRuntimeException cre = (ConfigRuntimeException)e;
                exceptionMessage = MCChatColor.YELLOW + cre.getMessage() 
                + MCChatColor.WHITE + " :: " + MCChatColor.GREEN 
                + cre.getExceptionType() + MCChatColor.WHITE + ":" 
                + MCChatColor.YELLOW + cre.getFile() + MCChatColor.WHITE + ":" 
                + MCChatColor.AQUA + cre.getLineNum();
				t = cre.getTarget();
            } else if(e != null){
                exceptionMessage = MCChatColor.YELLOW + e.getMessage();
            }
            String message = exceptionMessage + MCChatColor.WHITE + optionalMessage;
            CHLog.GetLogger().Log(CHLog.Tags.GENERAL, LogLevel.WARNING, Static.MCToANSIColors(message) + TermColors.reset(), t);
            //Warnings are not shown to players ever
        }
    }
    
    
    private ExceptionType ex;
    private int line_num = -1;
    private File file;
    private int column = -1;
    private Environment env;
    private Target target;
    /**
     * Creates a new ConfigRuntimeException. If the exception is intended to be uncatchable,
	 * use {@link #CreateUncatchableException} instead.
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
		if(ex == null){
			throw new NullPointerException("Use CreateUncatchableException instead.");
		}
        createException(ex, t);
    }
	
	private void createException(ExceptionType ex, Target t){
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
	 * Creates an uncatchable exception. This should rarely be used.
	 * @param msg
	 * @param t 
	 */
	public static ConfigRuntimeException CreateUncatchableException(String msg, Target t){
		return new ConfigRuntimeException(msg, t, null);
	}
	
	/**
	 * Creates an uncatchable exception with a cause. This should rarely be used.
	 * @param msg
	 * @param t 
	 * @param cause
	 */
	public static ConfigRuntimeException CreateUncatchableException(String msg, Target t, Throwable cause){
		return new ConfigRuntimeException(msg, t, cause);
	}
    
    private ConfigRuntimeException(String msg, Target t, Throwable cause){
		super(msg, cause);
        createException(null, t);
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
	
	public static class StackTraceElement{
		private final String procedureName;
		private final Target definedAt;

		public StackTraceElement(String procedureName, Target definedAt) {
			this.procedureName = procedureName;
			this.definedAt = definedAt;
		}

		public String getProcedureName() {
			return procedureName;
		}

		public Target getDefinedAt() {
			return definedAt;
		}

		@Override
		public String toString() {
			return procedureName + " (Defined at " + definedAt + ")";
		}
		
		
	}
}
