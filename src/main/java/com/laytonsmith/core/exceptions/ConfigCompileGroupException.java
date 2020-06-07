package com.laytonsmith.core.exceptions;

import java.util.Set;
import java.util.TreeSet;

/**
 *
 */
public class ConfigCompileGroupException extends Exception {

	private final Set<ConfigCompileException> list;

	public ConfigCompileGroupException(Set<ConfigCompileException> group) {
		this.list = new TreeSet<>(group);
	}

	public Set<ConfigCompileException> getList() {
		return new TreeSet<>(list);
	}
}
