package com.laytonsmith.core.compiler;

import com.laytonsmith.core.constructs.Target;

/**
 * A compiler warning represents an issue with the code, that isn't quite an error, but should still be brought to the
 * users attention.
 */
public class CompilerWarning {

	private final String message;
	private final Target target;
	private final FileOptions.SuppressWarning suppressCategory;

	/**
	 *
	 * @param message
	 * @param target
	 * @param suppressCategory
	 */
	public CompilerWarning(String message, Target target, FileOptions.SuppressWarning suppressCategory) {
		String prefix = "";
		if(suppressCategory != null) {
			prefix = "(" + suppressCategory.getName() + ") ";
		}
		this.message = prefix + message;
		this.target = target;
		this.suppressCategory = suppressCategory;
	}

	public String getMessage() {
		return message;
	}

	public Target getTarget() {
		return target;
	}

	/**
	 * The suppression category for this warning. This may be null, in which case the warning cannot be suppressed.
	 * @return
	 */
	public FileOptions.SuppressWarning getSuppressCategory() {
		return suppressCategory;
	}

}
