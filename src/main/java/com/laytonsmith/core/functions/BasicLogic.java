package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.breakable;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.OptimizationUtilities;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CIdentifier;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CKeyword;
import com.laytonsmith.core.constructs.CLabel;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CSlice;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.LoopBreakException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 */
@core
public class BasicLogic {

	public static String docs() {
		return "These functions provide basic logical operations.";
	}

	@api
	public static class _if extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "if";
		}

		@Override
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
					return CVoid.VOID;
				}
				return parent.seval(__else, env);
			}
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			return CVoid.VOID;
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		@Override
		public String docs() {
			return "mixed {cond, trueRet, [falseRet]} If the first argument evaluates to a true value, the second argument is returned, otherwise the third argument is returned."
					+ " If there is no third argument, it returns void.";
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public boolean preResolveVariables() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_0_1;
		}
		//Doesn't matter, this function is run out of state

		@Override
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

		private static final String and = new and().getName();

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> args, FileOptions fileOptions) throws ConfigCompileException {
			//Check for too many/too few arguments
			if (args.size() == 1) {
				throw new ConfigCompileException("Incorrect number of arguments passed to if()", t);
			}
			if (args.size() > 3) {
				throw new ConfigCompileException("if() can only have 3 parameters", t);
			}
			if(args.get(0).isConst()){
				// We can optimize this one way or the other, since the condition is const
				if(Static.getBoolean(args.get(0).getData())){
					// It's true, return the true condition
					return args.get(1);
				} else {
					// If there are three args, return the else condition, otherwise,
					// have it entirely remove us from the parse tree.
					if(args.size() == 3){
						return args.get(2);
					} else {
						return Optimizable.REMOVE_ME;
					}
				}
			}
			// If the code looks like this:
			// if(@a){
			//		if(@b){
			//		}
			// }
			// then we can turn this into if(@a && @b){ }, as they are functionally
			// equivalent, and this construct tends to be faster (less stack frames, presumably).
			// The caveat is that if the inner if statement has an else statement (or is ifelse)
			// or there are other nodes inside the statement, or we have an else clause
			// we cannot do this optimization, as it then has side effects.
			if(args.get(1).getData() instanceof CFunction && args.get(1).getData().val().equals("if") && args.size() == 2){
				ParseTree _if = args.get(1);
				if(_if.getChildren().size() == 2){
					// All the conditions are met, move this up
					ParseTree myCondition = args.get(0);
					ParseTree theirCondition = _if.getChildAt(0);
					ParseTree theirCode = _if.getChildAt(1);
					ParseTree andClause = new ParseTree(new CFunction(and, t), fileOptions);
					// If it's already an and(), just tack the other condition on
					if(myCondition.getData() instanceof CFunction && myCondition.getData().val().equals(and)){
						andClause = myCondition;
						andClause.addChild(theirCondition);
					} else {
						andClause.addChild(myCondition);
						andClause.addChild(theirCondition);
					}
					args.set(0, andClause);
					args.set(1, theirCode);
				}
			}
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Functional usage", "if(true, msg('This is true'), msg('This is false'))"),
				new ExampleScript("With braces, true condition", "if(true){\n\tmsg('This is true')\n}"),
				new ExampleScript("With braces, false condition", "msg('Start')\nif(false){\n\tmsg('This will not show')\n}\nmsg('Finish')"),};
		}

	}

		@api(environments = {GlobalEnv.class})
	public static class ifelse extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "ifelse";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "mixed {[boolean1, code]..., [elseCode]} Provides a more convenient method"
					+ " for running if/else chains. If none of the conditions are true, and"
					+ " there is no 'else' condition, void is returned.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InsufficientArgumentsException};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public boolean preResolveVariables() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return CNull.NULL;
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
			return CVoid.VOID;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public Set<Optimizable.OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					Optimizable.OptimizationOption.OPTIMIZE_DYNAMIC
			);
		}

		private static final String g = new DataHandling.g().getName();

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children, FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			// TODO: Redo this optimization.
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Functional usage", "ifelse(false, msg('This is false'), true, msg('This is true'))"),
				new ExampleScript("With braces", "if(false){\n\tmsg('This is false')\n} else {\n\tmsg('This is true')\n}"),
				new ExampleScript("With braces, with else if", "if(false){\n\tmsg('This will not show')\n} else if(false){\n"
				+ "\n\tmsg('This will not show')\n} else {\n\tmsg('This will show')\n}"),};
		}
	}

	@api
	@breakable
	public static class _switch extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "switch";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "mixed {value, [equals, code]..., [defaultCode]} Provides a switch statement. If none of the conditions"
					+ " match, and no default is provided, void is returned."
					+ " See the documentation on [[CommandHelper/Logic|Logic]] for more information. ----"
					+ " In addition, slices may be used to indicate ranges of integers that should trigger the specified"
					+ " case. Slices embedded in an array are fine as well. Switch statements also support brace/case/default"
					+ " syntax, as in most languages, althrough unlike most languages, fallthrough isn't supported. Breaking"
					+ " with break() isn't required, but allowed. A number greater than 1 may be sent to break, and breaking"
					+ " out of the switch will consume a \"break counter\" and the break will continue up the chain."
					+ " If you do use break(), the return value of switch is ignored. See the examples for usage"
					+ " of brace/case/default syntax, which is highly recommended.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InsufficientArgumentsException};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public boolean preResolveVariables() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return CNull.NULL;
		}

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			Construct value = parent.seval(nodes[0], env);
			equals equals = new equals();
			try {
				for (int i = 1; i <= nodes.length - 2; i += 2) {
					ParseTree statement = nodes[i];
					ParseTree code = nodes[i + 1];
					Construct evalStatement = parent.seval(statement, env);
					if (evalStatement instanceof CSlice) { //More specific subclass of array, we can do more optimal handling here
						long rangeLeft = ((CSlice) evalStatement).getStart();
						long rangeRight = ((CSlice) evalStatement).getFinish();
						if (value instanceof CInt) {
							long v = Static.getInt(value, t);
							if ((rangeLeft < rangeRight && v >= rangeLeft && v <= rangeRight)
									|| (rangeLeft > rangeRight && v >= rangeRight && v <= rangeLeft)
									|| (rangeLeft == rangeRight && v == rangeLeft)) {
								return parent.seval(code, env);
							}
						}
					} else if (evalStatement instanceof CArray) {
						for (String index : ((CArray) evalStatement).stringKeySet()) {
							Construct inner = ((CArray) evalStatement).get(index, t);
							if (inner instanceof CSlice) {
								long rangeLeft = ((CSlice) inner).getStart();
								long rangeRight = ((CSlice) inner).getFinish();
								if (value instanceof CInt) {
									long v = Static.getInt(value, t);
									if ((rangeLeft < rangeRight && v >= rangeLeft && v <= rangeRight)
											|| (rangeLeft > rangeRight && v >= rangeRight && v <= rangeLeft)
											|| (rangeLeft == rangeRight && v == rangeLeft)) {
										return parent.seval(code, env);
									}
								}
							} else {
								if (equals.exec(t, env, value, inner).getBoolean()) {
									return parent.seval(code, env);
								}
							}
						}
					} else {
						if (equals.exec(t, env, value, evalStatement).getBoolean()) {
							return parent.seval(code, env);
						}
					}
				}
				if (nodes.length % 2 == 0) {
					return parent.seval(nodes[nodes.length - 1], env);
				}
			} catch (LoopBreakException ex) {
				//Ignored, unless the value passed in is greater than 1, in which case
				//we rethrow.
				if (ex.getTimes() > 1) {
					ex.setTimes(ex.getTimes() - 1);
					throw ex;
				}
			}
			return CVoid.VOID;
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
				new ExampleScript("With braces/case/default", "switch('theValue'){\n"
				+ "\tcase 'notTheValue':\n"
				+ "\t\tmsg('Nope')\n"
				+ "\tcase 'theValue':\n"
				+ "\t\tmsg('Success')\n"
				+ "}"),
				new ExampleScript("With braces/case/default. Note the lack of fallthrough, even without a break(),"
						+ " except where two cases are directly back to back.",
						"@a = 5\nswitch(@a){\n"
								+ "\tcase 1:\n"
								+ "\tcase 2:\n"
								+ "\t\tmsg('1 or 2');\n"
								+ "\tcase 3..4:\n"
								+ "\t\tmsg('3 or 4');\n"
								+ "\t\tbreak(); # This is optional, as it would break here anyways, but is allowed.\n"
								+ "\tcase 5..6:\n"
								+ "\tcase 8:\n"
								+ "\t\tmsg('5, 6, or 8')\n"
								+ "\tdefault:\n"
								+ "\t\tmsg('Any other value'); # A default is optional\n"
								+ "}\n"),
				new ExampleScript("With default condition", "switch('noMatch',\n"
				+ "\t'notIt1',\n"
				+ "\t\tmsg('Nope'),\n"
				+ "\t'notIt2',\n"
				+ "\t\tmsg('Nope'),\n"
				+ "\t, #Default:\n"
				+ "\t\tmsg('Success')\n"
				+ ")"),
				new ExampleScript("With multiple matches using an array", "switch('string',\n"
				+ "\tarray('value1', 'value2', 'string'),\n"
				+ "\t\tmsg('Match'),\n"
				+ "\t'value3',\n"
				+ "\t\tmsg('No match')\n"
				+ ")"),
				new ExampleScript("With slices", "switch(5,\n"
				+ "\t1..2,\n"
				+ "\t\tmsg('First'),\n"
				+ "\t3..5,\n"
				+ "\t\tmsg('Second'),\n"
				+ "\t6..8,\n"
				+ "\t\tmsg('Third')\n"
				+ ")"),
				new ExampleScript("With slices in an array", "switch(5,\n"
				+ "\tarray(1..2, 3..5),\n"
				+ "\t\tmsg('First'),\n"
				+ "\t6..8,\n"
				+ "\t\tmsg('Second')\n"
				+ ")"),};
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children, FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			if (children.get(1).getData() instanceof CFunction
					&& new StringHandling.sconcat().getName().equals(children.get(1).getData().val())) {
				//This is the brace/case/default usage of switch, probably. We need
				//to refactor the data into the old switch format.
				List<ParseTree> newChildren = new ArrayList<>();
				newChildren.add(children.get(0)); //Initial child
				List<ParseTree> c = children.get(1).getChildren();
				List<ParseTree> lastCodeBlock = new ArrayList<>();
				CArray conditions = new CArray(t);
				boolean inCase = false;
				boolean inDefault = false;
				for (int i = 0; i < c.size(); i++) {
					//Need up to a 2 lookahead
					ParseTree c1 = c.get(i);
					ParseTree c2 = null;
					if (i + 1 < c.size()) {
						c2 = c.get(i + 1);
					}
					if(CKeyword.isKeyword(c1, "case")) {
						//If this is a case AND the next one is
						//a label, this is a case.
						if (c2 != null && c2.getData() instanceof CLabel) {
							if (inDefault) {
								//Default must come last
								throw new ConfigCompileException("Unexpected case; the default case must come last.", t);
							}
							if (lastCodeBlock.size() > 0) {
								//Ok, need to push some stuff on to the new children
								newChildren.add(new ParseTree(conditions, c2.getFileOptions()));
								conditions = new CArray(t);
								ParseTree codeBlock = new ParseTree(new CFunction(new StringHandling.sconcat().getName(), t), c2.getFileOptions());
								for (ParseTree line : lastCodeBlock) {
									codeBlock.addChild(line);
								}
								if (codeBlock.getChildren().size() == 1) {
									codeBlock = codeBlock.getChildAt(0);
								}
								newChildren.add(codeBlock);
								lastCodeBlock = new ArrayList<>();
							}
							//Yes, it is. Now we also have to look ahead for
							//other cases, because
							//case 1:
							//case 2:
							//	code()
							//would be turned into array(1, 2), code() in the
							//old style.
							conditions.push(((CLabel) c2.getData()).cVal());
							inCase = true;
							i++;
							continue;
						}
					}
					if (c1.getData() instanceof CLabel && CKeyword.isKeyword(((CLabel)c1.getData()).cVal(), "default")) {
						//Default case
						if (lastCodeBlock.size() > 0) {
							//Ok, need to push some stuff on to the new children
							newChildren.add(new ParseTree(conditions, c1.getFileOptions()));
							conditions = new CArray(t);
							ParseTree codeBlock = new ParseTree(new CFunction(new StringHandling.sconcat().getName(), t), c1.getFileOptions());
							for (ParseTree line : lastCodeBlock) {
								codeBlock.addChild(line);
							}
							if (codeBlock.getChildren().size() == 1) {
								codeBlock = codeBlock.getChildAt(0);
							}
							newChildren.add(codeBlock);
							lastCodeBlock = new ArrayList<>();
						} else if (conditions.size() > 0) {
							//Special case where they have
							//case 0:
							//default:
							//	code();
							//This causes there to be conditions, but no code,
							//which throws off the argument length. In actuality,
							//we can simply throw out the conditions, because
							//this block of code will run if 0 or the default is
							//hit, and if 0 is the condition provided, it would
							//work the same if it weren't specified at all.
							conditions = new CArray(t);
						}
						inDefault = true;
						continue;
					}

					//Loop forward until we get to the next case
					if (inCase || inDefault) {
						lastCodeBlock.add(c1);
					}
				}
				if (conditions.size() > 0) {
					newChildren.add(new ParseTree(conditions, children.get(0).getFileOptions()));
				}
				if (lastCodeBlock.size() > 0) {
					ParseTree codeBlock = new ParseTree(new CFunction(new StringHandling.sconcat().getName(), t), lastCodeBlock.get(0).getFileOptions());
					for (ParseTree line : lastCodeBlock) {
						codeBlock.addChild(line);
					}
					if (codeBlock.getChildren().size() == 1) {
						codeBlock = codeBlock.getChildAt(0);
					}
					newChildren.add(codeBlock);
				}
				children.clear();
				children.addAll(newChildren);
			}

			//Loop through all the conditions and make sure each is unique. Also
			//make sure that each value is not dynamic.
			String notConstant = "Cases for a switch statement must be constant, not variable";
			String alreadyContains = "The switch statement already contains a case for this value, remove the duplicate value";
			final equals EQUALS = new equals();
			Set<Construct> values = new TreeSet<>(new Comparator<Construct>() {

				@Override
				public int compare(Construct t, Construct t1) {
					if (EQUALS.exec(Target.UNKNOWN, null, t, t1).getBoolean()) {
						return 0;
					} else {
						return t.val().compareTo(t1.val());
					}
				}
			});
			for (int i = 1; i < children.size(); i += 2) {
				//To standardize the rest of the code (and to optimize), go ahead and resolve array()
				if (children.get(i).getData() instanceof CFunction
						&& new DataHandling.array().getName().equals(children.get(i).getData().val())) {
					CArray data = new CArray(t);
					for (ParseTree child : children.get(i).getChildren()) {
						if (child.getData().isDynamic()) {
							throw new ConfigCompileException(notConstant, child.getTarget());
						}
						data.push(child.getData());
					}
					children.set(i, new ParseTree(data, children.get(i).getFileOptions()));
				}
				//Now we validate that the values are constant and non-repeating.
				if (children.size() % 2 == 0 && i == children.size() - 1) {
					//Even number, means there is a default, so stop checking here
					break;
				}
				if (children.get(i).getData() instanceof CArray) {
					List<Construct> list = ((CArray) children.get(i).getData()).asList();
					for (Construct c : list) {
						if (c instanceof CSlice) {
							for (Construct cc : ((CSlice) c).asList()) {
								if (values.contains(cc)) {
									throw new ConfigCompileException(alreadyContains, cc.getTarget());
								}
								values.add(cc);
							}
						} else {
							if (c.isDynamic()) {
								throw new ConfigCompileException(notConstant, c.getTarget());
							}
							if (values.contains(c)) {
								throw new ConfigCompileException(alreadyContains, c.getTarget());
							}
							values.add(c);
						}
					}
				} else {
					Construct c = children.get(i).getData();
					if (c.isDynamic()) {
						throw new ConfigCompileException(notConstant, c.getTarget());
					}
					if (values.contains(c)) {
						throw new ConfigCompileException(alreadyContains, c.getTarget());
					}
					values.add(c);
				}
			}

			if ((children.size() > 3 || children.get(1).getData() instanceof CArray)
					//No point in doing this optimization if there are only 3 args and the case is flat.
					//Also, doing this check prevents an inifinite loop during optimization.
					&& !children.get(0).getData().isDynamic()) {
				ParseTree toReturn = null;
				//The item passed in is constant (or has otherwise been made constant)
				//so we can go ahead and condense this down to the single code path
				//in the switch.
				for (int i = 1; i < children.size(); i += 2) {
					Construct data = children.get(i).getData();

					if (!(data instanceof CArray) || data instanceof CSlice) {
						//Put it in an array to make the rest of this parsing easier.
						data = new CArray(t);
						((CArray) data).push(children.get(i).getData());
					}
					for (Construct value : ((CArray) data).asList()) {
						if (value instanceof CSlice) {
							long rangeLeft = ((CSlice) value).getStart();
							long rangeRight = ((CSlice) value).getFinish();
							if (children.get(0).getData() instanceof CInt) {
								long v = Static.getInt(children.get(0).getData(), t);
								if ((rangeLeft < rangeRight && v >= rangeLeft && v <= rangeRight)
										|| (rangeLeft > rangeRight && v >= rangeRight && v <= rangeLeft)
										|| (rangeLeft == rangeRight && v == rangeLeft)) {
									toReturn = children.get(i + 1);
									break;
								}
							}
						} else {
							if (EQUALS.exec(t, null, children.get(0).getData(), value).getBoolean()) {
								toReturn = children.get(i + 1);
								break;
							}
						}
					}
				}
				//None of the values match. Return the default case, if it exists, or remove the switch entirely
				//if it doesn't.
				if(toReturn == null){
					if (children.size() % 2 == 0) {
						toReturn = children.get(children.size() - 1);
					} else {
						return Optimizable.REMOVE_ME;
					}
				}
				//Unfortunately, we can't totally remove this, because otherwise break()s in the code
				//will go unchecked, so we need to keep switch in the code somehow. To make it easy though,
				//we'll make the most efficient switch we can.
				ParseTree ret = new ParseTree(new CFunction(new _switch().getName(), t), fileOptions);
				ret.addChild(new ParseTree(new CInt(1, t), fileOptions));
				ret.addChild(new ParseTree(new CInt(1, t), fileOptions));
				ret.addChild(toReturn);
				return ret;
			}
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC, OptimizationOption.PRIORITY_OPTIMIZATION);
		}
	}

	@api
	@seealso({nequals.class, sequals.class, snequals.class})
	public static class equals extends AbstractFunction implements Optimizable {

		private static final equals self = new equals();

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
			CBoolean ret = self.exec(Target.UNKNOWN, null, one, two);
			return ret.getBoolean();
		}

		@Override
		public String getName() {
			return "equals";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public CBoolean exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
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
				return CBoolean.TRUE;
			}
			if(Static.anyNulls(args)){
				boolean equals = true;
				for(int i = 0; i < args.length; i++){
					Construct c = args[i];
					if(!(c instanceof CNull)){
						equals = false;
					}
				}
				return CBoolean.get(equals);
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
				return CBoolean.get(equals);
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
					return CBoolean.TRUE;
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
				return CBoolean.get(equals);
			} catch (ConfigRuntimeException e) {
				return CBoolean.FALSE;
			}
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InsufficientArgumentsException};
		}

		@Override
		public String docs() {
			return "boolean {var1, var2[, varX...]} Returns true or false if all the arguments are equal. Operator syntax is"
					+ " also supported: @a == @b";
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		@Override
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
				new ExampleScript("Operator syntax", "1 == 1"),
				new ExampleScript("Not equivalent", "'one' == 'two'"),};
		}
	}

	@api
	@seealso({equals.class, nequals.class, snequals.class})
	public static class sequals extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "sequals";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {val1, val2} Uses a strict equals check, which determines if"
					+ " two values are not only equal, but also the same type. So, while"
					+ " equals('1', 1) returns true, sequals('1', 1) returns false, because"
					+ " the first one is a string, and the second one is an int. More often"
					+ " than not, you want to use plain equals(). In addition, type juggling is"
					+ " explicitely not performed on strings. Thus '2' !== '2.0', despite those"
					+ " being ==. Operator syntax is also"
					+ " supported: @a === @b";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public CBoolean exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if (args[1].getClass().equals(args[0].getClass())) {
				if(args[0] instanceof CString && args[1] instanceof CString){
					// Check for actual string equality, so we don't do type massaging
					// for numeric strings. Thus '2' !== '2.0'
					return CBoolean.get(args[0].val().equals(args[1].val()));
				}
				return new equals().exec(t, environment, args);
			} else {
				return CBoolean.FALSE;
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
				new ExampleScript("Symbolic usage", "'1' === '1'"),};
		}
	}

	@api
	@seealso({sequals.class})
	public static class snequals extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "snequals";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {val1, val2} Equivalent to not(sequals(val1, val2)). Operator syntax"
					+ " is also supported: @a !== @b";
		}

		@Override
		public ExceptionType[] thrown() {
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
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new sequals().exec(t, environment, args).not();
		}

		@Override
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
				new ExampleScript("Operator syntax", "'1' !== '1'"),
				new ExampleScript("Operator syntax", "'1' !== 1"),
			};
		}
	}

	@api
	@seealso({equals.class, sequals.class, snequals.class})
	public static class nequals extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "nequals";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {val1, val2} Returns true if the two values are NOT equal, or false"
					+ " otherwise. Equivalent to not(equals(val1, val2)). Operator syntax is also"
					+ " supported: @a != @b";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public CBoolean exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return new equals().exec(t, env, args).not();
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
				new ExampleScript("Operator syntax", "1 != 1"),
				new ExampleScript("Operator syntax", "1 != 2"),
			};
		}
	}

	@api
	public static class equals_ic extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "equals_ic";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "boolean {val1, val2[, valX...]} Returns true if all the values are equal to each other, while"
					+ " ignoring case.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InsufficientArgumentsException};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_2_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public CBoolean exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
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
				return CBoolean.get(equals);
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
					return CBoolean.TRUE;
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
				return CBoolean.get(equals);
			} catch (ConfigRuntimeException e) {
				return CBoolean.FALSE;
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
				new ExampleScript("Basic usage", "equals_ic('completely', 'DIFFERENT')"),};
		}
	}

	@api
	@seealso({equals_ic.class})
	public static class nequals_ic extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "nequals_ic";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {val1, val2} Returns true if the two values are NOT equal to each other, while"
					+ " ignoring case.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public CBoolean exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new equals_ic().exec(t, environment, args).not();
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
				new ExampleScript("Basic usage", "equals_ic('completely', 'DIFFERENT')"),};
		}
	}

	@api
	public static class ref_equals extends AbstractFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
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
		public CBoolean exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if (args[0] instanceof CArray && args[1] instanceof CArray) {
				return CBoolean.get(args[0] == args[1]);
			} else {
				return new equals().exec(t, environment, args);
			}
		}

		@Override
		public String getName() {
			return "ref_equals";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {val1, val2} Returns true if and only if the two values are actually the same reference."
					+ " Primitives that are equal will always be the same reference, this method is only useful for"
					+ " object/array comparisons.";
		}

		@Override
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
	@seealso({gt.class, lte.class, gte.class})
	public static class lt extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "lt";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			double arg1 = Static.getNumber(args[0], t);
			double arg2 = Static.getNumber(args[1], t);
			return CBoolean.get(arg1 < arg2);
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		@Override
		public String docs() {
			return "boolean {var1, var2} Returns the results of a less than operation. Operator syntax"
					+ " is also supported: @a < @b";
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		@Override
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
				new ExampleScript("Operator syntax, true condition", "4 < 5"),
				new ExampleScript("Operator syntax, false condition", "5 < 4"),};
		}
	}

	@api
	@seealso({lt.class, lte.class, gte.class})
	public static class gt extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "gt";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			double arg1 = Static.getNumber(args[0], t);
			double arg2 = Static.getNumber(args[1], t);
			return CBoolean.get(arg1 > arg2);
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		@Override
		public String docs() {
			return "boolean {var1, var2} Returns the result of a greater than operation. Operator syntax is also supported:"
					+ " @a > @b";
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		@Override
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
				new ExampleScript("Operator syntax, true condition", "5 > 4"),
				new ExampleScript("Operator syntax, false condition", "4 > 5"),};
		}
	}

	@api
	@seealso({lt.class, gt.class, gte.class})
	public static class lte extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "lte";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			double arg1 = Static.getNumber(args[0], t);
			double arg2 = Static.getNumber(args[1], t);
			return CBoolean.get(arg1 <= arg2);
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		@Override
		public String docs() {
			return "boolean {var1, var2} Returns the result of a less than or equal to operation. Operator"
					+ " syntax is also supported: @a <= @b";
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		@Override
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
				new ExampleScript("Operator syntax, true condition", "4 <= 5"),
				new ExampleScript("Operator syntax, true condition", "5 <= 5"),
				new ExampleScript("Operator syntax, false condition", "5 <= 4"),};
		}
	}

	@api
	@seealso({lt.class, gt.class, lte.class})
	public static class gte extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "gte";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			double arg1 = Static.getNumber(args[0], t);
			double arg2 = Static.getNumber(args[1], t);
			return CBoolean.get(arg1 >= arg2);
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		@Override
		public String docs() {
			return "boolean {var1, var2} Returns the result of a greater than or equal to operation. Operator"
					+ " sytnax is also supported: @a >= @b";
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		@Override
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
				new ExampleScript("Operator syntax, true condition", "4 >= 4"),
				new ExampleScript("Operator syntax, false condition", "4 >= 5"),};
		}
	}

	@api(environments = {GlobalEnv.class})
	@seealso({or.class})
	public static class and extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "and";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public CBoolean exec(Target t, Environment env, Construct... args) {
			//This will only happen if they hardcode true/false in, but we still
			//need to handle it appropriately.
			for (Construct c : args) {
				if (!Static.getBoolean(c)) {
					return CBoolean.FALSE;
				}
			}
			return CBoolean.TRUE;
		}

		@Override
		public CBoolean execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			for (ParseTree tree : nodes) {
				Construct c = env.getEnv(GlobalEnv.class).GetScript().seval(tree, env);
				boolean b = Static.getBoolean(c);
				if (b == false) {
					return CBoolean.FALSE;
				}
			}
			return CBoolean.TRUE;
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		@Override
		public String docs() {
			return "boolean {var1, [var2...]} Returns the boolean value of a logical AND across all arguments. Uses lazy determination, so once "
					+ "an argument returns false, the function returns. Operator syntax is supported:"
					+ " @a && @b";
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children, FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			OptimizationUtilities.pullUpLikeFunctions(children, getName());
			Iterator<ParseTree> it = children.iterator();
			boolean foundFalse = false;
			while (it.hasNext()) {
				//Remove hard coded true values, they won't affect the calculation at all
				//Also walk through the children, and if we find a hardcoded false, discard all the following values.
				//If we do find a hardcoded false, though we can know ahead of time that this statement as a whole
				//will be false, we can't remove everything, as the parameters beforehand may have side effects, so
				//we musn't remove them.
				ParseTree child = it.next();
				if(foundFalse){
					it.remove();
					continue;
				}
				if (child.isConst()){
					if(Static.getBoolean(child.getData()) == true){
						it.remove();
					} else {
						foundFalse = true;
					}
				}
			}
			// TODO: Can't do this yet, because children of side effect free functions may still have side effects that
			// we need to maintain. However, with complications introduced by code branch functions, we can't process
			// this yet.
//			if(foundFalse){
//				//However, we can remove any functions that have no side effects that come before the false.
//				it = children.iterator();
//				while(it.hasNext()){
//					Construct data = it.next().getData();
//					if(data instanceof CFunction && ((CFunction)data).getFunction() instanceof Optimizable){
//						if(((Optimizable)((CFunction)data).getFunction()).optimizationOptions().contains(OptimizationOption.NO_SIDE_EFFECTS)){
//							it.remove();
//						}
//					}
//				}
//			}
			// At this point, it could be that there are some conditions with side effects, followed by a final false. However,
			// if false is the only remaining condition (which could be) then we can simply return false here.
			if(children.size() == 1 && children.get(0).isConst() && Static.getBoolean(children.get(0).getData()) == false){
				return new ParseTree(CBoolean.FALSE, fileOptions);
			}
			if (children.isEmpty()) {
				//We've removed all the children, so return true, because they were all true.
				return new ParseTree(CBoolean.TRUE, fileOptions);
			}
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Functional usage", "and(true, true)"),
				new ExampleScript("Operator syntax, true condition", "true && true"),
				new ExampleScript("Operator syntax, false condition", "true && false"),
				new ExampleScript("Short circuit", "false && msg('This will not show')"),};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC, OptimizationOption.CONSTANT_OFFLINE);
		}
	}

	@api(environments = {GlobalEnv.class})
	@seealso({and.class})
	public static class or extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "or";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public CBoolean exec(Target t, Environment env, Construct... args) {
			//This will only happen if they hardcode true/false in, but we still
			//need to handle it appropriately.
			for (Construct c : args) {
				if (Static.getBoolean(c)) {
					return CBoolean.TRUE;
				}
			}
			return CBoolean.FALSE;
		}

		@Override
		public CBoolean execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			for (ParseTree tree : nodes) {
				Construct c = env.getEnv(GlobalEnv.class).GetScript().seval(tree, env);
				if (Static.getBoolean(c)) {
					return CBoolean.TRUE;
				}
			}
			return CBoolean.FALSE;
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		@Override
		public String docs() {
			return "boolean {var1, [var2...]} Returns the boolean value of a logical OR across all arguments. Uses lazy determination, so once an "
					+ "argument resolves to true, the function returns. Operator syntax is also supported: @a || @b";
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children, FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			OptimizationUtilities.pullUpLikeFunctions(children, getName());
			Iterator<ParseTree> it = children.iterator();
			boolean foundTrue = false;
			while (it.hasNext()) {
				//Remove hard coded false values, they won't affect the calculation at all
				//Also walk through the children, and if we find a hardcoded true, discard all the following values.
				//If we do find a hardcoded true, though we can know ahead of time that this statement as a whole
				//will be true, we can't remove everything, as the parameters beforehand may have side effects, so
				//we musn't remove them.
				ParseTree child = it.next();
				if(foundTrue){
					it.remove();
					continue;
				}
				if (child.isConst()){
					if(Static.getBoolean(child.getData()) == false){
						it.remove();
					} else {
						foundTrue = true;
					}
				}
			}
			// TODO: Can't do this yet, because children of side effect free functions may still have side effects that
			// we need to maintain. However, with complications introduced by code branch functions, we can't process
			// this yet.
//			if(foundTrue){
//				//However, we can remove any functions that have no side effects that come before the true.
//				it = children.iterator();
//				while(it.hasNext()){
//					Construct data = it.next().getData();
//					if(data instanceof CFunction && ((CFunction)data).getFunction() instanceof Optimizable){
//						if(((Optimizable)((CFunction)data).getFunction()).optimizationOptions().contains(OptimizationOption.NO_SIDE_EFFECTS)){
//							it.remove();
//						}
//					}
//				}
//			}
			// At this point, it could be that there are some conditions with side effects, followed by a final true. However,
			// if true is the only remaining condition (which could be) then we can simply return true here.
			if(children.size() == 1 && children.get(0).isConst() && Static.getBoolean(children.get(0).getData()) == true){
				return new ParseTree(CBoolean.TRUE, fileOptions);
			}
			if (children.isEmpty()) {
				//We've removed all the children, so return false, because they were all false.
				return new ParseTree(CBoolean.FALSE, fileOptions);
			}
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Functional usage", "or(false, true)"),
				new ExampleScript("Operator syntax, true condition", "true || false"),
				new ExampleScript("Operator syntax, false condition", "false || false"),
				new ExampleScript("Short circuit", "true || msg('This will not show')"),};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC, OptimizationOption.CONSTANT_OFFLINE);
		}
	}

	@api
	public static class not extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "not";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			return CBoolean.get(!Static.getBoolean(args[0]));
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		@Override
		public String docs() {
			return "boolean {var1} Returns the boolean value of a logical NOT for this argument. Operator syntax is also supported: !@var";
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		@Override
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
				new ExampleScript("Operator syntax, true condition", "!false"),
				new ExampleScript("Operator syntax, false condition", "!true"),
				new ExampleScript("Operator syntax, using variable", "!@var"),
			};
		}
	}

	@api
	public static class xor extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "xor";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {val1, val2} Returns the xor of the two values.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public CBoolean exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			boolean val1 = Static.getBoolean(args[0]);
			boolean val2 = Static.getBoolean(args[1]);
			return CBoolean.get(val1 ^ val2);
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
				new ExampleScript("Basic usage", "xor(true, false)"),};
		}
	}

	@api
	@seealso({and.class})
	public static class nand extends AbstractFunction {

		@Override
		public String getName() {
			return "nand";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "boolean {val1, [val2...]} Return the equivalent of not(and())";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) {
			return CNull.NULL;
		}

		@Override
		public CBoolean execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			return new and().execs(t, env, parent, nodes).not();
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
	@seealso({or.class})
	public static class nor extends AbstractFunction {

		@Override
		public String getName() {
			return "nor";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "boolean {val1, [val2...]} Returns the equivalent of not(or())";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) {
			return CNull.NULL;
		}

		@Override
		public CBoolean execs(Target t, Environment environment, Script parent, ParseTree... args) throws ConfigRuntimeException {
			return new or().execs(t, environment, parent, args).not();
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
	@seealso({xor.class})
	public static class xnor extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "xnor";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {val1, val2} Returns the xnor of the two values";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public CBoolean exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new xor().exec(t, environment, args).not();
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
				new ExampleScript("Basic usage", "xnor(true, true)"),};
		}
	}

	@api
	public static class bit_and extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "bit_and";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "int {int1, int2, [int3...]} Returns the bitwise AND of the values";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.InsufficientArgumentsException};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
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
					OptimizationOption.CACHE_RETURN,
					OptimizationOption.OPTIMIZE_DYNAMIC
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "bit_and(1, 2, 4)"),
				new ExampleScript("Usage in masking applications. Note that 5 in binary is 101 and 4 is 100. (See bit_or for a more complete example.)",
				"assign(@var, 5)\nif(bit_and(@var, 4),\n\tmsg('Third bit set')\n)"),};
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children, FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			if (children.size() < 2){
				throw new ConfigCompileException("bit_and() requires at least 2 arguments.", t);
			}
			return null;
		}
	}

	@api
	public static class bit_or extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "bit_or";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "int {int1, int2, [int3...]} Returns the bitwise OR of the specified values";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.InsufficientArgumentsException};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
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
					OptimizationOption.CACHE_RETURN,
					OptimizationOption.OPTIMIZE_DYNAMIC
			);
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

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children, FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			if (children.size() < 2){
				throw new ConfigCompileException("bit_or() requires at least 2 arguments.", t);
			}
			return null;
		}
	}

	@api
	public static class bit_xor extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "bit_xor";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "int {int1, int2, [int3...]} Returns the bitwise exclusive OR of the specified values";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.InsufficientArgumentsException};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			long val = Static.getInt(args[0], t);
			for (int i = 1; i < args.length; i++) {
				val = val ^ Static.getInt(args[i], t);
			}
			return new CInt(val, t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN,
					OptimizationOption.OPTIMIZE_DYNAMIC
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "bit_xor(1, 2, 4)"),};
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children, FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			if (children.size() < 2){
				throw new ConfigCompileException("bit_xor() requires at least 2 arguments.", t);
			}
			return null;
		}
	}

	@api
	public static class bit_not extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "bit_not";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "int {int1} Returns the bitwise NOT of the given value";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
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
				new ExampleScript("Basic usage", "bit_not(1)"),};
		}
	}

	@api
	public static class lshift extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "lshift";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "int {value, bitsToShift} Left shifts the value bitsToShift times";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
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
				new ExampleScript("Basic usage", "lshift(1, 1)"),};
		}
	}

	@api
	public static class rshift extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "rshift";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "int {value, bitsToShift} Right shifts the value bitsToShift times";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
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
				new ExampleScript("Basic usage", "rshift(-2, 1)"),};
		}
	}

	@api
	public static class urshift extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "urshift";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "int {value, bitsToShift} Right shifts value bitsToShift times, pushing a 0, making"
					+ " this an unsigned right shift.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
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
				new ExampleScript("Basic usage", "urshift(-2, 1)"),};
		}
	}

	@api
	public static class compile_error extends AbstractFunction implements Optimizable {

		@Override
		public ExceptionType[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "compile_error";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "nothing {message} Throws a compile error unconditionally at link time, if the function has not been fully compiled"
					+ " out with preprocessor directives. This is useful for causing a custom compile error if certain compilation environment"
					+ " settings are not correct.";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.CUSTOM_LINK, OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children, FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			if(!children.get(0).isConst()){
				throw new ConfigCompileException(getName() + "'s argument must be a hardcoded string.", t);
			}
			return null;
		}

		@Override
		public void link(Target t, List<ParseTree> children) throws ConfigCompileException {
			throw new ConfigCompileException(children.get(0).getData().val(), t);
		}

	}

}
