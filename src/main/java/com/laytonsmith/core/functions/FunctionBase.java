package com.laytonsmith.core.functions;

//import com.laytonsmith.PureUtilities.Common.Annotations.ForceImplementation;
import com.laytonsmith.annotations.core;
import com.laytonsmith.core.snapins.PackagePermission;

/**
 * All functions that are implemented across all compilation platforms must implement this interface at minimum. The
 * annotation provides the platform(s) that the function can be linked against.
 */
public interface FunctionBase {

	/**
	 * The name of this function, exactly as should be used in a script. Note that the name of the function must match
	 * the regex:
	 * <pre>
	 * [a-zA-Z_][a-zA-Z_0-9]*
	 * </pre> In other words, it must start with a letter or underscore, and may have any number of letters, numbers, or
	 * underscores after it.
	 *
	 * @return
	 */
	//@ForceImplementation
	public String getName();

	/**
	 * The number of arguments this function can accept. Some functions may be able to accept multiple numbers of
	 * arguments, so this function returns an array. If you return Integer.MAX_VALUE as one of the items in the array,
	 * then any number of arguments may be sent.
	 *
	 * @return
	 */
	public Integer[] numArgs();

	/**
	 * If a user asks for information about a particular function, this method is called to obtain the functions usage.
	 * The returned string must follow the following format:
	 *
	 * @return A string with the documentation, or null, which will give a standard message to the user telling them
	 * there is no documentation for this function yet.
	 */
	//@ForceImplementation
	public String docs();

	/**
	 * Some functions don't need to show up in documentation. Maybe they are experimental, or magic functions. If they
	 * shouldn't show up in the normal API documentation, return false.
	 *
	 * @return
	 */
	public boolean appearInDocumentation();

	/**
	 * Returns the package permission required to use this function.
	 *
	 * @return
	 */
	public PackagePermission getPermission();

	/**
	 * Returns whether or not this function, or the function's containing class is annotated with the {@link core}
	 * annotation.
	 *
	 * @return
	 */
	public boolean isCore();
}
