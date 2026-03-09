package com.laytonsmith.core.environments;

import com.laytonsmith.PureUtilities.ObjectHelpers;
import com.laytonsmith.PureUtilities.ObjectHelpers.StandardField;

import java.io.File;

/**
 * An immutable breakpoint at a specific file and line. Used for O(1) lookup in
 * {@link DebugContext}'s breakpoint set via {@link #equals(Object)} and {@link #hashCode()}.
 *
 * <p>Line numbers are 1-indexed, matching {@link com.laytonsmith.core.constructs.Target#line()}.
 * Currently only file+line are used for identity, but the class is designed to support
 * conditional breakpoints in the future.</p>
 */
public class Breakpoint {

	@StandardField
	private final File file;

	@StandardField
	private final int line;

	/**
	 * Creates a breakpoint at the given file and line.
	 *
	 * @param file The source file. Must not be null.
	 * @param line The 1-indexed line number. Must be positive.
	 */
	public Breakpoint(File file, int line) {
		if(file == null) {
			throw new IllegalArgumentException("Breakpoint file must not be null");
		}
		if(line <= 0) {
			throw new IllegalArgumentException("Breakpoint line must be positive, got " + line);
		}
		this.file = file;
		this.line = line;
	}

	/**
	 * Returns the source file.
	 */
	public File file() {
		return file;
	}

	/**
	 * Returns the 1-indexed line number.
	 */
	public int line() {
		return line;
	}

	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object obj) {
		return ObjectHelpers.DoEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return ObjectHelpers.DoHashCode(this);
	}

	@Override
	public String toString() {
		return file.getName() + ":" + line;
	}
}
