package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.AbstractCREException;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
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
 *
 */
@typeof("ms.lang.iclosure")
public class CIClosure extends CClosure {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get(CIClosure.class);

	public CIClosure(ParseTree node, Environment env, CClassType returnType, String[] names, Mixed[] defaults,
			CClassType[] types, Target t) {
		super(node, env, returnType, names, defaults, types, t);
	}

	@Override
	public void execute(Mixed... values)
			throws ConfigRuntimeException, ProgramFlowManipulationException, FunctionReturnException,
			CancelCommandException {
		if(node == null) {
			return;
		}
		StackTraceManager stManager = env.getEnv(GlobalEnv.class).GetStackTraceManager();
		stManager.addStackTraceElement(new ConfigRuntimeException.StackTraceElement("<<iclosure>>", getTarget()));
		try {
			Environment environment;
			synchronized(this) {
				boolean prev = env.getEnv(GlobalEnv.class).getCloneVars();
				env.getEnv(GlobalEnv.class).setCloneVars(false);
				environment = env.clone();
				env.getEnv(GlobalEnv.class).setCloneVars(prev);
			}
			environment.getEnv(GlobalEnv.class).setCloneVars(true);
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
	public String docs() {
		return "An iclosure is an isolated scope closure. This is more efficient than a regular closure, but it"
				+ " doesn't allow"
				+ " for access of variables outside of the scope of the closure, other than values passed in.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_1;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{CClosure.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return CClassType.EMPTY_CLASS_ARRAY;
	}

}
