package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.annotations.noprofile;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.constructs.CBracket;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CEntry;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CLabel;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CSymbol;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
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
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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
	public static class p extends DummyFunction {

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
			switch(nodes.length) {
				case 0:
					return CVoid.VOID;
				case 1:
					return parent.eval(nodes[0], env);
				default:
					return new __autoconcat__().execs(t, env, parent, nodes);
			}
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return CVoid.VOID;
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

		public static ParseTree getParseTree(List<ParseTree> children, FileOptions fo, Target t) {
			CFunction ac = new CFunction(__autoconcat__.NAME, t);
			ParseTree tree = new ParseTree(ac, fo);
			tree.setChildren(children);
			return tree;
		}

		public static ParseTree getParseTree(ParseTree child, FileOptions fo, Target t) {
			CFunction ac = new CFunction(__autoconcat__.NAME, t);
			ParseTree tree = new ParseTree(ac, fo);
			List<ParseTree> children = new ArrayList<>();
			children.add(child);
			tree.setChildren(children);
			return tree;
		}

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
		 * Rewrites this __autoconcat__ node to an executable AST node as part of compilation. This either results in
		 * this __autoconcat__ node being replaced or in a compile error if the __autoconcat__ cannot be converted to
		 * an executable AST node. This being a function is merely a convenient way to defer processing until after
		 * parsing, meaning that it should ALWAYS be rewritten before executing the AST.
		 * @param list - A list containing all {@link ParseTree} children of this __autoconcat__.
		 * @param returnSConcat - If parsing results in only one child function, then this argument is ignored.
		 * If {@code true}, the resulting parsed functions will be wrapped into {@link sconcat}.
		 * If {@code false}, the resulting parsed functions will be wrapped into {@link __statements__}.
		 * @return The executable AST node, representing the code/tokens in this __autoconcat__.
		 * @throws ConfigCompileException If this __autoconcat__ cannot be converted to an executable AST node.
		 */
		public ParseTree rewrite(List<ParseTree> list, boolean returnSConcat,
				Set<Class<? extends Environment.EnvironmentImpl>> envs) throws ConfigCompileException {
			//If any of our nodes are CSymbols, we have different behavior
			boolean inSymbolMode = false; //caching this can save Xn

			//Assignment
			//Note that we are walking the array in reverse, because multiple assignments,
			//say @a = @b = 1 will break if they look like assign(assign(@a, @b), 1),
			//they need to be assign(@a, assign(@b, 1)). As a variation, we also have
			//to support something like 1 + @a = 2, which will turn into add(1, assign(@a, 2),
			//and 1 + @a = @b + 3 would turn into add(1, assign(@a, add(@b, 3))).
			for(int i = list.size() - 2; i >= 0; i--) {
				ParseTree node = list.get(i + 1);
				if(node.getData() instanceof CSymbol && ((CSymbol) node.getData()).isAssignment()) {
					ParseTree lhs = list.get(i);
					ParseTree assignNode = new ParseTree(
							new CFunction(assign.NAME, node.getTarget()), node.getFileOptions());
					ParseTree rhs;
					if(i < list.size() - 3) {
						//Need to autoconcat
						ParseTree ac = new ParseTree(
								new CFunction(__autoconcat__.NAME, node.getTarget()), lhs.getFileOptions());
						int index = i + 2;
						// add all preceding symbols
						while(list.size() > index + 1 && list.get(index).getData() instanceof CSymbol) {
							ac.addChild(list.get(index));
							list.remove(index);
						}
						// add first item
						ac.addChild(list.get(index));
						list.remove(index);
						// loop through all additional symbols/items
						while(list.size() > index + 1 && list.get(index).getData() instanceof CSymbol) {
							// add all contiguous symbols
							do {
								// symbols first
								ac.addChild(list.get(index));
								list.remove(index);
							} while(list.size() > index && list.get(index).getData() instanceof CSymbol);
							if(list.size() <= index) {
								throw new ConfigCompileException("Unexpected end of statement",
										list.get(list.size() - 1).getTarget());
							}
							// then item
							ac.addChild(list.get(index));
							list.remove(index);
						}
						//Set this subset into the correct slot, the rest of the
						//code will grab it correctly that way.
						list.add(i + 2, ac);
					}
					if(list.size() <= i + 2) {
						throw new ConfigCompileException("Unexpected end of statement", list.get(i).getTarget());
					}

					// Additive assignment
					CSymbol sy = (CSymbol) node.getData();
					String conversionFunction = sy.convertAssignment();
					if(conversionFunction != null) {
						ParseTree conversion = new ParseTree(new CFunction(conversionFunction, node.getTarget()), node.getFileOptions());
						conversion.addChild(lhs);
						conversion.addChild(list.get(i + 2));
						list.set(i + 2, conversion);
					}

					rhs = list.get(i + 2);
					assignNode.addChild(lhs);
					assignNode.addChild(rhs);
					list.set(i, assignNode);
					list.remove(i + 1);
					list.remove(i + 1);
				}
			}
			//postfix
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
			if(inSymbolMode) {
				try {
					//look for unary operators
					for(int i = 0; i < list.size() - 1; i++) {
						ParseTree node = list.get(i);
						if(node.getData() instanceof CSymbol && ((CSymbol) node.getData()).isUnary()) {
							ParseTree conversion;
							if(node.getData().val().equals("-") || node.getData().val().equals("+")) {
								//These are special, because if the values to the left isn't a symbol,
								//it's not unary
								if((i == 0 || list.get(i - 1).getData() instanceof CSymbol)
										&& !(list.get(i + 1).getData() instanceof CSymbol)) {
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

					//Exponential
					for(int i = 0; i < list.size() - 1; i++) {
						ParseTree next = list.get(i + 1);
						if(next.getData() instanceof CSymbol) {
							if(((CSymbol) next.getData()).isExponential()) {
								ParseTree conversion = new ParseTree(new CFunction(((CSymbol) next.getData()).convert(), next.getTarget()), next.getFileOptions());
								conversion.addChild(list.get(i));
								conversion.addChild(list.get(i + 2));
								list.set(i, conversion);
								list.remove(i + 1);
								list.remove(i + 1);
								i--;
							}
						}
					}

					//Multiplicative
					for(int i = 0; i < list.size() - 1; i++) {
						ParseTree next = list.get(i + 1);
						if(next.getData() instanceof CSymbol) {
							CSymbol nextData = (CSymbol) next.getData();
							if(nextData.isMultaplicative() && !nextData.isAssignment()) {
								ParseTree conversion = new ParseTree(new CFunction(((CSymbol) next.getData()).convert(), next.getTarget()), next.getFileOptions());
								conversion.addChild(list.get(i));
								conversion.addChild(list.get(i + 2));
								list.set(i, conversion);
								list.remove(i + 1);
								list.remove(i + 1);
								i--;
							}
						}
					}
					//Additive
					for(int i = 0; i < list.size() - 1; i++) {
						ParseTree next = list.get(i + 1);
						if(next.getData() instanceof CSymbol && ((CSymbol) next.getData()).isAdditive() && !((CSymbol) next.getData()).isAssignment()) {
							ParseTree conversion = new ParseTree(new CFunction(((CSymbol) next.getData()).convert(), next.getTarget()), next.getFileOptions());
							conversion.addChild(list.get(i));
							conversion.addChild(list.get(i + 2));
							list.set(i, conversion);
							list.remove(i + 1);
							list.remove(i + 1);
							i--;
						}
					}
					//relational
					for(int i = 0; i < list.size() - 1; i++) {
						ParseTree node = list.get(i + 1);
						if(node.getData() instanceof CSymbol && ((CSymbol) node.getData()).isRelational()) {
							CSymbol sy = (CSymbol) node.getData();
							ParseTree conversion = new ParseTree(new CFunction(sy.convert(), node.getTarget()), node.getFileOptions());
							conversion.addChild(list.get(i));
							conversion.addChild(list.get(i + 2));
							list.set(i, conversion);
							list.remove(i + 1);
							list.remove(i + 1);
							i--;
						}
					}
					//equality
					for(int i = 0; i < list.size() - 1; i++) {
						ParseTree node = list.get(i + 1);
						if(node.getData() instanceof CSymbol && ((CSymbol) node.getData()).isEquality()) {
							CSymbol sy = (CSymbol) node.getData();
							ParseTree conversion = new ParseTree(new CFunction(sy.convert(), node.getTarget()), node.getFileOptions());
							conversion.addChild(list.get(i));
							conversion.addChild(list.get(i + 2));
							list.set(i, conversion);
							list.remove(i + 1);
							list.remove(i + 1);
							i--;
						}
					}
					// default and
					for(int i = 0; i < list.size() - 1; i++) {
						ParseTree node = list.get(i + 1);
						if(node.getData() instanceof CSymbol && ((CSymbol) node.getData()).isDefaultAnd()) {
							CSymbol sy = (CSymbol) node.getData();
							ParseTree conversion = new ParseTree(new CFunction(sy.convert(), node.getTarget()), node.getFileOptions());
							conversion.addChild(list.get(i));
							conversion.addChild(list.get(i + 2));
							list.set(i, conversion);
							list.remove(i + 1);
							list.remove(i + 1);
							i--;
						}
					}

					// default or
					for(int i = 0; i < list.size() - 1; i++) {
						ParseTree node = list.get(i + 1);
						if(node.getData() instanceof CSymbol && ((CSymbol) node.getData()).isDefaultOr()) {
							CSymbol sy = (CSymbol) node.getData();
							ParseTree conversion = new ParseTree(new CFunction(sy.convert(), node.getTarget()), node.getFileOptions());
							conversion.addChild(list.get(i));
							conversion.addChild(list.get(i + 2));
							list.set(i, conversion);
							list.remove(i + 1);
							list.remove(i + 1);
							i--;
						}
					}

					//logical and
					for(int i = 0; i < list.size() - 1; i++) {
						ParseTree node = list.get(i + 1);
						if(node.getData() instanceof CSymbol && ((CSymbol) node.getData()).isLogicalAnd()) {
							CSymbol sy = (CSymbol) node.getData();
							ParseTree conversion = new ParseTree(new CFunction(sy.convert(), node.getTarget()), node.getFileOptions());
							conversion.addChild(list.get(i));
							conversion.addChild(list.get(i + 2));
							list.set(i, conversion);
							list.remove(i + 1);
							list.remove(i + 1);
							i--;
						}
					}
					//logical or
					for(int i = 0; i < list.size() - 1; i++) {
						ParseTree node = list.get(i + 1);
						if(node.getData() instanceof CSymbol && ((CSymbol) node.getData()).isLogicalOr()) {
							CSymbol sy = (CSymbol) node.getData();
							ParseTree conversion = new ParseTree(new CFunction(sy.convert(), node.getTarget()), node.getFileOptions());
							conversion.addChild(list.get(i));
							conversion.addChild(list.get(i + 2));
							list.set(i, conversion);
							list.remove(i + 1);
							list.remove(i + 1);
							i--;
						}
					}
				} catch (IndexOutOfBoundsException e) {
					throw new ConfigCompileException("Unexpected symbol (" + list.get(list.size() - 1).getData().val() + "). Did you forget to quote your symbols?", list.get(list.size() - 1).getTarget());
				}
			}

			// Look for typed assignments
			for(int k = 0; k < list.size(); k++) {
				if(list.get(k).getData().equals(CVoid.VOID) || list.get(k).getData().isInstanceOf(CClassType.TYPE)) {
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
										&& list.get(k).getData().equals(CVoid.VOID)) {
									throw new ConfigCompileException("Variables may not be of type void",
											list.get(k).getTarget());
								}
								ParseTree type = list.remove(k);
								List<ParseTree> children = list.get(k).getChildren();
								children.add(0, type);
								list.get(k).setChildren(children);
								break;
							default:
								throw new ConfigCompileException("Unexpected ClassType \"" + list.get(k).getData().val() + "\"", list.get(k).getTarget());
						}
					} else if(list.get(k + 1).getData() instanceof IVariable) {
						// Not an assignment, a random variable declaration though.
						ParseTree node = new ParseTree(new CFunction(assign.NAME, list.get(k).getTarget()), list.get(k).getFileOptions());
						node.addChild(list.get(k));
						node.addChild(list.get(k + 1));
						node.addChild(new ParseTree(CNull.UNDEFINED, list.get(k).getFileOptions()));
						list.set(k, node);
						list.remove(k + 1);
					} else if(list.get(k + 1).getData() instanceof CLabel) {
						ParseTree node = new ParseTree(new CFunction(assign.NAME, list.get(k).getTarget()), list.get(k).getFileOptions());
						ParseTree labelNode = new ParseTree(new CLabel(node.getData()), list.get(k).getFileOptions());
						labelNode.addChild(list.get(k));
						labelNode.addChild(new ParseTree(((CLabel) list.get(k + 1).getData()).cVal(), list.get(k).getFileOptions()));
						labelNode.addChild(new ParseTree(CNull.UNDEFINED, list.get(k).getFileOptions()));
						list.set(k, labelNode);
						list.remove(k + 1);
					} else {
						throw new ConfigCompileException("Unexpected ClassType", list.get(k).getTarget());
					}
				}
			}

			//Look for a CEntry here
			if(list.size() >= 1) {
				ParseTree node = list.get(0);
				if(node.getData() instanceof CLabel) {
					ParseTree value = new ParseTree(new CFunction(__autoconcat__.NAME, node.getTarget()), node.getFileOptions());
					for(int i = 1; i < list.size(); i++) {
						value.addChild(list.get(i));
					}
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
				return list.get(0);
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
								return node;
							} catch (Exception e) {
								throw new Error("Unknown function " + identifier.val() + "?");
							}
						} else {
							//Hmm, this is weird. I'm not sure what condition this can happen in
							throw new ConfigCompileException("Unexpected IDENTIFIER? O.o Please report a bug,"
									+ " and include the script you used to get this error. At or around:", list.get(i).getTarget());
						}
					}
				}
				ParseTree tree;
				FileOptions options = new FileOptions(new HashMap<>());
				Target t = Target.UNKNOWN;
				if(!list.isEmpty()) {
					options = list.get(0).getFileOptions();
					t = list.get(0).getTarget();
				}
				if(returnSConcat) {
					tree = new ParseTree(new CFunction(sconcat.NAME, t), options);
				} else {
					tree = new ParseTree(new CFunction(__statements__.NAME, t), options);
				}
				tree.setChildren(list);
				return tree;
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
			return CVoid.TYPE;
		}

		@Override
		public boolean preResolveVariables() {
			return false;
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
				ParseTree root = new ParseTree(null, fileOptions);
				for(int i = 0; i < value.length(); i++) {
					char c = value.charAt(i);
					char c2 = (i + 1 < value.length() ? value.charAt(i + 1) : '\0');

					// The parser passes '\' as '\\' and literal '@' as '\@' for disambiguation in parsing here.
					if(c == '\\') {
						if(c2 == '@') {
							b.append('@');
						} else if(c2 == '\\') {
							b.append('\\');
						} else {
							throw new ConfigCompileException(
									"Invalid unhandled escape sequence passed to " + this.getName() + ": \\" + c2, t);
						}
						i++;
						continue;
					}

					if(c == '@') {
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
}
