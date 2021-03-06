package com.laytonsmith.core.functions.asm;

import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.asm.AsmUtil;
import com.laytonsmith.core.asm.LLVMEnvironment;
import com.laytonsmith.core.asm.LLVMFunction;
import com.laytonsmith.core.asm.LLVMVersion;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;

/**
 *
 */
public class Cmdline {

	@api(environments = LLVMEnvironment.class, platform = api.Platforms.COMPILER_LLVM)
	public static class exit extends LLVMFunction {

		@Override
		public String getIR(Target t, Environment env, Script parent, ParseTree... nodes) throws ConfigCompileException {
			OSUtils.OS os = env.getEnv(CompilerEnvironment.class).getTargetOS();
			String syscallNumber;
			LLVMEnvironment llvmenv = env.getEnv(LLVMEnvironment.class);
			if(os.isWindows()) {
				llvmenv.addGlobalDeclaration("declare dso_local void @_exit(i32)");
				return AsmUtil.emitIR(t, env,
						"%1 = alloca i32, align 4",
						"store i32 0, i32* %1, align 4",
						"call void @_exit(i32 0)",
						"unreachable"
				);
			} else if(os.isMac()) {
				syscallNumber = "0x2000001";
			} else if(os.isLinux()) {
				syscallNumber = "60";
			} else {
				throw new ConfigCompileException("Unsupported target OS for system call \"exit\"", t);
			}
			return AsmUtil.emitIR(t, env,
					"%rax = add i64 " + syscallNumber + ", 0",
					"call i64 asm sideeffect \"syscall\", \"=r,{rax},{rdi},{rsi},{rdx}\" (i64 %raxArg, i64 %rdiArg, i64 %rsiArg, i64 %rdxArg)"
			);
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
