package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;
import com.laytonsmith.core.natives.interfaces.Mixed;

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
		return val();
	}

	@Override
	public CFunction clone() throws CloneNotSupportedException {
		return (CFunction) super.clone();
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	/**
	 * Returns true if this CFunction is expected to represent a procedure based on the format.
	 *
	 * @return
	 */
	public boolean hasProcedure() {
		return val().charAt(0) == '_' && val().charAt(1) != '_';
	}

	/**
	 * Returns true if this CFunction is expected to represent an IVariable based on the format.
	 *
	 * @return
	 */
	public boolean hasIVariable() {
		return val().charAt(0) == '@';
	}

	/**
	 * Returns true if this CFunction is expected to represent a function based on the format.
	 *
	 * @return
	 */
	public boolean hasFunction() {
		return !hasProcedure() && !hasIVariable();
	}

	/**
	 * Returns the underlying function for this construct.
	 *
	 * @return
	 * @throws com.laytonsmith.core.exceptions.ConfigCompileException
	 */
	public Function getFunction() throws ConfigCompileException {
		if(function == null) {
			function = (Function) FunctionList.getFunction(val(), null, this.getTarget());
		}
		return function;
	}

	/**
	 * This function should only be called by the compiler.
	 *
	 * @param f
	 */
	public void setFunction(FunctionBase f) {
		function = (Function) f;
	}

	/**
	 * Returns true if the Construct is a function, and is of the specified type.
	 *
	 * @param possibleFunction
	 * @param ofType
	 * @return
	 */
	public static boolean IsFunction(Mixed possibleFunction, Class<? extends Function> ofType) {
		Function f = ReflectionUtils.newInstance(ofType);
		return possibleFunction instanceof CFunction && possibleFunction.val().equals(f.getName());
	}

	/**
	 * Returns true if the data in the ParseTree is a function, and is of the specified type.
	 *
	 * @param tree
	 * @param ofType
	 * @return
	 */
	public static boolean IsFunction(ParseTree tree, Class<? extends Function> ofType) {
		return IsFunction(tree.getData(), ofType);
	}

	@Override
	public Version since() {
		return super.since();
	}

	@Override
	public String docs() {
		return super.docs();
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{Mixed.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return new CClassType[]{};
	}

}
