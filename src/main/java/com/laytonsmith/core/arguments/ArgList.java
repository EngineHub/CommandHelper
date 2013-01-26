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
	/*package*/ ArgList(Map<String, Mixed> values) {
		this.values = values;
	}
	
	public <T extends Mixed> T get(String varName){
		if(values.containsKey(varName)){
			return (T)values.get(varName);
		} else {
			throw new Error("Values list does not contain this variable name: " + varName);
		}
	}
	
}
