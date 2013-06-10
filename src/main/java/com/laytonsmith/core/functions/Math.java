package com.laytonsmith.core.functions;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.compiler.Optimizable;
import com.laytonsmith.core.compiler.Optimizable.OptimizationOption;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.arguments.ArgList;
import com.laytonsmith.core.arguments.Argument;
import com.laytonsmith.core.arguments.ArgumentBuilder;
import com.laytonsmith.core.arguments.Generic;
import com.laytonsmith.core.compiler.OptimizationUtilities;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.functions.Math;
import com.laytonsmith.core.natives.annotations.Ranged;
import com.laytonsmith.core.natives.interfaces.ArrayAccess;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.natives.interfaces.Operators;
import com.sk89q.worldedit.expression.Expression;
import com.sk89q.worldedit.expression.ExpressionException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Layton
 */
public class Math {

	public static String docs() {
		return "Provides mathematical functions to scripts";
	}

	@api
	public static class add extends AbstractFunction implements Optimizable{

		public String getName() {
			return "add";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public Operators.Mathematical exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			List<Operators.Mathematical> numbers = new ArrayList<Operators.Mathematical>();
			numbers.add((Operators.Mathematical)list.get("var1"));
			numbers.add((Operators.Mathematical)list.get("var2"));
			for(Mixed m : (CArray)list.get("varN")){
				numbers.add((Operators.Mathematical)m);
			}
			Operators.Mathematical lhs = numbers.get(0);
			for(int i = 1; i < numbers.size(); i++){
				Operators.Mathematical rhs = numbers.get(i);
				lhs = lhs.operatorAddition(rhs);
			}
			return lhs;
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "Adds all the arguments together, and returns either a double or an integer";
		}

		public Argument returnType() {
			return new Argument("", CNumber.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("", Operators.Mathematical.class, "var1"),
					new Argument("", Operators.Mathematical.class, "var2"),
					new Argument("", CArray.class, "varN").setGenerics(new Generic(Operators.Mathematical.class)).setVarargs()
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
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Demonstrates adding two numbers together", "msg(add(2, 2))"),
						new ExampleScript("Demonstrates adding two numbers together, using the symbol notation", "2 + 2"),
						new ExampleScript("Demonstrates grouping with parenthesis", "(2 + 5) * 2"),
						new ExampleScript("Demonstrates order of operations", "2 + 5 * 2")
					};
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			OptimizationUtilities.pullUpLikeFunctions(children, this.getName());
			return null;
		}

		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.OPTIMIZE_DYNAMIC,
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
	}

	@api
	public static class subtract extends AbstractFunction implements Optimizable{

		public String getName() {
			return "subtract";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public Operators.Mathematical exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			List<Operators.Mathematical> numbers = new ArrayList<Operators.Mathematical>();
			numbers.add((Operators.Mathematical)list.get("var1"));
			numbers.add((Operators.Mathematical)list.get("var2"));
			for(Mixed m : (CArray)list.get("varN")){
				numbers.add((Operators.Mathematical)m);
			}
			Operators.Mathematical lhs = numbers.get(0);
			for(int i = 1; i < numbers.size(); i++){
				Operators.Mathematical rhs = numbers.get(i);
				lhs = lhs.operatorSubtraction(rhs);
			}
			return lhs;
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "Subtracts the arguments from left to right, and returns either a double or an integer";
		}
		
		public Argument returnType() {
			return new Argument("", CNumber.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("", Operators.Mathematical.class, "var1"),
					new Argument("", Operators.Mathematical.class, "var2"),
					new Argument("", CArray.class, "varN").setGenerics(new Generic(Operators.Mathematical.class)).setVarargs()
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
		public ParseTree optimizeDynamic(Target t, Environment env, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			OptimizationUtilities.pullUpLikeFunctions(children, this.getName());
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Demonstrates basic usage", "subtract(4 - 3)"),
						new ExampleScript("Demonstrates symbolic usage", "12 - 5"),};
		}

		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.OPTIMIZE_DYNAMIC,
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
	}

	@api
	public static class multiply extends AbstractFunction implements Optimizable{

		public String getName() {
			return "multiply";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public Operators.Mathematical exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			List<Operators.Mathematical> numbers = new ArrayList<Operators.Mathematical>();
			numbers.add((Operators.Mathematical)list.get("var1"));
			numbers.add((Operators.Mathematical)list.get("var2"));
			for(Mixed m : (CArray)list.get("varN")){
				numbers.add((Operators.Mathematical)m);
			}
			Operators.Mathematical lhs = numbers.get(0);
			for(int i = 1; i < numbers.size(); i++){
				Operators.Mathematical rhs = numbers.get(i);
				lhs = lhs.operatorMultiplication(rhs);
			}
			return lhs;
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "Multiplies the arguments together, and returns either a double or an integer";
		}
		
