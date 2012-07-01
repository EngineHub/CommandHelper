/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.constructs;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Layton
 */
public class IVariableList {
    Map<String, IVariable> varList = new HashMap<String, IVariable>();
    
    @Override
    public IVariableList clone(){
        IVariableList clone = new IVariableList();
        clone.varList = new HashMap<String, IVariable>(varList);
        return clone;
    }
    
    public IVariable get(String name, Target t){
        if(!varList.containsKey(name)){
            this.set(new IVariable(name, t));
        }
        varList.get(name).setTarget(t);
        return varList.get(name);
    }

    //only the reflection package should be accessing this
    public Set<String> keySet() {
        return varList.keySet();
    }
    
    public void set(IVariable v){
        varList.put(v.getName(), v);
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
            b.append(iv.getName()).append(":").append("(").append(iv.ival().getClass().getSimpleName()).append(")").append(iv.ival().val());
        }
        b.append("]");
        return b.toString();
    }
    
    
}
