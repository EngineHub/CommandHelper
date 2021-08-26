package com.laytonsmith.core.asm;

import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.asm.IRType.Category;
import com.laytonsmith.core.compiler.CompilerEnvironment;
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
		if(expectedType == IRType.STRING) {
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
	 * Given a CClassType, returns the IRType that this maps to. Auto and other unmappable concepts return the generic
	 * struct type.
	 * @param type
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static IRType convertCClassTypeToIRType(CClassType type) {
		try {
			if(CInt.TYPE.isExtendedBy(type)) {
				return IRType.INTEGER64;
			} else if(CDouble.TYPE.isExtendedBy(type)) {
				return IRType.DOUBLE;
			} else if(CString.TYPE.isExtendedBy(type)) {
				return IRType.STRING;
			}
		} catch (ClassNotFoundException ex) {
			throw new UnsupportedOperationException(ex);
		}

		// TODO: Eventually, this will just return the arbitrary data structure type. However, for native types,
		// we should support all of them directly, so for the time being, this just throws.
		throw new UnsupportedOperationException(type.getFQCN().getFQCN() + " is not yet supported");
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
			IRType datatype = convertCClassTypeToIRType(c.getData().typeof());
			String data = getValueFromConstant(builder, c, env);
			int alloca = e.getNewLocalVariableReference(datatype);
			int load = e.getNewLocalVariableReference(datatype);
			builder.generator(t, env).allocaStoreAndLoad(alloca, datatype, data, load);
			return IRDataBuilder.setReturnVariable(load, datatype);
		} else if(c.getData() instanceof CFunction cf) {
			Set<ConfigCompileException> exceptions = new HashSet<>();
			CClassType retType = cf.getCachedFunction().typecheck(e.getStaticAnalysis(), c, env, exceptions);
			IRData data = handleFunction(t, convertCClassTypeToIRType(retType), builder, env, c);
			int alloca = e.getNewLocalVariableReference(data.getResultType());
			int load = e.getNewLocalVariableReference(data.getResultType());
			builder.generator(t, env).allocaStoreAndLoad(alloca, data.getResultType(), data.getResultVariable(), load);
			return IRDataBuilder.setReturnVariable(load, data.getResultType());
		} else if(c.getData() instanceof IVariable ivar) {
			String name = ivar.getVariableName();
			IRType datatype = convertCClassTypeToIRType(e.getVariableType(name));
			int load = e.getNewLocalVariableReference(datatype);
			builder.generator(t, env).load(load, datatype, e.getVariableMapping(name));
			return IRDataBuilder.setReturnVariable(load, datatype);
		}
		throw new UnsupportedOperationException();
	}
}
