

package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;

/**
 * Things that implement this can be accessed like an array, with array_get, or [].
 * @author Layton
 */
public interface ArrayAccess extends Mixed, Sizable {
    /**
     * Return the mixed at this location. This should throw an exception if
     * the index does not exist.
     * @param index
     * @return 
     */
    public Construct get(String index, Target t) throws ConfigRuntimeException;
	
    /**
     * Return the size of the array
     * @return 
     */
	@Override
    public long size();
    
    /**
     * Just because it is accessible as an array doesn't mean it will be associative. For optimiziation purposes, it
     * may be possible to check at compile time if the code is attempting to send a non-integral index,
     * in which case we can throw a compile error.
     * @return 
     */
    public boolean canBeAssociative();
    
    /**
     * Returns a slice at the specified location. Should throw an exception if an element in
     * the range doesn't exist.
     * @param begin
     * @param end
     * @param t
     * @return 
     */
    public Construct slice(int begin, int end, Target t);
}
