package com.laytonsmith.core.asm;

import com.laytonsmith.core.asm.IRBuilder.FCmpPredicate;
import com.laytonsmith.core.asm.IRBuilder.ICmpPredicate;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;

/**
 * Shared helpers for coercing IR values between types, used by multiple LLVM function implementations.
 */
public final class IRCoercion {

	private IRCoercion() {
	}

	/**
	 * Coerces any IR value to i1 (boolean truthiness). Dispatches to the compile-time path
	 * for known types, or the runtime path for ms_value.
	 */
	public static int toBool(IRBuilder builder, Target t, Environment env,
			IRData data) {
		if(data.getResultType() == IRType.MS_VALUE) {
			IRBuilder.Gen gen = builder.generator(t, env);
			int tag = gen.extractvalue(IRType.MS_VALUE, data.getResultVariable(),
					0, IRType.INTEGER8);
			int payload = gen.extractvalue(IRType.MS_VALUE, data.getResultVariable(),
					1, IRType.INTEGER64);
			return emitCoerceToBoolRuntime(builder, t, env, tag, payload);
		}
		return coerceToBoolean(builder, t, env, data);
	}

	/**
	 * Coerces a compile-time known value to i1 (boolean truthiness).
	 * Handles INTEGER1 (passthrough), STRING (strlen != 0), other integers (ne 0), and floats (une 0.0).
	 */
	public static int coerceToBoolean(IRBuilder builder, Target t, Environment env,
			IRData data) {
		IRBuilder.Gen gen = builder.generator(t, env);
		LLVMEnvironment llvmenv = env.getEnv(LLVMEnvironment.class);
		IRType type = data.getResultType();
		if(type == IRType.INTEGER1) {
			return data.getResultVariable();
		} else if(type == IRType.STRING) {
			llvmenv.addGlobalDeclaration(AsmCommonLibTemplates.STRLEN, env);
			int len = llvmenv.getNewLocalVariableReference(IRType.INTEGER64);
			builder.appendLine(t, "%" + len + " = call i64 @strlen("
					+ data.getReference() + ")");
			return gen.icmp(ICmpPredicate.NE, IRType.INTEGER64, len, "0");
		} else if(type.getCategory() == IRType.Category.INTEGER) {
			return gen.icmp(ICmpPredicate.NE, type, data.getResultVariable(), "0");
		} else if(type.getCategory() == IRType.Category.FLOAT) {
			return gen.fcmp(FCmpPredicate.UNE, type, data.getResultVariable(), "0.0");
		}
		throw new UnsupportedOperationException(
				"Cannot coerce " + type + " to boolean");
	}

	/**
	 * Coerces a compile-time known value to double.
	 * Handles DOUBLE (passthrough), other floats (fpext), and integers (sitofp).
	 */
	public static int coerceToDouble(IRBuilder builder, Target t, Environment env,
			IRData data) {
		IRBuilder.Gen gen = builder.generator(t, env);
		IRType type = data.getResultType();
		if(type == IRType.DOUBLE) {
			return data.getResultVariable();
		} else if(type.getCategory() == IRType.Category.FLOAT) {
			return gen.fpext(type, data.getResultVariable(), IRType.DOUBLE);
		} else if(type.getCategory() == IRType.Category.INTEGER) {
			return gen.sitofp(type, data.getResultVariable(), IRType.DOUBLE);
		}
		throw new UnsupportedOperationException(
				"Cannot coerce " + type + " to double");
	}

	/**
	 * Emits coercion of an ms_value payload to i1 (boolean truthiness) at runtime.
	 * For string types: strlen != 0 (requires a branch since strlen dereferences a pointer).
	 * For float types: bitcast to double, fcmp une 0.0.
	 * For int/bool types: icmp ne i64 payload, 0.
	 */
	public static int emitCoerceToBoolRuntime(IRBuilder builder, Target t,
			Environment env, int tagVar, int payloadVar) {
		IRBuilder.Gen gen = builder.generator(t, env);
		LLVMEnvironment llvmenv = env.getEnv(LLVMEnvironment.class);
		llvmenv.addGlobalDeclaration(AsmCommonLibTemplates.STRLEN, env);

		int resultAlloca = llvmenv.getNewLocalVariableReference(IRType.INTEGER1);
		gen.alloca(resultAlloca, IRType.INTEGER1);

		int isStr = gen.isStringTag(tagVar);
		int brStrSlot = builder.reserveLine(t);

		// --- String path: strlen != 0 ---
		int lblStr = llvmenv.getGotoLabel();
		builder.appendLabel(t, lblStr);
		int strPtr = gen.inttoptr(IRType.INTEGER64, payloadVar, IRType.STRING);
		int len = llvmenv.getNewLocalVariableReference(IRType.INTEGER64);
		builder.appendLine(t, "%" + len + " = call i64 @strlen(i8* %" + strPtr + ")");
		int truthStr = gen.icmp(ICmpPredicate.NE, IRType.INTEGER64, len, "0");
		gen.store(IRType.INTEGER1, truthStr, resultAlloca);
		int brStrDoneSlot = builder.reserveLine(t);

		// --- Non-string path: float vs int/bool (branchless) ---
		int lblNonStr = llvmenv.getGotoLabel();
		builder.fillReservedLine(brStrSlot,
				() -> gen.brCond(isStr, lblStr, lblNonStr));
		builder.appendLabel(t, lblNonStr);
		int isFloat = gen.isFloatTag(tagVar);
		int asDbl = gen.bitcast(IRType.INTEGER64, payloadVar, IRType.DOUBLE);
		int truthFloat = gen.fcmp(FCmpPredicate.UNE, IRType.DOUBLE, asDbl, "0.0");
		int truthInt = gen.icmp(ICmpPredicate.NE, IRType.INTEGER64, payloadVar, "0");
		int truthNonStr = gen.select(isFloat, IRType.INTEGER1,
				truthFloat, truthInt);
		gen.store(IRType.INTEGER1, truthNonStr, resultAlloca);
		int brNonStrDoneSlot = builder.reserveLine(t);

		// --- Merge ---
		int lblDone = llvmenv.getGotoLabel();
		builder.fillReservedLine(brStrDoneSlot, () -> gen.br(lblDone));
		builder.fillReservedLine(brNonStrDoneSlot, () -> gen.br(lblDone));
		builder.appendLabel(t, lblDone);
		int result = llvmenv.getNewLocalVariableReference(IRType.INTEGER1);
		gen.load(result, IRType.INTEGER1, resultAlloca);
		return result;
	}

	/**
	 * Emits branchless coercion of an ms_value payload to double at runtime.
	 * For float types: bitcast i64 to double.
	 * For int/bool types: sitofp i64 to double.
	 */
	public static int emitCoerceToDoubleRuntime(IRBuilder builder, Target t,
			Environment env, int tagVar, int payloadVar) {
		IRBuilder.Gen gen = builder.generator(t, env);
		int isFloat = gen.isFloatTag(tagVar);
		int asDbl = gen.bitcast(IRType.INTEGER64, payloadVar, IRType.DOUBLE);
		int asInt = gen.sitofp(IRType.INTEGER64, payloadVar, IRType.DOUBLE);
		return gen.select(isFloat, IRType.DOUBLE, asDbl, asInt);
	}
}
