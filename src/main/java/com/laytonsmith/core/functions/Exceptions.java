package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.MathUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.FlowFunction;
import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.compiler.BranchStatement;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.SelfStatement;
import com.laytonsmith.core.compiler.VariableScope;
import com.laytonsmith.core.compiler.analysis.Scope;
import com.laytonsmith.core.compiler.analysis.StaticAnalysis;
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
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.environments.StaticRuntimeEnv;
import com.laytonsmith.core.exceptions.CRE.AbstractCREException;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CRECausedByWrapper;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.functions.Compiler.__type_ref__;
import com.laytonsmith.core.StepAction;
import com.laytonsmith.core.StepAction.Complete;
import com.laytonsmith.core.StepAction.Evaluate;
import com.laytonsmith.core.StepAction.StepResult;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
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

	/**
	 * Produced by {@code throw()} and by native functions that throw {@link ConfigRuntimeException}.
	 * The interpreter loop catches ConfigRuntimeException from exec() calls and wraps them in this.
	 * {@code _try} and {@code complex_try} handle this in their {@code childInterrupted()}.
	 */
	public static class ThrowAction implements StepAction.FlowControlAction {
		private final ConfigRuntimeException exception;

		public ThrowAction(ConfigRuntimeException exception) {
			this.exception = exception;
		}

		public ConfigRuntimeException getException() {
			return exception;
		}

		@Override
		public Target getTarget() {
			return exception.getTarget();
		}
	}

	@api
	@seealso({_throw.class, com.laytonsmith.tools.docgen.templates.Exceptions.class})
	@SelfStatement
	public static class _try extends AbstractFunction implements FlowFunction<_try.TryState>, BranchStatement, VariableScope {

		public static final String NAME = "try";

		enum Phase { RESOLVE_VAR, RESOLVE_TYPES, TRY_BODY, CATCH_BODY }

		static class TryState {
			Phase phase;
			ParseTree[] children;
			IVariable ivar;
			List<FullyQualifiedClassName> interest;
			int catchIndex;

			TryState(ParseTree[] children) {
				this.children = children;
				this.interest = new ArrayList<>();
				if(children.length == 2) {
					catchIndex = 1;
				} else if(children.length >= 3) {
					catchIndex = 2;
				} else {
					catchIndex = -1;
				}
			}

			@Override
			public String toString() {
				return phase.name();
			}
		}

		@Override
		public StepResult<TryState> begin(Target t, ParseTree[] children, Environment env) {
			TryState state = new TryState(children);
			if(children.length >= 3) {
				state.phase = Phase.RESOLVE_VAR;
				return new StepResult<>(new Evaluate(children[1], null, true), state);
			}
			state.phase = Phase.TRY_BODY;
			return new StepResult<>(new Evaluate(children[0]), state);
		}

		@Override
		public StepResult<TryState> childCompleted(Target t, TryState state, Mixed result, Environment env) {
			switch(state.phase) {
				case RESOLVE_VAR:
					if(result instanceof IVariable iv) {
						state.ivar = iv;
					} else {
						throw new CRECastException("Expected argument 2 to be an IVariable", t);
					}
					if(state.children.length == 4) {
						state.phase = Phase.RESOLVE_TYPES;
						return new StepResult<>(new Evaluate(state.children[3]), state);
					}
					state.phase = Phase.TRY_BODY;
					return new StepResult<>(new Evaluate(state.children[0]), state);
				case RESOLVE_TYPES:
					Mixed ptypes = result;
					if(ptypes.isInstanceOf(CString.TYPE, null, env)) {
						state.interest.add(FullyQualifiedClassName.forName(ptypes.val(), t, env));
					} else if(ptypes.isInstanceOf(CArray.TYPE, null, env)) {
						CArray ca = (CArray) ptypes;
						for(int i = 0; i < ca.size(); i++) {
							state.interest.add(FullyQualifiedClassName.forName(
									ca.get(i, t).val(), t, env));
						}
					} else {
						throw new CRECastException(
								"Expected argument 4 to be a string, or an array of strings.", t);
					}
					for(FullyQualifiedClassName in : state.interest) {
						try {
							NativeTypeList.getNativeClass(in);
						} catch(ClassNotFoundException e) {
							throw new CREFormatException(
									"Invalid exception type passed to try():" + in, t);
						}
					}
					state.phase = Phase.TRY_BODY;
					return new StepResult<>(new Evaluate(state.children[0]), state);
				case TRY_BODY:
				case CATCH_BODY:
					return new StepResult<>(new Complete(CVoid.VOID), state);
				default:
					throw ConfigRuntimeException.CreateUncatchableException(
							"Invalid try state: " + state.phase, t);
			}
		}

		@Override
		public StepResult<TryState> childInterrupted(Target t, TryState state,
				StepAction.FlowControl action, Environment env) {
			if(state.phase == Phase.TRY_BODY
					&& action.getAction() instanceof ThrowAction throwAction) {
				ConfigRuntimeException e = throwAction.getException();
				if(!(e instanceof AbstractCREException)) {
					return null;
				}
				FullyQualifiedClassName name
						= ((AbstractCREException) e).getExceptionType().getFQCN();
				if(Prefs.DebugMode()) {
					StreamUtils.GetSystemOut().println("[" + Implementation.GetServerType().getBranding() + "]:"
							+ " Exception thrown (debug mode on) -> " + e.getMessage() + " :: " + name + ":"
							+ e.getTarget().file() + ":" + e.getTarget().line());
				}
				if(state.interest.isEmpty() || state.interest.contains(name)) {
					if(state.catchIndex >= 0) {
						CArray ex = ObjectGenerator.GetGenerator().exception(e, env, t);
						if(state.ivar != null) {
							state.ivar.setIval(ex);
							env.getEnv(GlobalEnv.class).GetVarList().set(state.ivar);
						}
						state.phase = Phase.CATCH_BODY;
						return new StepResult<>(
								new Evaluate(state.children[state.catchIndex]), state);
					}
					return new StepResult<>(new Complete(CVoid.VOID), state);
				}
				return null;
			}
			return null;
		}

		@Override
		public String getName() {
			return NAME;
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
		public Mixed exec(Target t, Environment env, GenericParameters generics, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			return CVoid.VOID;
		}

		@Override
		@SuppressWarnings({"checkstyle:fallthrough", "checkstyle:defaultcomeslast"}) // Intended for control flow.
		public Scope linkScope(StaticAnalysis analysis, Scope parentScope,
				ParseTree ast, Environment env, Set<ConfigCompileException> exceptions) {
			int numArgs = ast.numberOfChildren();
			Scope catchParentScope = parentScope;
			switch(numArgs) {
				default: { // Too many arguments. Analyze the first 4 as usual and handle the rest generically.
					Scope scope = parentScope;
					for(int i = 4; i < ast.numberOfChildren(); i++) {
						scope = analysis.linkScope(scope, ast.getChildAt(i), env, exceptions);
					}
				}
				case 4: { // try(tryCode, exParam, catchCode, exTypes).
					ParseTree exTypes = ast.getChildAt(3);
					analysis.linkScope(parentScope, exTypes, env, exceptions);
				}
				case 3: { // try(tryCode, exParam, catchCode).
					ParseTree exParam = ast.getChildAt(1);
					Scope[] scopes = analysis.linkParamScope(parentScope, parentScope, exParam, env, exceptions);
					catchParentScope = scopes[0]; // paramScope.
				}
				case 2: { // try(tryCode, [exParam], catchCode).
					ParseTree catchCode = ast.getChildAt(numArgs == 2 ? 1 : 2);
					analysis.linkScope(catchParentScope, catchCode, env, exceptions);
				}
				case 1: { // try(tryCode).
					ParseTree tryCode = ast.getChildAt(0);
					analysis.linkScope(parentScope, tryCode, env, exceptions);
				}
				case 0: {
					return parentScope;
				}
			}
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
					+ " The exceptionType may be any valid exception type. ---- "
					+ "The core exception types are: " + exceptions
					+ "\n\nThere may be other exception types as well,"
					+ " refer to the documentation of any extensions you have installed.";
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
		public Mixed exec(Target t, Environment env, GenericParameters generics, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			if(args.length == 1) {
				try {
					// Exception type
					// We need to reverse the excpetion into an object
					throw ObjectGenerator.GetGenerator().exception(ArgumentValidation.getArray(args[0], t), t, env);
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
				if(!CREThrowable.class.isAssignableFrom(c)) {
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
					arguments.add(new CRECausedByWrapper(ArgumentValidation.getArray(args[2], t)));
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
		public Mixed exec(Target t, Environment env, GenericParameters generics, Mixed... args) throws ConfigRuntimeException {
			if(args[0].isInstanceOf(CClosure.TYPE, null, env)) {
				CClosure old = env.getEnv(StaticRuntimeEnv.class).getExceptionHandler();
				env.getEnv(StaticRuntimeEnv.class).setExceptionHandler((CClosure) args[0]);
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
					+ " around the whole code block, but it is highly recommended you do not suppress this. It is possible to completely"
					+ " suppress all runtime exceptions using this method, but it is highly recommended that you still have a generic"
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
	@SelfStatement
	public static class complex_try extends AbstractFunction implements FlowFunction<complex_try.ComplexTryState>, Optimizable, BranchStatement, VariableScope {

		public static final String NAME = "complex_try";

		/**
		 * Please do not change this name or make it final, it is used reflectively for testing
		 */
		@SuppressWarnings("FieldMayBeFinal")
		private static boolean doScreamError = false;

		enum Phase { TRY_BODY, CATCH_BODY, FINALLY }

		static class ComplexTryState {
			Phase phase;
			ParseTree[] children;
			boolean hasFinally;
			StepAction.FlowControl pendingAction;
			ConfigRuntimeException suppressedException;
			boolean exceptionWasCaught;
			String catchVarName;

			ComplexTryState(ParseTree[] children) {
				this.children = children;
				this.hasFinally = (children.length % 2 == 0);
			}

			@Override
			public String toString() {
				return phase.name();
			}
		}

		@Override
		public StepResult<ComplexTryState> begin(Target t, ParseTree[] children, Environment env) {
			ComplexTryState state = new ComplexTryState(children);
			state.phase = Phase.TRY_BODY;
			return new StepResult<>(new Evaluate(children[0]), state);
		}

		@Override
		public StepResult<ComplexTryState> childCompleted(Target t, ComplexTryState state,
				Mixed result, Environment env) {
			switch(state.phase) {
				case TRY_BODY:
					if(state.hasFinally) {
						state.phase = Phase.FINALLY;
						return new StepResult<>(new Evaluate(
								state.children[state.children.length - 1]), state);
					}
					return new StepResult<>(new Complete(CVoid.VOID), state);
				case CATCH_BODY:
					if(state.catchVarName != null) {
						env.getEnv(GlobalEnv.class).GetVarList().remove(state.catchVarName);
					}
					if(state.hasFinally) {
						state.phase = Phase.FINALLY;
						return new StepResult<>(new Evaluate(
								state.children[state.children.length - 1]), state);
					}
					return new StepResult<>(new Complete(CVoid.VOID), state);
				case FINALLY:
					if(state.pendingAction != null) {
						return new StepResult<>(state.pendingAction, state);
					}
					return new StepResult<>(new Complete(CVoid.VOID), state);
				default:
					throw ConfigRuntimeException.CreateUncatchableException(
							"Invalid complex_try state: " + state.phase, t);
			}
		}

		@Override
		public StepResult<ComplexTryState> childInterrupted(Target t, ComplexTryState state,
				StepAction.FlowControl action, Environment env) {
			switch(state.phase) {
				case TRY_BODY:
					if(action.getAction() instanceof ThrowAction throwAction) {
						ConfigRuntimeException ex = throwAction.getException();
						if(ex instanceof AbstractCREException) {
							AbstractCREException e = AbstractCREException.getAbstractCREException(ex);
							CClassType exceptionType = e.getExceptionType();
							for(int i = 1; i < state.children.length - 1; i += 2) {
								ParseTree assign = state.children[i];
								CClassType clauseType = ((CClassType) assign.getChildAt(0).getData());
								if(exceptionType.doesExtend(clauseType)) {
									IVariableList varList = env.getEnv(GlobalEnv.class).GetVarList();
									IVariable var = (IVariable) assign.getChildAt(1).getData();
									state.catchVarName = var.getVariableName();
									varList.set(new IVariable(CArray.TYPE, var.getVariableName(),
											e.getExceptionObject(), t));
									state.phase = Phase.CATCH_BODY;
									return new StepResult<>(new Evaluate(state.children[i + 1]), state);
								}
							}
						}
						// No clause matched or non-AbstractCREException
						if(state.hasFinally) {
							state.pendingAction = action;
							state.exceptionWasCaught = true;
							state.suppressedException = throwAction.getException();
							state.phase = Phase.FINALLY;
							return new StepResult<>(new Evaluate(
									state.children[state.children.length - 1]), state);
						}
						return null;
					}
					// Non-throw flow control (return, break, etc.) — run finally then re-propagate
					if(state.hasFinally) {
						state.pendingAction = action;
						state.phase = Phase.FINALLY;
						return new StepResult<>(new Evaluate(
								state.children[state.children.length - 1]), state);
					}
					return null;
				case CATCH_BODY:
					if(action.getAction() instanceof ThrowAction throwAction) {
						state.suppressedException = throwAction.getException();
					}
					state.exceptionWasCaught = true;
					if(state.hasFinally) {
						state.pendingAction = action;
						state.phase = Phase.FINALLY;
						return new StepResult<>(new Evaluate(
								state.children[state.children.length - 1]), state);
					}
					return null;
				case FINALLY:
					if(state.exceptionWasCaught
							&& (doScreamError || Prefs.ScreamErrors() || Prefs.DebugMode())) {
						MSLog.GetLogger().Log(MSLog.Tags.RUNTIME, LogLevel.WARNING,
								"Exception was thrown and unhandled in any catch clause,"
								+ " but is being hidden by a new exception being thrown"
								+ " in the finally clause.", t);
						if(state.suppressedException != null) {
							ConfigRuntimeException.HandleUncaughtException(
									state.suppressedException, env);
						}
					}
					return null;
				default:
					return null;
			}
		}

		@Override
		public String getName() {
			return NAME;
		}

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
		public Mixed exec(Target t, Environment env, GenericParameters generics, Mixed... args) throws ConfigRuntimeException {
			return CVoid.VOID;
		}

		@Override
		public Scope linkScope(StaticAnalysis analysis, Scope parentScope,
				ParseTree ast, Environment env, Set<ConfigCompileException> exceptions) {
			if(ast.numberOfChildren() >= 1) {

				// Handle try code.
				ParseTree tryCode = ast.getChildAt(0);
				analysis.linkScope(parentScope, tryCode, env, exceptions);

				// Handle catch blocks with a catch variable.
				for(int i = 1; i < ast.numberOfChildren() - 1; i += 2) {
					ParseTree exParam = ast.getChildAt(i);
					ParseTree catchCode = ast.getChildAt(i + 1);
					Scope[] scopes = analysis.linkParamScope(parentScope, parentScope, exParam, env, exceptions);
					Scope exParamScope = scopes[0]; // paramScope.
					analysis.linkScope(exParamScope, catchCode, env, exceptions);
				}

				// Handle optional last catch block.
				if(MathUtils.isEven(ast.numberOfChildren())) {
					ParseTree catchCode = ast.getChildAt(ast.numberOfChildren() - 1);
					analysis.linkScope(parentScope, catchCode, env, exceptions);
				}
			}
			return parentScope;
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
				ParseTree assign = children.get(i);

				// Check for a container function with a string as first argument, being an unknown type.
				if(assign.numberOfChildren() > 0) {
					if(assign.getChildAt(0).getData().isInstanceOf(CString.TYPE, null, env)) {
						throw new ConfigCompileException("Unknown class type: "
								+ assign.getChildAt(0).getData().val(), t);
					}
					if(assign.getChildAt(0).getData() instanceof CFunction
									&& assign.getChildAt(0).getData().val().equals(__type_ref__.NAME)) {
						if(!StaticAnalysis.enabled()) {
							throw new ConfigCompileException("Unknown class type: "
										+ assign.getChildAt(0).getChildAt(0).getData().val(), t);
						}
						return null; // Type error has already been generated by the type checker.
					}
				}

				// Validate that the node is an assign with 3 arguments.
				if(!(assign.getData() instanceof CFunction cf && cf.getFunction() != null
						&& (cf.getFunction().getName().equals(DataHandling.assign.NAME)
								|| cf.getFunction().getName().equals(Compiler.__unsafe_assign__.NAME)))
						|| assign.numberOfChildren() != 3) {
					throw new ConfigCompileException("Expecting a variable declaration, but instead "
						+ assign.getData().val() + " was found", t);
				}

				// Validate that the first argument of the assign is a valid type.
				if(!(assign.getChildAt(0).getData() instanceof CClassType)) {
					throw new ConfigCompileException("Unknown class type: " + assign.getChildAt(0).getData().val(), t);
				}
				CClassType type = ((CClassType) assign.getChildAt(0).getData());
				types.add(type);

				// Validate that the exception type extends throwable.
				if(!type.doesExtend(CREThrowable.TYPE)) {
					throw new ConfigCompileException("The type defined in a catch clause must extend the"
							+ " Throwable class.", t);
				}

				// Validate that the assigned value is the by default added null.
				// TODO - This check should probably be moved into the keyword,
				// since technically catch (Exception @e = null) { } would work.
				if(!(assign.getChildAt(2).getData() instanceof CNull)) {
					throw new ConfigCompileException("Assignments are not allowed in catch clauses", t);
				}
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
		public Mixed exec(Target t, Environment env, GenericParameters generics, Mixed... args) throws ConfigRuntimeException {
			StackTraceManager stManager = env.getEnv(GlobalEnv.class).GetStackTraceManager();
			List<com.laytonsmith.core.exceptions.StackTraceFrame> elements = stManager.getCurrentStackTrace();
			CArray ret = new CArray(t);
			for(com.laytonsmith.core.exceptions.StackTraceFrame e : elements) {
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
