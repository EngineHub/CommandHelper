package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.annotations.noprofile;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.analysis.StaticAnalysis;
import com.laytonsmith.core.compiler.signature.FunctionSignatures;
import com.laytonsmith.core.compiler.signature.SignatureBuilder;
import com.laytonsmith.core.constructs.CBareString;
import com.laytonsmith.core.constructs.CBracket;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CEntry;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CKeyword;
import com.laytonsmith.core.constructs.CLabel;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CSymbol;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.InstanceofUtil;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Token;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.functions.DataHandling._string;
import com.laytonsmith.core.functions.DataHandling.assign;
import com.laytonsmith.core.functions.Math.neg;
import com.laytonsmith.core.functions.Math.postdec;
import com.laytonsmith.core.functions.Math.postinc;
import com.laytonsmith.core.functions.StringHandling.concat;
import com.laytonsmith.core.functions.StringHandling.sconcat;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 *
 */
@core
public class Compiler {

	public static String docs() {
		return "Compiler internal functions should be declared here. If you're reading this from anywhere"
				+ " but the source code, there's a bug, because these functions shouldn't be public or used"
				+ " in a script.";
	}

	@api
	@noprofile
	@hide("This is only used internally by the compiler.")
	public static class p extends DummyFunction implements Optimizable {

		public static final String NAME = "p";

		@Override
		public String getName() {
			return NAME;
		}

		@Override
		public String docs() {
			return "mixed {c...} Used internally by the compiler. You shouldn't use it.";
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			return (nodes.length == 1 ? parent.eval(nodes[0], env) : CVoid.VOID);
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return CVoid.VOID;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env, Set<Class<? extends Environment.EnvironmentImpl>> envs, List<ParseTree> children, FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException, ConfigCompileGroupException {
			if(children.size() == 1) {
				return Optimizable.PULL_ME_UP;
			} else if(children.isEmpty()) {
				return Optimizable.REMOVE_ME;
			} else {
				return null;
			}
		}

		@Override
		public ParseTree postParseRewrite(ParseTree ast, Environment env, Set<Class<? extends Environment.EnvironmentImpl>> envs, Set<ConfigCompileException> exceptions) {
			if(ast.getChildren().size() == 1) {
				return ast.getChildAt(0);
			}
			return null;
		}

		@Override
		public CClassType getReturnType(Target t, List<CClassType> argTypes, List<Target> argTargets, Environment env, Set<ConfigCompileException> exceptions) {
			if(argTypes.size() == 1) {
				return argTypes.get(0);
			} else {
				return CVoid.TYPE;
			}
		}

	}

	@api
	@noprofile
	@hide("This is only used internally by the compiler.")
	public static class centry extends DummyFunction {

		public static final String NAME = "centry";

		@Override
		public String docs() {
			return "CEntry {label, content} Dynamically creates a CEntry. This is used internally by the "
					+ "compiler.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return new CEntry(args[0], args[1], t);
		}
	}

	@api
	@noprofile
	@hide("This is only used internally by the compiler.")
	public static class __autoconcat__ extends DummyFunction {

		public static final String NAME = "__autoconcat__";

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			throw new Error("Should not have gotten here, " + __autoconcat__.NAME + " was not removed before runtime.");
		}

		@Override
		public String docs() {
			return "string {var1, [var2...]} This function should only be used by the compiler, behavior"
					+ " may be undefined if it is used in code.";
		}

