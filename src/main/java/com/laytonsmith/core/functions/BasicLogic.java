package com.laytonsmith.core.functions;

import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.core.*;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.OptimizationUtilities;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
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
	public static class _if extends AbstractFunction implements Optimizable {

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

			if (Static.getBoolean(parent.seval(condition, env))) {
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
			return "mixed {cond, trueRet, [falseRet]} If the first argument evaluates to a true value, the second argument is returned, otherwise the third argument is returned."
					+ " If there is no third argument, it returns void.";
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
				OptimizationOption.OPTIMIZE_DYNAMIC
			);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> args) throws ConfigCompileException {
			//Just, always turn this into an ifelse, though throw a compile error if there are more than
			//3 arguments, if this isn't a pre-ifelse
			boolean allowOverloading = false;
			for (ParseTree arg : args) {
				//If any are CIdentifiers, forward this to ifelse
				if (arg.getData().wasIdentifier()) {
					allowOverloading = true;
					break;
				}
			}
			//Now check for too many/too few arguments
			if (args.size() == 1) {
				//Can't error for this, because if(..){ } is valid, and at this point, there is no
				//difference between if(..) and if(..){ }. For now, a warning, but once { } is its
				//own data structure, this can be revisited.
				//throw new ConfigCompileException("Incorrect number of arguments passed to if()", t);
				CHLog.GetLogger().Log(CHLog.Tags.COMPILER, LogLevel.WARNING, "Empty if statement. This could likely be an error.", t);
			}
			if(!allowOverloading && args.size() > 3){
				throw new ConfigCompileException("if() can only have 3 parameters", t);
			}
			return new ifelse().optimizeDynamic(t, args);
//			if (args.get(0).getData().isDynamic()) {
//				return super.optimizeDynamic(t, args); //Can't optimize
//			} else {
//				if (Static.getBoolean(args.get(0).getData())) {
//					return args.get(1);
//				} else {
//					if (args.size() == 3) {
//						return args.get(2);
//					} else {
//						FileOptions options = new FileOptions(new HashMap<String, String>());
//						if (!args.isEmpty()) {
//							options = args.get(0).getFileOptions();
//						}
//						ParseTree node = new ParseTree(new CVoid(t), options);
//						node.setOptimized(true);
//						return node;
//					}
//				}
//			}
		}

		@Override
		public boolean allowBraces() {
			return true;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Functional usage", "if(true, msg('This is true'), msg('This is false'))"),
				new ExampleScript("With braces, true condition", "if(true){\n\tmsg('This is true')\n}"),
				new ExampleScript("With braces, false condition", "msg('Start')\nif(false){\n\tmsg('This will not show')\n}\nmsg('Finish')"),
			};
		}
		
		
	}

	@api
	public static class _switch extends AbstractFunction {

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
			return new CNull(t);
		}

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			Construct value = parent.seval(nodes[0], env);
			equals equals = new equals();
			for (int i = 1; i <= nodes.length - 2; i += 2) {
				ParseTree statement = nodes[i];
				ParseTree code = nodes[i + 1];
				Construct evalStatement = parent.seval(statement, env);
				if(evalStatement instanceof CSlice){ //More specific subclass of array, we can do more optimal handling here
					long rangeLeft = ((CSlice)evalStatement).getStart();
					long rangeRight = ((CSlice)evalStatement).getFinish();
					if(value instanceof CInt){
						long v = Static.getInt(value, t);
						if((rangeLeft < rangeRight && v >= rangeLeft && v <= rangeRight) 
							|| (rangeLeft > rangeRight &&  v >= rangeRight && v <= rangeLeft) 
							|| (rangeLeft == rangeRight && v == rangeLeft)){
							return parent.seval(code, env);
						}
					} else {
						throw new ConfigRuntimeException("When using slice notation in a switch case, the value being switched on must be an integer, but instead, " + value.val() + " was found.", ExceptionType.CastException, t);
					}
				} else if (evalStatement instanceof CArray) {
					for (String index : ((CArray) evalStatement).keySet()) {
						Construct inner = ((CArray) evalStatement).get(index);
						if(inner instanceof CSlice){
							long rangeLeft = ((CSlice)inner).getStart();
							long rangeRight = ((CSlice)inner).getFinish();
							if(value instanceof CInt){
								long v = Static.getInt(value, t);
								if((rangeLeft < rangeRight && v >= rangeLeft && v <= rangeRight) 
									|| (rangeLeft > rangeRight &&  v >= rangeRight && v <= rangeLeft) 
									|| (rangeLeft == rangeRight && v == rangeLeft)){
									return parent.seval(code, env);
								}
							} else {
								throw new ConfigRuntimeException("When using slice notation in a switch case, the value being switched on must be an integer, but instead, " + value.val() + " was found.", ExceptionType.CastException, t);
							}
						} else {
							if (((CBoolean) equals.exec(t, env, value, inner)).getBoolean()) {
								return parent.seval(code, env);
							}
						}
					}
				} else {
					if (((CBoolean) equals.exec(t, env, value, evalStatement)).getBoolean()) {
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
		public boolean allowBraces() {
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
					+ "}"),
			};
		}
	}

	@api(environments={GlobalEnv.class})
	public static class ifelse extends AbstractFunction implements Optimizable {

		public String getName() {
			return "ifelse";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public String docs() {
			return "mixed {[boolean1, code]..., [elseCode]} Provides a more convenient method"
					+ " for running if/else chains. If none of the conditions are true, and"
					+ " there is no 'else' condition, void is returned.";
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
			return new CNull(t);
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
				if (Static.getBoolean(evalStatement)) {
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
				OptimizationOption.OPTIMIZE_DYNAMIC
			);
		}
		
		private static final String and = new and().getName();
		private static final String g = new Meta.g().getName();

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {

			FileOptions options = new FileOptions(new HashMap<String, String>());
			if (!children.isEmpty()) {
				options = children.get(0).getFileOptions();
			}
			ParseTree node = new ParseTree(new CFunction(this.getName(), t), options);
			node.setOptimized(true);
			List<ParseTree> optimizedTree = new ArrayList<ParseTree>();
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
					if (Static.getBoolean(evalStatement)) {
						if (toReturn == null) {
							toReturn = code;
						}
					} //else it's hard coded false, and we can ignore it.
				} else {
					//It's dynamic, so we can't do anything with it
					optimizedTree.add(statement);
					optimizedTree.add(code);
				}
				// We can pull up if(@a){ if(@b){ ...} } to if(@a && @b){ ... },
				// which, in my profiling is faster. The only special consideration
				// we have to make is to ensure that the inner if is the only statement
				// in the entire block, (including lack of an else) so any code outside the inner if causes this
				// optimization to be impossible. An inner ifelse cannot be optimized, unless it only has 2 arguments
				// (in which case, it's a normal if())
				// If the outer if has an else, we can't do it either, so check that the outer if only has 2 children too.
				if(children.size() == 2 && code.getChildren().size() == 2 && code.getData() instanceof CFunction && code.getData().val().equals("ifelse")){
					CFunction andNode = new CFunction(and, t);
					ParseTree andTree = new ParseTree(andNode, statement.getFileOptions());
					andTree.addChild(statement);
					andTree.addChild(code.getChildAt(0));
					if(optimizedTree.size() < 1){
						optimizedTree.add(andTree);
					} else {
						optimizedTree.set(i, andTree);
					}
					if(optimizedTree.size() < 2){
						optimizedTree.add(code.getChildAt(1));
					} else {
						optimizedTree.set(i + 1, code.getChildAt(1));
					}
					//We need to set this to re-optimize the children, because the and() construction may be unoptimal now
					for(ParseTree pt : andTree.getChildren()){
						pt.setOptimized(false);
					}
					node.setOptimized(false);
				}
			}
			if (toReturn != null) {
				return toReturn;
			}
			if (children.size() % 2 == 1) {
				ParseTree ret = children.get(children.size() - 1);
				if (ret.getData() instanceof CIdentifier) {
					optimizedTree.add(((CIdentifier) ret.getData()).contained());
				} else {
					optimizedTree.add(ret);
				}
				if (children.size() == 1 && optimizedTree.size() == 1) {
					//Oh. Well, we can just return this node then, though we have
					//to surround it with g() so there are no side effects. However,
					//if the function itself has no side effects, we can simply remove
					//it altogether.
					if(optimizedTree.get(0).getData() instanceof CFunction){
						Function f = ((CFunction)optimizedTree.get(0).getData()).getFunction();
						if(f instanceof Optimizable){
							if(((Optimizable)f).optimizationOptions().contains(OptimizationOption.NO_SIDE_EFFECTS)){
								return Optimizable.REMOVE_ME;
							}
						}
					}
					ParseTree gNode = new ParseTree(new CFunction(g, t), options);
					gNode.addChild(optimizedTree.get(0));
					return gNode;
				}
			}
			if (optimizedTree.size() == 1) {
				//The whole tree has been optimized out. Return just the element
				return optimizedTree.get(0);
			}
			node.setChildren(optimizedTree);
			if(node.getChildren().isEmpty()){
				//We have optimized it out entirely, so remove us
				return Optimizable.REMOVE_ME;
			}
			return node;

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
					+ "\n\tmsg('This will not show')\n} else {\n\tmsg('This will show')\n}"),
			};
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
			return ret.getBoolean();
		}

		public String getName() {
			return "equals";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			if (args.length <= 1) {
				throw new ConfigRuntimeException("At least two arguments must be passed to equals", ExceptionType.InsufficientArgumentsException, t);
			}
			boolean referenceMatch = true;
			for (int i = 0; i < args.length - 1; i++) {
				if (args[i] != args[i + 1]) {
					referenceMatch = false;
					break;
				}
			}
			if (referenceMatch) {
				return new CBoolean(true, t);
			}
			if (Static.anyBooleans(args)) {
				boolean equals = true;
				for (int i = 1; i < args.length; i++) {
					boolean arg1 = Static.getBoolean(args[i - 1]);
					boolean arg2 = Static.getBoolean(args[i]);
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
					if (!args[i - 1].val().equals(args[i].val())) {
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
					double arg1 = Static.getNumber(args[i - 1], t);
					double arg2 = Static.getNumber(args[i], t);
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

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InsufficientArgumentsException};
		}

		public String docs() {
			return "boolean {var1, var2[, varX...]} Returns true or false if all the arguments are equal";
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
						OptimizationOption.CACHE_RETURN
			);
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Functional usage", "equals(1, 1.0, '1')"),
				new ExampleScript("Symbolic usage", "1 == 1"),
				new ExampleScript("Not equivalent", "'one' == 'two'"),
			};
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
			return "boolean {val1, val2} Uses a strict equals check, which determines if"
					+ " two values are not only equal, but also the same type. So, while"
					+ " equals('1', 1) returns true, sequals('1', 1) returns false, because"
					+ " the first one is a string, and the second one is an int. More often"
					+ " than not, you want to use plain equals().";
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
			equals equals = new equals();
			if (args[1].getClass().equals(args[0].getClass())
					&& ((CBoolean) equals.exec(t, environment, args)).getBoolean()) {
				return new CBoolean(true, t);
			} else {
				return new CBoolean(false, t);
			}
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Functional usage", "sequals('1', 1)"),
				new ExampleScript("Symbolic usage", "'1' === 1"),
				new ExampleScript("Symbolic usage", "'1' === '1'"),
			};
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
			return "boolean {val1, val2} Equivalent to not(sequals(val1, val2))";
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
			return new CBoolean(!((CBoolean) new sequals().exec(t, environment, args)).getBoolean(), t);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "snequals('1', 1)"),
				new ExampleScript("Basic usage", "snequals('1', '1')"),
			};
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
			return "boolean {val1, val2} Returns true if the two values are NOT equal, or false"
					+ " otherwise. Equivalent to not(equals(val1, val2))";
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
			equals e = new equals();
			CBoolean b = (CBoolean) e.exec(t, env, args);
			return new CBoolean(!b.getBoolean(), t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "nequals('one', 'two')"),
				new ExampleScript("Basic usage", "nequals(1, 1)"),
			};
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
			return "boolean {val1, val2[, valX...]} Returns true if all the values are equal to each other, while"
					+ " ignoring case.";
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

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			if (args.length <= 1) {
				throw new ConfigRuntimeException("At least two arguments must be passed to equals_ic", ExceptionType.InsufficientArgumentsException, t);
			}
			if (Static.anyBooleans(args)) {
				boolean equals = true;
				for (int i = 1; i < args.length; i++) {
					boolean arg1 = Static.getBoolean(args[i - 1]);
					boolean arg2 = Static.getBoolean(args[i]);
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
					double arg1 = Static.getNumber(args[i - 1], t);
					double arg2 = Static.getNumber(args[i], t);
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
						OptimizationOption.CACHE_RETURN
			);
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "equals_ic('test', 'TEST')"),
				new ExampleScript("Basic usage", "equals_ic('completely', 'DIFFERENT')"),
			};
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
			equals_ic e = new equals_ic();
			return new CBoolean(!((CBoolean) e.exec(t, environment, args)).getBoolean(), t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "equals_ic('test', 'TEST')"),
				new ExampleScript("Basic usage", "equals_ic('completely', 'DIFFERENT')"),
			};
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

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if(args[0] instanceof CArray && args[1] instanceof CArray){
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
			return "boolean {val1, val2} Returns true if and only if the two values are actually the same reference."
					+ " Primitives that are equal will always be the same reference, this method is only useful for"
					+ " object/array comparisons.";
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
					+ "msg(ref_equals(@a, @b)) # Again, even though @a == @b and @a === @b, this is false, because they are two different references"),
			};
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
			double arg1 = Static.getNumber(args[0], t);
			double arg2 = Static.getNumber(args[1], t);
			return new CBoolean(arg1 < arg2, t);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "boolean {var1, var2} Returns the results of a less than operation";
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
						OptimizationOption.CACHE_RETURN
			);
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Functional usage", "lt(4, 5)"),
				new ExampleScript("Symbolic usage, true condition", "4 < 5"),
				new ExampleScript("Symbolic usage, false condition", "5 < 4"),
			};
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
			double arg1 = Static.getNumber(args[0], t);
			double arg2 = Static.getNumber(args[1], t);
			return new CBoolean(arg1 > arg2, t);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "boolean {var1, var2} Returns the result of a greater than operation";
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
						OptimizationOption.CACHE_RETURN
			);
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Functional usage", "gt(5, 4)"),
				new ExampleScript("Symbolic usage, true condition", "5 > 4"),
				new ExampleScript("Symbolic usage, false condition", "4 > 5"),
			};
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
			double arg1 = Static.getNumber(args[0], t);
			double arg2 = Static.getNumber(args[1], t);
			return new CBoolean(arg1 <= arg2, t);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "boolean {var1, var2} Returns the result of a less than or equal to operation";
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
						OptimizationOption.CACHE_RETURN
			);
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Functional usage", "lte(4, 5)"),
				new ExampleScript("Symbolic usage, true condition", "4 <= 5"),
				new ExampleScript("Symbolic usage, true condition", "5 <= 5"),
				new ExampleScript("Symbolic usage, false condition", "5 <= 4"),
			};
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
			double arg1 = Static.getNumber(args[0], t);
			double arg2 = Static.getNumber(args[1], t);
			return new CBoolean(arg1 >= arg2, t);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "boolean {var1, var2} Returns the result of a greater than or equal to operation";
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
						OptimizationOption.CACHE_RETURN
			);
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Functional usage", "gte(5, 4)"),
				new ExampleScript("Symbolic usage, true condition", "4 >= 4"),
				new ExampleScript("Symbolic usage, false condition", "4 >= 5"),
			};
		}
	}

	@api(environments={GlobalEnv.class})
	public static class and extends AbstractFunction implements Optimizable {

		public String getName() {
			return "and";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public Construct exec(Target t, Environment env, Construct... args) {
			//This will only happen if they hardcode true/false in, but we still
			//need to handle it appropriately.
			for(Construct c : args){
				if(!Static.getBoolean(c)){
					return new CBoolean(false, t);
				}
			}
			return new CBoolean(true, t);
		}

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			for (ParseTree tree : nodes) {
				Construct c = env.getEnv(GlobalEnv.class).GetScript().seval(tree, env);
				boolean b = Static.getBoolean(c);
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
			return "boolean {var1, [var2...]} Returns the boolean value of a logical AND across all arguments. Uses lazy determination, so once "
					+ "an argument returns false, the function returns.";
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
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			OptimizationUtilities.pullUpLikeFunctions(children, getName());
			Iterator<ParseTree> it = children.iterator();
			while(it.hasNext()){
				//Remove hard coded true values, they won't affect the calculation at all
				ParseTree child = it.next();
				if(child.isConst() && Static.getBoolean(child.getData()) == true){
					it.remove();
				}
			}
			if(children.isEmpty()){
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
				new ExampleScript("Short circuit", "false && msg('This will not show')"),
			};
		}

		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC, OptimizationOption.CONSTANT_OFFLINE);
		}
	}

	@api(environments={GlobalEnv.class})
	public static class or extends AbstractFunction implements Optimizable {

		public String getName() {
			return "or";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public Construct exec(Target t, Environment env, Construct... args) {
			//This will only happen if they hardcode true/false in, but we still
			//need to handle it appropriately.
			for(Construct c : args){
				if(Static.getBoolean(c)){
					return new CBoolean(true, t);
				}
			}
			return new CBoolean(false, t);
		}

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			for (ParseTree tree : nodes) {
				Construct c = env.getEnv(GlobalEnv.class).GetScript().eval(tree, env);
				if (Static.getBoolean(c)) {
					return new CBoolean(true, t);
				}
			}
			return new CBoolean(false, t);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "boolean {var1, [var2...]} Returns the boolean value of a logical OR across all arguments. Uses lazy determination, so once an "
					+ "argument resolves to true, the function returns.";
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
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			OptimizationUtilities.pullUpLikeFunctions(children, getName());
			Iterator<ParseTree> it = children.iterator();
			while(it.hasNext()){
				//Remove hard coded false values, they won't affect the calculation at all
				ParseTree child = it.next();
				if(child.isConst() && Static.getBoolean(child.getData()) == false){
					it.remove();
				}
			}
			if(children.isEmpty()){
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
				new ExampleScript("Short circuit", "true || msg('This will not show')"),
			};
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
			return new CBoolean(!Static.getBoolean(args[0]), t);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "boolean {var1} Returns the boolean value of a logical NOT for this argument";
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
						OptimizationOption.CACHE_RETURN
			);
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Functional usage", "not(false)"),
				new ExampleScript("Symbolic usage, true condition", "!false"),
				new ExampleScript("Symbolic usage, false condition", "!true"),				
			};
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
			return "boolean {val1, val2} Returns the xor of the two values.";
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
			boolean val1 = Static.getBoolean(args[0]);
			boolean val2 = Static.getBoolean(args[1]);
			return new CBoolean(val1 ^ val2, t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "xor(true, false)"),
			};
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
			return "boolean {val1, [val2...]} Return the equivalent of not(and())";
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
			return new CNull(t);
		}

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			and and = new and();
			boolean val = ((CBoolean) and.execs(t, env, parent, nodes)).getBoolean();
			return new CBoolean(!val, t);
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "nand(true, true)"),
			};
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
			return "boolean {val1, [val2...]} Returns the equivalent of not(or())";
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
			return new CNull(t);
		}

		@Override
		public Construct execs(Target t, Environment environment, Script parent, ParseTree... args) throws ConfigRuntimeException {
			or or = new or();
			boolean val = ((CBoolean) or.execs(t, environment, parent, args)).getBoolean();
			return new CBoolean(!val, t);
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "nor(true, false)"),
			};
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
			return "boolean {val1, val2} Returns the xnor of the two values";
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
			boolean val = ((CBoolean) xor.exec(t, environment, args)).getBoolean();
			return new CBoolean(!val, t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "xnor(true, true)"),
			};
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
			return "int {int1, [int2...]} Returns the bitwise AND of the values";
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
			long val = Static.getInt(args[0], t);
			for (int i = 1; i < args.length; i++) {
				val = val & Static.getInt(args[i], t);
			}
			return new CInt(val, t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "bit_and(1, 2, 4)"),
				new ExampleScript("Usage in masking applications. Note that 5 in binary is 101 and 4 is 100. (See bit_or for a more complete example.)", 
					"assign(@var, 5)\nif(bit_and(@var, 4),\n\tmsg('Third bit set')\n)"),
			};
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
			return "int {int1, [int2...]} Returns the bitwise OR of the specified values";
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
			long val = Static.getInt(args[0], t);
			for (int i = 1; i < args.length; i++) {
				val = val | Static.getInt(args[i], t);
			}
			return new CInt(val, t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "bit_or(1, 2, 4)"),
				new ExampleScript("Usage in masking applications. (Used to create a mask)", "assign(@flag1, 1)\nassign(@flag2, 2)\nassign(@flag3, 4)\n"
					+ "assign(@flags, bit_or(@flag1, @flag3))\n"
					+ "if(bit_and(@flags, @flag1),\n\tmsg('Contains flag 1')\n)\n"
					+ "if(!bit_and(@flags, @flag2),\n\tmsg('Does not contain flag 2')\n)"),
			};
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
			return new CInt(~Static.getInt(args[0], t), t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "bit_not(1)"),
			};
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
			return "int {value, bitsToShift} Left shifts the value bitsToShift times";
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
			long value = Static.getInt(args[0], t);
			long toShift = Static.getInt(args[1], t);
			return new CInt(value << toShift, t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "lshift(1, 1)"),
			};
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
			long value = Static.getInt(args[0], t);
			long toShift = Static.getInt(args[1], t);
			return new CInt(value >> toShift, t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "rshift(2, 1)"),
				new ExampleScript("Basic usage", "rshift(-2, 1)"),
			};
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
			return "int {value, bitsToShift} Right shifts value bitsToShift times, pushing a 0, making"
					+ " this an unsigned right shift.";
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
			long value = Static.getInt(args[0], t);
			long toShift = Static.getInt(args[1], t);
			return new CInt(value >>> toShift, t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "urshift(2, 1)"),
				new ExampleScript("Basic usage", "urshift(-2, 1)"),
			};
		}
	}

	@api
	@hide("This isn't a true function, and shouldn't be directly used. It will eventually be removed.")
	public static class _elseif extends AbstractFunction implements Optimizable {

		public String getName() {
			return "elseif";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "elseif {param} Returns an elseif construct. Used internally by the compiler, use"
					+ " in actual code will have undefined behavior.";
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

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			return new CIdentifier("elseif", nodes[0], t);
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CNull(t);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
				OptimizationOption.OPTIMIZE_DYNAMIC
			);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			return Optimizable.PULL_ME_UP;
		}
	}

	@api
	@hide("This isn't a true function, and shouldn't be used as such. Eventually this will be removed.")
	public static class _else extends AbstractFunction {

		public String getName() {
			return "else";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "else {param} Returns an else construct. Used internally by the compiler, use in"
					+ " code will result in undefined behavior.";
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

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			return new CIdentifier("else", nodes[0], t);
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CNull(t);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
}
