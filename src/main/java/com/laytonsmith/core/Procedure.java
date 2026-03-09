package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.SmartComment;
import com.laytonsmith.core.constructs.Auto;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.InstanceofUtil;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.AbstractCREException;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREStackOverflowError;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.StackTraceFrame;
import com.laytonsmith.core.exceptions.StackTraceManager;
import com.laytonsmith.core.exceptions.UnhandledFlowControlException;
import com.laytonsmith.core.functions.ControlFlow;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;
import com.laytonsmith.core.natives.interfaces.Callable;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A procedure is a user defined function, essentially. Unlike a closure, however, it does not clone a reference to the
 * environment when it is defined. It takes on the environment characteristics of the executing environment, not the
 * defining environment.
 */
public class Procedure implements Cloneable {

	private final String name;
	private Map<String, IVariable> varList;
	private final Map<String, Mixed> originals = new HashMap<>();
	private final List<IVariable> varIndex = new ArrayList<>();
	private ParseTree tree;
	private CClassType returnType;
	private boolean possiblyConstant = false;
	private SmartComment procComment;

	private static final Pattern PROCEDURE_NAME_REGEX = Pattern.compile("^_[\\p{L}0-9]+[\\p{L}_0-9]*");
	/**
	 * The line the procedure is defined at (for stacktraces)
	 */
	private final Target definedAt;

	public Procedure(String name, CClassType returnType, List<IVariable> varList, SmartComment procComment,
			ParseTree tree, Target t) {
		this.name = name;
		this.definedAt = t;
		this.varList = new HashMap<>();
		this.procComment = procComment;
		for(int i = 0; i < varList.size(); i++) {
			IVariable var = varList.get(i);
			if(var.getDefinedType().isVariadicType() && i != varList.size() - 1) {
				throw new CREFormatException("Varargs can only be added to the last argument.", t);
			}
			try {
				this.varList.put(var.getVariableName(), var.clone());
			} catch (CloneNotSupportedException e) {
				this.varList.put(var.getVariableName(), var);
			}
			this.varIndex.add(var);
			if(var.getDefinedType().isVariadicType() && var.ival() != CNull.UNDEFINED) {
				throw new CREFormatException("Varargs may not have default values", t);
			}
			this.originals.put(var.getVariableName(), var.ival());
		}
		this.tree = tree;
		if(!PROCEDURE_NAME_REGEX.matcher(name).matches()) {
			throw new CREFormatException("Procedure names must start with an underscore, and may only contain letters, underscores, and digits. (Found " + this.name + ")", t);
		}
		//Let's look through the tree now, and see if this is possibly constant or not.
		//If it is, it may or may not help us during compilation, but if it's not,
		//we can be sure that we cannot inline this in any way.
		this.possiblyConstant = checkPossiblyConstant(tree);
		this.returnType = returnType;
	}

	private boolean checkPossiblyConstant(ParseTree tree) {
		//TODO: This whole thing is a mess. Instead of doing it this way,
		//individual procs need to be inlined as deemed appropriate.
		if(true) {
			return false;
		}
		if(!Construct.IsDynamicHelper(tree.getData())) {
			//If it isn't dynamic, it certainly could be constant
			return true;
		} else if(tree.getData() instanceof IVariable) {
			//Variables will return true for isDynamic, but they are technically constant, because
			//they are being declared in this scope, or passed in. An import() would break this
			//contract, but import() itself is dynamic, so this is not an issue.
			return true;
		} else if(tree.getData() instanceof CFunction) {
			//If the function itself is not optimizable, we needn't recurse.
			try {
				FunctionBase fb = FunctionList.getFunction((CFunction) tree.getData(), null);
				if(fb instanceof Function) {
					Function f = (Function) fb;
					if(f instanceof ControlFlow._return) {
						//This is a special exception. Return itself is not optimizable,
						//but if the contents are optimizable, it is still considered constant.
						if(!tree.hasChildren()) {
							return true;
						} else {
							return checkPossiblyConstant(tree.getChildAt(0));
						}
					}
					//If it's optimizable, it's possible. If it's restricted, it doesn't matter, because
					//we can't optimize it out anyways, because we need to do the permission check
					Set<Optimizable.OptimizationOption> o = EnumSet.noneOf(Optimizable.OptimizationOption.class);
					if(f instanceof Optimizable) {
						o = ((Optimizable) f).optimizationOptions();
					}
					if(!((o != null && (o.contains(Optimizable.OptimizationOption.OPTIMIZE_DYNAMIC)
							|| o.contains(Optimizable.OptimizationOption.OPTIMIZE_CONSTANT))) && !f.isRestricted())) {
						return false; //Nope. Doesn't matter if the children are or not
					}
				} else {
					return false;
				}
			} catch (ConfigCompileException e) {
				//It's a proc. We will treat this just like any other function call,
			}
			//Ok, well, we have to check the children first.
			for(ParseTree child : tree.getChildren()) {
				if(!checkPossiblyConstant(child)) {
					return false; //Nope, since our child can't be constant, neither can we
				}
			}
			//They all check out, so, yep, we could possibly be constant
			return true;
		} else {
			//Uh. Ok, well, nope.
			return false;
		}
	}