		public Argument returnType() {
			return new Argument("", CNumber.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("", Operators.Mathematical.class, "var1"),
					new Argument("", Operators.Mathematical.class, "var2"),
					new Argument("", CArray.class, "varN").setGenerics(new Generic(Operators.Mathematical.class)).setVarargs()
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
		public ParseTree optimizeDynamic(Target t, Environment env, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			OptimizationUtilities.pullUpLikeFunctions(children, this.getName());
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Demonstrates basic usage", "multiply(8, 8)"),
						new ExampleScript("Demonstrates symbolic usage", "8 * 8"),};
		}
		
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.OPTIMIZE_DYNAMIC,
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
	}

	@api
	public static class divide extends AbstractFunction implements Optimizable{

		public String getName() {
			return "divide";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public Operators.Mathematical exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			List<Operators.Mathematical> numbers = new ArrayList<Operators.Mathematical>();
			numbers.add((Operators.Mathematical)list.get("var1"));
			numbers.add((Operators.Mathematical)list.get("var2"));
			for(Mixed m : (CArray)list.get("varN")){
				numbers.add((Operators.Mathematical)m);
			}
			Operators.Mathematical lhs = numbers.get(0);
			for(int i = 1; i < numbers.size(); i++){
				Operators.Mathematical rhs = numbers.get(i);
				try{
					lhs = lhs.operatorDivision(rhs);
				} catch(ArithmeticException e){
					throw new ConfigRuntimeException(e.getMessage(), ExceptionType.RangeException, t);
				}
			}
			return lhs;
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.RangeException};
		}

		public String docs() {
			return "Divides the arguments from left to right, and returns either a double or an integer."
					+ " If you divide by zero, a RangeException is thrown.";
		}
		
		public Argument returnType() {
			return new Argument("", CNumber.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("", CNumber.class, "var1"),
					new Argument("", CNumber.class, "var2"),
					new Argument("", CArray.class, "varN").setGenerics(new Generic(CNumber.class)).setVarargs()
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
		public ParseTree optimizeDynamic(Target t, Environment env, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			OptimizationUtilities.pullUpLikeFunctions(children, this.getName());
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Demonstrates basic usage", "divide(4, 2)"),
						new ExampleScript("Demonstrates double return", "divide(2, 4)"),
						new ExampleScript("Demonstrates symbolic usage", "2 / 4"),
						new ExampleScript("Demonstrates divide by zero error", "assign(@zero, 0)\nmsg(1 / @zero)"),};
		}
		
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.OPTIMIZE_DYNAMIC,
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
	}

	@api
	public static class mod extends AbstractFunction implements Optimizable{

		public String getName() {
			return "mod";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Construct exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			long arg1 = args[0].primitive(t).castToInt(t);
			long arg2 = args[1].primitive(t).castToInt(t);
			return new CInt(arg1 % arg2, t);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "Returns x modulo n";
		}
		
		public Argument returnType() {
			return new Argument("", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("", CInt.class, "x"),
					new Argument("", CInt.class, "n")
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
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Demonstrates basic usage", "mod(2, 2)"),
						new ExampleScript("Demonstrates symbolic usage", "2 % 2"),};
		}
		
		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
	}

	@api
	public static class pow extends AbstractFunction implements Optimizable {

		public String getName() {
			return "pow";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Construct exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			double arg1 = args[0].primitive(t).castToDouble(t);
			double arg2 = args[1].primitive(t).castToDouble(t);
			return new CDouble(java.lang.Math.pow(arg1, arg2), t);
		}

		public String docs() {
			return "Returns x to the power of n";
		}
		
		public Argument returnType() {
			return new Argument("x to the power of n", CDouble.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("", CNumber.class, "x"),
					new Argument("", CNumber.class, "n")
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
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
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Demonstrates basic usage", "pow(2, 4)"),
						new ExampleScript("Demonstrates symbolic usage", "2 ** 4"),};
		}
		
		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
	}
	
