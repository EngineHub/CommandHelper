package com.laytonsmith.core.exceptions;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class ConfigCompileGroupException extends Exception {

	private final Set<ConfigCompileException> list;

	public ConfigCompileGroupException(Set<ConfigCompileException> group) {
		this.list = new HashSet<>(group);
	}

	public Set<ConfigCompileException> getList() {
		return new HashSet<>(list);
	}
}
