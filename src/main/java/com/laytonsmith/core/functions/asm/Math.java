package com.laytonsmith.core.functions.asm;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.asm.AsmCommonLibTemplates;
import com.laytonsmith.core.asm.IRBuilder;
import com.laytonsmith.core.asm.IRData;
import com.laytonsmith.core.asm.IRDataBuilder;
import com.laytonsmith.core.asm.IRType;
import com.laytonsmith.core.asm.LLVMArgumentValidation;
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
public class Math {
	@api(environments = LLVMEnvironment.class, platform = api.Platforms.COMPILER_LLVM)
	public static class rand extends LLVMFunction {

		@Override
		public IRData buildIR(IRBuilder builder, Target t, Environment env, ParseTree... nodes) throws ConfigCompileException {
			LLVMEnvironment llvmenv = env.getEnv(LLVMEnvironment.class);
			llvmenv.addGlobalDeclaration(AsmCommonLibTemplates.RAND, env);
			// First, generate our random number, scaled to 0-1
			int allocaI = llvmenv.getNewLocalVariableReference();
			int allocaR = llvmenv.getNewLocalVariableReference();
			int rand = llvmenv.getNewLocalVariableReference();
			int loadRand = llvmenv.getNewLocalVariableReference();
			int icmpEq = llvmenv.getNewLocalVariableReference();

			int firstJump = llvmenv.getGotoLabel();
			int secondJump = llvmenv.getGotoLabel();

//			int loadI = llvmenv.getNewLocalVariableReference();
			int sdiv = llvmenv.getNewLocalVariableReference();
			int sitofp = llvmenv.getNewLocalVariableReference();

			int lastJump = llvmenv.getNewLocalVariableReference();

			int loadFinal = llvmenv.getNewLocalVariableReference();

			builder.appendLine(t, "%" + allocaI + " = alloca i32");
			builder.appendLine(t, "%" + allocaR + " = alloca double");
			builder.appendLine(t, "%" + rand + " = call i32 @rand()");
			builder.appendLine(t, "store i32 %" + rand + ", i32* %" + allocaI);
			builder.appendLine(t, "%" + loadRand + " = load i32, i32* %" + allocaI);
			builder.appendLine(t, "%" + icmpEq + " = icmp eq i32 %" + loadRand + ", 0");
			builder.appendLine(t, "br i1 %" + icmpEq + ", label %" + firstJump + ", label %" + secondJump);

			builder.appendLine(t, firstJump + ":");
			builder.appendLine(t, "store double 1.0e+00, double* %" + allocaR);
			builder.appendLine(t, "br label %" + lastJump);

			builder.appendLine(t, secondJump + ":");
			builder.appendLine(t, "%" + sdiv + " = sdiv i32 %" + rand + ", 32767"); // 32767 == RAND_MAX
			builder.appendLine(t, "%" + sitofp + " = sitofp i32 %" + sdiv + " to double");
			builder.appendLine(t, "store double %" + sitofp + ", double* %" + allocaR);
			builder.appendLine(t, "br label %" + lastJump);

			builder.appendLine(t, lastJump + ":");
			builder.appendLine(t, "%" + loadFinal + " = load double, double* %" + allocaR);

			if(nodes.length == 0) {
				return IRDataBuilder.setReturnVariable(loadFinal, IRType.DOUBLE);
			} else {
				String min;
				String max;
				if(nodes.length == 1) {
					min = "i64 0";
					IRData dmax = LLVMArgumentValidation.getInt64(builder, env, nodes[0], t);
					max = dmax.getReference();
				} else {
					IRData dmin = LLVMArgumentValidation.getInt64(builder, env, nodes[0], t);
					IRData dmax = LLVMArgumentValidation.getInt64(builder, env, nodes[1], t);
					min = dmax.getReference();
					max = dmax.getReference();
				}
			}
			throw new UnsupportedOperationException();
		}

		@Override
		public String getName() {
			return "rand";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1, 2};
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
