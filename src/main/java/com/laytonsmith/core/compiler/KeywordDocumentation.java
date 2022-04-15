package com.laytonsmith.core.compiler;

import com.laytonsmith.core.Documentation;

/**
 * A wrapper interface for reflective access among all keywords, normal, late, and early binding.
 */
public interface KeywordDocumentation extends Documentation {

	/**
	 * Returns the name of the keyword as used in code.
	 * @return
	 */
	public String getKeywordName();
}
