package com.laytonsmith.core.compiler;

import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.compiler.Optimizable.OptimizationOption;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author lsmith
 */
class OptimizerObject {

	private final static EnumSet<Optimizable.OptimizationOption> NO_OPTIMIZATIONS = EnumSet.noneOf(Optimizable.OptimizationOption.class);
	private ParseTree root;
	private Environment env;

	public OptimizerObject(ParseTree root, Environment compilerEnvironment) {
		this.root = root;
		env = compilerEnvironment;
	}

	public ParseTree optimize() throws ConfigCompileException {
		//If at any point we get a PullMeUpException, we just set root = to that node
		env.getEnv(CompilerEnvironment.class).pushProcedureScope();
		optimize01(root, env);
		optimize02(root, env, false);
		try{
			optimize03(root, env, false); // Once
		} catch(PullMeUpException e){
			root = e.getNode();
		}
		optimize04(root, env, new ArrayList<String>());
		optimize05(root, env);
		try{
			optimize03(root, env, true); // Twice
		} catch(PullMeUpException e){
			root = e.getNode();
		}
		try{
			optimize03(root, env, true); // Thrice
		} catch(PullMeUpException e){
			root = e.getNode();
		}
		optimize02(root, env, true); // Gotta do this one again too
		env.getEnv(CompilerEnvironment.class).popProcedureScope();

		return root;
	}

	/**
	 * This optimization level removes all the __autoconcat__s (and
	 * inadvertently several other constructs as well)
	 *
	 * @param tree
	 * @param compilerEnvironment
	 * @throws ConfigCompileException
	 */
	private void optimize01(ParseTree tree, Environment compilerEnvironment) throws ConfigCompileException {
		com.laytonsmith.core.compiler.CompilerFunctions.__autoconcat__ autoconcat = (com.laytonsmith.core.compiler.CompilerFunctions.__autoconcat__) FunctionList.getFunction("__autoconcat__");
		if (tree.getData() instanceof CFunction && tree.getData().val().equals("cc")) {
			for (int i = 0; i < tree.getChildren().size(); i++) {
				ParseTree node = tree.getChildAt(i);
				if (node.getData().val().equals("__autoconcat__")) {
					ParseTree tempNode = autoconcat.optimizeSpecial(node.getChildren(), false);
					tree.setData(tempNode.getData());
					tree.setChildren(tempNode.getChildren());
					optimize01(tree, compilerEnvironment);
					return;
				}
			}
		} else {
			if (tree.getData() instanceof CFunction && tree.getData().val().equals("__autoconcat__")) {
				ParseTree tempNode = autoconcat.optimizeSpecial(tree.getChildren(), true);
				tree.setData(tempNode.getData());
				tree.setChildren(tempNode.getChildren());
			}
			for (int i = 0; i < tree.getChildren().size(); i++) {
				ParseTree node = tree.getChildren().get(i);
				optimize01(node, compilerEnvironment);
			}
		}
	}

