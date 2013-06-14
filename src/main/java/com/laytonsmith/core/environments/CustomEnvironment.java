package com.laytonsmith.core.environments;

/**
 * This abstracts environments that allow for custom environment values to be set.
 * There may be multiple environment implementations that have this, but a single subtype
 * will be chosen by {@link Environment#getCustomEnvironment} consistently.
 */
public interface CustomEnvironment extends Environment.EnvironmentImpl {
	
	/**
	 * Use this if you would like to stick a custom variable in the environment.
	 * It should be discouraged to use this for more than one shot transfers.
	 * Typically, an setter and getter should be made to wrap the element.
	 *
	 * @param name The custom parameter name
	 * @param var The object to store
	 */
	public void SetCustom(String name, Object var);
	
	/**
	 * Returns the custom value to which the specified key is mapped, or null if
	 * this map contains no mapping for the key.
	 *
	 * @param name The custom parameter name
	 * @return The object previously stored with SetCustom. If the object had
	 * never been set, behavior is undefined.
	 */
	public Object GetCustom(String name);
	
	/**
	 * Returns true if the flag by this name has been set.
	 * @param name The flag name
	 * @return 
	 */
	public boolean HasFlag(String name);
	
	/**
	 * Sets a flag in the environment, causing future calls to {@link #HasFlag(java.lang.String)} to
	 * return true;
	 * @param name The flag name
	 */
	public void SetFlag(String name);
	
	/**
	 * Clears a flag in the environment, causing future calls to {@link #HasFlag(java.lang.String)} to
	 * return false;
	 * @param name The flag name
	 */
	public void ClearFlag(String name);
}
