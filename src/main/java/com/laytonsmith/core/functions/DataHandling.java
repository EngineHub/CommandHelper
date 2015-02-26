package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.ArrayUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.breakable;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.annotations.nolinking;
import com.laytonsmith.annotations.noprofile;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.annotations.unbreakable;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Globals;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.PermissionsResolver;
import com.laytonsmith.core.Procedure;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.keywords.InKeyword;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CByteArray;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CKeyword;
import com.laytonsmith.core.constructs.CLabel;
import com.laytonsmith.core.constructs.CMutablePrimitive;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CSlice;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.IVariableList;
import com.laytonsmith.core.constructs.InstanceofUtil;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.FunctionReturnException;
import com.laytonsmith.core.exceptions.LoopBreakException;
import com.laytonsmith.core.exceptions.LoopContinueException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.interfaces.ArrayAccess;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
@core
public class DataHandling {

	private static final String array_get = new ArrayHandling.array_get().getName();
	private static final String array_set = new ArrayHandling.array_set().getName();
	private static final String array_push = new ArrayHandling.array_push().getName();

	public static String docs() {
		return "This class provides various methods to control script data and program flow.";
	}

	@api
	public static class array extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "array";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			return new CArray(t, args);
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		@Override
		public String docs() {
			return "array {[var1, [var2...]]} Creates an array of values.";
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
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "assign(@array, array(1, 2, 3))\nmsg(@array)"),
				new ExampleScript("Associative array creation", "assign(@array, array(one: 'apple', two: 'banana'))\nmsg(@array)"),};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		FileOptions lastFileOptions = null;

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children, FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			//We need to check here to ensure that
			//we aren't getting a slice in a label, which is used in switch
			//statements, but doesn't make sense here.
			for (ParseTree child : children) {
				if (child.getData() instanceof CFunction && new Compiler.centry().getName().equals(child.getData().val())) {
					if (((CLabel) child.getChildAt(0).getData()).cVal() instanceof CSlice) {
						throw new ConfigCompileException("Slices cannot be used as array indices", child.getChildAt(0).getTarget());
					}
				}
			}
			return null;
		}

	}

	@api
	public static class associative_array extends AbstractFunction {

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
			CArray array = CArray.GetAssociativeArray(t, args);
			return array;
		}

		@Override
		public String getName() {
			return "associative_array";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "array {[args...]} Works exactly like array(), except the array created will be an associative array, even"
					+ " if the array has been created with no elements. This is the only use case where this is neccessary, vs"
					+ " using the normal array() function, or in the case where you assign sequential keys anyways, and the same"
					+ " array could have been created using array().";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Usage with an empty array", "assign(@array, associative_array())\nmsg(is_associative(@array))"),
				new ExampleScript("Usage with an array with sequential keys", "assign(@array, array(0: '0', 1: '1'))\nmsg(is_associative(@array))\n"
				+ "assign(@array, associative_array(0: '0', 1: '1'))\nmsg(is_associative(@array))"),};
		}

	}

	@api
	public static class assign extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "assign";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			IVariableList list = env.getEnv(GlobalEnv.class).GetVarList();
			int offset = 0;
			CClassType type = CClassType.AUTO;
			String name;
			if(args.length == 3){
				offset = 1;
				name = ((IVariable) args[offset + 0]).getName();
				if(list.has(name) && env.getEnv(GlobalEnv.class).GetFlag("no-check-duplicate-assign") == null){
					if(env.getEnv(GlobalEnv.class).GetFlag("closure-warn-overwrite") != null){
						CHLog.GetLogger().Log(CHLog.Tags.RUNTIME, LogLevel.ERROR, "The variable " + name + " is hiding another value of the"
								+ " same name in the main scope.", t);
					} else {
						CHLog.GetLogger().Log(CHLog.Tags.RUNTIME, LogLevel.ERROR, name + " was already defined at "
								+ list.get(name, t, true).getDefinedTarget() + " but is being redefined", t);
					}
				}
				type = ArgumentValidation.getClassType(args[0], t);
			}
			name = ((IVariable) args[offset + 0]).getName();
			Construct c = args[offset + 1];
			while (c instanceof IVariable) {
				IVariable cur = (IVariable) c;
				c = list.get(cur.getName(), cur.getTarget()).ival();
			}
			if (args[offset + 0] instanceof IVariable) {
				if(args.length == 2){
					type = list.get(name, t, true).getDefinedType();
				}
				IVariable v = new IVariable(type, name, c, t);
				list.set(v);
				return v;
			}
			throw new ConfigRuntimeException("assign only accepts an ivariable or array reference as the first argument", ExceptionType.CastException, t);
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		@Override
		public String docs() {
			return "ivariable {[type], ivar, mixed} Accepts an ivariable ivar as a parameter, and puts the specified value mixed in it."
					+ " Returns the variable that was assigned. Operator syntax is also supported: <code>@a = 5;</code>."
					+ " Other forms are supported as well, +=, -=, *=, /=, .=, which do multiple operations at once. Array assigns"
					+ " are also supported: @array[5] = 'new value in index 5';";
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

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.OPTIMIZE_CONSTANT,
					OptimizationOption.OPTIMIZE_DYNAMIC
			);
		}

		@Override
		public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
			//We can't really optimize, but we can check that we are
			//getting an ivariable.
			int offset = 0;
			if(args.length == 3){
				offset = 1;
				if(!(args[0] instanceof CClassType)){
					throw new ConfigCompileException("Expecting a ClassType for parameter 1 to assign", t);
				}
			}
			if (!(args[offset + 0] instanceof IVariable)) {
				throw new ConfigCompileException("Expecting an ivar for argument 1 to assign", t);
			}
			return null;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children, FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			if (children.get(0).getData() instanceof IVariable
					&& children.get(1).getData() instanceof IVariable) {
				if (((IVariable) children.get(0).getData()).getName().equals(
						((IVariable) children.get(1).getData()).getName())) {
					CHLog.GetLogger().Log(CHLog.Tags.COMPILER, LogLevel.WARNING, "Assigning a variable to itself", t);
				}
			}
			if (children.get(0).getData() instanceof CFunction && array_get.equals(children.get(0).getData().val())) {
				if (children.get(0).getChildAt(1).getData() instanceof CSlice) {
					CSlice cs = (CSlice) children.get(0).getChildAt(1).getData();
					if (cs.getStart() == 0 && cs.getFinish() == -1) {
						//Turn this into an array_push
						ParseTree tree = new ParseTree(new CFunction(array_push, t), children.get(0).getFileOptions());
						tree.addChild(children.get(0).getChildAt(0));
						tree.addChild(children.get(1));
						return tree;
					}
					//else, not really sure what's going on, so we'll just carry on, and probably there
					//will be an error generated elsewhere
				} else {
					//Turn this into an array set instead
					ParseTree tree = new ParseTree(new CFunction(array_set, t), children.get(0).getFileOptions());
					tree.addChild(children.get(0).getChildAt(0));
					tree.addChild(children.get(0).getChildAt(1));
					tree.addChild(children.get(1));
					return tree;
				}
			}
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "assign(@variable, 5);\nmsg(@variable);"),
				new ExampleScript("Array assignment", "assign(@variable, associative_array());\nassign(@variable['associative'], 5);\nmsg(@variable);"),
				new ExampleScript("Operator syntax", "@variable = 5;\nmsg(@variable);"),
				new ExampleScript("Operator syntax using combined operators", "@variable = 'string';\n@variable .= ' more string';\nmsg(@variable);"),
				new ExampleScript("Operator syntax using combined operators", "@variable = 5;\n@variable += 10;\nmsg(@variable);"),
				new ExampleScript("Operator syntax using combined operators", "@variable = 5;\n@variable -= 10;\nmsg(@variable);"),
				new ExampleScript("Operator syntax using combined operators", "@variable = 5;\n@variable *= 10;\nmsg(@variable);"),
				new ExampleScript("Operator syntax using combined operators", "@variable = 5;\n@variable /= 10;\nmsg(@variable);"),};
		}
	}

	@api
	@noboilerplate
	@breakable
	public static class _for extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "for";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{4};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) {
			return CVoid.VOID;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			return new forelse(true).execs(t, env, parent, nodes);
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		@Override
		public String docs() {
			return "void {assign, condition, expression1, expression2} Acts as a typical for loop. The assignment is first run. Then, a"
					+ " condition is checked. If that condition is checked and returns true, expression2 is run. After that, expression1 is run. In java"
					+ " syntax, this would be: for(assign; condition; expression1){expression2}. assign must be an ivariable, either a "
					+ "pre defined one, or the results of the assign() function. condition must be a boolean.";
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_0_1;
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
				new ExampleScript("With continue. (See continue() for more examples)", "for(assign(@i, 0), @i < 2, @i++){\n"
				+ "\tif(@i == 1, continue())\n"
				+ "\tmsg(@i)\n"
				+ "}"),};
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
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children, FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			//In for(@i = 0, @i < @x, @i++, ...), the @i++ is more optimally written as ++@i, but
			//it is commonplace to use postfix operations, so if the condition is in fact that simple,
			//let's reverse it.
			boolean isInc;
			try {
				if (children.get(2).getData() instanceof CFunction
						&& ((isInc = children.get(2).getData().val().equals("postinc"))
						|| children.get(2).getData().val().equals("postdec"))
						&& children.get(2).getChildAt(0).getData() instanceof IVariable) {
					ParseTree pre = new ParseTree(new CFunction(isInc ? "inc" : "dec", t), children.get(2).getFileOptions());
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

	}

	@api
	@noboilerplate
	@breakable
	public static class forelse extends AbstractFunction {

		public forelse() {
		}

		boolean runAsFor = false;

		forelse(boolean runAsFor) {
			this.runAsFor = runAsFor;
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
		public Boolean runAsync() {
			return null;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return null;
		}

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) throws ConfigRuntimeException {
			ParseTree assign = nodes[0];
			ParseTree condition = nodes[1];
			ParseTree expression = nodes[2];
			ParseTree runnable = nodes[3];
			ParseTree elseCode = null;
			if (!runAsFor) {
				elseCode = nodes[4];
			}
			boolean hasRunOnce = false;

			Construct counter = parent.eval(assign, env);
			if (!(counter instanceof IVariable)) {
				throw new ConfigRuntimeException("First parameter of for must be an ivariable", ExceptionType.CastException, t);
			}
			int _continue = 0;
			while (true) {
				boolean cond = Static.getBoolean(parent.seval(condition, env));
				if (cond == false) {
					break;
				}
				hasRunOnce = true;
				if (_continue >= 1) {
					--_continue;
					parent.eval(expression, env);
					continue;
				}
				try {
					parent.eval(runnable, env);
				} catch (LoopBreakException e) {
					int num = e.getTimes();
					if (num > 1) {
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
			if (!hasRunOnce && !runAsFor && elseCode != null) {
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
			return "void {assign, condition, expression1, expression2, else} Works like a normal for loop, but if upon checking the condition the first time,"
					+ " it is determined that it is false (that is, NO code loops are going to be run) the else code is run instead. If the loop runs,"
					+ " even once, it will NOT run the else branch. In general, brace syntax and use of for(){ } else { } syntax is preferred, instead"
					+ " of using forelse directly.";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

	@api(environments = CommandHelperEnvironment.class)
	@breakable
	public static class foreach extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "foreach";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3, 4};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			return CVoid.VOID;
		}

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			if(nodes.length < 3){
				throw new ConfigRuntimeException("Insufficient arguments passed to " + getName(), ExceptionType.InsufficientArgumentsException, t);
			}
			ParseTree array = nodes[0];
			ParseTree key = null;
			int offset = 0;
			if (nodes.length == 4) {
				//Key and value provided
				key = nodes[1];
				offset = 1;
			}
			ParseTree value = nodes[1 + offset];
			ParseTree code = nodes[2 + offset];
			Construct arr = parent.seval(array, env);
			Construct ik = null;
			if (key != null) {
				ik = parent.eval(key, env);
				if (!(ik instanceof IVariable)) {
					throw new ConfigRuntimeException("Parameter 2 of " + getName() + " must be an ivariable", ExceptionType.CastException, t);
				}
			}
			Construct iv = parent.eval(value, env);
			if (arr instanceof CSlice) {
				long start = ((CSlice) arr).getStart();
				long finish = ((CSlice) arr).getFinish();
				if (finish < start) {
					arr = new ArrayHandling.range().exec(t, env, new CInt(start, t), new CInt(finish - 1, t), new CInt(-1, t));
				} else {
					arr = new ArrayHandling.range().exec(t, env, new CInt(start, t), new CInt(finish + 1, t));
				}
			}
			if (!(arr instanceof ArrayAccess)) {
				throw new ConfigRuntimeException("Parameter 1 of " + getName() + " must be an array or array like data structure", ExceptionType.CastException, t);
			}
			if (!(iv instanceof IVariable)) {
				throw new ConfigRuntimeException("Parameter " + (2 + offset) + " of " + getName() + " must be an ivariable", ExceptionType.CastException, t);
			}
			ArrayAccess one = (ArrayAccess) arr;
			IVariable kkey = (IVariable) ik;
			IVariable two = (IVariable) iv;
			if (one.isAssociative()) {
					//Iteration of an associative array is much easier, and we have
				//special logic here to decrease the complexity.

					//Clone the set, so changes in the array won't cause changes in
				//the iteration order.
				Set<Construct> keySet = new LinkedHashSet<>(one.keySet());
					//Continues in an associative array are slightly different, so
				//we have to track this differently. Basically, we skip the
				//next element in the array key set.
				int continues = 0;
				for (Construct c : keySet) {
					if (continues > 0) {
						//If continues is greater than 0, continue in the loop,
						//however many times necessary to make it 0.
						continues--;
						continue;
					}
					//If the key isn't null, set that in the variable table.
					if (kkey != null) {
						env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(kkey.getDefinedType(), kkey.getName(), c, t));
					}
					//Set the value in the variable table
					env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(two.getDefinedType(), two.getName(), one.get(c.val(), t), t));
					try {
						//Execute the code
						parent.eval(code, env);
						//And handle any break/continues.
					} catch (LoopBreakException e) {
						int num = e.getTimes();
						if (num > 1) {
							e.setTimes(--num);
							throw e;
						}
						return CVoid.VOID;
					} catch (LoopContinueException e) {
						// In associative arrays, (unlike with normal arrays) we need to decrement it by one, because the nature of
						// the normal array is such that the counter is handled manually by our code. Because we are letting java
						// handle our code though, this run actually counts as one run.
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
				ArrayAccess.ArrayAccessIterator iterator = new ArrayAccess.ArrayAccessIterator(one);
				List<ArrayAccess.ArrayAccessIterator> arrayAccessList = env.getEnv(GlobalEnv.class).GetArrayAccessIterators();
				try {
					arrayAccessList.add(iterator);
					int continues = 0;
					while (true) {
						int current = iterator.getCurrent();
						if (continues > 0) {
								//We have some continues to handle. Blacklisted
							//values don't count for the continuing count, so
							//we have to consider that when counting.
							iterator.incrementCurrent();
							if (iterator.isBlacklisted(current)) {
								continue;
							} else {
								--continues;
								continue;
							}
						}
						if (current >= one.size()) {
							//Done with the iterations.
							break;
						}
						//If the item is blacklisted, we skip it.
						if (!iterator.isBlacklisted(current)) {
							if (kkey != null) {
								env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(kkey.getDefinedType(), kkey.getName(), new CInt(current, t), t));
							}
							env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(two.getDefinedType(), two.getName(), one.get(current, t), t));
							try {
								parent.eval(code, env);
							} catch (LoopBreakException e) {
								int num = e.getTimes();
								if (num > 1) {
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
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.RangeException};
		}

		@Override
		public String docs() {
			return "void {array, [key], ivar, code} Walks through array, setting ivar equal to each element in the array, then running code."
					+ " In addition, foreach(1..4, @i, code()) is also valid, setting @i to 1, 2, 3, 4 each time. The same syntax is valid as"
					+ " in an array slice. If key is set (it must be an ivariable) then the index of each iteration will be set to that."
					+ " See the examples for a demonstration. ---- "
					+ " Enhanced syntax may also be used in foreach, using the \"in\", \"as\" and \"else\" keywords. See the examples for"
					+ " examples of each structure. Using these keywords makes the structure of the foreach read much better. For instance,"
					+ " with foreach(@value in @array){ } the code very literally reads \"for each value in array\", making ascertaining"
					+ " the behavior of the loop easier. The \"as\" keyword reads less plainly, and so is not recommended for use, but is"
					+ " allowed. Note that the array and value are reversed with the \"as\" keyword. An \"else\" block may be used after"
					+ " the foreach, which will only run if the array provided is empty, that is, the loop code would never run. This provides"
					+ " a good way to provide \"default\" handling. Array modifications while iterating are supported, and are well defined."
					+ " See [[CommandHelper/Staged/Array_iteration|the page documenting array iterations]] for full details.";
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_0_1;
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
				new ExampleScript("Using \"a\" keyword", "@array = array(1, 2, 3);\n"
				+ "foreach(@array as @value){\n"
				+ "\tmsg(@value);\n"
				+ "}"),
				new ExampleScript("Using \"as\" keyword, with a key", "@array = array(1, 2, 3);\n"
				+ "foreach(@array as @key: @value){\n"
				+ "\tmsg(@key . ': ' . @value);\n"
				+ "}"),
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
				new ExampleScript("With array keys", "@array = array('one': 1, 'two': 2)\nforeach(@array, @key, @value){\n\tmsg(@key.':'.@value)\n}"),};
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
		private static final String ASSIGN = new assign().getName();
		private static final String SCONCAT = new StringHandling.sconcat().getName();
		private static final String IN = new InKeyword().getKeywordName();

		private boolean isFunction(ParseTree node, String function){
			return node.getData() instanceof CFunction && node.getData().val().equals(function);
		}

		private boolean isKeyword(ParseTree node, String keyword){
			return node.getData() instanceof CKeyword && node.getData().val().equals(keyword);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children, FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			if (children.size() < 2) {
				throw new ConfigCompileException("Invalid number of arguments passed to " + getName(), t);
			}
			if (isFunction(children.get(0), CENTRY)) {
				// This is what "@key: @value in @array" looks like initially. We'll refactor this so the next segment can take over properly.
				ParseTree sconcat = new ParseTree(new CFunction(new StringHandling.sconcat().getName(), t), fileOptions);
				sconcat.addChild(children.get(0).getChildAt(0));
				for (int i = 0; i < children.get(0).getChildAt(1).numberOfChildren(); i++) {
					sconcat.addChild(children.get(0).getChildAt(1).getChildAt(i));
				}
				children.set(0, sconcat);
			}
			if (children.get(0).getData() instanceof CFunction && children.get(0).getData().val().equals(new StringHandling.sconcat().getName())) {
				// We may be looking at a "@value in @array" or "@array as @value" type
				// structure, so we need to re-arrange this into the standard format.
				ParseTree array = null;
				ParseTree key = null;
				ParseTree value = null;
				List<ParseTree> c = children.get(0).getChildren();
				if (c.size() == 3) {
					// No key specified
					switch (c.get(1).getData().val()) {
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
				} else if (c.size() == 4) {
					if ("in".equals(c.get(2).getData().val())) {
						// @key: @value in @array
						key = c.get(0);
						value = c.get(1);
						array = c.get(3);
					} else if ("as".equals(c.get(1).getData().val())) {
						// @array as @key: @value
						array = c.get(0);
						key = c.get(2);
						value = c.get(3);
					}
				}
				if (key != null && key.getData() instanceof CLabel) {
					if (!(((CLabel) key.getData()).cVal() instanceof IVariable)
							&& !(((CLabel)key.getData()).cVal() instanceof CFunction
								&& ((CLabel)key.getData()).cVal().val().equals(ASSIGN))) {
						throw new ConfigCompileException("Expected a variable for key, but \"" + key.getData().val() + "\" was found", t);
					}
					key.setData(((CLabel) key.getData()).cVal());
				}
				// Now set up the new tree, and return that. Since foreachelse overrides us, we
				// need to accept all the arguments after the first, and put those in.
				List<ParseTree> newChildren = new ArrayList<>();
				newChildren.add(array);
				if (key != null) {
					newChildren.add(key);
				}
				newChildren.add(value);
				for (int i = 1; i < children.size(); i++) {
					newChildren.add(children.get(i));
				}
				children.clear();
				children.addAll(newChildren);
				// Change foreach(){ ... } else { ... } to a foreachelse.
				if (children.get(children.size() - 1).getData() instanceof CFunction
						&& children.get(children.size() - 1).getData().val().equals("else")) {
					ParseTree foreachelse = new ParseTree(new CFunction(new foreachelse().getName(), t), fileOptions);
					children.set(children.size() - 1, children.get(children.size() - 1).getChildAt(0));
					foreachelse.setChildren(children);
					return foreachelse;
				}
			}
			return null;
		}

	}

	@api
	@noboilerplate
	@breakable
	@seealso({foreach.class})
	public static class foreachelse extends foreach {

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			ParseTree array = nodes[0];
			//The last one
			ParseTree elseCode = nodes[nodes.length - 1];

			Construct data = parent.seval(array, env);

			if (!(data instanceof CArray) && !(data instanceof CSlice)) {
				throw new Exceptions.CastException(getName() + " expects an array for parameter 1", t);
			}

			if (((CArray) data).isEmpty()) {
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
			return "void {array, ivar, code, else} Works like a foreach, except if the array is empty, the else code runs instead. That is, if the code"
					+ " would not run at all, the else condition would. In general, brace syntax and use of foreach(){ } else { } syntax is preferred, instead"
					+ " of using foreachelse directly.";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
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
				+ ")"),};
		}

	}

	@api
	@noboilerplate
	@breakable
	public static class _while extends AbstractFunction {

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
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
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
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			try {
				while (Static.getBoolean(parent.seval(nodes[0], env))) {
					//We allow while(thing()); to be done. This makes certain
					//types of coding styles possible.
					if (nodes.length > 1) {
						try {
							parent.seval(nodes[1], env);
						} catch (LoopContinueException e) {
							//ok.
						}
					}
				}
			} catch (LoopBreakException e) {
				if (e.getTimes() > 1) {
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
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
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
				+ ")"),};
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

	}

	@api
	@noboilerplate
	@breakable
	public static class _dowhile extends AbstractFunction {

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
					+ " keeping track of any counters anyways. Breaking multiple times still works however. In general, using brace"
					+ " syntax is preferred: do { code(); } while(@condition); instead of using dowhile() directly.";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			try {
				do {
					try {
						parent.seval(nodes[0], env);
					} catch (LoopContinueException e) {
						//ok. No matter how many times it tells us to continue, we're only going to continue once.
					}
				} while (Static.getBoolean(parent.seval(nodes[1], env)));
			} catch (LoopBreakException e) {
				if (e.getTimes() > 1) {
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
			return "nothing {[int]} Stops the current loop. If int is specified, and is greater than 1, the break travels that many loops up. So, if you had"
					+ " a loop embedded in a loop, and you wanted to break in both loops, you would call break(2). If this function is called outside a loop"
					+ " (or the number specified would cause the break to travel up further than any loops are defined), the function will fail. If no"
					+ " argument is specified, it is the same as calling break(1). This function has special compilation rules. The break number"
					+ " must not be dynamic, or a compile error will occur. An integer must be hard coded into the function.";
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
			return CHVersion.V3_1_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			int num = 1;
			if (args.length == 1) {
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
				+ ")"),};
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children, FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			if (children.size() == 1) {
				if (children.get(0).isDynamic()) {
					//This is absolutely a bad design, if there is a variable here
					//in the break. Due to optimization, this is a compile error.
					throw new ConfigCompileException("The parameter sent to break() should"
							+ " be hard coded, and should not be dynamically determinable, since this is always a sign"
							+ " of loose code flow, which should be avoided.", t);
				}
				if(!(children.get(0).getData() instanceof CInt)){
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
			return "void {[int]} Skips the rest of the code in this loop, and starts the loop over, with it continuing at the next index. If this function"
					+ " is called outside of a loop, the command will fail. If int is set, it will skip 'int' repetitions. If no argument is specified,"
					+ " 1 is used.";
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
			return CHVersion.V3_1_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			int num = 1;
			if (args.length == 1) {
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
				+ "}"),};
		}
	}

	@api
	public static class is_stringable extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_stringable";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns whether or not the item is convertable to a string. Everything but arrays can be used as strings.";
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
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return CBoolean.get(!(args[0] instanceof CArray));
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
				new ExampleScript("True condition", "is_stringable('yes')"),
				new ExampleScript("True condition", "is_stringable(1) #This can be used as a string, yes"),
				new ExampleScript("False condition", "is_stringable(array(1))"),};
		}
	}

	@api
	public static class is_string extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_string";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns whether or not the item is actually a string datatype. If you just care if some data can be used as a string,"
					+ " use is_stringable().";
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
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return CBoolean.get(args[0] instanceof CString);
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
				new ExampleScript("True condition", "is_string('yes')"),
				new ExampleScript("False condition", "is_string(1) #is_stringable() would return true here"),};
		}
	}

	@api
	public static class is_bytearray extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_bytearray";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns whether or not the item is actually a ByteArray datatype.";
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
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return CBoolean.get(args[0] instanceof CByteArray);
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
				new ExampleScript("True condition", "is_bytearray(string_get_bytes('yay'))"),
				new ExampleScript("False condition", "is_bytearray('Nay')"),
				new ExampleScript("False condition", "is_bytearray(123)"),};
		}
	}

	@api
	public static class is_array extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_array";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns whether or not the item is an array";
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
		public CHVersion since() {
			return CHVersion.V3_1_2;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return CBoolean.get(args[0] instanceof CArray);
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
				new ExampleScript("True condition", "is_array(array(1))"),
				new ExampleScript("True condition", "is_array(array(one: 1))"),
				new ExampleScript("False condition", "is_array('no')"),};
		}
	}

	@api
	public static class is_double extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_double";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns whether or not the given item is a double. Note that numeric strings and integers"
					+ " can usually be used as a double, however this function checks the actual datatype of the item. If"
					+ " you just want to see if an item can be used as a number, use is_numeric() instead.";
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
		public CHVersion since() {
			return CHVersion.V3_1_2;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return CBoolean.get(args[0] instanceof CDouble);
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
				new ExampleScript("True condition", "is_double(1.0)"),
				new ExampleScript("False condition", "is_double(1)"),};
		}
	}

	@api
	public static class is_integer extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_integer";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns whether or not the given item is an integer. Note that numeric strings can usually be used as integers,"
					+ " however this function checks the actual datatype of the item. If you just want to see if an item can be used as a number,"
					+ " use is_integral() or is_numeric() instead.";
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
		public CHVersion since() {
			return CHVersion.V3_1_2;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return CBoolean.get(args[0] instanceof CInt);
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
				new ExampleScript("True condition", "is_integer(1)"),
				new ExampleScript("False condition", "is_integer(1.0)"),};
		}
	}

	@api
	public static class is_boolean extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_boolean";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns whether the given item is of the boolean datatype. Note that all datatypes can be used as booleans, however"
					+ " this function checks the specific datatype of the given item.";
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
		public CHVersion since() {
			return CHVersion.V3_1_2;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return CBoolean.get(args[0] instanceof CBoolean);
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
				new ExampleScript("True condition", "is_boolean(false)"),
				new ExampleScript("False condition", "is_boolean(0)"),};
		}
	}

	@api
	public static class is_null extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_null";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns whether or not the given item is null.";
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
		public CHVersion since() {
			return CHVersion.V3_1_2;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return CBoolean.get(args[0] instanceof CNull);
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
				new ExampleScript("True condition", "is_null(null)"),
				new ExampleScript("False condition", "is_null(0)"),};
		}
	}

	@api
	public static class is_numeric extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_numeric";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns false if the item would fail if it were used as a numeric value."
					+ " If it can be parsed or otherwise converted into a numeric value, true is returned.";
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
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			boolean b = true;
			try {
				Static.getNumber(args[0], t);
			} catch (ConfigRuntimeException e) {
				b = false;
			}
			return CBoolean.get(b);
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
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
				new ExampleScript("True condition", "is_numeric('1.0')"),
				new ExampleScript("True condition", "is_numeric('1')"),
				new ExampleScript("True condition", "is_numeric(1)"),
				new ExampleScript("True condition", "is_numeric(1.5)"),
				new ExampleScript("False condition", "is_numeric('string')"),
				new ExampleScript("True condition, because null is coerced to 0.0, which is numeric.", "is_numeric(null)"),};
		}
	}

	@api
	public static class is_integral extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_integral";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns true if the numeric value represented by "
					+ " a given double or numeric string could be cast to an integer"
					+ " without losing data (or if it's an integer). For instance,"
					+ " is_numeric(4.5) would return true, and integer(4.5) would work,"
					+ " however, equals(4.5, integer(4.5)) returns false, because the"
					+ " value was narrowed to 4.";
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
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			double d;
			try {
				d = Static.getDouble(args[0], t);
			} catch (ConfigRuntimeException e) {
				return CBoolean.FALSE;
			}
			return CBoolean.get((long) d == d);
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
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
				new ExampleScript("True condition", "is_integral(1.0)"),
				new ExampleScript("True condition", "is_integral(1)"),
				new ExampleScript("True condition", "is_integral('5.0')"),
				new ExampleScript("True condition", "is_integral('6')"),
				new ExampleScript("False condition", "is_integral(1.5)"),
				new ExampleScript("True condition, because null is coerced to 0, which is integral", "is_integral(null)"),};
		}
	}

	@api
	@unbreakable
	public static class proc extends AbstractFunction {

		@Override
		public String getName() {
			return "proc";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "void {[name], [ivar...], procCode} Creates a new user defined procedure (also known as \"function\") that can be called later in code. Please see the more detailed"
					+ " documentation on procedures for more information. In general, brace syntax and keyword usage is preferred:"
					+ " proc _myProc(@a, @b){ procCode(@a, @b); }";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException};
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
			return CHVersion.V3_1_3;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			Procedure myProc = getProcedure(t, env, parent, nodes);
			env.getEnv(GlobalEnv.class).GetProcs().put(myProc.getName(), myProc);
			return CVoid.VOID;
		}

		public static Procedure getProcedure(Target t, Environment env, Script parent, ParseTree... nodes) {
			String name = "";
			List<IVariable> vars = new ArrayList<>();
			ParseTree tree = null;
			List<String> varNames = new ArrayList<>();
			boolean usesAssign = false;
			CClassType returnType = CClassType.AUTO;
			if(nodes[0].getData() instanceof CClassType){
				returnType = (CClassType) nodes[0].getData();
				ParseTree[] newNodes = new ParseTree[nodes.length - 1];
				for(int i = 1; i < nodes.length; i++){
					newNodes[i - 1] = nodes[i];
				}
				nodes = newNodes;
			}
			// We have to restore the variable list once we're done
			IVariableList originalList = env.getEnv(GlobalEnv.class).GetVarList().clone();
			for (int i = 0; i < nodes.length; i++) {
				if (i == nodes.length - 1) {
					tree = nodes[i];
				} else {
					boolean thisNodeIsAssign = false;
					if (nodes[i].getData() instanceof CFunction) {
						if (((CFunction) nodes[i].getData()).getValue().equals("assign")) {
							thisNodeIsAssign = true;
							if ((nodes[i].getChildren().size() == 3 && nodes[i].getChildAt(0).getData().isDynamic())
								|| nodes[i].getChildAt(1).getData().isDynamic()) {
								usesAssign = true;
							}
						}
					}
					env.getEnv(GlobalEnv.class).SetFlag("no-check-duplicate-assign", true);
					Construct cons = parent.eval(nodes[i], env);
					env.getEnv(GlobalEnv.class).ClearFlag("no-check-duplicate-assign");
					if (i == 0 && cons instanceof IVariable) {
						throw new ConfigRuntimeException("Anonymous Procedures are not allowed", ExceptionType.InvalidProcedureException, t);
					} else {
						if (i == 0 && !(cons instanceof IVariable)) {
							name = cons.val();
						} else {
							if (!(cons instanceof IVariable)) {
								throw new ConfigRuntimeException("You must use IVariables as the arguments", ExceptionType.InvalidProcedureException, t);
							} else {
								IVariable ivar = null;
								try {
									Construct c = cons;
									if (c instanceof IVariable) {
										String varName = ((IVariable) c).getName();
										if (varNames.contains(varName)) {
											throw new ConfigRuntimeException("Same variable name defined twice in " + name, ExceptionType.InvalidProcedureException, t);
										}
										varNames.add(varName);
									}
									while (c instanceof IVariable) {
										c = env.getEnv(GlobalEnv.class).GetVarList().get(((IVariable) c).getName(), t, true).ival();
									}
									if (!thisNodeIsAssign) {
										//This is required because otherwise a default value that's already in the environment
										//would end up getting set to the existing value, thereby leaking in the global env
										//into this proc, if the call to the proc didn't have a value in this slot.
										c = new CString("", t);
									}
									ivar = new IVariable(((IVariable)cons).getDefinedType(), ((IVariable) cons).getName(), c.clone(), t);
								} catch (CloneNotSupportedException ex) {
									//
								}
								vars.add(ivar);
							}
						}
					}
				}
			}
			env.getEnv(GlobalEnv.class).SetVarList(originalList);
			Procedure myProc = new Procedure(name, returnType, vars, tree, t);
			if (usesAssign) {
				myProc.definitelyNotConstant();
			}
			return myProc;
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return CVoid.VOID;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		/**
		 * Returns either null to indicate that the procedure is not const, or
		 * returns a single Construct, which should replace the call to the
		 * procedure.
		 *
		 * @param t
		 * @param myProc
		 * @param children
		 * @return
		 * @throws ConfigRuntimeException
		 */
		public static Construct optimizeProcedure(Target t, Procedure myProc, List<ParseTree> children) throws ConfigRuntimeException {
			if (myProc.isPossiblyConstant()) {
				//Oooh, it's possibly constant. So, let's run it with our children.
				try {
					FileOptions options = new FileOptions(new HashMap<String, String>());
					if (!children.isEmpty()) {
						options = children.get(0).getFileOptions();
					}
					ParseTree root = new ParseTree(new CFunction("__autoconcat__", Target.UNKNOWN), options);
					Script fakeScript = Script.GenerateScript(root, PermissionsResolver.GLOBAL_PERMISSION);
					Environment env = Static.GenerateStandaloneEnvironment();
					env.getEnv(GlobalEnv.class).SetScript(fakeScript);
					Construct c = myProc.cexecute(children, env, t);
					//Yup! It worked. It's a const proc.
					return c;
				} catch (ConfigRuntimeException e) {
					if (e.getExceptionType() == ExceptionType.InvalidProcedureException) {
						//This is the only valid exception that doesn't strictly mean it's a bad
						//call.
						return null;
					}
					throw e; //Rethrow it. Since the functions are all static, and we actually are
					//running it with a mostly legit environment, this is a real runtime error,
					//and we can safely convert it to a compile error upstream
				} catch (Exception e) {
					//Nope. Something is preventing us from running it statically.
					//We don't really care. We just know it can't be optimized.
					return null;
				}
			} else {
				//Oh. Well, we tried.
				return null;
			}
		}

//        @Override
//        public boolean canOptimizeDynamic() {
//            return true;
//        }
//
//        @Override
//        public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
//            //We seriously lose out on the ability to optimize this procedure
//            //if we are assigning a dynamic value as a default, but we have to check
//            //that here. If we don't, we lose the information
//            return ;
//        }

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
			return "nothing {mixed} Returns the specified value from this procedure. It cannot be called outside a procedure.";
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
		public CHVersion since() {
			return CHVersion.V3_2_0;
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
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			Construct ret = (args.length == 1 ? args[0] : CVoid.VOID);
			throw new FunctionReturnException(ret, t);
		}
	}

	@api
	public static class include extends AbstractFunction /*implements Optimizable*/ {
		// Can't currently optimize this, because it depends on knowing whether or not
		// we are in cmdline mode, which is included with the environment, which doesn't
		// exist in optimizations yet.

		@Override
		public String getName() {
			return "include";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {path} Includes external code at the specified path.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.IncludeException};
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
			return true;
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return CVoid.VOID;
		}

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			ParseTree tree = nodes[0];
			Construct arg = parent.seval(tree, env);
			String location = arg.val();
			File file = Static.GetFileFromArgument(location, env, t, null);
			ParseTree include = IncludeCache.get(file, t);
			if(include != null){
				// It could be an empty file
				parent.eval(include.getChildAt(0), env);
			}
			return CVoid.VOID;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