		/**
		 * Gets an executable AST node to replace an __autoconcat__ with as part of compilation. This results in a
		 * compile error if the __autoconcat__ cannot be converted to an executable AST node.
		 *  __autoconcat__ being a function is merely a convenient way to defer processing until after parsing,
		 *  meaning that it should ALWAYS be rewritten before executing the AST.
		 * When this method is called, the caller must ensure that the passed children do not contain any
		 * __autoconcat__. This can be ensured by calling this method on all __autoconcat__ nodes in a depth-first
		 * post-order AST traversal.
		 * @param list - A list containing all {@link ParseTree} children of this __autoconcat__.
		 * @param returnSConcat - If parsing results in only one child function, then this argument is ignored.
		 * If {@code true}, the resulting parsed functions will be wrapped into {@link sconcat}.
		 * If {@code false}, the resulting parsed functions will be wrapped into {@link __statements__}.
		 * @return The executable AST node, representing the code/tokens in this __autoconcat__.
		 * @throws ConfigCompileException If this __autoconcat__ cannot be converted to an executable AST node.
		 */
		public static ParseTree rewrite(List<ParseTree> list, boolean returnSConcat,
				Set<Class<? extends Environment.EnvironmentImpl>> envs) throws ConfigCompileException {
			//If any of our nodes are CSymbols, we have different behavior
			boolean inSymbolMode = false; //caching this can save Xn

			// Rewrite execute operator.
			rewriteParenthesis(list);

			// Rewrite assignment operator.
			/*
			 * Note that we are walking the array in reverse, because multiple assignments,
			 * say @a = @b = 1 will break if they look like assign(assign(@a, @b), 1),
			 * they need to be assign(@a, assign(@b, 1)). As a variation, we also have
			 * to support something like 1 + @a = 2, which will turn into add(1, assign(@a, 2),
			 * and 1 + @a = @b + 3 would turn into add(1, assign(@a, add(@b, 3))).
			 */
			for(int i = list.size() - 2; i >= 0; i--) {
				ParseTree node = list.get(i + 1);
				if(node.getData() instanceof CSymbol && ((CSymbol) node.getData()).isAssignment()) {

					// Get assign left hand side and autoconcat assign right hand side if necessary.
					ParseTree lhs = list.get(i);
					if(i < list.size() - 3) {
						List<ParseTree> valChildren = new ArrayList<>();
						int index = i + 2;
						// add all preceding symbols
						while(list.size() > index + 1 && (list.get(index).getData() instanceof CSymbol
								|| (list.get(index).getData() instanceof CFunction cf
										&& cf.hasFunction() && cf.getFunction() != null
										&& cf.getFunction().getName().equals(Compiler.p.NAME)
										&& list.get(index).numberOfChildren() == 1))) {
							valChildren.add(list.get(index));
							list.remove(index);
						}
						// add first item
						valChildren.add(list.get(index));
						list.remove(index);
						// loop through all additional symbols/items
						while(list.size() > index + 1 && list.get(index).getData() instanceof CSymbol) {
							// add all contiguous symbols
							do {
								// symbols first
								valChildren.add(list.get(index));
								list.remove(index);
							} while(list.size() > index && list.get(index).getData() instanceof CSymbol);
							if(list.size() <= index) {
								throw new ConfigCompileException("Unexpected end of statement",
										list.get(list.size() - 1).getTarget());
							}
							// then item
							valChildren.add(list.get(index));
							list.remove(index);
						}
						//Set this subset into the correct slot, the rest of the
						//code will grab it correctly that way.
						list.add(i + 2, rewrite(valChildren, returnSConcat, envs));
					}
					if(list.size() <= i + 2) {
						throw new ConfigCompileException("Unexpected end of statement", list.get(i).getTarget());
					}
					ParseTree rhs = list.get(i + 2);

					// Wrap additive assignment in right hand side (e.g. convert @a += 1 to @a = @a + 1).
					CSymbol sy = (CSymbol) node.getData();
					String conversionFunction = sy.convertAssignment();
					if(conversionFunction != null) {
						ParseTree rhsReplacement = new ParseTree(
								new CFunction(conversionFunction, node.getTarget()), node.getFileOptions());
						rhsReplacement.addChild(lhs);
						rhsReplacement.addChild(rhs);
						rhs = rhsReplacement;
					}

					// Rewrite to assign node.
					ParseTree assignNode = new ParseTree(
							new CFunction(assign.NAME, node.getTarget()), node.getFileOptions());
					assignNode.addChild(lhs);
					assignNode.addChild(rhs);
					list.set(i, assignNode); // Overwrite lhs with assign node.
					list.remove(i + 1); // Remove "=" node.
					list.remove(i + 1); // Remove rhs node.
				}
			}

			// Rewrite postfix operators.
			for(int i = 0; i < list.size(); i++) {
				ParseTree node = list.get(i);
				if(node.getData() instanceof CSymbol) {
					inSymbolMode = true;
				}
				if(node.getData() instanceof CSymbol && ((CSymbol) node.getData()).isPostfix()) {
					if(i - 1 >= 0 && !(list.get(i - 1).getData() instanceof CSymbol)) {
						CSymbol sy = (CSymbol) node.getData();
						ParseTree conversion;
						if(sy.val().equals("++")) {
							conversion = new ParseTree(
									new CFunction(postinc.NAME, node.getTarget()), node.getFileOptions());
						} else {
							conversion = new ParseTree(
									new CFunction(postdec.NAME, node.getTarget()), node.getFileOptions());
						}
						conversion.addChild(list.get(i - 1));
						list.set(i - 1, conversion);
						list.remove(i);
						i--;
					}
				}
			}

			// Rewrite unary operators.
			if(inSymbolMode) {
				try {
					for(int i = 0; i < list.size() - 1; i++) {
						ParseTree node = list.get(i);
						if(node.getData() instanceof CSymbol && ((CSymbol) node.getData()).isUnary()) {
							ParseTree conversion;
							if(node.getData().val().equals("-") || node.getData().val().equals("+")) {
								//These are special, because if the values to the left isn't a symbol,
								//it's not unary. Labels also are a separate term.
								if((i == 0 || list.get(i - 1).getData() instanceof CSymbol)
										&& !(list.get(i + 1).getData() instanceof CSymbol)
										|| (i != 0 && list.get(i - 1).getData() instanceof CLabel)) {
									if(node.getData().val().equals("-")) {
										//We have to negate it
										conversion = new ParseTree(
												new CFunction(neg.NAME, node.getTarget()), node.getFileOptions());
									} else {
										conversion = new ParseTree(
												new CFunction(p.NAME, node.getTarget()), node.getFileOptions());
									}
								} else {
									continue;
								}
							} else {
								conversion = new ParseTree(new CFunction(((CSymbol) node.getData()).convert(), node.getTarget()), node.getFileOptions());
							}
							// We actually need to get all the remaining children, and shove them into an autoconcat
							List<ParseTree> ac = new ArrayList<>();
							list.set(i, conversion);
							for(int k = i + 1; k < list.size(); k++) {
								ParseTree m = list.get(k);
								if(m.getData() instanceof CSymbol && ((CSymbol) m.getData()).isUnary()) {
									ac.add(m);
									list.remove(k);
									k--;
									i--;
									continue;
								}
								ac.add(m);
								list.remove(k);
								break;
							}
							conversion.addChild(rewrite(ac, returnSConcat, envs));
						}
					}
				} catch (IndexOutOfBoundsException e) {
					throw new ConfigCompileException("Unexpected symbol (" + list.get(list.size() - 1).getData().val() + ")",
							list.get(list.size() - 1).getTarget());
				}
			}

			// Rewrite cast operator.
			for(int i = list.size() - 2; i >= 0; i--) {
				ParseTree node = list.get(i);
				if(node.getData() instanceof CFunction cf && cf.hasFunction() && cf.getFunction() != null
						&& cf.getFunction().getName().equals(Compiler.p.NAME) && node.numberOfChildren() == 1) {

					// Convert bare string or concat() to type reference if needed.
					ParseTree typeNode = node.getChildAt(0);
					if(!typeNode.getData().isInstanceOf(CClassType.TYPE)) {
						ParseTree convertedTypeNode = __type_ref__.createFromBareStringOrConcats(typeNode);
						if(convertedTypeNode != null) {
							typeNode = convertedTypeNode;
						} else {

							// This is not a "(classtype)" format. Skip node.
							continue;
						}
					}

					// Rewrite p(A) and the next list entry B to __cast__(B, A).
					ParseTree castNode = new ParseTree(
							new CFunction(__cast__.NAME, node.getTarget()), node.getFileOptions());
					castNode.addChild(list.get(i + 1));
					castNode.addChild(typeNode);
					list.set(i, castNode);
					list.remove(i + 1);
				}
			}

			// Rewrite binary operators.
			if(inSymbolMode) {
				try {

					//Exponential
					for(int i = 0; i < list.size() - 1; i++) {
						ParseTree next = list.get(i + 1);
						if(next.getData() instanceof CSymbol sy && sy.isExponential()) {
							rewriteBinaryOperator(list, sy, i--);
						}
					}
					//Multiplicative
					for(int i = 0; i < list.size() - 1; i++) {
						ParseTree next = list.get(i + 1);
						if(next.getData() instanceof CSymbol sy && sy.isMultaplicative() && !sy.isAssignment()) {
							rewriteBinaryOperator(list, sy, i--);
						}
					}
					//Additive
					for(int i = 0; i < list.size() - 1; i++) {
						ParseTree next = list.get(i + 1);
						if(next.getData() instanceof CSymbol sy && sy.isAdditive() && !sy.isAssignment()) {
							rewriteBinaryOperator(list, sy, i--);
						}
					}
					//relational
					for(int i = 0; i < list.size() - 1; i++) {
						ParseTree next = list.get(i + 1);
						if(next.getData() instanceof CSymbol sy && sy.isRelational()) {
							rewriteBinaryOperator(list, sy, i--);
						}
					}
					//equality
					for(int i = 0; i < list.size() - 1; i++) {
						ParseTree next = list.get(i + 1);
						if(next.getData() instanceof CSymbol sy && sy.isEquality()) {
							rewriteBinaryOperator(list, sy, i--);
						}
					}
					// default and
					for(int i = 0; i < list.size() - 1; i++) {
						ParseTree next = list.get(i + 1);
						if(next.getData() instanceof CSymbol sy && sy.isDefaultAnd()) {
							rewriteBinaryOperator(list, sy, i--);
						}
					}
					// default or
					for(int i = 0; i < list.size() - 1; i++) {
						ParseTree next = list.get(i + 1);
						if(next.getData() instanceof CSymbol sy && sy.isDefaultOr()) {
							rewriteBinaryOperator(list, sy, i--);
						}
					}
					//logical and
					for(int i = 0; i < list.size() - 1; i++) {
						ParseTree next = list.get(i + 1);
						if(next.getData() instanceof CSymbol sy && sy.isLogicalAnd()) {
							rewriteBinaryOperator(list, sy, i--);
						}
					}
					//logical or
					for(int i = 0; i < list.size() - 1; i++) {
						ParseTree next = list.get(i + 1);
						if(next.getData() instanceof CSymbol sy && sy.isLogicalOr()) {
							rewriteBinaryOperator(list, sy, i--);
						}
					}
				} catch (IndexOutOfBoundsException e) {
					throw new ConfigCompileException("Unexpected symbol (" + list.get(list.size() - 1).getData().val() + ")",
							list.get(list.size() - 1).getTarget());
				}
			}

			// Look for typed assignments
			for(int k = 0; k < list.size(); k++) {
				ParseTree typeNode = list.get(k);

				// Convert bare string or concat() to type reference if used like that in syntax.
				ParseTree convertedTypeNode = __type_ref__.createFromBareStringOrConcats(typeNode);
				ParseTree originalTypeNode = typeNode;
				if(convertedTypeNode != null) {
					typeNode = convertedTypeNode;
				}

				if(convertedTypeNode != null
						|| typeNode.getData().equals(CVoid.VOID) || typeNode.getData().isInstanceOf(CClassType.TYPE)) {
					if(k == list.size() - 1) {
						// This is not a typed assignment
						break;
						//throw new ConfigCompileException("Unexpected ClassType", list.get(k).getTarget());
					}

					if(list.get(k + 1).getData() instanceof CFunction) {
						switch(list.get(k + 1).getData().val()) {
							// closure is missing from this, because "closure" is both a ClassType and a keyword,
							// and since keywords take priority over __autoconcat__, it will have already been
							// handled by the time we reach this code.
							case "assign":
							case "proc":
								// Typed assign/closure
								if(list.get(k + 1).getData().val().equals(assign.NAME)
										&& typeNode.getData().equals(CVoid.VOID)) {
									throw new ConfigCompileException("Variables may not be of type void",
											list.get(k + 1).getTarget());
								}

								// Remove type node.
								list.remove(k);

								List<ParseTree> children = list.get(k).getChildren();
								children.add(0, typeNode);
								list.get(k).setChildren(children);
								break;
							default:
								if(typeNode.getData().equals(CVoid.VOID) || typeNode.getData().isInstanceOf(CClassType.TYPE)) {
									throw new ConfigCompileException("Unexpected ClassType \""
											+ typeNode.getData().val() + "\"", typeNode.getTarget());
								}
						}
					} else if(list.get(k + 1).getData() instanceof IVariable) {
						// Not an assignment, a random variable declaration though.
						ParseTree node = new ParseTree(new CFunction(assign.NAME, list.get(k + 1).getTarget()), typeNode.getFileOptions());
						node.addChild(typeNode);
						node.addChild(list.get(k + 1));
						node.addChild(new ParseTree(CNull.UNDEFINED, typeNode.getFileOptions()));
						list.set(k, node);
						list.remove(k + 1);
					} else if(list.get(k + 1).getData() instanceof CLabel) {
						ParseTree node = new ParseTree(new CFunction(assign.NAME, list.get(k + 1).getTarget()), typeNode.getFileOptions());
						ParseTree labelNode = new ParseTree(new CLabel(node.getData()), typeNode.getFileOptions());
						labelNode.addChild(typeNode);
						labelNode.addChild(new ParseTree(((CLabel) list.get(k + 1).getData()).cVal(), typeNode.getFileOptions()));
						labelNode.addChild(new ParseTree(CNull.UNDEFINED, typeNode.getFileOptions()));
						list.set(k, labelNode);
						list.remove(k + 1);
					} else if(originalTypeNode.getData().getClass().equals(CBareString.class)) {
						continue; // Bare string was not used as a type.
					} else {
						throw new ConfigCompileException("Unexpected data after ClassType", list.get(k + 1).getTarget());
					}
				}
			}

			//Look for a CEntry here
			if(list.size() >= 1) {
				ParseTree node = list.get(0);
				if(node.getData() instanceof CLabel) {

					// Remove the label from the children, leaving only value nodes as children.
					list.remove(0);

					// Rewrite value nodes to a single value using autoconcat logic.
					ParseTree value = rewrite(list, returnSConcat, envs);

					// Create centry node from the label and value, and return the result.
					ParseTree ce = new ParseTree(new CFunction(centry.NAME, node.getTarget()), node.getFileOptions());
					ce.addChild(node);
					ce.addChild(value);
					return ce;
				}
			}

			//We've eliminated the need for __autoconcat__ either way, however, if there are still arguments
			//left, it needs to go to sconcat, which MAY be able to be further optimized, but that will
			//be handled in MethodScriptCompiler's optimize function. Also, we must scan for CPreIdentifiers,
			//which may be turned into a function
			if(list.size() == 1) {
				ParseTree node = list.get(0);
				if(node.getData() instanceof CFunction && node.getData().val().equals(__autoconcat__.NAME)) {
					node = rewrite(node.getChildren(), returnSConcat, envs);
				}
				return node;
			} else {
				for(int i = 0; i < list.size(); i++) {
					if(Construct.IsCType(list.get(i).getData(), Construct.ConstructType.IDENTIFIER)) {
						if(i == 0) {
							//Yup, it's an identifier
							CFunction identifier = new CFunction(list.get(i).getData().val(), list.get(i).getTarget());
							list.remove(0);
							ParseTree child = list.get(0);
							if(list.size() > 1) {
								child = new ParseTree(
										new CFunction(sconcat.NAME, child.getTarget()), child.getFileOptions());
								child.setChildren(list);
							}
							try {
								Function f = (Function) FunctionList.getFunction(identifier, envs);
								ParseTree node = new ParseTree(
										f.execs(identifier.getTarget(), null, null, child), child.getFileOptions());
								if(node.getData() instanceof CFunction
										&& node.getData().val().equals(__autoconcat__.NAME)) {
									node = rewrite(node.getChildren(), returnSConcat, envs);
								}
								return node;
							} catch (Exception e) {
								throw new Error("Unknown function " + identifier.val() + "?");
							}
						} else {
							//Hmm, this is weird. I'm not sure what condition this can happen in
							throw new ConfigCompileException("Unexpected IDENTIFIER. Please report a bug,"
									+ " and include the script you used to get this error. At or around:", list.get(i).getTarget());
						}
					}
				}
				ParseTree tree;
				FileOptions options;
				Target t = Target.UNKNOWN;
				if(!list.isEmpty()) {
					options = list.get(0).getFileOptions();
					t = list.get(0).getTarget();
				} else {
					options = new FileOptions(new HashMap<>());
				}

				if(returnSConcat) {
					tree = new ParseTree(new CFunction(sconcat.NAME, t), options, true);
					tree.setChildren(list);
				} else {
					tree = new ParseTree(new CFunction(__statements__.NAME, t), options, true);
					// Instead of straight up adding children, we want to pull up any sub-statements and
					// simply append them here. This can generally happen if we have partially semi-colon'd code,
					// so `a; b` will cause this to look like `__autoconcat__(__statements__(a), b))` which we
					// want to turn into `__statements__(a, b)` rather than `__statements__(__statements__(a), b))`
					List<ParseTree> newChildren = new ArrayList<>();
					for(int i = 0; i < list.size(); i++) {
						ParseTree child = list.get(i);
						if(child.getData() instanceof CFunction cf && cf.val().equals(__statements__.NAME)) {
							for(ParseTree subChild : child.getChildren()) {
								newChildren.add(subChild);
							}
						} else {
							newChildren.add(child);
						}
					}
					tree.setChildren(newChildren);
				}
				return tree;
			}
		}

