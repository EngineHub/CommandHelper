package com.laytonsmith.core.functions;

/**
 * All functions that are implemented across all compilation platforms must implement
 * this interface at minimum. The annotation provides the platform(s) that the
 * function can be linked against.
 * @author layton
 */
public interface FunctionBase {
    /**
     * Some functions don't need to show up in documentation. Maybe they are experimental, or magic
     * functions. If they shouldn't show up in the normal API documentation, return false.
     */
    public boolean appearInDocumentation();

    /**
     * If a user asks for information about a particular function, this method is called to obtain the functions
     * usage. The returned string must follow the following format:
     * @return A string with the documentation, or null, which will give a standard message to the user telling them there
     * is no documentation for this function yet.
     */
    public String docs();

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
}
