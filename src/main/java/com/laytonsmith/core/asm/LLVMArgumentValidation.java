package com.laytonsmith.core.asm;

import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.asm.IRType.Category;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public final class LLVMArgumentValidation {
	private LLVMArgumentValidation() {}

	private static IRData handleFunction(Target t, IRType expectedType, IRBuilder builder, Environment env, ParseTree c) throws ConfigCompileException {
		IRData data = AsmCompiler.getIR(builder, c, env);
		return convert(t, expectedType, builder, env, data);
	}

	private static IRData convert(Target t, IRType expectedType, IRBuilder builder, Environment env, IRData data) throws ConfigCompileException {
		OSUtils.OS os = env.getEnv(CompilerEnvironment.class).getTargetOS();
		if(data.getResultType() == expectedType) {
			// If the thing returned the expected type, just return it as is, no need to wrap it.
			return data;
		}
		// We need to convert it. It might be easy (i.e. upcasting/downcasting) or it might be hard
		//(unknown/auto type/cross cast type), so let's rule out and handle the easy cases first.
		LLVMEnvironment llvmenv = env.getEnv(LLVMEnvironment.class);
		if(expectedType.getCategory() == IRType.Category.INTEGER && data.getResultType().getCategory() == IRType.Category.INTEGER) {
			// Up/downcast, easy
			int load = llvmenv.getNewLocalVariableReference(data.getResultType());
			builder.appendLine(t, "%" + load + " = load " + data.getResultType().getIRType()
					+ ", " + data.getResultType().getIRType() + "* %" + data.getResultVariable());
			int ret = llvmenv.getNewLocalVariableReference(data.getResultType()); // returned value
			if(expectedType.getBitDepth() >= data.getResultType().getBitDepth()) {
				// upcast
				builder.appendLine(t, "%" + ret + " = sext " + data.getResultType().getIRType() + " %" + load + " to " + expectedType.getIRType());
			} else {
				// downcast
				builder.appendLine(t, "%" + ret + " = trunc " + data.getResultType().getIRType() + " %" + load + " to " + expectedType.getIRType());
			}
			return IRDataBuilder.setReturnVariable(ret, expectedType);
		}
		if(data.getResultType() == IRType.MS_VALUE) {
			// Unbox ms_value to the expected concrete type
			IRData unboxed = emitUnbox(builder, t, env, expectedType, data.getResultVariable());
			return unboxed;
		}
		if(expectedType == IRType.MS_VALUE) {
			// Box the concrete type into an ms_value
			return emitBox(builder, t, env, data.getResultType(), data.getReference());
		}
		if(expectedType == IRType.STRING) {
			if(data.getResultType() == IRType.INTEGER1) {
				// Boolean to string: select between "true" and "false" constants
				String trueStr = llvmenv.getOrPutStringConstant("true");
				String falseStr = llvmenv.getOrPutStringConstant("false");
				int result = llvmenv.getNewLocalVariableReference(IRType.STRING);
				builder.appendLine(t, "%" + result + " = select " + data.getReference()
						+ ", i8* getelementptr inbounds ([5 x i8], [5 x i8]* @" + trueStr + ", i64 0, i64 0)"
						+ ", i8* getelementptr inbounds ([6 x i8], [6 x i8]* @" + falseStr + ", i64 0, i64 0)");
				return IRDataBuilder.setReturnVariable(result, IRType.STRING);
			}
			// Everything can be converted to a string via sprintf, but it requires slightly different code for each.
			if(os.isWindows()) {
				llvmenv.addSystemHeader("stdio.h");
			}
			llvmenv.addGlobalDeclaration(AsmCommonLibTemplates.SPRINTF, env);
			int alloca = llvmenv.getNewLocalVariableReference(IRType.OTHER);
			int gep = llvmenv.getNewLocalVariableReference(IRType.OTHER);
			int sprintf = llvmenv.getNewLocalVariableReference(IRType.INTEGER32);
			if(data.getResultType().getCategory() == Category.FLOAT) {
				// Largest size sprintf can return for a float is 317+1. Thus, we hardcode a 318 buffer.
				String f = llvmenv.getOrPutStringConstant("%.17f");
				builder.appendLine(t, "%" + alloca + " = alloca [318 x i8]");
				builder.appendLine(t, "%" + gep + " = getelementptr inbounds [318 x i8], [318 x i8]* %" + alloca + ", i64 0, i64 0");
				builder.appendLine(t, "%" + sprintf + " = call i32 (i8*, i8*, ...) @sprintf(i8* %" + gep + ", "
						+ "i8* getelementptr inbounds ([6 x i8], [6 x i8]* @" + f + ", i64 0, i64 0), " + data.getReference() + ")");

				return IRDataBuilder.setReturnVariable(gep, IRType.STRING);
			} else if(data.getResultType().getCategory() == Category.INTEGER) {
				// LONG_MIN is 19 digits long + a minus + null terminator = 21
				String i = llvmenv.getOrPutStringConstant("%i");
				builder.appendLine(t, "%" + alloca + " = alloca [21 x i8]");
				builder.appendLine(t, "%" + gep + " = getelementptr inbounds [21 x i8], [21 x i8]* %" + alloca + ", i64 0, i64 0");
				builder.appendLine(t, "%" + sprintf + " = call i32 (i8*, i8*, ...) @sprintf(i8* %" + gep + ", "
						+ "i8* getelementptr inbounds ([3 x i8], [3 x i8]* @" + i + ", i64 0, i64 0), " + data.getReference() + ")");
				return IRDataBuilder.setReturnVariable(gep, IRType.STRING);
			}
		}
		// TODO
		throw new UnsupportedOperationException("Conversion of " + data.getResultType().getIRType()
				+ " to " + expectedType.getIRType() + " is not implemented yet.");
	}

	public static IRData getInt64(IRBuilder builder, Environment env, ParseTree c, Target t) throws ConfigCompileException {
		if(c.isConst()) {
			Mixed data = c.getData();
			long i = ArgumentValidation.getInt(data, t);
			return IRDataBuilder.asConstant(IRType.INTEGER64, Long.toString(i));
		} else if(c.getData() instanceof CFunction) {
			return handleFunction(t, IRType.INTEGER64, builder, env, c);
		}
		throw new UnsupportedOperationException();
	}

	public static IRData getInt32(IRBuilder builder, Environment env, ParseTree c, Target t) throws ConfigCompileException {
		if(c.isConst()) {
			Mixed data = c.getData();
			int i = ArgumentValidation.getInt32(data, t, null);
			return IRDataBuilder.asConstant(IRType.INTEGER32, Integer.toString(i));
		} else if(c.getData() instanceof CFunction) {
			return handleFunction(t, IRType.INTEGER32, builder, env, c);
		}
		throw new UnsupportedOperationException();
	}

	public static IRData getDouble(IRBuilder builder, Environment env, ParseTree c, Target t) throws ConfigCompileException {
		if(c.isConst()) {
			Mixed data = c.getData();
			double i = ArgumentValidation.getDouble(data, t);
			return IRDataBuilder.asConstant(IRType.DOUBLE, Double.toString(i));
		} else if(c.getData() instanceof CFunction) {
			return handleFunction(t, IRType.DOUBLE, builder, env, c);
		}
		throw new UnsupportedOperationException();
	}


	/**
	 * Emits the IR necessary to convert the given value into a string.
	 * @param builder
	 * @param env
	 * @param c
	 * @param t
	 * @return
	 * @throws ConfigCompileException
	 */
	public static IRData getString(IRBuilder builder, Environment env, ParseTree c, Target t) throws ConfigCompileException {
		LLVMEnvironment e = env.getEnv(LLVMEnvironment.class);
		if(c.isConst()) {
			Mixed data = c.getData();
			String s = ArgumentValidation.getString(data, t);
			String id = e.getOrPutStringConstant(s);
			int length = s.length() + 1;
			String ref = "getelementptr inbounds ([" + length + " x i8], [" + length + " x i8]* @" + id + ", i64 0, i64 0)";
			return IRDataBuilder.asConstant(IRType.STRING, ref);
		} else if(c.getData() instanceof CFunction) {
			return handleFunction(t, IRType.STRING, builder, env, c);
		} else if(c.getData() instanceof IVariable ivar) {
			String name = ivar.getVariableName();
			IRType datatype = convertCClassTypeToIRType(e.getVariableType(name));
			int load = e.getVariableMapping(name);
			IRData data = IRDataBuilder.setReturnVariable(load, datatype);
			return convert(t, IRType.STRING, builder, env, data);
		}
		throw new UnsupportedOperationException();
	}

	/**
	 * Given a CClassType, returns the IRType that this maps to. Types that map 1:1 to a concrete LLVM primitive
	 * (int, double, string, boolean) return the corresponding IRType. Everything else (auto, number, mixed,
	 * arrays, objects, interface types) returns MS_VALUE.
	 * @param type
	 * @return
	 */
	public static IRType convertCClassTypeToIRType(CClassType type) {
		if(type == CInt.TYPE) {
			return IRType.INTEGER64;
		} else if(type == CDouble.TYPE) {
			return IRType.DOUBLE;
		} else if(type == CBoolean.TYPE) {
			return IRType.INTEGER1;
		} else if(CClassType.AUTO.equals(type)) {
			return IRType.MS_VALUE;
		}
		try {
			if(CString.TYPE.isExtendedBy(type)) {
				return IRType.STRING;
			}
		} catch (ClassNotFoundException ex) {
			throw new UnsupportedOperationException(ex);
		}

		return IRType.MS_VALUE;
	}

	public static String getValueFromConstant(IRBuilder builder, ParseTree data, Environment env) {
		if(!data.isConst()) {
			throw new Error();
		}
		Mixed value = data.getData();
		return LLVMPlatformResolver.outputConstant(builder, value, env).getReference();
	}

	/**
	 * Renders the IR necessary to evaluate the argument, then unconditionally stores it, and returns the referenced
	 * alloca node. Note that unlike the other operations where a specific type is called for, this will never be
	 * optimized to a constant call, even if the value is in fact a constant. This might be changed in the future, but
	 * in any case, if a specific type is required by the function, that is generally preferred to this method anyways.
	 *
	 * @param builder
	 * @param env
	 * @param c
	 * @param t
	 * @return
	 * @throws ConfigCompileException
	 */
	public static IRData getAny(IRBuilder builder, Environment env, ParseTree c, Target t) throws ConfigCompileException {
		LLVMEnvironment e = env.getEnv(LLVMEnvironment.class);
		if(c.isConst()) {
			IRType datatype = convertCClassTypeToIRType(c.getData().typeof(env));
			String data = getValueFromConstant(builder, c, env);
			int alloca = e.getNewLocalVariableReference(datatype);
			int load = e.getNewLocalVariableReference(datatype);
			builder.generator(t, env).allocaStoreAndLoad(alloca, datatype, data, load);
			return IRDataBuilder.setReturnVariable(load, datatype);
		} else if(c.getData() instanceof CFunction) {
			IRData data = AsmCompiler.getIR(builder, c, env);
			int alloca = e.getNewLocalVariableReference(data.getResultType());
			int load = e.getNewLocalVariableReference(data.getResultType());
			builder.generator(t, env).allocaStoreAndLoad(alloca, data.getResultType(), data.getResultVariable(), load);
			return IRDataBuilder.setReturnVariable(load, data.getResultType());
		} else if(c.getData() instanceof IVariable ivar) {
			String name = ivar.getVariableName();
			IRType datatype = convertCClassTypeToIRType(e.getVariableType(name));
			int varRef = e.getVariableMapping(name);
			return IRDataBuilder.setReturnVariable(varRef, datatype);
		}
		throw new UnsupportedOperationException();
	}

	/**
	 * Boxes a concrete value into an ms_value. If the value is already an ms_value (or ms_null),
	 * returns it unchanged.
	 * @param builder The IR builder to append to.
	 * @param t The code target for debug info.
	 * @param env The environment.
	 * @param data The value to box.
	 * @return IRData pointing to the boxed ms_value variable.
	 */
	public static IRData boxToMsValue(IRBuilder builder, Target t, Environment env, IRData data) {
		if(data.getResultType() == IRType.MS_VALUE || data.getResultType() == IRType.MS_NULL) {
			return data;
		}
		return emitBox(builder, t, env, data.getResultType(), data.getReference());
	}

	/**
	 * Emits IR to box a typed value into an ms_value. The result is an ms_value stored in a local variable.
	 * @param builder The IR builder to append to.
	 * @param t The code target for debug info.
	 * @param env The environment.
	 * @param sourceType The concrete IR type of the value being boxed.
	 * @param sourceRef The reference to the value (e.g. "i64 %5" or "i64 42").
	 * @return IRData pointing to the boxed ms_value variable.
	 */
	private static IRData emitBox(IRBuilder builder, Target t, Environment env,
			IRType sourceType, String sourceRef) {
		LLVMEnvironment llvmenv = env.getEnv(LLVMEnvironment.class);
		if(!sourceType.isBoxable()) {
			throw new UnsupportedOperationException("Cannot box type " + sourceType.getIRType());
		}

		// Convert the value to i64 payload
		String payloadRef;
		if(sourceType.getCategory() == IRType.Category.INTEGER) {
			if(sourceType == IRType.INTEGER64) {
				payloadRef = sourceRef;
			} else {
				int zext = llvmenv.getNewLocalVariableReference(IRType.INTEGER64);
				builder.appendLine(t, "%" + zext + " = zext " + sourceRef + " to i64");
				payloadRef = "i64 %" + zext;
			}
		} else if(sourceType.getCategory() == IRType.Category.FLOAT) {
			if(sourceType == IRType.DOUBLE) {
				int bitcast = llvmenv.getNewLocalVariableReference(IRType.INTEGER64);
				builder.appendLine(t, "%" + bitcast + " = bitcast " + sourceRef + " to i64");
				payloadRef = "i64 %" + bitcast;
			} else {
				// Upcast to double first, then bitcast to i64
				int fpext = llvmenv.getNewLocalVariableReference(IRType.DOUBLE);
				builder.appendLine(t, "%" + fpext + " = fpext " + sourceRef + " to double");
				int bitcast = llvmenv.getNewLocalVariableReference(IRType.INTEGER64);
				builder.appendLine(t, "%" + bitcast + " = bitcast double %" + fpext + " to i64");
				payloadRef = "i64 %" + bitcast;
			}
		} else if(sourceType == IRType.STRING || sourceType == IRType.INTEGER8POINTER) {
			int ptrtoint = llvmenv.getNewLocalVariableReference(IRType.INTEGER64);
			builder.appendLine(t, "%" + ptrtoint + " = ptrtoint " + sourceRef + " to i64");
			payloadRef = "i64 %" + ptrtoint;
		} else {
			throw new UnsupportedOperationException("Cannot box type " + sourceType.getIRType());
		}

		// Build the struct field by field from undef
		int insertTag = llvmenv.getNewLocalVariableReference(IRType.MS_VALUE);
		int insertPayload = llvmenv.getNewLocalVariableReference(IRType.MS_VALUE);
		builder.appendLine(t, "%" + insertTag + " = insertvalue { i8, i64 } undef, i8 "
				+ sourceType.getBoxTag() + ", 0");
		builder.appendLine(t, "%" + insertPayload + " = insertvalue { i8, i64 } %" + insertTag
				+ ", " + payloadRef + ", 1");

		return IRDataBuilder.setReturnVariable(insertPayload, IRType.MS_VALUE);
	}

	/**
	 * Emits IR to unbox an ms_value into a specific expected type.
	 * @param builder The IR builder to append to.
	 * @param t The code target for debug info.
	 * @param env The environment.
	 * @param expectedType The concrete IR type to extract.
	 * @param boxedVariable The variable reference holding the ms_value.
	 * @return IRData pointing to the unboxed value.
	 */
	private static IRData emitUnbox(IRBuilder builder, Target t, Environment env,
			IRType expectedType, int boxedVariable) {
		LLVMEnvironment llvmenv = env.getEnv(LLVMEnvironment.class);

		// Extract the i64 payload from index 1
		int payload = llvmenv.getNewLocalVariableReference(IRType.INTEGER64);
		builder.appendLine(t, "%" + payload + " = extractvalue { i8, i64 } %" + boxedVariable + ", 1");

		if(expectedType == IRType.INTEGER64) {
			return IRDataBuilder.setReturnVariable(payload, IRType.INTEGER64);
		} else if(expectedType.getCategory() == IRType.Category.INTEGER) {
			// Truncate from i64 to smaller integer
			int trunc = llvmenv.getNewLocalVariableReference(expectedType);
			builder.appendLine(t, "%" + trunc + " = trunc i64 %" + payload + " to "
					+ expectedType.getIRType());
			return IRDataBuilder.setReturnVariable(trunc, expectedType);
		} else if(expectedType == IRType.DOUBLE) {
			// Reinterpret the i64 bits as double
			int bitcast = llvmenv.getNewLocalVariableReference(IRType.DOUBLE);
			builder.appendLine(t, "%" + bitcast + " = bitcast i64 %" + payload + " to double");
			return IRDataBuilder.setReturnVariable(bitcast, IRType.DOUBLE);
		} else if(expectedType.getCategory() == IRType.Category.FLOAT) {
			// Reinterpret as double first, then narrow to smaller float
			int bitcast = llvmenv.getNewLocalVariableReference(IRType.DOUBLE);
			builder.appendLine(t, "%" + bitcast + " = bitcast i64 %" + payload + " to double");
			int fptrunc = llvmenv.getNewLocalVariableReference(expectedType);
			builder.appendLine(t, "%" + fptrunc + " = fptrunc double %" + bitcast + " to "
					+ expectedType.getIRType());
			return IRDataBuilder.setReturnVariable(fptrunc, expectedType);
		} else if(expectedType == IRType.STRING || expectedType == IRType.INTEGER8POINTER) {
			// Convert the integer back to a pointer
			int inttoptr = llvmenv.getNewLocalVariableReference(expectedType);
			builder.appendLine(t, "%" + inttoptr + " = inttoptr i64 %" + payload + " to "
					+ expectedType.getIRType());
			return IRDataBuilder.setReturnVariable(inttoptr, expectedType);
		}
		throw new UnsupportedOperationException("Cannot unbox to type " + expectedType.getIRType());
	}

	/**
	 * Emits IR to extract the type tag from an ms_value.
	 * @param builder The IR builder to append to.
	 * @param t The code target for debug info.
	 * @param env The environment.
	 * @param boxedVariable The variable reference holding the ms_value.
	 * @return IRData pointing to the i8 tag value.
	 */
	static IRData emitGetTag(IRBuilder builder, Target t, Environment env, int boxedVariable) {
		LLVMEnvironment llvmenv = env.getEnv(LLVMEnvironment.class);
		int tag = llvmenv.getNewLocalVariableReference(IRType.INTEGER8);
		builder.appendLine(t, "%" + tag + " = extractvalue { i8, i64 } %" + boxedVariable + ", 0");
		return IRDataBuilder.setReturnVariable(tag, IRType.INTEGER8);
	}
}
