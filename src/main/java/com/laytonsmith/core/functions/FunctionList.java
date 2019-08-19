package com.laytonsmith.core.functions;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.extensions.ExtensionManager;
import java.util.Set;

/**
 *
 */
public class FunctionList {

	/**
	 * Returns the given function in the INTERPRETER_JAVA platform, that applies to the given environments.
	 * If you are doing a meta
	 * operation, you may pass null for envs, and all functions will be returned, but during normal runtime, this should
	 * always be populated.
	 * @param s The function name
	 * @param envs The environments that will be present at runtime. May be null, which bypasses the filtering.
	 * @param t The code target, to indicate where in user code the error occured, if the function isn't found.
	 * @return The selected function
	 * @throws ConfigCompileException If the specified function doesn't exist (or it does exist, but requires an
	 * environment that was not specified in {@code envs}.
	 */
	public static FunctionBase getFunction(String s,
			Set<Class<? extends com.laytonsmith.core.environments.Environment.EnvironmentImpl>> envs, Target t)
			throws ConfigCompileException {
		return getFunction(new CFunction(s, t), envs);
	}

	/**
	 * Returns the given function in the specified platform, that applies to the given environments.
	 * If you are doing a meta
	 * operation, you may pass null for envs, and all functions will be returned, but during normal runtime, this should
	 * always be populated.
	 * @param s The function name
	 * @param platform The compiler platform.
	 * @param envs The environments that will be present at runtime. May be null, which bypasses the filtering.
	 * @param t The code target, to indicate where in user code the error occured, if the function isn't found.
	 * @return The selected function
	 * @throws ConfigCompileException If the specified function doesn't exist (or it does exist, but requires an
	 * environment that was not specified in {@code envs}.
	 */
	public static FunctionBase getFunction(String s, api.Platforms platform,
			Set<Class<? extends com.laytonsmith.core.environments.Environment.EnvironmentImpl>> envs, Target t)
			throws ConfigCompileException {
		return getFunction(new CFunction(s, t), platform, envs);
	}

	/**
	 * Returns the given function in the INTERPETER_JAVA platform, that applies to the given environments.
	 * If you are doing a meta
	 * operation, you may pass null for envs, and all functions will be returned, but during normal runtime, this should
	 * always be populated.
	 * @param c The function name
	 * @param envs The environments that will be present at runtime. May be null, which bypasses the filtering.
	 * @return The selected function
	 * @throws ConfigCompileException If the specified function doesn't exist (or it does exist, but requires an
	 * environment that was not specified in {@code envs}.
	 */
	public static FunctionBase getFunction(CFunction c,
			Set<Class<? extends com.laytonsmith.core.environments.Environment.EnvironmentImpl>> envs)
			throws ConfigCompileException {
		return getFunction(c, api.Platforms.INTERPRETER_JAVA, envs);
	}

	/**
	 * Returns the given function in the specified platform, that applies to the given environments.
	 * If you are doing a meta
	 * operation, you may pass null for envs, and all functions will be returned, but during normal runtime, this should
	 * always be populated.
	 * @param c The function name
	 * @param platform The compiler platform.
	 * @param envs The environments that will be present at runtime. May be null, which bypasses the filtering.
	 * @return The selected function
	 * @throws ConfigCompileException If the specified function doesn't exist (or it does exist, but requires an
	 * environment that was not specified in {@code envs}.
	 */
	public static FunctionBase getFunction(CFunction c, api.Platforms platform,
			Set<Class<? extends com.laytonsmith.core.environments.Environment.EnvironmentImpl>> envs)
			throws ConfigCompileException {
		return ExtensionManager.GetFunction(c, platform, envs);
	}

	/**
	 * Returns a list of functions on the given platform, that apply to the given environments. If you are doing a meta
	 * operation, you may pass null for envs, and all functions will be returned, but during normal runtime, this should
	 * always be populated.
	 * @param platform The compiler platform.
	 * @param envs The environments that will be present at runtime. May be null, which bypasses the filtering.
	 * @return A Set of the selected functions
	 */
	public static Set<FunctionBase> getFunctionList(api.Platforms platform,
			Set<Class<? extends com.laytonsmith.core.environments.Environment.EnvironmentImpl>> envs) {
		return ExtensionManager.GetFunctions(platform, envs);
	}
}
