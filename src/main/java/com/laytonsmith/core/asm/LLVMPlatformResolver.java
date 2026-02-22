package com.laytonsmith.core.asm;

import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 */
public final class LLVMPlatformResolver {

	private LLVMPlatformResolver() {
	}


	public static IRData outputConstant(IRBuilder builder, Mixed c, Environment env) {
		if(c == null) {
			throw new NullPointerException("Unexpected null value");
		}
		if(c instanceof CInt ci) {
			return IRDataBuilder.asConstant(IRType.INTEGER64, Long.toString(ci.getInt()));
		} else if(c instanceof CDouble cd) {
			// Get the raw bytes, and write those out as a hex string, to keep full precision.
			long bits = Double.doubleToRawLongBits(cd.getDouble());
			return IRDataBuilder.asConstant(IRType.DOUBLE, "0x" + Long.toHexString(bits));
		} else if(c instanceof CString) {
			LLVMEnvironment e = env.getEnv(LLVMEnvironment.class);
			// In this case, it's a [L x i8] array, and we need to convert it to a generic i8* array.
			e.addGlobalDeclaration(AsmCommonLibTemplates.LLVM_MEMCPY_P0I8_P0I8_I64, env);
			String output = e.getOrPutStringConstant(c.val());
			int length = c.val().length() + 1;
			int alloca = e.getNewLocalVariableReference(IRType.OTHER);
			int bitcast = e.getNewLocalVariableReference(IRType.OTHER);
			int gep = e.getNewLocalVariableReference(IRType.STRING);
			builder.appendLine(c.getTarget(), "%" + alloca + " = alloca [" + length + " x i8]");
			builder.appendLine(c.getTarget(), "%" + bitcast + " = bitcast [" + length + " x i8]* %" + alloca + " to i8*");
			builder.appendLine(c.getTarget(), "call void @llvm.memcpy.p0i8.p0i8.i64(i8* %" + bitcast + ", i8* getelementptr inbounds ([" + length + " x i8], [" + length + " x i8]* @" + output + ", i32 0, i32 0), i64 " + length + ", i1 false)");
			builder.appendLine(c.getTarget(), "%" + gep + " = getelementptr inbounds [" + length + " x i8], [" + length + " x i8]* %" + alloca + ", i64 0, i64 0");
			return IRDataBuilder.setReturnVariable(gep, IRType.STRING);
		}
		throw new UnsupportedOperationException("Unsupported data type " + c.typeof(env).getName());
	}
}
