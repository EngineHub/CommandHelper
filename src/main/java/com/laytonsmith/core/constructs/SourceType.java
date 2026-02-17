package com.laytonsmith.core.constructs;

import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 * A SourceType is a type that can be represented in source code.
 */
public interface SourceType extends Mixed {

	/**
	 * Returns true if this type was defined as a variadic type (i.e. `string ...`).
	 *
	 * @return
	 */
	boolean isVariadicType();

	/**
	 * For a non-variadic type, this returns a new instance as a variadic type (i.e. if this represents `string` then
	 * `string ...` is returned).
	 *
	 * @param env
	 * @return
	 */
	SourceType asVariadicType(Environment env);
}