	public boolean isPossiblyConstant() {
		return this.possiblyConstant;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name + "(" + StringUtils.Join(varList.keySet(), ", ") + ")";
	}

	public SmartComment getSmartComment() {
		return procComment;
	}

	/**
	 * Convenience wrapper around executing a procedure if the parameters are in tree mode still.
	 *
	 * @param args
	 * @param env
	 * @param t
	 * @return
	 */
	public Mixed cexecute(List<ParseTree> args, Environment env, Target t) {
		List<Mixed> list = new ArrayList<>();
		for(ParseTree arg : args) {
			list.add(env.getEnv(GlobalEnv.class).GetScript().seval(arg, env));
		}
		return execute(list, env, t);
	}

	/**
	 * Executes this procedure, with the arguments that were passed in
	 *
	 * @param args The arguments passed to the procedure call.
	 * @param oldEnv The environment to be cloned.
	 * @param t
	 * @return
	 */
	public Mixed execute(List<Mixed> args, Environment oldEnv, Target t) {
		Environment env = prepareEnvironment(args, oldEnv, t);

		Script fakeScript = Script.GenerateScript(tree, env.getEnv(GlobalEnv.class).GetLabel(), null);
		StackTraceManager stManager = env.getEnv(GlobalEnv.class).GetStackTraceManager();
		stManager.addStackTraceFrame(new StackTraceFrame("proc " + name, getTarget()));
		try {
			Mixed result = fakeScript.eval(tree, env);
			if(result == null) {
				result = CVoid.VOID;
			}
			return typeCheckReturn(result, env);
		} catch(UnhandledFlowControlException e) {
			if(e.getAction() instanceof ControlFlow.BreakAction
					|| e.getAction() instanceof ControlFlow.ContinueAction) {
				throw ConfigRuntimeException.CreateUncatchableException(
						"Loop manipulation operations (e.g. break() or continue()) cannot"
						+ " bubble up past procedures.", t);
			}
			if(e.getAction() instanceof Exceptions.ThrowAction ta) {
				ConfigRuntimeException ex = ta.getException();
				if(ex instanceof AbstractCREException ace) {
					ace.freezeStackTraceElements(stManager);
				}
				throw ex;
			}
			throw e;
		} catch(StackOverflowError e) {
			throw new CREStackOverflowError(null, t, e);
		} finally {
			stManager.popStackTraceFrame();
		}
	}

	public Target getTarget() {
		return definedAt;
	}

	@Override
	public Procedure clone() throws CloneNotSupportedException {
		Procedure clone = (Procedure) super.clone();
		if(this.varList != null) {
			clone.varList = new HashMap<>(this.varList);
		}
		if(this.tree != null) {
			clone.tree = this.tree.clone();
		}
		return clone;
	}

	public void definitelyNotConstant() {
		possiblyConstant = false;
	}

	/**
	 * Prepares this procedure for stack-based execution without re-entering eval().
	 * Clones the environment, binds arguments, and pushes a stack trace element.
	 * The caller is responsible for evaluating the returned tree in the returned
	 * environment, and for popping the stack trace element when done.
	 *
	 * @param args The evaluated argument values
	 * @param callerEnv The caller's environment (will be cloned)
	 * @param callTarget The target of the procedure call site
	 * @return The prepared call containing the procedure body tree and environment
	 */
	public Callable.PreparedCallable prepareCall(List<Mixed> args, Environment callerEnv, Target callTarget) {
		Environment env = prepareEnvironment(args, callerEnv, callTarget);
		StackTraceManager stManager = env.getEnv(GlobalEnv.class).GetStackTraceManager();
		stManager.addStackTraceFrame(
				new StackTraceFrame("proc " + name, getTarget()));
		return new Callable.PreparedCallable(tree, env);
	}

