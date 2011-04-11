/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.Constructs.IVariable;
import java.util.ArrayList;

/**
 *
 * @author Layton
 */
public class IVariableList {
    ArrayList<IVariable> varList = new ArrayList<IVariable>();
    
    public void set(IVariable v){
        boolean set = false;
        for(int i = 0; i < varList.size(); i++){
            IVariable l = varList.get(i);
            if(l.name.equals(v.name)){
                varList.set(i, v);
                set = true;
                break;
            }
        }
        if(!set){
            varList.add(v);
        }
    }
    
    public IVariable get(String name){
        for(int i = 0; i < varList.size(); i++){
            IVariable l = varList.get(i);
            if(l.name.equals(name)){
                return l;
            }
        }
        IVariable v = new IVariable(name, 0);
        varList.add(v);
        return v;
    }
}
