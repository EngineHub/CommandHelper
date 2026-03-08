package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.CallbackYield;
import com.laytonsmith.core.functions.Compiler;
import com.laytonsmith.core.natives.interfaces.Booleanish;
import com.laytonsmith.core.natives.interfaces.Callable;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.StepAction;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.AbstractCREException;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREStackOverflowError;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.StackTraceManager;
import com.laytonsmith.core.exceptions.UnhandledFlowControlException;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A closure is just an anonymous procedure.
 */
@typeof("ms.lang.closure")
public class CClosure extends Construct implements Callable, Booleanish {

	public static final long serialVersionUID = 1L;
	protected ParseTree node;
	protected final Environment env;
	protected final String[] names;
	protected final Mixed[] defaults;
	protected final CClassType[] types;
	protected final CClassType returnType;

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get(CClosure.class);

	public CClosure(ParseTree node, Environment env, CClassType returnType, String[] names, Mixed[] defaults,
			CClassType[] types, Target t) {
		super(node != null ? node.toString() : "", ConstructType.CLOSURE, t);
		this.node = node;
		this.env = env;
		this.names = names;
		this.defaults = defaults;
		this.types = types;
		if(types.length > 0) {
			for(int i = 0; i < types.length - 1; i++) {
				if(types[i].isVariadicType()) {
					throw new CREFormatException("Varargs can only be added to the last argument.", t);
				}
			}
		}
		this.returnType = returnType;
		for(String pName : names) {
			if(pName.equals("@arguments")) {
				env.getEnv(CompilerEnvironment.class)
						.addCompilerWarning(node.getFileOptions(),
								new CompilerWarning("This closure overrides the builtin @arguments parameter",
								t, FileOptions.SuppressWarning.OverrideArguments));
				break;
			}
		}
	}

	@Override
	public String val() {
		StringBuilder b = new StringBuilder();
		condense(getNode(), b);
		return b.toString();
	}

	private void condense(ParseTree node, StringBuilder b) {
		if(node == null) {
			return;
		}
		if(node.getData() instanceof CFunction) {
			CFunction func = (CFunction) node.getData();
			if(CFunction.IsFunction(func, com.laytonsmith.core.functions.Compiler.centry.class)) {
				// As a special case, we serialize this one with the label: value notation. This prevents issues
				// when deserializing it later.
				// Labels add : themselves, so no need to add that.
				b.append(node.getChildAt(0));
				condense(node.getChildAt(1), b);
			} else {
				b.append(func.val()).append("(");
				for(int i = 0; i < node.numberOfChildren(); i++) {
					condense(node.getChildAt(i), b);
					if(i != node.numberOfChildren() - 1 && !((CFunction) node.getData()).val().equals(Compiler.__autoconcat__.NAME)) {
						b.append(",");
					}
				}
				b.append(")");
			}
		} else if(node.getData().isInstanceOf(CString.TYPE, null, env)) {
			String data = ArgumentValidation.getString(node.getData(), node.getTarget());
			// Convert: \ -> \\ and ' -> \'
			b.append("'").append(data.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n")
					.replace("'", "\\'")).append("'");
		} else if(node.getData() instanceof IVariable) {
			b.append(((IVariable) node.getData()).getVariableName());
		} else {
			b.append(node.getData().val());
		}
	}

	public ParseTree getNode() {
		return node;
	}

