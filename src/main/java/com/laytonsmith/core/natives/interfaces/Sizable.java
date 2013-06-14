package com.laytonsmith.core.natives.interfaces;

/**
 * Any object that can report a size should implement this.
 */
public interface Sizable extends Mixed {
	
	/**
	 * Returns the size of this object.
	 * @return 
	 */
	int size();
}
