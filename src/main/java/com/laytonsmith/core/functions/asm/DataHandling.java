package com.laytonsmith.core.functions.asm;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.asm.IRBuilder;
import com.laytonsmith.core.asm.IRData;
import com.laytonsmith.core.asm.IRDataBuilder;
import com.laytonsmith.core.asm.LLVMArgumentValidation;
import com.laytonsmith.core.asm.LLVMEnvironment;
import com.laytonsmith.core.asm.LLVMFunction;
import com.laytonsmith.core.asm.LLVMVersion;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;

/**
 *
 */
public class DataHandling {

	@api(environments = LLVMEnvironment.class, platform = api.Platforms.COMPILER_LLVM)
	public static class assign extends LLVMFunction {

		@Override
		public IRData buildIR(IRBuilder builder, Target t, Environment env, ParseTree... nodes) throws ConfigCompileException {
			LLVMEnvironment llvmenv = env.getEnv(LLVMEnvironment.class);
			int offset;
			LeftHandSideType type;
			String name;
			if(nodes.length == 3) {
				offset = 1;
				if(!(nodes[offset].getData() instanceof IVariable)) {
					throw new CRECastException(getName() + " with 3 arguments only accepts an ivariable as the second argument.", t);
				}
				name = ((IVariable) nodes[offset].getData()).getVariableName();
				type = ArgumentValidation.getClassType(nodes[0].getData(), t, env);
				// TODO: Add duplicate check here, or remove if not needed
//				if(list.has(name) && env.getEnv(GlobalEnv.class).GetFlag("no-check-duplicate-assign") == null) {
//					if(env.getEnv(GlobalEnv.class).GetFlag("closure-warn-overwrite") != null) {
//						MSLog.GetLogger().Log(MSLog.Tags.RUNTIME, LogLevel.ERROR,
//								"The variable " + name + " is hiding another value of the"
//										+ " same name in the main scope.", t);
//					} else if(t != list.get(name, t, true, env).getDefinedTarget()) {
//						MSLog.GetLogger().Log(MSLog.Tags.RUNTIME, LogLevel.ERROR, name + " was already defined at "
//								+ list.get(name, t, true, env).getDefinedTarget() + " but is being redefined.", t);
//					}
//				}
			} else {
				offset = 0;
				if(!(nodes[offset].getData() instanceof IVariable)) {
					throw new CRECastException(getName() + " with 2 arguments only accepts an ivariable as the first argument.", t);
				}
				name = ((IVariable) nodes[offset].getData()).getVariableName();
				type = llvmenv.getVariableType(name);
				if(type == null) {
					type = CClassType.AUTO.asLeftHandSideType();
				}
			}

			IRData data = LLVMArgumentValidation.getAny(builder, env, nodes[offset + 1], t);
			llvmenv.addVariableMapping(name, data.getResultVariable(), type);
			return IRDataBuilder.asVoid();
		}

		@Override
		public String getName() {
			return "assign";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
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
