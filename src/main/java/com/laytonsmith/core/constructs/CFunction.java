package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.annotations.api;

import java.util.Set;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.Environment.EnvironmentImpl;
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
	private transient Function function = null;
	private transient Class<? extends EnvironmentImpl>[] envImpls = null;

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
	 * Returns true if this CFunction is expected to represent a function based on the format.
	 *
	 * @return
	 */
	public boolean hasFunction() {
		return !hasProcedure();
	}

	/**
	 * Returns the underlying function for this construct and caches it if it exists.
	 *
	 * @return
	 * @throws ConfigCompileException If the specified function doesn't exist.
	 */
	public Function getFunction() throws ConfigCompileException {
		if(this.function == null) {
			this.function = (Function) FunctionList.getFunction(val(), null, this.getTarget());
		}
		return this.function;
	}

	/**
	 * Returns the underlying function for this construct from the cache (which is cached on calling
	 * {@link #getFunction()} or {@link #setFunction(FunctionBase)}).
	 * @return The cached {@link Function} or {@code null} when the function has not yet been cached or doesn't exist.
	 */
	public Function getCachedFunction() {
		return this.function;
	}

	/**
	 * Returns the underlying function for this construct from the cache (which is cached on calling
	 * {@link #getFunction()} or {@link #setFunction(FunctionBase)}) if the function is known given the passed
	 * environments.
	 * @param envs - The environments.
	 * @return The cached {@link Function} or {@code null} when the function has not yet been cached or doesn't exist.
	 */
	public Function getCachedFunction(Set<Class<? extends Environment.EnvironmentImpl>> envs) {

		// Return null if the function has not yet been cached or does not exist.
		if(this.function == null) {
			return null;
		}

		// Cache the environments from the @api annotation if they have not yet been cached.
		if(this.envImpls == null) {
			api api = this.function.getClass().getAnnotation(api.class);
			this.envImpls = api.environments();
		}

		// Return the cached function if it is known given the passed environments.
		for(Class<? extends Environment.EnvironmentImpl> envImpl : this.envImpls) {
			if(!envs.contains(envImpl)) {
				return null;
			}
		}
		return this.function;
	}

	/**
	 * This function should only be called by the compiler.
	 *
	 * @param f
	 */
	public void setFunction(FunctionBase f) {
		this.function = (Function) f;
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
