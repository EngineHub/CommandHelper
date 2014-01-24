

package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Version;
import java.net.URL;

/**
 * Classes that implement this method know how to provide some documentation to the DocGen
 * class.
 * 
 * In general, classes that implement this should also tag themselves with the
 * <code>@docs</code> tag, so the ClassDiscovery method can more easily find them,
 * if the class intends on being parsed by DocGen.
 * @author layton
 */
public interface Documentation extends SimpleDocumentation {
	
	/**
	 * Returns the source jar this code element came from. This may return
	 * null if the source is dynamic, or it is otherwise unknown where it came from.
	 * It may not throw an exception though, if any exception were to be generated,
	 * it should simply return null.
	 * @return 
	 */
	URL getSourceJar();
	
	/**
	 * Returns a list of other Documentation elements that are similar to this
	 * one, and may be shown as links to those elements with the documentation
	 * for this element.
	 * @return 
	 */
	Class<? extends Documentation>[] seeAlso();
}
