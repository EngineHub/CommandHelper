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
import com.laytonsmith.core.compiler.CompilerEnvironment;
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
			CompilerEnvironment cEnv = env.getEnv(CompilerEnvironment.class);
			llvmenv.addGlobalDeclaration(AsmCommonLibTemplates.RAND, env);

			builder.appendStartupCode(this, (startupEnv) -> {
				LLVMEnvironment startupllvmenv = startupEnv.getEnv(LLVMEnvironment.class);
				int callTime = startupllvmenv.getNewLocalVariableReference();
				int rand1 = startupllvmenv.getNewLocalVariableReference();
				startupllvmenv.addGlobalDeclaration(AsmCommonLibTemplates.SRAND, env);
				startupllvmenv.addGlobalDeclaration(AsmCommonLibTemplates.TIME, env);
				String[] lines = new String[] {
					"%" + callTime + " = call i32 bitcast (i32 (...)* @time to i32 (i8*)*)(i8* null)",
					"call void @srand(i32 %" + callTime + ")",
					// burn one rand, so the second ("first" from the user perspective)
					// gets a bit of the bias out of the way
					"%" + rand1 + " = call i32 @rand()"
				};
				return lines;
			});

			// TODO: There has to be a better way to get this, but it seems like this is only defined in a C header
			// file, which is not particularly ideal to use. However, given that it is a header file, it should be
			// in plaintext on the system, and can be pretty straightforwardly parsed out.
			String RAND_MAX = "32767"; // Windows
			if(cEnv.getTargetOS().isLinux()) {
				RAND_MAX = "2147483647";
			}
			// First, generate our random number, scaled to 0-1

			int allocaD = llvmenv.getNewLocalVariableReference();
			int callRand = llvmenv.getNewLocalVariableReference();
			int sitofp = llvmenv.getNewLocalVariableReference();
			int fdiv = llvmenv.getNewLocalVariableReference(); // returned value

			builder.generator().alloca(allocaD, IRType.DOUBLE, t);
			builder.appendLine(t, "%" + callRand + " = call i32 @rand()");
			builder.appendLine(t, "%" + sitofp + " = sitofp i32 %" + callRand + " to double");
			builder.appendLine(t, "%" + fdiv + " = fdiv double %" + sitofp + ", " + RAND_MAX + ".0");
			builder.appendLine(t, "store double %" + fdiv + ", double* %" + allocaD);

			if(nodes.length == 0) {
				return IRDataBuilder.setReturnVariable(fdiv, IRType.DOUBLE);
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