	/**
	 * Clones the environment and assigns procedure arguments (with type checking).
	 * Used by both {@link #execute} and {@link ProcedureFlow}.
	 *
	 * @param args The evaluated argument values
	 * @param oldEnv The caller's environment (will be cloned)
	 * @param callTarget The target of the procedure call site
	 * @return The prepared environment for the procedure body
	 */
	private Environment prepareEnvironment(List<Mixed> args, Environment oldEnv, Target callTarget) {
		boolean prev = oldEnv.getEnv(GlobalEnv.class).getCloneVars();
		oldEnv.getEnv(GlobalEnv.class).setCloneVars(false);
		Environment env;
		try {
			env = oldEnv.clone();
			env.getEnv(GlobalEnv.class).setCloneVars(true);
		} catch(CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
		oldEnv.getEnv(GlobalEnv.class).setCloneVars(prev);

		CArray arguments = new CArray(Target.UNKNOWN, this.varIndex.size());

		int varInd;
		CArray vararg = null;
		for(varInd = 0; varInd < args.size(); varInd++) {
			Mixed c = args.get(varInd);
			arguments.push(c, callTarget);
			if(this.varIndex.size() > varInd
					|| (!this.varIndex.isEmpty()
						&& this.varIndex.get(this.varIndex.size() - 1).getDefinedType().isVariadicType())) {
				IVariable var;
				if(varInd < this.varIndex.size() - 1
						|| !this.varIndex.get(this.varIndex.size() - 1).getDefinedType().isVariadicType()) {
					var = this.varIndex.get(varInd);
				} else {
					var = this.varIndex.get(this.varIndex.size() - 1);
					if(vararg == null) {
						vararg = new CArray(callTarget);
						env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(CArray.TYPE,
								var.getVariableName(), vararg, c.getTarget()));
					}
				}

				if(c instanceof CVoid
						&& !(var.getDefinedType().equals(Auto.TYPE) || var.getDefinedType().equals(CVoid.TYPE))) {
					throw new CRECastException("Procedure \"" + name + "\" expects a value of type "
							+ var.getDefinedType().val() + " in argument " + (varInd + 1) + ", but"
							+ " a void value was found instead.", c.getTarget());
				}

				if(var.getDefinedType().isVariadicType()) {
					if(InstanceofUtil.isInstanceof(c.typeof(env), var.getDefinedType().getVarargsBaseType(), env)) {
						vararg.push(c, callTarget);
						continue;
					} else {
						throw new CRECastException("Procedure \"" + name + "\" expects a value of type "
								+ var.getDefinedType().val() + " in argument " + (varInd + 1) + ", but"
								+ " a value of type " + c.typeof(env) + " was found instead.", c.getTarget());
					}
				}

				if(InstanceofUtil.isInstanceof(c.typeof(env), var.getDefinedType(), env)) {
					env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(var.getDefinedType(),
							var.getVariableName(), c, c.getTarget()));
					continue;
				} else {
					throw new CRECastException("Procedure \"" + name + "\" expects a value of type "
							+ var.getDefinedType().val() + " in argument " + (varInd + 1) + ", but"
							+ " a value of type " + c.typeof(env) + " was found instead.", c.getTarget());
				}
			}
		}

