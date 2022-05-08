package com.laytonsmith.core.constructs;

import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 * A SourceType is a type that can be represented in source code.
 */
public interface SourceType extends Mixed {

	boolean isVariadicType();

	SourceType asVariadicType(Environment env);
}
