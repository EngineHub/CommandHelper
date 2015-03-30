package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.OptimizationUtilities;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CMutablePrimitive;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.interfaces.ArrayAccess;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
@core
public class Math {

	public static String docs() {
		return "Provides mathematical functions to scripts";
	}

	@api
	@seealso({subtract.class, multiply.class, divide.class})
	public static class add extends AbstractFunction implements Optimizable{

		@Override
		public String getName() {
			return "add";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			if (Static.anyDoubles(args)) {
				double tally = Static.getNumber(args[0], t);
				for (int i = 1; i < args.length; i++) {
					tally += Static.getNumber(args[i], t);
				}
				return new CDouble(tally, t);
			} else {
				long tally = Static.getInt(args[0], t);
				for (int i = 1; i < args.length; i++) {
					tally += Static.getInt(args[i], t);
				}
				return new CInt(tally, t);
			}
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		@Override
		public String docs() {
			return "mixed {var1, [var2...]} Adds all the arguments together, and returns either a double or an integer."
					+ " Operator syntax is also supported: @a + @b";
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
				new ExampleScript("Demonstrates adding two numbers together", "msg(add(2, 2))"),
				new ExampleScript("Demonstrates adding two numbers together, using the operator syntax", "2 + 2"),
				new ExampleScript("Demonstrates grouping with parenthesis", "(2 + 5) * 2"),
				new ExampleScript("Demonstrates order of operations", "2 + 5 * 2"),
			};
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children, FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			OptimizationUtilities.pullUpLikeFunctions(children, this.getName());
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.OPTIMIZE_DYNAMIC,
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
	}

	@api
	@seealso({add.class, multiply.class, divide.class})
	public static class subtract extends AbstractFunction implements Optimizable{

