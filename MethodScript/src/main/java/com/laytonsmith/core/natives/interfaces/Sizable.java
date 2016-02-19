package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.annotations.typeof;

/**
 * Any object that can report a size should implement this.
 */
@typeof("Sizable")
public interface Sizable {

	/**
	 * Returns the size of this object.
	 * @return
	 */
	long size();
}
