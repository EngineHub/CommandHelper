
package com.laytonsmith.core.compiler;

import com.laytonsmith.core.ParseTree;
import java.util.List;

/**
 * 
 */
public interface KeywordHandler {
	/**
	 * Given a list of elements (with the guarantee that this keyword will
	 * be the first keyword in the list) this method should rearrange the
	 * list to remove at MINIMUM the first instance of the keyword, and make
	 * the list fully functional.
	 * @param elements 
	 */
	void handle(List<ParseTree> elements);
}
