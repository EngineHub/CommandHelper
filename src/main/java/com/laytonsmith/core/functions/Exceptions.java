package com.laytonsmith.core.functions;

import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.*;
import com.laytonsmith.core.arguments.ArgList;
import com.laytonsmith.core.arguments.Argument;
import com.laytonsmith.core.arguments.ArgumentBuilder;
import com.laytonsmith.core.arguments.Generic;
import com.laytonsmith.core.arguments.Signature;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.MEnum;
import com.laytonsmith.core.natives.annotations.NonNull;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Layton
 */
public class Exceptions {

	public static String docs() {
		return "This class contains functions related to Exception handling in MethodScript";
	}
	
	public static class CastException extends ConfigRuntimeException {

		public CastException(String msg, Target t) {
			super(msg, ExceptionType.CastException, t);
		}

		public CastException(String msg, Target t, Throwable cause) {
			super(msg, ExceptionType.CastException, t, cause);
		}				
	}
	
	public static class FormatException extends ConfigRuntimeException {

		public FormatException(String msg, Target t) {
			super(msg, ExceptionType.FormatException, t);
		}

		public FormatException(String msg, Target t, Throwable cause) {
			super(msg, ExceptionType.FormatException, t, cause);
		}
	}
	
	public static class RangeException extends ConfigRuntimeException {

		public RangeException(String msg, Target t) {
			super(msg, ExceptionType.RangeException, t);
		}

		public RangeException(String msg, Target t, Throwable cause) {
			super(msg, ExceptionType.RangeException, t, cause);
		}
	}
	
	public static class LengthException extends ConfigRuntimeException {

		public LengthException(String msg, Target t) {
			super(msg, ExceptionType.LengthException, t);
		}

		public LengthException(String msg, Target t, Throwable cause) {
			super(msg, ExceptionType.LengthException, t, cause);
		}
	}

	@typename("ExceptionType")
	public enum ExceptionType implements Documentation, MEnum {

