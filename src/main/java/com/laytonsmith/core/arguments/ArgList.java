package com.laytonsmith.core.arguments;

import com.laytonsmith.PureUtilities.ReflectionUtils;
import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CNumber;
import com.laytonsmith.core.constructs.CPrimitive;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.functions.Exceptions.CastException;
import com.laytonsmith.core.natives.interfaces.MObject;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author lsmith
 */
public class ArgList {

	private Map<String, Mixed> values;
	private Signature signature;
	/*package*/ ArgList(Map<String, Mixed> values, Signature signature) {
		this.values = values;
		this.signature = signature;
	}
	
	/**
	 * Returns the underlying value, as a construct
	 * @param <T>
	 * @param varName
	 * @return 
	 */
	public <T extends Mixed> T get(String varName){
		if(values.containsKey(varName)){
			try{
				Mixed m = (T)values.get(varName);
				return (T)m;
			} catch(ClassCastException e){
				//This is actually a programming error, despite first glance.
				//When the ArgList is returned, the parameters have already
				//been parsed to ensure they are the correct type. If they were
				//an incorrect type, they would have already caused a CRE
				//to be thrown, so this cast failing is because the ArgumentBuilder
				//specified some type, and the programmer is attempting to cast
				//it to something else. This just means that the programmer needs
				//to check their assignment vs builder types
				throw new Error("Invalid cast. Check to make sure that the assignment"
						+ " you made is valid, compared to the arguments passed to the"
						+ " argument builder.", e);
			}
		} else {
			throw new Error("Values list does not contain this variable name: " + varName);
		}
	}
	
	/**
	 * Returns the underlying value, as a construct. The value will have been
	 * type checked and verified that if numeric, is within the specified range.
	 * @param <T>
	 * @param varName
	 * @param t
	 * @return 
	 */
	public <T extends CNumber> T getRanged(String varName, Target t){
		Mixed m = get(varName);
		Argument a = signature.getArgument(varName);
		if(!a.isRanged()){
			throw new Error("Using getRanged on a non-ranged value");
		}
		if(m instanceof CNumber){
			double actual = m.primitive(t).castToDouble(t);
			if(actual >= a.getMin() && actual < a.getMax()){
				return (T)m;
			} else {
				throw new Exceptions.RangeException("Expecting a value between (" + a.getMin() + ", " + a.getMax() + "], but " 
						+ actual + " was found.", t);
			}
		} else {
			throw new CastException("Expecting a numeric value, but " + m.typeName() + " was found instead.", t);
		}
	}
	
	private Construct getConstruct(String varName){
		Mixed m = get(varName);
		try{
			return (Construct)m;
		} catch(ClassCastException e){
			throw new Error(e);
		}
	}
	
	/**
	 * Returns a Long, or null if the underlying value is null.
	 * @param varName
	 * @param t
	 * @return 
	 */
	public Long getLongWithNull(String varName, Target t){
		CPrimitive p = getConstruct(varName).primitive(t);
		if(p.isNull()){
			return null;
		} else {
			return p.castToInt(t);
		}
	}
	
	/**
	 * Returns an Integer, or null if the underlying value is null.
	 * @param varName
	 * @param t
	 * @return 
	 */
	public Integer getIntegerWithNull(String varName, Target t){
		CPrimitive p = getConstruct(varName).primitive(t);
		if(p.isNull()){
			return null;
		} else {
			return p.castToInt32(t);
		}
	}
	
	/**
	 * Returns a Double, or null if the underlying value is null.
	 * @param varName
	 * @param t
	 * @return 
	 */
	public Double getDoubleWithNull(String varName, Target t){
		CPrimitive p = getConstruct(varName).primitive(t);
		if(p.isNull()){
			return null;
		} else {
			return p.castToDouble(t);
		}
	}
	
	/**
	 * Returns a Float, or null if the underlying value is null.
	 * @param varName
	 * @param t
	 * @return 
	 */
	public Float getFloatWithNull(String varName, Target t){
		CPrimitive p = getConstruct(varName).primitive(t);
		if(p.isNull()){
			return null;
		} else {
			return p.castToDouble32(t);
		}
	}
	
	/**
	 * Shorthand to getting a POJO long from a CInt
	 * @param varName
	 * @return 
	 */
	public long getLong(String varName, Target t){
		return getConstruct(varName).primitive(t).castToInt(t);
	}
	