	/**
	 * If we have the case {@code @array[0]++}, we have to increment it as
	 * though it were a variable, so we have to do that with execs. This method
	 * consolidates the code to do so.
	 * @return 
	 */
	private static Mixed doIncrementDecrement(ParseTree[] nodes, 
			Script parent, Environment env, Target t, 
			Function func, boolean pre, boolean inc){
		if(nodes[0].getData() instanceof CFunction){
				Function f = ((CFunction)nodes[0].getData()).getFunction();
				if(f.getName().equals(new ArrayHandling.array_get().getName())){
					//Ok, so, this is it, we're in charge here.
					long temp;
					long newVal;
					//First, pull out the current value. We're gonna do this manually though, and we will actually
					//skip the whole array_get execution.
					ParseTree eval = nodes[0];
					Mixed array = parent.seval(eval.getChildAt(0), env);
					CString index = parent.seval(eval.getChildAt(1), env).primitive(t).castToCString();
					Mixed cdelta = new CInt(1, t);
					if(nodes.length == 2){
						cdelta = parent.seval(nodes[1], env);
					}
					long delta = cdelta.primitive(t).castToInt(t);
					//First, error check, then get the old value, and store it in temp.
					if(!(array instanceof CArray) && !(array instanceof ArrayAccess)){
						//Let's just evaluate this like normal with array_get, so it will
						//throw the appropriate exception.
						new ArrayHandling.array_get().exec(t, env, array, index);
						throw ConfigRuntimeException.CreateUncatchableException("Shouldn't have gotten here. Please report this error, and how you got here.", t);
					} else if(!(array instanceof CArray)){
						//It's an ArrayAccess type, but we can't use that here, so, throw our
						//own exception.
						throw new ConfigRuntimeException("Cannot increment/decrement a non-array array"
								+ " accessed value. (The value passed in was \"" + array.val() + "\")", ExceptionType.CastException, t);
					} else {
						//Ok, we're good. Data types should all be correct.
						CArray myArray = ((CArray)array);
						Mixed value = myArray.get(index, t);
						if(value instanceof CInt || value instanceof CDouble){
							temp = value.primitive(t).castToInt(t);
							//Alright, now let's actually perform the increment, and store that in the array.
							
							if(inc){
								newVal = temp + delta;
							} else {
								newVal = temp - delta;
							}
							new ArrayHandling.array_set().exec(t, env, array, index, new CInt(newVal, t));
						} else {
							throw new ConfigRuntimeException("Cannot increment/decrement a non numeric value.", ExceptionType.CastException, t);
						}
					}
					long valueToReturn;
					if(pre){
						valueToReturn = newVal;
					} else {
						valueToReturn = temp;
					}
					return new CInt(valueToReturn, t);
				}
			}
			Mixed [] args = new Construct[nodes.length];
			for(int i = 0; i < args.length; i++){
				args[i] = parent.eval(nodes[i], env);
			}
			return func.exec(t, env, args);
	}

	@api
	public static class inc extends AbstractFunction implements Optimizable{

		public String getName() {
			return "inc";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}
		
		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			return doIncrementDecrement(nodes, parent, env, t, this, true, true);
		}
		
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			long value = 1;
			if (args.length == 2) {
				if (args[1] instanceof IVariable) {
					IVariable cur2 = (IVariable) args[1];
					args[1] = env.getEnv(GlobalEnv.class).GetVarList().get(cur2, cur2.getTarget());
				}
				value = args[1].primitive(t).castToInt(t);
			}
			if (args[0] instanceof IVariable) {
				IVariable cur = (IVariable) args[0];
				Mixed v = env.getEnv(GlobalEnv.class).GetVarList().get(cur, cur.getTarget());
				Mixed newVal;
				if (v instanceof CDouble) {
					newVal = new CDouble(v.primitive(t).castToDouble(t) + value, t);
				} else {
					newVal = new CInt(v.primitive(t).castToInt(t) + value, t);
				}
				env.getEnv(GlobalEnv.class).GetVarList().set(cur, newVal);
				return newVal;
			} else {
				if (args[0] instanceof CDouble) {
					return new CDouble(args[0].primitive(t).castToDouble(t) + value, t);
				} else {
					return new CInt(args[0].primitive(t).castToInt(t) + value, t);
				}
			}

		}

		public String docs() {
			return "Adds x to var, and stores the new value. Equivalent to ++var in other languages. Expects ivar to be a variable, then"
					+ " returns the ivar, or, if var is a constant number, simply adds x to it, and returns the new number.";
		}
		
		public Argument returnType() {
			return new Argument("The ivar meta object, or just the number, if it was hardcoded.", IVariable.class, CNumber.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The numeric value", IVariable.class, CNumber.class, "var"),
					new Argument("The amount to increment", CInt.class, "x").setOptionalDefault(1)
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
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
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Demonstrates basic usage", "assign(@x, 0)\nmsg(@x)\ninc(@x)\nmsg(@x)"),
						new ExampleScript("Demonstrates symbolic usage", "assign(@x, 0)\nmsg(@x)\n++@x\nmsg(@x)"),};
		}