		@Override
		public String getName() {
			return "subtract";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			if (Static.anyDoubles(args)) {
				double tally = Static.getNumber(args[0], t);
				for (int i = 1; i < args.length; i++) {
					tally -= Static.getNumber(args[i], t);
				}
				return new CDouble(tally, t);
			} else {
				long tally = Static.getInt(args[0], t);
				for (int i = 1; i < args.length; i++) {
					tally -= Static.getInt(args[i], t);
				}
				return new CInt(tally, t);
			}
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		@Override
		public String docs() {
			return "mixed {var1, [var2...]} Subtracts the arguments from left to right, and returns either a double or an integer."
					+ " Operator syntax is also supported: @a - @b";
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
						new ExampleScript("Demonstrates basic usage", "subtract(4 - 3)"),
						new ExampleScript("Demonstrates operator syntax", "12 - 5"),};
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
	@seealso({divide.class, add.class, subtract.class})
	public static class multiply extends AbstractFunction implements Optimizable{

		@Override
		public String getName() {
			return "multiply";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			if (Static.anyDoubles(args)) {
				double tally = Static.getNumber(args[0], t);
				for (int i = 1; i < args.length; i++) {
					tally *= Static.getNumber(args[i], t);
				}
				return new CDouble(tally, t);
			} else {
				long tally = Static.getInt(args[0], t);
				for (int i = 1; i < args.length; i++) {
					tally *= Static.getInt(args[i], t);
				}
				return new CInt(tally, t);
			}
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		@Override
		public String docs() {
			return "mixed {var1, [var2...]} Multiplies the arguments together, and returns either a double or an integer. Operator syntax"
					+ " is also supported: 2 * 2";
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
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children, FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			OptimizationUtilities.pullUpLikeFunctions(children, this.getName());
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Functional usage", "multiply(8, 8)"),
						new ExampleScript("Operator syntax", "8 * 8"),};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.OPTIMIZE_DYNAMIC,
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
	}

	@api
	@seealso({multiply.class, add.class, subtract.class})
	public static class divide extends AbstractFunction implements Optimizable{

		@Override
		public String getName() {
			return "divide";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			double tally = Static.getNumber(args[0], t);
			for (int i = 1; i < args.length; i++) {
				double next = Static.getNumber(args[i], t);
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

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.RangeException};
		}

		@Override
		public String docs() {
			return "mixed {var1, [var2...]} Divides the arguments from left to right, and returns either a double or an integer."
					+ " If you divide by zero, a RangeException is thrown. Operator syntax is also supported:"
					+ " 2 / 2";
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
						new ExampleScript("Functional usage", "divide(4, 2)"),
						new ExampleScript("Demonstrates double return", "divide(2, 4)"),
						new ExampleScript("Operator syntax", "2 / 4"),
						new ExampleScript("Demonstrates divide by zero error", "@zero = 0;\nmsg(1 / @zero);"),};
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
	public static class mod extends AbstractFunction implements Optimizable{

		@Override
		public String getName() {
			return "mod";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			long arg1 = Static.getInt(args[0], t);
			long arg2 = Static.getInt(args[1], t);
			return new CInt(arg1 % arg2, t);
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		@Override
		public String docs() {
			return "int {x, n} Returns x modulo n. Operator syntax is also supported: @x % @n";
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
						new ExampleScript("Functional usage", "mod(2, 2)"),
						new ExampleScript("Operator syntax", "2 % 2"),};
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

		@Override
		public String getName() {
			return "pow";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			double arg1 = Static.getNumber(args[0], t);
			double arg2 = Static.getNumber(args[1], t);
			return new CDouble(java.lang.Math.pow(arg1, arg2), t);
		}

		@Override
		public String docs() {
			return "double {x, n} Returns x to the power of n. Operator syntax is also supported: @x ** @n";
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
			return CHVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Functional usage", "pow(2, 4)"),
						new ExampleScript("Operator syntax", "2 ** 4"),};
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
	protected static Construct doIncrementDecrement(ParseTree[] nodes,
			Script parent, Environment env, Target t,
			Function func, boolean pre, boolean inc){
		if(nodes[0].getData() instanceof CFunction){
				Function f;
				try {
					f = ((CFunction)nodes[0].getData()).getFunction();
				} catch (ConfigCompileException ex) {
					// This can't really happen, as the compiler would have already caught this
					throw new Error(ex);
				}
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
					long delta = Static.getInt(cdelta, t);
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
						Construct value = myArray.get(index, t);
						if(value instanceof CInt || value instanceof CDouble){
							temp = Static.getInt(value, t);
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

	@api
	@seealso({dec.class, postdec.class, postinc.class})
	public static class inc extends AbstractFunction implements Optimizable{

		@Override
		public String getName() {
			return "inc";
		}

		@Override
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

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			long value = 1;
			if (args.length == 2) {
				if (args[1] instanceof IVariable) {
					IVariable cur2 = (IVariable) args[1];
					args[1] = env.getEnv(GlobalEnv.class).GetVarList().get(cur2.getName(), cur2.getTarget());
				}
				value = Static.getInt(args[1], t);
			}
			if (args[0] instanceof IVariable) {
				IVariable cur = (IVariable) args[0];
				IVariable v = env.getEnv(GlobalEnv.class).GetVarList().get(cur.getName(), cur.getTarget());
				Construct newVal;
				if (Static.anyDoubles(v.ival())) {
					newVal = new CDouble(Static.getDouble(v.ival(), t) + value, t);
				} else {
					newVal = new CInt(Static.getInt(v.ival(), t) + value, t);
				}
				if(v.ival() instanceof CMutablePrimitive){
					newVal = ((CMutablePrimitive)v.ival()).setAndReturn(newVal, t);
				}
				v = new IVariable(v.getDefinedType(), v.getName(), newVal, t);
				env.getEnv(GlobalEnv.class).GetVarList().set(v);
				return v;
			} else {
				if (Static.anyDoubles(args[0])) {
					return new CDouble(Static.getNumber(args[0], t) + value, t);
				} else {
					return new CInt(Static.getInt(args[0], t) + value, t);
				}
			}

		}

		@Override
		public String docs() {
			return "ivar {var, [x]} Adds x to var, and stores the new value. Equivalent to ++var in other languages. Expects ivar to be a variable, then"
					+ " returns the ivar, or, if var is a constant number, simply adds x to it, and returns the new number. Operator syntax"
					+ " is also supported: ++@var";
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
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Demonstrates basic usage", "@x = 0;\nmsg(@x);\ninc(@x);\nmsg(@x);"),
						new ExampleScript("Demonstrates symbolic usage", "@x = 0;\n"
								+ "msg(@x);\n"
								+ "(++@x); // Note the use of parenthesis, which is required in this case, otherwise it applies to the previous operation\n"
								+ "msg(@x);"),};
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
	@seealso({postdec.class, inc.class, dec.class})
	public static class postinc extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "postinc";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}
		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			return Math.doIncrementDecrement(nodes, parent, env, t, this, false, true);
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			long value = 1;
			if (args.length == 2) {
				if (args[1] instanceof IVariable) {
					IVariable cur2 = (IVariable) args[1];
					args[1] = env.getEnv(GlobalEnv.class).GetVarList().get(cur2.getName(), cur2.getTarget());
				}
				value = Static.getInt(args[1], t);
			}
			if (args[0] instanceof IVariable) {
				IVariable cur = (IVariable) args[0];
				IVariable v = env.getEnv(GlobalEnv.class).GetVarList().get(cur.getName(), cur.getTarget());
				Construct newVal;
				if (Static.anyDoubles(v.ival())) {
					newVal = new CDouble(Static.getDouble(v.ival(), t) + value, t);
				} else {
					newVal = new CInt(Static.getInt(v.ival(), t) + value, t);
				}
				if(v.ival() instanceof CMutablePrimitive){
					newVal = ((CMutablePrimitive)v.ival()).setAndReturn(newVal, t);
				}
				Construct oldVal = null;
				try {
					oldVal = v.ival().clone();
				} catch (CloneNotSupportedException ex) {
					Logger.getLogger(Math.class.getName()).log(Level.SEVERE, null, ex);
				}
				v = new IVariable(v.getDefinedType(), v.getName(), newVal, t);
				env.getEnv(GlobalEnv.class).GetVarList().set(v);
				return oldVal;
			} else {
				if (Static.anyDoubles(args[0])) {
					return new CDouble(Static.getNumber(args[0], t) + value, t);
				} else {
					return new CInt(Static.getInt(args[0], t) + value, t);
				}
			}
		}


		@Override
		public String docs() {
			return "ivar {var, [x]} Adds x to var, and stores the new value. Equivalent to var++ in other languages. Expects ivar to be a variable, then"
					+ " returns a copy of the old ivar, or, if var is a constant number, simply adds x to it, and returns the new number. Operator"
					+ " notation is also supported: @var++";
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
		public boolean preResolveVariables() {
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

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic functional usage", "@a = 5;\n"
						+ "msg(postinc(@a));\n"
						+ "msg(@a);"),
				new ExampleScript("Basic functional usage, with optional value set", "@a = 5;\n"
						+ "msg(postinc(@a, 6));\n"
						+ "msg(@a);"),
				new ExampleScript("Operator syntax", "@a = 5;\n"
						+ "msg(@a++);\n"
						+ "msg(@a);"),
			};
		}

	}

	@api
	@seealso({inc.class, postdec.class, postinc.class})
	public static class dec extends AbstractFunction implements Optimizable{

		@Override
		public String getName() {
			return "dec";
		}

		@Override
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

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			long value = 1;
			if (args.length == 2) {
				if (args[1] instanceof IVariable) {
					IVariable cur2 = (IVariable) args[1];
					args[1] = env.getEnv(GlobalEnv.class).GetVarList().get(cur2.getName(), cur2.getTarget());
				}
				value = Static.getInt(args[1], t);
			}
			if (args[0] instanceof IVariable) {
				IVariable cur = (IVariable) args[0];
				IVariable v = env.getEnv(GlobalEnv.class).GetVarList().get(cur.getName(), cur.getTarget());
				Construct newVal;
				if (Static.anyDoubles(v.ival())) {
					newVal = new CDouble(Static.getDouble(v.ival(), t) - value, t);
				} else {
					newVal = new CInt(Static.getInt(v.ival(), t) - value, t);
				}
				if(v.ival() instanceof CMutablePrimitive){
					newVal = ((CMutablePrimitive)v.ival()).setAndReturn(newVal, t);
				}
				v = new IVariable(v.getDefinedType(), v.getName(), newVal, t);
				env.getEnv(GlobalEnv.class).GetVarList().set(v);
				return v;
			} else {
				if (Static.anyDoubles(args[0])) {
					return new CDouble(Static.getNumber(args[0], t) + value, t);
				} else {
					return new CInt(Static.getInt(args[0], t) + value, t);
				}
			}
		}

		@Override
		public String docs() {
			return "ivar {var, [value]} Subtracts value from var, and stores the new value. Value defaults to 1. Equivalent to --var (or var -= value) in other languages. Expects ivar to be a variable, then"
					+ " returns the ivar, or if var is a constant number, simply adds x to it, and returns the new number. Operator"
					+ " syntax is also supported: --@var";
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

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Demonstrates basic usage", "@x = 1;\nmsg(@x);\ndec(@x);\nmsg(@x);"),
						new ExampleScript("Demonstrates symbolic usage", "@x = 1;\n"
								+ "msg(@x);\n"
								+ "(--@x); // Note the use of parenthesis, which is required in this case, otherwise it applies to the previous operation\n"
								+ "msg(@x);"),};
		}

	}

	@api
	@seealso({postinc.class, inc.class, dec.class})
	public static class postdec extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "postdec";
		}

		@Override
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

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			long value = 1;
			if (args.length == 2) {
				if (args[1] instanceof IVariable) {
					IVariable cur2 = (IVariable) args[1];
					args[1] = env.getEnv(GlobalEnv.class).GetVarList().get(cur2.getName(), cur2.getTarget());
				}
				value = Static.getInt(args[1], t);
			}
			if (args[0] instanceof IVariable) {
				IVariable cur = (IVariable) args[0];
				IVariable v = env.getEnv(GlobalEnv.class).GetVarList().get(cur.getName(), cur.getTarget());
				Construct newVal;
				if (Static.anyDoubles(v.ival())) {
					newVal = new CDouble(Static.getDouble(v.ival(), t) - value, t);
				} else {
					newVal = new CInt(Static.getInt(v.ival(), t) - value, t);
				}
				if(v.ival() instanceof CMutablePrimitive){
					newVal = ((CMutablePrimitive)v.ival()).setAndReturn(newVal, t);
				}
				Construct oldVal = null;
				try {
					oldVal = v.ival().clone();
				} catch (CloneNotSupportedException ex) {
					Logger.getLogger(Math.class.getName()).log(Level.SEVERE, null, ex);
				}
				v = new IVariable(v.getDefinedType(), v.getName(), newVal, t);
				env.getEnv(GlobalEnv.class).GetVarList().set(v);
				return oldVal;
			} else {
				if (Static.anyDoubles(args[0])) {
					return new CDouble(Static.getNumber(args[0], t) + value, t);
				} else {
					return new CInt(Static.getInt(args[0], t) + value, t);
				}
			}
		}

