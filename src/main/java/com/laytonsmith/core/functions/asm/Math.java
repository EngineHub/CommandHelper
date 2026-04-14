package com.laytonsmith.core.functions.asm;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ArgumentValidation;
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
		public void addStartupCode(IRBuilder builder, Environment startupEnv, Target t) {
			LLVMEnvironment startupllvmenv = startupEnv.getEnv(LLVMEnvironment.class);
			int callTime = startupllvmenv.getNewLocalVariableReference(IRType.INTEGER32);
			int rand1 = startupllvmenv.getNewLocalVariableReference(IRType.INTEGER32);
			startupllvmenv.addGlobalDeclaration(AsmCommonLibTemplates.SRAND, startupEnv);
			startupllvmenv.addGlobalDeclaration(AsmCommonLibTemplates.TIME, startupEnv);
			builder.appendLines(t,
					"%" + callTime + " = call i32 bitcast (i32 (...)* @time to i32 (i8*)*)(i8* null)",
					"call void @srand(i32 %" + callTime + ")",
					// burn one rand, so the second ("first" from the user perspective)
					// gets a bit of the bias out of the way
					"%" + rand1 + " = call i32 @rand()"
			);
		}

		@Override
		public IRData buildIR(IRBuilder builder, Target t, Environment env, ParseTree... nodes) throws ConfigCompileException {
			LLVMEnvironment llvmenv = env.getEnv(LLVMEnvironment.class);
			CompilerEnvironment cEnv = env.getEnv(CompilerEnvironment.class);
			llvmenv.addGlobalDeclaration(AsmCommonLibTemplates.RAND, env);

			String RAND_MAX = "2147483647"; // 2**31-1
			int callRand;
			String conversionInstruction;
			// callRand is the output of this block, for windows this is the output of the or
			if(cEnv.getTargetOS().isWindows()) {
				// On Windows, RAND_MAX is 2**15-1, and we just generally want much higher granularity than
				// that. In order to match POSIX's 2**31-1 range, we do rand() | rand() << 16, and use that
				// as our random number.
				int callRand1 = llvmenv.getNewLocalVariableReference(IRType.INTEGER32);
				int callRand2 = llvmenv.getNewLocalVariableReference(IRType.INTEGER32);
				int shl = llvmenv.getNewLocalVariableReference(IRType.INTEGER32);
				callRand = llvmenv.getNewLocalVariableReference(IRType.INTEGER32);
				builder.appendLine(t, "%" + callRand1 + " = call i32 @rand()");
				builder.appendLine(t, "%" + callRand2 + " = call i32 @rand()");
				builder.appendLine(t, "%" + shl + " = shl i32 %" + callRand2 + ", 16");
				builder.appendLine(t, "%" + callRand + " = or i32 %" + callRand1 + ", %" + shl);
				conversionInstruction = "uitofp";
			} else {
				callRand = llvmenv.getNewLocalVariableReference(IRType.INTEGER32);
				builder.appendLine(t, "%" + callRand + " = call i32 @rand()");
				conversionInstruction = "sitofp";
			}
			// First, generate our random number, scaled to 0-1

			int sitofp = llvmenv.getNewLocalVariableReference(IRType.INTEGER32);
			int fdiv = llvmenv.getNewLocalVariableReference(IRType.DOUBLE); // returned value for 0 arg

			builder.appendLine(t, "%" + sitofp + " = " + conversionInstruction + " i32 %" + callRand + " to double");
			builder.appendLine(t, "%" + fdiv + " = fdiv double %" + sitofp + ", " + RAND_MAX + ".0");

			if(nodes.length == 0) {
				return IRDataBuilder.setReturnVariable(fdiv, IRType.DOUBLE);
			} else {
				String min;
				String max;
				if(nodes[0].isConst()) {
					long vMax = ArgumentValidation.getInt(nodes[0].getData(), t);
					if(vMax > Integer.MAX_VALUE) {
						throw new ConfigCompileException("max and min must be below int max, defined as "
								+ Integer.MAX_VALUE, t);
					}
				} else {
					// TODO: Write out code to runtime check if the value is correct
				}
				if(nodes.length == 1) {
					int minReference = llvmenv.getNewLocalVariableReference(IRType.INTEGER32);
					int loadReference = llvmenv.getNewLocalVariableReference(IRType.INTEGER32);
					builder.generator(t, env).allocaStoreAndLoad(minReference, IRType.INTEGER32, "i32 0", loadReference);
					min = "%" + loadReference;
					IRData dmax = LLVMArgumentValidation.getInt32(builder, env, nodes[0], t);
					max = dmax.getReference();
				} else {
					IRData dmin = LLVMArgumentValidation.getInt32(builder, env, nodes[0], t);
					IRData dmax = LLVMArgumentValidation.getInt32(builder, env, nodes[1], t);
					int minReference = llvmenv.getNewLocalVariableReference(IRType.INTEGER32);
					int loadReference = llvmenv.getNewLocalVariableReference(IRType.INTEGER32);
					builder.generator(t, env).allocaStoreAndLoad(minReference, IRType.INTEGER32, dmin.getReference(), loadReference);
					min = "%" + loadReference;
					max = dmax.getReference();

					if(nodes[1].isConst()) {
						long vMax = ArgumentValidation.getInt(nodes[1].getData(), t);
						if(vMax > Integer.MAX_VALUE) {
							throw new ConfigCompileException("max and min must be below int max, defined as "
									+ Integer.MAX_VALUE, t);
						}
					} else {
						// TODO: Write out code to runtime check if the value is correct
					}
				}

//				int allocaRandScaled = llvmenv.getNewLocalVariableReference();
//				int allocaMin = llvmenv.getNewLocalVariableReference();
				int fmul = llvmenv.getNewLocalVariableReference(IRType.DOUBLE);
				int fptosi = llvmenv.getNewLocalVariableReference(IRType.DOUBLE);
				int range = llvmenv.getNewLocalVariableReference(IRType.INTEGER32);
				int srem = llvmenv.getNewLocalVariableReference(IRType.INTEGER32);
				int add = llvmenv.getNewLocalVariableReference(IRType.INTEGER32);

//				builder.generator(t, env).allocaAndStore(allocaMin, IRType.INTEGER32, min);

				// Scale the double into int max
				builder.appendLine(t, "%" + fmul + " = fmul double %" + fdiv + ", 2147483647.0");
				builder.appendLine(t, "%" + fptosi + " = fptosi double %" + fmul + " to i32");
				builder.appendLine(t, "%" + range + " = sub nsw " + max + ", " + min);
				builder.appendLine(t, "%" + srem + " = srem i32 %" + fptosi + ", %" + range);
				builder.appendLine(t, "%" + add + " = add nsw i32 %" + srem + ", " + min);
				return IRDataBuilder.setReturnVariable(add, IRType.INTEGER32);
			}
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
