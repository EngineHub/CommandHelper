package com.laytonsmith.core.functions.asm;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.asm.AsmCompiler;
import com.laytonsmith.core.asm.IRBuilder;
import com.laytonsmith.core.asm.IRCoercion;
import com.laytonsmith.core.asm.IRData;
import com.laytonsmith.core.asm.IRDataBuilder;
import com.laytonsmith.core.asm.LLVMArgumentValidation;
import com.laytonsmith.core.asm.LLVMEnvironment;
import com.laytonsmith.core.asm.LLVMFunction;
import com.laytonsmith.core.asm.LLVMVersion;
import com.laytonsmith.core.compiler.signature.FunctionSignatures;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;

/**
 *
 */
public class ControlFlow {

	@api(environments = LLVMEnvironment.class, platform = api.Platforms.COMPILER_LLVM)
	public static class ifelse extends LLVMFunction {

		@Override
		public IRData buildIR(IRBuilder builder, Target t, Environment env,
				GenericParameters generics, ParseTree... nodes)
				throws ConfigCompileException {
			LLVMEnvironment llvmenv = env.getEnv(LLVMEnvironment.class);
			IRBuilder.Gen gen = builder.generator(t, env);
			boolean hasElse = nodes.length % 2 == 1;
			int numPairs = nodes.length / 2;

			int[] condLabels = new int[numPairs];
			int[] codeLabels = new int[numPairs];
			int[] brCondSlots = new int[numPairs];
			int[] brCodeDoneSlots = new int[numPairs];
			int[] condBools = new int[numPairs];

			for(int i = 0; i < numPairs; i++) {
				// Condition block needs a label so the previous false branch can target it
				if(i > 0) {
					condLabels[i] = llvmenv.getGotoLabel();
					builder.appendLabel(t, condLabels[i]);
				}

				// Evaluate condition and coerce to i1
				IRData condData = LLVMArgumentValidation.getAny(builder, env, nodes[i * 2], t);
				condBools[i] = IRCoercion.toBool(builder, t, env, condData);
				brCondSlots[i] = builder.reserveLine(t);

				// Code block
				codeLabels[i] = llvmenv.getGotoLabel();
				builder.appendLabel(t, codeLabels[i]);
				llvmenv.pushVariableScope();
				AsmCompiler.getIR(builder, nodes[i * 2 + 1], env);
				llvmenv.popVariableScope();
				brCodeDoneSlots[i] = builder.reserveLine(t);
			}

			// Else block
			int lblElse = -1;
			int brElseDoneSlot = -1;
			if(hasElse) {
				lblElse = llvmenv.getGotoLabel();
				builder.appendLabel(t, lblElse);
				llvmenv.pushVariableScope();
				AsmCompiler.getIR(builder, nodes[nodes.length - 1], env);
				llvmenv.popVariableScope();
				brElseDoneSlot = builder.reserveLine(t);
			}

			// Merge block
			int lblMerge = llvmenv.getGotoLabel();
			builder.appendLabel(t, lblMerge);

			// Fill deferred branches
			for(int i = 0; i < numPairs; i++) {
				final int codeLabel = codeLabels[i];
				final int cond = condBools[i];
				final int falseLabel;
				if(i + 1 < numPairs) {
					falseLabel = condLabels[i + 1];
				} else if(hasElse) {
					falseLabel = lblElse;
				} else {
					falseLabel = lblMerge;
				}
				builder.fillReservedLine(brCondSlots[i],
						() -> gen.brCond(cond, codeLabel, falseLabel));
				builder.fillReservedLine(brCodeDoneSlots[i],
						() -> gen.br(lblMerge));
			}
			if(hasElse) {
				builder.fillReservedLine(brElseDoneSlot,
						() -> gen.br(lblMerge));
			}

			return IRDataBuilder.asVoid();
		}

		@Override
		public String getName() {
			return "ifelse";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return getDefaultFunction().thrown();
		}

		@Override
		public Version since() {
			return LLVMVersion.V0_0_1;
		}

		@Override
		public FunctionSignatures getSignatures() {
			return getDefaultFunction().getSignatures();
		}
	}

	@api(environments = LLVMEnvironment.class, platform = api.Platforms.COMPILER_LLVM)
	public static class _if extends LLVMFunction {

		@Override
		public IRData buildIR(IRBuilder builder, Target t, Environment env,
				GenericParameters generics, ParseTree... nodes)
				throws ConfigCompileException {
			return new ifelse().buildIR(builder, t, env, generics, nodes);
		}

		@Override
		public String getName() {
			return "if";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return getDefaultFunction().thrown();
		}

		@Override
		public Version since() {
			return LLVMVersion.V0_0_1;
		}

		@Override
		public FunctionSignatures getSignatures() {
			return getDefaultFunction().getSignatures();
		}
	}
}