	/**
	 * This pass optimizes all turing functions. That is, branch functions like
	 * if, and for. It also checks for unreachable code, and removes it, along
	 * with issuing a warning. Boolean simplification also happens during this step,
	 * so things like {@code if(@i == 4 && @i == 5)} can be removed.
	 *
	 * @param tree
	 * @param compilerEnvironment
	 */
	private void optimize02(ParseTree tree, Environment compilerEnvironment, boolean optimizeProcs) throws ConfigCompileException {
		ParseTree tempNode = null;
		int numChildren;
		do {
			numChildren = tree.numberOfChildren();
			if (tree.getData() instanceof CFunction) {
				if(((CFunction)tree.getData()).isProcedure()){
					//Procedure. Can't optimize this yet, so just return.
					return;
				}
				Function func = ((CFunction) tree.getData()).getFunction();
				List<ParseTree> children = tree.getChildren();
				//Loop through the children, and if any of them are functions that are terminal, truncate.
				//To explain this further, consider the following:
				//For the code: concat(die(), msg('')), this diagram shows the abstract syntax tree:
				//         (concat)
				//        /        \
				//       /          \
				//     (die)       (msg)
				//By looking at the code, we can tell that msg() will never be called, because die() will run first,
				//and since it is a "terminal" function, any code after it will NEVER run. However, consider a more complex condition:
				// if(@input){ die() msg('1') } else { msg('2') msg('3') }
				//              if(@input)
				//        [true]/         \[false]
				//             /           \
				//         (sconcat)     (sconcat)
				//           /   \         /    \
				//          /     \       /      \
				//       (die) (msg[1])(msg[2]) (msg[3])
				//In this case, only msg('1') is guaranteed not to run, msg('2') and msg('3') will still run in some cases.
				//So, we can optimize out msg('1') in this case, which would cause the tree to become much simpler, therefore a worthwile optimization:
				//              if(@input)
				//        [true]/        \[false]
				//             /          \
				//          (die)      (sconcat)
				//                      /    \
				//                     /      \
				//                 (msg[2]) (msg[3])
				//We do have to be careful though, because of functions like if, which actually work like this:
				//if(@var){ die() } else { msg('') }
				//                (if)
				//              /  |  \
				//             /   |   \
				//          @var (die) (msg)
				//We can't git rid of the msg() here, because it is actually in another branch.
				//For the time being, we will simply say that if a function uses execs, it
				//is a branch (branches always use execs, though using execs doesn't strictly
				//mean you are a branch type function).

				outer:
				for (int i = 0; i < children.size(); i++) {
					ParseTree t = children.get(i);
					if (t.getData() instanceof CFunction) {
						if (t.getData().val().startsWith("_") || (func != null && func.useSpecialExec())) {
							continue outer;
						}
						Function f = (Function) FunctionList.getFunction(t.getData());
						Set<Optimizable.OptimizationOption> options = NO_OPTIMIZATIONS;
						if (f instanceof Optimizable) {
							options = ((Optimizable) f).optimizationOptions();
						}
						if (options.contains(Optimizable.OptimizationOption.TERMINAL)) {
							if (children.size() > i + 1) {
								//First, a compiler warning
								CHLog.GetLogger().CompilerWarning(CompilerWarning.UnreachableCode, "Unreachable code. Consider removing this code.", children.get(i + 1).getTarget(), tree.getFileOptions());
								//Now, truncate the children
								for (int j = i + 1; j < children.size(); j++) {
									children.remove(j);
								}
								break outer;
							}
						}
					}
				}

				Function f = ((CFunction)tree.getData()).getFunction();
				if (f instanceof CodeBranch) {
					CodeBranch cb = (CodeBranch)f;
					//Go ahead and depth first optimize the non code branch parts
					List<Integer> branches = Arrays.asList(cb.getCodeBranches(tree.getChildren()));
					for(int i = 0; i < tree.getChildren().size(); i++){
						if(!branches.contains(i)){
							ParseTree child = tree.getChildAt(i);
							try{
								optimize03(child, compilerEnvironment, optimizeProcs);
							} catch(PullMeUpException e){
								tree.getChildren().set(i, e.getNode());
							}
						}
					}
					
					tempNode = cb.optimizeDynamic(tree.getTarget(), env, tree.getChildren());

					if (tempNode == Optimizable.PULL_ME_UP) {
						tempNode = tree.getChildAt(0);
					}
					if (tempNode == Optimizable.REMOVE_ME) {
						tree.setData(new CVoid(Target.UNKNOWN));
						tree.removeChildren();
					} else if (tempNode != null) {
						tree.setData(tempNode.getData());
						tree.setChildren(tempNode.getChildren());
					}
				}
				
				//TODO: Check for boolean optimizations here
			}
		} while (tempNode != null && tempNode.numberOfChildren() != numChildren); //Keep optimizing until we made no code branch changes
		//Now optimize the children
		for (int i = 0; i < tree.getChildren().size(); i++) {
			ParseTree node = tree.getChildAt(i);
			optimize02(node, compilerEnvironment, optimizeProcs);
		}
	}

