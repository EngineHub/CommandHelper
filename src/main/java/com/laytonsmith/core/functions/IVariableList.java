/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.core.constructs.IVariable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Layton
 */
public class IVariableList {
    Map<String, IVariable> varList = new HashMap<String, IVariable>();
    
    public void set(IVariable v){
        varList.put(v.getName(), v);
    }
    
    public IVariable get(String name){
        if(!varList.containsKey(name)){
            this.set(new IVariable(name, 0, null));
        }
        return varList.get(name);
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
    
    @Override
    public IVariableList clone(){
        IVariableList clone = new IVariableList();
        clone.varList = new HashMap<String, IVariable>(varList);
        return clone;
    }
    
    
}
