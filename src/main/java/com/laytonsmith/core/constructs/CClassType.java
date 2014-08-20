package com.laytonsmith.core.constructs;

import com.laytonsmith.annotations.typeof;

/**
 *
 */
@typeof("ClassType")
public class CClassType extends Construct {

	public static final CClassType MIXED = new CClassType("mixed", Target.UNKNOWN);
	public static final CClassType AUTO = new CClassType("auto", Target.UNKNOWN);

	public CClassType(String type, Target t) {
		super(type, ConstructType.CLASS_TYPE, t);
	}


	@Override
	public boolean isDynamic() {
		return false;
	}

}
