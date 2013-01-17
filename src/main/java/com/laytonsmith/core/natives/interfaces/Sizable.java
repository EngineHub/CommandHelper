package com.laytonsmith.core.natives.interfaces;

/**
 * Any object that can report a size should implement this.
 * @author lsmith
 */
public interface Sizable {
	
	/**
	 * Returns the size of this object.
	 * @return 
	 */
	long size();
}