		@Override
		public String docs() {
			return "ivar {var, [x]} Subtracts x from var, and stores the new value. Equivalent to var-- in other languages. Expects ivar to be a variable, then"
					+ " returns a copy of the old ivar, , or, if var is a constant number, simply adds x to it, and returns the new number."
					+ " Operator syntax is also supported: @var--";
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
		public boolean preResolveVariables() {
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
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic functional usage", "@a = 5;\n"
						+ "msg(postdec(@a));\n"
						+ "msg(@a);"),
				new ExampleScript("Basic functional usage, with optional value set", "@a = 5;\n"
						+ "msg(postdec(@a, 6));\n"
						+ "msg(@a);"),
				new ExampleScript("Operator syntax", "@a = 5;\n"
						+ "msg(@a--);\n"
						+ "msg(@a);"),
			};
		}
	}

	@api
	public static class rand extends AbstractFunction {

		Random r = new Random();

		@Override
		public String getName() {
			return "rand";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1, 2};
		}

		@Override
		public String docs() {
			return "mixed {[] | min/max, [max]} Returns a random number from 0 to max, or min to max, depending on usage. Max is exclusive. Min must"
					+ " be less than max, and both numbers must be >= 0. This will return an integer. Alternatively, you can pass no arguments, and a random"
					+ " double, from 0 to 1 will be returned.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.RangeException, ExceptionType.CastException};
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
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			if(args.length == 0){
				return new CDouble(java.lang.Math.random(), t);
			} else {
				long min = 0;
				long max = 0;
				if (args.length == 1) {
					max = Static.getInt(args[0], t);
				} else {
					min = Static.getInt(args[0], t);
					max = Static.getInt(args[1], t);
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

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage, with one paramter", "rand(10)", ":5"),
				new ExampleScript("Basic usage, with a range", "rand(50, 100)", ":95"),
				new ExampleScript("Usage with no parameters", "rand()", ":0.720543709668052")
			};
		}

	}

