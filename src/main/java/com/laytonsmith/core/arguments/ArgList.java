package com.laytonsmith.core.arguments;

import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author lsmith
 */
public class ArgList {

	private Map<String, Mixed> values;
	private int signatureId;
	/*package*/ ArgList(int signatureId, Map<String, Mixed> values) {
		this.signatureId = signatureId;
		this.values = values;
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
				return (T)values.get(varName);
			} catch(ClassCastException e){
				//This is actually a programming error
				throw new Error("Invalid cast", e);
			}
		} else {
			throw new Error("Values list does not contain this variable name: " + varName);
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
	 * Returns true if these arguments matched a particular signature id.
	 * @param id
	 * @return 
	 */
	public boolean isSignature(int id){
		return id == signatureId;
	}
	
	/**
	 * Returns the signature id that these arguments matched.
	 * @return 
	 */
	public int getSignatureId(){
		return signatureId;
	}
}
