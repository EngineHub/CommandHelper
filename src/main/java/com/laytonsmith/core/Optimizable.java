package com.laytonsmith.core;

import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.List;
import java.util.Set;

/**
 *
 *
 */
public interface Optimizable extends Function {

	/**
	 * This constant can be returned from an optimization method to indicate that the need for this node has been
	 * removed entirely, and it should be removed from the code tree or replaced with a no-op method. More than likely
	 * it will be replaced with a no-op method, so that the side effects are removed.
	 */
	public static final ParseTree REMOVE_ME = new ParseTree(null);

	/**
	 * This constant can be returned from an optimization method to indicate that the first child of this method should
	 * be pulled up and replace this method. For instance, if you had func('arg') it would turn into 'arg'. If there are
	 * multiple children, you must handle that manually. This is simply a convenience, and could be accomplished in
	 * other ways.
	 */
	public static final ParseTree PULL_ME_UP = new ParseTree(null);

	public enum OptimizationOption implements SimpleDocumentation {

		/**
		 * If this function can be run at compile time if all the parameters of a function are constant, this
		 * optimization can be selected. This is a compile time optimization.
		 */
		CONSTANT_OFFLINE("If all the parameters of a function are constant, a function with this optimization"
				+ " will be run at compile time, and that value stored, instead of it being run each time. For"
				+ " instance, the add function is like this, which means that if you were to do add(2, 2), it would"
				+ " simply replace that call with 4, at compile time, which makes it more efficient during runtime.", MSVersion.V3_3_1),
		/**
		 * If the function will return void, and the effects of the function do not need to be ordered, this can be
		 * selected, and the function will be run on another thread. Note that this will only be valid if a function
		 * also can be run async. This is a runtime optimization.
		 */
		INSTANT_RETURN("Some functions can be run async, and there is no benefit for it to wait around for the operation to finish."
				+ " For instance, using sys_out() does not need to wait for the IO to flush before returning control to the script.", MSVersion.V3_3_1),
		/**
		 * If a function can do some amount of optimization at compile time, but can't simply run the exec() function
		 * directly, this can be selected, which will cause the function's optimize() method to be called. This is a
		 * compile time optimization.
		 */
		OPTIMIZE_CONSTANT("A function may be able to do some optimization if the parameters are constant, but it may be"
				+ " a bit more complicated than simply running the function. Otherwise, this is exactly like " + CONSTANT_OFFLINE.getName(), MSVersion.V3_3_1),
		/**
		 * If a function can do some amount of optimization at compile time, even if some of the parameters are dynamic,
		 * this can be selected, which will cause the function's optimizeDynamic() method to be called. This is a
		 * compile time optimization.
		 */
		OPTIMIZE_DYNAMIC("Some functions can do some amount of optimization or compilation checks, even if the function is sent dynamic"
				+ " parameters. For instance, if(true, rand(), '1') can be changed simply to rand(), because the condition is hard coded"
				+ " to be true. In this case, the compile tree is smaller, which makes it more efficient.", MSVersion.V3_3_1),
		/**
		 * If, given the same parameters, the return of this function could be cached (that is, it is a const function)
		 * this can be selected. This does not guarantee that the results will be cached, since there is a memory trade
		 * off, and at large enough volumes, a time trade off too, with such a method, the system may decide to forego
		 * the cache, and simply re-call the method. However, given an often enough call, it should speed up execution
		 * in many cases. If the function returns an array, the array will be cloned before actually being returned.
		 */
		CACHE_RETURN("If a function is able to optimize out constant inputs, it can likely also cache the return value."
				+ " If the engine determines that it is faster to cache the returned values vs re-running the function,"
				+ " it may choose to do so. This is a runtime optimization, and is calculated by the engine itself to determine"
				+ " which method is faster, so there is no guarantee that any optimization will occur, however, unless this"
				+ " option is specified, it will certainly not.", MSVersion.V3_3_1),
		/**
		 * If this function is terminal, that is, it will ALWAYS interrupt program flow, this can be selected. For
		 * instance, return() is an example. This is used during optimization, and to give compiler warnings.
		 */
		TERMINAL("If a function is \"terminal\", that is, it is guaranteed to have abnormal code flow (for instance,"
				+ " return() or throw()) it is marked terminal, which is used by the compiler to issue warnings, in the"
				+ " event you make some code unreachable by putting it under a terminal statement, and to optimize"
				+ " by removing the unreachable code from the code tree.", MSVersion.V3_3_1),
		/**
		 * If a function is "side effect free", that is, if the return value is unused, the function itself does
		 * nothing, then this optimization can be specified. This is mostly useful for cases where the value returns a
		 * check, but the check has been determined by the compiler to be unused, making the entire call itself
		 * unneeded, allowing the call itself to be removed from the tree.
		 */
		NO_SIDE_EFFECTS("If a function is \"side effect free\", that is, if the return value is unused, the function"
				+ " itself does nothing, then this optimization can be specified. This is mostly useful for cases"
				+ " where the value returns a check, but the check has been determined by the compiler to be unused,"
				+ " making the entire call itself unneeded, allowing the call itself to be removed from the tree.", MSVersion.V3_3_1),
		/**
		 * Some functions do want to do linking, but in a special, custom way. If this is specified, then the function
		 * will have the link() method called on it, in place of the default linking mechanism that the compiler
		 * provides.
		 */
		CUSTOM_LINK("Some functions do want to do linking, but in a special, custom way. If this is specified, then"
				+ " the function will have the link() method called on it, in place of the default linking mechanism that"
				+ " the compiler provides.", MSVersion.V3_3_1),
		/**
		 * This is a priority optimization function, meaning it needs to be optimized before its children are. This is
		 * required when optimization of the children could cause different internal behavior, for instance if this
		 * function is expecting the presence of some code element, but the child gets optimized out, this would cause
		 * an error, even though the user did in fact provide code in that section.
		 */
		PRIORITY_OPTIMIZATION("This is a priority optimization function, meaning it needs to be optimized before its children are."
				+ " This is required when optimization of the children could cause different internal behavior, for instance"
				+ " if this function is expecting the presence of some code element, but the child gets optimized out, this"
				+ " would cause an error, even though the user did in fact provide code in that section.", MSVersion.V3_3_1);