		private static void rewriteBinaryOperator(List<ParseTree> list, CSymbol symbol, int leftIndex) throws ConfigCompileException {
			ParseTree left = list.get(leftIndex);
			if(left.getData() instanceof CSymbol) {
				throw new ConfigCompileException("Unexpected symbol (" + left.getData().val() + ") before binary operator ("
						+ list.get(leftIndex + 1).getData().val() + ")", left.getTarget());
			}
			ParseTree right = list.get(leftIndex + 2);
			if(right.getData() instanceof CSymbol) {
				throw new ConfigCompileException("Unexpected symbol (" + right.getData().val() + ") after binary operator ("
						+ list.get(leftIndex + 1).getData().val() + ")", right.getTarget());
			}
			ParseTree conversion = new ParseTree(new CFunction(symbol.convert(), symbol.getTarget()), left.getFileOptions());
			conversion.addChild(left);
			conversion.addChild(right);
			list.set(leftIndex, conversion);
			list.remove(leftIndex + 1);
			list.remove(leftIndex + 1);
		}

		private static void rewriteParenthesis(List<ParseTree> list) throws ConfigCompileException {
			for(int listInd = list.size() - 1; listInd >= 1; listInd--) {
				Stack<ParseTree> executes = new Stack<>();
				while(listInd > 0) {
					ParseTree node = list.get(listInd);
					try {
						if(node.getData() instanceof CFunction cf
								&& cf.hasFunction()
								&& cf.getFunction() != null
								&& cf.getFunction().getName().equals(Compiler.p.NAME)) {
							ParseTree prevNode = list.get(listInd - 1);
							Mixed prevNodeVal = prevNode.getData();

							// Do not rewrite parenthesis like "@a = (1);" or "key: (value)" to execute().
							if(prevNodeVal instanceof CSymbol
									|| prevNodeVal instanceof CLabel || prevNodeVal instanceof CString) {
								break;
							}

							// Do not rewrite casts to execute() if the callable is the cast (i.e. "(type) (val)").
							if(prevNodeVal instanceof CFunction cfunc && cfunc.hasFunction() && cfunc.getFunction() != null
									&& cfunc.getFunction().getName().equals(Compiler.p.NAME) && prevNode.numberOfChildren() == 1
									&& (prevNode.getChildAt(0).getData().isInstanceOf(CClassType.TYPE)
											|| __type_ref__.createFromBareStringOrConcats(prevNode.getChildAt(0)) != null)) {
								break;
							}

							executes.push(node);
							list.remove(listInd--);
						} else {
							break;
						}
					} catch (ConfigCompileException e) {
						break; // The function does not exist. Ignore and handle as "not a p()".
					}
				}
				if(!executes.isEmpty()) {
					if(listInd >= 0) {
						ParseTree executableNode = list.get(listInd);
						// This is the core executable. Build statements in reverse order, then store this in place of
						// list.size() - 1
						// @x(@a)(@b) is execute(@a, execute(@b, @x))
						ParseTree execute = new ParseTree(
								new CFunction(DataHandling.execute.NAME, executableNode.getTarget()),
								executableNode.getFileOptions(), true);
						while(!executes.empty()) {
							execute.setChildren(executes.pop().getChildren());
							execute.addChild(executableNode);
							list.set(listInd, execute);
							executableNode = execute;
							execute = new ParseTree(
									new CFunction(DataHandling.execute.NAME, executableNode.getTarget()),
									executableNode.getFileOptions(), true);
						}
					} else if(executes.size() != 1) {
						throw new ConfigCompileException("Unexpected parenthesis", executes.peek().getTarget());
					}
					// Else just a regular p statement that happens to be last.
				}
			}
		}
	}

