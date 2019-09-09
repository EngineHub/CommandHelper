package com.laytonsmith.core.exceptions;

import com.laytonsmith.PureUtilities.Common.StackTraceUtils;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.enums.MCChatColor;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.AbstractCREException;
import com.laytonsmith.core.exceptions.CRE.CRECausedByWrapper;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A ConfigRuntimeException is the base class for user level exceptions.
 */
public class ConfigRuntimeException extends RuntimeException {

	/**
	 * Creates a new ConfigRuntimeException.
	 *
	 * @param msg The message to be displayed
	 * @param t The code target this exception is being thrown from
	 */
	protected ConfigRuntimeException(String msg, Target t) {
		super(msg);
		createException(t);
	}

	/**
	 * Creates a new ConfigRuntimeException.
	 *
	 * @param msg The message to be displayed
	 * @param t The code target this exception is being thrown from
	 * @param cause The chained cause. This is not used for normal execution, but is helpful when debugging errors.
	 * Where exceptions are triggered by Java code (as opposed to organic MethodScript errors) this version should
	 * always be preferred.
	 */
	protected ConfigRuntimeException(String msg, Target t, Throwable cause) {
		super(msg, cause);
		createException(t);
	}

	/**
	 * Sets the environment of the exception.
	 *
	 * @param env
	 */
	public void setEnv(Environment env) {
		this.env = env;
	}

	/**
	 * This returns the environment that was set when the exception was thrown. It may be null, though that's due to an
	 * incomplete swapover, and should be fixed.
	 */
	public Environment getEnv() {
		return this.env;
	}

	/**
	 * A reaction is a pre-programmed response to the exception bubbling all the way up. One of these reaction types
	 * must be set by user code (or defaults to REPORT), and the correct action will occur.
	 */
	public static enum Reaction {
		/**
		 * This exception should be ignored, because a handler dealt with it as desired. The plugin is no longer
		 * responsible for dealing with this exception
		 */
		IGNORE,
		/**
		 * No handler knew how to deal with this exception, or they chose not to handle it. The plugin should handle it
		 * by using the default action for an uncaught exception
		 */
		REPORT,
		/**
		 * A handler knew how to deal with this exception, and furthermore, it escalated it to a more serious category.
		 * Though the behavior may be undefined, the plugin should pass the exception up further.
		 */
		FATAL
	}

	/**
	 * If a exception bubbles all the way up to the top, this should be called first, to see what reaction the plugin
	 * should take. Generally speaking, you'll want to use {@link #HandleUncaughtException} instead of this, though if
	 * you need to take custom action, you can determine the user's preferred reaction with this method.
	 *
	 * @param e
	 * @return
	 */
	public static Reaction GetReaction(ConfigRuntimeException e, Environment env) {

		// If there is an exception handler, call it to see what it says.
		if(env.getEnv(GlobalEnv.class).GetExceptionHandler() != null) {
			CClosure c = env.getEnv(GlobalEnv.class).GetExceptionHandler();
			CArray ex = ObjectGenerator.GetGenerator().exception(e, env, Target.UNKNOWN);
			if(e.getEnv() != null) {
				MCCommandSender sender = e.getEnv().getEnv(CommandHelperEnvironment.class).GetCommandSender();
				c.getEnv().getEnv(CommandHelperEnvironment.class).SetCommandSender(sender);
			}
			try {
				Mixed ret = c.executeCallable(env, Target.UNKNOWN, new Mixed[]{ex});
				if(ret instanceof CNull || ret instanceof CVoid || Prefs.ScreamErrors()) {
					return Reaction.REPORT; // Closure returned null or scream-errors was set in the config.
				}
				// Closure returned a boolean. TRUE -> IGNORE and FALSE -> FATAL.
				return (ArgumentValidation.getBooleanObject(ret, Target.UNKNOWN) ? Reaction.IGNORE : Reaction.FATAL);
			} catch (ConfigRuntimeException cre) {

				// A CRE occurred in the exception handler. Report both exceptions.
				HandleUncaughtException(cre, env, Reaction.REPORT);
				return Reaction.REPORT;
			}
		} else {
			return Reaction.REPORT; // No exception handler set -> REPORT.
		}
	}

	/**
	 * Compile errors are always handled with the default mechanism, but to standardize error handling, this method must
	 * be used.
	 *
	 * @param e
	 * @param optionalMessage
	 * @param player
	 */
	public static void HandleUncaughtException(ConfigCompileException e, String optionalMessage, MCPlayer player) {
		if(optionalMessage != null) {
			DoWarning(optionalMessage);
		}
		DoReport(e, player);
	}

	public static void HandleUncaughtException(ConfigCompileGroupException e, MCPlayer player) {
		for(ConfigCompileException ce : e.getList()) {
			HandleUncaughtException(ce, null, player);
		}
	}