		/**
		 * This exception is thrown if a value cannot be cast into an
		 * appropriate type. Functions that require a numeric value, for
		 * instance, would throw this if the string "hi" were passed in.
		 */
		CastException("This exception is thrown if a value cannot be cast into an"
			+ " appropriate type. Functions that require a numeric value, for"
			+ " instance, would throw this if the string \"hi\" were passed in.", CHVersion.V3_3_1),
		/**
		 * This exception is thrown if a value is requested from an array that
		 * is above the highest index of the array, or a negative number.
		 */
		IndexOverflowException("This exception is thrown if a value is requested from an array that"
			+ " is above the highest index of the array, or a negative number.", CHVersion.V3_3_1),
		/**
		 * This exception is thrown if a function expected a numeric value to be
		 * in a particular range, and it wasn't
		 */
		RangeException("This exception is thrown if a function expected a numeric value to be"
			+ " in a particular range, and it wasn't", CHVersion.V3_3_1),
		/**
		 * This exception is thrown if a function expected the length of
		 * something to be a particular value, but it was not.
		 */
		LengthException("This exception is thrown if a function expected the length of"
			+ " something to be a particular value, but it was not.", CHVersion.V3_3_1),
		/**
		 * This exception is thrown if the user running the command does not
		 * have permission to run the function
		 */
		InsufficientPermissionException("This exception is thrown if the user running the command does not"
			+ " have permission to run the function", CHVersion.V3_3_1),
		/**
		 * This exception is thrown if a function expected an online player, but
		 * that player was offline, or the command is being run from somewhere
		 * not in game, and the function was trying to use the current player.
		 */
		PlayerOfflineException("This exception is thrown if a function expected an online player, but"
			+ " that player was offline, or the command is being run from somewhere"
			+ " not in game, and the function was trying to use the current player.", CHVersion.V3_3_1),
		/**
		 * Some var arg functions may require at least a certain number of
		 * arguments to be passed to the function
		 */
		InsufficientArgumentsException("Some var arg functions may require at least a certain number of"
			+ " arguments to be passed to the function", CHVersion.V3_3_1),
		/**
		 * This exception is thrown if a function expected a string to be
		 * formatted in a particular way, but it could not interpret the given
		 * value.
		 */
		FormatException("This exception is thrown if a function expected a string to be"
			+ " formatted in a particular way, but it could not interpret the given"
			+ " value.", CHVersion.V3_3_1),
		/**
		 * This exception is thrown if a procedure is used without being
		 * defined, or if a procedure name does not follow proper naming
		 * conventions.
		 */
		InvalidProcedureException("This exception is thrown if a procedure is used without being"
			+ " defined, or if a procedure name does not follow proper naming"
			+ " conventions.", CHVersion.V3_3_1),
		/**
		 * This exception is thrown if there is a problem with an include. This
		 * is thrown if there is a compile error in the included script.
		 */
		IncludeException("This exception is thrown if there is a problem with an include. This"
			+ " is thrown if there is a compile error in the included script.", CHVersion.V3_3_1),
		/**
		 * This exception is thrown if a script tries to read or write to a
		 * location of the filesystem that is not allowed.
		 */
		SecurityException("This exception is thrown if a script tries to read or write to a"
			+ " location of the filesystem that is not allowed.", CHVersion.V3_3_1),
		/**
		 * This exception is thrown if a file cannot be read or written to.
		 */
		IOException("This exception is thrown if a file cannot be read or written to.", CHVersion.V3_3_1),
		/**
		 * This exception is thrown if a function uses an external plugin, and
		 * that plugin is not loaded, or otherwise unusable.
		 */
		InvalidPluginException("This exception is thrown if a function uses an external plugin, and"
			+ " that plugin is not loaded, or otherwise unusable.", CHVersion.V3_3_1),
		/**
		 * This exception is thrown when a plugin is loaded, but a call to the
		 * plugin failed, usually for some reason specific to the plugin. Check
		 * the error message for more details about this error.
		 */
		PluginInternalException("This exception is thrown when a plugin is loaded, but a call to the"
			+ " plugin failed, usually for some reason specific to the plugin. Check"
			+ " the error message for more details about this error.", CHVersion.V3_3_1),
		/**
		 * If a function requests a world, and the world given doesn't exist,
		 * this is thrown
		 */
		InvalidWorldException("If a function requests a world, and the world given doesn't exist,"
			+ " this is thrown", CHVersion.V3_3_1),
		/**
		 * This exception is thrown if an error occurs when trying to bind() an
		 * event, or if a event framework related error occurs.
		 */
		BindException("This exception is thrown if an error occurs when trying to bind() an"
			+ " event, or if a event framework related error occurs.", CHVersion.V3_3_1),
		/**
		 * If an enchantment is added to an item that isn't supported, this is
		 * thrown.
		 */
		EnchantmentException("If an enchantment is added to an item that isn't supported, this is thrown.", CHVersion.V3_3_1),
		/**
		 * If an age function is called on an unageable mob, this
		 * exception is thrown
		 */
		UnageableMobException("If an age function is called on an unageable mob, this "
			+ "exception is thrown.", CHVersion.V3_3_1),
		/**
		 * If an untameable mob is attempted to be tamed, this exception is
		 * thrown
		 */
		UntameableMobException("If an untameable mob is attempted to be tamed, this exception is"
			+ " thrown", CHVersion.V3_3_1),
		/**
		 * If a null is sent, but not expected, this exception is thrown.
		 */
		NullPointerException("If a null is sent, but not expected, this exception is thrown.", CHVersion.V3_3_1), 
		/**
		 * Thrown if an entity is looked up by id, but doesn't exist.
		 */
		BadEntityException("Thrown if an entity is looked up by id, but doesn't exist.", CHVersion.V3_3_1),
		
