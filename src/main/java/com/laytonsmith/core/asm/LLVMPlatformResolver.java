package com.laytonsmith.core.asm;

import com.laytonsmith.core.PlatformResolver;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 */
public class LLVMPlatformResolver implements PlatformResolver {

	@Override
	public String outputConstant(Mixed c, Environment env) {
		if(c instanceof CInt) {
			return c.val();
		} else if(c instanceof CString) {
			LLVMEnvironment e = env.getEnv(LLVMEnvironment.class);
			String output = e.getOrPutStringConstant(c.val());
			return "i8* @" + output;
		}
		throw new UnsupportedOperationException("Unsupported data type " + c.typeof().getName());
	}
}
