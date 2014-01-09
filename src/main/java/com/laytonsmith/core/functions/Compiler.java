package com.laytonsmith.core.functions;

import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.annotations.noprofile;
import com.laytonsmith.core.*;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 *
 * @author layton
 */
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

		private static final CVoid VOID = new CVoid(Target.UNKNOWN);

		@Override
		public String getName() {
			return "p";
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
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			switch (nodes.length) {
				case 0:
					return VOID;
				case 1:
					return parent.eval(nodes[0], env);
				default: 
					return new __autoconcat__().execs(t, env, parent, nodes);
			}
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return new CVoid(t);
		}
	}

	@api
	@noprofile
	@hide("This is only used internally by the compiler.")
	public static class centry extends DummyFunction {

		@Override
		public String docs() {
			return "CEntry {label, content} Dynamically creates a CEntry. This is used internally by the "
					+ "compiler.";
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CEntry(args[0], args[1], t);
		}
	}

	@api
	@noprofile
	@hide("This is only used internally by the compiler.")
	public static class __autoconcat__ extends DummyFunction implements Optimizable {

		public static ParseTree getParseTree(List<ParseTree> children, FileOptions fo, Target t) {
			CFunction ac = new CFunction(new __autoconcat__().getName(), t);
			ParseTree tree = new ParseTree(ac, fo);
			tree.setChildren(children);
			return tree;
		}

		public static ParseTree getParseTree(ParseTree child, FileOptions fo, Target t) {
			CFunction ac = new CFunction(new __autoconcat__().getName(), t);
			ParseTree tree = new ParseTree(ac, fo);
			List<ParseTree> children = new ArrayList<ParseTree>();
			children.add(child);
			tree.setChildren(children);
			return tree;
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			throw new Error("Should not have gotten here, __autoconcat__ was not removed before runtime.");
		}

		@Override
		public String docs() {
			return "string {var1, [var2...]} This function should only be used by the compiler, behavior"
					+ " may be undefined if it is used in code.";
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> list) throws ConfigCompileException {
			return optimizeSpecial(list, true);
		}

		/**
		 * __autoconcat__ has special optimization techniques needed, since it's
		 * really a part of the compiler itself, and not so much a function. It
		 * being a function is merely a convenience, so we can defer processing
		 * until after parsing. While it is tightly coupled with the compiler,
		 * this is ok, since it's really a compiler mechanism more than a
		 * function.
		 *
		 * @param t
		 * @param list
		 * @return
		 */
		public ParseTree optimizeSpecial(List<ParseTree> list, boolean returnSConcat) throws ConfigCompileException {
			//If any of our nodes are CSymbols, we have different behavior
			boolean inSymbolMode = false; //caching this can save Xn

			//In the lexer, array('one' : 1) would return 3 tokens (note the spaces).
			//We need to correct this by changing this to 'one:' (CLabel) and continuing.
			for(int i = 0; i < list.size() - 1; i++){
				if(list.get(i).getData() instanceof CLabel){
					//else if it's empty, then the previous token is the label
					if(list.get(i).getData().val().trim().equals("")){
						//If i == 0, compile error
						if(i == 0){
							throw new ConfigCompileException("Unexpected \":\" symbol", list.get(i).getTarget());
						}
						//Unless it's not a primitive, in which case, compiler error
						//TODO: This should be changed to primitive later. For now, it's
						//just making sure it's not a function
						if(list.get(i - 1).getData() instanceof CFunction){
							throw new ConfigCompileException("Expecting label, but found " + list.get(i - 1).getData().val(), list.get(i - 1).getTarget());
						}
						Construct c = new CLabel(list.get(i - 1).getData());
						list.set(i - 1, new ParseTree(c, list.get(i - 1).getFileOptions()));
						list.remove(i);
					}
				}
			}
			//Assignment
			//Note that we are walking the array in reverse, because multiple assignments,
			//say @a = @b = 1 will break if they look like assign(assign(@a, @b), 1),
			//they need to be assign(@a, assign(@b, 1)). As a variation, we also have
			//to support something like 1 + @a = 2, which will turn into add(1, assign(@a, 2),
			//and 1 + @a = @b + 3 would turn into add(1, assign(@a, add(@b, 3))).
			for (int i = list.size() - 2; i >= 0; i--) {
				ParseTree node = list.get(i + 1);
				if (node.getData() instanceof CSymbol && ((CSymbol) node.getData()).isAssignment()) {
					CSymbol sy = (CSymbol) node.getData();
					String conversionFunction = sy.convertAssignment();
					ParseTree lhs = list.get(i);
					if (conversionFunction != null) {
						ParseTree conversion = new ParseTree(new CFunction(conversionFunction, node.getTarget()), node.getFileOptions());
						//grab the entire right side, and turn it into an operation with the left side.
						//We have to take the entire right up to the next construct not followed by an
						//operator (or the end)
						try {
							ParseTree rhs;
							if (i < list.size() - 3) {
								//Need to autoconcat
								ParseTree ac = new ParseTree(new CFunction("__autoconcat__", Target.UNKNOWN), lhs.getFileOptions());
								int index = i + 2;
								ac.addChild(list.get(index));
								list.remove(index);
								while (true) {
									if (list.size() > index && list.get(index).getData() instanceof CSymbol) {
										//Add the next two children, (the symbol then the item)
										//and continue.
										ac.addChild(list.get(index));
										ac.addChild(list.get(index + 1));
										list.remove(index);
										list.remove(index);
										continue;
									} else {
										break;
									}
								}
								//Set this subset into the correct slot, the rest of the
								//code will grab it correctly that way.
								list.add(i + 2, ac);
							}
							rhs = list.get(i + 2);
							conversion.addChild(lhs);
							conversion.addChild(rhs);
							list.set(i + 2, conversion);
						} catch (IndexOutOfBoundsException e) {
							throw new ConfigCompileException("Invalid symbol listed", node.getTarget());
						}
					}
					//Simple assignment now
					ParseTree assign = new ParseTree(new CFunction("assign", node.getTarget()), node.getFileOptions());
					ParseTree rhs;
					if (i < list.size() - 3) {
						//Need to autoconcat
						ParseTree ac = new ParseTree(new CFunction("__autoconcat__", Target.UNKNOWN), lhs.getFileOptions());
						int index = i + 2;
						//As an incredibly special case, because (@value = !@value) is supported, and
						//! hasn't been reduced yet, we want to check for that case, and if present, grab
						//two symbols.
						if(list.get(index).getData() instanceof CSymbol && list.get(index).getData().val().equals("!")){
							ac.addChild(list.get(index));
							list.remove(index);
						}
						ac.addChild(list.get(index));
						list.remove(index);
						while (true) {
							if (list.size() > index && list.get(index).getData() instanceof CSymbol) {
								//Add the next two children, (the symbol then the item)
								//and continue.
								ac.addChild(list.get(index));
								ac.addChild(list.get(index + 1));
								list.remove(index);
								list.remove(index);
								continue;
							} else {
								break;
							}
						}
						//Set this subset into the correct slot, the rest of the
						//code will grab it correctly that way.
						list.add(i + 2, ac);
					}
					rhs = list.get(i + 2);
					assign.addChild(lhs);
					assign.addChild(rhs);
					list.set(i, assign);
					list.remove(i + 1);
					list.remove(i + 1);
				}
			}
			//postfix
			for (int i = 0; i < list.size(); i++) {
				ParseTree node = list.get(i);
				if (node.getData() instanceof CSymbol) {
					inSymbolMode = true;
				}
				if (node.getData() instanceof CSymbol && ((CSymbol) node.getData()).isPostfix()) {
					if (i - 1 >= 0) {// && list.get(i - 1).getData() instanceof IVariable) {
						CSymbol sy = (CSymbol) node.getData();
						ParseTree conversion;
						if (sy.val().equals("++")) {
							conversion = new ParseTree(new CFunction("postinc", node.getTarget()), node.getFileOptions());
						} else {
							conversion = new ParseTree(new CFunction("postdec", node.getTarget()), node.getFileOptions());
						}
						conversion.addChild(list.get(i - 1));
						list.set(i - 1, conversion);
						list.remove(i);
						i--;
					}
				}
			}
			if (inSymbolMode) {
				try {
					//look for unary operators
					for (int i = 0; i < list.size() - 1; i++) {
						ParseTree node = list.get(i);
						if (node.getData() instanceof CSymbol && ((CSymbol) node.getData()).isUnary()) {
							ParseTree conversion;
							if (node.getData().val().equals("-") || node.getData().val().equals("+")) {
								//These are special, because if the values to the left isn't a symbol,
								//it's not unary
								if ((i == 0 || list.get(i - 1).getData() instanceof CSymbol)
										&& !(list.get(i + 1).getData() instanceof CSymbol)) {
									if (node.getData().val().equals("-")) {
										//We have to negate it
										conversion = new ParseTree(new CFunction("neg", node.getTarget()), node.getFileOptions());
									} else {
										conversion = new ParseTree(new CFunction("p", node.getTarget()), node.getFileOptions());
									}
								} else {
									continue;
								}
							} else {
								conversion = new ParseTree(new CFunction(((CSymbol) node.getData()).convert(), node.getTarget()), node.getFileOptions());
							}
							conversion.addChild(list.get(i + 1));
							list.set(i, conversion);
							list.remove(i + 1);
							i--;
						}
					}

					for (int i = 0; i < list.size() - 1; i++) {
						ParseTree next = list.get(i + 1);
						if (next.getData() instanceof CSymbol) {
							if (((CSymbol) next.getData()).isExponential()) {
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
					for (int i = 0; i < list.size() - 1; i++) {
						ParseTree next = list.get(i + 1);
						if (next.getData() instanceof CSymbol) {
							CSymbol nextData = (CSymbol) next.getData();
							if (nextData.isMultaplicative() && !nextData.isAssignment()) {
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
					for (int i = 0; i < list.size() - 1; i++) {
						ParseTree next = list.get(i + 1);
						if (next.getData() instanceof CSymbol && ((CSymbol) next.getData()).isAdditive() && !((CSymbol) next.getData()).isAssignment()) {
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
					for (int i = 0; i < list.size() - 1; i++) {
						ParseTree node = list.get(i + 1);
						if (node.getData() instanceof CSymbol && ((CSymbol) node.getData()).isRelational()) {
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
					for (int i = 0; i < list.size() - 1; i++) {
						ParseTree node = list.get(i + 1);
						if (node.getData() instanceof CSymbol && ((CSymbol) node.getData()).isEquality()) {
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
					for (int i = 0; i < list.size() - 1; i++) {
						ParseTree node = list.get(i + 1);
						if (node.getData() instanceof CSymbol && ((CSymbol) node.getData()).isLogicalAnd()) {
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
					for (int i = 0; i < list.size() - 1; i++) {
						ParseTree node = list.get(i + 1);
						if (node.getData() instanceof CSymbol && ((CSymbol) node.getData()).isLogicalOr()) {
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

			//Look for a CEntry here
			if (list.size() >= 1) {
				ParseTree node = list.get(0);
				if (node.getData() instanceof CLabel) {
					ParseTree value = new ParseTree(new CFunction("__autoconcat__", node.getTarget()), node.getFileOptions());
					for (int i = 1; i < list.size(); i++) {
						value.addChild(list.get(i));
					}
					ParseTree ce = new ParseTree(new CFunction("centry", node.getTarget()), node.getFileOptions());
					ce.addChild(node);
					ce.addChild(value);
					return ce;
				}
			}

			//We've eliminated the need for __autoconcat__ either way, however, if there are still arguments
			//left, it needs to go to sconcat, which MAY be able to be further optimized, but that will
			//be handled in MethodScriptCompiler's optimize function. Also, we must scan for CPreIdentifiers,
			//which may be turned into a function
			if (list.size() == 1) {
				return list.get(0);
			} else {
				for (int i = 0; i < list.size(); i++) {
					if (list.get(i).getData().getCType() == Construct.ConstructType.IDENTIFIER) {
						if (i == 0) {
							//Yup, it's an identifier
							CFunction identifier = new CFunction(list.get(i).getData().val(), list.get(i).getTarget());
							list.remove(0);
							ParseTree child = list.get(0);
							if (list.size() > 1) {
								child = new ParseTree(new CFunction("sconcat", Target.UNKNOWN), child.getFileOptions());
								child.setChildren(list);
							}
							try {
								Function f = (Function) FunctionList.getFunction(identifier);
								ParseTree node = new ParseTree(f.execs(identifier.getTarget(), null, null, child), child.getFileOptions());
								return node;
							} catch (Exception e) {
								throw new Error("Unknown function " + identifier.val() + "?");
							}
						} else {
							//Hmm, this is weird. I'm not sure what condition this can happen in
							throw new ConfigCompileException("Unexpected IDENTIFIER? O.o Please report a bug,"
									+ " and include the script you used to get this error.", Target.UNKNOWN);
						}
					}
				}
				ParseTree tree;
				FileOptions options = new FileOptions(new HashMap<String, String>());
				if (!list.isEmpty()) {
					options = list.get(0).getFileOptions();
				}
				if (returnSConcat) {
					tree = new ParseTree(new CFunction("sconcat", Target.UNKNOWN), options);
				} else {
					tree = new ParseTree(new CFunction("concat", Target.UNKNOWN), options);
				}
				tree.setChildren(list);
				return tree;
			}
		}
	}

	@api
	@hide("This is only used for testing unexpected error handling.")
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
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			String s = null;
			if(args.length == 1){
				s = args[0].val();
			}
			if(s == null){
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
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if (args.length == 0) {
				return new CVoid(t);
			}
			return args[0];
		}
	}

	@api
	@hide("This is only used internally by the compiler, and will be removed at some point.")
	public static class __cbracket__ extends DummyFunction implements Optimizable {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			FileOptions options = new FileOptions(new HashMap<String, String>());
			if (!children.isEmpty()) {
				options = children.get(0).getFileOptions();
			}
			ParseTree node;
			if (children.isEmpty()) {
				node = new ParseTree(new CVoid(t), options);
			} else if (children.size() == 1) {
				node = children.get(0);
			} else {
				//This shouldn't happen. If it does, it means that the autoconcat didn't already run.
				throw new ConfigCompileException("Unexpected children. This appears to be an error, as __autoconcat__ should have already been processed. Please"
						+ " report this error to the developer.", t);
			}
			return new ParseTree(new CBracket(node), options);
		}
	}

	@api
	@hide("This is only used internally by the compiler, and will be removed at some point.")
	public static class __cbrace__ extends DummyFunction implements Optimizable {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			FileOptions options = new FileOptions(new HashMap<String, String>());
			if (!children.isEmpty()) {
				options = children.get(0).getFileOptions();
			}
			ParseTree node;
			if (children.isEmpty()) {
				node = new ParseTree(new CVoid(t), options);
			} else if (children.size() == 1) {
				node = children.get(0);
			} else {
				//This shouldn't happen. If it does, it means that the autoconcat didn't already run.
				throw new ConfigCompileException("Unexpected children. This appears to be an error, as __autoconcat__ should have already been processed. Please"
						+ " report this error to the developer.", t);
			}
			return new ParseTree(new CBrace(node), options);
		}
	}
}
