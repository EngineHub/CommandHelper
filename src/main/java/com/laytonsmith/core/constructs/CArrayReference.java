/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.constructs;

import com.laytonsmith.core.Env;

/**
 *
 * @author Layton
 */
public class CArrayReference extends Construct{
    public Construct array;
    public Construct index;
    public IVariable name = null;
    public CArrayReference(Construct array, Construct index, Env env){
        super("", ConstructType.ARRAY, 0, null);
        this.array = array;
        if(array instanceof CArrayReference){
            this.name = ((CArrayReference)array).name;
        }
        if(!(array instanceof CArray) && !(array instanceof CArrayReference)){
            if(array instanceof IVariable){
                name = (IVariable)array;
                Construct ival = env.GetVarList().get(name.getName(), name.getLineNum(), name.getFile()).ival();
                if(ival instanceof CArray){
                    this.array = ival;
                } else {
                    this.array = new CArray(line_num, file);
                }
            } else {
                this.array = new CArray(line_num, file);
            }
        }
        this.index = index;
    }
    
    @Override
    public String toString(){
        return "(" + array + ") -> " + index;
    }
    
    public Construct getInternalArray(){
        Construct temp = array;
        while(temp instanceof CArrayReference){
            temp = ((CArrayReference)temp).array;
        }
        return temp;
    }
    
    public Construct getInternalIndex(){
        if(!(array instanceof CArrayReference)){
            return index;
        }
        CArrayReference temp = (CArrayReference)array;
        while(temp.array instanceof CArrayReference){
            temp = (CArrayReference)temp.array;
        }
        return temp.index;
    }
}
