package com.laytonsmith.core.functions.asm;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.asm.LLVMEnvironment;
import com.laytonsmith.core.asm.LLVMFunction;
import com.laytonsmith.core.asm.LLVMVersion;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;

/**
 *
 */
public class Cmdline {

	@api(environments = LLVMEnvironment.class, platform = api.Platforms.COMPILER_LLVM)
	public static class exit extends LLVMFunction {

		@Override
		public String getIR(Target t, Environment env, Script parent, ParseTree... nodes) {
			return "br exit ;" + t.toString() + "\n";
		}

		@Override
		public String getName() {
			return "exit";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
		}

		@Override
		public Version since() {
			return LLVMVersion.V0_0_1;
		}

	}
}
