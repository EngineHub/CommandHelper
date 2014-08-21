package com.laytonsmith.core.constructs;

import com.laytonsmith.annotations.typeof;

/**
 *
 */
@typeof("number")
public abstract class CNumber extends CPrimitive {

	public CNumber(String value, ConstructType type, Target t) {
		super(value, type, t);
	}


}
