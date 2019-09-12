package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.Callable;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.AbstractCREException;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREStackOverflowError;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.FunctionReturnException;
import com.laytonsmith.core.exceptions.LoopManipulationException;
import com.laytonsmith.core.exceptions.ProgramFlowManipulationException;
import com.laytonsmith.core.exceptions.StackTraceManager;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A closure is just an anonymous procedure.
 *
 *
 */
@typeof("ms.lang.closure")
public class CClosure extends Construct implements Callable {

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
					if(i != node.numberOfChildren() - 1 && !((CFunction) node.getData()).val().equals("__autoconcat__")) {
						b.append(",");
					}
				}
				b.append(")");
			}
		} else if(node.getData().isInstanceOf(CString.TYPE)) {
			String data = ArgumentValidation.getString(node.getData(), node.getTarget());
			// Convert: \ -> \\ and ' -> \'
			b.append("'").append(data.replace("\\", "\\\\").replaceAll("\t", "\\\\t").replaceAll("\n", "\\\\n")
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
	 * @throws ProgramFlowManipulationException
	 * @throws CancelCommandException
	 */
	public Mixed executeCallable(Mixed... values) {
		return executeCallable(null, Target.UNKNOWN, values);
	}

	/**
	 * Executes the closure, giving it the supplied arguments. {@code values} may be null, which means that no arguments
	 * are being sent.
	 *
	 * LoopManipulationExceptions will never bubble up past this point, because they are never allowed, so they are
	 * handled automatically, but other ProgramFlowManipulationExceptions will, . ConfigRuntimeExceptions will also
	 * bubble up past this, since an execution mechanism may need to do custom handling.
	 *
	 * A typical execution will include the following code:
	 * <pre>
	 * try {
	 *	closure.execute();
	 * } catch (ConfigRuntimeException e){
	 *	ConfigRuntimeException.HandleUncaughtException(e);
	 * } catch (ProgramFlowManipulationException e){
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
	 * @throws ProgramFlowManipulationException If any ProgramFlowManipulationException is thrown (other than a
	 * LoopManipulationException) within the closure
	 */
	@Override
	public Mixed executeCallable(Environment env, Target t, Mixed... values)
			throws ConfigRuntimeException, ProgramFlowManipulationException, CancelCommandException {
		try {
			execute(values);
		} catch (FunctionReturnException e) {
			return e.getReturn();
		}
		return CVoid.VOID;
	}

	/**
	 * @deprecated This method suffers from the fact that a FunctionReturnException may end up bubbling up past the
	 * point of intended handling, given an error in the code that forgets to catch FunctionReturnException
	 * (or a superclass), but may be
	 * hard to detect. Instead, use {@link #ExecuteClosure} which unconditionally catches the exception, and then
	 * returns it. This also simplifies the code. This will not be removed earlier than 3.3.5.
	 * @param values
	 * @throws ConfigRuntimeException
	 * @throws ProgramFlowManipulationException
	 * @throws FunctionReturnException
	 * @throws CancelCommandException
	 */
	// This method actually shouldn't be removed when the deprecation period is over, it should just be made protected,
	// since it is still the foundation of executeClosure.
	@Deprecated
	public void execute(Mixed... values) throws ConfigRuntimeException, ProgramFlowManipulationException,
			FunctionReturnException, CancelCommandException {
		if(node == null) {
			return;
		}
		StackTraceManager stManager = env.getEnv(GlobalEnv.class).GetStackTraceManager();
		stManager.addStackTraceElement(new ConfigRuntimeException.StackTraceElement("<<closure>>", getTarget()));
		try {
			Environment environment;
			synchronized(this) {
				environment = env.clone();
			}
			if(values != null) {
				for(int i = 0; i < names.length; i++) {
					String name = names[i];
					Mixed value;
					try {
						value = values[i];
					} catch (Exception e) {
						value = defaults[i].clone();
					}
					environment.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(types[i], name, value,
							getTarget(), environment));
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
				CArray arguments = new CArray(node.getData().getTarget());
				if(values != null) {
					for(Mixed value : values) {
						arguments.push(value, node.getData().getTarget());
					}
				}
				environment.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(CArray.TYPE, "@arguments", arguments,
						node.getData().getTarget()));
			}

			ParseTree newNode = new ParseTree(new CFunction("g", getTarget()), node.getFileOptions());
			List<ParseTree> children = new ArrayList<>();
			children.add(node);
			newNode.setChildren(children);
			try {
				MethodScriptCompiler.execute(newNode, environment, null, environment.getEnv(GlobalEnv.class)
						.GetScript());
			} catch (LoopManipulationException e) {
				//This shouldn't ever happen.
				LoopManipulationException lme = ((LoopManipulationException) e);
				Target t = lme.getTarget();
				ConfigRuntimeException.HandleUncaughtException(ConfigRuntimeException.CreateUncatchableException("A "
						+ lme.getName() + "() bubbled up to the top of"
						+ " a closure, which is unexpected behavior.", t), environment);
			} catch (FunctionReturnException ex) {
				// Check the return type of the closure to see if it matches the defined type
				// Normal execution.
				Mixed ret = ex.getReturn();
				if(!InstanceofUtil.isInstanceof(ret, returnType, environment)) {
					throw new CRECastException("Expected closure to return a value of type " + returnType.val()
							+ " but a value of type " + ret.typeof() + " was returned instead", ret.getTarget());
				}
				// Now rethrow it
				throw ex;
			} catch (CancelCommandException e) {
				// die()
			} catch (ConfigRuntimeException ex) {
				if(ex instanceof AbstractCREException) {
					((AbstractCREException) ex).freezeStackTraceElements(stManager);
				}
				throw ex;
			} catch (StackOverflowError e) {
				throw new CREStackOverflowError(null, node.getTarget(), e);
			} finally {
				stManager.popStackTraceElement();
			}
			// If we got here, then there was no return type. This is fine, but only for returnType void or auto.
			if(!(returnType.equals(Auto.TYPE) || returnType.equals(CVoid.TYPE))) {
				throw new CRECastException("Expecting closure to return a value of type " + returnType.val() + ","
						+ " but no value was returned.", node.getTarget());
			}
		} catch (CloneNotSupportedException ex) {
			Logger.getLogger(CClosure.class.getName()).log(Level.SEVERE, null, ex);
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
		return CClassType.EMPTY_CLASS_ARRAY;
	}

}
