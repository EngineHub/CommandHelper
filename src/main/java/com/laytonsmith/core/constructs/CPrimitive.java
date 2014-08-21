package com.laytonsmith.core.constructs;

import com.laytonsmith.annotations.typeof;

/**
 *
 */
@typeof("primitive")
public abstract class CPrimitive extends Construct {

	public CPrimitive(String value, ConstructType type, Target t) {
		super(value, type, t);
	}

}
