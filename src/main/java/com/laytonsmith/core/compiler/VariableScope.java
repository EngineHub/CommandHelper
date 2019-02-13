package com.laytonsmith.core.compiler;

import com.laytonsmith.core.ParseTree;
import java.util.List;

/**
 * A VariableScope is a branch where, once the code is completed running, the {@link VariableStack} is popped, and when
 * entering, a new scope is pushed. In general, a VariableScope will also be a {@link BranchStatement}, but this is
 * not guaranteed, particular naked scope blocks, but potentially other situations.
 */
public interface VariableScope {

	/**
	 * Returns a list of booleans (where the list is the size of the {@code children}, that specifies which children
	 * constitute a variable scope. True should be provided if the child is a scope, false otherwise. For instance,
	 * in the code {@code if(v, s, r)}, {@code v} is not a scope, but {@code s} and {@code r} are.
	 * <p>
	 * A VariableScope is a branch where, once the code is completed running, the {@link VariableStack} is popped, and
	 * when entering, a new scope is pushed.
	 * @param children The (unevaluated) children that will be passed (evaluated) to this function call at runtime. It
	 * may not be necessary to determine what the scope is, but it may be useful/necessary to determine, and so is
	 * passed in regardless.
	 * @return A list of booleans representing which children are variable scopes.
	 */
	List<Boolean> isScope(List<ParseTree> children);
}
