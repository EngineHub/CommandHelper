package com.laytonsmith.core;

import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 *
 */
public interface PlatformResolver {

	public String outputConstant(Mixed c, Environment env);
}