	/**
	 * Shorthand to getting a POJO int from a CInt
	 * @param varName
	 * @return 
	 */
	public int getInt(String varName, Target t){
		return getConstruct(varName).primitive(t).castToInt32(t);
	}
	
	/**
	 * Shorthand to getting a POJO short from a CInt
	 * @param varName
	 * @return 
	 */
	public short getShort(String varName, Target t){
		return getConstruct(varName).primitive(t).castToInt16(t);
	}
	
	/**
	 * Shorthand to getting a POJO byte from a CInt
	 * @param varName
	 * @param t
	 * @return 
	 */
	public byte getByte(String varName, Target t){
		return getConstruct(varName).primitive(t).castToInt8(t);
	}
	
	/**
	 * Shorthand to getting a POJO double from a CDouble
	 * @param varName
	 * @param t
	 * @return 
	 */
	public double getDouble(String varName, Target t){
		return getConstruct(varName).primitive(t).castToDouble(t);
	}
	
	/**
	 * Shorthand to getting a POJO float from a CDouble
	 * @param varName
	 * @param t
	 * @return 
	 */
	public float getFloat(String varName, Target t){
		return getConstruct(varName).primitive(t).castToDouble32(t);
	}
	
	/**
	 * Shorthand to getting a POJO boolean from a primitive.
	 * @param varName
	 * @param t
	 * @return 
	 */
	public boolean getBoolean(String varName, Target t){
		return getConstruct(varName).primitive(t).castToBoolean();
	}
	
	/**
	 * Returns the toString'd value. A null check is done first,
	 * and will throw an exception if the underlying value is null.
	 * @param varName
	 * @param t
	 * @return 
	 */
	public String getString(String varName, Target t){
		CPrimitive p = getConstruct(varName).primitive(t);
		if(p.isNull()){
			throw new ConfigRuntimeException("Unexpected null value for " + varName, Exceptions.ExceptionType.NullPointerException, t);
		} else {
			return p.castToString();
		}
	}
	
	/**
	 * Returns the toString'd value, but will return a POJO null.
	 * @param varName
	 * @param t
	 * @return 
	 */
	public String getStringWithNull(String varName, Target t){
		CPrimitive p = getConstruct(varName).primitive(t);
		if(p.isNull()){
			return null;
		} else {
			return p.castToString();
		}
	}
	
	/**
	 * Returns an enum of the appropriate type. This is required for
	 * use if returning an enum, the normal get() method will not work.
	 * @param <T>
	 * @param name
	 * @param t
	 * @return 
	 */
	public <T extends Enum<?>> T getEnum(String name, Target t){
		String s = getConstruct(name).val();
		Argument a = null;
		for(Argument aa : signature.getArguments()){
			if(aa.getName().equals(name)){
				a = aa;
				break;
			}
		}
		if(a == null){
			throw new Error("Should not have gotten here, please alert a developer with this stack trace.");
		}
		Class clazz = a.getEnumClass();
		try{
			return (T)Enum.valueOf(clazz, s.toUpperCase());
		} catch(IllegalArgumentException e){
			try{
				return (T)Enum.valueOf(clazz, s); //Ok, try actual case.
			} catch(IllegalArgumentException ex){
				if(Prefs.DebugMode()){
					//We have to use reflection to get these, since the "values"
					//method is static. 
					Object v = ReflectionUtils.invokeMethod(clazz, null, "values");
					List<String> list = new ArrayList<String>();
					for(Enum ee : ReflectionUtils.values(clazz)){
						list.add(ee.name());
					}
					throw new CastException("Unexpected value \"" + name + "\" passed as \"" 
							+ a.getName() + "\", but should be one of " + StringUtils.Join(list, ", ", " or ", " or "), t);
				} else {
					throw new CastException("Unexpected value \"" + name + "\" passed as \"" + a.getName() + "\". Enable debug mode for all possible values,"
							+ " or check the documentation.", t);
				}
			}
		}
	}
	
	/**
	 * Returns true if these arguments matched a particular signature id.
	 * @param id
	 * @return 
	 */
	public boolean isSignature(int id){
		return id == signature.getSignatureId();
	}
	
	/**
	 * Returns the signature id that these arguments matched.
	 * @return 
	 */
	public int getSignatureId(){
		return signature.getSignatureId();
	}
}
