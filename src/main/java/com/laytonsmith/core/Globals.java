

package com.laytonsmith.core;

import com.laytonsmith.core.constructs.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author layton
 */
public final class Globals {
    
    private Globals(){}
    
    public static Map<String, Construct> global_ivar = new HashMap<String, Construct>();
    public static Map<String, Construct> global_construct = new HashMap<String, Construct>();
    
    public static synchronized void SetGlobal(IVariable ivar, Construct value){
        Map<String, Construct> vars = global_ivar;//(HashMap<String, IVariable>)env.get("global_ivar");
        vars.put(ivar.getName(), value);
    }
    public static synchronized Construct GetGlobalIVar(IVariable var){
        Map<String, Construct> vars = global_ivar;//(HashMap<String, IVariable>)env.get("global_ivar");
        if(vars.containsKey(var.getName())){
            return vars.get(var.getName());
        } else {
            Construct v = new CString("", Target.UNKNOWN);
            vars.put(var.getName(), v);
            return v;
        }
    }
	
    public static synchronized void SetGlobal(String name, Construct value){
        Map<String, Construct> vars = global_construct;//(HashMap<String, Construct>)env.get("global_construct");
        vars.put(name, value);
    }
    public static synchronized Construct GetGlobalConstruct(String name){
        Map<String, Construct> vars = global_construct;//(HashMap<String, Construct>)env.get("global_construct");
        if(vars.containsKey(name)){
            return vars.get(name);
        } else {
            return Construct.GetNullConstruct(Target.UNKNOWN);
        }
    }
    
    public static synchronized void clear(){
        global_ivar.clear();
        global_construct.clear();
    }
}