	@api
	public static class abs extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "abs";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "mixed {arg} Returns the absolute value of the argument.";
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
			return CHVersion.V3_1_2;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			if (args[0] instanceof CInt){
				return new CInt(java.lang.Math.abs(Static.getInt(args[0], t)), t);
			}else{
				return new CDouble(java.lang.Math.abs(Static.getDouble(args[0], t)), t);
			}
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

		@Override
		public String getName() {
			return "floor";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "int {number} Returns the floor of any given number. For example, floor(3.8) returns 3, and floor(-1.1) returns 2";
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
			return CHVersion.V3_1_3;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return new CInt((long) java.lang.Math.floor(Static.getNumber(args[0], t)), t);
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

		@Override
		public String getName() {
			return "ceil";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "int {number} Returns the ceiling of any given number. For example, ceil(3.2) returns 4, and ceil(-1.1) returns -1";
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
			return CHVersion.V3_1_3;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return new CInt((long) java.lang.Math.ceil(Static.getNumber(args[0], t)), t);
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

		@Override
		public String getName() {
			return "sqrt";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "number {number} Returns the square root of a number. Note that this is mathematically equivalent to pow(number, .5)."
					+ " Imaginary numbers are not supported, so number must be positive.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.RangeException, ExceptionType.CastException};
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
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			double d = Static.getNumber(args[0], t);
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

		@Override
		public String getName() {
			return "min";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "number {num1, [num2...]} Returns the lowest number in a given list of numbers. If any of the arguments"
					+ " are arrays, they are expanded into individual numbers, and also compared.";
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
			return CHVersion.V3_2_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			if (args.length == 0) {
				throw new ConfigRuntimeException("You must send at least one parameter to min",
						ExceptionType.InsufficientArgumentsException, t);
			}
			double lowest = Double.POSITIVE_INFINITY;
			List<Construct> list = new ArrayList<Construct>();
			recList(list, args);
			for (Construct c : list) {
				double d = Static.getNumber(c, t);
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

		@Override
		public String getName() {
			return "max";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "number {num1, [num2...]} Returns the highest number in a given list of numbers. If any of the arguments"
					+ " are arrays, they are expanded into individual numbers, and also compared.";
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
			return CHVersion.V3_2_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			if (args.length == 0) {
				throw new ConfigRuntimeException("You must send at least one parameter to max",
						ExceptionType.InsufficientArgumentsException, t);
			}
			double highest = Double.NEGATIVE_INFINITY;
			List<Construct> list = new ArrayList<Construct>();
			recList(list, args);
			for (Construct c : list) {
				double d = Static.getNumber(c, t);
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

		@Override
		public String getName() {
			return "sin";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "double {number} Returns the sin of the number";
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
			return new CDouble(java.lang.Math.sin(Static.getNumber(args[0], t)), t);
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

		@Override
		public String getName() {
			return "cos";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "double {number} Returns the cos of the number";
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
			return new CDouble(java.lang.Math.cos(Static.getNumber(args[0], t)), t);
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

		@Override
		public String getName() {
			return "tan";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "double {number} Returns the tan of the number";
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
			return new CDouble(java.lang.Math.tan(Static.getNumber(args[0], t)), t);
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

		@Override
		public String getName() {
			return "asin";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "double {number} Returns the arc sin of the number";
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
			return new CDouble(java.lang.Math.asin(Static.getNumber(args[0], t)), t);
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

		@Override
		public String getName() {
			return "acos";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "double {number} Returns the arc cos of the number";
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
			return new CDouble(java.lang.Math.acos(Static.getNumber(args[0], t)), t);
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

		@Override
		public String getName() {
			return "atan";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "double {number} Returns the arc tan of the number";
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
			return new CDouble(java.lang.Math.atan(Static.getNumber(args[0], t)), t);
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

		@Override
		public String getName() {
			return "to_radians";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "double {number} Converts the number to radians (which is assumed to have been in degrees)";
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
			return new CDouble(java.lang.Math.toRadians(Static.getNumber(args[0], t)), t);
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

		@Override
		public String getName() {
			return "to_degrees";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "double {number} Converts the number to degrees (which is assumed to have been in radians)";
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
			return new CDouble(java.lang.Math.toDegrees(Static.getNumber(args[0], t)), t);
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

		@Override
		public String getName() {
			return "atan2";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			//lolcopypaste
			return "double {y, x} Returns the angle theta from the conversion"
					+ " of rectangular coordinates (x, y) to polar coordinates"
					+ " (r, theta). This method computes the phase theta by"
					+ " computing an arc tangent of y/x in the range of -pi to pi.";
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
			return new CDouble(java.lang.Math.atan2(Static.getNumber(args[0], t), Static.getNumber(args[1], t)), t);
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

		@Override
		public String getName() {
			return "round";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "mixed {number, [precision]} Unlike floor and ceil, rounds the number to the nearest integer. Precision defaults to 0, but if set to 1 or more, rounds decimal places."
					+ " For instance, round(2.29, 1) would return 2.3. If precision is < 0, a RangeException is thrown. If precision is set to 0, an integer is always"
					+ " returned, and if precision is > 0, a double is always returned.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.RangeException};
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
			double number = Static.getNumber(args[0], t);
			int precision = 0;
			if(args.length > 1){
				precision = Static.getInt32(args[1], t);
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

		@Override
		public String getName() {
			return "expr";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "double {expression, [valueArray]} Sometimes, when you need to calculate an advanced"
					+ " mathematical expression, it is messy to write out everything in terms of functions."
					+ " This function will allow you to evaluate a mathematical expression as a string, using"
					+ " common mathematical notation. For example, (2 + 3) * 4 would return 20. Variables can"
					+ " also be included, and their values given as an associative array. expr('(x + y) * z',"
					+ " array(x: 2, y: 3, z: 4)) would be the same thing as the above example."
					+ " This function requires WorldEdit in plugins, lib, or the server root in order to run.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException, ExceptionType.CastException, ExceptionType.PluginInternalException};
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
			String expr = args[0].val().trim();
			if("".equals(expr)){
				throw new Exceptions.FormatException("Expression may not be empty", t);
			}
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
				for (String key : vars.stringKeySet()) {
					varNames[i] = key;
					da[i] = Static.getDouble(vars.get(key, t), t);
					i++;
				}
			} else {
				da = new double[0];
				varNames = new String[0];
			}
			/*try {
				Expression e = Expression.compile(expr, varNames);
				return new CDouble(e.evaluate(da), t);
			} catch (ExpressionException ex) {
				throw new ConfigRuntimeException("Your expression was invalidly formatted", ExceptionType.PluginInternalException, t, ex);
			}*/
			String eClass = "com.sk89q.worldedit.internal.expression.Expression";
			String errClass = "com.sk89q.worldedit.internal.expression.ExpressionException";
			Class eClazz, errClazz;
			try {
				eClazz = Class.forName(eClass);
				errClazz = Class.forName(errClass);
			} catch (ClassNotFoundException cnf) {
				throw new ConfigRuntimeException("You are missing a required dependency: " + eClass,
						ExceptionType.PluginInternalException, t);
			}
			try {
				Object e = ReflectionUtils.invokeMethod(eClazz, null, "compile",
						new Class[]{String.class, String[].class}, new Object[]{expr, varNames});
				Object d = ReflectionUtils.invokeMethod(eClazz, e, "evaluate",
						new Class[]{double[].class}, new Object[]{da});
				return new CDouble((double) d, t);
			} catch (ReflectionUtils.ReflectionException rex) {
				if (rex.getCause().getClass().isAssignableFrom(errClazz)) {
					throw new ConfigRuntimeException("Your expression was invalidly formatted",
							ExceptionType.PluginInternalException, args[0].getTarget(), rex.getCause());
				} else {
					throw new ConfigRuntimeException(rex.getMessage(), ExceptionType.PluginInternalException,
							args[0].getTarget(), rex.getCause());
				}
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

		@Override
		public String getName() {
			return "neg";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "number {number} Negates a number, essentially multiplying the number by -1";
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
			if (args[0] instanceof CInt) {
				return new CInt(-(Static.getInt(args[0], t)), t);
			} else {
				return new CDouble(-(Static.getDouble(args[0], t)), t);
			}
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
	}

	@api
	public static class logarithm extends AbstractFunction implements Optimizable {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.RangeException};
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
			double val = Static.getDouble(args[0], t);
			if(val <= 0){
				throw new Exceptions.RangeException("val was <= 0", t);
			}
			double r;
			if(args.length == 1){
				r = java.lang.Math.log(val);
			} else {// if(args.length == 2){
				r = java.lang.Math.log(val) / java.lang.Math.log(Static.getDouble(args[1], t));
			}
			return new CDouble(r, t);
		}

		@Override
		public String getName() {
			return "logarithm";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "double {val, [base]} Return the log of a number to the specified base, or the mathematical"
					+ " constant e if no base is provided (or ln). If val is less than or equal to zero, a RangeException is thrown."
					+ " Mathematically speaking, if val is 0, then the result would be negative infinity, and if it"
					+ " is less than 0 it is undefined (NaN), but since MethodScript has no way of representing either"
					+ " of these, a RangeException is thrown instead.";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
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
