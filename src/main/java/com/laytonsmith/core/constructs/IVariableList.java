

package com.laytonsmith.core.constructs;

import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.LogLevel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 *
 */
public class IVariableList {
    Map<String, IVariable> varList = new HashMap<>();

	/**
	 * Removes a value from the variable table
	 * @param name
	 */
	public void remove(String name){
		varList.remove(name);
	}

    public void set(IVariable v){
        varList.put(v.getVariableName(), v);
    }

    public IVariable get(String name, Target t, boolean bypassAssignedCheck){
        if(!varList.containsKey(name)){
            this.set(new IVariable(CClassType.AUTO, name, CNull.UNDEFINED, t));
        }
		IVariable v = varList.get(name);

		// TODO: Once the compiler can handle this, this check should be moved out of here,
		// and moved into the compiler. In strict mode, it will be a compiler error, in
		// non-strict mode it will be a compiler warning.
		// ==, not .equals
		if(v.ival() == CNull.UNDEFINED && !bypassAssignedCheck){
			CHLog.GetLogger().Log(CHLog.Tags.RUNTIME, LogLevel.ERROR, "Using undefined variable: " + name, t);
		}
        v.setTarget(t);
        return v;
    }

	public IVariable get(String name, Target t){
		return get(name, t, false);
	}

	/**
	 * Returns true if the variable table already has this value defined
	 * @param name
	 * @return
	 */
	public boolean has(String name){
		return varList.containsKey(name);
	}

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("[");
        boolean first = true;
        for(Map.Entry<String, IVariable> entry : varList.entrySet()){
            IVariable iv = entry.getValue();
            if(first){
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
    public IVariableList clone(){
        IVariableList clone = new IVariableList();
        clone.varList = new HashMap<String, IVariable>(varList);
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
