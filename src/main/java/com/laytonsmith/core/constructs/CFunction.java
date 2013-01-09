

package com.laytonsmith.core.constructs;

import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;

/**
 *
 * @author layton
 */
public class CFunction extends Construct {
    
    public static final long serialVersionUID = 1L;
	private transient Function function;

    public CFunction(String name, Target t) {
        super(name, ConstructType.FUNCTION, t);
    }

    @Override
    public String toString() {
        return getValue();
    }
    
    @Override
    public CFunction clone() throws CloneNotSupportedException{
        return (CFunction) super.clone();
    }

    @Override
    public boolean isDynamic() {
        return true;
    }
	
	/**
	 * Returns the underlying function for this construct.
	 * @return 
	 */
	public Function getFunction(){
		if(function == null){
			try {
				function = (Function)FunctionList.getFunction(val());
			} catch (ConfigCompileException ex) {
				//Shouldn't ever get here?
				throw new Error(ex);
			}
		}
		return function;
	}
	
	/**
	 * This function should only be called by the compiler.
	 * @param f 
	 */
	public void setFunction(FunctionBase f){
		function = (Function)f;
	}

	public boolean isProcedure() {
		//For now, just see if it starts with _. Later, this
		//will be made a bit more robust.
		return val().startsWith("_");
	}
	
}
