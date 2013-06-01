

package com.laytonsmith.core.constructs;

import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 * @author Layton
 */
public class CArrayReference extends Construct{
    public Mixed array;
    public Mixed index;
    public IVariable name = null;
    public CArrayReference(Construct array, Mixed index, Environment env){
        super("", Target.UNKNOWN);
        this.array = array;
        if(array instanceof CArrayReference){
            this.name = ((CArrayReference)array).name;
        }
        if(!(array instanceof CArray) && !(array instanceof CArrayReference)){
            if(array instanceof IVariable){
                name = (IVariable)array;
                Mixed ival = env.getEnv(GlobalEnv.class).GetVarList().get(name, name.getTarget());
                if(ival instanceof CArray){
                    this.array = ival;
                } else {
                    this.array = new CArray(getTarget());
                }
            } else {
                this.array = new CArray(getTarget());
            }
        }
        this.index = index;
    }
    
    @Override
    public String toString(){
        return "(" + array + ") -> " + index;
    }
    
    public Mixed getInternalArray(){
        Mixed temp = array;
        while(temp instanceof CArrayReference){
            temp = ((CArrayReference)temp).array;
        }
        return temp;
    }
    
    public Mixed getInternalIndex(){
        if(!(array instanceof CArrayReference)){
            return index;
        }
        CArrayReference temp = (CArrayReference)array;
        while(temp.array instanceof CArrayReference){
            temp = (CArrayReference)temp.array;
        }
        return temp.index;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

	public String typeName() {
		return "$array_reference";
	}
}
