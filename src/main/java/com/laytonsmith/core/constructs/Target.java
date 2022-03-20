package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Common.StackTraceUtils;
import java.io.File;

/**
 * A target allows code to be exactly identified in source. It contains the line number, file, and column that this
 * target represents.
 *
 *
 */
public class Target implements Comparable<Target> {

	/**
	 * For efficiency sake, since Targets are immutable, if you intend on using a blank target, that is to say, one that
	 * has been manufactured in source, you must use this target instead. If a null target is attempted to be created
	 * elsewhere, an exception is thrown.
	 */
	public static final Target UNKNOWN = new Target(0, null, 0, null);
	private final int line;
	private final File file;
	private final int col;
	private int length = 1;
	private boolean lengthSet = false;
	private String originalSet = null;

	private static final boolean IS_DEBUG = java.lang.management.ManagementFactory.getRuntimeMXBean()
			.getInputArguments().toString().contains("jdwp");

	private Target(int line, File file, int col, String ignored) {
		this.line = line;
		this.file = file;
		this.col = col;
	}

	/**
	 * Creates a new target that represents a location in a source file.
	 *
	 * @param line The line the token is defined on. 1 indexed.
	 * @param file The file the token is defined in.
	 * @param col The column the token starts on. 1 indexed.
	 */
	public Target(int line, File file, int col) {
		if(line == 0 && col == 0 && file == null) {
			throw new RuntimeException("For efficiency sake, use Target.UNKNOWN instead of constructing"
					+ " a new Target.");
		}
		this.line = line;
		this.file = file;
		this.col = col;
	}

	/**
	 * Returns the line number
	 *
	 * @return
	 */
	public int line() {
		return line;
	}

	/**
	 * Returns the file. (May exist in a zip file, if the contents need to be read, you should use {@link ZipReader})
	 *
	 * @return
	 */
	public File file() {
		return file;
	}

	/**
	 * Return the column.
	 *
	 * @return
	 */
	public int col() {
		return col;
	}

	/**
	 * The length of the token.
	 *
	 * @return
	 */
	public int length() {
		return length;
	}

	/**
	 * Sets the length of the token. (Defaults to 1.) Returns {@code this} for easy chaining. Note that setLength must
	 * ONLY be called once per object, or a RuntimeException is thrown, as this points to a definite bug.
	 *
	 * @param length
	 * @return
	 */
	public Target setLength(int length) {
		if(IS_DEBUG && lengthSet) {
			throw new RuntimeException("Length should not be set twice. Originally set at " + originalSet);
		}
		if(IS_DEBUG) {
			originalSet = StackTraceUtils.GetStacktrace(new Throwable());
		}
		this.length = length;
		this.lengthSet = true;
		return this;
	}

	public Target copy() {
		Target t = new Target(line(), file(), col());
		// Set the length, but allow it to be set again by leaving lengthSet false.
		t.length = length;
		return t;
	}

	/**
	 * Returns a string in the form: <code>absolute/path:0</code>
	 *
	 * @return
	 */
	@Override
	public String toString() {
		return (file != null ? file.getAbsolutePath() : "Unknown File") + ":" + line + "." + col;
	}

	/**
	 * This implementation can be used to sort {@link Target}s first on file path, then on line and last on column.
	 */
	@Override
	public int compareTo(Target t) {
		int ret = (this.file == null ? (t.file == null ? 0 : -1)
				: (t.file == null ? 1 : this.file.getAbsolutePath().compareTo(t.file.getAbsolutePath())));
		if(ret != 0) {
			return ret;
		}
		ret = Integer.compare(this.line, t.line);
		if(ret != 0) {
			return ret;
		}
		return Integer.compare(this.col, t.col);
	}
}
