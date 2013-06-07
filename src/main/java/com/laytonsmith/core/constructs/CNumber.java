package com.laytonsmith.core.constructs;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.natives.interfaces.Mixed;

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

	@Override
	public CNumber operatorAddition(Mixed m) {
		return (CNumber)super.operatorAddition(m);
	}

	@Override
	public CNumber operatorSubtraction(Mixed m) {
		return (CNumber)super.operatorSubtraction(m);
	}

	@Override
	public CNumber operatorMultiplication(Mixed m) {
		return (CNumber)super.operatorMultiplication(m);
	}

	@Override
	public CNumber operatorDivision(Mixed m) {
		return (CNumber)super.operatorDivision(m);
	}

}