		@Override
		public Mixed optimize(Target t, Environment env, Mixed... args) throws ConfigCompileException {
			if(args[0] instanceof IVariable){
				return null;
			} else {
				return exec(t, null, args);
			}
		}				
		
		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.OPTIMIZE_CONSTANT
			);
		}
	}

	@api
	public static class postinc extends AbstractFunction implements Optimizable {

		public String getName() {
			return "postinc";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}
		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			return doIncrementDecrement(nodes, parent, env, t, this, false, true);
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			long value = 1;
			if (args.length == 2) {
				if (args[1] instanceof IVariable) {
					IVariable cur2 = (IVariable) args[1];
					args[1] = env.getEnv(GlobalEnv.class).GetVarList().get(cur2, cur2.getTarget());
				}
				value = args[1].primitive(t).castToInt(t);
			}
			if (args[0] instanceof IVariable) {
				IVariable cur = (IVariable) args[0];
				Mixed v = env.getEnv(GlobalEnv.class).GetVarList().get(cur, cur.getTarget());
				Mixed newVal;
				if (v instanceof CDouble) {
					newVal = new CDouble(v.primitive(t).castToDouble(t) + value, t);
				} else {
					newVal = new CInt(v.primitive(t).castToInt(t) + value, t);
				}
				Mixed oldVal = null;
				oldVal = v.doClone();
				env.getEnv(GlobalEnv.class).GetVarList().set(cur, v);
				return oldVal;
			} else {
				if (args[0] instanceof CDouble) {
					return new CDouble(args[0].primitive(t).castToDouble(t) + value, t);
				} else {
					return new CInt(args[0].primitive(t).castToInt(t) + value, t);
				}
			}
		}


		public String docs() {
			return "Adds x to var, and stores the new value. Equivalent to var++ in other languages. Expects ivar to be a variable, then"
					+ " returns a copy of the old ivar, or, if var is a constant number, simply adds x to it, and returns the new number.";
		}
		
		public Argument returnType() {
			return new Argument("The ivar meta object, or just the number, if it was hardcoded.", IVariable.class, CNumber.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The numeric value", IVariable.class, CNumber.class, "var"),
					new Argument("The amount to increment", CInt.class, "x").setOptionalDefault(1)
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return false;
		}

		@Override
		public boolean preResolveVariables() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed optimize(Target t, Environment env, Mixed... args) throws ConfigCompileException {
			if(args[0] instanceof IVariable){
				return null;
			} else {
				return exec(t, null, args);
			}
		}				
		
		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.OPTIMIZE_CONSTANT
			);
		}
	}

	@api
	public static class dec extends AbstractFunction implements Optimizable{

		public String getName() {
			return "dec";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}
		
		@Override
		public boolean useSpecialExec() {
			return true;
		}
		
		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			return doIncrementDecrement(nodes, parent, env, t, this, true, false);
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			long value = 1;
			if (args.length == 2) {
				if (args[1] instanceof IVariable) {
					IVariable cur2 = (IVariable) args[1];
					args[1] = env.getEnv(GlobalEnv.class).GetVarList().get(cur2, cur2.getTarget());
				}
				value = args[1].primitive(t).castToInt(t);
			}
			if (args[0] instanceof IVariable) {
				IVariable cur = (IVariable) args[0];
				Mixed v = env.getEnv(GlobalEnv.class).GetVarList().get(cur, cur.getTarget());
				Construct newVal;
				if (v instanceof CDouble) {
					newVal = new CDouble(v.primitive(t).castToDouble(t) - value, t);
				} else {
					newVal = new CInt(v.primitive(t).castToInt(t) - value, t);
				}
				env.getEnv(GlobalEnv.class).GetVarList().set(cur, v);
				return v;
			} else {
				if (args[0] instanceof CDouble) {
					return new CDouble(args[0].primitive(t).castToDouble(t) + value, t);
				} else {
					return new CInt(args[0].primitive(t).castToInt(t) + value, t);
				}
			}
		}

		public String docs() {
			return "Subtracts value from var, and stores the new value. Value defaults to 1. Equivalent to --var (or var -= value) in other languages. Expects ivar to be a variable, then"
					+ " returns the ivar, , or, if var is a constant number, simply subtracts x from it, and returns the new number.";
		}
		
		public Argument returnType() {
			return new Argument("The ivar meta object, or just the number, if it was hardcoded.", IVariable.class, CNumber.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The numeric value", IVariable.class, CNumber.class, "var"),
					new Argument("The amount to decrement", CInt.class, "x").setOptionalDefault(1)
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
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
		public Mixed optimize(Target t, Environment env, Mixed... args) throws ConfigCompileException {
			if(args[0] instanceof IVariable){
				return null;
			} else {
				return exec(t, null, args);
			}
		}				
		
		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.OPTIMIZE_CONSTANT
			);
		}
	}

	@api
	public static class postdec extends AbstractFunction implements Optimizable {

		public String getName() {
			return "postdec";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}
		
		@Override
		public boolean useSpecialExec() {
			return true;
		}
		
		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			return doIncrementDecrement(nodes, parent, env, t, this, false, false);
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			long value = 1;
			if (args.length == 2) {
				if (args[1] instanceof IVariable) {
					IVariable cur2 = (IVariable) args[1];
					args[1] = env.getEnv(GlobalEnv.class).GetVarList().get(cur2, cur2.getTarget());
				}
				value = args[1].primitive(t).castToInt(t);
			}
			if (args[0] instanceof IVariable) {
				IVariable cur = (IVariable) args[0];
				Mixed v = env.getEnv(GlobalEnv.class).GetVarList().get(cur, cur.getTarget());
				Mixed newVal;
				if (v instanceof CDouble) {
					newVal = new CDouble(v.primitive(t).castToDouble(t) - value, t);
				} else {
					newVal = new CInt(v.primitive(t).castToInt(t) - value, t);
				}
				Mixed oldVal = null;
				oldVal = v.doClone();
				env.getEnv(GlobalEnv.class).GetVarList().set(cur, v);
				return oldVal;
			} else {
				if (args[0] instanceof CDouble) {
					return new CDouble(args[0].primitive(t).castToDouble(t) + value, t);
				} else {
					return new CInt(args[0].primitive(t).castToInt(t) + value, t);
				}
			}
		}

		public String docs() {
			return "Subtracts x from var, and stores the new value. Equivalent to var-- in other languages. Expects ivar to be a variable, then"
					+ " returns a copy of the old ivar, , or, if var is a constant number, simply subtracts x from it, and returns the new number.";
		}
		
		public Argument returnType() {
			return new Argument("The ivar meta object, or just the number, if it was hardcoded.", IVariable.class, CNumber.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The numeric value", IVariable.class, CNumber.class, "var"),
					new Argument("The amount to decrement", CInt.class, "x").setOptionalDefault(1)
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return false;
		}

		@Override
		public boolean preResolveVariables() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed optimize(Target t, Environment env, Mixed... args) throws ConfigCompileException {
			if(args[0] instanceof IVariable){
				return null;
			} else {
				return exec(t, null, args);
			}
		}				
		
		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.OPTIMIZE_CONSTANT
			);
		}
	}

	@api
	public static class rand extends AbstractFunction {

		Random r = new Random();

		public String getName() {
			return "rand";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1, 2};
		}

		public String docs() {
			return "Returns a random number from 0 to max, or min to max, depending on usage. Max is exclusive. Min must"
					+ " be less than max, and both numbers must be >= 0. This will return an integer. Alternatively, you can pass no arguments, and a random"
					+ " double, from 0 to 1 will be returned.";
		}
		
		public Argument returnType() {
			return new Argument("The randomly generated value", CNumber.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("If both arguments are provided, this is the min, otherwise, it is the max", CInt.class, "min_max").setOptional(),
					new Argument("The max value, exclusive", CInt.class, "max").setOptional()
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.RangeException, ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		public Construct exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			if(args.length == 0){
				return new CDouble(java.lang.Math.random(), t);
			} else {
				long min = 0;
				long max = 0;
				if (args.length == 1) {
					max = args[0].primitive(t).castToInt(t);
				} else {
					min = args[0].primitive(t).castToInt(t);
					max = args[1].primitive(t).castToInt(t);
				}
				if (max > Integer.MAX_VALUE || min > Integer.MAX_VALUE) {
					throw new ConfigRuntimeException("max and min must be below int max, defined as " + Integer.MAX_VALUE,
							ExceptionType.RangeException,
							t);
				}

				long range = max - min;
				if (range <= 0) {
					throw new ConfigRuntimeException("max - min must be greater than 0",
							ExceptionType.RangeException, t);
				}
				long rand = java.lang.Math.abs(r.nextLong());
				long i = (rand % (range)) + min;

				return new CInt(i, t);
			}
		}

		public Boolean runAsync() {
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage, with one paramter", "rand(10)"),
				new ExampleScript("Basic usage, with a range", "rand(50, 100)"),
				new ExampleScript("Usage with no parameters", "rand()")
			};
		}
				
	}

	@api
	public static class abs extends AbstractFunction implements Optimizable {

		public String getName() {
			return "abs";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Returns the absolute value of the argument.";
		}
		
		public Argument returnType() {
			return new Argument("The absolute value of arg", CDouble.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The value to find the absolute value of", CNumber.class, "arg")
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
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

		public Construct exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			double d = getBuilder().parse(args, this, t).getDouble("arg", t);
			return new CDouble(java.lang.Math.abs(d), t);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Demonstrates a positive number", "abs(5)"),
						new ExampleScript("Demonstrates a negative number", "abs(-5)")
					};
		}
		
		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
	}

	@api
	public static class floor extends AbstractFunction implements Optimizable {

		public String getName() {
			return "floor";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Returns the floor of any given number. For example, floor(3.8) returns 3, and floor(-1.1) returns 2";
		}
		
		public Argument returnType() {
			return new Argument("The floor of the number given", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The value to find the floor of", CNumber.class, "number")
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_1_3;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return new CInt((long) java.lang.Math.floor(getBuilder().parse(args, this, t).getDouble("number", t)), t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
	}

	@api
	public static class ceil extends AbstractFunction implements Optimizable {

		public String getName() {
			return "ceil";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Returns the ceiling of any given number. For example, ceil(3.2) returns 4, and ceil(-1.1) returns -1";
		}
		
		public Argument returnType() {
			return new Argument("The ceiling of the given number", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The value to find the ceiling of", CNumber.class, "number")
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_1_3;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return new CInt((long) java.lang.Math.ceil(getBuilder().parse(args, this, t).getDouble("number", t)), t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
	}

	@api
	public static class sqrt extends AbstractFunction implements Optimizable{

		public String getName() {
			return "sqrt";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Returns the square root of a number. Note that this is mathematically equivalent to pow(number, .5)."
					+ " Imaginary numbers are not supported, so number must be positive.";
		}
		
		public Argument returnType() {
			return new Argument("The square root", CDouble.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The number to find the square root of", CDouble.class, "number").addAnnotation(new Ranged(0, Double.MAX_VALUE))
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.RangeException, ExceptionType.CastException};
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

		public Construct exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			double d = list.getDouble("number", t);
			double m = java.lang.Math.sqrt(d);
			if (m == (int) m) {
				return new CInt((long) m, t);
			} else {
				return new CDouble(m, t);
			}
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
	}

	@api
	public static class min extends AbstractFunction implements Optimizable {

		public String getName() {
			return "min";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public String docs() {
			return "Returns the lowest number in a given list of numbers. If any of the arguments"
					+ " are arrays, they are expanded into individual numbers, and also compared.";
		}
		
		public Argument returnType() {
			return new Argument("The lowest value in the given set of numbers", CNumber.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The first value to consider", CNumber.class, "num1"),
					new Argument("Extra values to consider", CArray.class, "numX").setGenerics(new Generic(CNumber.class)).setVarargs()
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.InsufficientArgumentsException};
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

		public Construct exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			if (args.length == 0) {
				throw new ConfigRuntimeException("You must send at least one parameter to min",
						ExceptionType.InsufficientArgumentsException, t);
			}
			double lowest = Double.POSITIVE_INFINITY;
			List<Mixed> list = new ArrayList<Mixed>();
			recList(list, args);
			for (Mixed c : list) {
				double d = c.primitive(t).castToDouble(t);
				if (d < lowest) {
					lowest = d;
				}
			}
			if (lowest == (long) lowest) {
				return new CInt((long) lowest, t);
			} else {
				return new CDouble(lowest, t);
			}
		}

		public List<Mixed> recList(List<Mixed> list, Mixed... args) {
			for (Mixed c : args) {
				if (c instanceof CArray) {
					for (int i = 0; i < ((CArray) c).size(); i++) {
						recList(list, ((CArray) c).get(i, Target.UNKNOWN));
					}
				} else {
					list.add(c);
				}
			}
			return list;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
	}

	@api
	public static class max extends AbstractFunction implements Optimizable{

		public String getName() {
			return "max";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public String docs() {
			return "Returns the highest number in a given list of numbers. If any of the arguments"
					+ " are arrays, they are expanded into individual numbers, and also compared.";
		}
		
		public Argument returnType() {
			return new Argument("The largest value in the given set", CNumber.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The first value to consider", CNumber.class, "num1"),
					new Argument("Extra values to consider", CArray.class, "numX").setGenerics(new Generic(CNumber.class)).setVarargs()
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.InsufficientArgumentsException};
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

		public Construct exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			if (args.length == 0) {
				throw new ConfigRuntimeException("You must send at least one parameter to max",
						ExceptionType.InsufficientArgumentsException, t);
			}
			double highest = Double.NEGATIVE_INFINITY;
			List<Mixed> list = new ArrayList<Mixed>();
			recList(list, args);
			for (Mixed c : list) {
				double d = c.primitive(t).castToDouble(t);
				if (d > highest) {
					highest = d;
				}
			}
			if (highest == (long) highest) {
				return new CInt((long) highest, t);
			} else {
				return new CDouble(highest, t);
			}
		}

		public List<Mixed> recList(List<Mixed> list, Mixed... args) {
			for (Mixed c : args) {
				if (c instanceof CArray) {
					for (int i = 0; i < ((CArray) c).size(); i++) {
						recList(list, ((CArray) c).get(i, Target.UNKNOWN));
					}
				} else {
					list.add(c);
				}
			}
			return list;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
	}

	@api
	public static class sin extends AbstractFunction implements Optimizable {

		public String getName() {
			return "sin";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Returns the sin of the number";
		}
		
		public Argument returnType() {
			return new Argument("The sine of the number", CDouble.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The value to find the sine of", CNumber.class, "number")
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

		public Construct exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return new CDouble(java.lang.Math.sin(getBuilder().parse(args, this, t).getDouble("number", t)), t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
	}

	@api
	public static class cos extends AbstractFunction implements Optimizable {

		public String getName() {
			return "cos";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Returns the cos of the number";
		}
		
		public Argument returnType() {
			return new Argument("The cosine of the number", CDouble.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The value to find the cosine of", CNumber.class, "number")
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

		public Construct exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return new CDouble(java.lang.Math.cos(getBuilder().parse(args, this, t).getDouble("number", t)), t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
	}

	@api
	public static class tan extends AbstractFunction implements Optimizable {

		public String getName() {
			return "tan";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Returns the tan of the number";
		}
		
		public Argument returnType() {
			return new Argument("The tangent of the number", CDouble.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The value to find the tangent of", CNumber.class, "number")
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

		public Construct exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return new CDouble(java.lang.Math.tan(getBuilder().parse(args, this, t).getDouble("number", t)), t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
	}

	@api
	public static class asin extends AbstractFunction implements Optimizable {

		public String getName() {
			return "asin";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Returns the arc sin of the number";
		}
		
		public Argument returnType() {
			return new Argument("The arc sine of the number", CDouble.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The number to find the arc sine of", CNumber.class, "number")
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

		public Construct exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return new CDouble(java.lang.Math.asin(getBuilder().parse(args, this, t).getDouble("number", t)), t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
	}

	@api
	public static class acos extends AbstractFunction implements Optimizable {

		public String getName() {
			return "acos";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Returns the arc cos of the number";
		}
		
		public Argument returnType() {
			return new Argument("The arc cosine of the number", CDouble.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The value to find the arc cosine of", CNumber.class, "number")
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

		public Construct exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return new CDouble(java.lang.Math.acos(getBuilder().parse(args, this, t).getDouble("number", t)), t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
	}

	@api
	public static class atan extends AbstractFunction implements Optimizable {

		public String getName() {
			return "atan";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Returns the arc tan of the number";
		}
		
		public Argument returnType() {
			return new Argument("The arc tangent of number", CDouble.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The number to find the arc tangent of", CNumber.class, "number")
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

		public Construct exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return new CDouble(java.lang.Math.atan(getBuilder().parse(args, this, t).getDouble("number", t)), t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
	}

	@api
	public static class to_radians extends AbstractFunction implements Optimizable{

		public String getName() {
			return "to_radians";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Converts the number to radians (which is assumed to have been in degrees)";
		}
		
		public Argument returnType() {
			return new Argument("The convered number, in radians", CDouble.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The value to convert, in degrees", CNumber.class, "number")
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

		public Construct exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return new CDouble(java.lang.Math.toRadians(getBuilder().parse(args, this, t).getDouble("number", t)), t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
	}

	@api
	public static class to_degrees extends AbstractFunction implements Optimizable{

		public String getName() {
			return "to_degrees";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "double {number} Converts the number to degrees (which is assumed to have been in radians)";
		}
		
		public Argument returnType() {
			return new Argument("The converted number, now in degrees", CDouble.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The number to convert, in radians", CNumber.class, "number")
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

		public Construct exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return new CDouble(java.lang.Math.toDegrees(getBuilder().parse(args, this, t).getDouble("number", t)), t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
	}

	@api
	public static class atan2 extends AbstractFunction implements Optimizable {

		public String getName() {
			return "atan2";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			//lolcopypaste
			return "Returns the angle theta from the conversion"
					+ " of rectangular coordinates (x, y) to polar coordinates"
					+ " (r, theta). This method computes the phase theta by"
					+ " computing an arc tangent of y/x in the range of -pi to pi.";
		}
		
		public Argument returnType() {
			return new Argument("The converted angle", CDouble.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The first value", CNumber.class, "y"),
					new Argument("The second value", CNumber.class, "x")
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

		public Construct exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			double y = list.getDouble("y", t);
			double x = list.getDouble("x", t);
			return new CDouble(java.lang.Math.atan2(y, x), t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
	}

	@api
	public static class round extends AbstractFunction implements Optimizable{

		public String getName() {
			return "round";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Unlike floor and ceil, rounds the number to the nearest integer. Precision defaults to 0, but if set to 1 or more, rounds decimal places."
					+ " For instance, round(2.29, 1) would return 2.3. If precision is < 0, a RangeException is thrown. If precision is set to 0, an integer is always"
					+ " returned, and if precision is > 0, a double is always returned.";
		}
		
		public Argument returnType() {
			return new Argument("The rounded number", CNumber.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The number to round", CNumber.class, "number"),
					new Argument("The precision", CInt.class, "precision").addAnnotation(new Ranged(0, Integer.MAX_VALUE)).setOptionalDefault(0)
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.RangeException};
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

		public Construct exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			double number = list.getDouble("number", t);
			int precision = list.getInt("precision", t);
			number = number * java.lang.Math.pow(10, precision);
			number = java.lang.Math.round(number);
			number = number / java.lang.Math.pow(10, precision);
			if(precision == 0){
				return new CInt((long)number, t);
			} else {
				return new CDouble(number, t);
			}
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Rounding up", "round(2.5)"),
				new ExampleScript("Rounding down", "round(2.229)"),
				new ExampleScript("Higher precision round", "round(2.229, 2)"),
			};
		}
		
		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
		
	}

	@api
	public static class expr extends AbstractFunction implements Optimizable {

		public String getName() {
			return "expr";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "double {expression, [valueArray]} Sometimes, when you need to calculate an advanced"
					+ " mathematical expression, it is messy to write out everything in terms of functions."
					+ " This function will allow you to evaluate a mathematical expression as a string, using"
					+ " common mathematical notation. For example, (2 + 3) * 4 would return 20. Variables can"
					+ " also be included, and their values given as an associative array. expr('(x + y) * z',"
					+ " array(x: 2, y: 3, z: 4)) would be the same thing as the above example.";
		}
		
		public Argument returnType() {
			return new Argument("The evaluated expression", CDouble.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The expression to evaluate", CString.class, "expression"),
					new Argument("The array of values to fill in", CArray.class, "valueArray").setOptional()
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.PluginInternalException};
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

		public Construct exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String expr = args[0].val();
			CArray vars = null;
			if (args.length == 2 && args[1] instanceof CArray) {
				vars = (CArray) args[1];
			} else if (args.length == 2 && !(args[1] instanceof CArray)) {
				throw new ConfigRuntimeException("The second argument of expr() should be an array", ExceptionType.CastException, t);
			}
			if (vars != null && !vars.inAssociativeMode()) {
				throw new ConfigRuntimeException("The array provided to expr() must be an associative array", ExceptionType.CastException, t);
			}
			double[] da;
			String[] varNames;
			if (vars != null) {
				int i = 0;
				da = new double[(int)vars.size()];
				varNames = new String[(int)vars.size()];
				for (String key : vars.keySet()) {
					varNames[i] = key;
					da[i] = vars.get(key, t).primitive(t).castToDouble(t);
					i++;
				}
			} else {
				da = new double[0];
				varNames = new String[0];
			}
			try {
				Expression e = Expression.compile(expr, varNames);
				return new CDouble(e.evaluate(da), t);
			} catch (ExpressionException ex) {
				throw new ConfigRuntimeException("Your expression was invalidly formatted", ExceptionType.PluginInternalException, t, ex);
			}
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
	}

	@api
	public static class neg extends AbstractFunction implements Optimizable {

		public String getName() {
			return "neg";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Negates a number, essentially multiplying the number by -1";
		}
		
		public Argument returnType() {
			return new Argument("The inverted number", CNumber.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The number to invert", CNumber.class, "number")
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

		public Construct exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			CNumber number = getBuilder().parse(args, this, t).get("number");
			if (number instanceof CInt) {
				return new CInt(-(number.castToInt(t)), t);
			} else {
				return new CDouble(-(number.castToDouble(t)), t);
			}
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
	}
	
	@api
	public static class logarithm extends AbstractFunction implements Optimizable {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.RangeException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			double val = list.getDouble("val", t);
			double base = list.getDouble("base", t);
			double r = java.lang.Math.log(val) / java.lang.Math.log(base);
			return new CDouble(r, t);
		}

		public String getName() {
			return "logarithm";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "double {val, [base]} Return the log of a number to the specified base, or the mathematical"
					+ " constant e if no base is provided (that is, the natural log, or \"ln\")."
					+ " If val is less than or equal to zero, a RangeException is thrown."
					+ " Mathematically speaking, if val is 0, then the result would be negative infinity, and if it"
					+ " is less than 0 it is undefined (NaN), but since MethodScript has no way of representing either"
					+ " of these, a RangeException is thrown instead.";
		}
		
		public Argument returnType() {
			return new Argument("The log value", CDouble.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("", CDouble.class, "val").addAnnotation(new Ranged(0.00000000000001, Double.MAX_VALUE)),
					new Argument("", CDouble.class, "base").setOptionalDefault(java.lang.Math.E)
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.CONSTANT_OFFLINE);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("log base e (mathematical equivalent of ln)", "logarithm(1)"),
				new ExampleScript("log base e (mathematical equivalent of ln)", "logarithm(3)"),
				new ExampleScript("log base 10", "logarithm(100)"),
				new ExampleScript("log base 10", "logarithm(1000)"),
				new ExampleScript("log base n", "logarithm(123, 3)"),
				new ExampleScript("Error condition", "logarithm(0)"),
				new ExampleScript("Error condition", "logarithm(-1)"),
			};
		}		
		
	}
}
