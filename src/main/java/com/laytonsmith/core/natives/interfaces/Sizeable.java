package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.constructs.CClassType;

/**
 * Any object that can report a size should implement this.
 */
@typeof("ms::lang::Sizeable")
public interface Sizeable extends Mixed {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get("ms::lang::Sizeable");

	/**
	 * Returns the size of this object.
	 *
	 * @return
	 */
	long size();
}
