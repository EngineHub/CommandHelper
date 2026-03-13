package com.laytonsmith.core.environments;

import com.laytonsmith.PureUtilities.ObjectHelpers;
import com.laytonsmith.PureUtilities.ObjectHelpers.StandardField;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;

import java.io.File;

/**
 * A breakpoint at a specific file and line. Used for O(1) lookup in
 * {@link DebugContext}'s breakpoint set via {@link #equals(Object)} and {@link #hashCode()}.
 *
 * <p>Line numbers are 1-indexed, matching {@link com.laytonsmith.core.constructs.Target#line()}.
 * Identity is based on file+line only; condition and hit count are not part of equality.</p>
 */
public class Breakpoint {

	@StandardField
	private final File file;

	@StandardField
	private final int line;

	private final String condition;
	private final ParseTree compiledCondition;
	private final int hitCountThreshold;
	private int hitCount;

	/**
	 * Creates an unconditional breakpoint at the given file and line.
	 *
	 * @param file The source file. Must not be null.
	 * @param line The 1-indexed line number. Must be positive.
	 */
	public Breakpoint(File file, int line) {
		this(file, line, null, 0);
	}

	/**
	 * Creates a breakpoint with optional condition and hit count threshold.
	 * If a condition is provided, it is compiled immediately so that syntax
	 * errors are caught early.
	 *
	 * @param file The source file. Must not be null.
	 * @param line The 1-indexed line number. Must be positive.
	 * @param condition A MethodScript expression to evaluate, or null for unconditional.
	 * @param hitCountThreshold Number of times the breakpoint must be hit before pausing
	 *                          (0 means pause on first hit).
	 * @throws IllegalArgumentException if the condition cannot be compiled
	 */
	public Breakpoint(File file, int line, String condition, int hitCountThreshold) {
		if(file == null) {
			throw new IllegalArgumentException("Breakpoint file must not be null");
		}
		if(line <= 0) {
			throw new IllegalArgumentException("Breakpoint line must be positive, got " + line);
		}
		this.file = file;
		this.line = line;
		this.condition = condition;
		this.hitCountThreshold = hitCountThreshold;
		this.hitCount = 0;
		if(condition != null && !condition.isEmpty()) {
			try {
				this.compiledCondition = MethodScriptCompiler.compile(
						MethodScriptCompiler.lex(condition, null, null, true),
						null, Environment.getDefaultEnvClasses());
			} catch(ConfigCompileException | ConfigCompileGroupException e) {
				throw new IllegalArgumentException("Invalid breakpoint condition: " + e.getMessage(), e);
			}
		} else {
			this.compiledCondition = null;
		}
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

	/**
	 * Returns the condition expression source, or null if unconditional.
	 */
	public String condition() {
		return condition;
	}

	/**
	 * Returns the pre-compiled condition parse tree, or null if unconditional.
	 */
	public ParseTree compiledCondition() {
		return compiledCondition;
	}

	/**
	 * Returns the hit count threshold (0 means always pause).
	 */
	public int hitCountThreshold() {
		return hitCountThreshold;
	}

	/**
	 * Returns true if this breakpoint has a condition or hit count threshold.
	 */
	public boolean isConditional() {
		return compiledCondition != null || hitCountThreshold > 0;
	}

	/**
	 * Increments and returns the current hit count.
	 */
	public int incrementHitCount() {
		return ++hitCount;
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
