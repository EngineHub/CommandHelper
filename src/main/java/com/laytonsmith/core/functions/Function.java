

package com.laytonsmith.core.functions;

import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.arguments.Argument;
import com.laytonsmith.core.arguments.ArgumentBuilder;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.List;

/**
 * Note that to "activate" this class as a function, you must prefix the '@api' annotation
 * to it.
 * @author layton
 */
public interface Function extends FunctionBase, Documentation {
	
	/**
	 * Returns the expected return type of this function. This is
	 * used for both compile time checking of data types, and documentation
	 * purposes.
	 * @return 
	 */
	Argument returnType();
	
	/**
	 * Returns the ArgumentBuilder object for this function. This is used
	 * during compile time to check types, where applicable, and to ease parsing
	 * of the function's arguments, and for documentation purposes.
	 * @return 
	 */
	ArgumentBuilder arguments();
	
	/**
	 * If the function provides an actual argument builder which can be used
	 * to automatically produce the documentation, but cannot be used to
	 * do the actual type checking of variables, this should return false.
	 * For most functions, true should be returned, but if a function is extra complex,
	 * then it may return false. It is assumed that the function will provide the type
	 * checking, optimally at compile time where possible, with the optimization functions.
	 * <strong>If you return false from this method, you should seriously consider changing the
	 * signature of the function to work with the built in type system.</strong>
	 * @return 
	 */
	boolean doTypeCheck();
	
	/**
	 * If the ArgumentBuilder returns {@link ArgumentBuilder#MANUAL}, then 
	 * this function is called to get the signature of the method for documentation
	 * purposes. No typechecking is performed on the function parameters, and the builder
	 * will be unavailable. This is meant for existing functions to use, and should not
	 * be used by new functions. In the future, if the function's signature is too complex
	 * to use the actual type checking framework, you should consider reworking the 
	 * function so that it works with a more standard signature. Eventually, this
	 * method will be removed.
	 * @return 
	 */
	@Deprecated
	String argumentsManual();

    /**
     * Returns the types of catchable exceptions this function can throw. (Uncatchable exceptions need not be listed)
     * @return An array of the exception enums, or null, if the function throws no catchable exceptions.
     */
    ExceptionType[] thrown();

    /**
     * Whether or not a function needs to be checked against the permissions file, if there are possible security concerns
     * with a user compiling, or running this function. If this function returns true, the permissions file will be checked for
     * commandhelper.func.compile.&lt;function name&gt; upon compilation, and commandhelper.func.use.&lt;function name&gt; upon
     * usage in game. Note that the config script is never barred from compiling any function.
     * @return 
     */
    boolean isRestricted();

    /**
     * If a function doesn't want to have to deal with a variable as a variable, but instead wants to recieve it as
     * an atomic, resolved value, the function can return true from this function. This will signal the interpreter
     * to go ahead and resolve the variable into one of the atomic Constructs. If it returns false, it is possible
     * the exec function will receive an IVariable Construct.
     * @return 
     */
    boolean preResolveVariables();

    /**
     * Whether or not to run this function asynchronously from the main server thread. If you 
     * return true, you may NOT have any interaction with the bukkit api, other than
     * bukkit thread safe methods. Returning true WILL run this function in the CH thread, returning
     * false WILL run this function in the main server thread, and returning null will run this
     * function in whatever context the script is currently running in.
     * @return 
     */
    Boolean runAsync();

    /**
     * This function is invoked when the script is run. The line number is provided so that if there is an error,
     * the function can provide a more specific error message for the user. If the function was canceled due to a fatal error
     * in the syntax of the user input or some similar situation, it should throw a ConfigRuntimeException.
     * All parameters sent to the
     * function have already been resolved into an atomic value, so functions do not have to worry about
     * resolving parameters. There is an explicit check made before calling exec to ensure that Construct ... args
     * will only be one of the atomic Constructs. If a code tree is needed instead of a resolved construct,
     * the function should indicate so, and {@code execs} will be called instead. If exec is needed,
     * execs should return CVoid.
     * @param line_num The line that this function call is being run from
     * @param f The file that this function call is being run from
     * @param args An array of evaluated Constructs
     * @return
     * @throws CancelCommandException 
     */
    Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException;
    
    /**
     * If a function needs a code tree instead of a resolved construct, it should return true here. Most
     * functions will return false for this value.
     * @return 
     */
    boolean useSpecialExec();
    
    /**
     * If useSpecialExec indicates it needs the code tree instead of the resolved constructs,
     * this gets called instead of exec. If execs is needed, exec should return CVoid.
     * @param t
     * @param env
     * @param nodes
     * @return 
     */
    Construct execs(Target t, Environment env, Script parent, ParseTree ... nodes);    
	
	/**
	 * Returns an array of example scripts, which are used for documentation purposes.
	 * @return 
	 */
	ExampleScript[] examples() throws ConfigCompileException;
	
	/**
	 * Returns true if this function should be profilable. Only a very select few functions
	 * should avoid profiling. AbstractFunction handles this by checking if the noprofile
	 * annotation is present.
	 * @return 
	 */
	boolean shouldProfile();	
	
	/**
	 * Returns the minimum level at which this function should be profiled at.
	 * @return 
	 */
	LogLevel profileAt();
	
	/**
	 * Returns the message to use when this function gets profiled, if
	 * useSpecialExec returns false.
	 * @return 
	 */
	String profileMessage(Construct ... args);
	
	/**
	 * Returns the message to use when this function gets profiled, if
	 * useSpecialExec returns true.
	 * @param args
	 * @return 
	 */
	String profileMessageS(List<ParseTree> args);
}
