package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.BranchStatement;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.VariableScope;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.IVariableList;
import com.laytonsmith.core.constructs.NativeTypeList;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.AbstractCREException;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CRECausedByWrapper;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.FunctionReturnException;
import com.laytonsmith.core.exceptions.StackTraceManager;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
@core
public class Exceptions {

	public static String docs() {
		return "This class contains functions related to Exception handling in MethodScript";
	}

	@api
	@seealso({_throw.class, com.laytonsmith.tools.docgen.templates.Exceptions.class})
	public static class _try extends AbstractFunction implements BranchStatement, VariableScope {

		@Override
		public String getName() {
			return "try";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3, 4};
		}

		@Override
		public String docs() {
			return "void {tryCode, [varName, catchCode, [exceptionTypes]] | tryCode, catchCode} This function works similar to a try-catch block in most languages. If the code in"
					+ " tryCode throws an exception, instead of killing the whole script, it stops running, and begins running the catchCode."
					+ " var should be an ivariable, and it is set to an array containing information about the exception."
					+ " Consider using try/catch blocks instead of the try function."
					+ " ---- If exceptionTypes is provided, it should be an array of exception types, or a single string that this try function is interested in."
					+ " If the exception type matches one of the values listed, the exception will be caught, otherwise, the exception will continue up the stack."
					+ " If exceptionTypes is missing, it will catch all exceptions."
					+ " PLEASE NOTE! This function will not catch exceptions thrown by CommandHelper, only built in exceptions. "
					+ " Please see [[Exceptions|the wiki page on exceptions]] for more information about what possible "
					+ " exceptions can be thrown and where, and examples.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public boolean preResolveVariables() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_2;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed execs(Target t, Environment env, Script that, ParseTree... nodes) {
			ParseTree tryCode = nodes[0];
			ParseTree varName = null;
			ParseTree catchCode = null;
			ParseTree types = null;
			if(nodes.length == 2) {
				catchCode = nodes[1];
			} else if(nodes.length == 3) {
				varName = nodes[1];
				catchCode = nodes[2];
			} else if(nodes.length == 4) {
				varName = nodes[1];
				catchCode = nodes[2];
				types = nodes[3];
			}

			IVariable ivar = null;
			if(varName != null) {
				Mixed pivar = that.eval(varName, env);
				if(pivar instanceof IVariable) {
					ivar = (IVariable) pivar;
				} else {
					throw new CRECastException("Expected argument 2 to be an IVariable", t);
				}
			}
			List<FullyQualifiedClassName> interest = new ArrayList<>();
			if(types != null) {
				Mixed ptypes = that.seval(types, env);
				if(ptypes.isInstanceOf(CString.TYPE)) {
					interest.add(FullyQualifiedClassName.forName(ptypes.val(), t, env));
				} else if(ptypes.isInstanceOf(CArray.TYPE)) {
					CArray ca = (CArray) ptypes;
					for(int i = 0; i < ca.size(); i++) {
						interest.add(FullyQualifiedClassName.forName(ca.get(i, t).val(), t, env));
					}
				} else {
					throw new CRECastException("Expected argument 4 to be a string, or an array of strings.", t);
				}
			}

			for(FullyQualifiedClassName in : interest) {
				try {
					NativeTypeList.getNativeClass(in);
				} catch (ClassNotFoundException e) {
					throw new CREFormatException("Invalid exception type passed to try():" + in, t);
				}
			}

			try {
				that.eval(tryCode, env);
			} catch (ConfigRuntimeException e) {
				if(!(e instanceof AbstractCREException)) {
					throw e;
				}
				FullyQualifiedClassName name = ((AbstractCREException) e).getExceptionType().getFQCN();
				if(Prefs.DebugMode()) {
					StreamUtils.GetSystemOut().println("[" + Implementation.GetServerType().getBranding() + "]:"
							+ " Exception thrown (debug mode on) -> " + e.getMessage() + " :: " + name + ":"
							+ e.getTarget().file() + ":" + e.getTarget().line());
				}
				if(interest.isEmpty() || interest.contains(name)) {
					if(catchCode != null) {
						CArray ex = ObjectGenerator.GetGenerator().exception(e, env, t);
						if(ivar != null) {
							ivar.setIval(ex);
							env.getEnv(GlobalEnv.class).GetVarList().set(ivar);
						}
						that.eval(catchCode, env);
					}
				} else {
					throw e;
				}
			}

			return CVoid.VOID;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			return CVoid.VOID;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public List<Boolean> isBranch(List<ParseTree> children) {
			List<Boolean> ret = new ArrayList<>();
			ret.add(true);
			if(children.size() == 2) {
				ret.add(true);
			} else if(children.size() == 3) {
				ret.add(false);
				ret.add(true);
			} else if(children.size() == 4) {
				ret.add(false);
				ret.add(true);
				ret.add(false);
			}
			return ret;
		}

		@Override
		public List<Boolean> isScope(List<ParseTree> children) {
			List<Boolean> ret = new ArrayList<>(children.size());
			for(ParseTree child : children) {
				ret.add(true);
			}
			return ret;
		}

	}

