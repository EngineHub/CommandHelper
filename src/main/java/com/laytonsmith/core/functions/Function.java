

package com.laytonsmith.core.functions;

import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import java.util.List;

/**
 * Note that to "activate" this class as a function, you must prefix the '@api' annotation
 * to it.
 */
//This will be re-added at some point, so get ready!
//@MustUseOverride
public interface Function extends FunctionBase, Documentation {

    /**
     * Returns the types of catchable exceptions this function can throw. (Uncatchable exceptions need not be listed)
     * @return An array of the exception enums, or null, if the function throws no catchable exceptions.
     */
    public Class<? extends CREThrowable>[] thrown();

    /**
     * Whether or not a function needs to be checked against the permissions file, if there are possible security concerns
     * with a user running this function. If this function returns true, the permissions file will be checked for
     * commandhelper.func.use.&lt;function name&gt; upon usage in game.
     * @return
     */
    public boolean isRestricted();

    /**
     * If a function doesn't want to have to deal with a variable as a variable, but instead wants to recieve it as
     * an atomic, resolved value, the function can return true from this function. This will signal the interpreter
     * to go ahead and resolve the variable into one of the atomic Constructs. If it returns false, it is possible
     * the exec function will receive an IVariable Construct.
     * @return
     */
    public boolean preResolveVariables();

    /**
     * Whether or not to run this function asynchronously from the main server thread. If you
     * return true, you may NOT have any interaction with the bukkit api, other than
     * bukkit thread safe methods. Returning true WILL run this function in the CH thread, returning
     * false WILL run this function in the main server thread, and returning null will run this
     * function in whatever context the script is currently running in.
     * @return
     */
    public Boolean runAsync();

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
    public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException;

    /**
     * If a function needs a code tree instead of a resolved construct, it should return true here. Most
     * functions will return false for this value.
     * @return
     */
    public boolean useSpecialExec();

    /**
     * If useSpecialExec indicates it needs the code tree instead of the resolved constructs,
     * this gets called instead of exec. If execs is needed, exec should return CVoid.
     * @param t
     * @param env
     * @param nodes
     * @return
     */
    public Construct execs(Target t, Environment env, Script parent, ParseTree ... nodes);    

	/**
	 * Returns an array of example scripts, which are used for documentation purposes.
	 * @return
	 */
	public ExampleScript[] examples() throws ConfigCompileException;

	/**
	 * Returns true if this function should be profilable. Only a very select few functions
	 * should avoid profiling. AbstractFunction handles this by checking if the noprofile
	 * annotation is present.
	 * @return
	 */
	public boolean shouldProfile();

	/**
	 * Returns the minimum level at which this function should be profiled at.
	 * @return
	 */
	public LogLevel profileAt();

	/**
	 * Returns the message to use when this function gets profiled, if
	 * useSpecialExec returns false.
	 * @return
	 */
	public String profileMessage(Construct ... args);

	/**
	 * Returns the message to use when this function gets profiled, if
	 * useSpecialExec returns true.
	 * @param args
	 * @return
	 */
	public String profileMessageS(List<ParseTree> args);

    /**
     * In addition to being a function, an object may also be a code branch, that is,
     * it conditionally will execute some of its arguments.
     * For optimization and static code analysis purposes,
     * it it important for these functions to be able to provide more information
     * about their branches; if the branch conditions are static, they should be reduceable to a single
     * branch anyways, but some optimizations require knowledge about code branches.
     */
    public interface CodeBranch{
		/**
		 * Returns a list of all the child nodes that are considered separate code branches.
		 * Likely this is all of them, but not necessarily, especially if the optimization routine could
		 * eliminate some of the branches, due to const conditions. The current "self" ParseTree is passed
		 * in, which is the function's ParseTree wrapper, and from there, the children can be selected. The
		 * list of children returned should reference equal (==, not just .equals()) the children passed in.
		 * @return
		 */
        public List<ParseTree> getBranches(ParseTree self);
    }
}
