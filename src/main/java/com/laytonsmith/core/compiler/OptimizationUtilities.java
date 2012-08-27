/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.compiler;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.constructs.CFunction;
import java.util.List;

/**
 *
 * @author Layton
 */
public class OptimizationUtilities {

	/**
	 * Goes one deep, and pulls up like children. This is only for use where the
	 * same function being chained doesn't make sense, for instance, in the case
	 * of adding, add(2, add(2, add(2, 2))) should just turn into add(2, 2, 2,
	 * 2).
	 *
	 * @param children
	 * @param functionName
	 */
	public static void pullUpLikeFunctions(List<ParseTree> children, String functionName) {
		int size = children.size() - 1;
		for (int i = size; i >= 0; i--) {
			ParseTree tree = children.get(i);
			if (tree.getData() instanceof CFunction && tree.getData().val().equals(functionName)) {
				//We can pull it up. Just go through the children and insert them here. Remove the node
				//that was this child though.
				children.remove(i);
				for (int j = tree.getChildren().size() - 1; j >= 0; j--) {
					children.add(i, tree.getChildAt(j));
				}
			}
		}
	}
}
