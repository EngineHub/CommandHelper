package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.core.*;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.*;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Layton
 */
public class DataHandling {
	
	private static final String array_get = new ArrayHandling.array_get().getName();
	private static final String array_set = new ArrayHandling.array_set().getName();
	private static final String array_push = new ArrayHandling.array_push().getName();

	public static String docs() {
		return "This class provides various methods to control script data and program flow.";
	}

	@api
	public static class array extends AbstractFunction {

		public String getName() {
			return "array";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			return new CArray(t, args);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public String docs() {
			return "array {[var1, [var2...]]} Creates an array of values.";
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
//        @Override
//        public boolean canOptimize() {
//            //FALSE. Can't optimize, because this returns a reference. This is
//            //a much more complicated issue. TODO
//            return false;
//        }
//
//        @Override
//        public Construct optimize(Target t, Construct... args) {
//            return exec(t, null, args);
//        }
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "assign(@array, array(1, 2, 3))\nmsg(@array)"),
				new ExampleScript("Associative array creation", "assign(@array, array(one: 'apple', two: 'banana'))\nmsg(@array)"),
			};
		}
	}
	
	@api
	public static class associative_array extends AbstractFunction{

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
			CArray array = CArray.GetAssociativeArray(t, args);
			return array;
		}

		public String getName() {
			return "associative_array";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public String docs() {
			return "array {[args...]} Works exactly like array(), except the array created will be an associative array, even"
					+ " if the array has been created with no elements. This is the only use case where this is neccessary, vs"
					+ " using the normal array() function, or in the case where you assign sequential keys anyways, and the same"
					+ " array could have been created using array().";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Usage with an empty array", "assign(@array, associative_array())\nmsg(is_associative(@array))"),
				new ExampleScript("Usage with an array with sequential keys", "assign(@array, array(0: '0', 1: '1'))\nmsg(is_associative(@array))\n"
					+ "assign(@array, associative_array(0: '0', 1: '1'))\nmsg(is_associative(@array))"),
			};
		}
		
		
		
	}

	@api
	public static class assign extends AbstractFunction implements Optimizable {

		public String getName() {
			return "assign";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			Construct c = args[1];
			while (c instanceof IVariable) {
				IVariable cur = (IVariable) c;
				c = env.getEnv(GlobalEnv.class).GetVarList().get(cur.getName(), cur.getTarget()).ival();
			}
			if (args[0] instanceof IVariable) {
				IVariable v = new IVariable(((IVariable) args[0]).getName(), c, t);
				env.getEnv(GlobalEnv.class).GetVarList().set(v);
				return v;
			}
			throw new ConfigRuntimeException("assign only accepts an ivariable or array reference as the first argument", ExceptionType.CastException, t);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "ivariable {ivar, mixed} Accepts an ivariable ivar as a parameter, and puts the specified value mixed in it. Returns the variable that was assigned.";
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
			if (!(args[0] instanceof IVariable)) {
				throw new ConfigCompileException("Expecting an ivar for argument 1 to assign", t);
			}
			return null;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			if(children.get(0).getData() instanceof IVariable
					&& children.get(1).getData() instanceof IVariable){
				if(((IVariable)children.get(0).getData()).getName().equals(
						((IVariable)children.get(1).getData()).getName())){
					CHLog.GetLogger().Log(CHLog.Tags.COMPILER, LogLevel.WARNING, "Assigning a variable to itself", t);
				}
			}
			if(children.get(0).getData() instanceof CFunction && array_get.equals(children.get(0).getData().val())){
				if(children.get(0).getChildAt(1).getData() instanceof CSlice){
					CSlice cs = (CSlice) children.get(0).getChildAt(1).getData();
					if(cs.getStart() == 0 && cs.getFinish() == -1){
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
				new ExampleScript("Basic usage", "assign(@variable, 5)\nmsg(@variable)"),
				new ExampleScript("Array assignment", "assign(@variable['associative'], 5) #This creates the array for us\nmsg(@variable)"),
			};
		}
	}

	@api
	@noboilerplate
	public static class _for extends AbstractFunction implements Optimizable {

		public String getName() {
			return "for";
		}

		public Integer[] numArgs() {
			return new Integer[]{4};
		}

		public Construct exec(Target t, Environment env, Construct... args) {
			return new CVoid(t);
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			return new forelse(true).execs(t, env, parent, nodes);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "void {assign, condition, expression1, expression2} Acts as a typical for loop. The assignment is first run. Then, a"
					+ " condition is checked. If that condition is checked and returns true, expression2 is run. After that, expression1 is run. In java"
					+ " syntax, this would be: for(assign; condition; expression1){expression2}. assign must be an ivariable, either a "
					+ "pre defined one, or the results of the assign() function. condition must be a boolean.";
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_0_1;
		}
		//Doesn't matter, run out of state

		public Boolean runAsync() {
			return null;
		}

		@Override
		public boolean allowBraces() {
			return true;
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "for(assign(@i, 0), @i < 5, @i++,\n\tmsg(@i)\n)"),
				new ExampleScript("With braces", "for(assign(@i, 0), @i < 2, @i++){\n\tmsg(@i)\n}"),
				new ExampleScript("With continue. (See continue() for more examples)", "for(assign(@i, 0), @i < 2, @i++){\n"
					+ "\tif(@i == 1, continue())\n"
					+ "\tmsg(@i)\n"
					+ "}"),
			};
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
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			//In for(@i = 0, @i < @x, @i++, ...), the @i++ is more optimally written as ++@i, but
			//it is commonplace to use postfix operations, so if the condition is in fact that simple,
			//let's reverse it.
			boolean isInc;
			try{
				if(children.get(2).getData() instanceof CFunction &&
						((isInc = children.get(2).getData().val().equals("postinc"))
						|| children.get(2).getData().val().equals("postdec"))
						&& children.get(2).getChildAt(0).getData() instanceof IVariable){
					ParseTree pre = new ParseTree(new CFunction(isInc?"inc":"dec", t), children.get(2).getFileOptions());
					pre.addChild(children.get(2).getChildAt(0));
					children.set(2, pre);
				}
			} catch(IndexOutOfBoundsException e){
				//Just ignore it. It's a compile error, but we'll let the rest of the
				//existing system sort that out.
			}
			
			return null;
		}
		

		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}
				
	}
	
	@api
	@noboilerplate
	public static class forelse extends AbstractFunction{
		
		public forelse(){ }
		
		boolean runAsFor = false;
		forelse(boolean runAsFor){
			this.runAsFor = runAsFor;
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
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
			if(!runAsFor){
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
					return new CVoid(t);
				} catch (LoopContinueException e) {
					_continue = e.getTimes() - 1;
					parent.eval(expression, env);
					continue;
				}
				parent.eval(expression, env);
			}
			if(!hasRunOnce && !runAsFor && elseCode != null){
				parent.eval(elseCode, env);
			}
			return new CVoid(t);
		}

		public String getName() {
			return "forelse";
		}

		public Integer[] numArgs() {
			return new Integer[]{5};
		}

		public String docs() {
			return "void {assign, condition, expression1, expression2, else} Works like a normal for, but if upon checking the condition the first time,"
					+ " it is determined that it is false (that is, NO code loops are going to be run) the else code is run instead. If the loop runs,"
					+ " even once, it will NOT run the else branch.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}

	@api(environments=CommandHelperEnvironment.class)
	public static class foreach extends AbstractFunction {

		public String getName() {
			return "foreach";
		}

		public Integer[] numArgs() {
			return new Integer[]{3, 4};
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			return new CVoid(t);
		}

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			ParseTree array = nodes[0];
			ParseTree key = null;
			int offset = 0;
			if(nodes.length == 4){
				//Key and value provided
				key = nodes[1];
				offset = 1;
			}
			ParseTree value = nodes[1 + offset];
			ParseTree code = nodes[2 + offset];
			Construct arr = parent.seval(array, env);
			Construct ik = null;
			if(key != null){
				ik = parent.eval(key, env);
				if(!(ik instanceof IVariable)){
					throw new ConfigRuntimeException("Parameter 2 of " + getName() + " must be an ivariable", ExceptionType.CastException, t);
				}
			}
			Construct iv = parent.eval(value, env);
			if (arr instanceof CSlice) {
				long start = ((CSlice) arr).getStart();
				long finish = ((CSlice) arr).getFinish();
				if (finish < start) {
					throw new ConfigRuntimeException("When using the .. notation, the left number may not be greater than the right number. Recieved " + start + " and " + finish, ExceptionType.RangeException, t);
				}
				arr = new ArrayHandling.range().exec(t, env, new CInt(start, t), new CInt(finish + 1, t));
			}
			if (arr instanceof CArray) {
				if (iv instanceof IVariable) {
					CArray one = (CArray) arr;
					IVariable kkey = (IVariable) ik;
					IVariable two = (IVariable) iv;
					if (!one.inAssociativeMode()) {
						for (int i = 0; i < one.size(); i++) {
							if(kkey != null){
								env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(kkey.getName(), new CInt(i, t), t));
							}
							env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(two.getName(), one.get(i, t), t));
							try {
								parent.eval(code, env);
							} catch (LoopBreakException e) {
								int num = e.getTimes();
								if (num > 1) {
									e.setTimes(--num);
									throw e;
								}
								return new CVoid(t);
							} catch (LoopContinueException e) {
								i += e.getTimes() - 1;
								continue;
							}
						}
					} else {
						for (int i = 0; i < one.size(); i++) {
							String index = one.keySet().toArray(new String[]{})[i];
							if(kkey != null){
								env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(kkey.getName(), new CString(index, t), t));
							}
							env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(two.getName(), one.get(index, t), t));
							try {
								parent.eval(code, env);
							} catch (LoopBreakException e) {
								int num = e.getTimes();
								if (num > 1) {
									e.setTimes(--num);
									throw e;
								}
								return new CVoid(t);
							} catch (LoopContinueException e) {
								i += e.getTimes() - 1;
								continue;
							}
						}
					}
				} else {
					throw new ConfigRuntimeException("Parameter " + (2 +offset) + " of " + getName() + " must be an ivariable", ExceptionType.CastException, t);
				}
			} else {
				throw new ConfigRuntimeException("Parameter 1 of " + getName() + " must be an array", ExceptionType.CastException, t);
			}
			return new CVoid(t);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.RangeException};
		}

		public String docs() {
			return "void {array, [key], ivar, code} Walks through array, setting ivar equal to each element in the array, then running code."
					+ " In addition, foreach(1..4, @i, code()) is also valid, setting @i to 1, 2, 3, 4 each time. The same syntax is valid as"
					+ " in an array slice. If key is set (it must be an ivariable) then the index of each iteration will be set to that."
					+ " See the examples for a demonstration.";
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_0_1;
		}
		//Doesn't matter, runs out of state anyways

		public Boolean runAsync() {
			return null;
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
				new ExampleScript("Basic usage", "assign(@array, array(1, 2, 3))\nforeach(@array, @i,\n\tmsg(@i)\n)"),
				new ExampleScript("With braces", "assign(@array, array(1, 2, 3))\nforeach(@array, @i){\n\tmsg(@i)\n}"),
				new ExampleScript("With a slice", "foreach(1..3, @i){\n\tmsg(@i)\n}"),				
				new ExampleScript("With a keys", "@array = array('one': 1, 'two': 2)\nforeach(@array, @key, @value){\n\tmsg(@key.':'.@value)\n}"),
			};
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
	}
	
	@api
	@noboilerplate
	public static class foreachelse extends foreach{

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			ParseTree array = nodes[0];
			//The last one
			ParseTree elseCode = nodes[nodes.length - 1];

			Construct data = parent.seval(array, env);
			
			if(!(data instanceof CArray) && !(data instanceof CSlice)){
				throw new Exceptions.CastException(getName() + " expects an array for parameter 1", t);
			}
			
			if(((CArray)data).isEmpty()){
				parent.eval(elseCode, env);
			} else {
				ParseTree pass [] = new ParseTree[nodes.length - 1];
				System.arraycopy(nodes, 0, pass, 0, nodes.length - 1);
				nodes[0] = new ParseTree(data, null);
				return super.execs(t, env, parent, pass);
			}

			return new CVoid(t);
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
					+ " would not run at all, the else condition would.";
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
					+ ")"),
			};
		}
		
		
	}

	@api
	@noboilerplate
	public static class _while extends AbstractFunction {

		public String getName() {
			return "while";
		}

		public String docs() {
			return "void {condition, code} While the condition is true, the code is executed. break and continue work"
					+ " inside a dowhile, but continuing more than once is pointless, since the loop isn't inherently"
					+ " keeping track of any counters anyways. Breaking multiple times still works however.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
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
			try {
				while (Static.getBoolean(parent.seval(nodes[0], env))) {
					try {
						parent.seval(nodes[1], env);
					} catch (LoopContinueException e) {
						//ok.
					}
				}
			} catch (LoopBreakException e) {
				if (e.getTimes() > 1) {
					throw new LoopBreakException(e.getTimes() - 1, t);
				}
			}
			return new CVoid(t);
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CNull();
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
					+ ")"),
			};
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
	public static class _dowhile extends AbstractFunction {

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
			return new CNull();
		}

		public String getName() {
			return "dowhile";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "void {code, condition} Like while, but always runs the code at least once. The condition is checked"
					+ " after each run of the code, and if it is true, the code is run again. break and continue work"
					+ " inside a dowhile, but continuing more than once is pointless, since the loop isn't inherently"
					+ " keeping track of any counters anyways. Breaking multiple times still works however.";
		}

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
			return new CVoid(t);
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "dowhile(\n"
					+ "\tmsg('This will only run once')\n"
					+ ", #while\n"
					+ "false)"),
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

		public String getName() {
			return "break";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "nothing {[int]} Stops the current loop. If int is specified, and is greater than 1, the break travels that many loops up. So, if you had"
					+ " a loop embedded in a loop, and you wanted to break in both loops, you would call break(2). If this function is called outside a loop"
					+ " (or the number specified would cause the break to travel up further than any loops are defined), the function will fail. If no"
					+ " argument is specified, it is the same as calling break(1).";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_1_0;
		}

		public Boolean runAsync() {
			return null;
		}

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
					+ ")"),
			};
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() == 1){
				if(children.get(0).isDynamic()){
					//This is absolutely a bad design, if there is a variable here
					//in the break, HOWEVER, it is not an error, we will simply
					//issue a compiler warning. break() parameters should
					//be hard coded.
					CHLog.GetLogger().Log(CHLog.Tags.COMPILER, LogLevel.WARNING, "The parameter sent to break() should"
							+ " be hard coded, and should not be dynamically determinable, since this is always a sign"
							+ " of loose code flow, which should be avoided. This may break optimizations and other"
							+ " code analysis tools, and will most likely cause an error at runtime if not very carefully"
							+ " regulated. Due to all these reasons, not hardcoding the break parameter should always"
							+ " be avoided.", t);
				}
			}
			return null;
		}

		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}
		
		
	}

	@api
	public static class _continue extends AbstractFunction {

		public String getName() {
			return "continue";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "void {[int]} Skips the rest of the code in this loop, and starts the loop over, with it continuing at the next index. If this function"
					+ " is called outside of a loop, the command will fail. If int is set, it will skip 'int' repetitions. If no argument is specified,"
					+ " 1 is used.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_1_0;
		}

		public Boolean runAsync() {
			return null;
		}

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
					+ "}"),
			};
		}
	}

	@api
	public static class is_stringable extends AbstractFunction implements Optimizable {

		public String getName() {
			return "is_stringable";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "boolean {item} Returns whether or not the item is convertable to a string. Everything but arrays can be used as strings.";
		}

		public ExceptionType[] thrown() {
			return null;
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return new CBoolean(!(args[0] instanceof CArray), t);
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
				new ExampleScript("False condition", "is_stringable(array(1))"),
			};
		}
	}
	
	@api
	public static class is_string extends AbstractFunction implements Optimizable {

		public String getName() {
			return "is_string";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "boolean {item} Returns whether or not the item is actually a string datatype. If you just care if some data can be used as a string,"
					+ " use is_stringable().";
		}

		public ExceptionType[] thrown() {
			return null;
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return new CBoolean((args[0] instanceof CString), t);
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
				new ExampleScript("False condition", "is_string(1) #is_stringable() would return true here"),
			};
		}
	}

	@api
	public static class is_bytearray extends AbstractFunction implements Optimizable {

		public String getName() {
			return "is_bytearray";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "boolean {item} Returns whether or not the item is actually a ByteArray datatype.";
		}

		public ExceptionType[] thrown() {
			return null;
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return new CBoolean((args[0] instanceof CByteArray), t);
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
				new ExampleScript("False condition", "is_bytearray(123)"),
			};
		}
	}

	@api
	public static class is_array extends AbstractFunction implements Optimizable {

		public String getName() {
			return "is_array";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "boolean {item} Returns whether or not the item is an array";
		}

		public ExceptionType[] thrown() {
			return null;
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_1_2;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return new CBoolean(args[0] instanceof CArray, t);
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
				new ExampleScript("False condition", "is_array('no')"),
			};
		}
	}

	@api
	public static class is_double extends AbstractFunction implements Optimizable {

		public String getName() {
			return "is_double";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "boolean {item} Returns whether or not the given item is a double. Note that numeric strings and integers"
					+ " can usually be used as a double, however this function checks the actual datatype of the item. If"
					+ " you just want to see if an item can be used as a number, use is_numeric() instead.";
		}

		public ExceptionType[] thrown() {
			return null;
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_1_2;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return new CBoolean(args[0] instanceof CDouble, t);
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
				new ExampleScript("False condition", "is_double(1)"),
			};
		}
	}

	@api
	public static class is_integer extends AbstractFunction implements Optimizable {

		public String getName() {
			return "is_integer";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "boolean {item} Returns whether or not the given item is an integer. Note that numeric strings can usually be used as integers,"
					+ " however this function checks the actual datatype of the item. If you just want to see if an item can be used as a number,"
					+ " use is_integral() or is_numeric() instead.";
		}

		public ExceptionType[] thrown() {
			return null;
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_1_2;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return new CBoolean(args[0] instanceof CInt, t);
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
				new ExampleScript("False condition", "is_integer(1.0)"),
			};
		}
	}

	@api
	public static class is_boolean extends AbstractFunction implements Optimizable {

		public String getName() {
			return "is_boolean";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "boolean {item} Returns whether the given item is of the boolean datatype. Note that all datatypes can be used as booleans, however"
					+ " this function checks the specific datatype of the given item.";
		}

		public ExceptionType[] thrown() {
			return null;
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_1_2;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return new CBoolean(args[0] instanceof CBoolean, t);
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
				new ExampleScript("False condition", "is_boolean(0)"),
			};
		}
	}

	@api
	public static class is_null extends AbstractFunction implements Optimizable {

		public String getName() {
			return "is_null";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "boolean {item} Returns whether or not the given item is null.";
		}

		public ExceptionType[] thrown() {
			return null;
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_1_2;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return new CBoolean(args[0] instanceof CNull, t);
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
				new ExampleScript("False condition", "is_null(0)"),
			};
		}
	}

	@api
	public static class is_numeric extends AbstractFunction implements Optimizable {

		public String getName() {
			return "is_numeric";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "boolean {item} Returns false if the item would fail if it were used as a numeric value."
					+ " If it can be parsed or otherwise converted into a numeric value, true is returned.";
		}

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
			boolean b = true;
			try {
				Static.getNumber(args[0], t);
			} catch (ConfigRuntimeException e) {
				b = false;
			}
			return new CBoolean(b, t);
		}

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
				new ExampleScript("True condition, because null is coerced to 0.0, which is numeric.", "is_numeric(null)"),
			};
		}
	}

	@api
	public static class is_integral extends AbstractFunction implements Optimizable {

		public String getName() {
			return "is_integral";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "boolean {item} Returns true if the numeric value represented by "
					+ " a given double or numeric string could be cast to an integer"
					+ " without losing data (or if it's an integer). For instance,"
					+ " is_numeric(4.5) would return true, and integer(4.5) would work,"
					+ " however, equals(4.5, integer(4.5)) returns false, because the"
					+ " value was narrowed to 4.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			double d;
			try {
				d = Static.getDouble(args[0], t);
			} catch (ConfigRuntimeException e) {
				return new CBoolean(false, t);
			}
			return new CBoolean((long) d == d, t);
		}

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
				new ExampleScript("True condition, because null is coerced to 0, which is integral", "is_integral(null)"),
			};
		}
	}

	@api
	public static class proc extends AbstractFunction {

		public String getName() {
			return "proc";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public String docs() {
			return "void {[name], [ivar...], procCode} Creates a new user defined procedure (also known as \"function\") that can be called later in code. Please see the more detailed"
					+ " documentation on procedures for more information.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		@Override
		public boolean preResolveVariables() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_1_3;
		}

		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			Procedure myProc = getProcedure(t, env, parent, nodes);
			env.getEnv(GlobalEnv.class).GetProcs().put(myProc.getName(), myProc);
			return new CVoid(t);
		}

		public static Procedure getProcedure(Target t, Environment env, Script parent, ParseTree... nodes) {
			String name = "";
			List<IVariable> vars = new ArrayList<IVariable>();
			ParseTree tree = null;
			List<String> varNames = new ArrayList<String>();
			boolean usesAssign = false;
			for (int i = 0; i < nodes.length; i++) {
				if (i == nodes.length - 1) {
					tree = nodes[i];
				} else {
					boolean thisNodeIsAssign = false;
					if (nodes[i].getData() instanceof CFunction) {
						if (((CFunction) nodes[i].getData()).getValue().equals("assign")) {
							thisNodeIsAssign = true;
							if (nodes[i].getChildAt(1).getData().isDynamic()) {
								usesAssign = true;
							}
						}
					}
					Construct cons = parent.eval(nodes[i], env);
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
									if(c instanceof IVariable){
										String varName = ((IVariable)c).getName();
										if(varNames.contains(varName)){
											throw new ConfigRuntimeException("Same variable name defined twice in " + name, ExceptionType.InvalidProcedureException, t);
										}
										varNames.add(varName);
									}
									while (c instanceof IVariable) {
										c = env.getEnv(GlobalEnv.class).GetVarList().get(((IVariable) c).getName(), t).ival();
									}
									if(!thisNodeIsAssign){
										//This is required because otherwise a default value that's already in the environment
										//would end up getting set to the existing value, thereby leaking in the global env
										//into this proc, if the call to the proc didn't have a value in this slot.
										c = new CString("", t);
									}
									ivar = new IVariable(((IVariable) cons).getName(), c.clone(), t);
								} catch (CloneNotSupportedException ex) {
									//
								}
								vars.add(ivar);
							}
						}
					}
				}
			}
			Procedure myProc = new Procedure(name, vars, tree, t);
			if (usesAssign) {
				myProc.definitelyNotConstant();
			}
			return myProc;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return new CVoid(t);
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
		 * @throws ConfigCompileException
		 * @throws ConfigRuntimeException
		 */
		public static Construct optimizeProcedure(Target t, Procedure myProc, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			if (myProc.isPossiblyConstant()) {
				//Oooh, it's possibly constant. So, let's run it with our children.
				try {
					FileOptions options = new FileOptions(new HashMap<String, String>());
					if(!children.isEmpty()){
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
		@Override
		public boolean allowBraces() {
			return true;
		}
		
	}

	@api
	public static class _return extends AbstractFunction implements Optimizable {

		public String getName() {
			return "return";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "nothing {mixed} Returns the specified value from this procedure. It cannot be called outside a procedure.";
		}

		public ExceptionType[] thrown() {
			return null;
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

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.TERMINAL
			);
		}				

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			Construct ret = (args.length == 1 ? args[0] : new CVoid(t));
			throw new FunctionReturnException(ret, t);
		}
	}

	@api
	public static class include extends AbstractFunction implements Optimizable {

		public String getName() {
			return "include";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "void {path} Includes external code at the specified path.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.IncludeException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_2_0;
		}

		public Boolean runAsync() {
			return true;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return new CVoid(t);
		}

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			ParseTree tree = nodes[0];
			Construct arg = parent.seval(tree, env);
			String location = arg.val();
			ParseTree include = IncludeCache.get(new File(t.file().getParent(), location), t);
			parent.eval(include.getChildAt(0), env);
			return new CVoid(t);
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.OPTIMIZE_CONSTANT
			);
		}

		@Override
		public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
			//We can't optimize per se, but if the path is constant, and the code is uncompilable, we
			//can give a warning, and go ahead and cache the tree.
			String path = args[0].val();
			IncludeCache.get(new File(t.file().getParent(), path), t);
			return null;
		}
	}

	@api
	public static class call_proc extends AbstractFunction implements Optimizable {

		public String getName() {
			return "call_proc";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public String docs() {
			return "mixed {proc_name, [var1...]} Dynamically calls a user defined procedure. call_proc(_myProc, 'var1') is the equivalent of"
					+ " _myProc('var1'), except you could dynamically build the procedure name if need be. This is useful for dynamic coding,"
					+ " however, closures work best for callbacks. Throws an InvalidProcedureException if the procedure isn't defined. If you are"
					+ " hardcoding the first parameter, a warning will be issued, because it is much more efficient and safe to directly use"
					+ " a procedure if you know what its name is beforehand.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InvalidProcedureException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_2_0;
		}

		public Boolean runAsync() {
			return null;
		}

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

		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() < 1){
				throw new ConfigRuntimeException("Expecting at least one argument to " + getName(), ExceptionType.InsufficientArgumentsException, t);
			}
			if(children.get(0).isConst()){
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
			if(ca.inAssociativeMode()){
				throw new Exceptions.CastException("Expected the array passed to " + getName() + " to be non-associative.", t);
			}
			Construct [] args2 = new Construct[(int)ca.size() + 1];
			args2[0] = args[0];
			for(int i = 1; i < args2.length; i++){
				args2[i] = ca.get(i - 1);
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
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			//If they hardcode the name, that's fine, because the variables may just be the only thing that's variable.
			return null;
		}
		
	}

	@api(environments=CommandHelperEnvironment.class)
	public static class is_proc extends AbstractFunction {

		public String getName() {
			return "is_proc";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "boolean {procName} Returns whether or not the given procName is currently defined, i.e. if calling this proc wouldn't"
					+ " throw an exception.";
		}

		public ExceptionType[] thrown() {
			return null;
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_2_0;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment env, Construct... args) {
			return new CBoolean(env.getEnv(GlobalEnv.class).GetProcs().get(args[0].val()) == null ? false : true, t);
		}
	}

	@api
	public static class is_associative extends AbstractFunction implements Optimizable {

		public String getName() {
			return "is_associative";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "boolean {array} Returns whether or not the array is associative. If the parameter is not an array, throws a CastException.";
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

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			if (args[0] instanceof CArray) {
				return new CBoolean(((CArray) args[0]).inAssociativeMode(), t);
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
				new ExampleScript("False condition", "is_associative(array(1, 2, 3))"),
			};
		}
	}

	@api
	public static class is_closure extends AbstractFunction {

		public String getName() {
			return "is_closure";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "boolean {arg} Returns true if the argument is a closure (could be executed)"
					+ " or false otherwise";
		}

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
			return new CBoolean(args[0] instanceof CClosure, t);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("True condition", "is_closure(closure(msg('code')))"),
				new ExampleScript("False condition", "is_closure('a string')"),
			};
		}
	}

	@api
	public static class _import extends AbstractFunction implements Optimizable {

		public String getName() {
			return "import";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

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
					+ " for more information.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return true;
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
			if (args[0] instanceof IVariable) {
				//Mode 1     
				IVariable var = (IVariable) args[0];
				environment.getEnv(GlobalEnv.class).GetVarList().set(Globals.GetGlobalIVar(var));
				return new CVoid(t);
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

		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() > 2){
				CHLog.GetLogger().w(CHLog.Tags.DEPRECATION, "Automatic creation of namespaces is deprecated, and WILL be removed in the future."
							+ " Use import('my.namespace') instead of import('my', 'namespace')", t);
			}
			if(children.get(0).getData() instanceof IVariable){
				CHLog.GetLogger().w(CHLog.Tags.DEPRECATION, "import(@ivar) usage is deprecated. Please use the @ivar = import('custom.name') format,"
							+ " as this feature WILL be removed in the future.", t);
			}
			//Just a compiler warning
			return null;
		}
		
	}

	@api
	public static class _export extends AbstractFunction implements Optimizable {

		public String getName() {
			return "export";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public String docs() {
			return "void {ivar | key, value} Stores a value in the global storage register."
					+ " When using the first mode, the ivariable is stored so it can be imported"
					+ " later, and when using the second mode, an arbitrary value is stored with"
					+ " the give key, and can be retreived using the secode mode of import. The first mode will"
					+ " be deprecated in future versions, so should be avoided. If"
					+ " the value is already stored, it is overwritten. See {{function|import}} and"
					+ " [[CommandHelper/import-export|importing/exporting]]. The reference to the value"
					+ " is stored, not a copy of the value, so in the case of arrays, manipulating the"
					+ " contents of the array will manipulate the stored value.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InsufficientArgumentsException};
		}

		public boolean isRestricted() {
			return true;
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
			return new CVoid(t);
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
		
		public Set<Optimizable.OptimizationOption> optimizationOptions() {
			return EnumSet.of(Optimizable.OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() > 2){
				CHLog.GetLogger().w(CHLog.Tags.DEPRECATION, "Automatic creation of namespaces is deprecated, and WILL be removed in the future."
							+ " Use export('my.namespace', @var) instead of export('my', 'namespace', @var)", t);
			}
			if(children.get(0).getData() instanceof IVariable){
				CHLog.GetLogger().w(CHLog.Tags.DEPRECATION, "export(@ivar) usage is deprecated. Please use the export('custom.name', @ivar) format,"
							+ " as this feature WILL be removed in the future.", t);
			}
			//Just a compiler warning
			return null;
		}
		
	}

	@api(environments=CommandHelperEnvironment.class)
	public static class closure extends AbstractFunction {

		public String getName() {
			return "closure";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

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
					+ " See the wiki article on [[CommandHelper/Closures|closures]] for more details"
					+ " and examples.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return true;
		}

		@Override
		public boolean preResolveVariables() {
			return false;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CVoid(t);
		}

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			if(nodes.length == 0){
				//Empty closure, do nothing.
				return new CClosure(null, env, new String[]{}, new Construct[]{}, t);
			}
			String[] names = new String[nodes.length - 1];
			Construct[] defaults = new Construct[nodes.length - 1];
			for (int i = 0; i < nodes.length - 1; i++) {
				ParseTree node = nodes[i];
				ParseTree newNode = new ParseTree(new CFunction("g", t), node.getFileOptions());
				List<ParseTree> children = new ArrayList<ParseTree>();
				children.add(node);
				newNode.setChildren(children);
				Script fakeScript = Script.GenerateScript(newNode, env.getEnv(GlobalEnv.class).GetLabel());
				Construct ret = MethodScriptCompiler.execute(newNode, env, null, fakeScript);
				if (!(ret instanceof IVariable)) {
					throw new ConfigRuntimeException("Arguments sent to closure (barring the last) must be ivariables", ExceptionType.CastException, t);
				}
				names[i] = ((IVariable) ret).getName();
				try {
					defaults[i] = ((IVariable) ret).ival().clone();
				} catch (CloneNotSupportedException ex) {
					Logger.getLogger(DataHandling.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			CClosure closure = new CClosure(nodes[nodes.length - 1], env, names, defaults, t);
			return closure;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public boolean allowBraces() {
			return true;
		}		
	}

	@api
	public static class execute extends AbstractFunction {

		public String getName() {
			return "execute";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public String docs() {
			return "mixed {[values...,] closure} Executes the given closure. You can also send arguments"
					+ " to the closure, which it may or may not use, depending on the particular closure's"
					+ " definition. If the closure returns a value with return(), then that value will"
					+ " be returned with execute. Otherwise, void is returned.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if (args[args.length - 1] instanceof CClosure) {
				Construct[] vals = new Construct[args.length - 1];
				System.arraycopy(args, 0, vals, 0, args.length - 1);
				CClosure closure = (CClosure) args[args.length - 1];
				try{
					closure.execute(vals);
				} catch(FunctionReturnException e){
					return e.getReturn();
				}
			} else {
				throw new ConfigRuntimeException("Only a closure (created from the closure function) can be sent to execute()", ExceptionType.CastException, t);
			}
			return new CVoid(t);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class _boolean extends AbstractFunction implements Optimizable {

		public String getName() {
			return "boolean";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "boolean {item} Returns a new construct that has been cast to a boolean. The item is cast according to"
					+ " the boolean conversion rules. Since all data types can be cast to a"
					+ " a boolean, this function will never throw an exception.";
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
			return new CBoolean(Static.getBoolean(args[0]), t);
		}

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
				new ExampleScript("Basic usage", "boolean('')"),
			};
		}
	}

	@api
	public static class _integer extends AbstractFunction implements Optimizable {

		public String getName() {
			return "integer";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "integer {item} Returns a new construct that has been cast to an integer."
					+ " This function will throw a CastException if is_numeric would return"
					+ " false for this item, but otherwise, it will be cast properly. Data"
					+ " may be lost in this conversion. For instance, 4.5 will be converted"
					+ " to 4, by using integer truncation. You can use is_integral to see"
					+ " if this data loss would occur.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CInt((long) Static.getDouble(args[0], t), t);
		}

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
				new ExampleScript("Failure", "assign(@var, 'string')\ninteger(@var)"),
			};
		}
	}

	@api
	public static class _double extends AbstractFunction implements Optimizable {

		public String getName() {
			return "double";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "double {item} Returns a new construct that has been cast to an double."
					+ " This function will throw a CastException if is_numeric would return"
					+ " false for this item, but otherwise, it will be cast properly.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CDouble(Static.getDouble(args[0], t), t);
		}

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
				new ExampleScript("Failure", "assign(@var, 'string')\ndouble(@var)"),
			};
		}
	}

	@api
	public static class _string extends AbstractFunction implements Optimizable {

		public String getName() {
			return "string";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "string {item} Creates a new construct that is the \"toString\" of an item."
					+ " For arrays, an human readable version is returned; this should not be"
					+ " used directly, as the format is not guaranteed to remain consistent. Booleans return \"true\""
					+ " or \"false\" and null returns \"null\".";
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
			return new CString(args[0].val(), t);
		}

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
				new ExampleScript("Basic usage", "string(array(one: 'one', two: 'two'))"),
			};
		}
	}
	
	@api public static class to_radix extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.RangeException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			int radix = Static.getInt32(args[1], t);
			if(radix < Character.MIN_RADIX || radix > Character.MAX_RADIX){
				throw new Exceptions.RangeException("The radix must be between " + Character.MIN_RADIX + " and " + Character.MAX_RADIX + ", inclusive.", t);
			}
			return new CString(Long.toString(Static.getInt(args[0], t), radix), t);
		}

		public String getName() {
			return "to_radix";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

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

		public Version since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("To a hex string", "to_radix(15, 16)"),
				new ExampleScript("To a binary string", "to_radix(15, 2)")
			};
		}
	}
	
	@api public static class parse_int extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.RangeException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			String value = args[0].val();
			int radix = Static.getInt32(args[1], t);
			if(radix < Character.MIN_RADIX || radix > Character.MAX_RADIX){
				throw new Exceptions.RangeException("The radix must be between " + Character.MIN_RADIX + " and " + Character.MAX_RADIX + ", inclusive.", t);
			}
			long ret;
			try{
				ret = Long.parseLong(value, radix);
			} catch(NumberFormatException ex){
				throw new Exceptions.FormatException("The input string: \"" + value + "\" is improperly formatted. (Perhaps you're using a character greater than"
						+ " the radix specified?)", t);
			}
			return new CInt(ret, t);
		}

		public String getName() {
			return "parse_int";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "int {value, radix} Converts a string representation of an integer to a real integer, given the value's"
					+ " radix (base). See {{function|to_radix}} for a more detailed explanation of number theory. Radix must be"
					+ " between " + Character.MIN_RADIX + " and " + Character.MAX_RADIX + ", inclusive.";
		}

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
}


