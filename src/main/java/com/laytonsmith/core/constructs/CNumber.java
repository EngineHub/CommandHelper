package com.laytonsmith.core.constructs;

import com.laytonsmith.annotations.typename;

/**
 * The superclass of Numeric types.
 * @author lsmith
 */
@typename("number")
public abstract class CNumber extends CPrimitive {
	protected final Number number;
	protected CNumber(Number value, Target t){
		super(value, t);
		number = value;
	}
	
	@Override
	public String castToString() {
		return val();
	}

	@Override
	public double castToDouble(Target t) {
		return number.doubleValue();
	}

	@Override
	public long castToInt(Target t) {
		return number.intValue();
	}

	@Override
	public boolean castToBoolean() {
		return number.doubleValue() == 0 ? false : true;
	}

}
