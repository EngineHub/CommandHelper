package com.laytonsmith.core.functions.asm;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.asm.AsmCommonLibTemplates;
import com.laytonsmith.core.asm.IRBuilder;
import com.laytonsmith.core.asm.IRBuilder.FCmpPredicate;
import com.laytonsmith.core.asm.IRBuilder.ICmpPredicate;
import com.laytonsmith.core.asm.IRCoercion;
import com.laytonsmith.core.asm.IRData;
import com.laytonsmith.core.asm.IRDataBuilder;
import com.laytonsmith.core.asm.IRType;
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
public class BasicLogic {

	/**
	 * Comparison strategy for compile-time known types. Matches the interpreter's priority:
	 * boolean first, then string, then numeric.
	 */
	private enum ComparisonStrategy {
		BOOLEAN,
		NUMERIC,
		STRING
	}

	/**
	 * Determines the comparison strategy based on compile-time known types. If any arg is
	 * boolean, all are compared as booleans. If all are strings, compared as strings. Otherwise
	 * compared as doubles (numeric).
	 */
	private static ComparisonStrategy determineStrategy(IRData[] args) {
		boolean anyBoolean = false;
		boolean anyString = false;
		boolean anyNumeric = false;

		for(IRData arg : args) {
			IRType type = arg.getResultType();
			if(type == IRType.INTEGER1) {
				anyBoolean = true;
			} else if(type == IRType.STRING) {
				anyString = true;
			} else if(type.getCategory() == IRType.Category.INTEGER
					|| type.getCategory() == IRType.Category.FLOAT) {
				anyNumeric = true;
			} else {
				throw new UnsupportedOperationException(
						"Unsupported type in comparison: " + type);
			}
		}

		if(anyBoolean) {
			return ComparisonStrategy.BOOLEAN;
		}
		if(anyString) {
			if(anyNumeric) {
				throw new UnsupportedOperationException(
						"String + numeric comparison is not yet implemented");
			}
			return ComparisonStrategy.STRING;
		}
		return ComparisonStrategy.NUMERIC;
	}

	/**
	 * Emits a pairwise comparison for compile-time known types.
	 */
	private static int emitPairComparison(IRBuilder builder, Target t, Environment env,
			ComparisonStrategy strategy, IRData left, IRData right) {
		IRBuilder.Gen gen = builder.generator(t, env);
		LLVMEnvironment llvmenv = env.getEnv(LLVMEnvironment.class);
		switch(strategy) {
			case BOOLEAN: {
				int l = IRCoercion.coerceToBoolean(builder, t, env, left);
				int r = IRCoercion.coerceToBoolean(builder, t, env, right);
				return gen.icmp(ICmpPredicate.EQ, IRType.INTEGER1, l, r);
			}
			case NUMERIC: {
				int l = IRCoercion.coerceToDouble(builder, t, env, left);
				int r = IRCoercion.coerceToDouble(builder, t, env, right);
				return gen.fcmp(FCmpPredicate.OEQ, IRType.DOUBLE, l, r);
			}
			case STRING: {
				llvmenv.addGlobalDeclaration(AsmCommonLibTemplates.STRCMP, env);
				int cmp = llvmenv.getNewLocalVariableReference(IRType.INTEGER32);
				builder.appendLine(t, "%" + cmp + " = call i32 @strcmp("
						+ left.getReference() + ", "
						+ right.getReference() + ")");
				return gen.icmp(ICmpPredicate.EQ, IRType.INTEGER32, cmp, "0");
			}
			default:
				throw new UnsupportedOperationException(
						"Unknown strategy: " + strategy);
		}
	}

	// ---- ms_value runtime dispatch helpers ----

	/**
	 * Chains an array of i1 values with AND, returning the final result variable.
	 */
	private static int andChain(IRBuilder.Gen gen, int[] vals) {
		int result = vals[0];
		for(int i = 1; i < vals.length; i++) {
			result = gen.and(IRType.INTEGER1, result, vals[i]);
		}
		return result;
	}

	/**
	 * Chains an array of i1 values with OR, returning the final result variable.
	 */
	private static int orChain(IRBuilder.Gen gen, int[] vals) {
		int result = vals[0];
		for(int i = 1; i < vals.length; i++) {
			result = gen.or(IRType.INTEGER1, result, vals[i]);
		}
		return result;
	}

