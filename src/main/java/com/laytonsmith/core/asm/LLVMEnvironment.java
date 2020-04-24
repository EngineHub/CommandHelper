package com.laytonsmith.core.asm;


import com.laytonsmith.core.environments.Environment;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class LLVMEnvironment implements Environment.EnvironmentImpl {

	private final AtomicInteger stringIdCounter = new AtomicInteger();
	private final Map<String, String> strings = new HashMap<>();

	@Override
	public Environment.EnvironmentImpl clone() throws CloneNotSupportedException {
		return this;
	}

	public synchronized String getOrPutStringConstant(String string) {
		if(strings.containsKey(string)) {
			return strings.get(string);
		}
		String id = ".strings." + stringIdCounter.getAndIncrement();
		strings.put(string, id);
		return id;
	}

	public Map<String, String> getStrings() {
		return new HashMap<>(strings);
	}

}
