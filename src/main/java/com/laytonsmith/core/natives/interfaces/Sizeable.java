package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.annotations.typeof;

/**
 * Any object that can report a size should implement this.
 */
@typeof("Sizeable")
public interface Sizeable extends Mixed {

	/**
	 * Returns the size of this object.
	 * @return
	 */
	long size();
}