	/**
	 * Emits full runtime dispatch for comparing ms_value arguments.
	 * Follows the interpreter's priority: null > boolean > string > numeric.
	 */
	private static IRData emitMsValueEquals(IRBuilder builder, Target t,
			Environment env, IRData[] args) {
		IRBuilder.Gen gen = builder.generator(t, env);
		LLVMEnvironment llvmenv = env.getEnv(LLVMEnvironment.class);
		llvmenv.addGlobalDeclaration(AsmCommonLibTemplates.STRCMP, env);
		int n = args.length;

		int resultAlloca = llvmenv.getNewLocalVariableReference(IRType.INTEGER1);
		gen.alloca(resultAlloca, IRType.INTEGER1);

		java.util.List<Integer> doneSlots = new java.util.ArrayList<>();

		// Extract tags and payloads for all args
		int[] tags = new int[n];
		int[] payloads = new int[n];
		for(int i = 0; i < n; i++) {
			tags[i] = gen.extractvalue(IRType.MS_VALUE,
					args[i].getResultVariable(), 0, IRType.INTEGER8);
			payloads[i] = gen.extractvalue(IRType.MS_VALUE,
					args[i].getResultVariable(), 1, IRType.INTEGER64);
		}

		// Check if any tag is null (tag == 10)
		int[] isNull = new int[n];
		for(int i = 0; i < n; i++) {
			isNull[i] = gen.isNullTag(tags[i]);
		}
		int anyNull = orChain(gen, isNull);

		int brNullSlot = builder.reserveLine(t);

		// --- Null path: equal iff ALL are null ---
		int lblNullResult = llvmenv.getGotoLabel();
		builder.appendLabel(t, lblNullResult);
		int allNull = andChain(gen, isNull);
		gen.store(IRType.INTEGER1, allNull, resultAlloca);
		doneSlots.add(builder.reserveLine(t));

		int lblNotNull = llvmenv.getGotoLabel();
		builder.fillReservedLine(brNullSlot,
				() -> gen.brCond(anyNull, lblNullResult, lblNotNull));

		// --- Not null: check for booleans ---
		builder.appendLabel(t, lblNotNull);
		int[] isBool = new int[n];
		for(int i = 0; i < n; i++) {
			isBool[i] = gen.isBoolTag(tags[i]);
		}
		int anyBool = orChain(gen, isBool);

		int brBoolSlot = builder.reserveLine(t);

		// --- Boolean path: coerce all to bool, compare pairwise ---
		int lblBoolCmp = llvmenv.getGotoLabel();
		builder.appendLabel(t, lblBoolCmp);
		int[] boolVals = new int[n];
		for(int i = 0; i < n; i++) {
			boolVals[i] = IRCoercion.emitCoerceToBoolRuntime(builder, t, env,
					tags[i], payloads[i]);
		}
		int[] boolPairs = new int[n - 1];
		for(int i = 0; i < n - 1; i++) {
			boolPairs[i] = gen.icmp(ICmpPredicate.EQ, IRType.INTEGER1,
					boolVals[i], boolVals[i + 1]);
		}
		int boolResult = andChain(gen, boolPairs);
		gen.store(IRType.INTEGER1, boolResult, resultAlloca);
		doneSlots.add(builder.reserveLine(t));

		int lblStrCheck = llvmenv.getGotoLabel();
		builder.fillReservedLine(brBoolSlot,
				() -> gen.brCond(anyBool, lblBoolCmp, lblStrCheck));

		// --- String check: any string tags? ---
		builder.appendLabel(t, lblStrCheck);
		int[] isStr = new int[n];
		for(int i = 0; i < n; i++) {
			isStr[i] = gen.isStringTag(tags[i]);
		}
		int anyStr = orChain(gen, isStr);

		int brStrSlot = builder.reserveLine(t);

		// --- String dispatch: verify ALL are strings ---
		int lblStrDispatch = llvmenv.getGotoLabel();
		builder.appendLabel(t, lblStrDispatch);
		int allStr = andChain(gen, isStr);

		int brAllStrSlot = builder.reserveLine(t);

		// --- String compare: strcmp each pair ---
		int lblStrCmp = llvmenv.getGotoLabel();
		builder.appendLabel(t, lblStrCmp);
		int[] strPairs = new int[n - 1];
		for(int i = 0; i < n - 1; i++) {
			int ptrA = gen.inttoptr(IRType.INTEGER64, payloads[i],
					IRType.STRING);
			int ptrB = gen.inttoptr(IRType.INTEGER64, payloads[i + 1],
					IRType.STRING);
			int cmp = llvmenv.getNewLocalVariableReference(IRType.INTEGER32);
			builder.appendLine(t, "%" + cmp + " = call i32 @strcmp(i8* %"
					+ ptrA + ", i8* %" + ptrB + ")");
			strPairs[i] = gen.icmp(ICmpPredicate.EQ, IRType.INTEGER32, cmp, "0");
		}
		int strResult = andChain(gen, strPairs);
		gen.store(IRType.INTEGER1, strResult, resultAlloca);
		doneSlots.add(builder.reserveLine(t));

		// --- String mismatch: mixed string + other at runtime, return false ---
		int lblStrMismatch = llvmenv.getGotoLabel();
		builder.fillReservedLine(brAllStrSlot,
				() -> gen.brCond(allStr, lblStrCmp, lblStrMismatch));

		builder.appendLabel(t, lblStrMismatch);
		gen.store(IRType.INTEGER1, "i1 0", resultAlloca);
		doneSlots.add(builder.reserveLine(t));

		int lblNumCmp = llvmenv.getGotoLabel();
		builder.fillReservedLine(brStrSlot,
				() -> gen.brCond(anyStr, lblStrDispatch, lblNumCmp));

		// --- Numeric path: coerce all to double, compare pairwise ---
		builder.appendLabel(t, lblNumCmp);
		int[] dblVals = new int[n];
		for(int i = 0; i < n; i++) {
			dblVals[i] = IRCoercion.emitCoerceToDoubleRuntime(builder, t, env,
					tags[i], payloads[i]);
		}
		int[] numPairs = new int[n - 1];
		for(int i = 0; i < n - 1; i++) {
			numPairs[i] = gen.fcmp(FCmpPredicate.OEQ, IRType.DOUBLE,
					dblVals[i], dblVals[i + 1]);
		}
		int numResult = andChain(gen, numPairs);
		gen.store(IRType.INTEGER1, numResult, resultAlloca);
		doneSlots.add(builder.reserveLine(t));

		// --- Merge: fill all deferred branches and load result ---
		int lblDone = llvmenv.getGotoLabel();
		for(int slot : doneSlots) {
			builder.fillReservedLine(slot, () -> gen.br(lblDone));
		}
		builder.appendLabel(t, lblDone);
		int result = llvmenv.getNewLocalVariableReference(IRType.INTEGER1);
		gen.load(result, IRType.INTEGER1, resultAlloca);

		return IRDataBuilder.setReturnVariable(result, IRType.INTEGER1);
	}

