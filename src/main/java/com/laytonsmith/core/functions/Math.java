package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.ArrayUtils;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.MEnum;
import com.laytonsmith.annotations.OperatorPreferred;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.SimpleDocumentation;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.OptimizationUtilities;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CMutablePrimitive;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREInsufficientArgumentsException;
import com.laytonsmith.core.exceptions.CRE.CREPluginInternalException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.ArrayAccess;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.text.DecimalFormat;

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
	@OperatorPreferred("+")
	public static class add extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "add";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			if(Static.anyDoubles(args)) {
				double tally = Static.getNumber(args[0], t);
				for(int i = 1; i < args.length; i++) {
					tally += Static.getNumber(args[i], t);
				}
				return new CDouble(tally, t);
			} else {
				long tally = Static.getInt(args[0], t);
				for(int i = 1; i < args.length; i++) {
					tally += Static.getInt(args[i], t);
				}
				return new CInt(tally, t);
			}
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
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
		public MSVersion since() {
			return MSVersion.V3_0_1;
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
				new ExampleScript("Demonstrates order of operations", "2 + 5 * 2")};
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
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
	@OperatorPreferred("-")
	public static class subtract extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "subtract";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			if(Static.anyDoubles(args)) {
				double tally = Static.getNumber(args[0], t);
				for(int i = 1; i < args.length; i++) {
					tally -= Static.getNumber(args[i], t);
				}
				return new CDouble(tally, t);
			} else {
				long tally = Static.getInt(args[0], t);
				for(int i = 1; i < args.length; i++) {
					tally -= Static.getInt(args[i], t);
				}
				return new CInt(tally, t);
			}
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
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
		public MSVersion since() {
			return MSVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Demonstrates basic usage", "subtract(4 - 3)"),
				new ExampleScript("Demonstrates operator syntax", "12 - 5")};
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
	@OperatorPreferred("*")
	public static class multiply extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "multiply";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			if(Static.anyDoubles(args)) {
				double tally = Static.getNumber(args[0], t);
				for(int i = 1; i < args.length; i++) {
					tally *= Static.getNumber(args[i], t);
				}
				return new CDouble(tally, t);
			} else {
				long tally = Static.getInt(args[0], t);
				for(int i = 1; i < args.length; i++) {
					tally *= Static.getInt(args[i], t);
				}
				return new CInt(tally, t);
			}
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
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
		public MSVersion since() {
			return MSVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			OptimizationUtilities.pullUpLikeFunctions(children, this.getName());
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Functional usage", "multiply(8, 8)"),
				new ExampleScript("Operator syntax", "8 * 8")};
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
	@OperatorPreferred("/")
	public static class divide extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "divide";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			double tally = Static.getNumber(args[0], t);
			for(int i = 1; i < args.length; i++) {
				double next = Static.getNumber(args[i], t);
				if(next == 0) {
					throw new CRERangeException("Division by 0!", t);
				}
				tally /= next;
			}
			if(tally == (int) tally) {
				return new CInt((long) tally, t);
			} else {
				return new CDouble(tally, t);
			}
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRERangeException.class};
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
		public MSVersion since() {
			return MSVersion.V3_0_1;
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
				new ExampleScript("Demonstrates divide by zero error", "@zero = 0;\nmsg(1 / @zero);")};
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
	@OperatorPreferred("%")
	public static class mod extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "mod";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			long arg1 = Static.getInt(args[0], t);
			long arg2 = Static.getInt(args[1], t);
			if(arg2 == 0) {
				throw new CRERangeException("Modulo by 0!", t);
			}
			return new CInt(arg1 % arg2, t);
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRERangeException.class};
		}

		@Override
		public String docs() {
			return "int {x, n} Returns x modulo n. Throws a RangeException when n is 0. Operator syntax is also supported: @x % @n";
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
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Functional usage", "mod(2, 2)"),
				new ExampleScript("Operator syntax", "2 % 2")};
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
	@OperatorPreferred("**")
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
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			double arg1 = Static.getNumber(args[0], t);
			double arg2 = Static.getNumber(args[1], t);
			return new CDouble(java.lang.Math.pow(arg1, arg2), t);
		}

		@Override
		public String docs() {
			return "double {x, n} Returns x to the power of n. Operator syntax is also supported: @x ** @n";
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
			return MSVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Functional usage", "pow(2, 4)"),
				new ExampleScript("Operator syntax", "2 ** 4")};
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
	 * If we have the case {@code @array[0]++}, we have to increment it as though it were a variable, so we have to do
	 * that with execs. This method consolidates the code to do so.
	 *
	 * @return
	 */
	protected static Mixed doIncrementDecrement(ParseTree[] nodes,
			Script parent, Environment env, Target t,
			Function func, boolean pre, boolean inc) {
		if(nodes[0].getData() instanceof CFunction) {
			Function f;
			try {
				f = ((CFunction) nodes[0].getData()).getFunction();
			} catch (ConfigCompileException ex) {
				// This can't really happen, as the compiler would have already caught this
				throw new Error(ex);
			}
			if(f.getName().equals(new ArrayHandling.array_get().getName())) {
				//Ok, so, this is it, we're in charge here.
				//First, pull out the current value. We're gonna do this manually though, and we will actually
				//skip the whole array_get execution.
				ParseTree eval = nodes[0];
				Mixed array = parent.seval(eval.getChildAt(0), env);
				Mixed index = parent.seval(eval.getChildAt(1), env);
				Mixed cdelta = new CInt(1, t);
				if(nodes.length == 2) {
					cdelta = parent.seval(nodes[1], env);
				}
				long delta = Static.getInt(cdelta, t);
				//First, error check, then get the old value, and store it in temp.
				if(!(array.isInstanceOf(CArray.TYPE)) && !(array.isInstanceOf(ArrayAccess.TYPE))) {
					//Let's just evaluate this like normal with array_get, so it will
					//throw the appropriate exception.
					new ArrayHandling.array_get().exec(t, env, array, index);
					throw ConfigRuntimeException.CreateUncatchableException("Shouldn't have gotten here. Please report this error, and how you got here.", t);
				} else if(!(array.isInstanceOf(CArray.TYPE))) {
					//It's an ArrayAccess type, but we can't use that here, so, throw our
					//own exception.
					throw new CRECastException("Cannot increment/decrement a non-array array"
							+ " accessed value. (The value passed in was \"" + array.val() + "\")", t);
				}
				//Ok, we're good. Data types should all be correct.
				CArray myArray = ((CArray) array);
				Mixed value = myArray.get(index, t);

				//Alright, now let's actually perform the increment, and store that in the array.
				if(value.isInstanceOf(CInt.TYPE)) {
					CInt newVal;
					if(inc) {
						newVal = new CInt(Static.getInt(value, t) + delta, t);
					} else {
						newVal = new CInt(Static.getInt(value, t) - delta, t);
					}
					new ArrayHandling.array_set().exec(t, env, array, index, newVal);
					if(pre) {
						return newVal;
					} else {
						return value;
					}
				} else if(value.isInstanceOf(CDouble.TYPE)) {
					CDouble newVal;
					if(inc) {
						newVal = new CDouble(Static.getDouble(value, t) + delta, t);
					} else {
						newVal = new CDouble(Static.getDouble(value, t) - delta, t);
					}
					new ArrayHandling.array_set().exec(t, env, array, index, newVal);
					if(pre) {
						return newVal;
					} else {
						return value;
					}
				} else {
					throw new CRECastException("Cannot increment/decrement a non numeric value.", t);
				}
			}
		}
		Mixed[] args = new Mixed[nodes.length];
		for(int i = 0; i < args.length; i++) {
			args[i] = parent.eval(nodes[i], env);
		}
		return func.exec(t, env, args);
	}

	@api
	@seealso({dec.class, postdec.class, postinc.class})
	@OperatorPreferred("++")
	public static class inc extends AbstractFunction implements Optimizable {

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
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			return doIncrementDecrement(nodes, parent, env, t, this, true, true);
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			long value = 1;
			if(args.length == 2) {
				if(args[1] instanceof IVariable) {
					IVariable cur2 = (IVariable) args[1];
					args[1] = env.getEnv(GlobalEnv.class).GetVarList().get(cur2.getVariableName(), cur2.getTarget(),
							env);
				}
				value = Static.getInt(args[1], t);
			}
			if(args[0] instanceof IVariable) {
				IVariable cur = (IVariable) args[0];
				IVariable v = env.getEnv(GlobalEnv.class).GetVarList().get(cur.getVariableName(), cur.getTarget(), env);
				Mixed newVal;
				if(Static.anyDoubles(v.ival())) {
					newVal = new CDouble(Static.getDouble(v.ival(), t) + value, t);
				} else {
					newVal = new CInt(Static.getInt(v.ival(), t) + value, t);
				}
				if(v.ival() instanceof CMutablePrimitive) {
					newVal = ((CMutablePrimitive) v.ival()).setAndReturn(newVal, t);
				}
				v = new IVariable(v.getDefinedType(), v.getVariableName(), newVal, t, env);
				env.getEnv(GlobalEnv.class).GetVarList().set(v);
				return v;
			} else if(Static.anyDoubles(args[0])) {
				return new CDouble(Static.getNumber(args[0], t) + value, t);
			} else {
				return new CInt(Static.getInt(args[0], t) + value, t);
			}

		}

		@Override
		public String docs() {
			return "ivar {var, [x]} Adds x to var, and stores the new value. Equivalent to ++var in other languages. Expects ivar to be a variable, then"
					+ " returns the ivar, or, if var is a constant number, simply adds x to it, and returns the new number. Operator syntax"
					+ " is also supported: ++@var";
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
		public boolean preResolveVariables() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_0_1;
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
				+ "msg(@x);")};
		}

		@Override
		public Mixed optimize(Target t, Environment env, Mixed... args) throws ConfigCompileException {
			if(args[0] instanceof IVariable) {
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
	@OperatorPreferred("++")
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
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			return Math.doIncrementDecrement(nodes, parent, env, t, this, false, true);
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			long value = 1;
			if(args.length == 2) {
				if(args[1] instanceof IVariable) {
					IVariable cur2 = (IVariable) args[1];
					args[1] = env.getEnv(GlobalEnv.class).GetVarList().get(cur2.getVariableName(), cur2.getTarget(),
							env);
				}
				value = Static.getInt(args[1], t);
			}
			if(args[0] instanceof IVariable) {
				IVariable cur = (IVariable) args[0];
				IVariable v = env.getEnv(GlobalEnv.class).GetVarList().get(cur.getVariableName(), cur.getTarget(), env);
				Mixed newVal;
				if(Static.anyDoubles(v.ival())) {
					newVal = new CDouble(Static.getDouble(v.ival(), t) + value, t);
				} else {
					newVal = new CInt(Static.getInt(v.ival(), t) + value, t);
				}
				if(v.ival() instanceof CMutablePrimitive) {
					newVal = ((CMutablePrimitive) v.ival()).setAndReturn(newVal, t);
				}
				Mixed oldVal = null;
				try {
					oldVal = v.ival().clone();
				} catch (CloneNotSupportedException ex) {
					Logger.getLogger(Math.class.getName()).log(Level.SEVERE, null, ex);
				}
				v = new IVariable(v.getDefinedType(), v.getVariableName(), newVal, t, env);
				env.getEnv(GlobalEnv.class).GetVarList().set(v);
				return oldVal;
			} else if(Static.anyDoubles(args[0])) {
				return new CDouble(Static.getNumber(args[0], t) + value, t);
			} else {
				return new CInt(Static.getInt(args[0], t) + value, t);
			}
		}

		@Override
		public String docs() {
			return "ivar {var, [x]} Adds x to var, and stores the new value. Equivalent to var++ in other languages. Expects ivar to be a variable, then"
					+ " returns a copy of the old ivar, or, if var is a constant number, simply adds x to it, and returns the new number. Operator"
					+ " notation is also supported: @var++";
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
		public boolean preResolveVariables() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed optimize(Target t, Environment env, Mixed... args) throws ConfigCompileException {
			if(args[0] instanceof IVariable) {
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
				+ "msg(@a);")};
		}

	}

	@api
	@seealso({inc.class, postdec.class, postinc.class})
	@OperatorPreferred("--")
	public static class dec extends AbstractFunction implements Optimizable {

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
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			return doIncrementDecrement(nodes, parent, env, t, this, true, false);
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			long value = 1;
			if(args.length == 2) {
				if(args[1] instanceof IVariable) {
					IVariable cur2 = (IVariable) args[1];
					args[1] = env.getEnv(GlobalEnv.class).GetVarList().get(cur2.getVariableName(), cur2.getTarget(),
							env);
				}
				value = Static.getInt(args[1], t);
			}
			if(args[0] instanceof IVariable) {
				IVariable cur = (IVariable) args[0];
				IVariable v = env.getEnv(GlobalEnv.class).GetVarList().get(cur.getVariableName(), cur.getTarget(), env);
				Mixed newVal;
				if(Static.anyDoubles(v.ival())) {
					newVal = new CDouble(Static.getDouble(v.ival(), t) - value, t);
				} else {
					newVal = new CInt(Static.getInt(v.ival(), t) - value, t);
				}
				if(v.ival() instanceof CMutablePrimitive) {
					newVal = ((CMutablePrimitive) v.ival()).setAndReturn(newVal, t);
				}
				v = new IVariable(v.getDefinedType(), v.getVariableName(), newVal, t, env);
				env.getEnv(GlobalEnv.class).GetVarList().set(v);
				return v;
			} else if(Static.anyDoubles(args[0])) {
				return new CDouble(Static.getNumber(args[0], t) - value, t);
			} else {
				return new CInt(Static.getInt(args[0], t) - value, t);
			}
		}

		@Override
		public String docs() {
			return "ivar {var, [value]} Subtracts value from var, and stores the new value. Value defaults to 1. Equivalent to --var (or var -= value) in other languages. Expects ivar to be a variable, then"
					+ " returns the ivar, or if var is a constant number, simply adds x to it, and returns the new number. Operator"
					+ " syntax is also supported: --@var";
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
		public boolean preResolveVariables() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed optimize(Target t, Environment env, Mixed... args) throws ConfigCompileException {
			if(args[0] instanceof IVariable) {
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
				+ "msg(@x);")};
		}

	}

	@api
	@seealso({postinc.class, inc.class, dec.class})
	@OperatorPreferred("--")
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
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			return doIncrementDecrement(nodes, parent, env, t, this, false, false);
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			long value = 1;
			if(args.length == 2) {
				if(args[1] instanceof IVariable) {
					IVariable cur2 = (IVariable) args[1];
					args[1] = env.getEnv(GlobalEnv.class).GetVarList().get(cur2.getVariableName(), cur2.getTarget(),
							env);
				}
				value = Static.getInt(args[1], t);
			}
			if(args[0] instanceof IVariable) {
				IVariable cur = (IVariable) args[0];
				IVariable v = env.getEnv(GlobalEnv.class).GetVarList().get(cur.getVariableName(), cur.getTarget(), env);
				Mixed newVal;
				if(Static.anyDoubles(v.ival())) {
					newVal = new CDouble(Static.getDouble(v.ival(), t) - value, t);
				} else {
					newVal = new CInt(Static.getInt(v.ival(), t) - value, t);
				}
				if(v.ival() instanceof CMutablePrimitive) {
					newVal = ((CMutablePrimitive) v.ival()).setAndReturn(newVal, t);
				}
				Mixed oldVal = null;
				try {
					oldVal = v.ival().clone();
				} catch (CloneNotSupportedException ex) {
					Logger.getLogger(Math.class.getName()).log(Level.SEVERE, null, ex);
				}
				v = new IVariable(v.getDefinedType(), v.getVariableName(), newVal, t, env);
				env.getEnv(GlobalEnv.class).GetVarList().set(v);
				return oldVal;
			} else if(Static.anyDoubles(args[0])) {
				return new CDouble(Static.getNumber(args[0], t) - value, t);
			} else {
				return new CInt(Static.getInt(args[0], t) - value, t);
			}
		}

		@Override
		public String docs() {
			return "ivar {var, [x]} Subtracts x from var, and stores the new value. Equivalent to var-- in other languages. Expects ivar to be a variable, then"
					+ " returns a copy of the old ivar, , or, if var is a constant number, simply adds x to it, and returns the new number."
					+ " Operator syntax is also supported: @var--";
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
		public boolean preResolveVariables() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed optimize(Target t, Environment env, Mixed... args) throws ConfigCompileException {
			if(args[0] instanceof IVariable) {
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
				+ "msg(@a);")};
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRERangeException.class, CRECastException.class};
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
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			if(args.length == 0) {
				return new CDouble(java.lang.Math.random(), t);
			} else {
				long min = 0;
				long max = 0;
				if(args.length == 1) {
					max = Static.getInt(args[0], t);
				} else {
					min = Static.getInt(args[0], t);
					max = Static.getInt(args[1], t);
				}
				if(max > Integer.MAX_VALUE || min > Integer.MAX_VALUE) {
					throw new CRERangeException("max and min must be below int max, defined as " + Integer.MAX_VALUE,
							t);
				}

				long range = max - min;
				if(range <= 0) {
					throw new CRERangeException("max - min must be greater than 0", t);
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_2;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			if(args[0].isInstanceOf(CInt.TYPE)) {
				return new CInt(java.lang.Math.abs(Static.getInt(args[0], t)), t);
			} else {
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_3;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_3;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
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
	public static class sqrt extends AbstractFunction implements Optimizable {

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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRERangeException.class, CRECastException.class};
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
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			double d = Static.getNumber(args[0], t);
			if(d < 0) {
				throw new CRERangeException("sqrt expects a number >= 0", t);
			}
			double m = java.lang.Math.sqrt(d);
			if(m == (int) m) {
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREInsufficientArgumentsException.class};
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
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			if(args.length == 0) {
				throw new CREInsufficientArgumentsException("You must send at least one parameter to min", t);
			}
			double lowest = Double.POSITIVE_INFINITY;
			List<Mixed> list = new ArrayList<>();
			recList(list, args);
			for(Mixed c : list) {
				double d = Static.getNumber(c, t);
				if(d < lowest) {
					lowest = d;
				}
			}
			if(lowest == (long) lowest) {
				return new CInt((long) lowest, t);
			} else {
				return new CDouble(lowest, t);
			}
		}

		public List<Mixed> recList(List<Mixed> list, Mixed... args) {
			for(Mixed c : args) {
				if(c.isInstanceOf(CArray.TYPE)) {
					for(int i = 0; i < ((CArray) c).size(); i++) {
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
	public static class max extends AbstractFunction implements Optimizable {

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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREInsufficientArgumentsException.class};
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
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			if(args.length == 0) {
				throw new CREInsufficientArgumentsException("You must send at least one parameter to max", t);
			}
			double highest = Double.NEGATIVE_INFINITY;
			List<Mixed> list = new ArrayList<>();
			recList(list, args);
			for(Mixed c : list) {
				double d = Static.getNumber(c, t);
				if(d > highest) {
					highest = d;
				}
			}
			if(highest == (long) highest) {
				return new CInt((long) highest, t);
			} else {
				return new CDouble(highest, t);
			}
		}

		public List<Mixed> recList(List<Mixed> list, Mixed... args) {
			for(Mixed c : args) {
				if(c.isInstanceOf(CArray.TYPE)) {
					for(int i = 0; i < ((CArray) c).size(); i++) {
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
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
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
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
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
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
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
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
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
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
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
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
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
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
	public static class sinh extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "sinh";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "double {number} Returns the hyperbolic sine of the number";
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
		public Version since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return Static.getNumber(java.lang.Math.sinh(ArgumentValidation.getNumber(args[0], t)), t);
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
	public static class cosh extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "cosh";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "double {number} Returns the hyperbolic cosine of the number";
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
		public Version since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return Static.getNumber(java.lang.Math.cosh(ArgumentValidation.getNumber(args[0], t)), t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					Optimizable.OptimizationOption.CONSTANT_OFFLINE,
					Optimizable.OptimizationOption.CACHE_RETURN
			);
		}
	}

	@api
	public static class tanh extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "tanh";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "double {number} Returns the hyperbolic tangent of the number";
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
		public Version since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return Static.getNumber(java.lang.Math.tanh(ArgumentValidation.getNumber(args[0], t)), t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					Optimizable.OptimizationOption.CONSTANT_OFFLINE,
					Optimizable.OptimizationOption.CACHE_RETURN
			);
		}
	}

	@api
	public static class to_radians extends AbstractFunction implements Optimizable {

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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
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
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
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
	public static class to_degrees extends AbstractFunction implements Optimizable {

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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
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
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
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
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
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
	public static class round extends AbstractFunction implements Optimizable {

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
			return "double {number, [precision]} Unlike floor and ceil, rounds the number to the nearest double that is equal to an integer. Precision defaults to 0, but if set to 1 or more, rounds decimal places."
					+ " For instance, round(2.29, 1) would return 2.3. If precision is &lt; 0, a RangeException is thrown.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRERangeException.class};
		}

		@Override
		public boolean isRestricted() {
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
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			double number = Static.getNumber(args[0], t);
			int precision = 0;
			if(args.length > 1) {
				precision = Static.getInt32(args[1], t);
			}
			if(precision < 0) {
				throw new CRERangeException("precision cannot be less than 0, was " + precision, t);
			}
			number = number * java.lang.Math.pow(10, precision);
			number = java.lang.Math.round(number);
			number = number / java.lang.Math.pow(10, precision);
			return new CDouble(number, t);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Rounding up", "round(2.5)"),
				new ExampleScript("Rounding down", "round(2.229)"),
				new ExampleScript("Higher precision round", "round(2.229, 2)")};
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
	public static class round15 extends AbstractFunction implements Optimizable {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
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
			double x = Static.getDouble(args[0], t) + 1;
			DecimalFormat twoDForm = new DecimalFormat("0.##############E0");
			String str = twoDForm.format(x);
			double d = Double.valueOf(str) - 1;
			return new CDouble(d, t);
		}

		@Override
		public String getName() {
			return "round15";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "double {value} Rounds value to the 15th place. This is useful when doing math using approximations. For instance,"
					+ " sin(math_const('PI')) returns 1.2246467991473532E-16, but sin of pi is actually 0. This happens because"
					+ " pi cannot be accurately represented on a computer, it is an approximation. Using round15, you can round to the"
					+ " next nearest value, which often time should give a more useful answer to display. For instance,"
					+ " round15(sin(math_const('PI'))) is 0. This functionality is not provided by default in methods like sin(),"
					+ " because it technically makes the result less accurate, given the inputs. In general, you should only use this"
					+ " function just before displaying the value to the user. Internally, you should keep the value returned by the input"
					+ " functions.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
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
				new ExampleScript("Without round15", "sin(math_const('PI'));"),
				new ExampleScript("With round15", "round15(sin(math_const('PI')));")
			};
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CRECastException.class, CREPluginInternalException.class};
		}

		@Override
		public boolean isRestricted() {
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
			String expr = args[0].val().trim();
			if("".equals(expr)) {
				throw new CREFormatException("Expression may not be empty", t);
			}
			CArray vars = null;
			if(args.length == 2 && args[1].isInstanceOf(CArray.TYPE)) {
				vars = (CArray) args[1];
			} else if(args.length == 2 && !(args[1].isInstanceOf(CArray.TYPE))) {
				throw new CRECastException("The second argument of expr() should be an array", t);
			}
			if(vars != null && !vars.inAssociativeMode()) {
				throw new CRECastException("The array provided to expr() must be an associative array", t);
			}
			double[] da;
			String[] varNames;
			if(vars != null) {
				int i = 0;
				da = new double[(int) vars.size()];
				varNames = new String[(int) vars.size()];
				for(String key : vars.stringKeySet()) {
					varNames[i] = key;
					da[i] = Static.getDouble(vars.get(key, t), t);
					i++;
				}
			} else {
				da = ArrayUtils.EMPTY_DOUBLE_ARRAY;
				varNames = ArrayUtils.EMPTY_STRING_ARRAY;
			}
			/*try {
				Expression e = Expression.compile(expr, varNames);
				return new CDouble(e.evaluate(da), t);
			} catch (ExpressionException ex) {
				throw new CREPluginInternalException("Your expression was invalidly formatted", t, ex);
			}*/
			String eClass = "com.sk89q.worldedit.internal.expression.Expression";
			String errClass = "com.sk89q.worldedit.internal.expression.ExpressionException";
			Class eClazz;
			Class errClazz;
			try {
				eClazz = Class.forName(eClass);
				errClazz = Class.forName(errClass);
			} catch (ClassNotFoundException cnf) {
				throw new CREPluginInternalException("You are missing a required dependency: " + eClass, t);
			}
			try {
				Object e = ReflectionUtils.invokeMethod(eClazz, null, "compile",
						new Class[]{String.class, String[].class}, new Object[]{expr, varNames});
				Object d = ReflectionUtils.invokeMethod(eClazz, e, "evaluate",
						new Class[]{double[].class}, new Object[]{da});
				return new CDouble((double) d, t);
			} catch (ReflectionUtils.ReflectionException rex) {
				if(rex.getCause().getClass().isAssignableFrom(errClazz)) {
					throw new CREPluginInternalException("Your expression was invalidly formatted", args[0].getTarget(), rex.getCause());
				} else {
					throw new CREPluginInternalException(rex.getMessage(),
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
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
			if(args[0].isInstanceOf(CInt.TYPE)) {
				return new CInt(-(Static.getInt(args[0], t)), t);
			} else {
				return new CDouble(-(Static.getDouble(args[0], t)), t);
			}
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRERangeException.class};
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
			double val = Static.getDouble(args[0], t);
			if(val <= 0) {
				throw new CRERangeException("val was <= 0", t);
			}
			double r;
			if(args.length == 1) {
				r = java.lang.Math.log(val);
			} else { // if(args.length == 2) {
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
		public MSVersion since() {
			return MSVersion.V3_3_1;
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
				new ExampleScript("Error condition", "logarithm(0)", true),
				new ExampleScript("Error condition", "logarithm(-1)", true)};
		}

	}

	@api
	public static class math_const extends AbstractFunction implements Optimizable {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@MEnum("ms.lang.MathConstants")
		public static enum MathConstants implements SimpleDocumentation {
			NaN(Double.NaN, "A representation of an undefinied number (Not a Number), per the IEEE 754 standard"),
			NEGATIVE_INFINITY(Double.NEGATIVE_INFINITY, "A representation of negative infinity, per the IEEE 754 standard"),
			INFINITY(Double.POSITIVE_INFINITY, "A representation of positive infinity, per the IEEE 754 standard"),
			DOUBLE_MAX(Double.MAX_VALUE, "The higest number that can be represented as a double"),
			DOUBLE_MIN(Double.MIN_VALUE, "The lowest number that can be represented as a double"),
			LONG_MAX(Long.MAX_VALUE, "The higest number that can be represented as a long"),
			LONG_MIN(Long.MIN_VALUE, "The lowest number that can be represented as a long"),
			SHORT_MAX(Short.MAX_VALUE, "The higest number that can be represented as a short"),
			SHORT_MIN(Short.MIN_VALUE, "The lowest number that can be represented as a short"),
			INTEGER_MAX(Integer.MAX_VALUE, "The higest number that can be represented as a integer"),
			INTEGER_MIN(Integer.MIN_VALUE, "The lowest number that can be represented as an integer"),
			FLOAT_MAX(Float.MAX_VALUE, "The higest number that can be represented as a float"),
			FLOAT_MIN(Float.MIN_VALUE, "The lowest number that can be represented as a float"),
			BYTE_MAX(Byte.MAX_VALUE, "The higest number that can be represented as a byte"),
			BYTE_MIN(Byte.MIN_VALUE, "The lowest number that can be represented as a byte"),
			E(java.lang.Math.E, "The mathematical constant e, also known as Euler's number (not to be confused with the Euler-Mascheroni constant)"),
			PI(java.lang.Math.PI, "The value of π (pi)"),
			PHI(1.6180339887498948482045868343656381177203091798057628621, "The golden ratio"),
			C(2.99792458e8, "The speed of light in a vacuum, in meters per second"),
			EULER(0.5772156649015627, "The Euler-Mascheroni constant γ (not to be confused with e)");

			public static String enumDocs() {
				return "Contains a list of types of math constants";
			}

			public static Version enumSince() {
				return new math_const().since();
			}

			private final Number value;
			private final String doc;
			private final Version since;

			private MathConstants(Number value, String doc) {
				this(value, doc, new math_const().since());
			}

			private MathConstants(Number value, String doc, Version since) {
				this.value = value;
				this.doc = doc;
				this.since = since;
			}

			public Number getValue() {
				return this.value;
			}

			public String getDoc() {
				return doc;
			}

			@Override
			public String getName() {
				return MathConstants.class.getAnnotation(MEnum.class).value();
			}

			@Override
			public String docs() {
				return getDoc();
			}

			@Override
			public Version since() {
				return since;
			}
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			try {
				MathConstants c = MathConstants.valueOf(args[0].val());
				Number v = c.getValue();
				if(v instanceof Double) {
					return new CDouble((Double) c.getValue(), t);
				} else {
					return new CInt((Integer) c.getValue(), t);
				}
			} catch (IllegalArgumentException ex) {
				throw new CRECastException("No constant with the value " + args[0].val() + " exists.", t);
			}
		}

		@Override
		public String getName() {
			return "math_const";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			String docs = "number {constant} Returns the value of various math constants. The constant argument must be one of the following: "
					+ StringUtils.Join(MathConstants.values(), ", ", ", or ") + "\n";
			docs += "---- The following table lists the values, and a brief description of each:\n"
					+ "{| cellspacing=\"1\" cellpadding=\"1\" border=\"1\" class=\"wikitable\"\n"
					+ "|-\n"
					+ "! Constant Name\n"
					+ "! Description\n"
					+ "! Value\n";
			for(MathConstants value : MathConstants.values()) {
				docs += "|-\n"
						+ "| " + value.name() + "\n"
						+ "| " + value.getDoc() + "\n"
						+ "| " + value.getValue() + "\n";
			}
			docs += "|}\n\n"
					+ "Note that this function is optimized, and when given a constant value for the parameter, is resolved at compile time.";
			return docs;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.CONSTANT_OFFLINE);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "math_const('PI');")
			};
		}

	}

	@api
	public static class clamp extends CompositeFunction implements Optimizable {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
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
		protected String script() {
			return getBundledCode();
		}

		@Override
		public String getName() {
			return "clamp";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{3};
		}

		@Override
		public String docs() {
			return "float {value, min, max} Clamps a value between a certain range, inclusive. If"
					+ " the value is less than the min, the min is returned, if it is greater than"
					+ " the max, the max is returned, and if it is between the two values, the original"
					+ " value is returned. Alternatively, if min > max, clamp works in reverse mode. In"
					+ " that case, the value must be less than max (actually the minimum) and greater than"
					+ " min (actually the maximum). If the value is between the two, it is determined which"
					+ " of the two values it is closer to, and then that value is returned. If the value"
					+ " is exactly between both min and max, the minimum (actually the max) is returned."
					+ " If min == max, then min is returned.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.CONSTANT_OFFLINE);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("No clamping", "clamp(8, 1, 10);"),
				new ExampleScript("Clamp to minimum", "clamp(1, 10, 20);"),
				new ExampleScript("Clamp to maximum", "clamp(50, 10, 25);"),
				new ExampleScript("Reverse mode, no clamping below", "clamp(5, 20, 10);"),
				new ExampleScript("Reverse mode, no clamping above", "clamp(50, 20, 10);"),
				new ExampleScript("Reverse mode, clamping to minimum", "clamp(12, 20, 10);"),
				new ExampleScript("Reverse mode, clamping to maximum", "clamp(19, 20, 10);"),
				new ExampleScript("Reverse mode, clamping to minimum due to equal distance", "clamp(15, 20, 10);")
			};
		}
	}

	@api
	public static class hypot extends CompositeFunction {

		@Override
		protected String script() {
			return getBundledCode();
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRERangeException.class, CRECastException.class};
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
		public String getName() {
			return "hypot";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "number {a, b} Given two sides of a right triangle, returns the length of the hypotenuse, using the"
					+ " equation a² + b² = c², where a and b are the arguments provided.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Standard usage", "hypot(3, 4)"),
				new ExampleScript("Standard usage", "hypot(1, 1)"),
				new ExampleScript("Standard usage", "hypot(2.5, 4.6)"),
				new ExampleScript("Values may not be negative", "hypot(-1, -1)", true)
			};
		}

	}

}
