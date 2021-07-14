package com.laytonsmith.core.asm;

import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;

/**
 *
 */
public final class AsmUtil {

	private AsmUtil(){}

	/**
	 * Given a set of lines of code, outputs them in the proper format.In almost all cases, this should be used
	 * in getIR, rather than directly returning a string.
	 * @param t The code target, used for debug output.
	 * @param env The environment, including the CompilerEnvironment.
	 * @param lines The lines of IR to output. They will be properly formatted and indented, with code target
	 * information in a comment if the environment so dictates.
	 * @return
	 */
	public static String emitIR(Target t, Environment env, String... lines) {
		LLVMEnvironment llvmenv = env.getEnv(LLVMEnvironment.class);
		StringBuilder b = new StringBuilder();
		for(String line : lines) {
			b.append("\t").append(line);
			if(llvmenv.isOutputIRCodeTargetLoggingEnabled()) {
				b.append(" ; ").append(t.toString());
			}
			b.append(OSUtils.GetLineEnding());
		}
		return b.toString();

	}
}
