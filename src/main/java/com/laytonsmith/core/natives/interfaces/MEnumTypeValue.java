package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.core.Documentation;

/**
 *
 * @author caismith
 */
public interface MEnumTypeValue extends Documentation {

	/**
	 * Returns the ordinal value for this entry. The ordinal value goes from 0 to size-1, and is defined by the
	 * order the values were defined.
	 * @return
	 */
	int ordinal();

	/**
	 * The name of this value
	 * @return
	 */
	String name();

}
