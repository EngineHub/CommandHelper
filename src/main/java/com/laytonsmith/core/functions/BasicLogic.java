package com.laytonsmith.core.functions;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.*;
import com.laytonsmith.core.arguments.ArgList;
import com.laytonsmith.core.arguments.Argument;
import com.laytonsmith.core.arguments.ArgumentBuilder;
import com.laytonsmith.core.arguments.Generic;
import com.laytonsmith.core.compiler.Braceable;
import com.laytonsmith.core.compiler.CodeBranch;
import com.laytonsmith.core.compiler.CompilerFunctions;
import com.laytonsmith.core.compiler.Optimizable;
import com.laytonsmith.core.compiler.OptimizationUtilities;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.natives.interfaces.Operators;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Layton
 */
public class BasicLogic {

	public static String docs() {
		return "These functions provide basic logical operations.";
	}

	@api
	public static class _if extends AbstractFunction implements Optimizable, Braceable, CodeBranch {

		public String getName() {
			return "if";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			for (ParseTree node : nodes) {
				if (node.getData() instanceof CIdentifier) {
					return new ifelse().execs(t, env, parent, nodes);
				}
			}
			ParseTree condition = nodes[0];
			ParseTree __if = nodes[1];
			ParseTree __else = null;
			if (nodes.length == 3) {
				__else = nodes[2];
			}

			if (parent.seval(condition, env).primitive(t).castToBoolean()) {
				return parent.seval(__if, env);
			} else {
				if (__else == null) {
					return new CVoid(t);
				}
				return parent.seval(__else, env);
			}
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			return new CVoid(t);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "If the first argument evaluates to a true value, the second argument is returned, otherwise the third argument is returned."
					+ " If there is no third argument, it returns void.";
		}
		
		public Argument returnType() {
			return new Argument("The value resolved in whichever branch is true", Mixed.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The condition to check for. If true, trueRet is resolved and returned, otherwise, falseRet is resolved and returned (if present)", CBoolean.class, "cond"),
					new Argument("The code to run if the condition is true", CCode.class, "trueRet"),
					new Argument("The code to run if the condition if false", CCode.class, "falseRet").setOptional()
				);
		}

		public boolean isRestricted() {
			return false;
		}

		@Override
		public boolean preResolveVariables() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_0_1;
		}
		//Doesn't matter, this function is run out of state

