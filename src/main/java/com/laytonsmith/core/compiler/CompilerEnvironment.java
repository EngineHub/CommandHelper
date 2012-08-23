package com.laytonsmith.core.compiler;

/**
 * The compiler environment provides compilation settings, or other controller
 * specific values. This allows for separation of the compiler from the general layout
 * of configuration and other files. The compiler environment is available to the runtime
 * environment as well, but contains values that the compiler (or function optimizations)
 * might need, and are usually considered "static". The settings are all passed in to the constructor,
 * but you can use the various factory methods to create an environment from other sources.
 * @author lsmith
 */
public class CompilerEnvironment {	
	
}