	public static void HandleUncaughtException(ConfigCompileGroupException e, String optionalMessage, MCPlayer player) {
		DoWarning(optionalMessage);
		HandleUncaughtException(e, player);
	}

	/**
	 * If there's nothing special you want to do with the exception, you can send it here, and it will take the default
	 * action for an uncaught exception.
	 *
	 * @param e
	 * @param r
	 */
	public static void HandleUncaughtException(ConfigRuntimeException e, Environment env) {
		HandleUncaughtException(e, env, GetReaction(e, env));
	}

	/**
	 * If there's nothing special you want to do with the exception, you can send it here, and it will take the default
	 * action for an uncaught exception.
	 *
	 * @param e
	 * @param r
	 */
	private static void HandleUncaughtException(ConfigRuntimeException e, Environment env, Reaction r) {
		if(r == Reaction.IGNORE) {
			//Welp, you heard the man.
			MSLog.GetLogger().Log(MSLog.Tags.RUNTIME, LogLevel.DEBUG, "An exception bubbled to the top, but was instructed by an event handler to not cause output.", e.getTarget());
		} else if(r == ConfigRuntimeException.Reaction.REPORT) {
			ConfigRuntimeException.DoReport(e, env);
		} else if(r == ConfigRuntimeException.Reaction.FATAL) {
			ConfigRuntimeException.DoReport(e, env);
		}
	}

	private static void PrintMessage(StringBuilder log, StringBuilder console, StringBuilder player, String type, String message, Throwable ex, List<StackTraceElement> st) {
		log.append(type).append(message).append("\n");
		console.append(TermColors.RED).append(type).append(TermColors.WHITE).append(message).append("\n");
		player.append(MCChatColor.RED).append(type).append(MCChatColor.WHITE).append(message).append("\n");
		for(StackTraceElement e : st) {
			Target t = e.getDefinedAt();
			String proc = e.getProcedureName();
			File file = t.file();
			int line = t.line();
			int column = t.col();
			String filepath;
			String simplepath;
			if(file == null) {
				filepath = simplepath = "Unknown Source";
			} else {
				filepath = file.getPath();
				simplepath = file.getName();
			}

			log.append("\tat ").append(proc).append(":").append(filepath).append(":")
					.append(line).append(".")
					.append(column).append("\n");
			console.append("\t").append(TermColors.WHITE).append("at ").append(TermColors.GREEN).append(proc)
					.append(TermColors.WHITE).append(":")
					.append(TermColors.YELLOW).append(filepath)
					.append(TermColors.WHITE).append(":")
					.append(TermColors.CYAN).append(line).append(".").append(column).append("\n");
			player.append("\t").append(MCChatColor.WHITE).append("at ").append(MCChatColor.GREEN).append(proc)
					.append(MCChatColor.WHITE).append(":")
					.append(MCChatColor.YELLOW).append(simplepath)
					.append(MCChatColor.WHITE).append(":")
					.append(MCChatColor.AQUA).append(line).append(".").append(column).append("\n");

		}
	}

	/**
	 * If the Reaction returned by GetReaction is to report the exception, this function should be used to standardize
	 * the report format. If the error message wouldn't be very useful by itself, or if a hint is desired, an optional
	 * message may be provided (null otherwise).
	 *
	 * @param e
	 * @param optionalMessage
	 */
	@SuppressWarnings("ThrowableResultIgnored")
	private static void DoReport(String message, String exceptionType, ConfigRuntimeException ex, List<StackTraceElement> stacktrace, MCPlayer currentPlayer) {
		String type = exceptionType;
		if(exceptionType == null) {
			type = "FATAL";
		}
		List<StackTraceElement> st = new ArrayList<>(stacktrace);
		if(message == null) {
			message = "";
		}
		if(!"".equals(message.trim())) {
			message = ": " + message;
		}

		Target top = Target.UNKNOWN;
		for(StackTraceElement e : st) {
			Target t = e.getDefinedAt();
			if(top == Target.UNKNOWN) {
				top = t;
			}
		}
		StringBuilder log = new StringBuilder();
		StringBuilder console = new StringBuilder();
		StringBuilder player = new StringBuilder();
		PrintMessage(log, console, player, type, message, ex, st);
		if(ex != null) {
			// Otherwise, a CCE
			if(ex.getCause() != null && ex.getCause() instanceof ConfigRuntimeException) {
				ex = (ConfigRuntimeException) ex.getCause();
			}
			while(ex instanceof CRECausedByWrapper) {
				Target t = ex.getTarget();
				log.append("Caused by:\n");
				console.append(TermColors.CYAN).append("Caused by:\n");
				player.append(MCChatColor.AQUA).append("Caused by:\n");
				CArray exception = ((CRECausedByWrapper) ex).getException();
				CArray stackTrace = Static.getArray(exception.get("stackTrace", t), t);
				List<StackTraceElement> newSt = new ArrayList<>();
				for(Mixed consElement : stackTrace.asList()) {
					CArray element = Static.getArray(consElement, t);
					int line = Static.getInt32(element.get("line", t), t);
					File file = new File(element.get("file", t).val());
					int col = Static.getInt32(element.get("col", t), t);
					Target stElementTarget = new Target(line, file, col);
					newSt.add(new StackTraceElement(element.get("id", t).val(), stElementTarget));
				}

				String nType = exception.get("classType", t).val();
				String nMessage = exception.get("message", t).val();
				if(!"".equals(nMessage.trim())) {
					nMessage = ": " + nMessage;
				}
				PrintMessage(log, console, player, nType, nMessage, ex, newSt);
				ex = (ConfigRuntimeException) ex.getCause();
			}
		}
		//Log
		//Don't log to screen though, since we're ALWAYS going to do that ourselves.
		MSLog.GetLogger().Log("COMPILE ERROR".equals(exceptionType) ? MSLog.Tags.COMPILER : MSLog.Tags.RUNTIME,
				LogLevel.ERROR, log.toString(), top, false);
		//Console
		StreamUtils.GetSystemOut().println(console.toString() + TermColors.reset());
		//Player
		if(currentPlayer != null) {
			currentPlayer.sendMessage(player.toString());
		}
	}

