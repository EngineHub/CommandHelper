package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Common.StringUtils;
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
import com.laytonsmith.core.exceptions.FunctionReturnException;
import com.laytonsmith.core.exceptions.LoopManipulationException;
import com.laytonsmith.core.exceptions.StackTraceManager;
import com.laytonsmith.core.functions.ControlFlow;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;
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
	private static final Pattern PROCEDURE_NAME_REGEX = Pattern.compile("^_[\\p{L}0-9]+[\\p{L}_0-9]*");
	/**
	 * The line the procedure is defined at (for stacktraces)
	 */
	private final Target definedAt;

	public Procedure(String name, CClassType returnType, List<IVariable> varList, ParseTree tree, Target t) {
		this.name = name;
		this.definedAt = t;
		this.varList = new HashMap<>();
		for(IVariable var : varList) {
			try {
				this.varList.put(var.getVariableName(), var.clone());
			} catch (CloneNotSupportedException e) {
				this.varList.put(var.getVariableName(), var);
			}
			this.varIndex.add(var);
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
		boolean prev = oldEnv.getEnv(GlobalEnv.class).getCloneVars();
		oldEnv.getEnv(GlobalEnv.class).setCloneVars(false);
		Environment env;
		try {
			env = oldEnv.clone();
			env.getEnv(GlobalEnv.class).setCloneVars(true);
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
		oldEnv.getEnv(GlobalEnv.class).setCloneVars(prev);

		//This is what will become our @arguments var
		CArray arguments = new CArray(Target.UNKNOWN);
		for(String key : originals.keySet()) {
			Mixed c = originals.get(key);
			env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(Auto.TYPE, key, c, c.getTarget()));
			arguments.push(c, t);
		}
		Script fakeScript = Script.GenerateScript(tree, env.getEnv(GlobalEnv.class).GetLabel()); // new Script(null, null);
		for(int i = 0; i < args.size(); i++) {
			Mixed c = args.get(i);
			arguments.set(i, c, t);
			if(varIndex.size() > i) {
				IVariable var = varIndex.get(i);
				if(c instanceof CNull || var.getDefinedType().equals(Auto.TYPE)
						|| InstanceofUtil.isInstanceof(c, var.getDefinedType(), env)) {
					env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(var.getDefinedType(),
							var.getVariableName(), c, c.getTarget()));
				} else {
					throw new CRECastException("Procedure \"" + name + "\" expects a value of type "
							+ var.getDefinedType().val() + " in argument " + (i + 1) + ", but"
							+ " a value of type " + c.typeof() + " was found instead.", c.getTarget());
				}
			}
		}
		env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(CArray.TYPE, "@arguments", arguments, t));
		StackTraceManager stManager = env.getEnv(GlobalEnv.class).GetStackTraceManager();
		stManager.addStackTraceElement(new ConfigRuntimeException.StackTraceElement("proc " + name, getTarget()));
		try {
			if(tree.getData() instanceof CFunction
					&& "sconcat".equals(tree.getData().val())) {
				//If the inner tree is just an sconcat, we can optimize by
				//simply running the arguments to the sconcat. We're not going
				//to use the results, after all, and this is a common occurance,
				//because the compiler will often put it there automatically.
				//We *could* optimize this by removing it from the compiled code,
				//and we still should do that, but this check is quick enough,
				//and so can remain even once we do add the optimization to the
				//compiler proper.
				for(ParseTree child : tree.getChildren()) {
					fakeScript.eval(child, env);
				}
			} else {
				fakeScript.eval(tree, env);
			}
		} catch (FunctionReturnException e) {
			// Normal exit
			Mixed ret = e.getReturn();
			if(returnType.equals(Auto.TYPE)) {
				return ret;
			}
			if(returnType.equals(CVoid.TYPE) != ret.equals(CVoid.VOID)
					|| !ret.equals(CNull.NULL) && !ret.equals(CVoid.VOID)
					&& !InstanceofUtil.isInstanceof(ret, returnType, env)) {
				throw new CRECastException("Expected procedure \"" + name + "\" to return a value of type "
						+ returnType.val() + " but a value of type " + ret.typeof() + " was returned instead",
						ret.getTarget());
			}
			return ret;
		} catch (LoopManipulationException ex) {
			// These cannot bubble up past procedure calls. This will eventually be
			// a compile error.
			throw ConfigRuntimeException.CreateUncatchableException("Loop manipulation operations (e.g. break() or continue()) cannot"
					+ " bubble up past procedures.", t);
		} catch (ConfigRuntimeException e) {
			if(e instanceof AbstractCREException) {
				((AbstractCREException) e).freezeStackTraceElements(stManager);
			}
			throw e;
		} catch (StackOverflowError e) {
			throw new CREStackOverflowError(null, t, e);
		} finally {
			stManager.popStackTraceElement();
		}
		// Normal exit, but no return.
		// If we got here, then there was no return value. This is fine, but only for returnType void or auto.
		// TODO: Once strong typing is implemented at a compiler level, this should be removed to increase runtime
		// performance.
		if(!(returnType.equals(Auto.TYPE) || returnType.equals(CVoid.TYPE))) {
			throw new CRECastException("Expecting procedure \"" + name + "\" to return a value of type " + returnType.val() + ","
					+ " but no value was returned.", tree.getTarget());
		}
		return CVoid.VOID;
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
}
