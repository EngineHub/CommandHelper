/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;

/**
 * Things that implement this can be accessed like an array, with array_get, or [].
 * @author Layton
 */
public interface ArrayAccess extends Mixed {
    /**
     * Return the mixed at this location. This should throw an exception if
     * the index does not exist.
     * @param index
     * @return 
     */
    public Construct get(String index, Target t);
    /**
     * Return the size of the array
     * @return 
     */
    public int size();
    
    /**
     * Just because it is an array doesn't mean it will be associative. For optimiziation purposes, it
     * may be possible to check at compile time if the code is attempting to send a non-integral index,
     * in which case we can throw a compile error.
     * @return 
     */
    public boolean canBeAssociative();
}
