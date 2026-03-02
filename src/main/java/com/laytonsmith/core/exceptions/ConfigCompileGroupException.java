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

	/**
	 * Creates a new ConfigCompileGroupException. This wraps a group of exceptions, for exceptions which
	 * don't need to stop compilation, but mean that compilation has failed.
	 * @param group The group of ConfigCompileExceptions
	 * @param cause The cause, if there is one. In general, this can only support a single Throwable instance,
	 * because Java won't allow multiple causes. However, this may be useful to simply provide the cause of
	 * the first one, if applicable, so that they can at least be burned down one at a time, if the problem
	 * is due to native code.
	 */
	public ConfigCompileGroupException(Set<ConfigCompileException> group, Throwable cause) {
		super(cause);
		this.list = new TreeSet<>(group);
	}

	/**
	 * Creates a new ConfigCompileGroupException. This wraps a group of exceptions, for exceptions which
	 * don't need to stop compilation, but mean that compilation has failed.
	 * @param group The group of ConfigCompileExceptions
	 */
	public ConfigCompileGroupException(Set<ConfigCompileException> group) {
		this(group, null);
	}

	/**
	 * Returns the list of underlying ConfigCompileExceptions.
	 * @return
	 */
	public Set<ConfigCompileException> getList() {
		return new TreeSet<>(this.list);
	}
}
