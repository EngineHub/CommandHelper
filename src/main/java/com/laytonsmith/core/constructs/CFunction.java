

package com.laytonsmith.core.constructs;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 *
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
	 * @throws com.laytonsmith.core.exceptions.ConfigCompileException If the function can't be found.
	 */
	public Function getFunction() throws ConfigCompileException{
		if(function == null){
			function = (Function)FunctionList.getFunction(val(), this.getTarget());
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

	/**
	 * Static utility method for checking to see if a given parse tree node is the
	 * given function.
	 * @param node
	 * @param function
	 * @return
	 */
	public static boolean isFunction(ParseTree node, Class<? extends Function> function){
		if(!(node.getData() instanceof CFunction)){
			return false;
		}
		try {
			Function f = function.newInstance();
			return f.getName().equals(node.getData().val());
		} catch (InstantiationException | IllegalAccessException ex) {
			return false;
		}
	}

}
