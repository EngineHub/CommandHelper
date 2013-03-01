package com.laytonsmith.core.natives;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.natives.interfaces.MObject;

/**
 *
 * @author lsmith
 */
@typename("Vector3D")
public class MVector3D extends MObject {
	
	public double x;
	public double y;
	public double z;

	@Override
	protected String alias(String field) {
		if("0".equals(field)){
			return "x";
		} else if("1".equals(field)){
			return "y";
		} else if("2".equals(field)){
			return "z";
		} else {
			return null;
		}
	}
	
	
}