	private static void DoReport(ConfigRuntimeException e, Environment env) {
		MCPlayer p = null;
		if(e.getEnv() != null && e.getEnv().getEnv(CommandHelperEnvironment.class).GetPlayer() != null) {
			p = e.getEnv().getEnv(CommandHelperEnvironment.class).GetPlayer();
		}
		List<StackTraceElement> st = new ArrayList<>();
		if(e instanceof AbstractCREException) {
			st = ((AbstractCREException) e).getCREStackTrace();
		}
		DoReport(e.getMessage(), AbstractCREException.getExceptionName(e), e, st, p);
		if(Prefs.DebugMode()) {
			if(e.getCause() != null && !(e.getCause() instanceof CRECausedByWrapper)) {
				//This is more of a system level exception, so if debug mode is on, we also want to print this stack trace
				StreamUtils.GetSystemErr().println("The previous MethodScript error had an attached cause:");
				e.getCause().printStackTrace(StreamUtils.GetSystemErr());
			}
			if(e.getTarget().equals(Target.UNKNOWN)) {
				//This should never happen, but there are still some hard to track
				//down bugs that cause this. If it does happen, we want to print out
				//a stacktrace from here, which *might* assist in fixing the error
				//messages to provide a proper target.
				StreamUtils.GetSystemErr().println("Since the exception has an unknown code target, here is additional information that may help:");
				StreamUtils.GetSystemErr().println(StackTraceUtils.GetStacktrace(new Exception()));
			}
		}
	}

	private static void DoReport(ConfigCompileException e, MCPlayer player) {
		List<StackTraceElement> st = new ArrayList<StackTraceElement>();
		st.add(0, new StackTraceElement("", e.getTarget()));
		DoReport(e.getMessage(), "COMPILE ERROR", null, st, player);
	}

	/**
	 * Shorthand for DoWarning(exception, null, true);
	 *
	 * @param e
	 */
	public static void DoWarning(Exception e) {
		DoWarning(e, null, true);
	}

	/**
	 * Shorthand for DoWarning(null, message, true);
	 *
	 * @param optionalMessage
	 */
	public static void DoWarning(String optionalMessage) {
		DoWarning(null, optionalMessage, true);
	}

	/**
	 * To standardize the warning messages displayed, this function should be used. It checks the preference setting for
	 * warnings to see if the warning should be shown to begin with, if checkPref is true. The exception is simply used
	 * to get an error message, and is otherwise unused. If the exception is a ConfigRuntimeException, it is displayed
	 * specially (including line number and file)
	 *
	 * @param e
	 * @param optionalMessage
	 * @throws NullPointerException If both the exception and message are null (or empty)
	 */
	public static void DoWarning(Exception e, String optionalMessage, boolean checkPrefs) {
		if(e == null && (optionalMessage == null || optionalMessage.isEmpty())) {
			throw new NullPointerException("Both the exception and the message cannot be empty");
		}
		if(!checkPrefs || Prefs.ShowWarnings()) {
			String exceptionMessage = "";
			Target t = Target.UNKNOWN;
			if(e instanceof ConfigRuntimeException) {
				ConfigRuntimeException cre = (ConfigRuntimeException) e;
				exceptionMessage = MCChatColor.YELLOW + cre.getMessage()
						+ MCChatColor.WHITE + " :: " + MCChatColor.GREEN
						+ AbstractCREException.getExceptionName(cre) + MCChatColor.WHITE + ":"
						+ MCChatColor.YELLOW + cre.target.file() + MCChatColor.WHITE + ":"
						+ MCChatColor.AQUA + cre.target.line();
				t = cre.getTarget();
			} else if(e != null) {
				exceptionMessage = MCChatColor.YELLOW + e.getMessage();
			}
			String message = exceptionMessage + MCChatColor.WHITE + optionalMessage;
			MSLog.GetLogger().Log(MSLog.Tags.GENERAL, LogLevel.WARNING, Static.MCToANSIColors(message) + TermColors.reset(), t);
			//Warnings are not shown to players ever
		}
	}

