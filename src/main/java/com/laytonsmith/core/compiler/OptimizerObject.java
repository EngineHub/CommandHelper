package com.laytonsmith.core.compiler;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.FunctionList;
import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 */
class OptimizerObject {

	private final ParseTree root;
	private final CompilerEnvironment env;

	public OptimizerObject(ParseTree root, Environment compilerEnvironment) {
		this.root = root;
		env = compilerEnvironment.getEnv(CompilerEnvironment.class);
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
	 * This optimization level removes all the __autoconcat__s (and inadvertently several other constructs as well)
	 *
	 * @param tree
	 * @param compilerEnvironment
	 * @throws ConfigCompileException
	 */
	private void optimize01(ParseTree tree, CompilerEnvironment compilerEnvironment) throws ConfigCompileException {
		com.laytonsmith.core.functions.Compiler.__autoconcat__ autoconcat
				= (com.laytonsmith.core.functions.Compiler.__autoconcat__)
				FunctionList.getFunction("__autoconcat__", null, Target.UNKNOWN);
		if(tree.getData() instanceof CFunction && tree.getData().val().equals("__autoconcat__")) {
			ParseTree tempNode = autoconcat.optimizeSpecial(tree.getChildren(), true, null);
			tree.setData(tempNode.getData());
			tree.setChildren(tempNode.getChildren());
		}
		for(int i = 0; i < tree.getChildren().size(); i++) {
			ParseTree node = tree.getChildren().get(i);
			optimize01(node, compilerEnvironment);
		}
	}

	/**
	 * This pass optimizes all turing functions. That is, branch functions like if, and for.
	 *
	 * @param tree
	 * @param compilerEnvironment
	 */
	private void optimize02(ParseTree tree, CompilerEnvironment compilerEnvironment) {

	}

	/**
	 * This pass makes sure no weird constructs are left, for instance, a CEntry, or any bare strings, if strict mode is
	 * on.
	 *
	 * @param tree
	 * @param compilerEnvironment
	 * @throws ConfigCompileException
	 */
	private void optimize03(ParseTree tree, CompilerEnvironment compilerEnvironment) throws ConfigCompileException {
	}

	/**
	 * This pass makes sure that all variables are initialized before usage, if strict mode is on. For the first call,
	 * send a new List for assignments.
	 */
	private void optimize04(ParseTree tree, CompilerEnvironment compilerEnvironment, List<String> assignments) throws ConfigCompileException {
		//TODO: I don't think all this is necessary
//		if(tree.getFileOptions().isStrict()){
//			if(tree.getData() instanceof NewIVariable){
//				if(!assignments.contains(((NewIVariable)tree.getData()).getName())){
//					throw new ConfigCompileException("Variables must be declared before use, in strict mode.", tree.getTarget());
//				}
//			}
//		}
//		for(int i = 0; i < tree.getChildren().size(); i++) {
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
	 * This optimization level adds all known instances of procs to the environment. After this pass, all procs, if not
	 * obtainable, are a compile error.
	 *
	 * @param tree
	 * @param compilerEnvironment
	 * @throws ConfigCompileException
	 */
	private void optimize05(ParseTree tree, CompilerEnvironment compilerEnvironment) throws ConfigCompileException {
	}
}
