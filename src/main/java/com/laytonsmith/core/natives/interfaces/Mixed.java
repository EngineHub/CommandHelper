

package com.laytonsmith.core.natives.interfaces;

/**
 *
 * @author Layton
 */
public interface Mixed {
	/**
	 * Returns the underlying POJO represented by this object
	 * @return 
	 */
    Object value();
	
	/**
	 * Returns a string representation of the underlying POJO represented
	 * by this object.
	 * @return 
	 */
	@Override
	String toString();
	
	/**
	 * Returns true if the underlying object is null.
	 * @return 
	 */
	boolean isNull();
	
	/**
	 * Returns the type name of this object, for use in error messages
	 * and things. For now, "fully qualified" doesn't matter, though this
	 * will change in the future.
	 * @return 
	 */
	String typeName();
}
