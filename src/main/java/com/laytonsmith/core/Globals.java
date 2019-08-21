package com.laytonsmith.core;

import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a global registry for values. This is used by the import/export system.
 */
public final class Globals {

	private Globals() {
	}

	private static final Map<String, Mixed> GLOBAL_CONSTRUCT = new HashMap<>();

	/**
	 * Sets a variable in the global registry.
	 *
	 * @param name The value name
	 * @param value The value itself
	 */
	public static synchronized void SetGlobal(String name, Mixed value) {
		Map<String, Mixed> vars = GLOBAL_CONSTRUCT; // (HashMap<String, Construct>)env.get("global_construct");
		if(value instanceof CNull) {
			vars.remove(name);
		} else {
			vars.put(name, value);
		}
	}

	/**
	 * Returns a value previously stored in the global registry. If the value hasn't been set before, CNull is returned.
	 * Regardless, a valid Construct is always returned, never null.
	 *
	 * @param name The name of the value to return.
	 * @return the construct stored at this name, or CNull if none exists
	 */
	public static synchronized Mixed GetGlobalConstruct(String name) {
		Map<String, Mixed> vars = GLOBAL_CONSTRUCT; // (HashMap<String, Construct>)env.get("global_construct");
		return vars.getOrDefault(name, CNull.NULL);
	}

	/**
	 * Clears out all the values in the registry.
	 */
	public static synchronized void clear() {
		GLOBAL_CONSTRUCT.clear();
	}
}
