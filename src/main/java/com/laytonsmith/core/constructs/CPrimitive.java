package com.laytonsmith.core.constructs;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 * The superclass of all primitives in methodscript. Primitives all must
 * support implicit conversion from the three basic types, string, double, and 
 * integer, though they may throw exceptions if that conversion is not possible
 * at runtime. 
 * @author lsmith
 */
@typename("primitive")
public interface CPrimitive extends Mixed {
	
	CString castToCString();
	String castToString();
	CDouble castToCDouble();
	Double castToDouble();
	CInt castToCInt();
	Integer castToInt();
	
}
