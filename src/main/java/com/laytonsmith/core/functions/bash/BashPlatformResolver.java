package com.laytonsmith.core.functions.bash;

import com.laytonsmith.core.PlatformResolver;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;

/**
 *
 *
 */
public class BashPlatformResolver implements PlatformResolver {

	@Override
	public String outputConstant(Construct c) {
		if (c instanceof CString) {
			return "\"" + c.val() + "\"";
		} else if (c instanceof CArray) {
			throw new RuntimeException("Not implemented yet");
		} else {
			return c.val();
		}
	}

}
