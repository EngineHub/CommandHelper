

package com.laytonsmith.core;

import com.laytonsmith.core.constructs.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a global registry for values. This is used by the import/export system.
 */
public final class Globals {
    
    private Globals(){}
    
    public static Map<String, IVariable> global_ivar = new HashMap<String, IVariable>();
    public static Map<String, Construct> global_construct = new HashMap<String, Construct>();
    
	/**
	 * Sets a global variable. The ivar works as both the key and the value.
	 * @param ivar 
	 * @deprecated Use {@link #SetGlobal(java.lang.String, com.laytonsmith.core.constructs.Construct)}
	 * instead. This method will be removed in future versions.
	 */
    public static synchronized void SetGlobal(IVariable ivar){
        Map<String, IVariable> vars = global_ivar;//(HashMap<String, IVariable>)env.get("global_ivar");
        vars.put(ivar.getName(), ivar);
    }
	
	/**
	 * Gets a global variable. The ivar returned works as both the key and the value.
	 * @param var
	 * @return 
	 * @deprecated Use {@link #GetGlobalConstruct(java.lang.String)} instead. This method will
	 * be removed in future versions.
	 */
    public static synchronized IVariable GetGlobalIVar(IVariable var){
        Map<String, IVariable> vars = global_ivar;//(HashMap<String, IVariable>)env.get("global_ivar");
        if(vars.containsKey(var.getName())){
            return vars.get(var.getName());
        } else {
            IVariable v = new IVariable(var.getName(), new CString("", Target.UNKNOWN), Target.UNKNOWN);
            vars.put(v.getName(), v);
            return v;
        }
    }
	
	/**
	 * Sets a variable in the global registry.
	 * @param name The value name
	 * @param value The value itself
	 */
    public static synchronized void SetGlobal(String name, Construct value){
        Map<String, Construct> vars = global_construct;//(HashMap<String, Construct>)env.get("global_construct");
        vars.put(name, value);
    }
	
	/**
	 * Returns a value previously stored in the global registry. If the value
	 * hasn't been set before, CNull is returned. Regardless, a valid Construct
	 * is always returned, never null.
	 * @param name The name of the value to return.
	 * @return 
	 */
    public static synchronized Construct GetGlobalConstruct(String name){
        Map<String, Construct> vars = global_construct;//(HashMap<String, Construct>)env.get("global_construct");
        if(vars.containsKey(name)){
            return vars.get(name);
        } else {
            return new CNull();
        }
    }
    
	/**
	 * Clears out all the values in the registry.
	 */
    public static synchronized void clear(){
        global_ivar.clear();
        global_construct.clear();
    }
}
