package com.laytonsmith.core.asm;


import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.core.environments.Environment;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class LLVMEnvironment implements Environment.EnvironmentImpl {

	private final AtomicInteger stringIdCounter = new AtomicInteger();
	private final Map<String, String> strings = new HashMap<>();
	private final Set<String> globalDeclarations = new HashSet<>();

	@Override
	public Environment.EnvironmentImpl clone() throws CloneNotSupportedException {
		return this;
	}

	/**
	 * Puts a new constant string in the registry, and returns the ID for it.
	 * @param string
	 * @return
	 */
	public synchronized String getOrPutStringConstant(String string) {
		if(strings.containsKey(string)) {
			return strings.get(string);
		}
		String id = ".strings." + stringIdCounter.getAndIncrement();
		strings.put(string, id);
		return id;
	}

	/**
	 * Returns the list of registered strings, mapping from string -> id
	 * @return
	 */
	public Map<String, String> getStrings() {
		return new HashMap<>(strings);
	}

	// Default to true during initial development, eventually false
	private boolean outputIRCodeTargetLogging = true;

	public boolean isOutputIRCodeTargetLoggingEnabled() {
		return outputIRCodeTargetLogging;
	}

	public void setOutputIRCodeTargetLogging(boolean enabled) {
		this.outputIRCodeTargetLogging = enabled;
	}

	public void addGlobalDeclaration(String emission) {
		globalDeclarations.add(emission);
	}

	public String getGlobalDeclarations() {
		StringBuilder b = new StringBuilder();
		for(String gd : globalDeclarations) {
			b.append(gd).append(OSUtils.GetLineEnding());
		}
		return b.toString();
	}

}