	@api
	@noprofile
	@hide("This is only used internally by the compiler.")
	public static class __statements__ extends DummyFunction {

		public static final String NAME = "__statements__";

		@Override
		public String getName() {
			return NAME;
		}

		@Override
		public String docs() {
			return "void {[...]} Used internally by the compiler. You shouldn't use it.";
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return CVoid.VOID;
		}

		@Override
		public CClassType getReturnType(Target t, List<CClassType> argTypes,
				List<Target> argTargets, Environment env, Set<ConfigCompileException> exceptions) {
			for(CClassType argType : argTypes) {
				if(argType == null) {
					return null; // An argument alters control flow, so this function will never return.
				}
			}
			return CVoid.TYPE;
		}

		@Override
		public boolean preResolveVariables() {
			return false;
		}

		@Override
		public ParseTree postParseRewrite(ParseTree ast, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs, Set<ConfigCompileException> exceptions) {
			for(ParseTree child : ast.getChildren()) {
				// We only expect functions here.
				// Bare strings already have a better exception from MethodScriptCompiler.checkLinearComponents()
				if(!(child.getData() instanceof CFunction)
						&& (!(child.getData() instanceof CBareString) || child.getData() instanceof CKeyword)) {
					exceptions.add(new ConfigCompileException("Not a statement.", child.getTarget()));
				}
			}
			return null;
		}
	}

