package com.laytonsmith.core.compiler;

import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionList;
import java.util.ArrayList;
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
		optimize01(root, env);
		optimize02(root, env);
		optimize03(root, env);
		optimize04(root, env, new ArrayList<String>());
		optimize05(root, env);

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
	 * with issuing a warning.
	 *
	 * @param tree
	 * @param compilerEnvironment
	 */
	private void optimize02(ParseTree tree, Environment compilerEnvironment) throws ConfigCompileException {
		ParseTree tempNode = null;
		int numChildren;
		do {
			if (tree.getData().val().startsWith("_")) {
				//Procedure. Can't optimize this yet, so just return.
				return;
			}
			numChildren = tree.numberOfChildren();
			if (tree.getData() instanceof CFunction) {
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
								CHLog.GetLogger().Log(CHLog.Tags.COMPILER, LogLevel.WARNING, "Unreachable code. Consider removing this code.", children.get(i + 1).getTarget());
								//Now, truncate the children
								for (int j = i + 1; j < children.size(); j++) {
									children.remove(j);
								}
								break outer;
							}
						}
					}
				}

				if (((CFunction) tree.getData()).getFunction() instanceof Function.CodeBranch) {
					tempNode = ((Function.CodeBranch) func).optimizeDynamic(tree.getTarget(), env, tree.getChildren());

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
			}
		} while (tempNode != null && tempNode.numberOfChildren() != numChildren); //Keep optimizing until we made no code branch changes
		//Now optimize the children
		for (int i = 0; i < tree.getChildren().size(); i++) {
			ParseTree node = tree.getChildAt(i);
			optimize02(node, compilerEnvironment);
		}
	}

	/**
	 * This pass runs the optimization of the remaining functions.
	 *
	 * @param tree
	 * @param compilerEnvironment
	 * @throws ConfigCompileException
	 */
	private void optimize03(ParseTree tree, Environment compilerEnvironment) throws ConfigCompileException {
	}

	/**
	 * This pass makes sure that all variables are initialized before usage, if
	 * strict mode is on. For the first call, send a new List for assignments.
	 */
	private void optimize04(ParseTree tree, Environment compilerEnvironment, List<String> assignments) throws ConfigCompileException {
		//TODO: I don't think all this is necessary
//		if(tree.getFileOptions().isStrict()){
//			if(tree.getData() instanceof NewIVariable){
//				if(!assignments.contains(((NewIVariable)tree.getData()).getName())){
//					throw new ConfigCompileException("Variables must be declared before use, in strict mode.", tree.getTarget());
//				}
//			}
//		}
//		for (int i = 0; i < tree.getChildren().size(); i++) {
//			ParseTree node = tree.getChildren().get(i);
//			if(node.getData() instanceof CFunction && node.getData().val().equals("assign")){
//				if(node.getChildAt(0).getData() instanceof NewIVariable){
//					String name = ((NewIVariable)node.getChildAt(0).getData()).getName();
//					assignments.add(name);
//				} else {
//					throw new ConfigCompileException("An assignment can only occur on a variable.", node.getChildAt(0).getTarget());
//				}
//			}
//			optimize04(node, compilerEnvironment, assignments);
//		}
	}

	/**
	 * This optimization level adds all known instances of procs to the
	 * environment. After this pass, all procs, if not obtainable, are a compile
	 * error.
	 *
	 * @param tree
	 * @param compilerEnvironment
	 * @throws ConfigCompileException
	 */
	private void optimize05(ParseTree tree, Environment compilerEnvironment) throws ConfigCompileException {
	}
}
