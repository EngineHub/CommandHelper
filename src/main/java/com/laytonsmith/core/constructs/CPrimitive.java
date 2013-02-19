package com.laytonsmith.core.constructs;

import com.laytonsmith.annotations.immutable;
import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.natives.interfaces.Operators;

/**
 * The superclass of all primitives in methodscript. Primitives all must
 * support implicit conversion from the three basic types, string, double, and 
 * integer, though they may throw exceptions if that conversion is not possible
 * at runtime. 
 * @author lsmith
 */
@typename("primitive")
@immutable
public abstract class CPrimitive extends Construct implements Mixed, 
		Operators.Concatenation, Operators.Equality, Operators.Mathematical, Operators.Relational{
	
	protected CPrimitive(Object value, Target t){
		super(value, t);
	}
	
	/**
	 * Casts this primitive to a CString and returns it.
	 * @return 
	 */
	public CString castToCString(){
		return new CString(castToString(), Target.UNKNOWN);
	}
	
	/**
	 * Casts this primitive to a POJO String and returns it.
	 * @return 
	 */
	public abstract String castToString();
	
	/**
	 * Casts this primitive to a CDouble and returns it.
	 * @return 
	 * @throws ConfigFormatException If the number cannot be reasonably cast.
	 */
	public CDouble castToCDouble(Target t) throws ConfigRuntimeException{
		return new CDouble(castToDouble(t), Target.UNKNOWN);
	}
	
	/**
	 * Casts this primitive to a POJO Double and returns it.
	 * @return 
	 * @throws ConfigFormatException If the number cannot be reasonably cast.
	 */
	public abstract double castToDouble(Target t) throws ConfigRuntimeException;
	
	/**
	 * Casts this primitive to a CInt and returns it. Loss of
	 * precision may occur.
	 * @return 
	 * @throws ConfigFormatException If the number cannot be reasonably cast.
	 */
	public CInt castToCInt(Target t) throws ConfigRuntimeException{
		return new CInt(castToInt(t), Target.UNKNOWN);
	}
	
	/**
	 * Casts this primitive to a POJO int and returns it. Loss of
	 * precision may occur.
	 * @return 
	 * @throws ConfigFormatException If the number cannot be reasonably cast.
	 */
	public abstract long castToInt(Target t) throws ConfigRuntimeException;
	
	/**
	 * Casts this primitive to a CBoolean and returns it.
	 * Loss of precision may occur. This may NOT throw an exception.
	 * @return 
	 */
	public CBoolean castToCBoolean(){
		return new CBoolean(castToBoolean(), Target.UNKNOWN);
	}
	
	/**
	 * Casts this primitive to a POJO boolean and returns it.
	 * Loss of precision may occur. This may NOT throw an exception.
	 * @return 
	 */
	public abstract boolean castToBoolean();
	
	/**
	 * Returns a 32 bit int from the construct. Since the backing value is actually
	 * a long, if the number contained in the construct is not the same after truncating,
	 * an exception is thrown (fail fast). When needing an int from a construct, this
	 * method is much preferred over silently truncating.
	 * @param t
	 * @return 
	 */
	public int castToInt32(Target t){
		long l = castToInt(t);
		int i = (int)l;
		if(i != l){
			throw new Exceptions.RangeException("Expecting a 32 bit integer, but a larger value was found: " + l, t);
		}
		return i;
	}
	
	/**
	 * Returns a 16 bit int from the construct (a short). Since the backing value is actually
	 * a long, if the number contained in the construct is not the same after truncating,
	 * an exception is thrown (fail fast). When needing an short from a construct, this
	 * method is much preferred over silently truncating.
	 * @param t
	 * @return 
	 */
	public short castToInt16(Target t){
		long l = castToInt(t);
		short s = (short)l;
		if(s != l){
			throw new Exceptions.RangeException("Expecting a 16 bit integer, but a larger value was found: " + l, t);
		}
		return s;
	}
	
	/**
	 * Returns an 8 bit int from the construct (a byte). Since the backing value is actually
	 * a long, if the number contained in the construct is not the same after truncating,
	 * an exception is thrown (fail fast). When needing a byte from a construct, this
	 * method is much preferred over silently truncating.
	 * @param t
	 * @return 
	 */
	public byte castToInt8(Target t){
		long l = castToInt(t);
		byte b = (byte)l;
		if(b != l){
			throw new Exceptions.RangeException("Expecting a 16 bit integer, but a larger value was found: " + l, t);
		}
		return b;
	}
	
	/**
	 * Returns a 32 bit floating point number from the construct. Since the backing
	 * value is actually a double, if the number contained in the construct is not the same
	 * after truncating, an exception is thrown (fail fast). When needing a float from
	 * a construct, this method is much preferred over silently truncating.
	 * @param t
	 * @return 
	 */
	public float castToDouble32(Target t){
		double delta = 0.000000001; //Eight places should be enough, right?
		double l = castToDouble(t);
		float f = (float)l;
		if(Math.abs(f - l) > delta){
			throw new Exceptions.RangeException("Expecting a 32 bit float, but a larger value was found: " + l, t);
		}
		return f;
	}

	@Override
	public CPrimitive primitive(Target t) throws ConfigRuntimeException {
		return this;
	}

	public Mixed operatorAddition(Mixed m) {
		double lhs = this.castToDouble(getTarget());
		double rhs = m.primitive(Target.UNKNOWN).castToDouble(Target.UNKNOWN);
		double result = lhs + rhs;
		if((long)result == result){
			return new CInt((long)result, getTarget());
		} else {
			return new CDouble(result, getTarget());
		}
	}

	public boolean operatorTestAddition(Class<? extends Mixed> clazz) {
		return CPrimitive.class.isAssignableFrom(clazz);
	}

	public Mixed operatorConcatenation(Mixed m) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean operatorTestConcatenation(Class<? extends Mixed> clazz) {
		return CPrimitive.class.isAssignableFrom(clazz);
	}

	public Mixed operatorDivision(Mixed m) {
		double lhs = this.castToDouble(getTarget());
		double rhs = m.primitive(Target.UNKNOWN).castToDouble(Target.UNKNOWN);
		double result = lhs / rhs;
		if((long)result == result){
			return new CInt((long)result, getTarget());
		} else {
			return new CDouble(result, getTarget());
		}
	}

	public boolean operatorTestDivision(Class<? extends Mixed> clazz) {
		return CPrimitive.class.isAssignableFrom(clazz);
	}

	public boolean operatorEquals(Mixed m) {
		return this.val().equals(m.val());
	}

	public boolean operatorTestEquals(Class<? extends Mixed> clazz) {
		return CPrimitive.class.isAssignableFrom(clazz);
	}

	public boolean operatorGreaterThan(Mixed m) {
		double lhs = this.castToDouble(getTarget());
		double rhs = m.primitive(Target.UNKNOWN).castToDouble(Target.UNKNOWN);
		return lhs > rhs;
	}

	public boolean operatorTestGreaterThan(Class<? extends Mixed> clazz) {
		return CPrimitive.class.isAssignableFrom(clazz);
	}

	public boolean operatorLessThan(Mixed m) {
		double lhs = this.castToDouble(getTarget());
		double rhs = m.primitive(Target.UNKNOWN).castToDouble(Target.UNKNOWN);
		return lhs < rhs;
	}

	public boolean operatorTestLessThan(Class<? extends Mixed> clazz) {
		return CPrimitive.class.isAssignableFrom(clazz);
	}

	public Mixed operatorMultiplication(Mixed m) {
		double lhs = this.castToDouble(getTarget());
		double rhs = m.primitive(Target.UNKNOWN).castToDouble(Target.UNKNOWN);
		double result = lhs * rhs;
		if((long)result == result){
			return new CInt((long)result, getTarget());
		} else {
			return new CDouble(result, getTarget());
		}
	}

	public boolean operatorTestMultiplication(Class<? extends Mixed> clazz) {
		return CPrimitive.class.isAssignableFrom(clazz);
	}

	public Mixed operatorSubtraction(Mixed m) {
		double lhs = this.castToDouble(getTarget());
		double rhs = m.primitive(Target.UNKNOWN).castToDouble(Target.UNKNOWN);
		double result = lhs - rhs;
		if((long)result == result){
			return new CInt((long)result, getTarget());
		} else {
			return new CDouble(result, getTarget());
		}
	}

	public boolean operatorTestSubtraction(Class<? extends Mixed> clazz) {
		return CPrimitive.class.isAssignableFrom(clazz);
	}
	
}
