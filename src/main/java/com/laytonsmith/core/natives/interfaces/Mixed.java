

package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.constructs.CPrimitive;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;

/**
 *
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
	
	/**
	 * Returns true if this is dynamic, that is to say, isn't a constant.
	 * If the underlying value (or values) is mutable, it is dynamic.
	 * @return 
	 */
	boolean isDynamic();
	
	/**
	 * This is called upon destruction of the object.
	 */
	void destructor();
	
	/**
	 * All mixed objects must be cloneable by default.
	 * @return 
	 */
	public Mixed doClone();
	
	/**
	 * Most things, other than constructs can return Target.UNKNOWN for this.
	 * @return 
	 */
	public Target getTarget();
}