	/**
	 * Prepares the closure for execution by cloning the environment, binding arguments,
	 * and pushing a stack trace element. This extracts the setup logic from {@link #execute}
	 * so that the iterative interpreter can evaluate the closure body on the shared EvalStack
	 * instead of recursing into a new eval() call.
	 *
	 * <p>The caller is responsible for evaluating the body (via {@link #getNode()}) in the
	 * returned environment, and for calling {@link StackTraceManager#popStackTraceElement()}
	 * when done (or ensuring it's done via a cleanup mechanism).</p>
	 *
	 * @param values The argument values to bind, or null for no arguments
	 * @return A {@link PreparedExecution} containing the prepared environment
	 * @throws ConfigRuntimeException If argument type checking fails
	 */
	public PreparedExecution prepareExecution(Mixed... values) throws ConfigRuntimeException {
		if(node == null) {
			return null;
		}
		Environment env;
		try {
			synchronized(this) {
				env = this.env.clone();
			}
		} catch(CloneNotSupportedException ex) {
			Logger.getLogger(CClosure.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
		StackTraceManager stManager = env.getEnv(GlobalEnv.class).GetStackTraceManager();
		stManager.addStackTraceElement(new ConfigRuntimeException.StackTraceElement("<<closure>>", getTarget()));

		CArray arguments = new CArray(node.getData().getTarget());
		CArray vararg = null;
		CClassType varargType = null;
		if(values != null) {
			for(int i = 0; i < Math.max(values.length, names.length); i++) {
				Mixed value;
				if(i < values.length) {
					value = values[i];
				} else {
					try {
						value = defaults[i].clone();
					} catch(CloneNotSupportedException ex) {
						Logger.getLogger(CClosure.class.getName()).log(Level.SEVERE, null, ex);
						value = defaults[i];
					}
				}
				arguments.push(value, node.getData().getTarget());
				boolean isVarArg = false;
				if(this.names.length > i
					|| (this.names.length != 0
						&& this.types[this.names.length - 1].isVariadicType())) {
					String name;
					if(i < this.names.length - 1
							|| !this.types[this.types.length - 1].isVariadicType()) {
						name = names[i];
					} else {
						name = this.names[this.names.length - 1];
						if(vararg == null) {
							// TODO: Once generics are added, add the type
							vararg = new CArray(value.getTarget());
							env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(CArray.TYPE,
									name, vararg, value.getTarget()));
							varargType = this.types[this.types.length - 1];
						}
						isVarArg = true;
					}
					if(isVarArg) {
						if(!InstanceofUtil.isInstanceof(value.typeof(env), varargType.getVarargsBaseType(), env)) {
							throw new CRECastException("Expected type " + varargType + " but found " + value.typeof(env),
									getTarget());
						}
						vararg.push(value, value.getTarget());
					} else {
						IVariable var = new IVariable(types[i], name, value, getTarget(), env);
						env.getEnv(GlobalEnv.class).GetVarList().set(var);
					}
				}
			}
		}
		boolean hasArgumentsParam = false;
		for(String pName : this.names) {
			if(pName.equals("@arguments")) {
				hasArgumentsParam = true;
				break;
			}
		}
		if(!hasArgumentsParam) {
			env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(CArray.TYPE, "@arguments", arguments,
					node.getData().getTarget()));
		}

		return new PreparedExecution(env, returnType);
	}

	/**
	 * The result of {@link #prepareExecution}. Contains the cloned environment with arguments
	 * bound, ready for the closure body to be evaluated.
	 */
	public static class PreparedExecution {
		private final Environment env;
		private final CClassType returnType;

		PreparedExecution(Environment env, CClassType returnType) {
			this.env = env;
			this.returnType = returnType;
		}

		public Environment getEnv() {
			return env;
		}

		public CClassType getReturnType() {
			return returnType;
		}
	}

	@Override
	public CClosure clone() throws CloneNotSupportedException {
		CClosure clone = (CClosure) super.clone();
		if(this.node != null) {
			clone.node = this.node.clone();
		}
		return clone;
	}

	/**
	 * If meta code needs to affect this closure's environment, it can access it with this function. Note that changing
	 * this will only affect future runs of the closure, it will not affect the currently running closure, (if any) due
	 * to the environment being cloned right before running.
	 *
	 * @return
	 */
	public synchronized Environment getEnv() {
		return env;
	}

	/**
	 * Shorthand for calling
	 * {@link #executeCallable(com.laytonsmith.core.environments.Environment,
	 * com.laytonsmith.core.constructs.Target, com.laytonsmith.core.natives.interfaces.Mixed...)}
	 * with a null environment, and Target.UNKNOWN. Since closures don't need these parameters,
	 * this is easier, however, Callables do not have this.
	 * @param values
	 * @return
	 * @throws ConfigRuntimeException
	 * @throws CancelCommandException
	 * @deprecated Functions that call closures should extend {@link CallbackYield}
	 * instead of calling this directly, which re-enters eval() and defeats the iterative interpreter.
	 */
	@Deprecated
	public Mixed executeCallable(Mixed... values) {
		return executeCallable(null, Target.UNKNOWN, values);
	}

	/**
	 * Executes the closure, giving it the supplied arguments. {@code values} may be null, which means that no arguments
	 * are being sent.
	 *
	 * ConfigRuntimeExceptions will bubble up past this, since an execution mechanism may need to do custom handling.
	 *
	 * A typical execution will include the following code:
	 * <pre>
	 * try {
	 *	closure.execute();
	 * } catch (ConfigRuntimeException e){
	 *	ConfigRuntimeException.HandleUncaughtException(e);
	 * } catch (CancelCommandException e){
	 *	// Ignored
	 * }
	 * </pre>
	 *
	 * @param env Unused, since the environment is fixed at time of definition,
	 * not at execution time.
	 * @param t The target at which the closure is executed.
	 * @param values The values to be passed to the closure
	 * @return The return value of the closure, or VOID if nothing was returned
	 * @throws ConfigRuntimeException If any call inside the closure causes a CRE
	 * @throws CancelCommandException If die() is called within the closure
	 * @deprecated Functions that call closures should extend {@link CallbackYield}
	 * instead of calling this directly, which re-enters eval() and defeats the iterative interpreter.
	 */
	@Deprecated
	@Override
	public Mixed executeCallable(Environment env, Target t, Mixed... values)
			throws ConfigRuntimeException, CancelCommandException {
		return execute(values);
	}

