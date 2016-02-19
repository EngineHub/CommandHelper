package com.laytonsmith.tools.docgen.templates;

/**
 * A Template subclass represents a template in the docs, that can be referenced in
 * code. There is no check to ensure that the template file in fact exists, so ensure
 * that if a subclass is created, there is a corresponding file for it.
 */
public abstract class Template {
	
	/**
	 * Returns the name of this Template.
	 * @return 
	 */
	public abstract String getName();
	
	/**
	 * Returns the display name of this Template. By default, this is just the
	 * value of getName().
	 * @return 
	 */
	public String getDisplayName(){
		return getName();
	}
	
}