		/**
		 * Thrown if a field was read only, but a write operation was attempted.
		 */
		ReadOnlyException("Thrown if a field was read only, but a write operation was attempted.", CHVersion.V3_3_1),
		/**
		 * Thrown if a scoreboard error occurs, such as attempting to create a
		 * team or objective with a name that is already in use,
		 * or trying to access one that doesn't exist. 
		 */
		ScoreboardException("Thrown if a scoreboard error occurs, such as attempting to create a"
				+ " team or objective with a name that is already in use,"
				+ " or trying to access one that doesn't exist.", CHVersion.V3_3_1),
		/**
		 * Thrown if trying to register a plugin channel that is already registered, 
		 * or unregister one that isn't registered.
		 */
		PluginChannelException("Thrown if trying to register a plugin channel that is"
				+ " already registered, or unregister one that isn't registered.", 
				CHVersion.V3_3_1),
		/**
		 * Thrown if data was not found, but expected.
		 */
		NotFoundException("Thrown if data was not found, but expected.", CHVersion.V3_3_1);

		;
		
		private String docs;
		private CHVersion since;
		private ExceptionType(String docs, CHVersion since){
			this.docs = docs;
			this.since = since;
		}

		public String getName() {
			return name();
		}

		public String docs() {
			return docs;
		}

		public CHVersion since() {
			return since;
		}

		public Object value() {
			return this;
		}

		public String val() {
			return name();
		}

		public boolean isNull() {
			return false;
		}

		public String typeName() {
			return this.getClass().getAnnotation(typename.class).value();
		}

		public CPrimitive primitive(Target t) throws ConfigRuntimeException {
			throw new Error();
		}

		public boolean isImmutable() {
			return true;
		}

		public boolean isDynamic() {
			return false;
		}

		public void destructor() {
			
		}

		public Mixed doClone() {
			return this;
		}

		public Target getTarget() {
			return Target.UNKNOWN;
		}
	}

	@api(environments=CommandHelperEnvironment.class)
	public static class _try extends AbstractFunction {

		public String getName() {

			return "try";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3, 4};
		}

