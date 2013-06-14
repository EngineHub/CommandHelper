package com.laytonsmith.core.environments;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.annotations.api;

/**
 *
 */
public interface RuntimeEnvironment extends Environment.EnvironmentImpl {
	
	/**
	 * Returns the implementation type for this runtime.
	 * @return 
	 */
	Implementation.Type GetImplementation();
	
	/**
	 * Gets the compilation platform for this runtime.
	 * @return 
	 */
	api.Platforms GetPlatform();

}
