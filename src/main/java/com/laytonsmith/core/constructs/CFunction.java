

package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;

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
	 * @throws com.laytonsmith.core.exceptions.ConfigCompileException
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
	 * Returns true if the Construct is a function, and is of the specified type.
	 * @param possibleFunction
	 * @param ofType
	 * @return
	 */
	public static boolean IsFunction(Construct possibleFunction, Class<? extends Function> ofType){
		Function f = ReflectionUtils.newInstance(ofType);
		return possibleFunction instanceof CFunction && possibleFunction.val().equals(f.getName());
	}

	/**
	 * Returns true if the data in the ParseTree is a funciton, and is of the specified type.
	 * @param tree
	 * @param ofType
	 * @return
	 */
	public static boolean IsFunction(ParseTree tree, Class<? extends Function> ofType){
		return IsFunction(tree.getData(), ofType);
	}

}