	private Environment env;
	private Target target;

	private void createException(Target t) {
		this.target = t;
	}

	public void setTarget(Target t) {
		this.target = t;
	}

//	public ConfigRuntimeException(String msg, ExceptionType ex, int line_num){
//		this(msg, ex, line_num, null);
//	}
//	public ConfigRuntimeException(String msg, int line_num){
//		this(msg, null, line_num, null);
//	}
	/**
	 * Creates an uncatchable exception. This should rarely be used. An uncatchable exception is one where the user code
	 * is unable to catch the exception, and the type of the exception is null. Generally, this should only be used for
	 * completely fatal errors, for instance, a break/continue being used in top level code, or other user errors that
	 * would be compile errors, except the facilities aren't there yet for catching such an error in the compiler, or
	 * for errors in MethodScript itself.
	 *
	 * @param msg
	 * @param t
	 * @return Returns an uncatchable exception.
	 */
	public static ConfigRuntimeException CreateUncatchableException(String msg, Target t) {
		return new ConfigRuntimeException(msg, t, null);
	}

	/**
	 * Creates an uncatchable exception with a cause. This should rarely be used. An uncatchable exception is one where
	 * the user code is unable to catch the exception, and the type of the exception is null. Generally, this should
	 * only be used for completely fatal errors, for instance, a break/continue being used in top level code, or other
	 * user errors that would be compile errors, except the facilities aren't there yet for catching such an error in
	 * the compiler, or for errors in MethodScript itself.
	 *
	 * @param msg
	 * @param t
	 * @param cause
	 * @return Returns an uncatchable exception
	 */
	public static ConfigRuntimeException CreateUncatchableException(String msg, Target t, Throwable cause) {
		return new ConfigRuntimeException(msg, t, cause);
	}

	/**
	 * Gets the code target for this exception.
	 *
	 * @return
	 */
	public Target getTarget() {
		return this.target;
	}

	/**
	 * Gets the shorter name of the file, or null if no file has been set.
	 *
	 * @return
	 */
	public String getSimpleFile() {
		if(this.target.file() != null) {
			return this.target.file().getName();
		} else {
			return null;
		}
	}

	/**
	 * A stacktrace contains 1 or more stack trace elements. A new stacktrace element is added each time an exception
	 * bubbles up past a procedure.
	 */
	public static class StackTraceElement {

		private final String procedureName;
		private Target definedAt;

		/**
		 * Creates a new StackTraceElement.
		 *
		 * @param procedureName The name of the procedure
		 * @param definedAt The code target where the procedure is defined at.
		 */
		public StackTraceElement(String procedureName, Target definedAt) {
			this.procedureName = procedureName;
			this.definedAt = definedAt;
		}

		/**
		 * Gets the name of the procedure.
		 *
		 * @return
		 */
		public String getProcedureName() {
			return procedureName;
		}

		/**
		 * Gets the code target where the procedure is defined at.
		 *
		 * @return
		 */
		public Target getDefinedAt() {
			return definedAt;
		}

		@Override
		public String toString() {
			return procedureName + " (Defined at " + definedAt + ")";
		}

		public CArray getObjectFor() {
			CArray element = CArray.GetAssociativeArray(Target.UNKNOWN);
			element.set("id", getProcedureName());
			try {
				String name = "Unknown file";
				if(getDefinedAt().file() != null) {
					name = getDefinedAt().file().getCanonicalPath();
				}
				element.set("file", name);
			} catch (IOException ex) {
				// This shouldn't happen, but if it does, we want to fall back to something marginally useful
				String name = "Unknown file";
				if(getDefinedAt().file() != null) {
					name = getDefinedAt().file().getAbsolutePath();
				}
				element.set("file", name);
			}
			element.set("line", new CInt(getDefinedAt().line(), Target.UNKNOWN), Target.UNKNOWN);
			element.set("col", new CInt(getDefinedAt().col(), Target.UNKNOWN), Target.UNKNOWN);
			return element;
		}

		/**
		 * In general, only the core elements should change this
		 *
		 * @param target
		 */
		void setDefinedAt(Target target) {
			definedAt = target;
		}

	}

}