	/**
	 * @param values
	 * @throws ConfigRuntimeException
	 * @throws CancelCommandException
	 */
	protected Mixed execute(Mixed... values) throws ConfigRuntimeException,
			CancelCommandException {
		if(node == null) {
			return CVoid.VOID;
		}
		Environment env;
		try {
			synchronized(this) {
				env = this.env.clone();
			}
		} catch (CloneNotSupportedException ex) {
			Logger.getLogger(CClosure.class.getName()).log(Level.SEVERE, null, ex);
			return CVoid.VOID;
		}
		StackTraceManager stManager = env.getEnv(GlobalEnv.class).GetStackTraceManager();
		stManager.addStackTraceElement(new ConfigRuntimeException.StackTraceElement("<<closure>>", getTarget()));
		try {
			CArray arguments = new CArray(node.getData().getTarget());
			CArray vararg = null;
			CClassType varargType = null;
			if(values != null) {
				for(int i = 0; i < Math.max(values.length, names.length); i++) {
					Mixed value;
					if(i < values.length) {
						value = values[i];
					} else {
						value = defaults[i].clone();
					}
					arguments.push(value, node.getData().getTarget());
					boolean isVarArg = false;
					if(this.names.length > i
						|| (this.names.length != 0
							&& this.types[this.names.length - 1].isVariadicType())) {
						String name;
						if(i < this.names.length - 1
								|| !this.types[this.types.length - 1].isVariadicType()) {
							name = names[i];
						} else {
							name = this.names[this.names.length - 1];
							if(vararg == null) {
								// TODO: Once generics are added, add the type
								vararg = new CArray(value.getTarget());
								env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(CArray.TYPE,
										name, vararg, value.getTarget()));
								varargType = this.types[this.types.length - 1];
							}
							isVarArg = true;
						}
						if(isVarArg) {
							if(!InstanceofUtil.isInstanceof(value.typeof(env), varargType.getVarargsBaseType(), env)) {
								throw new CRECastException("Expected type " + varargType + " but found " + value.typeof(env),
										getTarget());
							}
							vararg.push(value, value.getTarget());
						} else {
							IVariable var = new IVariable(types[i], name, value, getTarget(), env);
							env.getEnv(GlobalEnv.class).GetVarList().set(var);
						}
					}
				}
			}
			boolean hasArgumentsParam = false;
			for(String pName : this.names) {
				if(pName.equals("@arguments")) {
					hasArgumentsParam = true;
					break;
				}
			}

			if(!hasArgumentsParam) {
				env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(CArray.TYPE, "@arguments", arguments,
						node.getData().getTarget()));
			}

			Script script = env.getEnv(GlobalEnv.class).GetScript();
			if(script == null) {
				script = Script.GenerateScript(node, env.getEnv(GlobalEnv.class).GetLabel(), null);
			}
			Mixed result;
			try {
				result = script.eval(node, env);
			} catch(UnhandledFlowControlException e) {
				StepAction.FlowControlAction action = e.getAction();
				if(action instanceof Exceptions.ThrowAction throwAction) {
					ConfigRuntimeException ex = throwAction.getException();
					ex.setEnv(env);
					if(ex instanceof AbstractCREException) {
						((AbstractCREException) ex).freezeStackTraceElements(stManager);
					}
					throw ex;
				}
				Target t = action.getTarget();
				ConfigRuntimeException.HandleUncaughtException(ConfigRuntimeException.CreateUncatchableException("A "
						+ "flow control action bubbled up to the top of"
						+ " a closure, which is unexpected behavior.", t), env);
				return CVoid.VOID;
			} catch(StackOverflowError e) {
				throw new CREStackOverflowError(null, node.getTarget(), e);
			}

			if(!returnType.equals(Auto.TYPE)
					&& !InstanceofUtil.isInstanceof(result.typeof(env), returnType, env)) {
				throw new CRECastException("Expected closure to return a value of type " + returnType.val()
						+ " but a value of type " + result.typeof(env) + " was returned instead",
						result.getTarget());
			}
			return result;
		} catch (CloneNotSupportedException ex) {
			Logger.getLogger(CClosure.class.getName()).log(Level.SEVERE, null, ex);
			return CVoid.VOID;
		} finally {
			stManager.popStackTraceElement();
		}
	}

	@Override
	public boolean isDynamic() {
		return false;
	}

	@Override
	public String docs() {
		return "A closure is a data type that contains executable code. This is similar to a procedure, but the value"
				+ " is first class,"
				+ " and can be stored in variables, and executed.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_1;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{Mixed.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return new CClassType[]{Callable.TYPE, Booleanish.TYPE};
	}

	/** @deprecated Use {@link #getBooleanValue(Target, Environment)} instead. */
	@Deprecated
	@Override
	public boolean getBooleanValue(Target t) {
		return getBooleanValue(t, null);
	}

	@Override
	public boolean getBooleanValue(Target t, Environment env) {
		return true;
	}
}