		private final MSVersion since;
		private final String docs;

		private OptimizationOption(String docs, MSVersion since) {
			this.docs = docs;
			this.since = since;
		}

		@Override
		public String getName() {
			return name();
		}

		@Override
		public String docs() {
			return docs;
		}

		@Override
		public MSVersion since() {
			return since;
		}

	}

	/**
	 * Returns an array of optimization options that apply to this function.
	 *
	 * @return
	 */
	public Set<OptimizationOption> optimizationOptions();

	/**
	 * This is called during compile time, if canOptimize returns true. It should return the construct to replace this
	 * function, if possible. If only type checking is being done, it may return null, in which case no changes will be
	 * made to the parse tree. During the optimization, it is also possible for a function to throw a
	 * ConfigCompileException. It may also throw a ConfigRuntimeException, which will be caught, and changed into a
	 * ConfigCompileException.
	 *
	 * @param t
	 * @param env The environment. The only guaranteed useable environment is the {@link CompilerEnvironment}.
	 * @param args
	 * @return
	 * @throws com.laytonsmith.core.exceptions.ConfigCompileException
	 */
	public Mixed optimize(Target t, Environment env, Mixed... args)
			throws ConfigRuntimeException, ConfigCompileException;

	/**
	 * If the function indicates it can optimize dynamic values, this method is called. It may also throw a compile
	 * exception should the parameters be unacceptable. It may return null if no changes should be made (which is likely
	 * the default).
	 *
	 * @param t
	 * @param env The environment. The only guaranteed useable environment is the {@link CompilerEnvironment}.
	 * @param envs The target environments. Unfortunately, we cannot use the environment list in {@code env}, as that
	 * does not represent the runtime environment.
	 * @param children The children that are being passed to this function
	 * @param fileOptions The file options for the top level function
	 * @return
	 * @throws com.laytonsmith.core.exceptions.ConfigCompileException
	 */
	public ParseTree optimizeDynamic(Target t, Environment env, Set<Class<? extends Environment.EnvironmentImpl>> envs,
			List<ParseTree> children, FileOptions fileOptions)
			throws ConfigCompileException, ConfigRuntimeException;

	/**
	 * Does custom linking in a given function. This is called if the {@link OptimizationOption#CUSTOM_LINK} option is
	 * specified. This is useful to override if some error checking should happen after all optimizations are done,
	 * across the entire AST, not just across this function and it's children. For instance, bind() uses this to provide
	 * a compile error if it isn't compiled out via an event_exists directive.
	 *
	 * @param t
	 * @param children
	 * @throws com.laytonsmith.core.exceptions.ConfigCompileException
	 */
	public void link(Target t, List<ParseTree> children) throws ConfigCompileException;

}