	@api
	@noprofile
	@hide("This is only used internally by the compiler.")
	public static class __type_ref__ extends DummyFunction implements Optimizable {

		public static final String NAME = "__type_ref__";

		public static final String TYPE_REGEX = "[a-zA-Z0-9\\-_\\.]+";

		@Override
		public String getName() {
			return NAME;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class};
		}

		@Override
		public String docs() {
			return "mixed {string} Used internally by the compiler. You shouldn't use it.";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			throw new CRENotFoundException("\"" + args[0].val() + "\" cannot be resolved to a type.", t);
		}

		@Override
		public CClassType getReturnType(Target t, List<CClassType> argTypes,
				List<Target> argTargets, Environment env, Set<ConfigCompileException> exceptions) {
			return CClassType.TYPE;
		}

		public static ParseTree createASTNode(String typeName, Target t, FileOptions fileOptions) {
			ParseTree node = new ParseTree(new CFunction(NAME, t), fileOptions);
			node.addChild(new ParseTree(new CString(typeName, t), fileOptions, true));
			return node;
		}

		/**
		 * Converts already parsed AST node to a {@link __type_ref__} {@link ParseTree} node when the used syntax
		 * actually implies usage of a class type reference (also when the type reference cannot be resolved).
		 * @param typeNode - The node to convert.
		 * @return A {@link ParseTree} containing the resulting {@link __type_ref__},
		 * or {@code null} when conversion was not possible.
		 */
		public static ParseTree createFromBareStringOrConcats(ParseTree typeNode) {

			// Convert bare string types to __type_ref__.
			if(typeNode.getData().getClass().equals(CBareString.class)
					&& typeNode.getData().val().matches(TYPE_REGEX)) {
				return __type_ref__.createASTNode(
						typeNode.getData().val(), typeNode.getTarget(), typeNode.getFileOptions());
			}

			// Convert concatenated bare string types such as "concat(concat(ms, lang), int)" to "ms.lang.int".
			if(typeNode.getData() instanceof CFunction
					&& typeNode.getData().val().equals(concat.NAME)
					&& typeNode.getChildren().size() == 2) {
				String typeName = null;
				ParseTree node = typeNode;
				while(true) {
					ParseTree child1 = node.getChildAt(0);
					ParseTree child2 = node.getChildAt(1);

					if(child2.getData() instanceof CBareString) {
						typeName = child2.getData().val() + (typeName == null ? "" : "." + typeName);
					} else if(child2.getData() instanceof CClassType) {
						// "ms.lang.int" will have "int" parsed as CClassType. Convert to original string.
						String[] cClassTypeStrSplit = child2.getData().val().split("\\.");
						typeName = cClassTypeStrSplit[cClassTypeStrSplit.length - 1]
								+ (typeName == null ? "" : "." + typeName);
					} else {
						return null;
					}
					if(child1.getData() instanceof CBareString) {
						typeName = child1.getData().val() + "." + typeName;
						break;
					} else if(child1.getData() instanceof CClassType) {
						// "int.my.type" will have "int" parsed as CClassType. Convert to original string.
						String[] cClassTypeStrSplit = child1.getData().val().split("\\.");
						typeName = cClassTypeStrSplit[cClassTypeStrSplit.length - 1] + "." + typeName;
						break;
					} else if(!(child1.getData() instanceof CFunction) || !concat.NAME.equals(child1.getData().val())
							|| child1.getChildren().size() != 2) {
						return null;
					}
					node = child1;
				}
				if(typeName.matches(TYPE_REGEX)) {
					return __type_ref__.createASTNode(typeName, typeNode.getTarget(), typeNode.getFileOptions());
				}
			}

			return null;
		}