		public Boolean runAsync() {
			return false;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env, List<ParseTree> args) throws ConfigCompileException {
			//Just, always turn this into an ifelse, though throw a compile error if there are more than
			//3 arguments, if this isn't a pre-ifelse
			boolean allowOverloading = false;
			for (ParseTree arg : args) {
				//If any are CIdentifiers, forward this to ifelse
				if (arg.getData() instanceof CBrace) {
					allowOverloading = true;
					break;
				}
			}
			if (!allowOverloading && args.size() > 3) {
				throw new ConfigCompileException("if() can only have 3 parameters", t);
			}
			return new ifelse().optimizeDynamic(t, env, args);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Functional usage", "if(true, msg('This is true'), msg('This is false'))"),
						new ExampleScript("With braces, true condition", "if(true){\n\tmsg('This is true')\n}"),
						new ExampleScript("With braces, false condition", "msg('Start')\nif(false){\n\tmsg('This will not show')\n}\nmsg('Finish')"),};
		}
		
		private static final String __autoconcat__ = new CompilerFunctions.__autoconcat__().getName();
		private static final String _if = new _if().getName();
		private static final String _ifelse = new ifelse().getName();
		private static final String _else = "else";

		@Override
		@SuppressWarnings({"null", "ConstantConditions"})
		public void handleBraces(List<ParseTree> allNodes, int startingWith) throws ConfigCompileException {
			boolean pastIf = false;
			boolean hasIf = false;
			ParseTree ifelse = null;
			for (int i = startingWith; i < allNodes.size(); i++) {
				//Only this configuration is expected:
				// - if/brace/[else/if/brace]*[/else/brace]
				//We will use a mini-lexer here for this process. Note that we have the exceptional circumstance
				//where an else if() is going to be in a __autoconcat__, so we'll need to detect and pull that up
				//ourselves.
				//Need a 2 lookahead
				ParseTree n1 = allNodes.get(i);
				ParseTree n2 = allNodes.size() > i + 1 ? allNodes.get(i + 1) : null;
				ParseTree n3 = allNodes.size() > i + 2 ? allNodes.get(i + 2) : null;
				if (!pastIf) {
					if (n1.getData() instanceof CFunction && _if.equals(n1.getData().val())) {
						pastIf = true;
						hasIf = true;
						ifelse = new ParseTree(new CFunction(_ifelse, n1.getTarget()), n1.getFileOptions());
						try {
							//Check for weirdness, like if(1, 1){ ... }
							if (n1.numberOfChildren() != 1) {
								throw new ConfigCompileException("Unexpected elements in if statement. (You have a comma in your if(<code>,<code>){...}).", n1.getTarget());
							}
							ifelse.addChild(n1.getChildAt(0)); //Grab the child of the if(...)
							ifelse.addChild(((CBrace)n2.getData()).getNode()); //Grab the child of the braces { ... }
							allNodes.remove(i);
							allNodes.remove(i);
							i--;
						} catch (NullPointerException e) {
							//This occurs if they do something like: if(...)
							//Note the lack of {}
							throw new ConfigCompileException("Have if with no braces, and not enough arguments for use in functional style.", n1.getTarget());
						}
					}
				} else {
					if(n1.getData() instanceof CFunction && __autoconcat__.equals(n1.getData().val()) && n1.getChildren().size() == 2){
						//Check to see if this is an autoconcat we need to pull up.
						ParseTree pn1 = n1.getChildAt(0);
						ParseTree pn2 = n1.getChildAt(1);
						if(pn1.getData() instanceof CKeyword && _else.equals(pn1.getData().val())
								&& pn2.getData() instanceof CFunction && _if.equals(pn2.getData().val())){
							//Yep. We need to pull them up, then continue.
							allNodes.remove(i);
							allNodes.add(i, pn2);
							allNodes.add(i, pn1);
							i--;
							continue;
						}
					}
					if (n1.getData() instanceof CKeyword && _else.equals(n1.getData().val())) {
						//Yep, see if this is the end or just the middle.
						if (n2.getData() instanceof CFunction && _if.equals(n2.getData().val())) {
							//This is a middle
							try {
								//Check for weirdness, like if(1, 1){ ... }
								if (n2.numberOfChildren() != 1) {
									throw new ConfigCompileException("Unexpected elements in if statement. (You have a comma in your if(<code>,<code>){...}).", n1.getTarget());
								}
								ifelse.addChild(n2.getChildAt(0)); //Grab the child of the if(...)
								ifelse.addChild(((CBrace)n3.getData()).getNode()); //Grab the child of the braces { ... }
								allNodes.remove(i);
								allNodes.remove(i);
								allNodes.remove(i);
								i--;
							} catch (NullPointerException e) {
								//This occurs if they do something like: if(...)
								//Note the lack of {}
								throw new ConfigCompileException("Have else if() with no braces.", n2.getTarget());
							}
						} else if (n2.getData() instanceof CBrace) {
							//This is the end
							try {
								//Check for weirdness, like if(1, 1){ ... }
								ifelse.addChild(((CBrace)n2.getData()).getNode()); //Grab the child of the {...}
								allNodes.remove(i);
								allNodes.remove(i);
								i--;
							} catch (NullPointerException e) {
								//This occurs if they do something like: else
								//Note the lack of {}
								throw new ConfigCompileException("Have else with no braces.", n2.getTarget());
							}
						}
					}
				}
			}
			if(hasIf){
				if(allNodes.size() <= startingWith){
					//We got rid of the last one, so just push onto the end
					allNodes.add(ifelse);
				} else {
					allNodes.add(startingWith, ifelse);
				}
			}
		}

		public Integer[] getCodeBranches(List<ParseTree> children) {
			return new Integer[]{1, 2};
		}
	}

	@api
	public static class _switch extends AbstractFunction implements Braceable {

		public String getName() {
			return "switch";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public String docs() {
			return "mixed {value, [equals, code]..., [defaultCode]} Provides a switch statement. If none of the conditions"
					+ " match, and no default is provided, void is returned."
					+ " See the documentation on [[CommandHelper/Logic|Logic]] for more information. ----"
					+ " In addition, slices may be used to indicate ranges of integers that should trigger the specified"
					+ " case. Slices embedded in an array are fine as well.";
		}
		
		public Argument returnType() {
			return new Argument("The value resolved by the switch case that is matched", Mixed.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.MANUAL;
		}

		@Override
		public String argumentsManual() {
			return "mixed value, [mixed case, code code]..., [code defaultCode]";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InsufficientArgumentsException};
		}

		public boolean isRestricted() {
			return false;
		}

		@Override
		public boolean preResolveVariables() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return Construct.GetNullConstruct(t);
		}

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			Construct value = parent.seval(nodes[0], env);
			equals equals = new equals();
			for (int i = 1; i <= nodes.length - 2; i += 2) {
				ParseTree statement = nodes[i];
				ParseTree code = nodes[i + 1];
				Construct evalStatement = parent.seval(statement, env);
				if (evalStatement instanceof CSlice) { //More specific subclass of array, we can do more optimal handling here
					long rangeLeft = ((CSlice) evalStatement).getStart();
					long rangeRight = ((CSlice) evalStatement).getFinish();
					if (value instanceof CInt) {
						long v = value.primitive(t).castToInt(t);
						if ((rangeLeft < rangeRight && v >= rangeLeft && v <= rangeRight)
								|| (rangeLeft > rangeRight && v >= rangeRight && v <= rangeLeft)
								|| (rangeLeft == rangeRight && v == rangeLeft)) {
							return parent.seval(code, env);
						}
					} else {
						throw new ConfigRuntimeException("When using slice notation in a switch case, the value being switched on must be an integer, but instead, " + value.val() + " was found.", ExceptionType.CastException, t);
					}
				} else if (evalStatement instanceof CArray) {
					for (String index : ((CArray) evalStatement).keySet()) {
						Construct inner = ((CArray) evalStatement).get(index);
						if (inner instanceof CSlice) {
							long rangeLeft = ((CSlice) inner).getStart();
							long rangeRight = ((CSlice) inner).getFinish();
							if (value instanceof CInt) {
								long v = value.primitive(t).castToInt(t);
								if ((rangeLeft < rangeRight && v >= rangeLeft && v <= rangeRight)
										|| (rangeLeft > rangeRight && v >= rangeRight && v <= rangeLeft)
										|| (rangeLeft == rangeRight && v == rangeLeft)) {
									return parent.seval(code, env);
								}
							} else {
								throw new ConfigRuntimeException("When using slice notation in a switch case, the value being switched on must be an integer, but instead, " + value.val() + " was found.", ExceptionType.CastException, t);
							}
						} else {
							if (equals.exec(t, env, value, inner).castToBoolean()) {
								return parent.seval(code, env);
							}
						}
					}
				} else {
					if (equals.exec(t, env, value, evalStatement).castToBoolean()) {
						return parent.seval(code, env);
					}
				}
			}
			if (nodes.length % 2 == 0) {
				return parent.seval(nodes[nodes.length - 1], env);
			}
			return new CVoid(t);
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Functional usage", "switch('theValue',\n"
						+ "\t'notTheValue',\n"
						+ "\t\tmsg('Nope'),\n"
						+ "\t'theValue',\n"
						+ "\t\tmsg('Success')\n"
						+ ")"),
						new ExampleScript("With braces", "switch('theValue'){\n"
						+ "\t'notTheValue',\n"
						+ "\t\tmsg('Nope'),\n"
						+ "\t'theValue',\n"
						+ "\t\tmsg('Success')\n"
						+ "}"),
						new ExampleScript("With default condition", "switch('noMatch',\n"
						+ "\t'notIt1',\n"
						+ "\t\tmsg('Nope'),\n"
						+ "\t'notIt2',\n"
						+ "\t\tmsg('Nope'),\n"
						+ "\t, #Default:\n"
						+ "\t\tmsg('Success')\n"
						+ ")"),
						new ExampleScript("With multiple matches using an array", "switch('string'){\n"
						+ "\tarray('value1', 'value2', 'string'),\n"
						+ "\t\tmsg('Match'),\n"
						+ "\t'value3',\n"
						+ "\t\tmsg('No match')\n"
						+ "}"),
						new ExampleScript("With slices", "switch(5){\n"
						+ "\t1..2,\n"
						+ "\t\tmsg('First'),\n"
						+ "\t3..5,\n"
						+ "\t\tmsg('Second'),\n"
						+ "\t6..8,\n"
						+ "\t\tmsg('Third')\n"
						+ "}"),
						new ExampleScript("With slices in an array", "switch(5){\n"
						+ "\tarray(1..2, 3..5),\n"
						+ "\t\tmsg('First'),\n"
						+ "\t6..8,\n"
						+ "\t\tmsg('Second')\n"
						+ "}"),};
		}

		public void handleBraces(List<ParseTree> allNodes, int startingWith) throws ConfigCompileException {
			throw new UnsupportedOperationException("Not supported yet.");
		}
	}

	@api(environments = {GlobalEnv.class})
	public static class ifelse extends AbstractFunction implements Optimizable, CodeBranch {

		public String getName() {
			return "ifelse";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public String docs() {
			return "Provides a more convenient method"
					+ " for running if/else chains. If none of the conditions are true, and"
					+ " there is no 'else' condition, void is returned.";
		}
		
		public Argument returnType() {
			return new Argument("The value resolved in the branch of the condition that is true", Mixed.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.MANUAL;
		}

		@Override
		public String argumentsManual() {
			return "[boolean bool, code code]..., [code elseCode]";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InsufficientArgumentsException};
		}

		public boolean isRestricted() {
			return false;
		}

		@Override
		public boolean preResolveVariables() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return Construct.GetNullConstruct(t);
		}

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			if (nodes.length < 2) {
				throw new ConfigRuntimeException("ifelse expects at least 2 arguments", ExceptionType.InsufficientArgumentsException, t);
			}
			for (int i = 0; i <= nodes.length - 2; i += 2) {
				ParseTree statement = nodes[i];
				ParseTree code = nodes[i + 1];
				Construct evalStatement = parent.seval(statement, env);
				if (evalStatement instanceof CIdentifier) {
					evalStatement = parent.seval(((CIdentifier) evalStatement).contained(), env);
				}
				if (evalStatement.primitive(t).castToBoolean()) {
					Construct ret = env.getEnv(GlobalEnv.class).GetScript().eval(code, env);
					return ret;
				}
			}
			if (nodes.length % 2 == 1) {
				Construct ret = env.getEnv(GlobalEnv.class).GetScript().seval(nodes[nodes.length - 1], env);
				if (ret instanceof CIdentifier) {
					return parent.seval(((CIdentifier) ret).contained(), env);
				} else {
					return ret;
				}
			}
			return new CVoid(t);
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {

			//We have to cache the return value if even if we find it, so we can check for syntax errors
			//in all the branches, not just the ones before the first hardcoded true
			ParseTree toReturn = null;
			for (int i = 0; i <= children.size() - 2; i += 2) {
				ParseTree statement = children.get(i);
				ParseTree code = children.get(i + 1);
				Construct evalStatement = statement.getData();
				if (evalStatement instanceof CIdentifier) {
					//check for an else here, if so, it's a compile error
					if (evalStatement.val().equals("else")) {
						throw new ConfigCompileException("Unexpected else", t);
					}
				}
				if (!statement.getData().isDynamic()) {
					if (evalStatement instanceof CIdentifier) {
						evalStatement = ((CIdentifier) evalStatement).contained().getData();
					}
					//If it's hardcoded true, we found it.
					if (evalStatement.primitive(t).castToBoolean()) {
						if (toReturn == null) {
							toReturn = code;
						}
					} else {
						//else it's hard coded false, and we can ignore it.
						children.remove(i);
						children.remove(i);
						i -= 2;
					}
				}
				// We can pull up if(@a){ if(@b){ ...} } to if(@a && @b){ ... },
				// which, in my profiling is faster. The only special consideration
				// we have to make is to ensure that the inner if is the only statement
				// in the entire block, (including lack of an else) so any code outside the inner if causes this
				// optimization to be impossible. An inner ifelse cannot be optimized, unless it only has 2 arguments
				// (in which case, it's a normal if())
				if (code.getChildren().size() == 2 && code.getData() instanceof CFunction && code.getData().val().equals("ifelse")) {
					CFunction and = new CFunction("and", t);
					ParseTree andTree = new ParseTree(and, statement.getFileOptions());
					andTree.addChild(statement);
					andTree.addChild(code.getChildAt(0));
					children.set(i, andTree);
					children.set(i + 1, code.getChildAt(1));
					//TODO: Finish this. The optimized tree approach is wrong,
					//and the reverse approach is not being used, so there is
					//
//					if (optimizedTree.size() < 1) {
//						optimizedTree.add(andTree);
//					} else {
//						optimizedTree.set(i, andTree);
//					}
//					if (optimizedTree.size() < 2) {
//						optimizedTree.add(code.getChildAt(1));
//					} else {
//						optimizedTree.set(i + 1, code.getChildAt(1));
//					}
				}
			}
			if (toReturn != null) {
				return toReturn;
			}
			if (children.size() % 2 == 1) {
				//Look at the final else block
				ParseTree ret = children.get(children.size() - 1);
				if (children.size() == 1) {
					//Oh. Well, we can just return this node then.
					return ret;
				}
			}
			if (children.isEmpty()) {
				//We have optimized it out entirely, so remove us
				return Optimizable.REMOVE_ME;
			}
			return null;
		}
//        @Override
//        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
//            boolean inNewMode = false;
//            for(int i = 0; i < args.length; i++){
//                if(args[0] instanceof CIdentifier){
//                    inNewMode = true;
//                    break;
//                }
//            }
//            
//            if(!inNewMode){
//                return null;//TODO: We can optimize this, even with some parameters dynamic, but
//                we need the tree.
//            } else {
//                Only an else shoved in the middle is disallowed
//                if(!(args[args.length - 1] instanceof CIdentifier)){
//                    throw new ConfigCompileException("Syntax error", t);
//                }
//                for(int i = 1; i <= args.length; i++){
//                    if(!(args[i] instanceof CIdentifier)){
//                        throw new ConfigCompileException("Syntax error", t);
//                    } else {
//                        CIdentifier ci = (CIdentifier)args[i];
//                        if(ci.val().equals("else")){
//                            throw new ConfigCompileException("Unexpected else, was expecting else if", t);
//                        }
//                    }
//                }
//                return null;
//            }
//        }
//
//        public ParseTree optimizeSpecial(Target target, List<ParseTree> children) {
//            throw new UnsupportedOperationException("Not yet implemented");
//        }

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Functional usage", "ifelse(false, msg('This is false'), true, msg('This is true'))"),
						new ExampleScript("With braces", "if(false){\n\tmsg('This is false')\n} else {\n\tmsg('This is true')\n}"),
						new ExampleScript("With braces, with else if", "if(false){\n\tmsg('This will not show')\n} else if(false){\n"
						+ "\n\tmsg('This will not show')\n} else {\n\tmsg('This will show')\n}"),};
		}

		public Integer[] getCodeBranches(List<ParseTree> children) {
			Set<Integer> list = new HashSet<Integer>();
			list.add(1);
			for(int i = 3; i < children.size(); i+=2){
				list.add(i);
			}
			//Last one is always a code branch
			list.add(children.size() - 1);
			return list.toArray(new Integer[list.size()]);
		}
	}

	@api
	public static class equals extends AbstractFunction implements Optimizable {

		private static equals self = new equals();

		/**
		 * Returns the results that this function would provide, but in a java
		 * specific manner, so other code may easily determine how this method
		 * would respond.
		 *
		 * @param one
		 * @param two
		 * @return
		 */
		public static boolean doEquals(Construct one, Construct two) {
			CBoolean ret = (CBoolean) self.exec(Target.UNKNOWN, null, one, two);
			return ret.castToBoolean();
		}

		public String getName() {
			return "equals";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public CBoolean exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			List<Construct> argList = new ArrayList<Construct>();
			argList.add((Construct)list.get("var1"));
			argList.add((Construct)list.get("var2"));
			for(Construct c : ((CArray)list.get("varX"))){
				argList.add(c);
			}
			boolean equals = true;
			for(int i = 0; i < argList.size() - 1; i++){
				Construct lhs = argList.get(i);
				Construct rhs = argList.get(i + 1);
				if(lhs instanceof Operators.Equality){
					//Test first
					Operators.Equality elhs = (Operators.Equality)lhs;
					if(elhs.operatorTestEquals(rhs.getClass())){
						if(!elhs.operatorEquals(rhs)){
							equals = false;
							break;
						}
					} else {
						throw new Exceptions.CastException(lhs.typeName() + " does not support equals comparisons with " + rhs.typeName(), t);
					}
				} else {
					throw new Exceptions.CastException(lhs.typeName() + " does not support equals comparisons", t);
				}
			}
			return new CBoolean(equals, t);
//			if (args.length <= 1) {
//				throw new ConfigRuntimeException("At least two arguments must be passed to equals", ExceptionType.InsufficientArgumentsException, t);
//			}
//			boolean referenceMatch = true;
//			for (int i = 0; i < args.length - 1; i++) {
//				if (args[i] != args[i + 1]) {
//					referenceMatch = false;
//					break;
//				}
//			}
//			if (referenceMatch) {
//				return new CBoolean(true, t);
//			}
//			if (Static.anyBooleans(args)) {
//				boolean equals = true;
//				for (int i = 1; i < args.length; i++) {
//					boolean arg1 = args[i - 1].primitive(t).castToBoolean();
//					boolean arg2 = args[i].primitive(t).castToBoolean();
//					if (arg1 != arg2) {
//						equals = false;
//						break;
//					}
//				}
//				return new CBoolean(equals, t);
//			}
//
//			{
//				boolean equals = true;
//				for (int i = 1; i < args.length; i++) {
//					if (!args[i - 1].val().equals(args[i].val())) {
//						equals = false;
//						break;
//					}
//				}
//				if (equals) {
//					return new CBoolean(true, t);
//				}
//			}
//			try {
//				boolean equals = true;
//				for (int i = 1; i < args.length; i++) {
//					double arg1 = args[i - 1].primitive(t).castToDouble(t);
//					double arg2 = args[i].primitive(t).castToDouble(t);
//					if (arg1 != arg2) {
//						equals = false;
//						break;
//					}
//				}
//				return new CBoolean(equals, t);
//			} catch (ConfigRuntimeException e) {
//				return new CBoolean(false, t);
//			}
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InsufficientArgumentsException};
		}

		public String docs() {
			return "Returns true or false if all the arguments are equal";
		}
		
		public Argument returnType() {
			return new Argument("Iff ALL the arguments are equal, this returns true.", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The first variable to compare", Mixed.class, "var1"),
					new Argument("The second variable to compare", Mixed.class, "var2"),
					new Argument("If additional variables should be compared, they can also be provided.", CArray.class, "varX").setVarargs()
				);
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		public Boolean runAsync() {
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Functional usage", "equals(1, 1.0, '1')"),
						new ExampleScript("Symbolic usage", "1 == 1"),
						new ExampleScript("Not equivalent", "'one' == 'two'"),};
		}
	}

	@api
	public static class sequals extends AbstractFunction implements Optimizable {

		public String getName() {
			return "sequals";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "Uses a strict equals check, which determines if"
					+ " two values are not only equal, but also the same type. So, while"
					+ " equals('1', 1) returns true, sequals('1', 1) returns false, because"
					+ " the first one is a string, and the second one is an int. More often"
					+ " than not, you want to use plain equals().";
		}
		
		public Argument returnType() {
			return new Argument("Iff both arguments are both the same type and equal, returns true", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The first value to consider", Mixed.class, "val1"),
					new Argument("The second value to consider", Mixed.class, "val2")
				);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return null;
		}

		public CBoolean exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			equals equals = new equals();
			if (args[1].getClass().equals(args[0].getClass())
					&& equals.exec(t, environment, args).castToBoolean()) {
				return new CBoolean(true, t);
			} else {
				return new CBoolean(false, t);
			}
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Functional usage", "sequals('1', 1)"),
						new ExampleScript("Symbolic usage", "'1' === 1"),
						new ExampleScript("Symbolic usage", "'1' === '1'"),};
		}
	}

	@api
	public static class snequals extends AbstractFunction implements Optimizable {

		public String getName() {
			return "snequals";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "Equivalent to not(sequals(val1, val2))";
		}
		
		public Argument returnType() {
			return new Argument("If the values are not strictly equal, returns true", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The first value to consider", Mixed.class, "val1"),
					new Argument("The second value to consider", Mixed.class, "val2")
				);
		}

		public ExceptionType[] thrown() {
			return null;
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CBoolean(!new sequals().exec(t, environment, args).castToBoolean(), t);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Basic usage", "snequals('1', 1)"),
						new ExampleScript("Basic usage", "snequals('1', '1')"),};
		}
	}

	@api
	public static class nequals extends AbstractFunction implements Optimizable {

		public String getName() {
			return "nequals";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "Returns true if the two values are NOT equal, or false"
					+ " otherwise. Equivalent to not(equals(val1, val2))";
		}
		
		public Argument returnType() {
			return new Argument("If the two values are not equals, return true", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The first value to consider", Mixed.class, "val1"),
					new Argument("The second value to consider", Mixed.class, "val2")
				);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return new CBoolean(!new equals().exec(t, env, args).castToBoolean(), t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Basic usage", "nequals('one', 'two')"),
						new ExampleScript("Basic usage", "nequals(1, 1)"),};
		}
	}

	@api
	public static class equals_ic extends AbstractFunction implements Optimizable {

		public String getName() {
			return "equals_ic";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public String docs() {
			return "Returns true if all the values are equal to each other, while"
					+ " ignoring case.";
		}
		
		public Argument returnType() {
			return new Argument("Iff all values are equal, ignoring case for strings, returns true", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The first value to consider", Mixed.class, "val1"),
					new Argument("The second value to consider", Mixed.class, "val2"),
					new Argument("Additional values to consider", CArray.class, "valX").setGenerics(new Generic(Mixed.class)).setVarargs()
				);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InsufficientArgumentsException};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_2_0;
		}

		public Boolean runAsync() {
			return null;
		}

		public CBoolean exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			if (args.length <= 1) {
				throw new ConfigRuntimeException("At least two arguments must be passed to equals_ic", ExceptionType.InsufficientArgumentsException, t);
			}
			if (Static.anyBooleans(args)) {
				boolean equals = true;
				for (int i = 1; i < args.length; i++) {
					boolean arg1 = args[i - 1].primitive(t).castToBoolean();
					boolean arg2 = args[i].primitive(t).castToBoolean();
					if (arg1 != arg2) {
						equals = false;
						break;
					}
				}
				return new CBoolean(equals, t);
			}

			{
				boolean equals = true;
				for (int i = 1; i < args.length; i++) {
					if (!args[i - 1].val().equalsIgnoreCase(args[i].val())) {
						equals = false;
						break;
					}
				}
				if (equals) {
					return new CBoolean(true, t);
				}
			}
			try {
				boolean equals = true;
				for (int i = 1; i < args.length; i++) {
					double arg1 = args[i - 1].primitive(t).castToDouble(t);
					double arg2 = args[i].primitive(t).castToDouble(t);
					if (arg1 != arg2) {
						equals = false;
						break;
					}
				}
				return new CBoolean(equals, t);
			} catch (ConfigRuntimeException e) {
				return new CBoolean(false, t);
			}
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Basic usage", "equals_ic('test', 'TEST')"),
						new ExampleScript("Basic usage", "equals_ic('completely', 'DIFFERENT')"),};
		}
	}

	@api
	public static class nequals_ic extends AbstractFunction implements Optimizable {

		public String getName() {
			return "nequals_ic";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "boolean {val1, val2} Returns true if the two values are NOT equal to each other, while"
					+ " ignoring case.";
		}
		
		public Argument returnType() {
			return new Argument("Returns true if the two values are not equal to each other.", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The first value to consider", Mixed.class, "val1"),
					new Argument("The second value to consider", Mixed.class, "val2")
				);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CBoolean(!new equals_ic().exec(t, environment, args).castToBoolean(), t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Basic usage", "equals_ic('test', 'TEST')"),
						new ExampleScript("Basic usage", "equals_ic('completely', 'DIFFERENT')"),};
		}
	}

	@api
	public static class ref_equals extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return null;
		}

		public CBoolean exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if (args[0] instanceof CArray && args[1] instanceof CArray) {
				return new CBoolean(args[0] == args[1], t);
			} else {
				return new equals().exec(t, environment, args);
			}
		}

		public String getName() {
			return "ref_equals";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "Returns true if and only if the two values are actually the same reference."
					+ " Primitives that are equal will always be the same reference, this method is only useful for"
					+ " object/array comparisons.";
		}
		
		public Argument returnType() {
			return new Argument("Returns true if the two values are the exact same reference", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The first value to consider", Mixed.class, "val1"),
					new Argument("The second value to consider", Mixed.class, "val2")
				);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Usage with primitives", "msg(ref_equals(1, 1))\n"
						+ "msg(ref_equals(1, 2))"),
						new ExampleScript("Usage with arrays that are the same reference", "@a = array(1, 2, 3)\n"
						+ "@b = @a\n"
						+ "msg(ref_equals(@a, @b)) # Note that an assignment simply sets it to reference the same underlying object, so this is true"),
						new ExampleScript("Usage with a cloned array", "@a = array(1, 2, 3)\n"
						+ "@b = @a[] # Clone the array\n"
						+ "msg(ref_equals(@a, @b)) # False, because although the arrays are == (and ===) they are different references"),
						new ExampleScript("Usage with a duplicated array", "@a = array(1, 2, 3)\n"
						+ "@b = array(1, 2, 3) # New array with duplicate content\n"
						+ "msg(ref_equals(@a, @b)) # Again, even though @a == @b and @a === @b, this is false, because they are two different references"),};
		}
	}

	@api
	public static class lt extends AbstractFunction implements Optimizable {

		public String getName() {
			return "lt";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			double arg1 = args[0].primitive(t).castToDouble(t);
			double arg2 = args[1].primitive(t).castToDouble(t);
			return new CBoolean(arg1 < arg2, t);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "Returns the results of a less than operation";
		}
		
		public Argument returnType() {
			return new Argument("Returns true if var1 is less than var2", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The first value to consider", CPrimitive.class, "var1"),
					new Argument("The second value to consider", CPrimitive.class, "var2")
				);
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		public Boolean runAsync() {
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Functional usage", "lt(4, 5)"),
						new ExampleScript("Symbolic usage, true condition", "4 < 5"),
						new ExampleScript("Symbolic usage, false condition", "5 < 4"),};
		}
	}

	@api
	public static class gt extends AbstractFunction implements Optimizable {

		public String getName() {
			return "gt";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			double arg1 = args[0].primitive(t).castToDouble(t);
			double arg2 = args[1].primitive(t).castToDouble(t);
			return new CBoolean(arg1 > arg2, t);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "Returns the result of a greater than operation";
		}
		
		public Argument returnType() {
			return new Argument("Returns true if var1 is greater than var2", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The first value to consider", CPrimitive.class, "var1"),
					new Argument("The second value to consider", CPrimitive.class, "var2")
				);
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		public Boolean runAsync() {
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Functional usage", "gt(5, 4)"),
						new ExampleScript("Symbolic usage, true condition", "5 > 4"),
						new ExampleScript("Symbolic usage, false condition", "4 > 5"),};
		}
	}

	@api
	public static class lte extends AbstractFunction implements Optimizable {

		public String getName() {
			return "lte";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			double arg1 = args[0].primitive(t).castToDouble(t);
			double arg2 = args[1].primitive(t).castToDouble(t);
			return new CBoolean(arg1 <= arg2, t);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "Returns the result of a less than or equal to operation";
		}
		
		public Argument returnType() {
			return new Argument("Returns true if var1 is less than  or equal to var2", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The first value to consider", CPrimitive.class, "var1"),
					new Argument("The second value to consider", CPrimitive.class, "var2")
				);
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		public Boolean runAsync() {
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Functional usage", "lte(4, 5)"),
						new ExampleScript("Symbolic usage, true condition", "4 <= 5"),
						new ExampleScript("Symbolic usage, true condition", "5 <= 5"),
						new ExampleScript("Symbolic usage, false condition", "5 <= 4"),};
		}
	}

	@api
	public static class gte extends AbstractFunction implements Optimizable {

		public String getName() {
			return "gte";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			double arg1 = args[0].primitive(t).castToDouble(t);
			double arg2 = args[1].primitive(t).castToDouble(t);
			return new CBoolean(arg1 >= arg2, t);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "Returns the result of a greater than or equal to operation";
		}
		
		public Argument returnType() {
			return new Argument("Returns true if var1 is greater than or equal to var2", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The first value to consider", CPrimitive.class, "var1"),
					new Argument("The second value to consider", CPrimitive.class, "var2")
				);
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		public Boolean runAsync() {
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Functional usage", "gte(5, 4)"),
						new ExampleScript("Symbolic usage, true condition", "4 >= 4"),
						new ExampleScript("Symbolic usage, false condition", "4 >= 5"),};
		}
	}

	@api(environments = {GlobalEnv.class})
	public static class and extends AbstractFunction implements Optimizable {

		public String getName() {
			return "and";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public Construct exec(Target t, Environment env, Construct... args) {
			//This will only happen if they hardcode true/false in, but we still
			//need to handleBraces it appropriately.
			for (Construct c : args) {
				if (!c.primitive(t).castToBoolean()) {
					return new CBoolean(false, t);
				}
			}
			return new CBoolean(true, t);
		}

		@Override
		public CBoolean execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			for (ParseTree tree : nodes) {
				Construct c = env.getEnv(GlobalEnv.class).GetScript().seval(tree, env);
				boolean b = c.primitive(t).castToBoolean();
				if (b == false) {
					return new CBoolean(false, t);
				}
			}
			return new CBoolean(true, t);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "Returns the boolean value of a logical AND across all arguments. Uses lazy determination, so once "
					+ "an argument returns false, the function returns.";
		}
		
		public Argument returnType() {
			return new Argument("Returns the logical AND value", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The first value to consider", CPrimitive.class, "var1"),
					new Argument("The second value to consider", CPrimitive.class, "var2"),
					new Argument("Additional values to consider", CArray.class, "varX").setGenerics(new Generic(CPrimitive.class)).setVarargs()
				);
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		public Boolean runAsync() {
			return null;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			OptimizationUtilities.pullUpLikeFunctions(children, getName());
			Iterator<ParseTree> it = children.iterator();
			while (it.hasNext()) {
				//Remove hard coded true values, they won't affect the calculation at all
				ParseTree child = it.next();
				if (child.isConst() && child.getData().primitive(t).castToBoolean() == true) {
					it.remove();
				}
			}
			if (children.isEmpty()) {
				//We've removed all the children, so return true, because they were all true.
				return new ParseTree(new CBoolean(true, t), null);
			}
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Functional usage", "and(true, true)"),
						new ExampleScript("Symbolic usage, true condition", "true && true"),
						new ExampleScript("Symbolic usage, false condition", "true && false"),
						new ExampleScript("Short circuit", "false && msg('This will not show')"),};
		}

		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC, OptimizationOption.CONSTANT_OFFLINE);
		}
	}

	@api(environments = {GlobalEnv.class})
	public static class or extends AbstractFunction implements Optimizable {

		public String getName() {
			return "or";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public Construct exec(Target t, Environment env, Construct... args) {
			//This will only happen if they hardcode true/false in, but we still
			//need to handleBraces it appropriately.
			for (Construct c : args) {
				if (c.primitive(t).castToBoolean()) {
					return new CBoolean(true, t);
				}
			}
			return new CBoolean(false, t);
		}

		@Override
		public CBoolean execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			for (ParseTree tree : nodes) {
				Construct c = env.getEnv(GlobalEnv.class).GetScript().eval(tree, env);
				if (c.primitive(t).castToBoolean()) {
					return new CBoolean(true, t);
				}
			}
			return new CBoolean(false, t);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "Returns the boolean value of a logical OR across all arguments. Uses lazy determination, so once an "
					+ "argument resolves to true, the function returns.";
		}
		
		public Argument returnType() {
			return new Argument("Returns the logical OR value", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The first value to consider", CPrimitive.class, "var1"),
					new Argument("The second value to consider", CPrimitive.class, "var2"),
					new Argument("Additional values to consider", CArray.class, "varX").setGenerics(new Generic(CPrimitive.class)).setVarargs()
				);
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		public Boolean runAsync() {
			return null;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			OptimizationUtilities.pullUpLikeFunctions(children, getName());
			Iterator<ParseTree> it = children.iterator();
			while (it.hasNext()) {
				//Remove hard coded false values, they won't affect the calculation at all
				ParseTree child = it.next();
				if (child.isConst() && child.getData().primitive(t).castToBoolean() == false) {
					it.remove();
				}
			}
			if (children.isEmpty()) {
				//We've removed all the children, so return false, because they were all false.
				return new ParseTree(new CBoolean(false, t), null);
			}
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Functional usage", "or(false, true)"),
						new ExampleScript("Symbolic usage, true condition", "true || false"),
						new ExampleScript("Symbolic usage, false condition", "false || false"),
						new ExampleScript("Short circuit", "true || msg('This will not show')"),};
		}

		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC, OptimizationOption.CONSTANT_OFFLINE);
		}
	}

	@api
	public static class not extends AbstractFunction implements Optimizable {

		public String getName() {
			return "not";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			return new CBoolean(!args[0].primitive(t).castToBoolean(), t);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "Returns the boolean value of a logical NOT for this argument";
		}
		
		public Argument returnType() {
			return new Argument("The boolean NOT of the value", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The value to consider", CPrimitive.class, "var1")
				);
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		public Boolean runAsync() {
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Functional usage", "not(false)"),
						new ExampleScript("Symbolic usage, true condition", "!false"),
						new ExampleScript("Symbolic usage, false condition", "!true"),};
		}
	}

	@api
	public static class xor extends AbstractFunction implements Optimizable {

		public String getName() {
			return "xor";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "Returns the xor of the two values.";
		}
		
		public Argument returnType() {
			return new Argument("Returns the logical XOR value", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The first value to consider", CPrimitive.class, "var1"),
					new Argument("The second value to consider", CPrimitive.class, "var2")
				);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return null;
		}

		public CBoolean exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			boolean val1 = args[0].primitive(t).castToBoolean();
			boolean val2 = args[1].primitive(t).castToBoolean();
			return new CBoolean(val1 ^ val2, t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Basic usage", "xor(true, false)"),};
		}
	}

	@api
	public static class nand extends AbstractFunction {

		public String getName() {
			return "nand";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public String docs() {
			return "Return the equivalent of not(and())";
		}
		
		public Argument returnType() {
			return new Argument("Returns the logical NAND value", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The first value to consider", CPrimitive.class, "var1"),
					new Argument("The second value to consider", CPrimitive.class, "var2"),
					new Argument("Additional values to consider", CArray.class, "varX").setGenerics(new Generic(CPrimitive.class)).setVarargs()
				);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) {
			return Construct.GetNullConstruct(t);
		}

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			and and = new and();
			boolean val = and.execs(t, env, parent, nodes).castToBoolean();
			return new CBoolean(!val, t);
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Basic usage", "nand(true, true)"),};
		}
	}

	@api
	public static class nor extends AbstractFunction {

		public String getName() {
			return "nor";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public String docs() {
			return "Returns the equivalent of not(or())";
		}
		
		public Argument returnType() {
			return new Argument("Returns the logical NOR value", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The first value to consider", CPrimitive.class, "var1"),
					new Argument("The second value to consider", CPrimitive.class, "var2"),
					new Argument("Additional values to consider", CArray.class, "varX").setGenerics(new Generic(CPrimitive.class)).setVarargs()
				);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) {
			return Construct.GetNullConstruct(t);
		}

		@Override
		public Construct execs(Target t, Environment environment, Script parent, ParseTree... args) throws ConfigRuntimeException {
			or or = new or();
			boolean val = or.execs(t, environment, parent, args).castToBoolean();
			return new CBoolean(!val, t);
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Basic usage", "nor(true, false)"),};
		}
	}

	@api
	public static class xnor extends AbstractFunction implements Optimizable {

		public String getName() {
			return "xnor";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "Returns the xnor of the two values";
		}
		
		public Argument returnType() {
			return new Argument("Returns the logical XNOR value", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The first value to consider", CPrimitive.class, "var1"),
					new Argument("The second value to consider", CPrimitive.class, "var2")
				);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			xor xor = new xor();
			boolean val = xor.exec(t, environment, args).castToBoolean();
			return new CBoolean(!val, t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Basic usage", "xnor(true, true)"),};
		}
	}

	@api
	public static class bit_and extends AbstractFunction implements Optimizable {

		public String getName() {
			return "bit_and";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public String docs() {
			return "Returns the bitwise AND of the values";
		}
		
		public Argument returnType() {
			return new Argument("Returns the bitwise AND value", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The first value to consider", CInt.class, "var1"),
					new Argument("The second value to consider", CInt.class, "var2"),
					new Argument("Additional values to consider", CArray.class, "varX").setGenerics(new Generic(CInt.class)).setVarargs()
				);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.InsufficientArgumentsException};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if (args.length < 1) {
				throw new ConfigRuntimeException("bit_and requires at least one argument", ExceptionType.InsufficientArgumentsException, t);
			}
			long val = args[0].primitive(t).castToInt(t);
			for (int i = 1; i < args.length; i++) {
				val = val & args[i].primitive(t).castToInt(t);
			}
			return new CInt(val, t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Basic usage", "bit_and(1, 2, 4)"),
						new ExampleScript("Usage in masking applications. Note that 5 in binary is 101 and 4 is 100. (See bit_or for a more complete example.)",
						"assign(@var, 5)\nif(bit_and(@var, 4),\n\tmsg('Third bit set')\n)"),};
		}
	}

	@api
	public static class bit_or extends AbstractFunction implements Optimizable {

		public String getName() {
			return "bit_or";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public String docs() {
			return "Returns the bitwise OR of the specified values";
		}
		
		public Argument returnType() {
			return new Argument("Returns the bitwise OR value", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The first value to consider", CInt.class, "var1"),
					new Argument("The second value to consider", CInt.class, "var2"),
					new Argument("Additional values to consider", CArray.class, "varX").setGenerics(new Generic(CInt.class)).setVarargs()
				);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.InsufficientArgumentsException};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if (args.length < 1) {
				throw new ConfigRuntimeException("bit_or requires at least one argument", ExceptionType.InsufficientArgumentsException, t);
			}
			long val = args[0].primitive(t).castToInt(t);
			for (int i = 1; i < args.length; i++) {
				val = val | args[i].primitive(t).castToInt(t);
			}
			return new CInt(val, t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Basic usage", "bit_or(1, 2, 4)"),
						new ExampleScript("Usage in masking applications. (Used to create a mask)", "assign(@flag1, 1)\nassign(@flag2, 2)\nassign(@flag3, 4)\n"
						+ "assign(@flags, bit_or(@flag1, @flag3))\n"
						+ "if(bit_and(@flags, @flag1),\n\tmsg('Contains flag 1')\n)\n"
						+ "if(!bit_and(@flags, @flag2),\n\tmsg('Does not contain flag 2')\n)"),};
		}
	}

	@api
	public static class bit_not extends AbstractFunction implements Optimizable {

		public String getName() {
			return "bit_not";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "int {int1} Returns the bitwise NOT of the given value";
		}
		
		public Argument returnType() {
			return new Argument("Returns the bitwise NOT value", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The value to consider", CInt.class, "var1")
				);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CInt(~args[0].primitive(t).castToInt(t), t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Basic usage", "bit_not(1)"),};
		}
	}

	@api
	public static class lshift extends AbstractFunction implements Optimizable {

		public String getName() {
			return "lshift";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "Left shifts the value bitsToShift times";
		}
		
		public Argument returnType() {
			return new Argument("Returns the shifted value", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The first value to consider", CInt.class, "value"),
					new Argument("The second value to consider", CInt.class, "bitsToShift")
				);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			long value = args[0].primitive(t).castToInt(t);
			long toShift = args[1].primitive(t).castToInt(t);
			return new CInt(value << toShift, t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Basic usage", "lshift(1, 1)"),};
		}
	}

	@api
	public static class rshift extends AbstractFunction implements Optimizable {

		public String getName() {
			return "rshift";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "int {value, bitsToShift} Right shifts the value bitsToShift times";
		}
		
		public Argument returnType() {
			return new Argument("Returns the shifted value", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The first value to consider", CInt.class, "value"),
					new Argument("The second value to consider", CInt.class, "bitsToShift")
				);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			long value = args[0].primitive(t).castToInt(t);
			long toShift = args[1].primitive(t).castToInt(t);
			return new CInt(value >> toShift, t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Basic usage", "rshift(2, 1)"),
						new ExampleScript("Basic usage", "rshift(-2, 1)"),};
		}
	}

	@api
	public static class urshift extends AbstractFunction implements Optimizable {

		public String getName() {
			return "urshift";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "Right shifts value bitsToShift times, pushing a 0, making"
					+ " this an unsigned right shift.";
		}
		
		public Argument returnType() {
			return new Argument("Returns the shifted value", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The first value to consider", CInt.class, "value"),
					new Argument("The second value to consider", CInt.class, "bitsToShift")
				);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			long value = args[0].primitive(t).castToInt(t);
			long toShift = args[1].primitive(t).castToInt(t);
			return new CInt(value >>> toShift, t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Basic usage", "urshift(2, 1)"),
						new ExampleScript("Basic usage", "urshift(-2, 1)"),};
		}
	}
//	@api
//	public static class _elseif extends AbstractFunction implements Optimizable {
//
//		public String getName() {
//			return "elseif";
//		}
//
//		public Integer[] numArgs() {
//			return new Integer[]{1};
//		}
//
//		public String docs() {
//			return "elseif {param} Returns an elseif construct. Used internally by the compiler, use"
//					+ " in actual code will have undefined behavior.";
//		}
//
//		public ExceptionType[] thrown() {
//			return null;
//		}
//
//		public boolean isRestricted() {
//			return false;
//		}
//
//		public Boolean runAsync() {
//			return null;
//		}
//
//		@Override
//		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
//			return new CIdentifier("elseif", nodes[0], t);
//		}
//
//		@Override
//		public boolean useSpecialExec() {
//			return true;
//		}
//
//		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
//			return new CNull(t);
//		}
//
//		public CHVersion since() {
//			return CHVersion.V3_3_1;
//		}
//
//		@Override
//		public Set<OptimizationOption> optimizationOptions() {
//			return EnumSet.of(
//				OptimizationOption.OPTIMIZE_DYNAMIC
//			);
//		}
//
//		@Override
//		public ParseTree optimizeDynamic(Target t, Environment env, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
//			return Optimizable.PULL_ME_UP;
//		}
//
//		@Override
//		public boolean appearInDocumentation() {
//			return false;
//		}
//	}
//
//	@api
//	public static class _else extends AbstractFunction {
//
//		public String getName() {
//			return "else";
//		}
//
//		public Integer[] numArgs() {
//			return new Integer[]{1};
//		}
//
//		public String docs() {
//			return "else {param} Returns an else construct. Used internally by the compiler, use in"
//					+ " code will result in undefined behavior.";
//		}
//
//		public ExceptionType[] thrown() {
//			return null;
//		}
//
//		public boolean isRestricted() {
//			return false;
//		}
//
//		public Boolean runAsync() {
//			return null;
//		}
//
//		@Override
//		public boolean useSpecialExec() {
//			return true;
//		}
//
//		@Override
//		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
//			return new CIdentifier("else", nodes[0], t);
//		}
//
//		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
//			return new CNull(t);
//		}
//
//		public CHVersion since() {
//			return CHVersion.V3_3_1;
//		}
//
//		@Override
//		public boolean appearInDocumentation() {
//			return false;
//		}
//	}
}
