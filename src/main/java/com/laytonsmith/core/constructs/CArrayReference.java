

package com.laytonsmith.core.constructs;

import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;

/**
 *
 * @author Layton
 */
public class CArrayReference extends Construct{
    public Construct array;
    public Construct index;
    public IVariable name = null;
    public CArrayReference(Construct array, Construct index, Environment env){
        super("", Target.UNKNOWN);
        this.array = array;
        if(array instanceof CArrayReference){
            this.name = ((CArrayReference)array).name;
        }
        if(!(array instanceof CArray) && !(array instanceof CArrayReference)){
            if(array instanceof IVariable){
                name = (IVariable)array;
                Construct ival = env.getEnv(GlobalEnv.class).GetVarList().get(name, name.getTarget());
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

    @Override
    public boolean isDynamic() {
        return false;
    }

	public String typeName() {
		return "$array_reference";
	}
}
