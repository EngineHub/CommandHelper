package com.laytonsmith.core.functions;

import com.laytonsmith.core.compiler.Braceable;
import com.laytonsmith.core.compiler.Optimizable;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.core.*;
import com.laytonsmith.core.arguments.Argument;
import com.laytonsmith.core.arguments.ArgumentBuilder;
import com.laytonsmith.core.arguments.Generic;
import com.laytonsmith.core.arguments.Signature;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.*;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Layton
 */
public class DataHandling {

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

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			return new CArray(t, args);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public String docs() {
			return "Creates an array of values.";
		}
		
		public Argument returnType() {
			return new Argument("The newly created array", CArray.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("Any number of values. If no values are"
					+ " provided, an empty array is created.", CArray.class, "varX").setGenerics(new Generic(Mixed.class)).setVarargs()
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

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
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
		
		public Argument returnType() {
			return new Argument("The newly created array", CArray.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("Any number of values. If no values are provided,"
					+ " an empty associative array is produced.", CArray.class, "args").setGenerics(new Generic(Mixed.class)).setVarargs()
					);
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

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			if (args[0] instanceof IVariable) {
				env.getEnv(GlobalEnv.class).GetVarList().set((IVariable)args[0], args[1]);
				return args[0];
			}
			throw new ConfigRuntimeException("assign only accepts an ivariable or array reference as the first argument", ExceptionType.CastException, t);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "Accepts an ivariable ivar as a parameter, and puts the specified value mixed in it. Returns the variable that was assigned.";
		}
		
		public Argument returnType() {
			return new Argument("The ivar meta object that was assigned", IVariable.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The variable to assign", IVariable.class, "ivar"),
					new Argument("The value to assign the variable to", Mixed.class, "mixed")
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

		public Boolean runAsync() {
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.OPTIMIZE_DYNAMIC
			);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			if(children.get(0).getData() instanceof IVariable
					&& children.get(1).getData() instanceof IVariable){
				if(((IVariable)children.get(0).getData()).getName().equals(
						((IVariable)children.get(1).getData()).getName())){
					CHLog.GetLogger().CompilerWarning(CompilerWarning.AssignmentToItself, "Assigning a variable to itself", t, children.get(0).getFileOptions());
				}
			} else if(children.get(0).getData() instanceof CFunction 
					&& ((CFunction)children.get(0).getData()).val().equals("array_get")){
				//Special handling for array assignment. This should be transformed into an array set
				ParseTree array_set = new ParseTree(new CFunction("array_set", t), children.get(0).getFileOptions());
				array_set.addChild(children.get(0).getChildAt(0));
				array_set.addChild(children.get(0).getChildAt(1));
				array_set.addChild(children.get(1));
				return array_set;
			} else if(!(children.get(0).getData() instanceof IVariable)){
				throw new ConfigCompileException("Only ivariables may be assigned a value", t);
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
	public static class _for extends AbstractFunction implements Optimizable, Braceable {

		public String getName() {
			return "for";
		}

		public Integer[] numArgs() {
			return new Integer[]{4};
		}

		public Mixed exec(Target t, Environment env, Mixed... args) {
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
			return "Acts as a typical for loop. The assignment is first run. Then, a"
					+ " condition is checked. If that condition is checked and returns true, expression2 is run. After that, expression1 is run. In java"
					+ " syntax, this would be: for(assign; condition; expression1){expression2}. assign must be an ivariable, either a "
					+ "pre defined one, or the results of the assign() function. condition must be a boolean.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The variable that is assigned", IVariable.class, "assign"),
					new Argument("The condition that is checked before running", CBoolean.class, "condition"),
					new Argument("The expression that is run at the end of each loop"
					+ " iteration. Typically used for incrementing/decrementing the counter", CCode.class, "expression1"),
					new Argument("The code to run several iterations of", CCode.class, "expression2")
					);
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
		public ParseTree optimizeDynamic(Target t, Environment env, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
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

		public void handleBraces(List<ParseTree> allNodes, int startingWith) throws ConfigCompileException {
			if(allNodes.get(startingWith + 1).getData() instanceof CBrace){
				ParseTree child = allNodes.remove(startingWith + 1);
				allNodes.get(startingWith).addChild(((CBrace)child.getData()).getNode());
			}
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

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
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

			Mixed counter = parent.eval(assign, env);
			if (!(counter instanceof IVariable)) {
				throw new ConfigRuntimeException("First parameter of for must be an ivariable", ExceptionType.CastException, t);
			}
			int _continue = 0;
			while (true) {
				boolean cond = parent.seval(condition, env).primitive(t).castToBoolean();
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
			return "Works like a normal for, but if upon checking the condition the first time,"
					+ " it is determined that it is false (that is, NO code loops are going to be run) the else code is run instead. If the loop runs,"
					+ " even once, it will NOT run the else branch.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The variable that is assigned", IVariable.class, "assign"),
					new Argument("The condition that is checked before running", CBoolean.class, "condition"),
					new Argument("The expression that is run at the end of each loop"
					+ " iteration. Typically used for incrementing/decrementing the counter", CCode.class, "expression1"),
					new Argument("The code to run several iterations of", CCode.class, "expression2"),
					new Argument("The code to run should there be 0 iterations of the main loop code", CCode.class, "else")
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}

	@api(environments=CommandHelperEnvironment.class)
	public static class foreach extends AbstractFunction implements Braceable {

		public String getName() {
			return "foreach";
		}

		public Integer[] numArgs() {
			return new Integer[]{3, 4};
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
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
			Mixed arr = parent.seval(array, env);
			Mixed ik = null;
			if(key != null){
				ik = parent.eval(key, env);
				if(!(ik instanceof IVariable)){
					throw new ConfigRuntimeException("Parameter 2 of " + getName() + " must be an ivariable", ExceptionType.CastException, t);
				}
			}
			Mixed iv = parent.eval(value, env);
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
								env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(kkey.getName(), t), new CInt(i, t));
							}
							env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(two.getName(), t), one.get(i, t));
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
								env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(kkey.getName(), t), new CString(index, t));
							}
							env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(two.getName(), t), one.get(index, t));
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
			return "Walks through array, setting ivar equal to each element in the array, then running code."
					+ " In addition, foreach(1..4, @i, code()) is also valid, setting @i to 1, 2, 3, 4 each time. The same syntax is valid as"
					+ " in an array slice. If key is set (it must be an ivariable) then the index of each iteration will be set to that."
					+ " See the examples for a demonstration.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The array to walk through", CArray.class, "array").setGenerics(new Generic(Mixed.class)),
					new Argument("The ivar that will have the current item's key value assigned to", IVariable.class, "key").setOptional(),
					new Argument("The ivar that will have the current item's value assigned to", IVariable.class, "value"),
					new Argument("The code to run for each item in the array", CCode.class, "code")
					);
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

