package com.laytonsmith.core.functions.asm;

/**
 *
 */
public class ControlFlow {
//	@api(environments = LLVMEnvironment.class, platform = api.Platforms.COMPILER_LLVM)
//	public static class _if extends LLVMFunction {
//
//		@Override
//		public boolean usePreExecution() {
//			return true;
//		}
//
//		@Override
//		public IRData getIR(Target t, Environment env, Script parent, IRData... nodes) throws ConfigCompileException {
//			throw new Error();
//		}
//
//
//		@Override
//		public IRData preGetIR(Target t, Environment env, Script parent, ParseTree... nodes) throws ConfigCompileException {
//			StringBuilder output = new StringBuilder();
//			LLVMEnvironment llvmenv = env.getEnv(LLVMEnvironment.class);
//			IRData conditionIR = AsmCompiler.getIR(nodes[0], env);
//			output.append(conditionIR.getIr());
//			int condition;
//			if(conditionIR.getReturnCategory() == IRReturnCategory.VOID) {
//				condition = llvmenv.getNewLocalVariableReference();
//				output.append(AsmUtil.formatLine(t, llvmenv, "%" + condition + " = i32 0"));
//			} else {
//				condition = conditionIR.getResultVariable();
//			}
//
//			int firstJmp = llvmenv.getNewLocalVariableReference();
//			int secondJmp = llvmenv.getNewLocalVariableReference();
//			int finalJmp = llvmenv.getNewLocalVariableReference();
//
//			// TODO: Need to potentially (probably?) cast the condition to a i1
//			String jmpLine = "br i1 %" + condition + ", label %" + firstJmp;
//			if(nodes.length == 2) {
//				jmpLine += ", label %" + finalJmp;
//			} else if(nodes.length > 2) {
//				jmpLine += ", label %" + secondJmp;
//			}
//
//			output.append(AsmUtil.formatLine(t, llvmenv, jmpLine));
//			output.append(AsmUtil.formatLine(t, llvmenv, firstJmp + ":"));
//			output.append(AsmCompiler.getIR(nodes[1], env).getIr());
//			output.append(AsmUtil.formatLine(t, llvmenv, "br label %" + finalJmp));
//			if(nodes.length > 2) {
//				output.append(AsmUtil.formatLine(t, llvmenv, secondJmp + ":"));
//				output.append(AsmCompiler.getIR(nodes[2], env).getIr());
//			}
//			output.append(AsmUtil.formatLine(t, llvmenv, finalJmp + ":"));
//			// TODO This shouldn't return void
//			return IRDataBuilder.setRawIR(output.toString()).asVoid();
//		}
//
//		@Override
//		public String getName() {
//			return "if";
//		}
//
//		@Override
//		public Integer[] numArgs() {
//			return new Integer[]{2, 3};
//		}
//
//		@Override
//		public Class<? extends CREThrowable>[] thrown() {
//			return null;
//		}
//
//		@Override
//		public Version since() {
//			return LLVMVersion.V0_0_1;
//		}
//
//	}
}