		@Override
		public ParseTree postParseRewrite(ParseTree ast, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs, Set<ConfigCompileException> exceptions) {

			// Attempt to resolve type reference to CClassType.
			String typeName = ast.getChildAt(0).getData().val();
			try {
				CClassType classType = CClassType.get(FullyQualifiedClassName.forName(typeName, ast.getTarget(), env));
				return new ParseTree(classType, ast.getFileOptions());
			} catch (CRECastException | ClassNotFoundException e) {
				return null;
			}
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {

			/*
			 * With static analysis enabled, the typechecker has already taken care of this AST node.
			 * Until MethodScript supports user types, we know that custom types cannot be resolved regardless of the
			 * outcome of static analysis. So we can generate an exception here until user type support is added.
			 */
			if(!StaticAnalysis.enabled()) {
				throw new ConfigCompileException(
						"\"" + children.get(0).getData().val() + "\" cannot be resolved to a type.", t);
			}
			return null;
		}
	}

	@api
	@hide("This is only used for testing unexpected error handling.")
	@noboilerplate
	public static class npe extends DummyFunction {

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			String s = null;
			if(args.length == 1) {
				s = args[0].val();
			}
			if(s == null) {
				throw new NullPointerException();
			} else {
				throw new NullPointerException(s);
			}
		}
	}

	@api
	@noprofile
	@hide("This is only used for testing.")
	public static class dyn extends DummyFunction {

		@Override
		public String docs() {
			return "exception {[argument]} Registers as a dynamic component, for optimization testing; that is"
					+ " to say, this will not be optimizable ever."
					+ " It simply returns the argument provided, or void if none.";
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(args.length == 0) {
				return CVoid.VOID;
			}
			return args[0];
		}
	}

	@api
	@hide("This is only used internally by the compiler, and will be removed at some point.")
	public static class __cbracket__ extends DummyFunction implements Optimizable {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			ParseTree node;
			if(children.isEmpty()) {
				node = new ParseTree(CVoid.VOID, fileOptions);
			} else if(children.size() == 1) {
				node = children.get(0);
			} else {
				//This shouldn't happen. If it does, it means that the autoconcat didn't already run.
				throw new ConfigCompileException("Unexpected children. This appears to be an error, as __autoconcat__ should have already been processed. Please"
						+ " report this error to the developer.", t);
			}
			return new ParseTree(new CBracket(node), fileOptions);
		}
	}

	@api
	@hide("This is only used internally by the compiler, and will be removed at some point.")
	public static class __cbrace__ extends DummyFunction implements Optimizable {

		public static final String NAME = "__cbrace__";

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			throw new ConfigCompileException("Unexpected use of braces", t);
		}
	}

	@api
	@hide("This is more of a compiler feature, rather than a function, and so it is hidden from normal"
			+ " documentation.")
	public static class __smart_string__ extends AbstractFunction {

		public static final String NAME = "__smart_string__";

		/**
		 * If token is a SMART_STRING, returns a dumb string if could be. If the token isn't a SMART_STRING, an
		 * Error is thrown, and if it is a SMART_STRING but it uses dynamic inputs, it throws a ConfigCompileException.
		 * @param token The token to convert.
		 * @return A regular STRING token, with an equivalent (perhaps slightly transformed) dumb string.
		 * @throws ConfigCompileException If the string contains an ivariable reference.
		 */
		public static Token getDumbStringOrFail(Token token) throws ConfigCompileException {
			if(token.type != Token.TType.SMART_STRING) {
				throw new Error("This method can only be called on SMART_STRING tokens.");
			}
			String original = token.value;
			StringBuilder b = new StringBuilder();
			for(int i = 0; i < original.length(); i++) {
				char c = original.charAt(i);
				char c2 = (i + 1 < original.length() ? original.charAt(i + 1) : '\0');
				if(c == '\\') {
					if(c2 == '@' || c2 == '\\') {
						b.append(c2);
						i++;
						continue;
					} else {
						throw new ConfigCompileException(
								"Invalid unhandled escape sequence passed to " + NAME + ": \\" + c2, token.target);
					}
				} else if(c == '@') {
					throw new ConfigCompileException("Cannot use smart strings here", token.target);
				} else {
					b.append(c);
				}
			}
			return new Token(Token.TType.STRING, b.toString(), token.target.copy());
		}

		@Override
		public String getName() {
			return NAME;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			throw new UnsupportedOperationException(getName() + " should have been compiled out. If you are reaching"
					+ " this, an error has occurred in the parser. Please report this error to the developers.");
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "none {string} This is a compiler construct, and is not normally used directly. It is created via double quoted strings.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ParseTree postParseRewrite(ParseTree ast, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs, Set<ConfigCompileException> exceptions) {
			List<ParseTree> children = ast.getChildren();
			Target t = ast.getTarget();
			FileOptions fileOptions = ast.getFileOptions();
			try {
				if(children.size() != 1) {
					throw new ConfigCompileException(getName() + " can only take one parameter", t);
				}
				if(!(children.get(0).getData().isInstanceOf(CString.TYPE))) {
					throw new ConfigCompileException("Only hardcoded strings may be passed into " + getName(), t);
				}
				String value = children.get(0).getData().val();

				// Rewrite empty smart strings to empty normal strings.
				if(value.isEmpty()) {
					return new ParseTree(new CString("", t), fileOptions);
				}

				// Parse the given string into string and ivariable parts, and add them as children to a new ParseTree.
				StringBuilder b = new StringBuilder();
				boolean inBrace = false;
				boolean inSimpleVar = false;
				boolean isDumbString = true;
				ParseTree root = new ParseTree(null, fileOptions);
				for(int i = 0; i < value.length(); i++) {
					char c = value.charAt(i);
					char c2 = (i + 1 < value.length() ? value.charAt(i + 1) : '\0');

					// The parser passes '\' as '\\' and literal '@' as '\@' for disambiguation in parsing here.
					if(c == '\\') {
						if(c2 == '@' || c2 == '\\') {
							b.append(c2);
							i++;
							continue;
						} else {
							throw new ConfigCompileException(
									"Invalid unhandled escape sequence passed to " + this.getName() + ": \\" + c2, t);
						}
					}

					if(c == '@') {
						isDumbString = false;
						if(c2 == '{') {
							//Start of a complex variable
							inBrace = true;
							i++; // Don't include this
						} else if(Character.isLetterOrDigit(c2) || c2 == '_') {
							//Start of a simple variable
							inSimpleVar = true;
						} else {
							// Loose @, this is a compile error
							throw new ConfigCompileException("Unexpected \"@\" in smart string."
									+ " If you want a literal at sign, escape it with \"\\@\".", t);
						}
						if(b.length() > 0) {
							root.addChild(new ParseTree(new CString(b.toString(), t), fileOptions));
							b = new StringBuilder();
						}
						continue;
					}
					if(inSimpleVar && !(Character.isLetterOrDigit(c) || c == '_')) {
						// End of simple var. The buffer is the variable name.
						String vname = b.toString();
						b = new StringBuilder();
						root.addChild(new ParseTree(new IVariable("@" + vname, t), fileOptions));
						inSimpleVar = false;
					}
					if(inBrace && c == '}') {
						// End of complex var. Still more parsing to be done though.
						String complex = b.toString().trim();
						b = new StringBuilder();
						inBrace = false;
						if(complex.matches("[a-zA-Z0-9_]+")) {
							//This is a simple variable name.
							root.addChild(new ParseTree(new IVariable("@" + complex, t), fileOptions));
							continue;
						} else {
							//Complex variable name, with arrays (or perhaps an error case)
							continue;
						}
					}
					b.append(c);
				}
				if(isDumbString) {
					return new ParseTree(new CString(b.toString(), t), fileOptions);
				}
				if(inBrace) {
					throw new ConfigCompileException("Missing end brace (}) in smart string", t);
				}
				if(inSimpleVar) {
					root.addChild(new ParseTree(new IVariable("@" + b.toString(), t), fileOptions));
				} else if(b.length() > 0) {
					root.addChild(new ParseTree(new CString(b.toString(), t), fileOptions));
				}

				// Concat multiple terms, or cast single term to string if a non-string term was found.
				assert root.numberOfChildren() != 0 : "Empty strings should have already been handled.";
				if(root.numberOfChildren() == 1) {
					ParseTree child = root.getChildAt(0);
					if(child.getData() instanceof CString) {
						return child; // A single normal string was found, so a cast is not required.
					}
					root.setData(new CFunction(_string.NAME, t));
				} else {
					root.setData(new CFunction(concat.NAME, t));
				}
				return root;
			} catch (ConfigCompileException e) {
				exceptions.add(e);
				return null;
			}
		}
	}
	@api
	@noprofile
	@hide("This is only used internally by the compiler.")
	public static class __cast__ extends DummyFunction {

		public static final String NAME = "__cast__";

		@Override
		public String getName() {
			return NAME;
		}

		@Override
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(CClassType.AUTO)
					.param(Mixed.TYPE, "value", "The value.")
					.param(CClassType.TYPE, "type", "The type.")
					.throwsEx(CRECastException.class, "When value cannot be cast to type.")
					.build();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[] {CRECastException.class};
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[] {2};
		}

		@Override
		public String docs() {
			return "mixed {mixed value, ClassType type} Used internally by the compiler. You shouldn't use it.";
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			Mixed value = args[0];
			CClassType type = ArgumentValidation.getClassType(args[1], t);
			if(!InstanceofUtil.isInstanceof(value, type, env)) {
				throw new CRECastException(
						"Cannot cast from " + value.typeof().getSimpleName() + " to " + type.getSimpleName() + ".", t);
			}
			return value;
		}

		@Override
		public CClassType typecheck(StaticAnalysis analysis,
				ParseTree ast, Environment env, Set<ConfigCompileException> exceptions) {

			// Fall back to default behavior for invalid usage.
			if(ast.numberOfChildren() != 2) {
				return super.typecheck(analysis, ast, env, exceptions);
			}

			// Typecheck value and type nodes.
			ParseTree valNode = ast.getChildAt(0);
			CClassType valType = analysis.typecheck(valNode, env, exceptions);
			StaticAnalysis.requireType(valType, Mixed.TYPE, valType.getTarget(), env, exceptions);
			ParseTree typeNode = ast.getChildAt(1);
			CClassType typeType = analysis.typecheck(typeNode, env, exceptions);
			StaticAnalysis.requireType(typeType, CClassType.TYPE, typeNode.getTarget(), env, exceptions);

			// Get cast-to type.
			if(!(typeNode.getData() instanceof CClassType)) {
				assert !exceptions.isEmpty() : "Missing compile-time type error for cast type argument.";
				return CClassType.AUTO;
			}
			CClassType castToType = (CClassType) typeNode.getData();

			// Generate redundancy warning for casts to the value type.
			if(castToType.equals(valType)) {
				env.getEnv(CompilerEnvironment.class).addCompilerWarning(ast.getFileOptions(),
						new CompilerWarning("Redundant cast to " + castToType.getSimpleName(), ast.getTarget(),
								FileOptions.SuppressWarning.UselessCode));
			}

			// Generate compile error for impossible casts.
			if(!InstanceofUtil.isInstanceof(valType, castToType, env)
					&& !InstanceofUtil.isInstanceof(castToType, valType, env)) {
				exceptions.add(new ConfigCompileException("Cannot cast from "
						+ valType.getSimpleName() + " to " + castToType.getSimpleName() + ".", ast.getTarget()));
			}

			// Return type that is being cast to.
			return castToType;
		}
	}
}
