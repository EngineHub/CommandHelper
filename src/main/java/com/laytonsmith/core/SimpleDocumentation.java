package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Version;

/**
 * Elements implementing this provide simple documentation data to the docgen,
 * not complex data.
 */
public interface SimpleDocumentation {
	/**
	 * The name of this code element
	 *
	 * @return The name of this code element.
	 */
	String getName();

	/**
	 * Returns documentation in a format that is specified by the code type
	 *
	 * @return
	 */
	String docs();

	/**
	 * Returns the version number of when this functionality was added. It
	 * should follow the format 0.0.0
	 *
	 * @return
	 */
	Version since();
}
