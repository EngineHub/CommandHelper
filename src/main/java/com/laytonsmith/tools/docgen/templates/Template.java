package com.laytonsmith.tools.docgen.templates;

/**
 * A Template subclass represents a template in the docs, that can be referenced in code. There is no check to ensure
 * that the template file in fact exists, so ensure that if a subclass is created, there is a corresponding file for it.
 */
public abstract class Template {

	/**
	 * Returns the name of this Template.
	 *
	 * @return
	 */
	public abstract String getName();

	/**
	 * Returns the display name of this Template. By default, this is just the value of getName().
	 *
	 * @return
	 */
	public String getDisplayName() {
		return getName();
	}

	/**
	 * If the template should be located at a different path on the website, then this should be overridden
	 * to provide the path. By default, empty string is returned, which means the root path. If this is not
	 * the empty string, the path returned should end with {@code /}
	 * @return
	 */
	public String getPath() {
		return "";
	}

}
