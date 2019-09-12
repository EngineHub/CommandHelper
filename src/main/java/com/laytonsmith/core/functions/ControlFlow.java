package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.breakable;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Procedure;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.BranchStatement;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.VariableScope;
import com.laytonsmith.core.compiler.keywords.InKeyword;
import com.laytonsmith.core.constructs.CArray;
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
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREInsufficientArgumentsException;
import com.laytonsmith.core.exceptions.CRE.CREInvalidProcedureException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.FunctionReturnException;
import com.laytonsmith.core.exceptions.LoopBreakException;
import com.laytonsmith.core.exceptions.LoopContinueException;
import com.laytonsmith.core.natives.interfaces.Iterator;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.tools.docgen.templates.ArrayIteration;
import com.laytonsmith.tools.docgen.templates.Loops;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@core
public class ControlFlow {

	public static String docs() {
		return "This class provides various functions to manage control flow.";
	}

	@api
	public static class _if extends AbstractFunction implements Optimizable, BranchStatement, VariableScope {

		@Override
		public String getName() {
			return "if";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			for(ParseTree node : nodes) {
				if(node.getData() instanceof CIdentifier) {
					return new ifelse().execs(t, env, parent, nodes);
				}
			}
			ParseTree condition = nodes[0];
			ParseTree __if = nodes[1];
			ParseTree __else = null;
			if(nodes.length == 3) {
				__else = nodes[2];
			}

			if(ArgumentValidation.getBooleanish(parent.seval(condition, env), t)) {
				return parent.seval(__if, env);
			} else {
				if(__else == null) {
					return CVoid.VOID;
				}
				return parent.seval(__else, env);
			}
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args)
				throws CancelCommandException, ConfigRuntimeException {
			return CVoid.VOID;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public String docs() {
			return "mixed {cond, trueRet, [falseRet]} If the first argument evaluates to a true value, the second"
					+ " argument is returned, otherwise the third argument is returned."
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
		public MSVersion since() {
			return MSVersion.V3_0_1;
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

		@SuppressWarnings("checkstyle:constantname")
		private static final String and = new BasicLogic.and().getName();

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> args, FileOptions fileOptions)
				throws ConfigCompileException {
			//Check for too many/few arguments
			if(args.size() < 2) {
				throw new ConfigCompileException("Too few arguments passed to if()", t);
			}
			if(args.size() > 3) {
				throw new ConfigCompileException("if() can only have 3 parameters", t);
			}
			if(args.get(0).isConst()) {
				// We can optimize this one way or the other, since the condition is const
				if(ArgumentValidation.getBoolean(args.get(0).getData(), t)) {
					// It's true, return the true condition
					return args.get(1);
				} else // If there are three args, return the else condition, otherwise,
				// have it entirely remove us from the parse tree.
				if(args.size() == 3) {
					return args.get(2);
				} else {
					return Optimizable.REMOVE_ME;
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
			if(args.get(1).getData() instanceof CFunction
					&& args.get(1).getData().val().equals("if") && args.size() == 2) {
				ParseTree _if = args.get(1);
				if(_if.getChildren().size() == 2) {
					// All the conditions are met, move this up
					ParseTree myCondition = args.get(0);
					ParseTree theirCondition = _if.getChildAt(0);
					ParseTree theirCode = _if.getChildAt(1);
					ParseTree andClause = new ParseTree(new CFunction(and, t), fileOptions);
					// If it's already an and(), just tack the other condition on
					if(myCondition.getData() instanceof CFunction && myCondition.getData().val().equals(and)) {
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
				new ExampleScript("With braces, false condition",
						"msg('Start')\nif(false){\n\tmsg('This will not show')\n}\nmsg('Finish')")};
		}

		@Override
		public List<Boolean> isBranch(List<ParseTree> children) {
			List<Boolean> branches = new ArrayList<>(children.size());
			// Only the first child is not a branch. Everything else is a branch.
			branches.add(false);
			for(int i = 1; i < children.size(); i++) {
				branches.add(true);
			}
			return branches;
		}

		@Override
		public List<Boolean> isScope(List<ParseTree> children) {
			// It's the exact same logic as the branches
			return isBranch(children);
		}

	}

	@api(environments = {GlobalEnv.class})
	public static class ifelse extends AbstractFunction implements Optimizable, BranchStatement, VariableScope {

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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInsufficientArgumentsException.class};
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
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return CNull.NULL;
		}

		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			if(nodes.length < 2) {
				throw new CREInsufficientArgumentsException("ifelse expects at least 2 arguments", t);
			}
			for(int i = 0; i <= nodes.length - 2; i += 2) {
				ParseTree statement = nodes[i];
				ParseTree code = nodes[i + 1];
				Mixed evalStatement = parent.seval(statement, env);
				if(evalStatement instanceof CIdentifier) {
					evalStatement = parent.seval(((CIdentifier) evalStatement).contained(), env);
				}
				if(ArgumentValidation.getBooleanish(evalStatement, t)) {
					Mixed ret = env.getEnv(GlobalEnv.class).GetScript().eval(code, env);
					return ret;
				}
			}
			if(nodes.length % 2 == 1) {
				Mixed ret = env.getEnv(GlobalEnv.class).GetScript().seval(nodes[nodes.length - 1], env);
				if(ret instanceof CIdentifier) {
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

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			// TODO: Redo this optimization.
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Functional usage", "ifelse(false, msg('This is false'), true, msg('This is true'))"),
				new ExampleScript("With braces",
						"if(false){\n\tmsg('This is false')\n} else {\n\tmsg('This is true')\n}"),
				new ExampleScript("With braces, with else if", "if(false){\n\tmsg('This will not show')\n}"
						+ " else if(false){\n\n\tmsg('This will not show')\n} else {\n\tmsg('This will show')\n}")};
		}

		@Override
		public List<Boolean> isBranch(List<ParseTree> children) {
			List<Boolean> branches = new ArrayList<>(children.size());
			// Only the first child is not a branch. Everything else is a branch.
			branches.add(false);
			for(int i = 1; i < children.size(); i++) {
				branches.add(true);
			}
			return branches;
		}

		@Override
		public List<Boolean> isScope(List<ParseTree> children) {
			// It's the exact same logic as the branches
			return isBranch(children);
		}
	}

	@api
	@breakable
	public static class _switch extends AbstractFunction implements Optimizable, BranchStatement, VariableScope {

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
			return "mixed {value, [equals, code]..., [defaultCode]} Provides a switch statement. If none of the"
					+ " conditions match, and no default is provided, void is returned."
					+ " See the documentation on [[Logic|Logic]] for more information. ----"
					+ " In addition, slices may be used to indicate ranges of integers that should trigger the"
					+ " specified case. Slices embedded in an array are fine as well. Switch statements also support"
					+ " brace/case/default syntax, as in most languages, althrough unlike most languages, fallthrough"
					+ " isn't supported. Breaking with break() isn't required, but recommended. A number greater than 1"
					+ " may be sent to break, and breaking out of the switch will consume a \"break counter\" and the"
					+ " break will continue up the chain. If you do use break(), the return value of switch is ignored."
					+ " See the examples for usage of brace/case/default syntax, which is highly recommended.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInsufficientArgumentsException.class};
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
		public Version since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return CNull.NULL;
		}

		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			Mixed value = parent.seval(nodes[0], env);
			BasicLogic.equals equals = new BasicLogic.equals();
			try {
				for(int i = 1; i <= nodes.length - 2; i += 2) {
					ParseTree statement = nodes[i];
					ParseTree code = nodes[i + 1];
					Mixed evalStatement = parent.seval(statement, env);
					if(evalStatement instanceof CSlice) { //Can do more optimal handling for this Array subclass
						long rangeLeft = ((CSlice) evalStatement).getStart();
						long rangeRight = ((CSlice) evalStatement).getFinish();
						if(value.isInstanceOf(CInt.TYPE)) {
							long v = Static.getInt(value, t);
							if((rangeLeft < rangeRight && v >= rangeLeft && v <= rangeRight)
									|| (rangeLeft > rangeRight && v >= rangeRight && v <= rangeLeft)
									|| (rangeLeft == rangeRight && v == rangeLeft)) {
								return parent.seval(code, env);
							}
						}
					} else if(evalStatement.isInstanceOf(CArray.TYPE)) {
						for(String index : ((CArray) evalStatement).stringKeySet()) {
							Mixed inner = ((CArray) evalStatement).get(index, t);
							if(inner instanceof CSlice) {
								long rangeLeft = ((CSlice) inner).getStart();
								long rangeRight = ((CSlice) inner).getFinish();
								if(value.isInstanceOf(CInt.TYPE)) {
									long v = Static.getInt(value, t);
									if((rangeLeft < rangeRight && v >= rangeLeft && v <= rangeRight)
											|| (rangeLeft > rangeRight && v >= rangeRight && v <= rangeLeft)
											|| (rangeLeft == rangeRight && v == rangeLeft)) {
										return parent.seval(code, env);
									}
								}
							} else if(equals.exec(t, env, value, inner).getBoolean()) {
								return parent.seval(code, env);
							}
						}
					} else if(equals.exec(t, env, value, evalStatement).getBoolean()) {
						return parent.seval(code, env);
					}
				}
				if(nodes.length % 2 == 0) {
					return parent.seval(nodes[nodes.length - 1], env);
				}
			} catch (LoopBreakException ex) {
				//Ignored, unless the value passed in is greater than 1, in which case
				//we rethrow.
				if(ex.getTimes() > 1) {
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
				new ExampleScript("With braces/case/default", "switch('theValue'){\n"
				+ "\tcase 'notTheValue':\n"
				+ "\t\tmsg('Nope')\n"
				+ "\t\tbreak();\n"
				+ "\tcase 'theValue':\n"
				+ "\t\tmsg('Success')\n"
				+ "\t\tbreak();\n"
				+ "}"),
				new ExampleScript("With braces/case/default. Note the lack of fallthrough, even without a break(),"
				+ " except where two cases are directly back to back.",
				"@a = 5\nswitch(@a){\n"
				+ "\tcase 1:\n"
				+ "\tcase 2:\n"
				+ "\t\tmsg('1 or 2');\n"
				+ "\tcase 3..4:\n"
				+ "\t\tmsg('3 or 4');\n"
				+ "\t\tbreak(); // This is optional, as it would break here anyways, but is recommended.\n"
				+ "\tcase 5..6:\n"
				+ "\tcase 8:\n"
				+ "\t\tmsg('5, 6, or 8')\n"
				+ "\tdefault:\n"
				+ "\t\tmsg('Any other value'); # A default is optional\n"
				+ "}\n"),
				new ExampleScript("With default condition", "switch('noMatch'){\n"
				+ "\tcase 'notIt1':\n"
				+ "\t\tmsg('Nope');\n"
				+ "\t\tbreak();\n"
				+ "\tcase 'notIt2':\n"
				+ "\t\tmsg('Nope');\n"
				+ "\t\tbreak();\n"
				+ "\tdefault:\n"
				+ "\t\tmsg('Success');\n"
				+ "\t\tbreak();\n"
				+ "}"),
				new ExampleScript("With slices", "switch(5){\n"
				+ "\tcase 1..2:\n"
				+ "\t\tmsg('First');\n"
				+ "\t\tbreak();\n"
				+ "\tcase 3..5:\n"
				+ "\t\tmsg('Second');\n"
				+ "\t\tbreak();\n"
				+ "\tcase 6..8:\n"
				+ "\t\tmsg('Third');\n"
				+ "\t\tbreak();\n"
				+ "}"),
				new ExampleScript("Functional usage", "switch('theValue',\n"
				+ "\t'notTheValue',\n"
				+ "\t\tmsg('Nope'),\n"
				+ "\t'theValue',\n"
				+ "\t\tmsg('Success')\n"
				+ ")"),
				new ExampleScript("With multiple matches using an array", "switch('string',\n"
				+ "\tarray('value1', 'value2', 'string'),\n"
				+ "\t\tmsg('Match'),\n"
				+ "\t'value3',\n"
				+ "\t\tmsg('No match')\n"
				+ ")"),
				new ExampleScript("With slices in an array", "switch(5,\n"
				+ "\tarray(1..2, 3..5),\n"
				+ "\t\tmsg('First'),\n"
				+ "\t6..8,\n"
				+ "\t\tmsg('Second')\n"
				+ ")")};
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() > 1 && children.get(1).getData() instanceof CFunction
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
				for(int i = 0; i < c.size(); i++) {
					//Need up to a 2 lookahead
					ParseTree c1 = c.get(i);
					ParseTree c2 = null;
					if(i + 1 < c.size()) {
						c2 = c.get(i + 1);
					}
					if(CKeyword.isKeyword(c1, "case")) {
						//If this is a case AND the next one is
						//a label, this is a case.
						if(c2 != null && c2.getData() instanceof CLabel) {
							if(inDefault) {
								//Default must come last
								throw new ConfigCompileException(
										"Unexpected case; the default case must come last.", t);
							}
							if(lastCodeBlock.size() > 0) {
								//Ok, need to push some stuff on to the new children
								newChildren.add(new ParseTree(conditions, c2.getFileOptions()));
								conditions = new CArray(t);
								ParseTree codeBlock = new ParseTree(
										new CFunction(new StringHandling.sconcat().getName(), t), c2.getFileOptions());
								for(ParseTree line : lastCodeBlock) {
									codeBlock.addChild(line);
								}
								if(codeBlock.getChildren().size() == 1) {
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
							conditions.push(((CLabel) c2.getData()).cVal(), t);
							inCase = true;
							i++;
							continue;
						}
					}
					if(c1.getData() instanceof CLabel
							&& CKeyword.isKeyword(((CLabel) c1.getData()).cVal(), "default")) {
						//Default case
						if(lastCodeBlock.size() > 0) {
							//Ok, need to push some stuff on to the new children
							newChildren.add(new ParseTree(conditions, c1.getFileOptions()));
							conditions = new CArray(t);
							ParseTree codeBlock = new ParseTree(
									new CFunction(new StringHandling.sconcat().getName(), t), c1.getFileOptions());
							for(ParseTree line : lastCodeBlock) {
								codeBlock.addChild(line);
							}
							if(codeBlock.getChildren().size() == 1) {
								codeBlock = codeBlock.getChildAt(0);
							}
							newChildren.add(codeBlock);
							lastCodeBlock = new ArrayList<>();
						} else if(conditions.size() > 0) {
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
					if(inCase || inDefault) {
						lastCodeBlock.add(c1);
					}
				}
				if(conditions.size() > 0) {
					newChildren.add(new ParseTree(conditions, children.get(0).getFileOptions()));
				}
				if(lastCodeBlock.size() > 0) {
					ParseTree codeBlock = new ParseTree(new CFunction(new StringHandling.sconcat().getName(), t),
							lastCodeBlock.get(0).getFileOptions());
					for(ParseTree line : lastCodeBlock) {
						codeBlock.addChild(line);
					}
					if(codeBlock.getChildren().size() == 1) {
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
			String alreadyContains = "The switch statement already contains a case for this value,"
					+ " remove the duplicate value";
			final BasicLogic.equals equals = new BasicLogic.equals();
			Set<Mixed> values = new TreeSet<>((Mixed t1, Mixed t2) -> {
				if(equals.exec(Target.UNKNOWN, null, t1, t2).getBoolean()) {
					return 0;
				} else {
					return t1.val().compareTo(t2.val());
				}
			});
			// hasDefaultCase = size % 2 == 0 -> Even number means there is a default.
			final boolean hasDefaultCase = (children.size() & 0b00000001) == 0;
			for(int i = 1; i < children.size(); i += 2) {
				if(hasDefaultCase && i == children.size() - 1) {
					// This is the default case code. Stop checking here.
					break;
				}
				//To standardize the rest of the code (and to optimize), go ahead and resolve array()
				if(children.get(i).getData() instanceof CFunction
						&& new DataHandling.array().getName().equals(children.get(i).getData().val())) {
					CArray data = new CArray(t);
					for(ParseTree child : children.get(i).getChildren()) {
						if(Construct.IsDynamicHelper(child.getData())) {
							throw new ConfigCompileException(notConstant, child.getTarget());
						}
						data.push(child.getData(), t);
					}
					children.set(i, new ParseTree(data, children.get(i).getFileOptions()));
				}
				//Now we validate that the values are constant and non-repeating.
				if(children.get(i).getData().isInstanceOf(CArray.TYPE)) {
					List<Mixed> list = ((CArray) children.get(i).getData()).asList();
					for(Mixed c : list) {
						if(c instanceof CSlice) {
							for(Mixed cc : ((CSlice) c).asList()) {
								if(values.contains(cc)) {
									throw new ConfigCompileException(alreadyContains, cc.getTarget());
								}
								values.add(cc);
							}
						} else {
							if(Construct.IsDynamicHelper(c)) {
								throw new ConfigCompileException(notConstant, c.getTarget());
							}
							if(values.contains(c)) {
								throw new ConfigCompileException(alreadyContains, c.getTarget());
							}
							values.add(c);
						}
					}
				} else {
					Mixed c = children.get(i).getData();
					if(Construct.IsDynamicHelper(c)) {
						throw new ConfigCompileException(notConstant, c.getTarget());
					}
					if(values.contains(c)) {
						throw new ConfigCompileException(alreadyContains, c.getTarget());
					}
					values.add(c);
				}
			}

			if((children.size() > 3 || (children.size() > 1 && children.get(1).getData().isInstanceOf(CArray.TYPE)))
					//No point in doing this optimization if there are only 3 args and the case is flat.
					//Also, doing this check prevents an inifinite loop during optimization.
					&& (children.size() > 0 && !Construct.IsDynamicHelper(children.get(0).getData()))) {
				ParseTree toReturn = null;
				//The item passed in is constant (or has otherwise been made constant)
				//so we can go ahead and condense this down to the single code path
				//in the switch.
				for(int i = 1; i < children.size(); i += 2) {
					Mixed data = children.get(i).getData();

					if(!(data.isInstanceOf(CArray.TYPE)) || data instanceof CSlice) {
						//Put it in an array to make the rest of this parsing easier.
						data = new CArray(t);
						((CArray) data).push(children.get(i).getData(), t);
					}
					for(Mixed value : ((CArray) data).asList()) {
						if(value instanceof CSlice) {
							long rangeLeft = ((CSlice) value).getStart();
							long rangeRight = ((CSlice) value).getFinish();
							if(children.get(0).getData().isInstanceOf(CInt.TYPE)) {
								long v = Static.getInt(children.get(0).getData(), t);
								if((rangeLeft < rangeRight && v >= rangeLeft && v <= rangeRight)
										|| (rangeLeft > rangeRight && v >= rangeRight && v <= rangeLeft)
										|| (rangeLeft == rangeRight && v == rangeLeft)) {
									toReturn = children.get(i + 1);
									break;
								}
							}
						} else if(equals.exec(t, null, children.get(0).getData(), value).getBoolean()) {
							toReturn = children.get(i + 1);
							break;
						}
					}
				}
				//None of the values match. Return the default case, if it exists, or remove the switch entirely
				//if it doesn't.
				if(toReturn == null) {
					if(children.size() % 2 == 0) {
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

		@Override
		public List<Boolean> isBranch(List<ParseTree> children) {
			List<Boolean> branches = new ArrayList<>(children.size());
			branches.add(false);
			if(children.size() == 2) {
				branches.add(true);
			} else {
				for(int i = 1; i < children.size() - 1; i += 2) {
					branches.add(false);
					branches.add(true);
				}
				if(children.size() % 2 == 0) {
					branches.add(true);
				}
			}
			return branches;
		}

		@Override
		public List<Boolean> isScope(List<ParseTree> children) {
			// It's the exact same logic as the branches
			return isBranch(children);
		}
	}

	@api
	public static class switch_ic extends _switch implements Optimizable, BranchStatement, VariableScope {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			throw new Error();
		}

		@Override
		public String getName() {
			return "switch_ic";
		}

		@Override
		public String docs() {
			return "mixed {value, [equals, code]..., [defaultCode]} Provides a case insensitive switch statement, for"
					+ " switching over strings. This works by compiler transformations, transforming this into a normal"
					+ " switch statement, with each case lowercased, and the input to the switch wrapped in to_lower."
					+ " The case statements must be strings, however, which is the main difference between this method"
					+ " and the normal switch statement. The lowercasing is done with the system's default locale.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs, List<ParseTree> children,
				FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			// Allow the normal switch optimization to run, which does the heavy lifting of getting the code into the
			// functional format, which becomes easier for us to parse.
			ParseTree switchTree = super.optimizeDynamic(t, env, envs, children, fileOptions);
			// Replace the 0th child with to_lower(child)
			ParseTree condition = children.get(0);
			if(!CFunction.IsFunction(condition, StringHandling.to_lower.class)) {
				// Don't re-add it if it's already there
				ParseTree to_lower = new ParseTree(new CFunction(new StringHandling.to_lower().getName(), t),
						fileOptions);
				to_lower.addChild(condition);
				children.set(0, to_lower);
			}
			// Now loop through the children, looking for the case statements. Also ensure each is a string.
			for(int i = 1; i < children.size(); i += 2) {
				ParseTree cseArray = children.get(i);
				CArray newData = new CArray(cseArray.getTarget());
				for(Mixed cse : ((CArray) cseArray.getData()).asList()) {
					if(cse instanceof CString) {
						CString data = (CString) cse;
						newData.push(new CString(data.val().toLowerCase(), data.getTarget()), data.getTarget());
					} else {
						throw new ConfigCompileException(getName() + " can only accept strings in case statements.",
								cse.getTarget());
					}
				}
				cseArray.setData(newData);
			}
			return switchTree;
		}

	}

	@api
	@noboilerplate
	@breakable
	@seealso({com.laytonsmith.tools.docgen.templates.Loops.class,
		com.laytonsmith.tools.docgen.templates.ArrayIteration.class})
	public static class _for extends AbstractFunction implements Optimizable, BranchStatement, VariableScope {

		@Override
		public String getName() {
			return "for";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{4};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) {
			return CVoid.VOID;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			return new forelse(true).execs(t, env, parent, nodes);
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public String docs() {
			return "void {assign, condition, expression1, expression2} Acts as a typical for loop. The assignment is"
					+ " first run. Then, a condition is checked. If that condition is checked and returns true,"
					+ " expression2 is run. After that, expression1 is run. In java syntax, this would be:"
					+ " for(assign; condition; expression1){expression2}. assign must be an ivariable, either a "
					+ "pre defined one, or the results of the assign() function. condition must be a boolean.";
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_0_1;
		}
		//Doesn't matter, run out of state

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "for(assign(@i, 0), @i < 5, @i++,\n\tmsg(@i)\n)"),
				new ExampleScript("With braces", "for(assign(@i, 0), @i < 2, @i++){\n\tmsg(@i)\n}"),
				new ExampleScript("With continue. (See continue() for more examples)",
						"for(assign(@i, 0), @i < 2, @i++){\n"
						+ "\tif(@i == 1, continue())\n"
						+ "\tmsg(@i)\n"
						+ "}")};
		}

		@Override
		public LogLevel profileAt() {
			return LogLevel.WARNING;
		}

		@Override
		public String profileMessageS(List<ParseTree> args) {
			return "Executing function: " + this.getName() + "("
					+ args.get(0).toStringVerbose() + ", " + args.get(1).toStringVerbose()
					+ ", " + args.get(2).toStringVerbose() + ", <code>)";
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			//In for(@i = 0, @i < @x, @i++, ...), the @i++ is more optimally written as ++@i, but
			//it is commonplace to use postfix operations, so if the condition is in fact that simple,
			//let's reverse it.
			boolean isInc;
			try {
				if(children.get(2).getData() instanceof CFunction
						&& ((isInc = children.get(2).getData().val().equals("postinc"))
						|| children.get(2).getData().val().equals("postdec"))
						&& children.get(2).getChildAt(0).getData() instanceof IVariable) {
					ParseTree pre = new ParseTree(
							new CFunction(isInc ? "inc" : "dec", t), children.get(2).getFileOptions());
					pre.addChild(children.get(2).getChildAt(0));
					children.set(2, pre);
				}
			} catch (IndexOutOfBoundsException e) {
				//Just ignore it. It's a compile error, but we'll let the rest of the
				//existing system sort that out.
			}

			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public List<Boolean> isBranch(List<ParseTree> children) {
			List<Boolean> ret = new ArrayList<>();
			ret.add(false);
			ret.add(false);
			ret.add(true);
			ret.add(true);
			return ret;
		}

		@Override
		public List<Boolean> isScope(List<ParseTree> children) {
			List<Boolean> ret = new ArrayList<>();
			ret.add(true);
			ret.add(false);
			ret.add(false);
			ret.add(true);
			return ret;
		}

	}

	@api
	@noboilerplate
	@breakable
	public static class forelse extends AbstractFunction implements BranchStatement, VariableScope {

		public forelse() {
		}

		boolean runAsFor = false;

		forelse(boolean runAsFor) {
			this.runAsFor = runAsFor;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
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
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return null;
		}

		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) throws ConfigRuntimeException {
			ParseTree assign = nodes[0];
			ParseTree condition = nodes[1];
			ParseTree expression = nodes[2];
			ParseTree runnable = nodes[3];
			ParseTree elseCode = null;
			if(!runAsFor) {
				elseCode = nodes[4];
			}
			boolean hasRunOnce = false;

			Mixed counter = parent.eval(assign, env);
			if(!(counter instanceof IVariable)) {
				throw new CRECastException("First parameter of for must be an ivariable", t);
			}
			int _continue = 0;
			while(true) {
				boolean cond = ArgumentValidation.getBoolean(parent.seval(condition, env), t);
				if(cond == false) {
					break;
				}
				hasRunOnce = true;
				if(_continue >= 1) {
					--_continue;
					parent.eval(expression, env);
					continue;
				}
				try {
					parent.eval(runnable, env);
				} catch (LoopBreakException e) {
					int num = e.getTimes();
					if(num > 1) {
						e.setTimes(--num);
						throw e;
					}
					return CVoid.VOID;
				} catch (LoopContinueException e) {
					_continue = e.getTimes() - 1;
					parent.eval(expression, env);
					continue;
				}
				parent.eval(expression, env);
			}
			if(!hasRunOnce && !runAsFor && elseCode != null) {
				parent.eval(elseCode, env);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "forelse";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{5};
		}

		@Override
		public String docs() {
			return "void {assign, condition, expression1, expression2, else} Works like a normal for loop, but if upon"
					+ " checking the condition the first time, it is determined that it is false (that is, NO code"
					+ " loops are going to be run) the else code is run instead. If the loop runs, even once, it will"
					+ " NOT run the else branch. In general, brace syntax and use of for(){ } else { } syntax is"
					+ " preferred, instead of using forelse directly.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public List<Boolean> isBranch(List<ParseTree> children) {
			return Arrays.asList(false, false, true, true, true);
		}

		@Override
		public List<Boolean> isScope(List<ParseTree> children) {
			return Arrays.asList(true, false, false, true, true);
		}

	}

	@api
	@breakable
	@seealso({com.laytonsmith.tools.docgen.templates.Loops.class, ArrayIteration.class})
	public static class foreach extends AbstractFunction implements Optimizable, BranchStatement, VariableScope {

		@Override
		public String getName() {
			return "foreach";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3, 4};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args)
				throws CancelCommandException, ConfigRuntimeException {
			return CVoid.VOID;
		}

		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			if(nodes.length < 3) {
				throw new CREInsufficientArgumentsException("Insufficient arguments passed to " + getName(), t);
			}
			ParseTree array = nodes[0];
			ParseTree key = null;
			int offset = 0;
			if(nodes.length == 4) {
				//Key and value provided
				key = nodes[1];
				offset = 1;
			}
			ParseTree value = nodes[1 + offset];
			ParseTree code = nodes[2 + offset];
			Mixed arr = parent.seval(array, env);
			Mixed ik = null;
			if(key != null) {
				ik = parent.eval(key, env);
				if(!(ik instanceof IVariable)) {
					throw new CRECastException("Parameter 2 of " + getName() + " must be an ivariable", t);
				}
			}
			Mixed iv = parent.eval(value, env);
			if(arr instanceof CSlice) {
				long start = ((CSlice) arr).getStart();
				long finish = ((CSlice) arr).getFinish();
				if(finish < start) {
					arr = new ArrayHandling.range()
							.exec(t, env, new CInt(start, t), new CInt(finish - 1, t), new CInt(-1, t));
				} else {
					arr = new ArrayHandling.range().exec(t, env, new CInt(start, t), new CInt(finish + 1, t));
				}
			}
			if(!(arr instanceof com.laytonsmith.core.natives.interfaces.Iterable)) {
				throw new CRECastException("Parameter 1 of " + getName() + " must be an Iterable data structure", t);
			}
			if(!(iv instanceof IVariable)) {
				throw new CRECastException(
						"Parameter " + (2 + offset) + " of " + getName() + " must be an ivariable", t);
			}
			com.laytonsmith.core.natives.interfaces.Iterable one
				= (com.laytonsmith.core.natives.interfaces.Iterable) arr;
			IVariable kkey = (IVariable) ik;
			IVariable two = (IVariable) iv;
			if(one.isAssociative()) {
				//Iteration of an associative array is much easier, and we have
				//special logic here to decrease the complexity.

				//Clone the set, so changes in the array won't cause changes in
				//the iteration order.
				Set<Mixed> keySet = new LinkedHashSet<>(one.keySet());
				//Continues in an associative array are slightly different, so
				//we have to track this differently. Basically, we skip the
				//next element in the array key set.
				int continues = 0;
				for(Mixed c : keySet) {
					if(continues > 0) {
						//If continues is greater than 0, continue in the loop,
						//however many times necessary to make it 0.
						continues--;
						continue;
					}
					//If the key isn't null, set that in the variable table.
					if(kkey != null) {
						env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(kkey.getDefinedType(),
								kkey.getVariableName(), c, t, env));
					}
					//Set the value in the variable table
					env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(two.getDefinedType(),
							two.getVariableName(), one.get(c.val(), t), t, env));
					try {
						//Execute the code
						parent.eval(code, env);
						//And handle any break/continues.
					} catch (LoopBreakException e) {
						int num = e.getTimes();
						if(num > 1) {
							e.setTimes(--num);
							throw e;
						}
						return CVoid.VOID;
					} catch (LoopContinueException e) {
						// In associative arrays, (unlike with normal arrays) we need to decrement it by one, because
						// the nature of the normal array is such that the counter is handled manually by our code.
						// Because we are letting java handle our code though, this run actually counts as one run.
						continues += e.getTimes() - 1;
					}
				}
				return CVoid.VOID;
			} else {
				//It's not associative, so we have more complex handling. We will create an ArrayAccessIterator,
				//and store that in the environment. As the array is iterated, underlying changes in the array
				//will be reflected in the object, and we will adjust as necessary. The reason we use this mechanism
				//is to avoid cloning the array, and iterating that. Arrays may be extremely large, and cloning the
				//entire array is wasteful in that case. We are essentially tracking deltas this way, which prevents
				//memory usage from getting out of hand.
				Iterator iterator = new Iterator(one);
				List<Iterator> arrayAccessList = env.getEnv(GlobalEnv.class).GetArrayAccessIterators();
				try {
					arrayAccessList.add(iterator);
					int continues = 0;
					while(true) {
						int current = iterator.getCurrent();
						if(continues > 0) {
							//We have some continues to handle. Blacklisted
							//values don't count for the continuing count, so
							//we have to consider that when counting.
							iterator.incrementCurrent();
							if(iterator.isBlacklisted(current)) {
								continue;
							} else {
								--continues;
								continue;
							}
						}
						if(current >= one.size()) {
							//Done with the iterations.
							break;
						}
						//If the item is blacklisted, we skip it.
						if(!iterator.isBlacklisted(current)) {
							if(kkey != null) {
								env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(kkey.getDefinedType(),
										kkey.getVariableName(), new CInt(current, t), t, env));
							}
							env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(two.getDefinedType(),
									two.getVariableName(), one.get(current, t), t, env));
							try {
								parent.eval(code, env);
							} catch (LoopBreakException e) {
								int num = e.getTimes();
								if(num > 1) {
									e.setTimes(--num);
									throw e;
								}
								return CVoid.VOID;
							} catch (LoopContinueException e) {
								continues += e.getTimes();
								continue;
							}
						}
						iterator.incrementCurrent();
					}
				} finally {
					arrayAccessList.remove(iterator);
				}
			}
			return CVoid.VOID;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRERangeException.class};
		}

		@Override
		public String docs() {
			return "void {array, [key], ivar, code} Walks through array, setting ivar equal to each element in the"
					+ " array, then running code."
					+ " In addition, foreach(1..4, @i, code()) is also valid, setting @i to 1, 2, 3, 4 each time."
					+ " The same syntax is valid as in an array slice."
					+ " If key is set (it must be an ivariable) then the index of each iteration will be set to that."
					+ " See the examples for a demonstration. ---- "
					+ " Enhanced syntax may also be used in foreach, using the \"in\", \"as\" and \"else\" keywords."
					+ " See the examples for examples of each structure. Using these keywords makes the structure of"
					+ " the foreach read much better. For instance, with foreach(@value in @array){ } the code very"
					+ " literally reads \"for each value in array\", making ascertaining the behavior of the loop"
					+ " easier. The \"as\" keyword reads less plainly, and so is not recommended for use, but is"
					+ " allowed. Note that the array and value are reversed with the \"as\" keyword. An \"else\" block"
					+ " may be used after the foreach, which will only run if the array provided is empty, that is, the"
					+ " loop code would never run. This provides a good way to provide \"default\" handling."
					+ " Array modifications while iterating are supported, and are well defined."
					+ " See [[Array_iteration|the page documenting array iterations]]"
					+ " for full details.";
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_0_1;
		}
		//Doesn't matter, runs out of state anyways

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Using \"in\" keyword", "@array = array(1, 2, 3);\n"
				+ "foreach(@value in @array){\n"
				+ "\tmsg(@value);\n"
				+ "}"),
				new ExampleScript("Using \"in\" keyword, with a key", "@array = array(1, 2, 3);\n"
				+ "foreach(@key: @value in @array){\n"
				+ "\tmsg(@key . ': ' . @value);\n"
				+ "}"),
				new ExampleScript("Using \"as\" keyword", "@array = array(1, 2, 3);\n"
				+ "foreach(@array as @value){\n"
				+ "\tmsg(@value);\n"
				+ "}"),
				/* This is actually borked in real code, so it needs to be fixed.
				 * In the meantime, whatever, just remove the example.
				new ExampleScript("Using \"as\" keyword, with a key", "@array = array(1, 2, 3);\n"
				+ "foreach(@array as @key: @value){\n"
				+ "\tmsg(@key . ': ' . @value);\n"
				+ "}"),
				*/
				new ExampleScript("With else clause", "@array = array() # Note empty array\n"
				+ "foreach(@value in @array){\n"
				+ "\tmsg(@value);\n"
				+ "} else {\n"
				+ "\tmsg('No values were in the array');\n"
				+ "}"),
				new ExampleScript("Basic functional usage", "assign(@array, array(1, 2, 3))\nforeach(@array, @i,\n\tmsg(@i)\n)"),
				new ExampleScript("With braces", "assign(@array, array(1, 2, 3))\nforeach(@array, @i){\n\tmsg(@i)\n}"),
				new ExampleScript("With a slice", "foreach(1..3, @i){\n\tmsg(@i)\n}"),
				new ExampleScript("With a slice, counting down", "foreach(3..1, @i){\n\tmsg(@i)\n}"),
				new ExampleScript("With array keys", "@array = array('one': 1, 'two': 2)\nforeach(@array, @key, @value){\n\tmsg(@key.':'.@value)\n}")};
		}

		@Override
		public LogLevel profileAt() {
			return LogLevel.WARNING;
		}

		@Override
		public String profileMessageS(List<ParseTree> args) {
			return "Executing function: " + this.getName() + "("
					+ args.get(0).toStringVerbose() + ", " + args.get(1).toStringVerbose()
					+ ", <code>)";
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		private static final String CENTRY = new Compiler.centry().getName();
		private static final String ASSIGN = new DataHandling.assign().getName();
		private static final String SCONCAT = new StringHandling.sconcat().getName();
		private static final String IN = new InKeyword().getKeywordName();

		private boolean isFunction(ParseTree node, String function) {
			return node.getData() instanceof CFunction && node.getData().val().equals(function);
		}

		private boolean isKeyword(ParseTree node, String keyword) {
			return node.getData() instanceof CKeyword && node.getData().val().equals(keyword);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() < 2) {
				throw new ConfigCompileException("Invalid number of arguments passed to " + getName(), t);
			}
			if(isFunction(children.get(0), CENTRY)) {
				// This is what "@key: @value in @array" looks like initially.
				// We'll refactor this so the next segment can take over properly.
				ParseTree sconcat = new ParseTree(
						new CFunction(new StringHandling.sconcat().getName(), t), fileOptions);
				sconcat.addChild(children.get(0).getChildAt(0));
				for(int i = 0; i < children.get(0).getChildAt(1).numberOfChildren(); i++) {
					sconcat.addChild(children.get(0).getChildAt(1).getChildAt(i));
				}
				children.set(0, sconcat);
			}
			if(children.get(0).getData() instanceof CFunction
					&& children.get(0).getData().val().equals(new StringHandling.sconcat().getName())) {
				// We may be looking at a "@value in @array" or "@array as @value" type
				// structure, so we need to re-arrange this into the standard format.
				ParseTree array = null;
				ParseTree key = null;
				ParseTree value = null;
				List<ParseTree> c = children.get(0).getChildren();
				if(c.size() == 3) {
					// No key specified
					switch(c.get(1).getData().val()) {
						case "in":
							// @value in @array
							value = c.get(0);
							array = c.get(2);
							break;
						case "as":
							// @array as @value
							value = c.get(2);
							array = c.get(0);
							break;
					}
				} else if(c.size() == 4) {
					if("in".equals(c.get(2).getData().val())) {
						// @key: @value in @array
						key = c.get(0);
						value = c.get(1);
						array = c.get(3);
					} else if("as".equals(c.get(1).getData().val())) {
						// @array as @key: @value
						array = c.get(0);
						key = c.get(2);
						value = c.get(3);
					}
				}
				if(array == null) {
					throw new ConfigCompileException("Invalid argument format passed to " + getName(), t);
				}
				if(key != null && key.getData() instanceof CLabel) {
					if(!(((CLabel) key.getData()).cVal() instanceof IVariable)
							&& !(((CLabel) key.getData()).cVal() instanceof CFunction
							&& ((CLabel) key.getData()).cVal().val().equals(ASSIGN))) {
						throw new ConfigCompileException("Expected a variable for key, but \""
								+ key.getData().val() + "\" was found", t);
					}
					key.setData(((CLabel) key.getData()).cVal());
				}
				// Now set up the new tree, and return that. Since foreachelse overrides us, we
				// need to accept all the arguments after the first, and put those in.
				List<ParseTree> newChildren = new ArrayList<>();
				newChildren.add(array);
				if(key != null) {
					newChildren.add(key);
				}
				newChildren.add(value);
				for(int i = 1; i < children.size(); i++) {
					newChildren.add(children.get(i));
				}
				children.clear();
				children.addAll(newChildren);
				// Change foreach(){ ... } else { ... } to a foreachelse.
				if(children.get(children.size() - 1).getData() instanceof CFunction
						&& children.get(children.size() - 1).getData().val().equals("else")) {
					ParseTree foreachelse = new ParseTree(new CFunction(new foreachelse().getName(), t), fileOptions);
					children.set(children.size() - 1, children.get(children.size() - 1).getChildAt(0));
					foreachelse.setChildren(children);
					return foreachelse;
				}
			}
			return null;
		}

		@Override
		public List<Boolean> isBranch(List<ParseTree> children) {
			List<Boolean> ret = new ArrayList<>(children.size());
			ret.add(false);
			if(children.size() == 4) {
				// 3 and 4 arguments are the only actually possible ones here
				ret.add(false);
			}
			ret.add(false);
			ret.add(true);
			return ret;
		}

		@Override
		public List<Boolean> isScope(List<ParseTree> children) {
			List<Boolean> ret = new ArrayList<>(children.size());
			for(ParseTree c : children) {
				ret.add(true);
			}
			return ret;
		}

	}