//		@Override
//		public Set<OptimizationOption> optimizationOptions() {
//			return EnumSet.of(
//					OptimizationOption.OPTIMIZE_CONSTANT
//			);
//		}
//
//		@Override
//		public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
//			//We can't optimize per se, but if the path is constant, and the code is uncompilable, we
//			//can give a warning, and go ahead and cache the tree.
//			String path = args[0].val();
//			File file = Static.GetFileFromArgument(path, env, t, null);
//			IncludeCache.get(file, t);
//			return null;
//		}
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
			return "mixed {proc_name, [var1...]} Dynamically calls a user defined procedure. call_proc(_myProc, 'var1') is the equivalent of"
					+ " _myProc('var1'), except you could dynamically build the procedure name if need be. This is useful for dynamic coding,"
					+ " however, closures work best for callbacks. Throws an InvalidProcedureException if the procedure isn't defined. If you are"
					+ " hardcoding the first parameter, a warning will be issued, because it is much more efficient and safe to directly use"
					+ " a procedure if you know what its name is beforehand.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InvalidProcedureException};
		}

		@Override
		public boolean isRestricted() {
			return true;
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
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			if (args.length < 1) {
				throw new ConfigRuntimeException("Expecting at least one argument to " + getName(), ExceptionType.InsufficientArgumentsException, t);
			}
			Procedure proc = env.getEnv(GlobalEnv.class).GetProcs().get(args[0].val());
			if (proc != null) {
				List<Construct> vars = new ArrayList<Construct>(Arrays.asList(args));
				vars.remove(0);
				Environment newEnv = null;
				try {
					newEnv = env.clone();
				} catch (CloneNotSupportedException ex) {
					throw new RuntimeException(ex);
				}
				return proc.execute(vars, newEnv, t);
			}
			throw new ConfigRuntimeException("Unknown procedure \"" + args[0].val() + "\"",
					ExceptionType.InvalidProcedureException, t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children, FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			if (children.size() < 1) {
				throw new ConfigRuntimeException("Expecting at least one argument to " + getName(), ExceptionType.InsufficientArgumentsException, t);
			}
			if (children.get(0).isConst()) {
				CHLog.GetLogger().Log(CHLog.Tags.COMPILER, LogLevel.WARNING, "Hardcoding procedure name in " + getName() + ", which is inefficient."
						+ " Consider calling the procedure directly if the procedure name is known at compile time.", t);
			}
			return null;
		}

	}

	@api
	public static class call_proc_array extends call_proc {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CArray ca = Static.getArray(args[1], t);
			if (ca.inAssociativeMode()) {
				throw new Exceptions.CastException("Expected the array passed to " + getName() + " to be non-associative.", t);
			}
			Construct[] args2 = new Construct[(int) ca.size() + 1];
			args2[0] = args[0];
			for (int i = 1; i < args2.length; i++) {
				args2[i] = ca.get(i - 1, t);
			}
			return super.exec(t, environment, args2);
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InvalidProcedureException, ExceptionType.CastException};
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
			return "mixed {proc_name, array} Works like call_proc, but allows for variable or unknown number of arguments to be passed to"
					+ " a proc. The array parameter is \"flattened\", and call_proc is essentially called. If the array is associative, an"
					+ " exception is thrown.";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children, FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			//If they hardcode the name, that's fine, because the variables may just be the only thing that's variable.
			return null;
		}

	}

	@api(environments = CommandHelperEnvironment.class)
	public static class is_proc extends AbstractFunction {

		@Override
		public String getName() {
			return "is_proc";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {procName} Returns whether or not the given procName is currently defined, i.e. if calling this proc wouldn't"
					+ " throw an exception.";
		}

		@Override
		public ExceptionType[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return true;
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
		public Construct exec(Target t, Environment env, Construct... args) {
			return CBoolean.get(env.getEnv(GlobalEnv.class).GetProcs().get(args[0].val()) != null);
		}
	}

	@api
	public static class is_associative extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_associative";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {array} Returns whether or not the array is associative. If the parameter is not an array, throws a CastException.";
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
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			if (args[0] instanceof CArray) {
				return CBoolean.get(((CArray) args[0]).inAssociativeMode());
			} else {
				throw new ConfigRuntimeException(this.getName() + " expects argument 1 to be an array", ExceptionType.CastException, t);
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
				new ExampleScript("True condition", "is_associative(array(one: 1, two: 2))"),
				new ExampleScript("False condition", "is_associative(array(1, 2, 3))"),};
		}
	}

	@api
	public static class is_closure extends AbstractFunction {

		@Override
		public String getName() {
			return "is_closure";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {arg} Returns true if the argument is a closure (could be executed)"
					+ " or false otherwise";
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
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return CBoolean.get(args[0] instanceof CClosure);
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("True condition", "is_closure(closure(msg('code')))"),
				new ExampleScript("False condition", "is_closure('a string')"),};
		}
	}

	@api
	@seealso({_export.class})
	public static class _import extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "import";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "mixed {ivar | key} This function imports a value from the global value"
					+ " register. In the first mode, it looks for an ivariable with the specified"
					+ " name, and stores the value in the variable, and returns void. The first"
					+ " mode is deprecated, and should not be used. In the"
					+ " second mode, it looks for a value stored with the specified key, and"
					+ " returns that value. Items can be stored with the export function. If"
					+ " the specified ivar doesn't exist, the ivar will be assigned an empty"
					+ " string, and if the specified string key doesn't exist, null is returned."
					+ " See the documentation on [[CommandHelper/import-export|imports/exports]]"
					+ " for more information. import() is threadsafe.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
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
			if (args[0] instanceof IVariable) {
				//Mode 1
				IVariable var = (IVariable) args[0];
				environment.getEnv(GlobalEnv.class).GetVarList().set(Globals.GetGlobalIVar(var));
				return CVoid.VOID;
			} else {
				//Mode 2
				String key = GetNamespace(args, null, getName(), t);
				return Globals.GetGlobalConstruct(key);
			}
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new _export().examples();
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children, FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			if (children.size() > 2) {
				CHLog.GetLogger().w(CHLog.Tags.DEPRECATION, "Automatic creation of namespaces is deprecated, and WILL be removed in the future."
						+ " Use import('my.namespace') instead of import('my', 'namespace')", t);
			}
			if (children.get(0).getData() instanceof IVariable) {
				CHLog.GetLogger().w(CHLog.Tags.DEPRECATION, "import(@ivar) usage is deprecated. Please use the @ivar = import('custom.name') format,"
						+ " as this feature WILL be removed in the future.", t);
			}
			//Just a compiler warning
			return null;
		}

	}

	@api
	@seealso({_import.class})
	public static class _export extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "export";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "void {ivar | key, value} Stores a value in the global storage register."
					+ " When using the first mode, the ivariable is stored so it can be imported"
					+ " later, and when using the second mode, an arbitrary value is stored with"
					+ " the give key, and can be retreived using the secode mode of import. The first mode will"
					+ " be deprecated in future versions, so should be avoided. If"
					+ " the value is already stored, it is overwritten. See {{function|import}} and"
					+ " [[CommandHelper/import-export|importing/exporting]]. The reference to the value"
					+ " is stored, not a copy of the value, so in the case of arrays, manipulating the"
					+ " contents of the array will manipulate the stored value. export() is threadsafe.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InsufficientArgumentsException};
		}

		@Override
		public boolean isRestricted() {
			return true;
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
			if (args.length == 1) {
				if (args[0] instanceof IVariable) {
					IVariable cur = (IVariable) args[0];
					Globals.SetGlobal(environment.getEnv(GlobalEnv.class).GetVarList().get(cur.getName(), cur.getTarget()));
				} else {
					throw new ConfigRuntimeException("Expecting a IVariable when only one parameter is specified", ExceptionType.InsufficientArgumentsException, t);
				}
			} else {
				String key = GetNamespace(args, args.length - 1, getName(), t);
				Construct c = args[args.length - 1];
				//We want to store the value contained, not the ivar itself
				while (c instanceof IVariable) {
					c = environment.getEnv(GlobalEnv.class).GetVarList().get(((IVariable) c).getName(), t).ival();
				}
				Globals.SetGlobal(key, c);
			}
			return CVoid.VOID;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Deprecated usage", "@var = 2\n"
				+ "export(@var)\n"
				+ "@var = 0\n"
				+ "# In other code, perhaps inside a proc, or another execution unit\n"
				+ "import(@var)\n"
				+ "msg(@var)"),
				new ExampleScript("Preferred usage", "@var = 2\n"
				+ "export('custom.name', @var)\n"
				+ "@var2 = import('custom.name')\n"
				+ "msg(@var2)"),
				new ExampleScript("Storage of references", "@array = array(1, 2, 3)\n"
				+ "export('array', @array)\n"
				+ "@array[0] = 4\n"
				+ "@array2 = import('array')\n"
				+ "msg(@array2)")
			};
		}

		@Override
		public Set<Optimizable.OptimizationOption> optimizationOptions() {
			return EnumSet.of(Optimizable.OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children, FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			if (children.size() > 2) {
				CHLog.GetLogger().w(CHLog.Tags.DEPRECATION, "Automatic creation of namespaces is deprecated, and WILL be removed in the future."
						+ " Use export('my.namespace', @var) instead of export('my', 'namespace', @var)", t);
			}
			if (children.get(0).getData() instanceof IVariable) {
				CHLog.GetLogger().w(CHLog.Tags.DEPRECATION, "export(@ivar) usage is deprecated. Please use the export('custom.name', @ivar) format,"
						+ " as this feature WILL be removed in the future.", t);
			}
			//Just a compiler warning
			return null;
		}

	}

	@api(environments = CommandHelperEnvironment.class)
	@unbreakable
	public static class closure extends AbstractFunction {

		@Override
		public String getName() {
			return "closure";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "closure {[varNames...,] code} Returns a closure on the provided code. A closure is"
					+ " a datatype that represents some code as code, not the results of some"
					+ " code after it is run. Code placed in a closure can be used as"
					+ " a string, or executed by other functions using the eval() function."
					+ " If a closure is \"to string'd\" it will not necessarily look like"
					+ " the original code, but will be functionally equivalent. The current environment"
					+ " is \"snapshotted\" and stored with the closure, however, this information is"
					+ " only stored in memory, it isn't retained during a serialization operation."
					+ " Also, the special variable @arguments is automatically created for you, and contains"
					+ " an array of all the arguments passed to the closure, much like procedures."
					+ " See the wiki article on [[CommandHelper/Staged/Closures|closures]] for more details"
					+ " and examples.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public boolean preResolveVariables() {
			return false;
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
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			if (nodes.length == 0) {
				//Empty closure, do nothing.
				return new CClosure(null, env, CClassType.AUTO, new String[]{}, new Construct[]{}, new CClassType[]{}, t);
			}
			// Handle the closure type first thing
			CClassType returnType = CClassType.AUTO;
			if(nodes[0].getData() instanceof CClassType){
				returnType = (CClassType) nodes[0].getData();
				ParseTree[] newNodes = new ParseTree[nodes.length - 1];
				for(int i = 1; i < nodes.length; i++){
					newNodes[i - 1] = nodes[i];
				}
				nodes = newNodes;
			}
			String[] names = new String[nodes.length - 1];
			Construct[] defaults = new Construct[nodes.length - 1];
			CClassType[] types = new CClassType[nodes.length - 1];
			// We clone the enviornment at this point, because we don't want the values
			// that are assigned here to overwrite values in the main scope.
			Environment myEnv;
			try {
				myEnv = env.clone();
			} catch (CloneNotSupportedException ex) {
				myEnv = env;
			}
			for (int i = 0; i < nodes.length - 1; i++) {
				ParseTree node = nodes[i];
				ParseTree newNode = new ParseTree(new CFunction("g", t), node.getFileOptions());
				List<ParseTree> children = new ArrayList<>();
				children.add(node);
				newNode.setChildren(children);
				Script fakeScript = Script.GenerateScript(newNode, myEnv.getEnv(GlobalEnv.class).GetLabel());
				myEnv.getEnv(GlobalEnv.class).SetFlag("closure-warn-overwrite", true);
				Construct ret = MethodScriptCompiler.execute(newNode, myEnv, null, fakeScript);
				myEnv.getEnv(GlobalEnv.class).ClearFlag("closure-warn-overwrite");
				if (!(ret instanceof IVariable)) {
					throw new ConfigRuntimeException("Arguments sent to " + getName() + " barring the last) must be ivariables", ExceptionType.CastException, t);
				}
				names[i] = ((IVariable) ret).getName();
				try {
					defaults[i] = ((IVariable) ret).ival().clone();
					types[i] = ((IVariable)ret).getDefinedType();
				} catch (CloneNotSupportedException ex) {
					Logger.getLogger(DataHandling.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			CClosure closure = new CClosure(nodes[nodes.length - 1], myEnv, returnType, names, defaults, types, t);
			return closure;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Creates a closure", "closure(){\n"
						+ "\tmsg('Hello World!');\n"
						+ "};"),
				new ExampleScript("Executes a closure", "execute(closure(){\n"
						+ "\tmsg('Hello World!');"
						+ "});")
			};
		}

	}

	@api
	@hide("Until the Federation system is finished, this is hidden")
	@unbreakable
	@nolinking
	public static class rclosure extends closure {

		@Override
		public String getName() {
			return "rclosure";
		}

		@Override
		public String docs() {
			return "closure {[varNames...], code} Returns a non-linking closure on the provided code. The same rules apply"
					+ " for closures, except the top level internal code does not check for proper linking at compile time,"
					+ " and instead links at runtime. Lexer errors and some other compile time checks ARE done however, but"
					+ " functions are not optimized or linked. This is used for remote code execution, since the remote platform"
					+ " may have some functionality unavailable on this current platform.";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class execute extends AbstractFunction {

		@Override
		public String getName() {
			return "execute";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "mixed {[values...,] closure} Executes the given closure. You can also send arguments"
					+ " to the closure, which it may or may not use, depending on the particular closure's"
					+ " definition. If the closure returns a value with return(), then that value will"
					+ " be returned with execute. Otherwise, void is returned.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
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
			if (args[args.length - 1] instanceof CClosure) {
				Construct[] vals = new Construct[args.length - 1];
				System.arraycopy(args, 0, vals, 0, args.length - 1);
				CClosure closure = (CClosure) args[args.length - 1];
				try {
					closure.execute(vals);
				} catch (FunctionReturnException e) {
					return e.getReturn();
				}
			} else {
				throw new ConfigRuntimeException("Only a closure (created from the closure function) can be sent to execute()", ExceptionType.CastException, t);
			}
			return CVoid.VOID;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class _boolean extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "boolean";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns a new construct that has been cast to a boolean. The item is cast according to"
					+ " the boolean conversion rules. Since all data types can be cast to a"
					+ " a boolean, this function will never throw an exception.";
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
			return CBoolean.get(Static.getBoolean(args[0]));
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
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
				new ExampleScript("Basic usage", "boolean(1)"),
				new ExampleScript("Basic usage", "boolean(0)"),
				new ExampleScript("Basic usage", "boolean(array(1))"),
				new ExampleScript("Basic usage", "boolean(array())"),
				new ExampleScript("Basic usage", "boolean(null)"),
				new ExampleScript("Basic usage", "boolean('string')"),
				new ExampleScript("Basic usage", "boolean('')"),};
		}
	}

	@api
	public static class _integer extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "integer";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "integer {item} Returns a new construct that has been cast to an integer."
					+ " This function will throw a CastException if is_numeric would return"
					+ " false for this item, but otherwise, it will be cast properly. Data"
					+ " may be lost in this conversion. For instance, 4.5 will be converted"
					+ " to 4, by using integer truncation. You can use is_integral to see"
					+ " if this data loss would occur.";
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
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CInt((long) Static.getDouble(args[0], t), t);
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
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
				new ExampleScript("Basic usage", "integer(1.0)"),
				new ExampleScript("Basic usage", "integer(1.5)"),
				new ExampleScript("Failure", "assign(@var, 'string')\ninteger(@var)"),};
		}
	}

	@api
	public static class _double extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "double";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "double {item} Returns a new construct that has been cast to an double."
					+ " This function will throw a CastException if is_numeric would return"
					+ " false for this item, but otherwise, it will be cast properly.";
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
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CDouble(Static.getDouble(args[0], t), t);
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
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
				new ExampleScript("Basic usage", "double(1)"),
				new ExampleScript("Failure", "@var = 'string';\ndouble(@var);"),};
		}
	}

	@api
	public static class _string extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "string";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {item} Creates a new construct that is the \"toString\" of an item."
					+ " For arrays, an human readable version is returned; this should not be"
					+ " used directly, as the format is not guaranteed to remain consistent. Booleans return \"true\""
					+ " or \"false\" and null returns \"null\".";
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
			return new CString(args[0].val(), t);
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
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
				new ExampleScript("Basic usage", "string(1)"),
				new ExampleScript("Basic usage", "string(true)"),
				new ExampleScript("Basic usage", "string(false)"),
				new ExampleScript("Basic usage", "string(null)"),
				new ExampleScript("Basic usage", "string(array(1, 2))"),
				new ExampleScript("Basic usage", "string(array(one: 'one', two: 'two'))"),};
		}
	}

	@api
	@seealso(parse_int.class)
	public static class to_radix extends AbstractFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.RangeException, ExceptionType.FormatException};
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
			int radix = Static.getInt32(args[1], t);
			if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
				throw new Exceptions.RangeException("The radix must be between " + Character.MIN_RADIX + " and " + Character.MAX_RADIX + ", inclusive.", t);
			}
			return new CString(Long.toString(Static.getInt(args[0], t), radix), t);
		}

		@Override
		public String getName() {
			return "to_radix";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "string {value, radix} Given an int and a radix, returns a string representation of the integer value"
					+ " in the given base. A common use would be to output a hex or binary representation of a number, for"
					+ " instance. ---- It is useful to note that all integers are stored internally by the computer as binary,"
					+ " but since we usually represent numbers in text as base 10 numbers, we often times forget that both"
					+ " base 16 'F' and base 10 '15' and base 2 '1111' are actually the same number, just represented differently"
					+ " as strings in different bases. This doesn't change how the program behaves, since the base is just a way to represent"
					+ " the number on paper. The 'radix' is the base. So, given to_radix(10, 10), that would return '10', because"
					+ " in code, we wrote out our value '10' in base 10, and we convert it to base 10, so nothing changes. However,"
					+ " if we write to_radix(15, 16) we are saying \"convert the base 10 value 15 to base 16\", so it returns 'F'."
					+ " See {{function|parse_int}} for the opposite operation. The radix must be between " + Character.MIN_RADIX + " and "
					+ Character.MAX_RADIX + ", inclusive, or a range exception is thrown. This is because there are only " + Character.MAX_RADIX
					+ " characters that are normally used to represent different base numbers (that is, 0-9, a-z). The minimum radix is "
					+ Character.MIN_RADIX + ", because it is impossible to represent any numbers with out at least a binary base.";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("To a hex string", "to_radix(15, 16)"),
				new ExampleScript("To a binary string", "to_radix(15, 2)"),
				new ExampleScript("Using hex value in source", "to_radix(0xff, 16)"),
				new ExampleScript("Using binary value in source", "to_radix(0b10101010, 2)")
			};
		}

	}

	@api
	@seealso(to_radix.class)
	public static class parse_int extends AbstractFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.RangeException, ExceptionType.FormatException};
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
			String value = args[0].val();
			int radix = Static.getInt32(args[1], t);
			if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
				throw new Exceptions.RangeException("The radix must be between " + Character.MIN_RADIX + " and " + Character.MAX_RADIX + ", inclusive.", t);
			}
			long ret;
			try {
				ret = Long.parseLong(value, radix);
			} catch (NumberFormatException ex) {
				throw new Exceptions.FormatException("The input string: \"" + value + "\" is improperly formatted. (Perhaps you're using a character greater than"
						+ " the radix specified?)", t);
			}
			return new CInt(ret, t);
		}

		@Override
		public String getName() {
			return "parse_int";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "int {value, radix} Converts a string representation of an integer to a real integer, given the value's"
					+ " radix (base). See {{function|to_radix}} for a more detailed explanation of number theory. Radix must be"
					+ " between " + Character.MIN_RADIX + " and " + Character.MAX_RADIX + ", inclusive.";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("From hex string", "parse_int('F', 16)"),
				new ExampleScript("From binary string", "parse_int('1111', 2)")
			};
		}
	}

	/**
	 * Generates the namespace for this value, given an array of constructs. If
	 * the entire list of arguments isn't supposed to be part of the namespace,
	 * the value to be excluded may be specified.
	 *
	 * @param args
	 * @param exclude
	 * @return
	 */
	private static String GetNamespace(Construct[] args, Integer exclude, String name, Target t) {
		if (exclude != null && args.length < 2 || exclude == null && args.length < 1) {
			throw new ConfigRuntimeException(name + " was not provided with enough arguments. Check the documentation, and try again.", ExceptionType.InsufficientArgumentsException, t);
		}
		boolean first = true;
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			if (exclude != null && exclude == i) {
				continue;
			}
			if (!first) {
				b.append(".");
			}
			first = false;
			b.append(args[i].val());
		}
		return b.toString();
	}

	@api
	public static class typeof extends AbstractFunction implements Optimizable {

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
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			try {
				return new CClassType(args[0].typeof(), t);
			} catch (IllegalArgumentException ex) {
				throw new Error("Class " + args[0].getClass().getName() + " is not annotated with @typeof. Please report this"
						+ " error to the developers.");
			}
		}

		@Override
		public String getName() {
			return "typeof";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {arg} Returns a string value of the typeof a value. For instance 'array' is returned"
					+ " for typeof(array()). This is a generic replacement for the is_* series of functions.";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage, typeof string", "typeof('value')"),
				new ExampleScript("Basic usage, typeof int", "typeof(1)"),
				new ExampleScript("Basic usage, typeof double", "typeof(1.0)"),
				new ExampleScript("Basic usage, typeof closure", "typeof(closure(){ msg('test') })"),};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.CONSTANT_OFFLINE);
		}

	}

	@api
	public static class eval extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "eval";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {script_string} Executes arbitrary MethodScript. Note that this function is very experimental, and is subject to changing or "
					+ "removal.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_1_0;
		}

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			boolean oldDynamicScriptMode = env.getEnv(GlobalEnv.class).GetDynamicScriptingMode();
			ParseTree node = nodes[0];
			try {
				env.getEnv(GlobalEnv.class).SetDynamicScriptingMode(true);
				Construct script = parent.seval(node, env);
				if(script instanceof CClosure){
					throw new Exceptions.CastException("Closures cannot be eval'd directly. Use execute() instead.", t);
				}
				ParseTree root = MethodScriptCompiler.compile(MethodScriptCompiler.lex(script.val(), t.file(), true));
				StringBuilder b = new StringBuilder();
				int count = 0;
				for (ParseTree child : root.getChildren()) {
					Construct s = parent.seval(child, env);
					if (!s.val().trim().isEmpty()) {
						if (count > 0) {
							b.append(" ");
						}
						b.append(s.val());
					}
					count++;
				}
				return new CString(b.toString(), t);
			} catch (ConfigCompileException e) {
				throw new ConfigRuntimeException("Could not compile eval'd code: " + e.getMessage(), ExceptionType.FormatException, t);
			} catch(ConfigCompileGroupException ex){
				StringBuilder b = new StringBuilder();
				b.append("Could not compile eval'd code: ");
				for(ConfigCompileException e : ex.getList()){
					b.append(e.getMessage()).append("\n");
				}
				throw new ConfigRuntimeException(b.toString(), ExceptionType.FormatException, t);
			} finally {
				env.getEnv(GlobalEnv.class).SetDynamicScriptingMode(oldDynamicScriptMode);
			}
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			return CVoid.VOID;
		}
		//Doesn't matter, run out of state anyways

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public Set<Optimizable.OptimizationOption> optimizationOptions() {
			return EnumSet.of(Optimizable.OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children, FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() != 1){
				throw new ConfigCompileException(getName() + " expects only one argument", t);
			}
			if(children.get(0).isConst()){
				CHLog.GetLogger().Log(CHLog.Tags.COMPILER, LogLevel.WARNING, "Eval'd code is hardcoded, consider simply using the code directly, as wrapping"
						+ " hardcoded code in " + getName() + " is much less efficient.", t);
			}
			return null;
		}

	}

	@api
	@noprofile
	@hide("This will eventually be replaced by ; statements.")
	public static class g extends AbstractFunction {

		@Override
		public String getName() {
			return "g";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			for (int i = 0; i < args.length; i++) {
				args[i].val();
			}
			return CVoid.VOID;
		}

		@Override
		public String docs() {
			return "string {func1, [func2...]} Groups any number of functions together, and returns void. ";
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
			return CHVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}
	}

	/**
	 * For now, this feature works as is. However, I'm debating on whether or not I should just override assign() instead.
	 * The only issue with this is that if assign is overwritten, then a mutable_primitive will be "stuck" in the variable.
	 * So if later you wanted to make a value not a mutable primitive, there would be no way to do so. Another method could
	 * be introduced to "clear" the value out, but then there would be no way to tell if the value were actually mutable or
	 * not, so a third function would have to be added. The other point of concern is how to handle typeof() for a CMutablePrimitive.
	 * Should it return the underlying type, or mutable_primitive? If assignments are "sticky", then it would make sense to have
	 * it return the underlying type, but there's an issue with that, because then typeof wouldn't be useable for debug type
	 * situations. Given all these potential issues, it is still hidden, but available for experimental cases.
	 */
	@api
	@hide("This is still experimental")
	public static class mutable_primitive extends AbstractFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException};
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
			Construct val = CNull.NULL;
			if(args.length > 0){
				val = args[0];
			}
			return new CMutablePrimitive(val, t);
		}

		@Override
		public String getName() {
			return "mutable_primitive";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "mutable_primitive {[primitive_value]} Creates a mutable primitive object, initially setting the value of the object to"
					+ " null, or the specified value. The value must be a primitive value, and cannot be an array or object. ----"
					+ " The underlying primitive value is used in all cases where a value can be inferred. In all other cases, you must convert"
					+ " the primitive to the desired type, e.g. double(@mutable_primitive). Mutable primitives work like an array as well,"
					+ " in some cases, but not others. In general, setting of the underlying values may be done with array_push(). Assigning"
					+ " a new value to the variable works the same as assigning a new value to any other value, it overwrites the value with"
					+ " the new type. Most array functions will work with the mutable primitive, however, they will return useless data, for"
					+ " instance, array_resize() will simply set the value to the default value shown. array_size() is an exception to this"
					+ " rule, it will not work, and will throw an exception. See the examples for more use cases. In general, this is meant"
					+ " as a convenience feature for values that are passed to closures or procs, but should be passed by reference. Cloning the"
					+ " mutable primitive with the array clone operation creates a distinct copy.";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "@val = mutable_primitive(0);\n"
						+ "msg('typeof: ' . typeof(@val));\n"
						+ "msg('value: ' . @val);\n"
						+ "msg('@val + 5: ' . (@val + 5)); // Works as if it were a primitive with most functions\n"
						+ "(++@val); // As a special exception to how assignments work, increment/decrement works as well\n"
						+ "msg(@val); // 1\n"),
				new ExampleScript("Basic usage with procs", "proc(_testWithMutable, @a){\n"
						+ "\t@a[] = 5;\n"
						+ "}\n\n"
						+ ""
						+ "proc(_testWithoutMutable, @a){\n"
						+ "\t@a = 10;\n"
						+ "}\n\n"
						+ ""
						+ "@a = mutable_primitive(0);\n"
						+ "msg(@a); // The value starts out as 0\n"
						+ "_testWithMutable(@a); // This will actually change the value\n"
						+ "msg(@a); // Here, the value is 5\n"
						+ "_testWithoutMutable(@a); // This will not change the value\n"
						+ "msg(@a); // Still teh value is 5\n"),
				new ExampleScript("Basic usage with closure", "@a = mutable_primitive(0);\n"
						+ "execute(closure(){\n"
						+ "\t@a++;\n"
						+ "});\n"
						+ "msg(@a); // 1\n"),
				new ExampleScript("Cloning the value", "@a = mutable_primitive(0);\n"
						+ "@b = @a[];\n"
						+ "@a[] = 5;\n"
						+ "msg(@a);\n"
						+ "msg(@b);\n")
			};
		}



	}

	@api
	public static class _instanceof extends AbstractFunction implements Optimizable {

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
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if(args[0] instanceof CNull){
				return CBoolean.FALSE;
			}
			boolean b = InstanceofUtil.isInstanceof(args[0], args[1].val());
			return CBoolean.get(b);
		}

		@Override
		public String getName() {
			return "instanceof";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {value, type} Checks to see if the value is, extends, or implements the given type. Keyword usage is preferred:"
					+ " @value instanceof int ---- Null is a special value, while any type may be assigned null, it does not extend"
					+ " any type, and therefore \"null instanceof AnyType\" will always return false. Likewise, other than null, all"
					+ " values extend \"mixed\", and therefore \"anyNonNullValue instanceof mixed\" will always return true.";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children, FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			// There are two specific cases here where we will give more precise error messages.
			// If it's a string, yell at them
			if(children.get(1).getData() instanceof CString){
				throw new ConfigCompileException("Unexpected string type passed to \"instanceof\"", t);
			}
			// If it's a variable, also yell at them
			if(children.get(1).getData() instanceof IVariable){
				throw new ConfigCompileException("Variable types are not allowed in \"instanceof\"", t);
			}
			// Unknown error, but this is still never valid.
			if(!(children.get(1).getData() instanceof CClassType)){
				throw new ConfigCompileException("Unexpected type for \"instanceof\": " + children.get(1).getData(), t);
			}
			// null is technically a type, but instanceof shouldn't work with that
			if(children.get(1).getData().val().equals("null")){
				throw new ConfigCompileException("\"null\" cannot be compared against with instanceof", t);
			}
			// It's hardcoded, allow it, but optimize it out.
			if(children.get(0).isConst()){
				return new ParseTree(exec(t, null, children.get(0).getData(), children.get(1).getData()), fileOptions);
			}
			return null;
		}

	}

}
