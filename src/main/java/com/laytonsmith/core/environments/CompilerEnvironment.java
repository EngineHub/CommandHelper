package com.laytonsmith.core.environments;

/**
 * A CompilerEnvironment is available only at compile time, and contains compilation
 * specific data.
 */
public class CompilerEnvironment implements Environment.EnvironmentImpl, Cloneable {



	@Override
	public Environment.EnvironmentImpl clone() throws CloneNotSupportedException {
		CompilerEnvironment clone = (CompilerEnvironment) super.clone();

		return clone;
	}

}
