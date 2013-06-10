

package com.laytonsmith.core;

import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author layton
 */
public final class Globals {
    
    private Globals(){}
    
    public static Map<String, Mixed> global_ivar = new HashMap<String, Mixed>();
    public static Map<String, Mixed> global_construct = new HashMap<String, Mixed>();
    
    public static synchronized void SetGlobal(IVariable ivar, Mixed value){
        Map<String, Mixed> vars = global_ivar;//(HashMap<String, IVariable>)env.get("global_ivar");
        vars.put(ivar.getName(), value);
    }
    public static synchronized Mixed GetGlobalIVar(IVariable var){
        Map<String, Mixed> vars = global_ivar;//(HashMap<String, IVariable>)env.get("global_ivar");
        if(vars.containsKey(var.getName())){
            return vars.get(var.getName());
        } else {
            Construct v = new CString("", Target.UNKNOWN);
            vars.put(var.getName(), v);
            return v;
        }
    }
	
    public static synchronized void SetGlobal(String name, Mixed value){
        Map<String, Mixed> vars = global_construct;//(HashMap<String, Construct>)env.get("global_construct");
        vars.put(name, value);
    }
    public static synchronized Mixed GetGlobalConstruct(String name){
        Map<String, Mixed> vars = global_construct;//(HashMap<String, Construct>)env.get("global_construct");
        if(vars.containsKey(name)){
            return vars.get(name);
        } else {
            return null;
        }
    }
    
    public static synchronized void clear(){
        global_ivar.clear();
        global_construct.clear();
    }
}