	@api
	@noboilerplate
	@breakable
	@seealso({foreach.class, Loops.class, ArrayIteration.class})
	public static class foreachelse extends foreach {

		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			ParseTree array = nodes[0];
			//The last one
			ParseTree elseCode = nodes[nodes.length - 1];

			Mixed data = parent.seval(array, env);

			if(!(data.isInstanceOf(CArray.TYPE)) && !(data instanceof CSlice)) {
				throw new CRECastException(getName() + " expects an array for parameter 1", t);
			}

			if(((CArray) data).isEmpty()) {
				parent.eval(elseCode, env);
			} else {
				ParseTree pass[] = new ParseTree[nodes.length - 1];
				System.arraycopy(nodes, 0, pass, 0, nodes.length - 1);
				nodes[0] = new ParseTree(data, null);
				return super.execs(t, env, parent, pass);
			}

			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "foreachelse";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{4, 5};
		}

		@Override
		public String docs() {
			return "void {array, [key], ivar, code, else} Works like a foreach, except if the array is empty, the else"
					+ " code runs instead. That is, if the code would not run at all, the else condition would."
					+ " In general, brace syntax and use of foreach(){ } else { } syntax is preferred, instead of"
					+ " using foreachelse directly.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage, with the else code not running",
				"@array = array(1, 2, 3)\n"
				+ "foreachelse(@array, @val,\n"
				+ "    msg(@val)\n"
				+ ", #else \n"
				+ "    msg('No values in the array')\n"
				+ ")"),
				new ExampleScript("Empty array, so else block running",
				"@array = array()\n"
				+ "foreachelse(@array, @val,\n"
				+ "    msg(@val)\n"
				+ ", #else \n"
				+ "    msg('No values in the array')\n"
				+ ")")};
		}

		@Override
		public List<Boolean> isBranch(List<ParseTree> children) {
			List<Boolean> ret = new ArrayList<>(children.size());
			ret.add(false);
			if(children.size() == 5) {
				// 4 and 5 arguments are the only actually possible ones here
				ret.add(false);
			}
			ret.add(false);
			ret.add(true);
			ret.add(true);
			return ret;
		}

	}

	@api
	@noboilerplate
	@breakable
	@seealso({com.laytonsmith.tools.docgen.templates.Loops.class})
	public static class _while extends AbstractFunction implements BranchStatement, VariableScope {

		@Override
		public String getName() {
			return "while";
		}

		@Override
		public String docs() {
			return "void {condition, [code]} While the condition is true, the code is executed. break and continue work"
					+ " inside a dowhile, but continuing more than once is pointless, since the loop isn't inherently"
					+ " keeping track of any counters anyways. Breaking multiple times still works however.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
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
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			try {
				while(ArgumentValidation.getBoolean(parent.seval(nodes[0], env), t)) {
					//We allow while(thing()); to be done. This makes certain
					//types of coding styles possible.
					if(nodes.length > 1) {
						try {
							parent.eval(nodes[1], env);
						} catch (LoopContinueException e) {
							//ok.
						}
					}
				}
			} catch (LoopBreakException e) {
				if(e.getTimes() > 1) {
					throw new LoopBreakException(e.getTimes() - 1, t);
				}
			}
			return CVoid.VOID;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return CNull.NULL;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "assign(@i, 5)\nwhile(@i > 0,\n"
				+ "\tmsg(@i)\n"
				+ "\t@i--\n"
				+ ")"),
				new ExampleScript("With a break", "assign(@i, 0)\nwhile(true,\n"
				+ "\tmsg(@i)\n"
				+ "\t@i++\n"
				+ "\tif(@i > 5, break())\n"
				+ ")")};
		}

		@Override
		public LogLevel profileAt() {
			return LogLevel.WARNING;
		}

		@Override
		public String profileMessageS(List<ParseTree> args) {
			return "Executing function: " + this.getName() + "("
					+ args.get(0).toStringVerbose() + ", <code>)";
		}

		@Override
		public List<Boolean> isBranch(List<ParseTree> children) {
			List<Boolean> ret = new ArrayList<>();
			ret.add(false);
			ret.add(true);
			return ret;
		}

		@Override
		public List<Boolean> isScope(List<ParseTree> children) {
			return isBranch(children);
		}

	}

	@api
	@noboilerplate
	@breakable
	@seealso({com.laytonsmith.tools.docgen.templates.Loops.class})
	public static class _dowhile extends AbstractFunction implements BranchStatement, VariableScope {

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
			return CNull.NULL;
		}

		@Override
		public String getName() {
			return "dowhile";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {code, condition} Like while, but always runs the code at least once. The condition is checked"
					+ " after each run of the code, and if it is true, the code is run again. break and continue work"
					+ " inside a dowhile, but continuing more than once is pointless, since the loop isn't inherently"
					+ " keeping track of any counters anyways. Breaking multiple times still works however. In general,"
					+ " using brace syntax is preferred: do { code(); } while(@condition); instead of using dowhile()"
					+ " directly.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			try {
				do {
					try {
						parent.eval(nodes[0], env);
					} catch (LoopContinueException e) {
						//ok. No matter how many times it tells us to continue, we're only going to continue once.
					}
				} while(ArgumentValidation.getBoolean(parent.seval(nodes[1], env), t));
			} catch (LoopBreakException e) {
				if(e.getTimes() > 1) {
					throw new LoopBreakException(e.getTimes() - 1, t);
				}
			}
			return CVoid.VOID;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "do {\n"
				+ "\tmsg('This will only run once');\n"
				+ "} while(false);"),
				new ExampleScript("Pure functional usage", "dowhile(\n"
				+ "\tmsg('This will only run once')\n"
				+ ", #while\n"
				+ "false)")
			};
		}

		@Override
		public LogLevel profileAt() {
			return LogLevel.WARNING;
		}

		@Override
		public String profileMessageS(List<ParseTree> args) {
			return "Executing function: " + this.getName() + "(<code>, "
					+ args.get(1).toStringVerbose() + ")";
		}

		@Override
		public List<Boolean> isBranch(List<ParseTree> children) {
			List<Boolean> ret = new ArrayList<>(2);
			ret.add(true);
			ret.add(false);
			return ret;
		}

		@Override
		public List<Boolean> isScope(List<ParseTree> children) {
			return isBranch(children);
		}
	}

	@api
	public static class _break extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "break";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "nothing {[int]} Stops the current loop. If int is specified, and is greater than 1, the break"
					+ " travels that many loops up. So, if you had a loop embedded in a loop, and you wanted to break"
					+ " in both loops, you would call break(2). If this function is called outside a loop (or the"
					+ " number specified would cause the break to travel up further than any loops are defined), the"
					+ " function will fail. If no argument is specified, it is the same as calling break(1)."
					+ " This function has special compilation rules. The break number must not be dynamic,"
					+ " or a compile error will occur. An integer must be hard coded into the function.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args)
				throws CancelCommandException, ConfigRuntimeException {
			int num = 1;
			if(args.length == 1) {
				num = Static.getInt32(args[0], t);
			}
			throw new LoopBreakException(num, t);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "for(assign(@i, 0), @i < 1000, @i++,\n"
				+ "\tfor(assign(@j, 0), @j < 1000, @j++,\n"
				+ "\t\tmsg('This will only display once')\n"
				+ "\t\tbreak(2)\n"
				+ "\t)"
				+ ")"),
				new ExampleScript("Invalid number", "for(assign(@i, 0), @i < 1000, @i++,\n"
				+ "\tfor(assign(@j, 0), @j < 1000, @j++,\n"
				+ "\t\tbreak(3) #There are only 2 loops to break out of\n"
				+ "\t)"
				+ ")", true)};
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() == 1) {
				if(children.get(0).isDynamic()) {
					//This is absolutely a bad design, if there is a variable here
					//in the break. Due to optimization, this is a compile error.
					throw new ConfigCompileException("The parameter sent to break() should"
							+ " be hard coded, and should not be dynamically determinable, since this is always a sign"
							+ " of loose code flow, which should be avoided.", t);
				}
				if(!(children.get(0).getData().isInstanceOf(CInt.TYPE))) {
					throw new ConfigCompileException("break() only accepts integer values.", t);
				}
			}
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC
			//, OptimizationOption.TERMINAL This can't be added yet, because of things like switch, where code
			//branches aren't considered correctly.
			);
		}

	}

	@api
	public static class _continue extends AbstractFunction {

		@Override
		public String getName() {
			return "continue";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "void {[int]} Skips the rest of the code in this loop, and starts the loop over, with it continuing"
					+ " at the next index. If this function is called outside of a loop, the command will fail."
					+ " If int is set, it will skip 'int' repetitions. If no argument is specified, 1 is used.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args)
				throws CancelCommandException, ConfigRuntimeException {
			int num = 1;
			if(args.length == 1) {
				num = Static.getInt32(args[0], t);
			}
			throw new LoopContinueException(num, t);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "for(assign(@i, 0), @i < 5, @i++){\n"
				+ "\tif(@i == 2, continue())\n"
				+ "\tmsg(@i)\n"
				+ "}"),
				new ExampleScript("Argument specified", "for(assign(@i, 0), @i < 5, @i++){\n"
				+ "\tif(@i == 2, continue(2))\n"
				+ "\tmsg(@i)\n"
				+ "}")};
		}
	}

	@api
	public static class _return extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "return";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "nothing {mixed} Returns the specified value from this procedure."
					+ " It cannot be called outside a procedure.";
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
		public MSVersion since() {
			return MSVersion.V3_2_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.TERMINAL
			);
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			Mixed ret = (args.length == 1 ? args[0] : CVoid.VOID);
			throw new FunctionReturnException(ret, t);
		}
	}

	@api
	public static class call_proc extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "call_proc";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "mixed {proc_name, [var1...]} Dynamically calls a user defined procedure. call_proc(_myProc, 'var1')"
					+ " is the equivalent of _myProc('var1'), except you could dynamically build the procedure name if"
					+ " need be. This is useful for dynamic coding, however, closures work best for callbacks."
					+ " Throws an InvalidProcedureException if the procedure isn't defined. If you are hardcoding the"
					+ " first parameter, a warning will be issued, because it is much more efficient and safe to"
					+ " directly use a procedure if you know what its name is beforehand.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidProcedureException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_2_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			if(args.length < 1) {
				throw new CREInsufficientArgumentsException("Expecting at least one argument to " + getName(), t);
			}
			Procedure proc = env.getEnv(GlobalEnv.class).GetProcs().get(args[0].val());
			if(proc != null) {
				List<Mixed> vars = new ArrayList<>(Arrays.asList(args));
				vars.remove(0);
				return proc.execute(vars, env, t);
			}
			throw new CREInvalidProcedureException("Unknown procedure \"" + args[0].val() + "\"", t);
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
			if(children.size() < 1) {
				throw new CREInsufficientArgumentsException("Expecting at least one argument to " + getName(), t);
			}
			if(children.get(0).isConst()) {
				env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions,
						new CompilerWarning("Hardcoding procedure name in "
						+ getName() + ", which is inefficient. Consider calling the procedure directly if the"
						+ " procedure name is known at compile time.", t,
								FileOptions.SuppressWarning.HardcodedDynamicParameter));
			}
			return null;
		}

	}

	@api
	public static class call_proc_array extends call_proc {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			CArray ca = Static.getArray(args[1], t);
			if(ca.inAssociativeMode()) {
				throw new CRECastException("Expected the array passed to " + getName() + " to be non-associative.", t);
			}
			Mixed[] args2 = new Mixed[(int) ca.size() + 1];
			args2[0] = args[0];
			for(int i = 1; i < args2.length; i++) {
				args2[i] = ca.get(i - 1, t);
			}
			return super.exec(t, environment, args2);
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidProcedureException.class, CRECastException.class};
		}

		@Override
		public String getName() {
			return "call_proc_array";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "mixed {proc_name, array} Works like call_proc, but allows for variable or unknown number of"
					+ " arguments to be passed to a proc. The array parameter is \"flattened\", and call_proc is"
					+ " essentially called. If the array is associative, an exception is thrown.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			//If they hardcode the name, that's fine, because the variables may just be the only thing that's variable.
			return null;
		}

	}

	@api
	@noboilerplate
	public static class die extends AbstractFunction implements Optimizable {

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException {
			if(args.length == 0) {
				throw new CancelCommandException("", t);
			}
			StringBuilder b = new StringBuilder();
			for(Mixed arg : args) {
				b.append(arg.val());
			}
			try {
				// TODO: References to this environment should be removed, in favor of an exit/die handler interface
				if(env.hasEnv(CommandHelperEnvironment.class)) {
					Static.SendMessage(env.getEnv(CommandHelperEnvironment.class).GetCommandSender(), b.toString(), t);
				} else {
					String mes = Static.MCToANSIColors(b.toString());
					if(mes.contains("\033")) {
						//We have terminal colors, we need to reset them at the end
						mes += TermColors.reset();
					}
					StreamUtils.GetSystemOut().println(mes);
				}
			} finally {
				throw new CancelCommandException("", t);
			}
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public String getName() {
			return "die";
		}

		@Override
		public String docs() {
			return "nothing {[var1, var2...,]} Kills the command immediately, without completing it. A message is"
					+ " optional, but if provided, displayed to the user.";
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.TERMINAL
			);
		}
	}
}
