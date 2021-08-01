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
public class Echoes {
	@api(environments = LLVMEnvironment.class, platform = api.Platforms.COMPILER_LLVM)
	public static class console extends LLVMFunction {

		@Override
		public IRData buildIR(IRBuilder builder, Target t, Environment env, ParseTree... nodes) throws ConfigCompileException {
			OSUtils.OS os = env.getEnv(CompilerEnvironment.class).getTargetOS();
			LLVMEnvironment llvmenv = env.getEnv(LLVMEnvironment.class);
			List<String> lines = new ArrayList<>();
			IRData string = LLVMArgumentValidation.getString(builder, env, nodes[0], t);
			if(os.isWindows()) {
				llvmenv.addGlobalDeclaration(AsmCommonLibTemplates.PUTS, env);
				int ret = llvmenv.getNewLocalVariableReference(); // returned value
				lines.add("%" + ret + " = call i32 @puts(" + string.getReference() + ")");
				builder.appendLines(t, lines);
				// TODO: Use the return value, puts doesn't actually return void.
				return IRDataBuilder.asVoid();
			} else {
				throw new ConfigCompileException("Unsupported target OS for system call \"puts\"", t);
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
