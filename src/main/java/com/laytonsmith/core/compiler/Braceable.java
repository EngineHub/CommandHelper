package com.laytonsmith.core.compiler;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.util.List;

/**
 *
 */
public interface Braceable {
	/**
	 * Handles a list of ParseTree nodes, which should be
	 * transformed into the proper arrangement (or a compile
	 * error should be thrown if they are in an invalid state).
	 * Only nodes startingWith the given index and beyond should be considered,
	 * and not all nodes need considering, as they may not be associated with this
	 * particular function.
	 * @param allNodes A list of all nodes to consider
	 * @param startingWith While looping through, this function should start
	 * with this index.
	 */
	void handleBraces(List<ParseTree> allNodes, int startingWith) throws ConfigCompileException;
}
