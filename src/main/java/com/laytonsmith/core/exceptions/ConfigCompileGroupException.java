package com.laytonsmith.core.exceptions;

import java.util.Set;
import java.util.TreeSet;

/**
 * This {@link Exception} can be used to bundle multiple {@link ConfigCompileException}s together for cases were
 * multiple compile exceptions have occurred during compilation.
 */
@SuppressWarnings("serial")
public class ConfigCompileGroupException extends AbstractCompileException {

	private final Set<ConfigCompileException> list;

	public ConfigCompileGroupException(Set<ConfigCompileException> group) {
		super();
		this.list = new TreeSet<>(group);
	}

	public Set<ConfigCompileException> getList() {
		return new TreeSet<>(this.list);
	}
}
