
package com.laytonsmith.core.constructs;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class DocComment {
	
	Map<String, String> annotations = new HashMap<String, String>();
	/**
	 * Creates a new DocComment object based on the given unparsed text. The text should not
	 * contain the opening /** and *./ blocks. * at the beginning of newlines are removed, and
	 * duplicate whitespace is removed.
	 * @param text 
	 */
	public DocComment(String text){
		//TODO:
	}
	
	public String getMain(){
		return null;
	}
	
	public String getAnnotation(String name){
		return annotations.get(name);
	}
}
