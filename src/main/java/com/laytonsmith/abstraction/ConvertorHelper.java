package com.laytonsmith.abstraction;

import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.functions.Exceptions;

/**
 * Some methods in Convertor can be abstract regardless of the server implementation,
 * and so can therefore be abstracted out. However, since Convertor is an interface,
 * a helper is here, and subclasses can just call these static methods directly.
 */
public class ConvertorHelper {

	public static MCColor GetColor(String colorName, Target t) throws Exceptions.FormatException {
		if(MCColor.STANDARD_COLORS.containsKey(colorName.toUpperCase())){
			 return MCColor.STANDARD_COLORS.get(colorName.toUpperCase());
		} else {
			throw new Exceptions.FormatException("Unknown color type: " + colorName, t);
		}
	}

}
