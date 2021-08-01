package com.laytonsmith.core.asm;

import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import java.util.Arrays;
import java.util.List;

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
		return emitIR(t, env, Arrays.asList(lines));
	}

	/**
	 * Given a set of lines of code, outputs them in the proper format.In almost all cases, this should be used
	 * in getIR, rather than directly returning a string.
	 * @param t The code target, used for debug output.
	 * @param env The environment, including the CompilerEnvironment.
	 * @param lines The lines of IR to output. They will be properly formatted and indented, with code target
	 * information in a comment if the environment so dictates.
	 * @return
	 */
	public static String emitIR(Target t, Environment env, List<String> lines) {
		LLVMEnvironment llvmenv = env.getEnv(LLVMEnvironment.class);
		StringBuilder b = new StringBuilder();
		for(String line : lines) {
			if("".equals(line.trim())) {
				continue;
			}
			b.append(formatLine(t, llvmenv, line, 0));
		}
		return b.toString();

	}

	/**
	 * Formats an individual line of IR.
	 * @param t The code target this line came from.
	 * @param llvmenv The LLVM Environment.
	 * @param line The IR
	 * @param commentTab The minimum padding for the comment. This should generally be the length of the longest line, to make things line up properly.
	 * @return
	 */
	public static String formatLine(Target t, LLVMEnvironment llvmenv, String line, int commentTab) {
		String s = "";
		if(!line.endsWith(":")) {
			// labels traditionally don't have indent
			s += "  ";
		}
		s += line;
		if(llvmenv.isOutputIRCodeTargetLoggingEnabled()) {
			if(s.length() < commentTab) {
				s += StringUtils.stringMultiply(commentTab - s.length(), " ");
			}
			s += " ; " + t.toString();
		}
		s += OSUtils.GetLineEnding();
		return s;
	}

	public static String getIRType(String irLine) {
		return irLine.split(" ")[0];
	}
}
