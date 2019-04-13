package com.laytonsmith.core;

import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import java.util.Objects;

/**
 * An UnqualifiedClassName is a class name that is potentially not fully qualified. There is no stipulation that the
 * class be able to be resolved at this point, but by the time compilation is completed, this should have been resolved
 * into a fully qualified class name, to ensure that a reference to the class actually does exist. But it may remain in
 * an unqualified state during compile time, before all the classes are defined.
 */
public class UnqualifiedClassName {

	private final String unqualifedClassName;
	private final Target target;
	private final FullyQualifiedClassName fqcn;

	/**
	 * Constructs a new UnqualifiedClassName.
	 * @param unqualifiedClassName The potentially unqualified class name
	 * @param target The code target where this is being defined.
	 */
	public UnqualifiedClassName(String unqualifiedClassName, Target target) {
		Objects.requireNonNull(unqualifiedClassName, "unqualifiedClassName may not be null");
		this.unqualifedClassName = unqualifiedClassName;
		this.target = target;
		this.fqcn = null;
	}

	/**
	 * In some cases, you may need an unqualified class object, but already have fully qualified it. In that case, you
	 * can use this constructor, and this will store the FullyQualifiedClassName internally, and simply return that
	 * if {@link #getFQCN(com.laytonsmith.core.environments.Environment)} is called.
	 * @param fqcn
	 */
	public UnqualifiedClassName(FullyQualifiedClassName fqcn) {
		Objects.requireNonNull(fqcn, "fqcn may not be null");
		this.unqualifedClassName = null;
		this.target = null;
		this.fqcn = fqcn;
	}

	/**
	 * Gets the unqualified class name as a string.
	 * @return
	 */
	public String getUnqualifiedClassName() {
		return this.unqualifedClassName == null ? fqcn.getFQCN() : this.unqualifedClassName;
	}

	/**
	 * Where this class usage was defined
	 * @return
	 */
	public Target getTarget() {
		return target;
	}

	/**
	 * Given the compiler environment and code target, attempts to fully qualify the class name, and returns a fully
	 * qualified class name.
	 * @param env The environment, which must include a compiler environment.
	 * @return The fully qualified class name
	 * @throws CRECastException If the class name could not be resolved. This is guaranteed to never be thrown if the
	 * object was originally constructed with a FullyQualifiedClassName, as it will simply be returned as is.
	 */
	public FullyQualifiedClassName getFQCN(Environment env) throws CRECastException {
		return this.fqcn != null ? this.fqcn : FullyQualifiedClassName.forName(unqualifedClassName, target, env);
	}
}
