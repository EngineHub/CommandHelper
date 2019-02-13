package com.laytonsmith.core.compiler;

import com.laytonsmith.core.ParseTree;
import java.util.List;

/**
 * A BranchStatement is a function that has different branches, only one of which may in fact execute. During
 * optimization, this is considered, and certain features, such as exit detection, uses this information to determine
 * various conditions, such as dead code analysis and other functionality.
 * <p>
 * Not implementing this interface, and returning a list of all false booleans, should in practice be equivalent.
 * <p>
 * Many functions that are BranchStatements are also potentially {@link VariableScope}s, but this is not guaranteed,
 * and is therefore a separate mechanism.
 */
public interface BranchStatement {

	/**
	 * Returns a list of booleans (where the list is the size of the {@code children}, that specifies which children
	 * constitute a code branch. True should be provided if the child is a branch, false otherwise. For instance,
	 * in the code {@code if(v, s, r)}, {@code v} is not a branch, but {@code s} and {@code r} are.
	 * <p>
	 * A "branch" is more properly defined as parts of the code that may or may not run, depending on the resolution
	 * of the value at runtime. In general, if the branch can be determined at compile time, then this function
	 * probably shouldn't exist in the first place.
	 * <p>
	 * For functions that are lazily evaluated, for instance {@code and()}, this is not truly a branch statement,
	 * and should not be considered as such, even though at first glance it seems like it would be. The reason for this
	 * is that in some cases, ALL children will be run, whereas in the case of {@code if()}, if one branch is run,
	 * the other most certainly will not. Therefore, in cases where all arguments could potentially be evaluaed,
	 * this should not be considered a branch function.
	 * @param children The (unevaluated) children that will be passed (evaluated) to this function call at runtime. It
	 * may not be necessary to determine what the branch is, but it may be useful/necessary to determine, and so is
	 * passed in regardless.
	 * @return A list of booleans representing which children are branch statements.
	 */
	List<Boolean> isBranch(List<ParseTree> children);
}