	@api(environments = LLVMEnvironment.class, platform = api.Platforms.COMPILER_LLVM)
	public static class equals extends LLVMFunction {

		@Override
		public IRData buildIR(IRBuilder builder, Target t, Environment env,
				GenericParameters generics, ParseTree... nodes)
				throws ConfigCompileException {
			if(nodes.length < 2) {
				throw new ConfigCompileException(
						"At least two arguments must be passed to equals", t);
			}

			// Evaluate all arguments, preserving their native types
			IRData[] args = new IRData[nodes.length];
			for(int i = 0; i < nodes.length; i++) {
				args[i] = LLVMArgumentValidation.getAny(
						builder, env, nodes[i], t);
			}

			// Check if any arg is ms_value (runtime-typed)
			boolean anyMsValue = false;
			for(IRData arg : args) {
				if(arg.getResultType() == IRType.MS_VALUE
						|| arg.getResultType() == IRType.MS_NULL) {
					anyMsValue = true;
					break;
				}
			}

			if(anyMsValue) {
				// Box all concrete args to ms_value for uniform dispatch
				for(int i = 0; i < args.length; i++) {
					args[i] = LLVMArgumentValidation.boxToMsValue(
							builder, t, env, args[i]);
				}
				return emitMsValueEquals(builder, t, env, args);
			}

			// All compile-time known types - determine strategy statically
			IRBuilder.Gen gen = builder.generator(t, env);
			ComparisonStrategy strategy = determineStrategy(args);
			int finalResult = -1;
			for(int i = 0; i < args.length - 1; i++) {
				int pairResult = emitPairComparison(builder, t, env,
						strategy, args[i], args[i + 1]);
				if(finalResult == -1) {
					finalResult = pairResult;
				} else {
					finalResult = gen.and(IRType.INTEGER1,
							finalResult, pairResult);
				}
			}

			return IRDataBuilder.setReturnVariable(finalResult,
					IRType.INTEGER1);
		}

		@Override
		public String getName() {
			return "equals";
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
}
