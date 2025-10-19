package com.laytonsmith.core.constructs;

import com.laytonsmith.core.Finalizable;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 *
 */
public class IVariableList {

	private final IVariableList parentScope;

	/**
	 * Creates a new IVariableList.
	 * @param parentScope The parent scope, used when freeing variables. If this is the top level scope, this
	 *                    can be null.
	 */
	public IVariableList(IVariableList parentScope) {
		this.parentScope = parentScope;
	}

	Map<String, IVariable> varList = new HashMap<>();

	public void freeValue(Mixed value) {
		// TODO: If assign adds backreferences, then this can be made more efficient. Research needed.
		List<String> toRemove = new ArrayList<>();
		for(String ivarS : keySet()) {
			IVariable ivar = varList.get(ivarS);
			// ==, not equals, we only want to clear it if it's the same exact reference.
			if(ivar.ival() == value) {
				toRemove.add(ivar.getVariableName());
			}
		}

		for(String ivarS : toRemove) {
			IVariable ivar = varList.get(ivarS);
			remove(ivar.getVariableName());
			ivar.setIval(CNull.NULL);
			set(ivar);
		}

		if(parentScope != null) {
			parentScope.freeValue(value);
		} else {
			if(value instanceof Finalizable f) {
				try {
					f.msFinalize();
				} catch (RuntimeException ex) {
					if(Prefs.DebugMode() || Prefs.ScreamErrors()) {
						ex.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Removes a value from the variable table
	 *
	 * @param name
	 */
	public void remove(String name) {
		varList.remove(name);
	}

	public void set(IVariable v) {
		varList.put(v.getVariableName(), v);
	}

	public IVariable get(String name, Target t, boolean bypassAssignedCheck, Environment env) {
		IVariable v = varList.get(name);
		if(v == null) {
			v = new IVariable(Auto.TYPE, name, CNull.UNDEFINED, t);
			this.set(v);
		}

		// TODO: Once the compiler can handle this, this check should be moved out of here,
		// and moved into the compiler. In strict mode, it will be a compiler error, in
		// non-strict mode it will be a compiler warning.
		// ==, not .equals
		if(v.ival() == CNull.UNDEFINED && !bypassAssignedCheck
				&& env.getEnv(GlobalEnv.class).GetFlag(GlobalEnv.FLAG_NO_CHECK_UNDEFINED) == null) {
			MSLog.GetLogger().Log(MSLog.Tags.RUNTIME, LogLevel.ERROR, "Using undefined variable: " + name, t);
		}
		v.setTarget(t);
		return v;
	}

	public IVariable get(String name, Target t, Environment env) {
		return get(name, t, false, env);
	}

	/**
	 * Returns true if the variable table already has this value defined
	 *
	 * @param name
	 * @return
	 */
	public boolean has(String name) {
		return varList.containsKey(name);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("[");
		boolean first = true;
		for(Map.Entry<String, IVariable> entry : varList.entrySet()) {
			IVariable iv = entry.getValue();
			if(first) {
				first = false;
			} else {
				b.append(", ");
			}
			b.append(iv.getVariableName()).append(":").append("(").append(iv.ival().getClass().getSimpleName()).append(")").append(iv.ival().val());
		}
		b.append("]");
		return b.toString();
	}

	@Override
	public IVariableList clone() {
		IVariableList clone = new IVariableList(this);
		clone.varList = new HashMap<>(varList);
		return clone;
	}

	/**
	 * only the reflection package should be accessing this
	 */
	public Set<String> keySet() {
		return varList.keySet();
	}

	/**
	 * Clears the IVariableList of all variables.
	 */
	public void clear() {
		varList.clear();
	}

}
