package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.DryFunction;
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
	 * Returns the underlying function for this construct.
	 *
	 * @return
	 * @throws com.laytonsmith.core.exceptions.ConfigCompileException
	 */
	public Function getFunction() throws ConfigCompileException {
		if(function == null) {
			function = (Function) FunctionList.getFunction(val(), this.getTarget());
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
	 * Returns true if the data in the ParseTree is a funciton, and is of the specified type.
	 *
	 * @param tree
	 * @param ofType
	 * @return
	 */
	public static boolean IsFunction(ParseTree tree, Class<? extends Function> ofType) {
		return IsFunction(tree.getData(), ofType);
	}

	/**
	 * Checks to see if a ParseTree represents a DryFunction or not.
	 * @param data
	 * @return
	 */
	public static boolean CanDryEval(ParseTree data) {
		if(!(data.getData() instanceof CFunction)) {
			return false;
		}
		try {
			FunctionBase fb = FunctionList.getFunction((CFunction) data.getData());
			if(fb instanceof DryFunction) {
				return true;
			} else {
				return false;
			}
		} catch (ConfigCompileException ex) {
			return false;
		}
	}

	/**
	 * Evaluates a DryFunction, or if this is a primitive Construct, simply returns that.
	 *
	 * @param env The environment
	 * @param data The ParseTree to evaluate.
	 * @return The Mixed that this function evaluates to
	 * @throws ConfigCompileException If the function execution throws a CCE
	 * @throws IllegalArgumentException If the underlying function doesn't represent a DryFunction
	 */
	public static Mixed evaluateDryFunction(Environment env, ParseTree data) throws ConfigCompileException {
		if(!(data.getData() instanceof CFunction) && data.getData() instanceof Construct
				&& data.getChildren().isEmpty()) {
			return data.getData();
		}
		if(!CanDryEval(data)) {
			throw new IllegalArgumentException("Data (" + data.getData() + ") does not contain a DryFunction");
		}
		DryFunction f = (DryFunction) FunctionList.getFunction((CFunction) data.getData());
		return f.dryExec(data.getTarget(), env, data.getChildren().toArray(new ParseTree[data.getChildren().size()]));
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