		while(varInd < this.varIndex.size()) {
			String varName = this.varIndex.get(varInd++).getVariableName();
			Mixed defVal = this.originals.get(varName);
			env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(Auto.TYPE, varName, defVal, defVal.getTarget()));
			arguments.push(defVal, callTarget);
		}

		env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(CArray.TYPE, "@arguments", arguments, callTarget));
		return env;
	}

	/**
	 * Type-checks a return value against this procedure's declared return type.
	 */
	private Mixed typeCheckReturn(Mixed ret, Environment env) {
		if(returnType.equals(Auto.TYPE)) {
			return ret;
		}
		if(returnType.equals(CVoid.TYPE) != ret.equals(CVoid.VOID)
				|| !ret.equals(CNull.NULL) && !ret.equals(CVoid.VOID)
				&& !InstanceofUtil.isInstanceof(ret.typeof(env), returnType, env)) {
			throw new CRECastException("Expected procedure \"" + name + "\" to return a value of type "
					+ returnType.val() + " but a value of type " + ret.typeof(env) + " was returned instead",
					ret.getTarget());
		}
		return ret;
	}

	/**
	 * Checks that this procedure's return type allows a void return (no explicit return statement).
	 */
	private Mixed typeCheckVoidReturn() {
		if(!(returnType.equals(Auto.TYPE) || returnType.equals(CVoid.TYPE))) {
			throw new CRECastException("Expecting procedure \"" + name + "\" to return a value of type "
					+ returnType.val() + ", but no value was returned.", tree.getTarget());
		}
		return CVoid.VOID;
	}

	/**
	 * Creates a {@link FlowFunction} for this procedure call, for use with the iterative
	 * interpreter. The flow function manages the procedure call lifecycle:
	 * <ol>
	 *   <li>Evaluates argument expressions (with IVariable resolution)</li>
	 *   <li>Prepares the procedure environment (clones env, assigns parameters)</li>
	 *   <li>Evaluates the procedure body in the new environment</li>
	 *   <li>Handles Return (type-checks and completes), blocks Break/Continue</li>
	 * </ol>
	 *
	 * @param callTarget The target of the procedure call site
	 * @return A per-call FlowFunction for this procedure
	 */
	public FlowFunction<?> createProcedureFlow(Target callTarget) {
		return new ProcedureFlow(callTarget);
	}

	/**
	 * Per-call flow function for procedure execution in the iterative interpreter.
	 * Manages the two-phase lifecycle: arg evaluation then body evaluation.
	 * Since this is created per-call, it stores state in its own fields
	 * rather than using the generic S type parameter.
	 */
	private class ProcedureFlow implements FlowFunction<Void> {
		private final Target callTarget;
		private final List<Mixed> evaluatedArgs = new ArrayList<>();
		private ParseTree[] children;
		private int argIndex = 0;
		private boolean bodyStarted = false;
		private Environment procEnv;

		ProcedureFlow(Target callTarget) {
			this.callTarget = callTarget;
		}

		@Override
		public StepAction.StepResult<Void> begin(Target t, ParseTree[] children, Environment env) {
			this.children = children;
			if(children.length == 0) {
				return new StepAction.StepResult<>(startBody(env), null);
			}
			return new StepAction.StepResult<>(new StepAction.Evaluate(children[0]), null);
		}

		@Override
		public StepAction.StepResult<Void> childCompleted(Target t, Void state, Mixed result, Environment env) {
			if(!bodyStarted) {
				// Resolve IVariables (seval semantics for proc arguments)
				Mixed resolved = result;
				while(resolved instanceof IVariable cur) {
					resolved = env.getEnv(GlobalEnv.class).GetVarList()
							.get(cur.getVariableName(), cur.getTarget(), env).ival();
				}
				evaluatedArgs.add(resolved);
				argIndex++;
				if(argIndex < children.length) {
					return new StepAction.StepResult<>(new StepAction.Evaluate(children[argIndex]), null);
				}
				return new StepAction.StepResult<>(startBody(env), null);
			}
			// Body completed normally (no explicit return)
			return new StepAction.StepResult<>(new StepAction.Complete(typeCheckVoidReturn()), null);
		}

		@Override
		public StepAction.StepResult<Void> childInterrupted(Target t, Void state,
				StepAction.FlowControl action, Environment env) {
			StepAction.FlowControlAction fca = action.getAction();
			if(fca instanceof ControlFlow.ReturnAction ret) {
				return new StepAction.StepResult<>(
						new StepAction.Complete(typeCheckReturn(ret.getValue(), procEnv)), null);
			}
			if(fca instanceof ControlFlow.BreakAction || fca instanceof ControlFlow.ContinueAction) {
				throw ConfigRuntimeException.CreateUncatchableException(
						"Loop manipulation operations (e.g. break() or continue()) cannot"
						+ " bubble up past procedures.", callTarget);
			}
			// Unknown flow control — propagate
			return null;
		}

		@Override
		public void cleanup(Target t, Void state, Environment env) {
			popStackTrace();
		}

		private StepAction startBody(Environment callerEnv) {
			bodyStarted = true;
			procEnv = prepareEnvironment(evaluatedArgs, callerEnv, callTarget);
			StackTraceManager stManager = procEnv.getEnv(GlobalEnv.class).GetStackTraceManager();
			stManager.addStackTraceFrame(
					new StackTraceFrame("proc " + name, getTarget()));
			return new StepAction.Evaluate(tree, procEnv);
		}

		private void popStackTrace() {
			if(procEnv != null) {
				procEnv.getEnv(GlobalEnv.class).GetStackTraceManager().popStackTraceFrame();
			}
		}
	}
}