		public String docs() {
			return "This function works similar to a try-catch block in most languages. If the code in"
					+ " tryCode throws an exception, instead of killing the whole script, it stops running, and begins running the catchCode."
					+ " var should be an ivariable, and it is set to an array containing the following information about the exception:"
					+ " 0 - The class of the exception; 1 - The message generated by the exception; 2 - The file the exception was generated from; 3 - The line the exception"
					+ " occured on. ---- If exceptionTypes is provided, it should be an array of exception types, or a single string that this try function is interested in."
					+ " If the exception type matches one of the values listed, the exception will be caught, otherwise, the exception will continue up the stack."
					+ " If exceptionTypes is missing, it will catch all exceptions."
					+ " PLEASE NOTE! This function will not catch exceptions thrown by CommandHelper, only built in exceptions. "
					+ " Please see [[CommandHelper/Exceptions|the wiki page on exceptions]] for more information about what possible "
					+ " exceptions can be thrown and where, and examples.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Signature(1, 
						new Argument("The code to try", CCode.class, "tryCode"),
						new Argument("The variable name which will be used to assign the exception to", IVariable.class, "varName"),
						new Argument("The code that runs if the try block throws an exception", CCode.class, "catchCode"),
						new Argument("The exception types of interest", CString.class, "exceptionTypes")
					), new Signature(2, 
						new Argument("The code to try", CCode.class, "tryCode"),
						new Argument("The code that runs if the try block throws an exception", CCode.class, "catchCode")
					)
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return false;
		}

		@Override
		public boolean preResolveVariables() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_1_2;
		}

		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed execs(Target t, Environment env, Script that, ParseTree... nodes) {
			ParseTree tryCode = nodes[0];
			ParseTree varName = null;
			ParseTree catchCode = null;
			ParseTree types = null;
			if (nodes.length == 2) {
				catchCode = nodes[1];
			} else if (nodes.length == 3) {
				varName = nodes[1];
				catchCode = nodes[2];
			} else if (nodes.length == 4) {
				varName = nodes[1];
				catchCode = nodes[2];
				types = nodes[3];
			}

			IVariable ivar = null;
			if (varName != null) {
				Mixed pivar = that.eval(varName, env);
				if (pivar instanceof IVariable) {
					ivar = (IVariable) pivar;
				} else {
					throw new ConfigRuntimeException("Expected argument 2 to be an IVariable", ExceptionType.CastException, t);
				}
			}
			List<String> interest = new ArrayList<String>();
			if (types != null) {
				Mixed ptypes = that.seval(types, env);
				if (ptypes instanceof CString) {
					interest.add(ptypes.val());
				} else if (ptypes instanceof CArray) {
					CArray ca = (CArray) ptypes;
					for (int i = 0; i < ca.size(); i++) {
						interest.add(ca.get(i, t).val());
					}
				} else {
					throw new ConfigRuntimeException("Expected argument 4 to be a string, or an array of strings.",
							ExceptionType.CastException, t);
				}
			}

			for (String in : interest) {
				try {
					ExceptionType.valueOf(in);
				} catch (IllegalArgumentException e) {
					throw new ConfigRuntimeException("Invalid exception type passed to try():" + in,
							ExceptionType.FormatException, t);
				}
			}

			try {
				that.eval(tryCode, env);
			} catch (ConfigRuntimeException e) {
				if (Prefs.DebugMode()) {
					System.out.println("[CommandHelper]: Exception thrown -> " + e.getMessage() + " :: " + e.getExceptionType() + ":" + e.getFile() + ":" + e.getLineNum());
				}
				if (e.getExceptionType() != null && (interest.isEmpty() || interest.contains(e.getExceptionType().toString()))) {
					if (catchCode != null) {
						CArray ex = ObjectGenerator.GetGenerator().exception(e, t);
						if (ivar != null) {
							env.getEnv(GlobalEnv.class).GetVarList().set(ivar, ex);
						}
						that.eval(catchCode, env);
					}
				} else {
					throw e;
				}
			}

			return new CVoid(t);
		}

		public Construct exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			return new CVoid(t);
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}
	}

	@api
	public static class _throw extends AbstractFunction {

		public String getName() {
			return "throw";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "This function causes an exception to be thrown. If the exception type is null,"
					+ " it will be uncatchable. Otherwise, exceptionType may be any valid exception type.";
		}
		
		public Argument returnType() {
			return Argument.NONE;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The type of the exception to throw", ExceptionType.class, "exceptionType").addAnnotation(new NonNull()),
					new Argument("The message to include as a part of the exception", CString.class, "msg")
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_1_2;
		}

		public Boolean runAsync() {
			return null;
		}

		//The code: try(throw(...), @ex, ...) doesn't work,
		//because it sees throw, then kills the other children to try.
		//Blah.
//		@Override
//		public boolean isTerminal() {
//			return true;
//		}

		public Construct exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			ExceptionType exceptionType = list.getEnum("exceptionType", ExceptionType.class);
			String msg = list.getString("msg", t);
			throw new ConfigRuntimeException(msg, exceptionType, t);
		}
	}
	
	@api
	public static class set_uncaught_exception_handler extends AbstractFunction{

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(args[0] instanceof CClosure){
				environment.getEnv(GlobalEnv.class).SetExceptionHandler((CClosure)args[0]);
				return new CVoid(t);
			} else {
				throw new CastException("Expecting arg 1 of " + getName() + " to be a Closure, but it was " + args[0].val(), t);
			}
		}

		public String getName() {
			return "set_uncaught_exception_handler";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Sets the uncaught exception handler. If code throws an exception, instead of doing"
					+ " the default (displaying the error to the user/console) it will run your code instead. The exception"
					+ " that was thrown will be passed to the closure, and it is expected that the closure returns either null,"
					+ " true, or false. ---- If null is returned, the default handling will occur. If false is returned, it will"
					+ " be \"escalated\" which in the current implementation is the same as returning null (this will be used"
					+ " in the future). If true is returned, then default action will not occur, as it is assumed you have handled"
					+ " it. Only one exception handler can be registered at this time. If code inside the closure generates it's own"
					+ " exception, this will be handled by displaying both exceptions. To prevent this, you could put a try() block"
					+ " around the whole code block, but it is highly recommended you do not supress this. It is possible to completely"
					+ " supress all runtime exceptions using this method, but it is highly recommended that you still have a generic"
					+ " logging mechanism, perhaps to console, so you don't \"lose\" your exceptions, and fail to realize anything is wrong.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The closure to run when an uncaught exception travels all the way up the execution scope", CClosure.class, "closure").setGenerics(new Generic(CArray.class))
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
}
