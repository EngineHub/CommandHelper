package com.laytonsmith.core.natives;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.natives.interfaces.MObject;

/**
 *
 * @author lsmith
 */
@typename("Color")
public class MColor extends MObject {
	
	public int red;
	
	public int green;
	
	public int blue;

	@Override
	protected String alias(String field) {
		if("r".equals(field) || "0".equals(field)){
			return "red";
		}
		if("g".equals(field) || "1".equals(field)){
			return "green";
		}
		if("b".equals(field) || "2".equals(field)){
			return "blue";
		}
		return null;
	}
	
	
}
