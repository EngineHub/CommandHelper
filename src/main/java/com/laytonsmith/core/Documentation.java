

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
public interface Documentation {
    /**
     * The name of this code element
	 * @return The name of this code element.
     */
    String getName();
	
    /**
     * Returns documentation in a format that is specified by the code type
     * @return 
     */
    String docs();
	
    /**
     * Returns the version number of when this functionality was added. It should
     * follow the format 0.0.0
     * @return 
     */
    Version since();
	
	/**
	 * Returns the source jar this code element came from. This may return
	 * null if the source is dynamic, or it is otherwise unknown where it came from.
	 * It may not throw an exception though, if any exception were to be generated,
	 * it should simply return null.
	 * @return 
	 */
	URL getSourceJar();
}