	@api
	@seealso({_try.class, com.laytonsmith.tools.docgen.templates.Exceptions.class})
	public static class _throw extends AbstractFunction {

		@Override
		public String getName() {
			return "throw";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		@Override
		public String docs() {
			Set<Class<? extends CREThrowable>> e = ClassDiscovery.getDefaultInstance().loadClassesWithAnnotationThatExtend(typeof.class, CREThrowable.class);
			String exceptions = "\n";
			List<String> ee = new ArrayList<>();
			for(Class<? extends CREThrowable> c : e) {
				String exceptionType = ClassDiscovery.GetClassAnnotation(c, typeof.class).value();
				ee.add(exceptionType);
			}
			Collections.sort(ee);
			exceptions += StringUtils.Join(ee, ", ", ", and ");

			return "nothing {exceptionType, msg, [causedBy] | exception} This function causes an exception to be thrown."
					+ " The exceptionType may be any valid exception type."
					+ "\n\nThe core exception types are: " + exceptions
					+ "\n\nThere may be other exception types as well, refer to the documentation of any extensions you have installed.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_2;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		//The code: try(throw(...), @ex, ...) doesn't work,
		//because it sees throw, then kills the other children to try.
		//Blah.
//		@Override
//		public boolean isTerminal() {
//			return true;
//		}
		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			if(args.length == 1) {
				try {
					// Exception type
					// We need to reverse the excpetion into an object
					throw ObjectGenerator.GetGenerator().exception(Static.getArray(args[0], t), t, env);
				} catch (ClassNotFoundException ex) {
					throw new CRECastException(ex.getMessage(), t);
				}
			} else {
				if(args[0] instanceof CNull) {
					throw new CRECastException("An exception type must be specified", t);
				}
				Class<? extends Mixed> c;
				try {
					c = NativeTypeList.getNativeClass(FullyQualifiedClassName.forName(args[0].val(), t, env));
				} catch (ClassNotFoundException ex) {
					throw new CREFormatException("Expected a valid exception type, but found \"" + args[0].val() + "\"", t);
				}
				List<Class> classes = new ArrayList<>();
				List<Object> arguments = new ArrayList<>();
				classes.add(String.class);
				classes.add(Target.class);
				arguments.add(args[1].val());
				arguments.add(t);
				if(args.length == 3) {
					classes.add(Throwable.class);
					arguments.add(new CRECausedByWrapper(Static.getArray(args[2], t)));
				}
				CREThrowable throwable = (CREThrowable) ReflectionUtils.newInstance(c, classes.toArray(new Class[classes.size()]), arguments.toArray());
				throw throwable;
			}
		}
	}

	@api
	@seealso({_throw.class, _try.class, com.laytonsmith.tools.docgen.templates.Exceptions.class})
	public static class set_uncaught_exception_handler extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(args[0].isInstanceOf(CClosure.TYPE)) {
				CClosure old = environment.getEnv(GlobalEnv.class).GetExceptionHandler();
				environment.getEnv(GlobalEnv.class).SetExceptionHandler((CClosure) args[0]);
				if(old == null) {
					return CNull.NULL;
				} else {
					return old;
				}
			} else {
				throw new CRECastException("Expecting arg 1 of " + getName() + " to be a Closure, but it was " + args[0].val(), t);
			}
		}

