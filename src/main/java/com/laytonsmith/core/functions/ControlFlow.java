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
import com.laytonsmith.core.compiler.ConditionalSelfStatement;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.SelfStatement;
import com.laytonsmith.core.compiler.VariableScope;
import com.laytonsmith.core.compiler.analysis.Declaration;
import com.laytonsmith.core.compiler.analysis.Namespace;
import com.laytonsmith.core.compiler.analysis.ReturnableReference;
import com.laytonsmith.core.compiler.analysis.Scope;
import com.laytonsmith.core.compiler.analysis.StaticAnalysis;
import com.laytonsmith.core.compiler.signature.FunctionSignatures;
import com.laytonsmith.core.compiler.signature.FunctionSignatures.MatchType;
import com.laytonsmith.core.compiler.signature.SignatureBuilder;
import com.laytonsmith.core.constructs.Auto;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CKeyword;
import com.laytonsmith.core.constructs.CLabel;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CSlice;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.InstanceofUtil;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREInsufficientArgumentsException;
import com.laytonsmith.core.exceptions.CRE.CREInvalidProcedureException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.functions.BasicLogic.and;
import com.laytonsmith.core.functions.Compiler.__statements__;
import com.laytonsmith.core.functions.Compiler.centry;
import com.laytonsmith.core.functions.DataHandling.assign;
import com.laytonsmith.core.functions.Math.dec;
import com.laytonsmith.core.functions.Math.inc;
import com.laytonsmith.core.functions.Math.postdec;
import com.laytonsmith.core.functions.Math.postinc;
import com.laytonsmith.core.functions.StringHandling.sconcat;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.FunctionReturnException;
import com.laytonsmith.core.exceptions.LoopBreakException;
import com.laytonsmith.core.exceptions.LoopContinueException;
import com.laytonsmith.core.natives.interfaces.Booleanish;
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
	@ConditionalSelfStatement
	public static class _if extends AbstractFunction implements Optimizable, BranchStatement, VariableScope {

		public static final String NAME = "if";

		@Override
		public String getName() {
			return NAME;
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			ParseTree condition = nodes[0];
			if(ArgumentValidation.getBooleanish(parent.seval(condition, env), t)) {
				ParseTree ifCode = nodes[1];
				return parent.seval(ifCode, env);
			} else if(nodes.length == 3) {
				ParseTree elseCode = nodes[2];
				return parent.seval(elseCode, env);
			} else {
				return CVoid.VOID;
			}
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args)
				throws CancelCommandException, ConfigRuntimeException {
			return CVoid.VOID;
		}

		@Override
		public FunctionSignatures getSignatures() {
			/*
			 *  TODO - Decide how to define the ternary return value.
			 *  Note that getReturnType is overridden, so these signatures are not used for typechecking.
			 */
			return new SignatureBuilder(CClassType.AUTO, MatchType.MATCH_FIRST)
					.param(Booleanish.TYPE, "cond", "The condition.")
					.param(Mixed.TYPE, "ifValue", "The value that is returned when the condition is true.")
					.param(Mixed.TYPE, "elseValue", "The value that is returned when the condition is false.")
					.newSignature(CVoid.TYPE).param(Booleanish.TYPE, "cond", "The condition.")
					.param(null, "ifCode", "The code that runs when the condition is true.")
					.param(null, "elseCode", "The optional code that runs when the condition is false.", true).build();
		}

		@Override
		public CClassType getReturnType(Target t, List<CClassType> argTypes,
				List<Target> argTargets, Environment env, Set<ConfigCompileException> exceptions) {

			// Get return type based on the function signatures. This generates all necessary compile errors.
			CClassType retType = super.getReturnType(t, argTypes, argTargets, env, exceptions);

			// When void is returned, ternary usage could still be possible when a branch is terminating.
			// It is also possible that both branches are terminating, in which case this should return null as well.
			if(retType == CVoid.TYPE && argTypes.size() == 3) {

				// Return the type of the other branch if one branch is terminating (ternary, terminating or void).
				if(argTypes.get(1) == null) {
					return argTypes.get(2);
				}
				if(argTypes.get(2) == null) {
					return argTypes.get(1);
				}
			}

			// Perform partial type inference since there is no way to express an A OR B type yet.
			/*
			 * TODO - This currently returns the lowest type if one extends the other.
			 * Make this return a multitype instead as soon as all typechecking code supports multitypes.
			 */
			if(retType == CClassType.AUTO && argTypes.size() == 3) {
				if(InstanceofUtil.isInstanceof(argTypes.get(1), argTypes.get(2), env)) {
					return argTypes.get(2);
				}
				if(InstanceofUtil.isInstanceof(argTypes.get(2), argTypes.get(1), env)) {
					return argTypes.get(1);
				}
			}

			// Return the super result.
			return retType;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public Scope linkScope(StaticAnalysis analysis, Scope parentScope,
				ParseTree ast, Environment env, Set<ConfigCompileException> exceptions) {
			if(ast.numberOfChildren() < 2) {
				return parentScope;
			}

			// Handle condition in parent scope.
			Scope condScope = analysis.linkScope(parentScope, ast.getChildAt(0), env, exceptions);

			// Handle if and else branches in separate scopes.
			analysis.linkScope(analysis.createNewScope(condScope), ast.getChildAt(1), env, exceptions);
			if(ast.numberOfChildren() == 3) {
				analysis.linkScope(analysis.createNewScope(condScope), ast.getChildAt(2), env, exceptions);
			}

			// Return the condition scope.
			return condScope;
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

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> args, FileOptions fileOptions)
				throws ConfigCompileException {
			//Check for too many/few arguments
			if(args.size() < 2) {
				throw new ConfigCompileException("Too few arguments passed to " + this.getName() + "()", t);
			}
			if(args.size() > 3) {
				throw new ConfigCompileException(this.getName() + "() can only have 3 parameters", t);
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
					&& args.get(1).getData().val().equals(_if.NAME) && args.size() == 2) {
				ParseTree _ifNode = args.get(1);
				if(_ifNode.getChildren().size() == 2) {
					// All the conditions are met, move this up
					ParseTree myCondition = args.get(0);
					ParseTree theirCondition = _ifNode.getChildAt(0);
					ParseTree theirCode = _ifNode.getChildAt(1);
					ParseTree andClause = new ParseTree(new CFunction(and.NAME, t), fileOptions);
					// If it's already an and(), just tack the other condition on
					if(myCondition.getData() instanceof CFunction && myCondition.getData().val().equals(and.NAME)) {
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

		@Override
		public boolean isSelfStatement(Target t, Environment env, List<ParseTree> nodes, Set<Class<? extends Environment.EnvironmentImpl>> envs) throws ConfigCompileException {
			if(nodes.size() < 2) {
				return true;
			}
			doAutoconcatRewrite(nodes.get(1), env, envs);
			if(nodes.get(1).getData() instanceof CFunction cf && cf.val().equals(Compiler.__statements__.NAME)) {
				return true;
			}
			if(nodes.size() > 2) {
				doAutoconcatRewrite(nodes.get(2), env, envs);
				if(nodes.get(2).getData() instanceof CFunction cf && cf.val().equals(Compiler.__statements__.NAME)) {
					return true;
				}
			}
			return false;
		}

	}

	@api(environments = {GlobalEnv.class})
	@ConditionalSelfStatement
	public static class ifelse extends AbstractFunction implements Optimizable, BranchStatement, VariableScope {

		public static final String NAME = "ifelse";

		@Override
		public String getName() {
			return NAME;
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
				ParseTree condition = nodes[i];
				if(ArgumentValidation.getBooleanish(parent.seval(condition, env), t)) {
					ParseTree ifCode = nodes[i + 1];
					return env.getEnv(GlobalEnv.class).GetScript().seval(ifCode, env);
				}
			}
			if(nodes.length % 2 == 1) {
				return env.getEnv(GlobalEnv.class).GetScript().seval(nodes[nodes.length - 1], env);
			}
			return CVoid.VOID;
		}

		@Override
		public FunctionSignatures getSignatures() {
			/*
			 * TODO - Implement a way to define [cond, code]* using signatures, and use it here.
			 * Also check switch() and switch_ic(), as they need the same feature.
			 */
			return super.getSignatures();
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public Scope linkScope(StaticAnalysis analysis, Scope parentScope,
				ParseTree ast, Environment env, Set<ConfigCompileException> exceptions) {
			Scope firstCondScope = null;
			for(int i = 0; i <= ast.numberOfChildren() - 2; i += 2) {
				ParseTree cond = ast.getChildAt(i);
				ParseTree code = ast.getChildAt(i + 1);

				// Handle condition in parent scope.
				Scope condScope = analysis.linkScope(parentScope, cond, env, exceptions);
				if(firstCondScope == null) {
					firstCondScope = condScope;
				}

				// Handle code branches in separate scope.
				analysis.linkScope(analysis.createNewScope(condScope), code, env, exceptions);
			}

			// Handle optional else branch in separate scope.
			if((ast.numberOfChildren() & 0x01) == 0x01) { // (size % 2) == 1.
				analysis.linkScope(analysis.createNewScope(parentScope),
						ast.getChildAt(ast.numberOfChildren() - 1), env, exceptions);
			}

			// Return the first condition scope if available, as this is the only code that always runs.
			return (firstCondScope != null ? firstCondScope : parentScope);
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
				List<ParseTree> children, FileOptions fileOptions) throws ConfigCompileException {

			// Check for too few arguments.
			if(children.size() < 2) {
				throw new ConfigCompileException("Too few arguments passed to " + this.getName() + "()", t);
			}

			// Optimize per condition code pair for constant conditions.
			boolean foundDynamicCond = false;
			for(int i = 0; i < children.size() - 1; i += 2) {
				ParseTree condNode = children.get(i);
				if(condNode.isConst()) {
					if(ArgumentValidation.getBooleanish(condNode.getData(), t)) {

						// Optimize to true condition code block if no dynamic condition was present before this.
						if(!foundDynamicCond) {
							ParseTree codeNode = children.get(i + 1);
							return codeNode;
						}

						// Remove condition code block pairs and else code block after this static true condition.
						for(int j = children.size() - 1; j >= i + 2; j--) {
							children.remove(j);
						}
						return null;
					} else {

						// Remove this constant false condition and its code block.
						children.remove(i + 1);
						children.remove(i);
						i -= 2; // Compensate for next loop increment.
					}
				} else {
					foundDynamicCond = true;
				}
			}

			// Remove this ifelse() if no children are left.
			if(children.size() == 0) {
				return Optimizable.REMOVE_ME;
			}

			// Optimize this ifelse() to its else code block if only that code block is remaining.
			if(children.size() == 1) {
				return children.get(0);
			}
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
			if(children.size() == 0) {
				return branches;
			}
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

		@Override
		public boolean isSelfStatement(Target t, Environment env, List<ParseTree> nodes, Set<Class<? extends Environment.EnvironmentImpl>> envs) throws ConfigCompileException {

			for(int i = 1; i < nodes.size(); i += 2) {
				doAutoconcatRewrite(nodes.get(i), env, envs);
				if(nodes.get(i).getData() instanceof CFunction cf && cf.val().equals(Compiler.__statements__.NAME)) {
					return true;
				}
			}

			if(nodes.size() % 2 == 1) {
				doAutoconcatRewrite(nodes.get(nodes.size() - 1), env, envs);
				if(nodes.get(nodes.size() - 1).getData() instanceof CFunction cf
						&& cf.val().equals(Compiler.__statements__.NAME)) {
					return true;
				}
			}
			return false;
		}
	}

	@api
	@breakable
	@ConditionalSelfStatement
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
					+ " brace/case/default syntax, as in most languages, although unlike most languages, fallthrough"
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
		public boolean isSelfStatement(Target t, Environment env, List<ParseTree> nodes,
				Set<Class<? extends Environment.EnvironmentImpl>> envs) throws ConfigCompileException {
			if(nodes.size() < 2) {
				return true;
			}
			for(int i = 2; i < nodes.size(); i += 2) {
				doAutoconcatRewrite(nodes.get(i), env, envs);
				if(nodes.get(i).getData() instanceof CFunction cf && cf.val().equals(Compiler.__statements__.NAME)) {
					return true;
				}
			}
			if(nodes.size() % 2 == 0) {
				if(nodes.get(nodes.size() - 1).getData() instanceof CFunction cf
						&& cf.val().equals(Compiler.__statements__.NAME)) {
					return true;
				}
			}
			return false;
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
							long v = ArgumentValidation.getInt(value, t);
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
									long v = ArgumentValidation.getInt(value, t);
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
		public FunctionSignatures getSignatures() {
			/*
			 * TODO - Implement a way to define [case, code]* using signatures, and use it here.
			 * Also check ifelse() and switch_ic(), as they need the same feature.
			 */
			return super.getSignatures();
		}

		@Override
		public Scope linkScope(StaticAnalysis analysis, Scope parentScope,
				ParseTree ast, Environment env, Set<ConfigCompileException> exceptions) {
			List<ParseTree> children = ast.getChildren();
			for(int i = 0; i <= children.size() - 2; i += 2) {
				ParseTree cond = children.get(i);
				ParseTree code = children.get(i + 1);

				// Handle condition in parent scope.
				Scope condScope = analysis.linkScope(parentScope, cond, env, exceptions);

				// Handle code branches in separate scope.
				analysis.linkScope(analysis.createNewScope(condScope), code, env, exceptions);
			}

			// Handle optional default branch in separate scope.
			if((children.size() & 0x01) == 0x01) { // (size % 2) == 1.
				analysis.linkScope(
						analysis.createNewScope(parentScope), children.get(children.size() - 1), env, exceptions);
			}

			// Return the parent scope.
			return parentScope;
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
				+ " except where cases are directly back to back.",
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

			if(children.size() < 1) {
				throw new ConfigCompileException("Too few arguments passed to " + this.getName() + "()", t);
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
						&& children.get(i).getData().val().equals(DataHandling.array.NAME)) {
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
								long v = ArgumentValidation.getInt(children.get(0).getData(), t);
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
	@breakable
	@ConditionalSelfStatement
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
			for(int i = 1; i < children.size() - 1; i += 2) {
				ParseTree child = children.get(i);
				Mixed caseData = child.getData();
				if(caseData instanceof CArray) {
					CArray newData = new CArray(child.getTarget());
					for(Mixed cse : ((CArray) caseData).asList()) {
						if(cse instanceof CString) {
							CString data = (CString) cse;
							newData.push(new CString(data.val().toLowerCase(), data.getTarget()), data.getTarget());
						} else {
							throw new ConfigCompileException(getName() + " can only accept strings in case statements.",
									cse.getTarget());
						}
					}
					child.setData(newData);
				} else if(caseData instanceof CString) {
					CString data = (CString) caseData;
					child.setData(new CString(data.val().toLowerCase(), data.getTarget()));
				} else {
					throw new ConfigCompileException(getName() + " can only accept strings in case statements.",
							caseData.getTarget());
				}
			}
			return switchTree;
		}

	}

	@api
	@noboilerplate
	@breakable
	@seealso({com.laytonsmith.tools.docgen.templates.Loops.class,
		com.laytonsmith.tools.docgen.templates.ArrayIteration.class})
	@SelfStatement
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
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(CVoid.TYPE)
					.param(Mixed.TYPE, "assign", "The ivariable assign for the loop variable in this loop.")
					.param(Booleanish.TYPE, "condition",
							"The loop condition that is checked each time before the loopCode is executed."
							+ "When this is false, this function returns.")
					.param(Mixed.TYPE, "loopExpr", "The expression that is executed each time the loop continues"
							+ " after executing the loopCode.")
					.param(null, "loopCode", "The code that is executed in the loop.").build();
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public Scope linkScope(StaticAnalysis analysis, Scope parentScope,
				ParseTree ast, Environment env, Set<ConfigCompileException> exceptions) {
			super.linkScope(analysis, parentScope, ast, env, exceptions);
			return parentScope;
		}

		@Override
		public String docs() {
			return "void {assign, condition, loopExpr, loopCode} Acts as a typical for loop. The assignment is"
					+ " first run. Then, a condition is checked. If that condition is checked and returns true,"
					+ " loopCode is run. After that, loopExpr is run. In java syntax, this would be:"
					+ " for(assign; condition; loopExpr){loopCode}. assign must be an ivariable, either a "
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
						&& ((isInc = children.get(2).getData().val().equals(postinc.NAME))
						|| children.get(2).getData().val().equals(postdec.NAME))
						&& children.get(2).getChildAt(0).getData() instanceof IVariable) {
					ParseTree pre = new ParseTree(
							new CFunction(isInc ? inc.NAME : dec.NAME, t), children.get(2).getFileOptions());
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
		public List<Boolean> statementsAllowed(List<ParseTree> children) {
			List<Boolean> ret = new ArrayList<>();
			ret.add(false);
			ret.add(false);
			ret.add(false);
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
	@SelfStatement
	public static class forelse extends AbstractFunction implements BranchStatement, VariableScope {

		public static final String NAME = "forelse";

		public forelse() {
		}

		boolean runAsFor = false;

		forelse(boolean runAsFor) {
			this.runAsFor = runAsFor;
		}

		@Override
		public String getName() {
			return NAME;
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
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(CVoid.TYPE)
					.param(Mixed.TYPE, "assign", "The ivariable assign for the loop variable in this loop.")
					.param(Booleanish.TYPE, "condition",
							"The loop condition that is checked each time before the loopCode is executed."
							+ "When this is false, this function returns."
							+ " If loopCode has not been executed in the first iteration, then elseCode is executed.")
					.param(Mixed.TYPE, "loopExpr", "The expression that is executed each time the loop continues"
							+ " after executing the loopCode.")
					.param(null, "loopCode", "The code that is executed in the loop.")
					.param(null, "elseCode", "The code that is executed when the condition returns"
							+ " false in the first iteration of the loop.").build();
		}

		@Override
		public Scope linkScope(StaticAnalysis analysis, Scope parentScope,
				ParseTree ast, Environment env, Set<ConfigCompileException> exceptions) {
			if(ast.numberOfChildren() >= (this.runAsFor ? 3 : 4)) {
				ParseTree assign = ast.getChildAt(0);
				ParseTree cond = ast.getChildAt(1);
				ParseTree exp = ast.getChildAt(2);
				ParseTree code = ast.getChildAt(3);
				ParseTree elseCode = (this.runAsFor ? null : ast.getChildAt(4));

				// Order: assign -> cond -> (code -> exp -> cond)* -> elseCode?.
				Scope assignScope = analysis.linkScope(parentScope, assign, env, exceptions);
				Scope condScope = analysis.linkScope(assignScope, cond, env, exceptions);
				Scope codeScope = analysis.linkScope(condScope, code, env, exceptions);
				analysis.linkScope(codeScope, exp, env, exceptions);
				if(elseCode != null) {
					analysis.linkScope(condScope, code, env, exceptions);
				}
			}
			return parentScope;
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{5};
		}

		@Override
		public String docs() {
			return "void {assign, condition, loopExpr, loopCode, elseCode} Works like a normal for loop, but if upon"
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
	@SelfStatement
	public static class foreach extends AbstractFunction implements BranchStatement, VariableScope {

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
								kkey.getVariableName(), c, kkey.getDefinedTarget(), env));
					}
					//Set the value in the variable table
					env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(two.getDefinedType(),
							two.getVariableName(), one.get(c, t), two.getDefinedTarget(), env));
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
										kkey.getVariableName(), new CInt(current, t), kkey.getDefinedTarget(), env));
							}
							env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(two.getDefinedType(),
									two.getVariableName(), one.get(current, t), two.getDefinedTarget(), env));
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
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(CVoid.TYPE)
					.param(com.laytonsmith.core.natives.interfaces.Iterable.TYPE, "data", "The iterable data.")
					.param(null, "key",
							"The optional ivariable used to assign the key of each data entry key to.", true)
					.param(null, "value", "The ivariable used to assign each data entry value to.")
					.param(null, "code", "The code that will be executed for each entry in the data.").build();
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRERangeException.class};
		}

		@Override
		public Scope linkScope(StaticAnalysis analysis, Scope parentScope,
				ParseTree ast, Environment env, Set<ConfigCompileException> exceptions) {
			if(ast.numberOfChildren() >= 3) {
				int ind = 0;
				ParseTree array = ast.getChildAt(ind++);
				ParseTree key = (ast.numberOfChildren() == 4 ? ast.getChildAt(ind++) : null);
				ParseTree val = ast.getChildAt(ind++);
				ParseTree code = ast.getChildAt(ind++);

				// Order: array -> [key] -> val -> code?.
				Scope arrayScope = analysis.linkScope(parentScope, array, env, exceptions);
				Scope keyParamScope = arrayScope;
				Scope keyValScope = arrayScope;
				if(key != null) {
					Scope[] scopes = analysis.linkParamScope(keyParamScope, keyValScope, key, env, exceptions);
					keyParamScope = scopes[0];
					keyValScope = scopes[1];
				}
				Scope[] scopes = analysis.linkParamScope(keyParamScope, keyValScope, val, env, exceptions);
				Scope valScope = scopes[0]; // paramScope.
				analysis.linkScope(valScope, code, env, exceptions);
			}
			return parentScope;
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

		private boolean isFunction(ParseTree node, String function) {
			return node.getData() instanceof CFunction && node.getData().val().equals(function);
		}

		private boolean isKeyword(ParseTree node, String keyword) {
			return node.getData() instanceof CKeyword && node.getData().val().equals(keyword);
		}

		@Override
		public ParseTree postParseRewrite(ParseTree ast, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs, Set<ConfigCompileException> exceptions) {
			List<ParseTree> children = ast.getChildren();
			if(children.size() < 2) {
				return null;
			}
			if(isFunction(children.get(0), centry.NAME)) {
				// This is what "@key: @value in @array" looks like initially.
				// We'll refactor this so the next segment can take over properly.
				ParseTree statementsNode = new ParseTree(
						new CFunction(__statements__.NAME, ast.getTarget()), ast.getFileOptions());
				statementsNode.addChild(children.get(0).getChildAt(0));
				for(int i = 0; i < children.get(0).getChildAt(1).numberOfChildren(); i++) {
					statementsNode.addChild(children.get(0).getChildAt(1).getChildAt(i));
				}
				children.set(0, statementsNode);
			}
			if(children.get(0).getData() instanceof CFunction
					&& (children.get(0).getData().val().equals(sconcat.NAME)
							|| children.get(0).getData().val().equals(__statements__.NAME))) {
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
					exceptions.add(new ConfigCompileException(
							"Invalid argument format passed to " + getName(), ast.getTarget()));
					return null;
				}
				if(key != null && key.getData() instanceof CLabel) {
					if(!(((CLabel) key.getData()).cVal() instanceof IVariable)
							&& !(((CLabel) key.getData()).cVal() instanceof CFunction
							&& ((CLabel) key.getData()).cVal().val().equals(assign.NAME))) {
						exceptions.add(new ConfigCompileException("Expected a variable for key, but \""
								+ key.getData().val() + "\" was found", ast.getTarget()));
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
					ParseTree foreachelseNode = new ParseTree(
							new CFunction(foreachelse.NAME, ast.getTarget()), ast.getFileOptions());
					children.set(children.size() - 1, children.get(children.size() - 1).getChildAt(0));
					foreachelseNode.setChildren(children);
					return foreachelseNode;
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

		public static final String NAME = "foreachelse";

		@Override
		public String getName() {
			return NAME;
		}

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
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(CVoid.TYPE)
					.param(com.laytonsmith.core.natives.interfaces.Iterable.TYPE, "data", "The iterable data.")
					.param(null, "key",
							"The optional ivariable used to assign the key of each data entry key to.", true)
					.param(null, "value", "The ivariable used to assign each data entry value to.")
					.param(null, "code", "The code that will be executed for each entry in the data.")
					.param(null, "elseCode", "The code that will be executed when the data contains no entries.")
					.build();
		}

		@Override
		public Scope linkScope(StaticAnalysis analysis, Scope parentScope,
				ParseTree ast, Environment env, Set<ConfigCompileException> exceptions) {
			if(ast.numberOfChildren() >= 4) {
				int ind = 0;
				ParseTree array = ast.getChildAt(ind++);
				ParseTree key = (ast.numberOfChildren() == 5 ? ast.getChildAt(ind++) : null);
				ParseTree val = ast.getChildAt(ind++);
				ParseTree code = ast.getChildAt(ind++);
				ParseTree elseCode = ast.getChildAt(ind++);

				// Order: array -> [key] -> val -> code? | array -> elseCode.
				Scope arrayScope = analysis.linkScope(parentScope, array, env, exceptions);
				Scope keyParamScope = arrayScope;
				Scope keyValScope = arrayScope;
				if(key != null) {
					Scope[] scopes = analysis.linkParamScope(keyParamScope, keyValScope, key, env, exceptions);
					keyParamScope = scopes[0];
					keyValScope = scopes[1];
				}
				Scope[] scopes = analysis.linkParamScope(keyParamScope, keyValScope, val, env, exceptions);
				Scope valScope = scopes[0]; // paramScope.
				analysis.linkScope(valScope, code, env, exceptions);
				analysis.linkScope(arrayScope, elseCode, env, exceptions);
			}
			return parentScope;
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
	@SelfStatement
	public static class _while extends AbstractFunction implements BranchStatement, VariableScope {

		public static final String NAME = "while";

		@Override
		public String getName() {
			return NAME;
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
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(CVoid.TYPE)
					.param(Booleanish.TYPE, "cond",
							"The loop condition that is checked each time before the code is executed.")
					.param(null, "code", "The code that is executed in the loop.", true).build();
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
		public Scope linkScope(StaticAnalysis analysis, Scope parentScope,
				ParseTree ast, Environment env, Set<ConfigCompileException> exceptions) {
			super.linkScope(analysis, parentScope, ast, env, exceptions);
			return parentScope;
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
			if(children.size() >= 2) {
				ret.add(true);
			}
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
	@SelfStatement
	public static class _dowhile extends AbstractFunction implements BranchStatement, VariableScope {

		public static final String NAME = "dowhile";

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
			return CNull.NULL;
		}

		@Override
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(CVoid.TYPE)
					.param(null, "code", "The code that is executed in the loop.")
					.param(Booleanish.TYPE, "cond",
							"The loop condition that is checked each time after the code is executed.").build();
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
		public Scope linkScope(StaticAnalysis analysis, Scope parentScope,
				ParseTree ast, Environment env, Set<ConfigCompileException> exceptions) {
			super.linkScope(analysis, parentScope, ast, env, exceptions);
			return parentScope;
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

		public static final String NAME = "break";

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
				num = ArgumentValidation.getInt32(args[0], t);
			}
			throw new LoopBreakException(num, t);
		}

		@Override
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(null)
					.param(CInt.TYPE, "loopAmount", "The amount of loops to break from.", true).build();
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

		public static final String NAME = "continue";

		@Override
		public String getName() {
			return NAME;
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
				num = ArgumentValidation.getInt32(args[0], t);
			}
			throw new LoopContinueException(num, t);
		}

		@Override
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(null)
					.param(CInt.TYPE, "loopAmount", "The amount of loop iterations to continue.", true).build();
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

		public static final String NAME = "return";

		@Override
		public String getName() {
			return NAME;
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "nothing {mixed} Returns the specified value from this procedure or closure."
					+ " It cannot be called outside a procedure or closure. The function itself does"
					+ " not return a value as such, as it is a terminal function, and prevents further"
					+ " execution within the calling code. Instead it causes the host procedure or closure"
					+ " to return the specified value, and ends termination. (There are exceptions to this rule,"
					+ " see the docs on try/catch, particularly the finally clause for example).";
		}

		@Override
		public CClassType typecheck(StaticAnalysis analysis,
				ParseTree ast, Environment env, Set<ConfigCompileException> exceptions) {

			// Get value type.
			CClassType valType;
			Target valTarget;
			if(ast.numberOfChildren() == 0) {
				valType = CVoid.TYPE;
				valTarget = ast.getTarget();
			} else if(ast.numberOfChildren() == 1) {
				ParseTree valNode = ast.getChildAt(0);
				valType = analysis.typecheck(valNode, env, exceptions);
				valTarget = valNode.getTarget();
			} else {

				// Fall back to default behavior for invalid usage.
				return super.typecheck(analysis, ast, env, exceptions);
			}

			// Resolve this returnable reference to its returnable declaration to get its required return type.
			Scope scope = analysis.getTermScope(ast);
			if(scope != null) {
				Set<Declaration> decls = scope.getDeclarations(Namespace.RETURNABLE, null);
				if(decls.size() == 0) {
					exceptions.add(new ConfigCompileException("Return is not valid in this context.", ast.getTarget()));
				} else {

					// Type check return value for all found declared return types.
					for(Declaration decl : decls) {
						StaticAnalysis.requireType(valType, decl.getType(), valTarget, env, exceptions);
					}
				}
			}

			// Return void.
			return CVoid.TYPE;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
		}

		@Override
		public Scope linkScope(StaticAnalysis analysis, Scope parentScope, ParseTree ast,
				Environment env, Set<ConfigCompileException> exceptions) {

			// Handle children. These will execute before this return().
			Scope scope = super.linkScope(analysis, parentScope, ast, env, exceptions);

			// Add returnable reference in new scope.
			scope = analysis.createNewScope(scope);
			scope.addReference(new ReturnableReference(ast.getTarget()));
			analysis.setTermScope(ast, scope);

			// Return scope.
			return scope;
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

		@Override
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(null)
					.param(Mixed.TYPE, "value", "The value to return. If omitted, void will be returned.", true)
					.build();
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
		public FunctionSignatures getSignatures() {
			// TODO - Overwrite getReturnType() to return the return type of the proc when available.
			return new SignatureBuilder(Auto.TYPE)
					.param(CString.TYPE, "procName", "The name of the procedure.")
					.varParam(Mixed.TYPE, "args", "The procedure arguments.").build();
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
			CArray ca = ArgumentValidation.getArray(args[1], t);
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
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(null).varParam(Mixed.TYPE, "messages",
					"The messages that will be shown to the user (concatenated together).").build();
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
