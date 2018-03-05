package com.laytonsmith.core.constructs;

import java.io.File;

/**
 * A target allows code to be exactly identified in source. It contains the line number, file, and column that this
 * target represents.
 *
 *
 */
public class Target {

	/**
	 * For efficiency sake, since Targets are immutable, if you intend on using a blank target, that is to say, one that
	 * has been manufactured in source, you must use this target instead. If a null target is attempted to be created
	 * elsewhere, an exception is thrown.
	 */
	public static final Target UNKNOWN = new Target(0, null, 0, null);
	private final int line;
	private final File file;
	private final int col;

	private Target(int line, File file, int col, String Null) {
		this.line = line;
		this.file = file;
		this.col = col;
	}

	/**
	 * Creates a new target that represents a location in a source file.
	 *
	 * @param line
	 * @param file
	 * @param col
	 */
	public Target(int line, File file, int col) {
		if (line == 0 && col == 0 && file == null) {
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
	 * Returns a string in the form: <code>absolute/path:0</code>
	 *
	 * @return
	 */
	@Override
	public String toString() {
		return (file != null ? file.getAbsolutePath() : "Unknown File") + ":" + line + "." + col;
	}
}
