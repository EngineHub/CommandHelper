package com.laytonsmith.core.arguments;

import com.laytonsmith.core.constructs.CString;
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
	
	public <T extends Mixed> T get(String varName){
		if(values.containsKey(varName)){
			return (T)values.get(varName);
		} else {
			throw new Error("Values list does not contain this variable name: " + varName);
		}
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
