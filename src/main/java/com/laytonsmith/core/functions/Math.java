package com.laytonsmith.core.functions;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.Optimizable.OptimizationOption;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.OptimizationUtilities;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.interfaces.ArrayAccess;
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

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			double tally = Static.getNumber(args[0]);
			for (int i = 1; i < args.length; i++) {
				tally += Static.getNumber(args[i]);
			}
			if (Static.anyDoubles(args)) {
				return new CDouble(tally, t);
			} else {
				return new CInt((long) tally, t);
			}
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "mixed {var1, [var2...]} Adds all the arguments together, and returns either a double or an integer";
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
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
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

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			double tally = Static.getNumber(args[0]);
			for (int i = 1; i < args.length; i++) {
				tally -= Static.getNumber(args[i]);
			}
			if (Static.anyDoubles(args)) {
				return new CDouble(tally, t);
			} else {
				return new CInt((long) tally, t);
			}
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "mixed {var1, [var2...]} Subtracts the arguments from left to right, and returns either a double or an integer";
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
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
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

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			double tally = Static.getNumber(args[0]);
			for (int i = 1; i < args.length; i++) {
				tally *= Static.getNumber(args[i]);
			}
			if (Static.anyDoubles(args)) {
				return new CDouble(tally, t);
			} else {
				return new CInt((long) tally, t);
			}
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "mixed {var1, [var2...]} Multiplies the arguments together, and returns either a double or an integer";
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
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
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

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			double tally = Static.getNumber(args[0]);
			for (int i = 1; i < args.length; i++) {
				double next = Static.getNumber(args[i]);
				if (next == 0) {
					throw new ConfigRuntimeException("Division by 0!", ExceptionType.RangeException, t);
				}
				tally /= next;
			}
			if (tally == (int) tally) {
				return new CInt((long) tally, t);
			} else {
				return new CDouble(tally, t);
			}
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.RangeException};
		}

		public String docs() {
			return "mixed {var1, [var2...]} Divides the arguments from left to right, and returns either a double or an integer."
					+ " If you divide by zero, a RangeException is thrown.";
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
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
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

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			long arg1 = Static.getInt(args[0]);
			long arg2 = Static.getInt(args[1]);
			return new CInt(arg1 % arg2, t);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "int {x, n} Returns x modulo n";
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

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			double arg1 = Static.getNumber(args[0]);
			double arg2 = Static.getNumber(args[1]);
			return new CDouble(java.lang.Math.pow(arg1, arg2), t);
		}

		public String docs() {
			return "double {x, n} Returns x to the power of n";
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
	private static Construct doIncrementDecrement(ParseTree[] nodes, 
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
					Construct array = parent.seval(eval.getChildAt(0), env);
					Construct index = parent.seval(eval.getChildAt(1), env);
					Construct cdelta = new CInt(1, t);
					if(nodes.length == 2){
						cdelta = parent.seval(nodes[1], env);
					}
					long delta = Static.getInt(cdelta);
					//First, error check, then get the old value, and store it in temp.
					if(!(array instanceof CArray) && !(array instanceof ArrayAccess)){
						//Let's just evaluate this like normal with array_get, so it will
						//throw the appropriate exception.
						new ArrayHandling.array_get().exec(t, env, array, index);
						throw new ConfigRuntimeException("Shouldn't have gotten here. Please report this error, and how you got here.", t);
					} else if(!(array instanceof CArray)){
						//It's an ArrayAccess type, but we can't use that here, so, throw our
						//own exception.
						throw new ConfigRuntimeException("Cannot increment/decrement a non-array array"
								+ " accessed value. (The value passed in was \"" + array.val() + "\")", ExceptionType.CastException, t);
					} else {
						//Ok, we're good. Data types should all be correct.
						CArray myArray = ((CArray)array);
						Construct value = myArray.get(index, t);
						if(value instanceof CInt || value instanceof CDouble){
							temp = Static.getInt(value);
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
			Construct [] args = new Construct[nodes.length];
			for(int i = 0; i < args.length; i++){
				args[i] = parent.eval(nodes[i], env);
			}
			return func.exec(t, env, args);
	}

	@api(environments=CommandHelperEnvironment.class)
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
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			return doIncrementDecrement(nodes, parent, env, t, this, true, true);
		}
		
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			long value = 1;
			if (args.length == 2) {
				if (args[1] instanceof IVariable) {
					IVariable cur2 = (IVariable) args[1];
					args[1] = env.getEnv(CommandHelperEnvironment.class).GetVarList().get(cur2.getName(), cur2.getTarget());
				}
				value = Static.getInt(args[1]);
			}
			if (args[0] instanceof IVariable) {
				IVariable cur = (IVariable) args[0];
				IVariable v = env.getEnv(CommandHelperEnvironment.class).GetVarList().get(cur.getName(), cur.getTarget());
				Construct newVal;
				if (Static.anyDoubles(v.ival())) {
					newVal = new CDouble(Static.getDouble(v.ival()) + value, t);
				} else {
					newVal = new CInt(Static.getInt(v.ival()) + value, t);
				}
				v = new IVariable(v.getName(), newVal, t);
				env.getEnv(CommandHelperEnvironment.class).GetVarList().set(v);
				return v;
			} else {
				if (Static.anyDoubles(args[0])) {
					return new CDouble(Static.getNumber(args[0]) + value, t);
				} else {
					return new CInt(Static.getInt(args[0]) + value, t);
				}
			}

		}

		public String docs() {
			return "ivar {var, [x]} Adds x to var, and stores the new value. Equivalent to ++var in other languages. Expects ivar to be a variable, then"
					+ " returns the ivar, or, if var is a constant number, simply adds x to it, and returns the new number.";
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
		public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
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

	@api(environments=CommandHelperEnvironment.class)
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
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			return doIncrementDecrement(nodes, parent, env, t, this, false, true);
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			long value = 1;
			if (args.length == 2) {
				if (args[1] instanceof IVariable) {
					IVariable cur2 = (IVariable) args[1];
					args[1] = env.getEnv(CommandHelperEnvironment.class).GetVarList().get(cur2.getName(), cur2.getTarget());
				}
				value = Static.getInt(args[1]);
			}
			if (args[0] instanceof IVariable) {
				IVariable cur = (IVariable) args[0];
				IVariable v = env.getEnv(CommandHelperEnvironment.class).GetVarList().get(cur.getName(), cur.getTarget());
				Construct newVal;
				if (Static.anyDoubles(v.ival())) {
					newVal = new CDouble(Static.getDouble(v.ival()) + value, t);
				} else {
					newVal = new CInt(Static.getInt(v.ival()) + value, t);
				}
				Construct oldVal = null;
				try {
					oldVal = v.ival().clone();
				} catch (CloneNotSupportedException ex) {
					Logger.getLogger(Math.class.getName()).log(Level.SEVERE, null, ex);
				}
				v = new IVariable(v.getName(), newVal, t);
				env.getEnv(CommandHelperEnvironment.class).GetVarList().set(v);
				return oldVal;
			} else {
				if (Static.anyDoubles(args[0])) {
					return new CDouble(Static.getNumber(args[0]) + value, t);
				} else {
					return new CInt(Static.getInt(args[0]) + value, t);
				}
			}
		}


		public String docs() {
			return "ivar {var, [x]} Adds x to var, and stores the new value. Equivalent to var++ in other languages. Expects ivar to be a variable, then"
					+ " returns a copy of the old ivar, or, if var is a constant number, simply adds x to it, and returns the new number.";
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
		public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
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

	@api(environments=CommandHelperEnvironment.class)
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
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			return doIncrementDecrement(nodes, parent, env, t, this, true, false);
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			long value = 1;
			if (args.length == 2) {
				if (args[1] instanceof IVariable) {
					IVariable cur2 = (IVariable) args[1];
					args[1] = env.getEnv(CommandHelperEnvironment.class).GetVarList().get(cur2.getName(), cur2.getTarget());
				}
				value = Static.getInt(args[1]);
			}
			if (args[0] instanceof IVariable) {
				IVariable cur = (IVariable) args[0];
				IVariable v = env.getEnv(CommandHelperEnvironment.class).GetVarList().get(cur.getName(), cur.getTarget());
				Construct newVal;
				if (Static.anyDoubles(v.ival())) {
					newVal = new CDouble(Static.getDouble(v.ival()) - value, t);
				} else {
					newVal = new CInt(Static.getInt(v.ival()) - value, t);
				}
				v = new IVariable(v.getName(), newVal, t);
				env.getEnv(CommandHelperEnvironment.class).GetVarList().set(v);
				return v;
			} else {
				if (Static.anyDoubles(args[0])) {
					return new CDouble(Static.getNumber(args[0]) + value, t);
				} else {
					return new CInt(Static.getInt(args[0]) + value, t);
				}
			}
		}

		public String docs() {
			return "ivar {var, [value]} Subtracts value from var, and stores the new value. Value defaults to 1. Equivalent to --var (or var -= value) in other languages. Expects ivar to be a variable, then"
					+ " returns the ivar, , or, if var is a constant number, simply adds x to it, and returns the new number.";
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
		public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
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

	@api(environments=CommandHelperEnvironment.class)
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
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			return doIncrementDecrement(nodes, parent, env, t, this, false, false);
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			long value = 1;
			if (args.length == 2) {
				if (args[1] instanceof IVariable) {
					IVariable cur2 = (IVariable) args[1];
					args[1] = env.getEnv(CommandHelperEnvironment.class).GetVarList().get(cur2.getName(), cur2.getTarget());
				}
				value = Static.getInt(args[1]);
			}
			if (args[0] instanceof IVariable) {
				IVariable cur = (IVariable) args[0];
				IVariable v = env.getEnv(CommandHelperEnvironment.class).GetVarList().get(cur.getName(), cur.getTarget());
				Construct newVal;
				if (Static.anyDoubles(v.ival())) {
					newVal = new CDouble(Static.getDouble(v.ival()) - value, t);
				} else {
					newVal = new CInt(Static.getInt(v.ival()) - value, t);
				}
				Construct oldVal = null;
				try {
					oldVal = v.ival().clone();
				} catch (CloneNotSupportedException ex) {
					Logger.getLogger(Math.class.getName()).log(Level.SEVERE, null, ex);
				}
				v = new IVariable(v.getName(), newVal, t);
				env.getEnv(CommandHelperEnvironment.class).GetVarList().set(v);
				return oldVal;
			} else {
				if (Static.anyDoubles(args[0])) {
					return new CDouble(Static.getNumber(args[0]) + value, t);
				} else {
					return new CInt(Static.getInt(args[0]) + value, t);
				}
			}
		}

		public String docs() {
			return "ivar {var, [x]} Subtracts x from var, and stores the new value. Equivalent to var-- in other languages. Expects ivar to be a variable, then"
					+ " returns a copy of the old ivar, , or, if var is a constant number, simply adds x to it, and returns the new number.";
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
		public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
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
			return "mixed {[] | min/max, [max]} Returns a random number from 0 to max, or min to max, depending on usage. Max is exclusive. Min must"
					+ " be less than max, and both numbers must be >= 0. This will return an integer. Alternatively, you can pass no arguments, and a random"
					+ " double, from 0 to 1 will be returned.";
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

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			if(args.length == 0){
				return new CDouble(java.lang.Math.random(), t);
			} else {
				long min = 0;
				long max = 0;
				if (args.length == 1) {
					max = Static.getInt(args[0]);
				} else {
					min = Static.getInt(args[0]);
					max = Static.getInt(args[1]);
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
			return "double {arg} Returns the absolute value of the argument.";
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

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			double d = Static.getDouble(args[0]);
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
			return "int {number} Returns the floor of any given number. For example, floor(3.8) returns 3, and floor(-1.1) returns 2";
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

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return new CInt((long) java.lang.Math.floor(Static.getNumber(args[0])), t);
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
			return "int {number} Returns the ceiling of any given number. For example, ceil(3.2) returns 4, and ceil(-1.1) returns -1";
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

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return new CInt((long) java.lang.Math.ceil(Static.getNumber(args[0])), t);
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
			return "number {number} Returns the square root of a number. Note that this is mathematically equivalent to pow(number, .5)."
					+ " Imaginary numbers are not supported, so number must be positive.";
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

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			double d = Static.getNumber(args[0]);
			if (d < 0) {
				throw new ConfigRuntimeException("sqrt expects a number >= 0", ExceptionType.RangeException, t);
			}
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
			return "number {num1, [num2...]} Returns the lowest number in a given list of numbers. If any of the arguments"
					+ " are arrays, they are expanded into individual numbers, and also compared.";
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

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			if (args.length == 0) {
				throw new ConfigRuntimeException("You must send at least one parameter to min",
						ExceptionType.InsufficientArgumentsException, t);
			}
			double lowest = Double.POSITIVE_INFINITY;
			List<Construct> list = new ArrayList<Construct>();
			recList(list, args);
			for (Construct c : list) {
				double d = Static.getNumber(c);
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

		public List<Construct> recList(List<Construct> list, Construct... args) {
			for (Construct c : args) {
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
			return "number {num1, [num2...]} Returns the highest number in a given list of numbers. If any of the arguments"
					+ " are arrays, they are expanded into individual numbers, and also compared.";
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

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			if (args.length == 0) {
				throw new ConfigRuntimeException("You must send at least one parameter to max",
						ExceptionType.InsufficientArgumentsException, t);
			}
			double highest = Double.NEGATIVE_INFINITY;
			List<Construct> list = new ArrayList<Construct>();
			recList(list, args);
			for (Construct c : list) {
				double d = Static.getNumber(c);
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

		public List<Construct> recList(List<Construct> list, Construct... args) {
			for (Construct c : args) {
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
			return "double {number} Returns the sin of the number";
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
			return new CDouble(java.lang.Math.sin(Static.getNumber(args[0])), t);
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
			return "double {number} Returns the cos of the number";
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
			return new CDouble(java.lang.Math.cos(Static.getNumber(args[0])), t);
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
			return "double {number} Returns the tan of the number";
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
			return new CDouble(java.lang.Math.tan(Static.getNumber(args[0])), t);
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
			return "double {number} Returns the arc sin of the number";
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
			return new CDouble(java.lang.Math.asin(Static.getNumber(args[0])), t);
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
			return "double {number} Returns the arc cos of the number";
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
			return new CDouble(java.lang.Math.acos(Static.getNumber(args[0])), t);
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
			return "double {number} Returns the arc tan of the number";
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
			return new CDouble(java.lang.Math.atan(Static.getNumber(args[0])), t);
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
			return "double {number} Converts the number to radians (which is assumed to have been in degrees)";
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
			return new CDouble(java.lang.Math.toRadians(Static.getNumber(args[0])), t);
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
			return new CDouble(java.lang.Math.toDegrees(Static.getNumber(args[0])), t);
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
			return "double {number} Returns the angle theta from the conversion"
					+ " of rectangular coordinates (x, y) to polar coordinates"
					+ " (r, theta). This method computes the phase theta by"
					+ " computing an arc tangent of y/x in the range of -pi to pi.";
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
			return new CDouble(java.lang.Math.atan2(Static.getNumber(args[0]), Static.getNumber(args[1])), t);
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
			return "mixed {number, [precision]} Unlike floor and ceil, rounds the number to the nearest integer. Precision defaults to 0, but if set to 1 or more, rounds decimal places."
					+ " For instance, round(2.29, 1) would return 2.3. If precision is < 0, a RangeException is thrown. If precision is set to 0, an integer is always"
					+ " returned, and if precision is > 0, a double is always returned.";
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

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			double number = Static.getNumber(args[0]);
			int precision = 0;
			if(args.length > 1){
				precision = (int)Static.getInt(args[1]);
			}
			if(precision < 0){
				throw new Exceptions.RangeException("precision cannot be less than 0, was " + precision, t);
			}
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

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
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
				da = new double[vars.size()];
				varNames = new String[vars.size()];
				for (String key : vars.keySet()) {
					varNames[i] = key;
					da[i] = Static.getDouble(vars.get(key, t));
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
			return "number {number} Negates a number, essentially multiplying the number by -1";
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
			if (args[0] instanceof CInt) {
				return new CInt(-(Static.getInt(args[0])), t);
			} else {
				return new CDouble(-(Static.getDouble(args[0])), t);
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
}
