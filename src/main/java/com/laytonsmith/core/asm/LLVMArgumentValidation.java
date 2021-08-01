package com.laytonsmith.core.asm;

import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 */
public final class LLVMArgumentValidation {
	private LLVMArgumentValidation() {}

	private static IRData handleFunction(Target t, IRType expectedType, IRBuilder builder, Environment env, ParseTree c) throws ConfigCompileException {
		IRData data = AsmCompiler.getIR(builder, c, env);
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
			int load = llvmenv.getNewLocalVariableReference();
			builder.appendLine(t, "%" + load + " = load " + data.getResultType().getIRType()
					+ ", " + data.getResultType().getIRType() + "* %" + data.getResultVariable());
			int ret = llvmenv.getNewLocalVariableReference(); // returned value
			if(expectedType.getBitDepth() >= data.getResultType().getBitDepth()) {
				// upcast
				builder.appendLine(t, "%" + ret + " = sext " + data.getResultType().getIRType() + " %" + load + " to " + expectedType.getIRType());
			} else {
				// downcast
				builder.appendLine(t, "%" + ret + " = trunc " + data.getResultType().getIRType() + " %" + load + " to " + expectedType.getIRType());
			}
			return IRDataBuilder.setReturnVariable(ret, expectedType);
		}
		if(expectedType == IRType.STRING) {
			// Everything can be converted to a string via sprintf, but it requires slightly different code for each.
			if(os.isWindows()) {
				llvmenv.addSystemHeader("stdio.h");
			}
			llvmenv.addGlobalDeclaration(AsmCommonLibTemplates.SPRINTF, env);
			if(data.getResultType().getCategory() == IRType.Category.FLOAT) {
				// Largest size sprintf can return for a float is 317+1. Thus, we hardcode a 318 buffer.
				int alloca = llvmenv.getNewLocalVariableReference();
				int gep = llvmenv.getNewLocalVariableReference();
				int sprintf = llvmenv.getNewLocalVariableReference();

				String f = llvmenv.getOrPutStringConstant("%f");
				builder.appendLine(t, "%" + alloca + " = alloca [318 x i8]");
				builder.appendLine(t, "%" + gep + " = getelementptr inbounds [318 x i8], [318 x i8]* %" + alloca + ", i64 0, i64 0");
				builder.appendLine(t, "%" + sprintf + " = call i32 (i8*, i8*, ...) @sprintf(i8* %" + gep + ", "
						+ "i8* getelementptr inbounds ([3 x i8], [3 x i8]* @" + f + ", i64 0, i64 0), " + data.getReference() + ")");

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
			int i = ArgumentValidation.getInt32(data, t);
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
		}
		throw new UnsupportedOperationException();
	}
}
