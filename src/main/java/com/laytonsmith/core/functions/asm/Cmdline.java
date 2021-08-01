package com.laytonsmith.core.functions.asm;

import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.asm.AsmCommonLibTemplates;
import com.laytonsmith.core.asm.IRBuilder;
import com.laytonsmith.core.asm.IRData;
import com.laytonsmith.core.asm.IRDataBuilder;
import com.laytonsmith.core.asm.LLVMArgumentValidation;
import com.laytonsmith.core.asm.LLVMEnvironment;
import com.laytonsmith.core.asm.LLVMFunction;
import com.laytonsmith.core.asm.LLVMVersion;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Cmdline {

	@api(environments = LLVMEnvironment.class, platform = api.Platforms.COMPILER_LLVM)
	public static class exit extends LLVMFunction {

		@Override
		public IRData buildIR(IRBuilder builder, Target t, Environment env, ParseTree... nodes) throws ConfigCompileException {
			OSUtils.OS os = env.getEnv(CompilerEnvironment.class).getTargetOS();
			LLVMEnvironment llvmenv = env.getEnv(LLVMEnvironment.class);
			String code = "i32 0";
			List<String> lines = new ArrayList<>();
			if(nodes.length > 0) {
				IRData data = LLVMArgumentValidation.getInt32(builder, env, nodes[0], t);
				code = data.getReference();
			}
			if(os.isWindows()) {
				llvmenv.addGlobalDeclaration(AsmCommonLibTemplates.EXIT, env);
				lines.add("call void @exit(" + code + ")");
			} else {
				throw new ConfigCompileException("[WIP] Unsupported target OS for system call \"exit\"", t);
			}
			lines.add("unreachable");
			builder.appendLines(t, lines);
			return IRDataBuilder.asUnreachable();
		}

		@Override
		public String getName() {
			return "exit";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
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
