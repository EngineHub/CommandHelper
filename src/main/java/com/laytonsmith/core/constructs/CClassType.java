package com.laytonsmith.core.constructs;

import com.laytonsmith.annotations.typeof;

/**
 *
 */
@typeof("ClassType")
public class CClassType extends Construct {

	public CClassType(String type, Target t) {
		super(type, ConstructType.CLASS_TYPE, t);
	}


	@Override
	public boolean isDynamic() {
		return false;
	}

}
