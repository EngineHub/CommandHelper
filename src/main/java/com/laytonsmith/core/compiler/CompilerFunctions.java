package com.laytonsmith.core.compiler;

import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.noprofile;
import com.laytonsmith.core.*;
import com.laytonsmith.core.arguments.ArgList;
import com.laytonsmith.core.arguments.Argument;
import com.laytonsmith.core.arguments.ArgumentBuilder;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.DataHandling;
import com.laytonsmith.core.functions.DummyFunction;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.StringHandling;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Though technically a function container, none of these functions should
 * typically be used in the course of normal scripting, and ideally they would
 * be removed at some point in the future. (The paradigm doesn't seem very stable).
 * For now, functions like __autoconcat__ are a necessary evil to simplify other parts
 * of the code, but at the price of complicating other things.
 */
public class CompilerFunctions {

	public static String docs() {
		return "Compiler internal functions should be declared here. If you're reading this from anywhere"
				+ " but the source code, there's a bug, because these functions shouldn't be public or used"
				+ " in a script.";
	}

	@api
	@noprofile
	public static class p extends DummyFunction {

		private static final CVoid VOID = new CVoid(Target.UNKNOWN);

		@Override
		public String getName() {
			return "p";
		}

		@Override
		public String docs() {
			return "Used internally by the compiler. You shouldn't use it.";
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			switch (nodes.length) {
				case 0:
					return VOID;
				case 1:
					return parent.eval(nodes[0], env);
				default: 
					return new __autoconcat__().execs(t, env, parent, nodes);
			}
		}

		public Construct exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return new CVoid(t);
		}

		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(new Argument("", CArray.class, "c"));
		}
	}

	@api
	@noprofile
	public static class centry extends DummyFunction {

		@Override
		public String docs() {
			return "Dynamically creates a CEntry. This is used internally by the "
					+ "compiler.";
		}

		public Construct exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			ArgList list = this.getBuilder().parse(args, this, t);
			return new CEntry((Construct)list.get("label"), (Construct)list.get("content"), t);
		}

		@Override
		public Argument returnType() {
			return new Argument("", CEntry.class);
		}

		@Override
		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(new Argument("", CString.class, "label"), new Argument("", CString.class, "content"));
		}
	}

	@api
	@noprofile
	public static class __autoconcat__ extends DummyFunction implements Optimizable {
		
		private static final String __autoconcat__ = new __autoconcat__().getName();
		private static final String assign = new DataHandling.assign().getName();
		private static final String postinc = new com.laytonsmith.core.functions.Math.postinc().getName();
		private static final String postdec = new com.laytonsmith.core.functions.Math.postdec().getName();
		private static final String neg = new com.laytonsmith.core.functions.Math.neg().getName();
		private static final String p = new p().getName();
		private static final String __cbrace__ = new __cbrace__().getName();
		private static final String centry = new centry().getName();
		private static final String sconcat = new StringHandling.sconcat().getName();
		private static final String concat = new StringHandling.concat().getName();

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

		public Construct exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			throw new Error("Should not have gotten here, " + getName() + " was not removed before runtime.");
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
		public ParseTree optimizeDynamic(Target t, Environment env, List<ParseTree> list) throws ConfigCompileException {
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
		public static ParseTree optimizeSpecial(List<ParseTree> list, boolean returnSConcat) throws ConfigCompileException {
			//If any of our nodes are CSymbols, we have different behavior
			boolean inSymbolMode = false; //caching this can save Xn
			
//			//[Bracket syntax] - This needs resolving far before most other operations.
//			for (int i = 0; i < list.size() - 1; i++) {
//				if (list.size() > i + 1) {
//					ParseTree ident = list.get(i);
//					ParseTree node = list.get(i + 1);
//					if (node.getData() instanceof CBracket) {
//						ParseTree replacement = new ParseTree(new CFunction("array_get", node.getTarget()), node.getFileOptions());
//						ParseTree newNode = new ParseTree(new CFunction("__autoconcat__", node.getTarget()), node.getFileOptions());
//						newNode.setChildren(node.getChildren());
//						replacement.addChild(ident);
//						replacement.addChild(newNode);
//						list.set(i, replacement);
//						list.remove(i + 1);
//						i--;
//					}
//				}
//			}

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
								List<ParseTree> values = new ArrayList<ParseTree>();
								int index = i + 2;
								values.add(list.get(index));
								list.remove(index);
								while (true) {
									if (list.size() > index && list.get(index).getData() instanceof CSymbol) {
										//Add the next two children, (the symbol then the item)
										//and continue.
										values.add(list.get(index));
										values.add(list.get(index + 1));
										list.remove(index);
										list.remove(index);
										continue;
									} else {
										break;
									}
								}
								//Set this subset into the correct slot, the rest of the
								//code will grab it correctly that way.
								ParseTree ac = optimizeSpecial(values, returnSConcat);
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
					ParseTree assignTree = new ParseTree(new CFunction(assign, node.getTarget()), node.getFileOptions());
					ParseTree rhs;
					if (i < list.size() - 3) {
						//Need to autoconcat
						List<ParseTree> values = new ArrayList<ParseTree>();
						int index = i + 2;
						values.add(list.get(index));
						list.remove(index);
						while (true) {
							if (list.size() > index && list.get(index).getData() instanceof CSymbol) {
								//Add the next two children, (the symbol then the item)
								//and continue.
								values.add(list.get(index));
								values.add(list.get(index + 1));
								list.remove(index);
								list.remove(index);
								continue;
							} else {
								break;
							}
						}
						//Set this subset into the correct slot, the rest of the
						//code will grab it correctly that way.
						ParseTree ac = optimizeSpecial(values, returnSConcat);
						list.add(i + 2, ac);
					}
					rhs = list.get(i + 2);
					assignTree.addChild(lhs);
					assignTree.addChild(rhs);
					list.set(i, assignTree);
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
						if(list.size() > i + 1){
							//Ambiguous. Trigger warning.
							CHLog.GetLogger().CompilerWarning(CompilerWarning.AmbiguousUnaryOperators, 
									"You have used " + sy.val() + " in an ambiguous way. Consider using parenthesis to"
									+ " make it more obvious what you are trying to do.", sy.getTarget(), node.getFileOptions());
						}
						ParseTree conversion;
						if (sy.val().equals("++")) {
							conversion = new ParseTree(new CFunction(postinc, node.getTarget()), node.getFileOptions());
						} else {
							conversion = new ParseTree(new CFunction(postdec, node.getTarget()), node.getFileOptions());
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
										conversion = new ParseTree(new CFunction(neg, node.getTarget()), node.getFileOptions());
									} else {
										conversion = new ParseTree(new CFunction(p, node.getTarget()), node.getFileOptions());
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
					List<ParseTree> values = new ArrayList<ParseTree>();
					for (int i = 1; i < list.size(); i++) {
						values.add(list.get(i));
					}
					ParseTree value = optimizeSpecial(values, returnSConcat);
					ParseTree ce = new ParseTree(new CFunction(centry, node.getTarget()), node.getFileOptions());
					ce.addChild(node);
					ce.addChild(value);
					return ce;
				}
			}
			
			//We need to look for CBraces (actually, the function) and resolve that, and then grab the first function (or bare string
			//that is a keyword) and pass it to that function (or keyword) to finish resolving. We will send it the whole list.
			//There could be multiple things that need resolving, so we will continue resolving things until we get to the end,
			//though we will make corrections for list size shrinkage.
			for(int i = 0; i < list.size(); i++){
				ParseTree tree = list.get(i);
				if(tree.getData() instanceof CFunction && tree.getData().val().equals(__cbrace__)){
					ParseTree tempNode = CompilerFunctions.__cbrace__.optimizeSpecial(tree.getChildren());
					tree.setData(tempNode.getData());
					tree.setChildren(tempNode.getChildren());
				}
			}
			for(int i = 0; i < list.size(); i++){
				int currentSize = list.size();
				ParseTree node = list.get(i);
				if(node.getData() instanceof CBrace){
					if(i == 0){
						//This is a compile error, they have a { at the beginning of a sibling chain.
						//TODO: I think this is eventually where object notation can be put
						throw new ConfigCompileException("Unexpected {", node.getTarget());
					}
					ParseTree prev = list.get(i - 1);
					Mixed data = prev.getData();
					if(data instanceof CFunction){
						Function f = ((CFunction)prev.getData()).getFunction();
						if(f instanceof Braceable){
							((Braceable)f).handleBraces(list, i - 1);
						}
					} else if(data instanceof CString){
						//Eventually keywords will be allowed, but not yet
						throw new ConfigCompileException("Unexpected {", node.getTarget());
					}
				}
				int newSize = list.size();
				//Index correction. Note that it will be 0 if no correction was needed,
				//and so i will be unaffected.
				if(currentSize - newSize != 0){
					i--;
				}
			}
			
			// Look for any keywords. If there are any, we need to send this whole
			// chain to that keyword handler. Continue until all keywords are gone.
			for(int i = 0; i < list.size(); i++){
				ParseTree node = list.get(i);
				if(node.getData() instanceof CKeyword){
					((CKeyword)node.getData()).getHandler().handle(list);
					//reset now
					i = 0;
				}
			}

			for(int i = 0; i < list.size(); i++){
				//Recurse down and optimize all instances of __autoconcat__ that are left
				ParseTree element = list.get(i);
				Mixed data = element.getData();
				if(data instanceof CFunction && __autoconcat__.equals(data.val())){
					list.set(i, optimizeSpecial(element.getChildren(), returnSConcat));
				}
			}
			
			//We've eliminated the need for __autoconcat__ either way, however, if there are still arguments
			//left, it needs to go to sconcat, which MAY be able to be further optimized, but that will
			//be handled in MethodScriptCompiler's optimize function. Also, we must scan for CPreIdentifiers,
			//which may be turned into a function
			if (list.size() == 1) {
				return list.get(0);
			} else {
				//TODO: Don't think this block is needed anymore?
//				for (int i = 0; i < list.size(); i++) {
//					if (list.get(i).getData().wasIdentifier()){//.getCType() == Construct.ConstructType.IDENTIFIER) {
//						if (i == 0) {
//							//Yup, it's an identifier
//							CFunction identifier = new CFunction(list.get(i).getData().val(), list.get(i).getTarget());
//							list.remove(0);
//							ParseTree child = list.get(0);
//							if (list.size() > 1) {
//								child = new ParseTree(new CFunction("sconcat", Target.UNKNOWN), child.getFileOptions());
//								child.setChildren(list);
//							}
//							try {
//								Function f = (Function) FunctionList.getFunction(identifier);
//								ParseTree node = new ParseTree(f.execs(identifier.getTarget(), null, null, child), child.getFileOptions());
//								return node;
//							} catch (Exception e) {
//								throw new Error("Unknown function " + identifier.val() + "?");
//							}
//						} else {
//							//Hmm, this is weird. I'm not sure what condition this can happen in
//							throw new ConfigCompileException("Unexpected IDENTIFIER? O.o Please report a bug,"
//									+ " and include the script you used to get this error.", Target.UNKNOWN);
//						}
//					}
//				}
				ParseTree tree;
				FileOptions options = new FileOptions(new EnumMap<FileOptions.Directive, String>(FileOptions.Directive.class), Target.UNKNOWN);
				if (!list.isEmpty()) {
					options = list.get(0).getFileOptions();
				}
				if (returnSConcat) {
					tree = new ParseTree(new CFunction(sconcat, Target.UNKNOWN), options);
				} else {
					tree = new ParseTree(new CFunction(concat, Target.UNKNOWN), options);
				}
				tree.setChildren(list);
				return tree;
			}
		}

		@Override
		public Argument returnType() {
			return new Argument("", Mixed.class);
		}

		@Override
		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(new Argument("", CArray.class, "var").setVarargs());
		}
	}

	@api
	public static class npe extends DummyFunction {

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		public Construct exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			Object o = null;
			o.toString();
			return new CVoid(t);
		}

		public Argument returnType() {
			return Argument.NONE;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.NONE;
		}
	}

	@api
	@noprofile
	public static class dyn extends DummyFunction {

		@Override
		public String docs() {
			return "Registers as a dynamic component, for optimization testing; that is"
					+ " to say, this will not be optimizable ever."
					+ " It simply returns the argument provided, or void if none.";
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if (args.length == 0) {
				return new CVoid(t);
			}
			return args[0];
		}

		@Override
		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(new Argument("", Mixed.class, "argument").setOptional());
		}
	}

	@api
	public static class __cbrace__ extends DummyFunction implements Optimizable {

		public Construct exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.OPTIMIZE_DYNAMIC);
		}
		
		public static ParseTree optimizeSpecial(List<ParseTree> children) throws ConfigCompileException{
			return new __cbrace__().optimizeDynamic(Target.UNKNOWN, null, children);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			FileOptions options = new FileOptions(new EnumMap<FileOptions.Directive, String>(FileOptions.Directive.class), Target.UNKNOWN);
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
	
	public static class elseKeywordHandler implements KeywordHandler {

		public void handle(List<ParseTree> elements) {
			for(ParseTree element : elements){
				Mixed data = element.getData();
				if(data instanceof CKeyword && "else".equals(data.val())){
					// We have 3 different situations to handle. We can glue
					// together up to 3 
				}
			}
		}
		
	}
}