		@Override
		public String getName() {
			return "set_uncaught_exception_handler";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "closure {closure(@ex)} Sets the uncaught exception handler, returning the currently set one, or null if none has been"
					+ " set yet. If code throws an exception, instead of doing"
					+ " the default (displaying the error to the user/console) it will run your code instead. The exception"
					+ " that was thrown will be passed to the closure, and it is expected that the closure returns either null,"
					+ " true, or false. ---- If null is returned, the default handling will occur. If false is returned, it will"
					+ " be \"escalated\" which in the current implementation is the same as returning null (this will be used"
					+ " in the future). If true is returned, then default action will not occur, as it is assumed you have handled"
					+ " it. Only one exception handler can be registered at this time. If code inside the closure generates it's own"
					+ " exception, this will be handled by displaying both exceptions. To prevent this, you could put a try() block"
					+ " around the whole code block, but it is highly recommended you do not supress this. It is possible to completely"
					+ " supress all runtime exceptions using this method, but it is highly recommended that you still have a generic"
					+ " logging mechanism, perhaps to console, so you don't \"lose\" your exceptions, and fail to realize anything is wrong.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "set_uncaught_exception_handler(closure(@ex){\n"
				+ "\tmsg('Exception caught!');\n"
				+ "\tmsg(@ex);\n"
				+ "\treturn(true);\n"
				+ "});\n\n"
				+ "@zero = 0;\n"
				+ "@exception = 1 / @zero; // This should throw an exception\n",
				// Can't automatically run this, since the examples don't have
				// the exception handling fully working.
				"Exception caught!\n"
				+ "{RangeException, Division by 0!, /path/to/script.ms, 8}")};
		}

	}

	@api
	@hide("In general, this should never be used in the functional syntax, and should only be"
			+ " automatically generated by the try keyword.")
	public static class complex_try extends AbstractFunction implements Optimizable, BranchStatement, VariableScope {

		/**
		 * Please do not change this name or make it final, it is used reflectively for testing
		 */
		@SuppressWarnings("FieldMayBeFinal")
		private static boolean doScreamError = false;

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return CVoid.VOID;
		}

		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			boolean exceptionCaught = false;
			ConfigRuntimeException caughtException = null;
			try {
				parent.eval(nodes[0], env);
			} catch (ConfigRuntimeException ex) {
				if(!(ex instanceof AbstractCREException)) {
					// This should never actually happen, but we want to protect
					// against errors, and continue to throw this one up the chain
					throw ex;
				}
				AbstractCREException e = AbstractCREException.getAbstractCREException(ex);
				CClassType exceptionType = e.getExceptionType();
				for(int i = 1; i < nodes.length - 1; i += 2) {
					ParseTree assign = nodes[i];
					CClassType clauseType = ((CClassType) assign.getChildAt(0).getData());
					if(exceptionType.unsafeDoesExtend(clauseType)) {
						try {
							// We need to define the exception in the variable table
							IVariableList varList = env.getEnv(GlobalEnv.class).GetVarList();
							IVariable var = (IVariable) assign.getChildAt(1).getData();
							// This should eventually be changed to be of the appropriate type. Unfortunately, that will
							// require reworking basically everything. We need all functions to accept Mixed, instead of Mixed.
							// This will have to do in the meantime.
							varList.set(new IVariable(CArray.TYPE, var.getVariableName(), e.getExceptionObject(), t));
							parent.eval(nodes[i + 1], env);
							varList.remove(var.getVariableName());
						} catch (ConfigRuntimeException | FunctionReturnException newEx) {
							if(newEx instanceof ConfigRuntimeException) {
								caughtException = (ConfigRuntimeException) newEx;
							}
							exceptionCaught = true;
							throw newEx;
						}
						return CVoid.VOID;
					}
				}
				// No clause caught it. Continue to throw the exception up the chain
				caughtException = ex;
				exceptionCaught = true;
				throw ex;
			} finally {
				if(nodes.length % 2 == 0) {
					// There is a finally clause. Run that here.
					try {
						parent.eval(nodes[nodes.length - 1], env);
					} catch (ConfigRuntimeException | FunctionReturnException ex) {
						if(exceptionCaught && (doScreamError || Prefs.ScreamErrors() || Prefs.DebugMode())) {
							MSLog.GetLogger().Log(MSLog.Tags.RUNTIME, LogLevel.WARNING, "Exception was thrown and"
									+ " unhandled in any catch clause,"
									+ " but is being hidden by a new exception being thrown in the finally clause.", t);
							ConfigRuntimeException.HandleUncaughtException(caughtException, env);
						}
						throw ex;
					}
				}
			}

			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "complex_try";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "void {tryBlock, [catchVariable, catchBlock]+, [catchBlock]}";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			List<CClassType> types = new ArrayList<>();
			for(int i = 1; i < children.size() - 1; i += 2) {
				// TODO: Eh.. should probably move this check into the keyword, since techincally
				// catch (Exception @e = null) { } would work.
				ParseTree assign = children.get(i);
				if(assign.getChildAt(0).getData().isInstanceOf(CString.TYPE)) {
					// This is an unknown exception type, because otherwise it would have been cast to a CClassType
					throw new ConfigCompileException("Unknown class type: " + assign.getChildAt(0).getData().val(), t);
				}
				types.add((CClassType) assign.getChildAt(0).getData());
				if(CFunction.IsFunction(assign, DataHandling.assign.class)) {
					// assign() will validate params 0 and 1
					CClassType type = ((CClassType) assign.getChildAt(0).getData());
					if(!type.unsafeDoesExtend(CREThrowable.TYPE)) {
						throw new ConfigCompileException("The type defined in a catch clause must extend the"
								+ " Throwable class.", t);
					}
					if(!(assign.getChildAt(2).getData() instanceof CNull)) {
						throw new ConfigCompileException("Assignments are not allowed in catch clauses", t);
					}
					continue;
				}
				throw new ConfigCompileException("Expecting a variable declaration, but instead "
						+ assign.getData().val() + " was found", t);
			}
			for(int i = 0; i < types.size(); i++) {
				CClassType t1 = types.get(i);
				for(int j = i + 1; j < types.size(); j++) {
					CClassType t2 = types.get(j);
					if(t1.equals(t2)) {
						throw new ConfigCompileException("Duplicate catch clauses found. Only one clause may"
								+ " catch exceptions of a particular type, but we found that " + t1.val() + " has"
								+ " a duplicate signature", t);
					}
				}
			}
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public boolean preResolveVariables() {
			return false;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public List<Boolean> isBranch(List<ParseTree> children) {
			List<Boolean> ret = new ArrayList<>(children.size());
			ret.add(true);
			for(int i = 1; i < children.size() - 1; i += 2) {
				ret.add(false);
				ret.add(true);
			}
			if(children.size() % 2 == 0) {
				ret.add(true);
			}
			return ret;
		}

		@Override
		public List<Boolean> isScope(List<ParseTree> children) {
			List<Boolean> ret = new ArrayList<>(children.size());
			for(ParseTree children1 : children) {
				ret.add(true);
			}
			return ret;
		}

	}

	@api
	@seealso({com.laytonsmith.tools.docgen.templates.Exceptions.class})
	public static class get_stack_trace extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			StackTraceManager stManager = environment.getEnv(GlobalEnv.class).GetStackTraceManager();
			List<ConfigRuntimeException.StackTraceElement> elements = stManager.getCurrentStackTrace();
			CArray ret = new CArray(t);
			for(ConfigRuntimeException.StackTraceElement e : elements) {
				ret.push(e.getObjectFor(), Target.UNKNOWN);
			}
			return ret;
		}

		@Override
		public String getName() {
			return "get_stack_trace";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} Returns an array of stack trace elements. This is the same stack trace that would be generated"
					+ " if one were to throw an exception, then catch it.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "proc _a(){\n"
				+ "\t_b();\n"
				+ "}\n"
				+ "\n"
				+ "proc _b(){\n"
				+ "\tmsg(get_stack_trace());\n"
				+ "}\n"
				+ "\n"
				+ "_a();")
			};
		}

	}
}