	/**
	 * This pass runs the optimization of the remaining functions.
	 * This gets run three times, once before proc optimizations (to optimize inside
	 * procedures), once after (to optimize down procedure usages),
	 * and a final third time, for the benefit of functions that can pull up or
	 * otherwise reoptimize now that some procs may be gone.
	 *
	 * @param tree
	 * @param compilerEnvironment
	 * @throws ConfigCompileException
	 */
	private void optimize03(ParseTree tree, Environment compilerEnvironment, boolean optimizeProcs) throws ConfigCompileException, PullMeUpException {
		//Depth first
		for(int i = 0; i < tree.numberOfChildren(); i++){
			ParseTree child = tree.getChildAt(i);
			try{
				optimize03(child, compilerEnvironment, optimizeProcs);
			} catch(PullMeUpException e){
				tree.getChildren().set(i, e.getNode());
			}
		}
		
		CompilerEnvironment env = compilerEnvironment.getEnv(CompilerEnvironment.class);
		if(!(tree.getData() instanceof CFunction)){
			//Not a function, no optimization needed
			return;
		}
		if(optimizeProcs && ((CFunction)tree.getData()).isProcedure()){
			//Different way to optimize these, but it won't happen the first go through
			//TODO
		} else {
			Function func = ((CFunction)tree.getData()).getFunction();
			if(func instanceof Optimizable){
				Optimizable f = (Optimizable)func;
				Set<Optimizable.OptimizationOption> options = f.optimizationOptions();
				if(options.contains(Optimizable.OptimizationOption.OPTIMIZE_DYNAMIC)){
					ParseTree tempNode;
					try{
						tempNode = f.optimizeDynamic(tree.getTarget(), compilerEnvironment, tree.getChildren());
					} catch(ConfigRuntimeException e){
						//Turn this into a compile exception, then rethrow
						throw new ConfigCompileException(e);
					}
					if(tempNode == Optimizable.PULL_ME_UP){
						//We're fully done with this function now, because it's completely
						//gone from the tree, so actually we need to replace us with the child
						//in our parent's child list, but we don't have access to that information,
						//so throw an exception up with the child, and the parent will have to
						//deal with it.
						throw new PullMeUpException(tree.getChildAt(0));
					} else if(tempNode == Optimizable.REMOVE_ME){
						tree.setData(new CVoid(tree.getTarget()));
						tree.removeChildren();
					} else if(tempNode != null){
						tree.setData(tempNode.getData());
						tree.setChildren(tempNode.getChildren());
					} //else it was just a compile check
				}
				
				//Now that we have done all the optimizations we can with dynamic functions,
				//let's see if we are constant, and then optimize const stuff
				if(options.contains(OptimizationOption.OPTIMIZE_CONSTANT)
						|| options.contains(OptimizationOption.CONSTANT_OFFLINE)){
					Construct [] constructs = new Construct[tree.getChildren().size()];
					for(int i = 0; i < tree.getChildren().size(); i++){
						constructs[i] = tree.getChildAt(i).getData();
						if(constructs[i].isDynamic()){
							//Can't optimize any further, so return
							return;
						}
					}
					try{
						Construct result;
						if(options.contains(OptimizationOption.CONSTANT_OFFLINE)){
							result = f.exec(tree.getData().getTarget(), compilerEnvironment, constructs);
						} else {
							result = f.optimize(tree.getData().getTarget(), compilerEnvironment, constructs);
						}
						//If the result is null, it was just a check, it can't optimize further
						if(result != null){
							tree.setData(result);
							tree.removeChildren();
						}
					} catch(ConfigRuntimeException e){
						throw new ConfigCompileException(e);
					}
				}
			}
		}
	}

	/**
	 * This pass ensures no violations of strict mode, if strict mode is enabled.
	 * This includes use of uninitialized variables. For the first call, send a new List for assignments. Scopes
	 * will be handled appropriately.
	 */
	private void optimize04(ParseTree tree, Environment compilerEnvironment, List<String> assignments) throws ConfigCompileException {
		//Depth first, except for code branches, which have to have the non-branches initialized first.
		//We still go through all the motions, even if strict mode is off, because only part
		//of the tree may be in strict mode, in which case we will simply skip throwing the exception
		//if we aren't in strict mode.
		if(tree.getFileOptions().isStrict()){

		}
	}

	/**
	 * This optimization level adds all known instances of procs to the
	 * environment. After this pass, all procs, if not obtainable, are a compile
	 * error. Additionally during this stage, tail call recursion is optimized.
	 * An overview of tail call recursion can be found here: http://en.wikipedia.org/wiki/Tail_call
	 *
	 * @param tree
	 * @param compilerEnvironment
	 * @throws ConfigCompileException
	 */
	private void optimize05(ParseTree tree, Environment compilerEnvironment) throws ConfigCompileException {
		
	}
	
	/**
	 * Used by {@link #optimize03(com.laytonsmith.core.ParseTree, com.laytonsmith.core.environments.Environment, boolean)}
	 */
	private class PullMeUpException extends Exception{
		private ParseTree tree;
		public PullMeUpException(ParseTree tree){
			this.tree = tree;
		}
		
		public ParseTree getNode(){
			return tree;
		}
	}

}
