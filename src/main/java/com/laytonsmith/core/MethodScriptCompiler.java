package com.laytonsmith.core;

import com.laytonsmith.core.Optimizable.OptimizationOption;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.NewMethodScriptCompiler;
import com.laytonsmith.core.compiler.TokenStream;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.constructs.Token.TType;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.compiler.CompilerFunctions;
import com.laytonsmith.core.functions.DataHandling;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionList;
import com.laytonsmith.core.functions.IncludeCache;
import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Layton
 */
public final class MethodScriptCompiler {

	private final static EnumSet<Optimizable.OptimizationOption> NO_OPTIMIZATIONS = EnumSet.noneOf(Optimizable.OptimizationOption.class);
	private final static FileOptions fileOptions = new FileOptions(new HashMap<String, String>());

	private MethodScriptCompiler() {
	}

	public static TokenStream lex(String config, File file, boolean inPureMScript) throws ConfigCompileException {
		return NewMethodScriptCompiler.lex(config, file, inPureMScript);
	}

	/**
	 * This function breaks the token stream into parts, separating the
	 * aliases/MethodScript from the command triggers
	 *
	 * @param tokenStream
	 * @return
	 * @throws ConfigCompileException
	 */
	public static List<Script> preprocess(List<Token> tokenStream) throws ConfigCompileException {
		//First, pull out the duplicate newlines
		ArrayList<Token> temp = new ArrayList<Token>();
		for (int i = 0; i < tokenStream.size(); i++) {
			try {
				if (tokenStream.get(i).type.equals(TType.NEWLINE)) {
					temp.add(new Token(TType.NEWLINE, "\n", tokenStream.get(i).target));
					while (tokenStream.get(++i).type.equals(TType.NEWLINE)) {
					}
				}
				if (tokenStream.get(i).type != TType.WHITESPACE) {
					temp.add(tokenStream.get(i));
				}
			} catch (IndexOutOfBoundsException e) {
			}
		}

		if (temp.size() > 0 && temp.get(0).type.equals(TType.NEWLINE)) {
			temp.remove(0);
		}

		tokenStream = temp;

		//Handle multiline constructs
		ArrayList<Token> tokens1_1 = new ArrayList<Token>();
		boolean inside_multiline = false;
		Token thisToken = null;
		for (int i = 0; i < tokenStream.size(); i++) {
			Token prevToken = i - 1 >= tokenStream.size() ? tokenStream.get(i - 1) : new Token(TType.UNKNOWN, "", Target.UNKNOWN);
			thisToken = tokenStream.get(i);
			Token nextToken = i + 1 < tokenStream.size() ? tokenStream.get(i + 1) : new Token(TType.UNKNOWN, "", Target.UNKNOWN);
			//take out newlines between the = >>> and <<< tokens (also the tokens)
			if (thisToken.type.equals(TType.ALIAS_END) && nextToken.val().equals(">>>")) {
				inside_multiline = true;
				tokens1_1.add(thisToken);
				i++;
				continue;
			}
			if (thisToken.val().equals("<<<")) {
				if (!inside_multiline) {
					throw new ConfigCompileException("Found multiline end symbol, and no multiline start found",
							thisToken.target);
				}
				inside_multiline = false;
				continue;
			}
			if (thisToken.val().equals(">>>") && inside_multiline) {
				throw new ConfigCompileException("Did not expect a multiline start symbol here, are you missing a multiline end symbol above this line?", thisToken.target);
			}
			if (thisToken.val().equals(">>>") && !prevToken.type.equals(TType.ALIAS_END)) {
				throw new ConfigCompileException("Multiline symbol must follow the alias_end token", thisToken.target);
			}

			//If we're not in a multiline construct, or we are in it and it's not a newline, add
			//it
			if (!inside_multiline || (inside_multiline && !thisToken.type.equals(TType.NEWLINE))) {
				tokens1_1.add(thisToken);
			}
		}

		if (inside_multiline) {
			throw new ConfigCompileException("Expecting a multiline end symbol, but your last multiline alias appears to be missing one.", thisToken.target);
		}

		//take out newlines that are behind a \
		ArrayList<Token> tokens2 = new ArrayList<Token>();
		for (int i = 0; i < tokens1_1.size(); i++) {
			if (!tokens1_1.get(i).type.equals(TType.STRING) && tokens1_1.get(i).val().equals("\\") && tokens1_1.size() > i
					&& tokens1_1.get(i + 1).type.equals(TType.NEWLINE)) {
				tokens2.add(tokens1_1.get(i));
				i++;
				continue;
			}
			tokens2.add(tokens1_1.get(i));
		}





		//Now that we have all lines minified, we should be able to split
		//on newlines, and easily find the left and right sides

		List<Token> left = new ArrayList<Token>();
		List<Token> right = new ArrayList<Token>();
		List<Script> scripts = new ArrayList<Script>();
		boolean inLeft = true;
		for (Token t : tokens2) {
			if (inLeft) {
				if (t.type == TType.ALIAS_END) {
					inLeft = false;
				} else {
					left.add(t);
				}
			} else {
				if (t.type == TType.NEWLINE) {
					inLeft = true;
					//Env newEnv = new Env();//env;
//                    try{
//                        newEnv = env.clone();
//                    } catch(Exception e){}
					Script s = new Script(left, right, fileOptions);
					scripts.add(s);
					left = new ArrayList();
					right = new ArrayList();
				} else {
					right.add(t);
				}
			}
		}
		return scripts;
	}

