package com.laytonsmith.core.constructs;

import com.laytonsmith.annotations.typeof;

/**
 *
 */
@typeof("ClassType")
public class CClassType extends Construct {

	public static final CClassType MIXED = new CClassType("mixed", Target.UNKNOWN);
	public static final CClassType AUTO = new CClassType("auto", Target.UNKNOWN);
	public static final CClassType VOID = new CClassType("void", Target.UNKNOWN);

	public CClassType(String type, Target t) {
		super(type, ConstructType.CLASS_TYPE, t);
	}


	@Override
	public boolean isDynamic() {
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CClassType){
			return this.val().equals(((CClassType)obj).val());
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

}
