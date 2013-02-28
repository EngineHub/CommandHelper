package com.laytonsmith.core.natives.annotations;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.Documentation;

/**
 * The superclass for all annotations.
 * @author lsmith
 */
public abstract class MAnnotation implements Documentation {

	public String getName() {
		return this.getClass().getAnnotation(typename.class).value();
	}

}