	public static ParseTree compile(TokenStream stream, Environment env) throws ConfigCompileException {
		ParseTree tree = NewMethodScriptCompiler.compile(stream, env);
		Stack<List<Procedure>> procs = new Stack<List<Procedure>>();
		procs.add(new ArrayList<Procedure>());
		optimize(tree, procs, env); //TODO: Is this needed anymore?
		return tree;
	}

	/**
	 * Recurses down into the tree, attempting to optimize where possible. A few
	 * things have strong coupling, for information on these items, see the
	 * documentation included in the source.
	 *
	 * @param tree
	 * @return
	 */
	private static void optimize(ParseTree tree, Stack<List<Procedure>> procs, Environment env) throws ConfigCompileException {
		if (tree.isOptimized()) {
			return; //Don't need to re-run this
		}
//		if (tree.getData() instanceof CIdentifier) {
//			optimize(((CIdentifier) tree.getData()).contained(), procs);
//			return;
//		}
		if (!(tree.getData() instanceof CFunction)) {
			//There's no way to optimize something that's not a function
			return;
		}
		//cc has to be inb4 other autoconcats, so sconcats on the lower level won't get run
		if (tree.getData().val().equals("cc")) {
			for (int i = 0; i < tree.getChildren().size(); i++) {
				ParseTree node = tree.getChildAt(i);
				if (node.getData().val().equals("__autoconcat__")) {
					CompilerFunctions.__autoconcat__ func = (CompilerFunctions.__autoconcat__) FunctionList.getFunction(node.getData());
					ParseTree tempNode = func.optimizeSpecial(node.getChildren(), false);
					tree.setData(tempNode.getData());
					tree.setChildren(tempNode.getChildren());
					optimize(tree, procs, env);
					return;
				}
			}
		}
		//If it is a proc definition, we need to go ahead and see if we can add it to the const proc stack
		if (tree.getData().val().equals("proc")) {
			procs.push(new ArrayList<Procedure>());
		}
		CFunction cFunction = (CFunction) tree.getData();
		Function func;
		try {
			func = (Function) FunctionList.getFunction(cFunction);
		} catch (ConfigCompileException e) {
			func = null;
		}
		if (cFunction instanceof CIdentifier) {
			//Add the child to the identifier
			ParseTree c = ((CIdentifier) cFunction).contained();
			tree.addChild(c);
			c.getData().setWasIdentifier(true);
		}
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
				Set<OptimizationOption> options = NO_OPTIMIZATIONS;
				if (f instanceof Optimizable) {
					options = ((Optimizable) f).optimizationOptions();
				}
				if (options.contains(OptimizationOption.TERMINAL)) {
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
		boolean fullyStatic = true;
		boolean hasIVars = false;
		for (int i = 0; i < children.size(); i++) {
			ParseTree node = children.get(i);
			if (node.getData() instanceof CFunction) {
				optimize(node, procs, env);
			}

			if (node.getData().isDynamic() && !(node.getData() instanceof IVariable)) {
				fullyStatic = false;
			}
			if (node.getData() instanceof IVariable) {
				hasIVars = true;
			}
		}
		//In all cases, at this point, we are either unable to optimize, or we will
		//optimize, so set our optimized variable at this point.
		tree.setOptimized(true);

		if (func == null) {
			//It's a proc call. Let's see if we can optimize it
			Procedure p = null;
			//Did you know about this feature in java? I didn't until recently.
			//I break to the loop label, which makes it jump to the bottom of
			//that loop.
			loop:
			for (int i = 0; i < procs.size(); i++) {
				for (Procedure pp : procs.get(i)) {
					if (pp.getName().equals(cFunction.val())) {
						p = pp;
						break loop;
					}
				}
			}
			if (p != null) {
				try {
					Construct c = DataHandling.proc.optimizeProcedure(Target.UNKNOWN, p, children);
					if (c != null) {
						tree.setData(c);
						tree.removeChildren();
						return;
					}//else Nope, couldn't optimize.
				} catch (ConfigRuntimeException ex) {
					//Cool. Caught a runtime error at compile time :D
					throw new ConfigCompileException(ex);
				}
			}
			//else this procedure isn't listed yet. Maybe a compiler error, maybe not, depends,
			//so we can't for sure say, but we do know we can't optimize this
			return;
		}
		if (tree.getData().val().equals("proc")) {
			//We just went out of scope, so we need to pop the layer of Procedures that
			//are internal to us
			procs.pop();
			//However, as a special function, we *might* be able to get a const proc out of this
			//Let's see.
			try {
				ParseTree root = new ParseTree(new CFunction("__autoconcat__", Target.UNKNOWN), fileOptions);
				Script fakeScript = Script.GenerateScript(root, "*");
				Procedure myProc = DataHandling.proc.getProcedure(Target.UNKNOWN, env, fakeScript, children.toArray(new ParseTree[children.size()]));
				procs.peek().add(myProc); //Yep. So, we can move on with our lives now, and if it's used later, it could possibly be static.
			} catch (ConfigRuntimeException e) {
				//Well, they have an error in there somewhere
				throw new ConfigCompileException(e);
			} catch (Exception e) {
				//Nope, can't optimize.
				return;
			}
		}

		//the compiler trick functions know how to deal with it specially, even if everything isn't
		//static, so do this first.
		String oldFunctionName = func.getName();
		Set<OptimizationOption> options = NO_OPTIMIZATIONS;
		if (func instanceof Optimizable) {
			options = ((Optimizable) func).optimizationOptions();
		}
		if (options.contains(OptimizationOption.OPTIMIZE_DYNAMIC)) {
			ParseTree tempNode;
			try {
				tempNode = ((Optimizable) func).optimizeDynamic(tree.getData().getTarget(), env, tree.getChildren());
			} catch (ConfigRuntimeException e) {
				//Turn it into a compile exception, then rethrow
				throw new ConfigCompileException(e);
			}
			if (tempNode == Optimizable.PULL_ME_UP) {
				tempNode = tree.getChildAt(0);
			}
			if (tempNode == Optimizable.REMOVE_ME) {
				tree.setData(new CFunction("p", Target.UNKNOWN));
				tree.removeChildren();
			} else if (tempNode != null) {
				tree.setData(tempNode.getData());
				tree.setOptimized(tempNode.isOptimized());
				tree.setChildren(tempNode.getChildren());
				tree.getData().setWasIdentifier(tempNode.getData().wasIdentifier());
				optimize(tree, procs, env);
				tree.setOptimized(true);
				//Some functions can actually make static the arguments, for instance, by pulling up a hardcoded
				//array, so if they have reversed this, make note of that now
				if (tempNode.hasBeenMadeStatic()) {
					fullyStatic = true;
				}
			} //else it wasn't an optimization, but a compile check
		}
		if (!fullyStatic) {
			return;
		}
		//Otherwise, everything is static, or an IVariable and we can proceed.
		//Note since we could still have IVariables, we have to handle those
		//specially from here forward
		if (func.preResolveVariables() && hasIVars) {
			//Well, this function isn't equipped to deal with IVariables.
			return;
		}
		//It could have optimized by changing the name, in that case, we
		//don't want to run this now
		if (tree.getData().getValue().equals(oldFunctionName)
				&& (options.contains(OptimizationOption.OPTIMIZE_CONSTANT) || options.contains(OptimizationOption.CONSTANT_OFFLINE))) {
			Construct[] constructs = new Construct[tree.getChildren().size()];
			for (int i = 0; i < tree.getChildren().size(); i++) {
				constructs[i] = tree.getChildAt(i).getData();
			}
			try {
				Construct result;
				if (options.contains(OptimizationOption.CONSTANT_OFFLINE)) {
					result = func.exec(tree.getData().getTarget(), env, constructs);
				} else {
					result = ((Optimizable) func).optimize(tree.getData().getTarget(), env, constructs);
				}

				//If the result is null, it was just a check, it can't optimize further.
				if (result != null) {
					result.setWasIdentifier(tree.getData().wasIdentifier());
					tree.setData(result);
					tree.removeChildren();
				}
			} catch (ConfigRuntimeException e) {
				//Turn this into a ConfigCompileException, then rethrow
				throw new ConfigCompileException(e);
			}
		}

		//It doesn't know how to optimize. Oh well.
	}

	/**
	 * Executes a pre-compiled MethodScript, given the specified Script
	 * environment. Both done and script may be null, and if so, reasonable
	 * defaults will be provided. The value sent to done will also be returned,
	 * as a Construct, so this one function may be used synchronously also.
	 *
	 * @param root
	 * @param done
	 * @param script
	 */
	public static Construct execute(ParseTree root, Environment env, MethodScriptComplete done, Script script) {
		return execute(root, env, done, script, null);
	}

	/**
	 * Executes a pre-compiled MethodScript, given the specified Script
	 * environment, but also provides a method to set the constants in the
	 * script.
	 *
	 * @param root
	 * @param env
	 * @param done
	 * @param script
	 * @param vars
	 * @return
	 */
	public static Construct execute(ParseTree root, Environment env, MethodScriptComplete done, Script script, List<Variable> vars) {
		if (script == null) {
			script = new Script(null, null, fileOptions);
		}
		if (vars != null) {
			Map<String, Variable> varMap = new HashMap<String, Variable>();
			for (Variable v : vars) {
				varMap.put(v.getName(), v);
			}
			for (Construct tempNode : root.getAllData()) {
				if (tempNode instanceof Variable) {
					((Variable) tempNode).setVal(
							varMap.get(((Variable) tempNode).getName()).getDefault());
				}
			}
		}
		StringBuilder b = new StringBuilder();
		Construct returnable = null;
		//for (ParseTree gg : root.getChildren()) {
		script.setLabel(env.getEnv(GlobalEnv.class).GetLabel());
		Construct retc = script.eval(root, env);
		if (root.numberOfChildren() == 1) {
			returnable = retc;
		}
		String ret = retc instanceof CNull ? "null" : retc.val();
		if (ret != null && !ret.trim().isEmpty()) {
			b.append(ret).append(" ");
		}
		//}
		if (done != null) {
			done.done(b.toString().trim());
		}
		if (returnable != null) {
			return returnable;
		}
		return Static.resolveConstruct(b.toString().trim(), Target.UNKNOWN);
	}

	public static void registerAutoIncludes(Environment env, Script s) {
		File root = env.getEnv(GlobalEnv.class).GetRootFolder();
		File auto_include = new File(root, "auto_include.ms");
		if (auto_include.exists()) {
			MethodScriptCompiler.execute(IncludeCache.get(auto_include, new Target(0, auto_include, 0), env), env, null, s);
		}

		for (File f : Static.getAliasCore().autoIncludes) {
			MethodScriptCompiler.execute(IncludeCache.get(f, new Target(0, f, 0), env), env, null, s);
		}
	}
}
