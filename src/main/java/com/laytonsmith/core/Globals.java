/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core;

import com.laytonsmith.core.constructs.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author layton
 */
public class Globals {
    
    public static Map<String, Construct> global_construct = new HashMap<String, Construct>();
    public static Map<String, IVariable> global_ivar = new HashMap<String, IVariable>();
    
    public static synchronized void clear(){
        global_ivar.clear();
        global_construct.clear();
    }
    public static synchronized Construct GetGlobalConstruct(String name){
        Map<String, Construct> vars = global_construct;//(HashMap<String, Construct>)env.get("global_construct");
        if(vars.containsKey(name)){
            return vars.get(name);
        } else {
            return new CNull();
        }
    }
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
    public static synchronized void SetGlobal(IVariable ivar){
        Map<String, IVariable> vars = global_ivar;//(HashMap<String, IVariable>)env.get("global_ivar");
        vars.put(ivar.getName(), ivar);
    }
    
    public static synchronized void SetGlobal(String name, Construct value){
        Map<String, Construct> vars = global_construct;//(HashMap<String, Construct>)env.get("global_construct");
        vars.put(name, value);
    }
}
