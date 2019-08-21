package com.laytonsmith.core;

import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;

/**
 * Contains utility methods for dealing with strict mode.
 */
public class StrictMode {

	/**
	 * There are a couple of factors that go into determining if strict mode is enabled or not. This method
	 * combines all the reasons into a single function call.
	 * @param fileOptions This parameter is mandatory, and should be provided by the compiler or other source object.
	 * @param environment This parameter is optional, and may be set to null. This is going to be the case on the first
	 * compiler pass, but in dynamic code instances, may be present.
	 * @param t The code target, for error messages
	 * @return True if strict mode rules should be followed for this object.
	 */
	public static boolean isStrictMode(FileOptions fileOptions, Environment environment, Target t) {
		boolean runtimeSetting = false;
		if(environment != null) {
			GlobalEnv env = environment.getEnv(GlobalEnv.class);
			runtimeSetting = env.GetRuntimeSetting("system.strict_mode.enabled", false, t);
		}
		return fileOptions.isStrict() || runtimeSetting;
	}
}
