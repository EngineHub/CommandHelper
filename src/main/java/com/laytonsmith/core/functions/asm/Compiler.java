package com.laytonsmith.core.functions.asm;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.asm.AsmCompiler;
import com.laytonsmith.core.asm.IRBuilder;
import com.laytonsmith.core.asm.IRData;
import com.laytonsmith.core.asm.IRDataBuilder;
import com.laytonsmith.core.asm.LLVMEnvironment;
import com.laytonsmith.core.asm.LLVMFunction;
import com.laytonsmith.core.asm.LLVMVersion;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;

/**
 *
 */
public class Compiler {
	@api(environments = LLVMEnvironment.class, platform = api.Platforms.COMPILER_LLVM)
	public static class __statements__ extends LLVMFunction {

		@Override
		public IRData buildIR(IRBuilder builder, Target t, Environment env, ParseTree... nodes) throws ConfigCompileException {
			for(ParseTree node : nodes) {
				AsmCompiler.getIR(builder, node, env);
			}
			return IRDataBuilder.asVoid();
		}

		@Override
		public String getName() {
			return "__statements__";
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

	@api(environments = LLVMEnvironment.class, platform = api.Platforms.COMPILER_LLVM)
	public static class dyn extends LLVMFunction {

		@Override
		public IRData buildIR(IRBuilder builder, Target t, Environment env, ParseTree... nodes) throws ConfigCompileException {
			IRData data = AsmCompiler.getIR(builder, nodes[0], env);
			LLVMEnvironment llvmenv = env.getEnv(LLVMEnvironment.class);
			int alloca = llvmenv.getNewLocalVariableReference(); // returned value
			builder.appendLine(t, "%" + alloca + " = alloca " + data.getResultType().getIRType());
			builder.appendLine(t, "store " + data.getReference() + ", " + data.getResultType().getIRType() + "* %" + alloca);
			return IRDataBuilder.setReturnVariable(alloca, data.getResultType());
		}

		@Override
		public String getName() {
			return "dyn";
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
