package com.laytonsmith.core.compiler;

import com.laytonsmith.core.ParseTree;
import java.util.List;

/**
 * In addition to being a function, an object may also be a code branch, that is,
 * it conditionally will execute some of its arguments.
 * For optimization and static code analysis purposes,
 * it it important for these functions to be able to provide more information
 * about their branches; if the branch conditions are static, they should be reduceable to a single
 * branch anyways, but some optimizations require knowledge about code branches.
 */
public interface CodeBranch extends Optimizable {

	/**
	 * Given a list of children, this function should
	 * return the indexes of the code branches themselves.
	 * This optimization step occurs during the breadth first optimization,
	 * but non-code branches need to be optimized down fully before
	 * they can be reliably determined whether or not to be removed.
	 * This function identifies which branches are code branches, and
	 * prevents them from being depth first analysed at the wrong stage
	 * of optimization.
	 * @param children
	 * @return
	 */
	Integer[] getCodeBranches(List<ParseTree> children);
	
}
