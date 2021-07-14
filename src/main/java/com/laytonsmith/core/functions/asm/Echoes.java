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
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;

/**
 *
 */
public class Echoes {
	@api(environments = LLVMEnvironment.class, platform = api.Platforms.COMPILER_LLVM)
	public static class console extends LLVMFunction {

		@Override
		public String getIR(Target t, Environment env, Script parent, ParseTree... nodes) throws ConfigCompileException {
			OSUtils.OS os = env.getEnv(CompilerEnvironment.class).getTargetOS();
			String hardcodedOutput = null;
			if(nodes[0].getData() instanceof CString) {
				hardcodedOutput = nodes[0].getData().val();
			}
			LLVMEnvironment llvmenv = env.getEnv(LLVMEnvironment.class);
			if(hardcodedOutput != null) {
				String id = llvmenv.getOrPutStringConstant(hardcodedOutput);
				if(os.isWindows()) {
					llvmenv.addGlobalDeclaration("declare dso_local i32 @puts(i8*)");
					int strSize = hardcodedOutput.length() + 1;
					return AsmUtil.emitIR(t, env,
							"call i32 @puts(i8* getelementptr inbounds"
									+ " ([" + strSize + " x i8], [" + strSize + " x i8]* @" + id + ", i64 0, i64 0))"
					);
				} else {
					throw new ConfigCompileException("Unsupported target OS for system call \"puts\"", t);
				}
			} else {
				throw new ConfigCompileException("Only hardcoded strings are supported at this time.", t);
			}
		}

		@Override
		public String getName() {
			return "console";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
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
