/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.GenericTreeNode;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.util.List;

/**
 * Note that to "activate" this class as a function, you must prefix the '@api' annotation
 * to it.
 * @author layton
 */
public interface Function extends Documentation {

    /**
     * The name of this function, exactly as should be used in a script. Note that the name of
     * the function must match the regex:
     * <pre>
     * [a-zA-Z_][a-zA-Z_0-9]*
     * </pre>
     * In other words, it must start with a letter or underscore, and may have any number of
     * letters, numbers, or underscores after it.
     * @return 
     */
    public String getName();

    /**
     * The number of arguments this function can accept. Some functions may be able to accept multiple numbers
     * of arguments, so this function returns an array. If you return Integer.MAX_VALUE as one of the
     * items in the array, then any number of arguments may be sent.
     * @return 
     */
    public Integer[] numArgs();

    /**
     * If a user asks for information about a particular function, this method is called to obtain the functions
     * usage. The returned string must follow the following format:
     * @return A string with the documentation, or null, which will give a standard message to the user telling them there
     * is no documentation for this function yet.
     */
    public String docs();

    /**
     * Returns the types of catchable exceptions this function can throw. (Uncatchable exceptions need not be listed)
     * @return An array of the exception enums, or null, if the function throws no catchable exceptions.
     */
    public ExceptionType[] thrown();

    /**
     * Whether or not a function needs to be checked against the permissions file, if there are possible security concerns
     * with a user compiling, or running this function. If this function returns true, the permissions file will be checked for
     * commandhelper.func.compile.&lt;function name&gt; upon compilation, and commandhelper.func.use.&lt;function name&gt; upon
     * usage in game. Note that the config script is never barred from compiling any function.
     * @return 
     */
    public boolean isRestricted();

    /**
     * Most functions don't care that a construct is a variable, they simply care about the value stored in the variable.
     * If the function is concerned with the variable listing however, then it has direct access to the variable list for
     * this command.
     * @param varList 
     */
    //public void varList(IVariableList varList);
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
    public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException;
    
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
    public Construct execs(Target t, Env env, Script parent, GenericTreeNode<Construct> ... nodes);
    
    /**
     * Some functions don't need to show up in documentation. Maybe they are experimental, or magic
     * functions. If they shouldn't show up in the normal API documentation, return false.
     */
    public boolean appearInDocumentation();
    
    /**
     * If a function can possibly optimize during compilation, this should return true. This is only
     * relevant for functions that do NOT useSpecialExec, as if it takes a code tree, it is assumed that
     * there is no way to optimize. If a function CAN optimize, and the arguments being sent to the
     * function are constants, then the function will go ahead and run during compile time. Only functions
     * that are true functions in the mathematical sense should return true, that is, if there is ANY
     * case where the function might return differently, even given the same arguments, it CANNOT be
     * optimized. A function that returns false for preResolveVariables will be sent the arguments even
     * if they are variables, which may allow for errors to be caught at compile time if a function is
     * expecting an ivariable to be passed in (such as assign). Functions that can't optimize, but
     * can check argument types can also return true here.
     * 
     * If this returns true, optimize() will be called during compile time, which should return the construct
     * to replace this function, if possible. It may also return {@code null}, in which case no changes will
     * be made; this is useful for simple type checking however.
     * @return 
     */
    public boolean canOptimize();   
    
    /**
     * This is called during compile time, if canOptimize returns true. It should return the construct
     * to replace this function, if possible. If only type checking is being done, it may return null, in
     * which case no changes will be made to the parse tree. During the optimization, it is also possible
     * for a function to throw a ConfigCompileException. It may also throw a ConfigRuntimeException, which
     * will be caught, and changed into a ConfigCompileException.
     * @param t
     * @param args
     * @return 
     */
    public Construct optimize(Target t, Construct... args) throws ConfigRuntimeException, ConfigCompileException;
    
    /**
     * If a function knows how to optimize, even if portions are dynamic, it can return true here.
     * If this returns true, the value of canOptimize is ignored, and optimize will never get called.
     * @return 
     */
    public boolean canOptimizeDynamic();
    
    /**
     * If the function indicates it can optimize dynamic values, this method is called. It may
     * also throw a compile exception should the parameters be unacceptable. It may return null
     * if no changes should be made (which is likely the default).
     * @param t
     * @param children
     * @return 
     */
    public GenericTreeNode<Construct> optimizeDynamic(Target t, List<GenericTreeNode<Construct>> children) throws ConfigCompileException, ConfigRuntimeException;

    /**
     * If a function's syntax allows { braces }, then this method should return true. Exceedingly few functions
     * should allow this, as only language construct emulators should use braces.
     * @return 
     */
    public boolean allowBraces();
}
