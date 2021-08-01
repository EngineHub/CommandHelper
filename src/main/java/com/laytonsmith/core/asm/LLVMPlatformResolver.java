package com.laytonsmith.core.asm;

import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 */
public final class LLVMPlatformResolver {

	private LLVMPlatformResolver(){}


	public static IRData outputConstant(Mixed c, Environment env) {
		if(c == null) {
			throw new NullPointerException("Unexpected null value");
		}
		if(c instanceof CInt ci) {
			return IRDataBuilder.asConstant(IRType.INTEGER64, Long.toString(ci.getInt()));
		} else if(c instanceof CString) {
			LLVMEnvironment e = env.getEnv(LLVMEnvironment.class);
			String output = e.getOrPutStringConstant(c.val());
			return IRDataBuilder.asConstant(IRType.INTEGER8POINTER, "@" + output);
		}
		throw new UnsupportedOperationException("Unsupported data type " + c.typeof().getName());
	}
}