		public void handleBraces(List<ParseTree> allNodes, int startingWith) throws ConfigCompileException {
			throw new UnsupportedOperationException("Not supported yet.");
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

			Mixed data = parent.seval(array, env);
			
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
			return "Works like a foreach, except if the array is empty, the else code runs instead. That is, if the code"
					+ " would not run at all, the else condition would.";
		}
		
		@Override
		public Argument returnType() {
			return Argument.VOID;
		}

		@Override
		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The array to walk through", CArray.class, "array").setGenerics(new Generic(Mixed.class)),
					new Argument("The ivar that will have the current item's key value assigned to", IVariable.class, "key").setOptional(),
					new Argument("The ivar that will have the current item's value assigned to", IVariable.class, "value"),
					new Argument("The code to run for each item in the array", CCode.class, "code"),
					new Argument("The code to run should there be no items in the array", CCode.class, "else")
					);
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
			return "While the condition is true, the code is executed. break and continue work"
					+ " inside a dowhile, but continuing more than once is pointless, since the loop isn't inherently"
					+ " keeping track of any counters anyways. Breaking multiple times still works however.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The condition to check", CBoolean.class, "condition"),
					new Argument("The code to run, while condition remains true", CCode.class, "code")
					);
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
				while (parent.seval(nodes[0], env).primitive(t).castToBoolean()) {
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

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			throw new UnsupportedOperationException();
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

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			throw new UnsupportedOperationException();
		}

		public String getName() {
			return "dowhile";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "Like while, but always runs the code at least once. The condition is checked"
					+ " after each run of the code, and if it is true, the code is run again. break and continue work"
					+ " inside a dowhile, but continuing more than once is pointless, since the loop isn't inherently"
					+ " keeping track of any counters anyways. Breaking multiple times still works however.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The code to run, once always, the continually while condition remains true", CCode.class, "code"),
					new Argument("The condition to check", CBoolean.class, "condition")
					);
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
				} while (parent.seval(nodes[1], env).primitive(t).castToBoolean());
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
			return "Stops the current loop. If int is specified, and is greater than 1, the break travels that many loops up. So, if you had"
					+ " a loop embedded in a loop, and you wanted to break in both loops, you would call break(2). If this function is called outside a loop"
					+ " (or the number specified would cause the break to travel up further than any loops are defined), the function will fail. If no"
					+ " argument is specified, it is the same as calling break(1).";
		}
		
		public Argument returnType() {
			return Argument.NONE;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The number of loops to break out of", CInt.class, "times").setOptionalDefault(1)
					);
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

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			int num = 1;
			if (args.length == 1) {
				num = args[0].primitive(t).castToInt32(t);
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
		public ParseTree optimizeDynamic(Target t, Environment env, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() == 1){
				if(children.get(0).isDynamic()){
					//This is absolutely a bad design, if there is a variable here
					//in the break, HOWEVER, it is not an error, we will simply
					//issue a compiler warning. break() parameters should
					//be hard coded.
					CHLog.GetLogger().CompilerWarning(CompilerWarning.VariableBreak, "The parameter sent to break() should"
							+ " be hard coded, and should not be dynamically determinable, since this is always a sign"
							+ " of loose code flow, which should be avoided. This may break optimizations and other"
							+ " code analysis tools, and will most likely cause an error at runtime if not very carefully"
							+ " regulated. Due to all these reasons, not hardcoding the break parameter should always"
							+ " be avoided.", t, children.get(0).getFileOptions());
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
			return "Skips the rest of the code in this loop, and starts the loop over, with it continuing at the next index. If this function"
					+ " is called outside of a loop, the command will fail. If int is set, it will skip 'int' repetitions. If no argument is specified,"
					+ " 1 is used.";
		}
		
		public Argument returnType() {
			return Argument.NONE;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The number of loop iterations to skip", CInt.class, "times").setOptionalDefault(1)
					);
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

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			int num = 1;
			if (args.length == 1) {
				num = args[0].primitive(t).castToInt32(t);
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
			return "Returns whether or not the item is convertable to a string. Everything but arrays can be used as strings.";
		}
		
		public Argument returnType() {
			return new Argument("true if this value is automatically castable to a string", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The value to check", Mixed.class, "item")
					);
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

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return new CBoolean(args[0] instanceof CPrimitive, t);
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
			return "Returns whether or not the item is actually a string datatype. If you just care if some data can be used as a string,"
					+ " use is_stringable().";
		}
		
		public Argument returnType() {
			return new Argument("true iff the item is an instance of string", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The item to check", Mixed.class, "item")
					);
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

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
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
	public static class is_array extends AbstractFunction implements Optimizable {

		public String getName() {
			return "is_array";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Returns whether or not the item is an array";
		}
		
		public Argument returnType() {
			return new Argument("true iff the item is an instance of an array", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The item to check", Mixed.class, "item")
					);
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

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
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
			return "Returns whether or not the given item is a double. Note that numeric strings and integers"
					+ " can usually be used as a double, however this function checks the actual datatype of the item. If"
					+ " you just want to see if an item can be used as a number, use is_numeric() instead.";
		}
		
		public Argument returnType() {
			return new Argument("true iff this is an instance of a double", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The item to check", Mixed.class, "item")
					);
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

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
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
		
		public Argument returnType() {
			return new Argument("true iff this is an instance of int", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The item to check", Mixed.class, "item")
					);
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

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
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
			return "boolean {item} Returns whether the given item is of the boolean datatype. Note that all primitive datatypes can be used as booleans, however"
					+ " this function checks the specific datatype of the given item.";
		}
		
		public Argument returnType() {
			return new Argument("true iff this is an instance of a boolean", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The item to check", Mixed.class, "item")
					);
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

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
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
			return "Returns whether or not the given item is null. You can also simply check to see if a value is strictly"
					+ " equal to null";
		}
		
		public Argument returnType() {
			return new Argument("true iff this is null", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The item to check", Mixed.class, "item")
					);
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

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return new CBoolean(args[0].isNull(), t);
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
			return "Returns false if the item would fail if it were used as a numeric value."
					+ " If it can be parsed or otherwise converted into a numeric value, true is returned.";
		}
		
		public Argument returnType() {
			return new Argument("true if this is automatically castable to a numeric type", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The item to check", Mixed.class, "item")
					);
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

		public CBoolean exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			boolean b = true;
			try {
				args[0].primitive(t).castToDouble(t);
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
				new ExampleScript("False condition", "is_numeric('string')"),
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
			return "Returns true if the numeric value represented by "
					+ " a given double or numeric string could be cast to an integer"
					+ " without losing data (or if it's an integer). For instance,"
					+ " is_numeric(4.5) would return true, and integer(4.5) would work,"
					+ " however, equals(4.5, integer(4.5)) returns false, because the"
					+ " value was narrowed to 4.";
		}
		
		public Argument returnType() {
			return new Argument("true if this is automatically castable to an integer type", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The item to check", Mixed.class, "item")
					);
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

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			double d;
			try {
				d = args[0].primitive(t).castToDouble(t);
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
			return "Creates a new user defined procedure (also known as \"function\") that can be called later in code. Please see the more detailed"
					+ " documentation on procedures for more information.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.MANUAL;
		}

		@Override
		public String argumentsManual() {
			return "[string name], [ivar ivar...], code procCode";
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
			for (int i = 0; i < nodes.length; i++) {
				if (i == nodes.length - 1) {
					tree = nodes[i];
				} else {
					Mixed cons = parent.eval(nodes[i], env);
					if (i == 0 && cons instanceof IVariable) {
						throw new ConfigRuntimeException("Anonymous Procedures are not allowed", ExceptionType.InvalidProcedureException, t);
					} else {
						if (i == 0 && !(cons instanceof IVariable)) {
							name = cons.val();
						} else {
							if (!(cons instanceof IVariable)) {
								throw new ConfigRuntimeException("You must use IVariables as the arguments", ExceptionType.InvalidProcedureException, t);
							} else {
								vars.add((IVariable)cons);
							}
						}
					}
				}
			}
			Procedure myProc = new Procedure(name, vars, tree, t);
			return myProc;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
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
		public static Mixed optimizeProcedure(Target t, Procedure myProc, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			if (myProc.isPossiblyConstant()) {
				//Oooh, it's possibly constant. So, let's run it with our children.
				try {
					FileOptions options = new FileOptions(new EnumMap<FileOptions.Directive, String>(FileOptions.Directive.class), Target.UNKNOWN);
					if(!children.isEmpty()){
						options = children.get(0).getFileOptions();
					}
					ParseTree root = new ParseTree(new CFunction("__autoconcat__", Target.UNKNOWN), options);
					Script fakeScript = Script.GenerateScript(root, PermissionsResolver.GLOBAL_PERMISSION);
					Environment env = Static.GenerateStandaloneEnvironment();
					env.getEnv(GlobalEnv.class).SetScript(fakeScript);
					Mixed c = myProc.cexecute(children, env, t);
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
			return "Returns the specified value from this procedure. It cannot be called outside a procedure.";
		}
		
		public Argument returnType() {
			return Argument.NONE;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The value to return from the procedure", Mixed.class, "return")
					);
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

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			Mixed ret = (args.length == 1 ? args[0] : new CVoid(t));
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
			return "Includes external code at the specified path.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The path to the file, relative to the file this code is running from", CString.class, "path")
					);
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

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return new CVoid(t);
		}

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			ParseTree tree = nodes[0];
			Mixed arg = parent.seval(tree, env);
			String location = arg.val();
			ParseTree include = IncludeCache.get(new File(t.file().getParent(), location), t, env);
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
		public Construct optimize(Target t, Environment env, Mixed... args) throws ConfigCompileException {
			//We can't optimize per se, but if the path is constant, and the code is uncompilable, we
			//can give a warning, and go ahead and cache the tree.
			String path = args[0].val();
			IncludeCache.get(new File(t.file().getParent(), path), t, env);
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
			return "Dynamically calls a user defined procedure. call_proc(_myProc, 'var1') is the equivalent of"
					+ " _myProc('var1'), except you could dynamically build the procedure name if need be. This is useful for dynamic coding,"
					+ " however, closures work best for callbacks. Throws an InvalidProcedureException if the procedure isn't defined. If you are"
					+ " hardcoding the first parameter, a warning will be issued, because it is much more efficient and safe to directly use"
					+ " a procedure if you know what its name is beforehand.";
		}
		
		public Argument returnType() {
			return new Argument("The value returned from the procedure", Mixed.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The name of the procedure to call", CString.class, "proc_name"),
					new Argument("The parameters to send to the procedure", CArray.class, "params").setGenerics(new Generic(Mixed.class)).setVarargs()
					);
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

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			if (args.length < 1) {
				throw new ConfigRuntimeException("Expecting at least one argument to " + getName(), ExceptionType.InsufficientArgumentsException, t);
			}
			Procedure proc = env.getEnv(GlobalEnv.class).GetProcs().get(args[0].val());
			if (proc != null) {
				List<Mixed> vars = new ArrayList<Mixed>(Arrays.asList(args));
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
		public ParseTree optimizeDynamic(Target t, Environment env, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			CArray ca = Static.getArray(args[1], t);
			if(ca.inAssociativeMode()){
				throw new Exceptions.CastException("Expected the array passed to " + getName() + " to be non-associative.", t);
			}
			Mixed [] args2 = new Construct[(int)ca.size() + 1];
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
			return "Works like call_proc, but allows for variable or unknown number of arguments to be passed to"
					+ " a proc. The array parameter is \"flattened\", and call_proc is essentially called. If the array is associative, an"
					+ " exception is thrown.";
		}
		
		@Override
		public Argument returnType() {
			return new Argument("The value returned from the procedure", Mixed.class);
		}

		@Override
		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The name of the procedure", CString.class, "proc_name"),
					new Argument("The parameters to send to the procedure", CArray.class, "array")
					);
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
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
			return "Returns whether or not the given procName is currently defined, i.e. if calling this proc wouldn't"
					+ " throw an exception.";
		}
		
		public Argument returnType() {
			return new Argument("true if the procedure name is defined", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The name of the procedure to check for", CString.class, "proc_name")
					);
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

		public Mixed exec(Target t, Environment env, Mixed... args) {
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
			return "Returns whether or not the array is associative. If the parameter is not an array, throws a CastException.";
		}
		
		public Argument returnType() {
			return new Argument("true if the array is associative", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The array to check", CArray.class, "array").setGenerics(new Generic(Mixed.class))
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

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
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
			return "Returns true if the argument is a closure (could be executed)"
					+ " or false otherwise";
		}
		
		public Argument returnType() {
			return new Argument("true if the argument is a closure", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The item to check", Mixed.class, "item")
					);
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

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
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
			return "This function imports a value from the global value"
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
		
		public Argument returnType() {
			return new Argument("The stored value", Mixed.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Signature(1,
						new Argument("The variable to import", IVariable.class, "ivar")
					), new Signature(2, 
						new Argument("The key to import", CString.class, "key"),
						new Argument("Optional namespaces", CArray.class, "namespace").setGenerics(new Generic(CString.class)).setVarargs()
					)
					);
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

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if (args[0] instanceof IVariable) {
				//Mode 1     
				IVariable var = (IVariable) args[0];
				//This method is going to be deprecated next release
				CHLog.GetLogger().Log(CHLog.Tags.DEPRECATION, LogLevel.WARNING, "In future versions of CH, this will work differently."
						+ " The value stored in the variable will be used, not the name of the variable itself. Take corrective action"
						+ " now to get rid of this warning, then once the feature is switched, you can use the method as you would"
						+ " normally. (For instance, change usages of import(@var) to import('@var'), or use a more meaningful name"
						+ " in general.)", t);
				environment.getEnv(GlobalEnv.class).GetVarList().set(var, Globals.GetGlobalIVar(var));
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
		public ParseTree optimizeDynamic(Target t, Environment env, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
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
			return "Stores a value in the global storage register."
					+ " When using the first mode, the ivariable is stored so it can be imported"
					+ " later, and when using the second mode, an arbitrary value is stored with"
					+ " the give key, and can be retreived using the secode mode of import. The first mode will"
					+ " be deprecated in future versions, so should be avoided. If"
					+ " the value is already stored, it is overwritten. See {{function|import}} and"
					+ " [[CommandHelper/import-export|importing/exporting]]. The reference to the value"
					+ " is stored, not a copy of the value, so in the case of arrays, manipulating the"
					+ " contents of the array will manipulate the stored value.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.MANUAL;
//			return ArgumentBuilder.Build(
//					new Signature(1, 
//						new Argument("The variable to export, using the value currently assigned to it. This usage is deprecated.", IVariable.class, "ivar")
//					), new Signature(2,
//						new Argument("", C.class, ""),
//						new Argument("", C.class, "")
//					)
//					);
		}

		@Override
		public String argumentsManual() {
			return "ivar | key[, namespace, ...,], value";
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

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if (args.length == 1) {
				if (args[0] instanceof IVariable) {
					IVariable cur = (IVariable) args[0];
					CHLog.GetLogger().Log(CHLog.Tags.DEPRECATION, LogLevel.WARNING, "In future versions of CH, this will work differently."
						+ " The value stored in the variable will be used, not the name of the variable itself. Take corrective action"
						+ " now to get rid of this warning, then once the feature is switched, you can use the method as you would"
						+ " normally. (For instance, change usages of export(@var) to export('@var'), or use a more meaningful name"
						+ " in general.)", t);
					Globals.SetGlobal(cur, environment.getEnv(GlobalEnv.class).GetVarList().get(cur, cur.getTarget()));
				} else {
					throw new ConfigRuntimeException("Expecting a IVariable when only one parameter is specified", ExceptionType.InsufficientArgumentsException, t);
				}
			} else {
				String key = GetNamespace(args, args.length - 1, getName(), t);
				Mixed c = args[args.length - 1];
				//We want to store the value contained, not the ivar itself
				while (c instanceof IVariable) {
					c = environment.getEnv(GlobalEnv.class).GetVarList().get(((IVariable) c), t);
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
		public ParseTree optimizeDynamic(Target t, Environment env, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
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
			return "Returns a closure on the provided code. A closure is"
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
		
		public Argument returnType() {
			return new Argument("The new closure", CClosure.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.MANUAL;
		}

		@Override
		public String argumentsManual() {
			return "[varNames...,] code";
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

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return new CVoid(t);
		}

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			String[] names = new String[nodes.length - 1];
			Mixed[] defaults = new Construct[nodes.length - 1];
			for (int i = 0; i < nodes.length - 1; i++) {
				ParseTree node = nodes[i];
				ParseTree newNode = new ParseTree(new CFunction("g", t), node.getFileOptions());
				List<ParseTree> children = new ArrayList<ParseTree>();
				children.add(node);
				newNode.setChildren(children);
				Script fakeScript = Script.GenerateScript(newNode, env.getEnv(GlobalEnv.class).GetLabel());
				Mixed ret = MethodScriptCompiler.execute(newNode, env, null, fakeScript);
				if (!(ret instanceof IVariable)) {
					throw new ConfigRuntimeException("Arguments sent to closure (barring the last) must be ivariables", ExceptionType.CastException, t);
				}
				names[i] = ((IVariable) ret).getName();
				defaults[i] = env.getEnv(GlobalEnv.class).GetVarList().get(((IVariable)ret), t);
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
			return "Executes the given closure. You can also send arguments"
					+ " to the closure, which it may or may not use, depending on the particular closure's"
					+ " definition.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.MANUAL;
		}

		@Override
		public String argumentsManual() {
			return "[values...,] closure";
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

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if (args[args.length - 1] instanceof CClosure) {
				Construct[] vals = new Construct[args.length - 1];
				System.arraycopy(args, 0, vals, 0, args.length - 1);
				CClosure closure = (CClosure) args[args.length - 1];
				closure.execute(vals);
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
					+ " the boolean conversion rules. Since all primitive data types can be cast to a"
					+ " a boolean, this function will never throw an exception.";
		}
		
		public Argument returnType() {
			return new Argument("The item, cast to a boolean", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The item to cast", CPrimitive.class, "item")
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

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return new CBoolean(args[0].primitive(t).castToBoolean(), t);
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
			return "Returns a new construct that has been cast to an integer."
					+ " This function will throw a CastException if is_numeric would return"
					+ " false for this item, but otherwise, it will be cast properly. Data"
					+ " may be lost in this conversion. For instance, 4.5 will be converted"
					+ " to 4, by using integer truncation. You can use is_integral to see"
					+ " if this data loss would occur.";
		}
		
		public Argument returnType() {
			return new Argument("The item, cast to an integer", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The item to cast", CPrimitive.class, "item")
					);
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

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return new CInt(args[0].primitive(t).castToInt(t), t);
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
			return "Returns a new construct that has been cast to an double."
					+ " This function will throw a CastException if is_numeric would return"
					+ " false for this item, but otherwise, it will be cast properly.";
		}
		
		public Argument returnType() {
			return new Argument("The item, cast to a double", CDouble.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The item to cast", CPrimitive.class, "item")
					);
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

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return new CDouble(args[0].primitive(t).castToDouble(t), t);
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
			return "Creates a new construct that is the \"toString\" of an item."
					+ " Booleans return \"true\""
					+ " or \"false\" and null returns \"null\". Numeric values return their numeric"
					+ " values represented as a string.";
		}
		
		public Argument returnType() {
			return new Argument("The item, cast to a string", CString.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The item to cast", CPrimitive.class, "item")
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

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
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

	/**
	 * Generates the namespace for this value, given an array of constructs. If
	 * the entire list of arguments isn't supposed to be part of the namespace,
	 * the value to be excluded may be specified.
	 *
	 * @param args
	 * @param exclude
	 * @return
	 */
	private static String GetNamespace(Mixed[] args, Integer exclude, String name, Target t) {
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
