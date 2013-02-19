

package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.constructs.CPrimitive;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;

/**
 *
 * @author Layton
 */
@typename("mixed")
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
	 * Returns a standardized string representation of the underlying Construct
	 * represented by this object. This can be different than the toString method,
	 * since the toString method is meant for debugging purposes.
	 * @return 
	 */
	String val();
	
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
	
	/**
	 * Casts this value to a primitive, if possible. This intended as a convenience
	 * method for INTERNAL code, as opposed to user facing code. Only the CPrimitive
	 * class should actually implement this, other implementations should throw a
	 * ConfigRuntimeException.
	 * @return 
	 */
	CPrimitive primitive(Target t) throws ConfigRuntimeException;
	
	/**
	 * Returns true if the class definition is defined as immutable.
	 * @return 
	 */
	boolean isImmutable();
}
