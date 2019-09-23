package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.LinkedComparatorSet;
import com.laytonsmith.PureUtilities.RunnableQueue;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.StaticLayer;
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
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CMutablePrimitive;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CSlice;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.CRE.CREIndexOverflowException;
import com.laytonsmith.core.exceptions.CRE.CREInsufficientArgumentsException;
import com.laytonsmith.core.exceptions.CRE.CRELengthException;
import com.laytonsmith.core.exceptions.CRE.CREPluginInternalException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.ProgramFlowManipulationException;
import com.laytonsmith.core.functions.BasicLogic.equals;
import com.laytonsmith.core.functions.BasicLogic.equals_ic;
import com.laytonsmith.core.functions.BasicLogic.sequals;
import com.laytonsmith.core.functions.DataHandling.array;
import com.laytonsmith.core.natives.interfaces.ArrayAccess;
import com.laytonsmith.core.natives.interfaces.Iterator;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

@core
public class ArrayHandling {

	public static String docs() {
		return "This class contains functions that provide a way to manipulate arrays. To create an array, use the"
				+ " <code>array</code> function. For more detailed information on array usage, see the page on"
				+ " [[Arrays|arrays]]";
	}

	@api
	public static class array_size extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "array_size";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			if(args[0].isInstanceOf(CArray.TYPE) && !(args[0] instanceof CMutablePrimitive)) {
				return new CInt(((CArray) args[0]).size(), t);
			}
			throw new CRECastException("Argument 1 of array_size must be an array", t);
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public String docs() {
			return "int {array} Returns the size of this array as an integer.";
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
				new ExampleScript("Demonstrates usage", "array_size(array(1, 2, 3, 4, 5));")};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.NO_SIDE_EFFECTS);
		}

	}

	@api(environments = {GlobalEnv.class})
	@seealso({array_set.class, array.class, com.laytonsmith.tools.docgen.templates.Arrays.class})
	public static class array_get extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "array_get";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			Mixed index;
			Mixed defaultConstruct = null;
			if(args.length >= 2) {
				index = args[1];
			} else {
				index = new CSlice(0, -1, t);
			}
			if(args.length >= 3) {
				defaultConstruct = args[2];
			}

			if(args[0].isInstanceOf(CArray.TYPE)) {
				CArray ca = (CArray) args[0];
				if(index instanceof CSlice) {

					// Deep clone the array if the "index" is the initial one.
					if(((CSlice) index).getStart() == 0 && ((CSlice) index).getFinish() == -1) {
						return ca.deepClone(t);
					} else if(ca.inAssociativeMode()) {
						throw new CRECastException("Array slices are not allowed with an associative array", t);
					}

					//It's a range
					long start = ((CSlice) index).getStart();
					long finish = ((CSlice) index).getFinish();
					try {
						//Convert negative indexes
						if(start < 0) {
							start = ca.size() + start;
						}
						if(finish < 0) {
							finish = ca.size() + finish;
						}
						CArray na = ca.createNew(t);
						if(finish < start) {
							//return an empty array in cases where the indexes don't make sense
							return na;
						}
						for(long i = start; i <= finish; i++) {
							try {
								na.push(ca.get((int) i, t).clone(), t);
							} catch (CloneNotSupportedException e) {
								na.push(ca.get((int) i, t), t);
							}
						}
						return na;
					} catch (NumberFormatException e) {
						throw new CRECastException("Ranges must be integer numbers, i.e., [0..5]", t);
					}
				} else {
					try {
						if(!ca.inAssociativeMode()) {
							if(index instanceof CNull) {
								throw new CRECastException("Expected a number, but received null instead", t);
							}
							long iindex = Static.getInt(index, t);
							if(iindex < 0) {
								//negative index, convert to positive index
								iindex = ca.size() + iindex;
							}
							return ca.get(iindex, t);
						} else {
							return ca.get(index, t);
						}
					} catch (ConfigRuntimeException e) {
						if(e instanceof CREThrowable
								&& ((CREThrowable) e).isInstanceOf(CREIndexOverflowException.TYPE)) {
							if(defaultConstruct != null) {
								return defaultConstruct;
							}
						}
						if(env.getEnv(GlobalEnv.class).GetFlag("array-special-get") != null) {
							//They are asking for an array that doesn't exist yet, so let's create it now.
							CArray c;
							if(ca.inAssociativeMode()) {
								c = CArray.GetAssociativeArray(t);
							} else {
								c = new CArray(t);
							}
							ca.set(index, c, t);
							return c;
						}
						throw e;
					}
				}
			} else if(args[0].isInstanceOf(ArrayAccess.TYPE)) {
				com.laytonsmith.core.natives.interfaces.Iterable aa
						= (com.laytonsmith.core.natives.interfaces.Iterable) args[0];
				if(index instanceof CSlice) {
					//It's a range
					int start = (int) ((CSlice) index).getStart();
					int finish = (int) ((CSlice) index).getFinish();
					try {
						//Convert negative indexes
						if(start < 0) {
							start = (int) aa.size() + start;
						}
						if(finish < 0) {
							finish = (int) aa.size() + finish;
						}
						return aa.slice(start, finish + 1, t);
					} catch (NumberFormatException e) {
						throw new CRECastException("Ranges must be integer numbers, i.e., [0..5]", t);
					}
				} else if(index.isInstanceOf(CInt.TYPE)) {
					return aa.get(Static.getInt32(index, t), t);
				} else {
					return aa.get(index, t);
				}
			} else {
				throw new CRECastException("Argument 1 of array_get must be an array", t);
			}
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREIndexOverflowException.class};
		}

		@Override
		public String docs() {
			return "mixed {array, index, [default]} Returns the element specified at the index of the array."
					+ " If the element doesn't exist, an exception is thrown. ---- You can use a more traditional"
					+ " method to access elements in an array: array[index] is the same as array_get(array, index),"
					+ " where array is a variable, or function that is an array. In fact, the compiler converts"
					+ " array[index] into array_get(array, index). So if there is a problem with your code, you will"
					+ " get an error message about a problem with the array_get function, even though you may not be"
					+ " using that function directly. If using the plain function access, then if a default is"
					+ " provided, the function will always return that value if the array otherwise doesn't have a"
					+ " value there. This is opposed to throwing an exception or returning null.";
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
		public Mixed optimize(Target t, Environment env, Mixed... args) throws ConfigCompileException {
			if(args.length == 0) {
				throw new CRECastException("Argument 1 of array_get must be an array", t);
			}
			if(args[0].isInstanceOf(ArrayAccess.TYPE)) {
				ArrayAccess aa = (ArrayAccess) args[0];
				if(!aa.canBeAssociative()) {
					if(!(args[1].isInstanceOf(CInt.TYPE)) && !(args[1] instanceof CSlice)) {
						throw new ConfigCompileException("Accessing an element as an associative array,"
								+ " when it can only accept integers.", t);
					}
				}
				return null;
			} else {
				throw new ConfigCompileException("Trying to access an element like an array,"
						+ " but it does not support array access.", t);
			}

		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Demonstrates basic usage", "msg(array(0, 1, 2)[2]);"),
				new ExampleScript("Demonstrates exception", "msg(array()[1]);"),
				new ExampleScript("Demonstrates basic functional usage", "msg(array_get(array(1, 2, 3), 2));"),
				new ExampleScript("Demonstrates default (note that you cannot use the bracket syntax with this)",
				"msg(array_get(array(), 1, 'default'));")};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.OPTIMIZE_CONSTANT
			);
		}

	}

	@api
	@seealso({array_get.class, array.class, array_push.class, com.laytonsmith.tools.docgen.templates.Arrays.class})
	@OperatorPreferred("@array[@key] = @value")
	public static class array_set extends AbstractFunction {

		@Override
		public String getName() {
			return "array_set";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{3};
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			env.getEnv(GlobalEnv.class).SetFlag("array-special-get", true);
			Mixed array = parent.seval(nodes[0], env);
			env.getEnv(GlobalEnv.class).ClearFlag("array-special-get");
			Mixed index = parent.seval(nodes[1], env);
			Mixed value = parent.seval(nodes[2], env);
			if(!(array.isInstanceOf(CArray.TYPE))) {
				throw new CRECastException("Argument 1 of array_set must be an array", t);
			}
			try {
				((CArray) array).set(index, value, t);
			} catch (IndexOutOfBoundsException e) {
				throw new CREIndexOverflowException("The index " + new CString(index).getQuote() + " is out of bounds", t);
			}
			return value;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			if(args[0].isInstanceOf(CArray.TYPE)) {
				try {
					((CArray) args[0]).set(args[1], args[2], t);
				} catch (IndexOutOfBoundsException e) {
					throw new CREIndexOverflowException("The index " + args[1].val() + " is out of bounds", t);
				}
				return args[2];
			}
			throw new CRECastException("Argument 1 of array_set must be an array", t);
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREIndexOverflowException.class};
		}

		@Override
		public String docs() {
			return "mixed {array, index, value} Sets the value of the array at the specified index."
					+ " The value that was set is returned, to allow for chaining.";
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
				new ExampleScript("Demonstrates using assignment",
				"array @array = array(null);\n"
				+ "msg(@array);\n"
				+ "@array[0] = 'value0';\n"
				+ "msg(@array);"),
				new ExampleScript("Demonstrates functional usage",
				"array @array = array(null);\n"
				+ "msg(@array);\n"
				+ "array_set(@array, 0, 'value0');\n"
				+ "msg(@array);")};
		}
	}

	@api
	@seealso({array_set.class, array_push_all.class})
	@OperatorPreferred("@array[] = @value")
	public static class array_push extends AbstractFunction {

		@Override
		public String getName() {
			return "array_push";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			if(args.length < 2) {
				throw new CREInsufficientArgumentsException("At least 2 arguments must be provided to array_push", t);
			}
			if(args[0].isInstanceOf(CArray.TYPE)) {
				CArray array = (CArray) args[0];
				int initialSize = (int) array.size();
				for(int i = 1; i < args.length; i++) {
					array.push(args[i], t);
					for(Iterator iterator : env.getEnv(GlobalEnv.class).GetArrayAccessIteratorsFor(((ArrayAccess) args[0]))) {
						//This is always pushing after the current index.
						//Given that this is the last one, we don't need to waste
						//time with a call to increment the blacklist items either.
						iterator.addToBlacklist(initialSize + i - 1);
					}
				}
				return CVoid.VOID;
			}
			throw new CRECastException("Argument 1 of array_push must be an array", t);
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public String docs() {
			return "void {array, value, [value2...]} Pushes the specified value(s) onto the end of the array. Unlike"
					+ " calling array_set(@array, array_size(@array), @value) on a normal array, the size of the array"
					+ " is increased first. The special operator syntax @array[] = 'value' is also supported, as"
					+ " shorthand for array_push().";
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
				new ExampleScript("Operator syntax. Note the difference between this and the array clone"
				+ " operator is that this occurs on the Left Hand Side (LHS) of the assignment.",
				"array @array = array();\n"
				+ "@array[] = 'new value';"),
				new ExampleScript("Demonstrates functional usage",
				"array @array = array();\n"
				+ "msg(@array);\n"
				+ "array_push(@array, 0);\n"
				+ "msg(@array);"),
				new ExampleScript("Demonstrates pushing multiple values (note that it is not possible to use the bracket notation"
				+ " and push multiple values)",
				"array @array = array();\n"
				+ "msg(@array);\n"
				+ "array_push(@array, 0, 1, 2);\n"
				+ "msg(@array);")};
		}
	}

	@api
	public static class array_insert extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREIndexOverflowException.class};
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
			CArray array = Static.getArray(args[0], t);
			Mixed value = args[1];
			int index = Static.getInt32(args[2], t);
			try {
				array.push(value, index, t);
				//If the push succeeded (actually an insert) we need to check to see if we are currently iterating
				//and act appropriately.
				for(Iterator iterator : environment.getEnv(GlobalEnv.class).GetArrayAccessIteratorsFor(array)) {
					if(index <= iterator.getCurrent()) {
						//The insertion happened before (or at) this index, so we need to increment the
						//iterator, as well as increment all the blacklist items above this one.
						iterator.incrementCurrent();
					} else {
						//The insertion happened after this index, so we need to increment the
						//blacklist values after this one, and add this index to the blacklist
						iterator.incrementBlacklistAfter(index);
						iterator.addToBlacklist(index);
					}
				}
			} catch (IllegalArgumentException e) {
				throw new CRECastException(e.getMessage(), t);
			} catch (IndexOutOfBoundsException ex) {
				throw new CREIndexOverflowException(ex.getMessage(), t);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "array_insert";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{3};
		}

		@Override
		public String docs() {
			return "void {array, item, index} Inserts an item at the specified index, and shifts all other items in the"
					+ " array to the right one. If index is greater than the size of the array, an IndexOverflowException"
					+ " is thrown, though the index may be equal to the size, in which case this works just like"
					+ " array_push(). The array must be normal though; associative arrays are not supported.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "array @array = array(1, 3, 4);\n"
				+ "array_insert(@array, 2, 1);\n"
				+ "msg(@array);"),
				new ExampleScript("Usage as if it were array_push", "@array = array(1, 2, 3);\n"
				+ "array_insert(@array, 4, array_size(@array));\n"
				+ "msg(@array);")
			};
		}

	}

	@api
	@seealso({array_index_exists.class, array_scontains.class})
	public static class array_contains extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "array_contains";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			if(!(args[0].isInstanceOf(CArray.TYPE))) {
				throw new CRECastException("Argument 1 of " + this.getName() + " must be an array", t);
			}
			CArray ca = (CArray) args[0];
			for(Mixed key : ca.keySet()) {
				if(new equals().exec(t, env, ca.get(key, t), args[1]).getBoolean()) {
					return CBoolean.TRUE;
				}
			}
			return CBoolean.FALSE;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public String docs() {
			return "boolean {array, testValue} Checks to see if testValue is in array. For associative arrays, only the"
					+ " values are searched, the keys are ignored. If you need to check for the existence of a"
					+ " particular key, use array_index_exists().";
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
				new ExampleScript("Demonstrates finding a value", "array_contains(array(0, 1, 2), 2)"),
				new ExampleScript("Demonstrates not finding a value", "array_contains(array(0, 1, 2), 5)"),
				new ExampleScript("Demonstrates finding a value listed multiple times", "array_contains(array(1, 1, 1), 1)"),
				new ExampleScript("Demonstrates finding a string", "array_contains(array('a', 'b', 'c'), 'b')"),
				new ExampleScript("Demonstrates finding a value in an associative array", "array_contains(array('a': 1, 'b': 2), 2)")
			};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.NO_SIDE_EFFECTS);
		}
	}

	@api
	@seealso({array_contains.class})
	public static class array_contains_ic extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "array_contains_ic";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {array, testValue} Works like array_contains, except the comparison ignores case.";
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(args[0].isInstanceOf(CArray.TYPE)) {
				CArray ca = (CArray) args[0];
				for(int i = 0; i < ca.size(); i++) {
					if(new equals_ic().exec(t, environment, ca.get(i, t), args[1]).getBoolean()) {
						return CBoolean.TRUE;
					}
				}
				return CBoolean.FALSE;
			} else {
				throw new CRECastException("Argument 1 of " + this.getName() + " must be an array", t);
			}
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Demonstrates usage", "array_contains_ic(array('A', 'B', 'C'), 'A')"),
				new ExampleScript("Demonstrates usage", "array_contains_ic(array('A', 'B', 'C'), 'a')"),
				new ExampleScript("Demonstrates usage", "array_contains_ic(array('A', 'B', 'C'), 'd')")};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.NO_SIDE_EFFECTS);
		}
	}

	@api
	@seealso({array_index_exists.class, array_contains.class})
	public static class array_scontains extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "array_scontains";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			if(!(args[0].isInstanceOf(CArray.TYPE))) {
				throw new CRECastException("Argument 1 of " + this.getName() + " must be an array", t);
			}
			CArray ca = (CArray) args[0];
			for(Mixed key : ca.keySet()) {
				if(new sequals().exec(t, env, ca.get(key, t), args[1]).getBoolean()) {
					return CBoolean.TRUE;
				}
			}
			return CBoolean.FALSE;
		}

		@Override
		public Class[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public String docs() {
			return "boolean {array, testValue} Checks if the array contains a value of the same datatype and value as"
					+ " testValue. For associative arrays, only the values are searched, the keys are ignored."
					+ " If you need to check for the existence of a particular key, use array_index_exists().";
		}

		@Override
		public boolean isRestricted() {
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
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Demonstrates finding a value",
				"array_scontains(array(0, 1, 2), 2)"),
				new ExampleScript("Demonstrates not finding a value because of a value mismatch",
				"array_scontains(array(0, 1, 2), 5)"),
				new ExampleScript("Demonstrates not finding a value because of a type mismatch",
				"array_scontains(array(0, 1, 2), '2')"),
				new ExampleScript("Demonstrates finding a value listed multiple times",
				"array_scontains(array(1, 1, 1), 1)"),
				new ExampleScript("Demonstrates finding a string",
				"array_scontains(array('a', 'b', 'c'), 'b')"),
				new ExampleScript("Demonstrates finding a value in an associative array",
				"array_scontains(array('a': 1, 'b': 2), 2)")
			};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.NO_SIDE_EFFECTS);
		}
	}

	@api
	public static class array_index_exists extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "array_index_exists";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "boolean {array, index...} Checks to see if the specified array has an element at index. If more"
					+ " than one index is specified, then it recursively checks down nested arrays.";
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
			if(args[0].isInstanceOf(CArray.TYPE)) {
				Mixed m = args[0];
				for(int i = 1; i < args.length; i++) {
					if(!(m.isInstanceOf(CArray.TYPE))) {
						return CBoolean.FALSE;
					}
					CArray ca = (CArray) m;
					if(!ca.inAssociativeMode()) {
						try {
							int index = Static.getInt32(args[i], t);
							if(index >= ca.size() || index < 0) {
								return CBoolean.FALSE;
							}
						} catch (ConfigRuntimeException e) {
							//They probably sent a key that can't be translated into an int, so it doesn't exist here.
							return CBoolean.FALSE;
						}
					} else {
						if(!ca.containsKey(args[i].val())) {
							return CBoolean.FALSE;
						}
					}
					m = ca.get(args[i], t);
				}
				return CBoolean.TRUE;
			} else {
				throw new CRECastException("Expecting argument 1 to be an array", t);
			}
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs, List<ParseTree> children,
				FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() < 2) {
				throw new ConfigCompileException(getName() + " must have 2 or more arguments", t);
			}
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Demonstrates a true condition", "array_index_exists(array(0, 1, 2), 0)"),
				new ExampleScript("Demonstrates a false condition", "array_index_exists(array(0, 1, 2), 3)"),
				new ExampleScript("Demonstrates an associative array", "array_index_exists(array(a: 'A', b: 'B'), 'a')"),
				new ExampleScript("Demonstrates an associative array", "array_index_exists(array(a: 'A', b: 'B'), 'c')"),
				new ExampleScript("Demonstrates nested arrays", "// Check to make sure that @array['a']['b']['c'] would work\n"
				+ "@array = array(a: array(b: array(c: null)));\n"
				+ "msg(array_index_exists(@array, 'a', 'b', 'c'));"),
				new ExampleScript("Demonstrates nested arrays, where the value is not an array (if the first element is"
						+ " not an array an exception will be thrown, but inner values need not be arrays).",
						"@array = array(a: array(b: 1));\n"
						+ "msg(array_index_exists(@array, 'a', 'b', 'c'));")
			};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.NO_SIDE_EFFECTS, OptimizationOption.OPTIMIZE_DYNAMIC);
		}
	}

	@api
	public static class array_resize extends AbstractFunction {

		@Override
		public String getName() {
			return "array_resize";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "array {array, size, [fill]} Resizes the given array so that it is at least of size size, filling"
					+ " the blank spaces with fill, or null by default. If the size of the array is already at least"
					+ " size, nothing happens; in other words this function can only be used to increase the size of"
					+ " the array. A reference to the array is returned, for easy chaining.";
			//+ " If the array is an associative array, the non numeric values are simply copied over.";
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
			return MSVersion.V3_2_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public CArray exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			if(args[0].isInstanceOf(CArray.TYPE) && args[1].isInstanceOf(CInt.TYPE)) {
				CArray original = (CArray) args[0];
				int size = (int) ((CInt) args[1]).getInt();
				Mixed fill = CNull.NULL;
				if(args.length == 3) {
					fill = args[2];
				}
				for(long i = original.size(); i < size; i++) {
					original.push(fill, t);
				}
			} else {
				throw new CRECastException("Argument 1 must be an array, and argument 2 must be an integer in array_resize", t);
			}
			return (CArray) args[0];
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Demonstrates basic usage",
				"array @array = array();\n"
				+ "msg(@array);\n"
				+ "array_resize(@array, 2);\n"
				+ "msg(@array);"),
				new ExampleScript("Demonstrates custom fill",
				"array @array = array();\n"
				+ "msg(@array);\n"
				+ "array_resize(@array, 2, 'a');\n"
				+ "msg(@array);")};
		}
	}

	@api
	public static class range extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "range";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		@Override
		public String docs() {
			return "array {start, finish, [increment] | finish} Returns an array of numbers from start to (finish - 1)"
					+ " skipping increment integers per count. start defaults to 0, and increment defaults to 1."
					+ " All inputs must be integers. If the input doesn't make sense, it will reasonably degrade, and"
					+ " return an empty array.";
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
			return MSVersion.V3_2_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public CArray exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			long start = 0;
			long finish = 0;
			long increment = 1;
			if(args.length == 1) {
				finish = Static.getInt(args[0], t);
			} else if(args.length == 2) {
				start = Static.getInt(args[0], t);
				finish = Static.getInt(args[1], t);
			} else if(args.length == 3) {
				start = Static.getInt(args[0], t);
				finish = Static.getInt(args[1], t);
				increment = Static.getInt(args[2], t);
			}
			if(start < finish && increment < 0 || start > finish && increment > 0 || increment == 0) {
				return new CArray(t);
			}
			CArray ret = new CArray(t);
			for(long i = start; (increment > 0 ? i < finish : i > finish); i = i + increment) {
				ret.push(new CInt(i, t), t);
			}
			return ret;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "range(10)"),
				new ExampleScript("Complex usage", "range(0, 10)"),
				new ExampleScript("With skips", "range(0, 10, 2)"),
				new ExampleScript("Invalid input", "range(0, 10, -1)"),
				new ExampleScript("In reverse", "range(10, 0, -1)")};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.NO_SIDE_EFFECTS);
		}

	}

	@api
	public static class array_keys extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "array_keys";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "array {array} Returns the keys in this array as a normal array. If the array passed in is already a"
					+ " normal array, the keys will be 0 -> (array_size(array) - 1)";
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
			// As an exception, strings aren't supported here. There's no reason to do this for a string that isn't accidental.
			if(args[0].isInstanceOf(ArrayAccess.TYPE) && !(args[0].isInstanceOf(CString.TYPE))) {
				ArrayAccess ca = (ArrayAccess) args[0];
				CArray ca2 = new CArray(t);
				for(Mixed c : ca.keySet()) {
					ca2.push(c, t);
				}
				return ca2;
			} else {
				throw new CRECastException(this.getName() + " expects arg 1 to be an array", t);
			}
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "array_keys(array('a', 'b', 'c'))"),
				new ExampleScript("With associative array", "array_keys(array(one: 'a', two: 'b', three: 'c'))")};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.NO_SIDE_EFFECTS);
		}
	}

	@api
	public static class array_normalize extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "array_normalize";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "array {array} Returns a new normal array, given an associative array. (If the array passed in is"
					+ " not associative, a copy of the array is returned).";
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
			if(args[0].isInstanceOf(CArray.TYPE)) {
				CArray ca = Static.getArray(args[0], t);
				CArray ca2 = new CArray(t);
				for(Mixed c : ca.keySet()) {
					ca2.push(ca.get(c, t), t);
				}
				return ca2;
			} else {
				throw new CRECastException(this.getName() + " expects arg 1 to be an array", t);
			}
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "array_normalize(array(one: 'a', two: 'b', three: 'c'))"),
				new ExampleScript("Usage with normal array", "array_normalize(array(1, 2, 3))")};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.NO_SIDE_EFFECTS);
		}
	}

	@api
	@seealso({array_intersect.class})
	public static class array_merge extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "array_merge";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "array {array1, array2, [arrayN...]} Merges the specified arrays from left to right, and returns a"
					+ " new array. If the array merged is associative, it will overwrite the keys from left to right,"
					+ " but if the arrays are normal, the keys are ignored, and values are simply pushed.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInsufficientArgumentsException.class, CRECastException.class};
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
			CArray newArray = new CArray(t);
			if(args.length < 2) {
				throw new CREInsufficientArgumentsException("array_merge must be called with at least two parameters", t);
			}
			for(Mixed arg : args) {
				if(arg.isInstanceOf(ArrayAccess.TYPE)) {
					com.laytonsmith.core.natives.interfaces.Iterable cur
							= (com.laytonsmith.core.natives.interfaces.Iterable) arg;
					if(!cur.isAssociative()) {
						for(int j = 0; j < cur.size(); j++) {
							newArray.push(cur.get(j, t), t);
						}
					} else {
						for(Mixed key : cur.keySet()) {
							if(key.isInstanceOf(CInt.TYPE)) {
								newArray.set(key, cur.get((int) ((CInt) key).getInt(), t), t);
							} else {
								newArray.set(key, cur.get(key.val(), t), t);
							}
						}
					}
				} else {
					throw new CRECastException("All arguments to array_merge must be arrays", t);
				}
			}
			return newArray;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "array_merge(array(1), array(2), array(3))"),
				new ExampleScript("With associative arrays", "array_merge(array(one: 1), array(two: 2), array(three: 3))"),
				new ExampleScript("With overwrites", "array_merge(array(one: 1), array(one: 2), array(one: 3))")};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.NO_SIDE_EFFECTS);
		}
	}

	@api
	public static class array_remove extends AbstractFunction {

		@Override
		public String getName() {
			return "array_remove";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "mixed {array, index} Removes an index from an array. If the array is a normal"
					+ " array, all values' indices are shifted left one. If the array is associative,"
					+ " the index is simply removed. If the index exists, the value removed is returned."
					+ " If the index doesn't exist, the array remains unchanged,"
					+ " however it'll throw a RangeException for normal arrays (returns null for associative arrays).";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRERangeException.class, CRECastException.class, CREPluginInternalException.class};
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
			CArray array = Static.getArray(args[0], t);
			if(array.isAssociative()) {
				return array.remove(args[1]);
			} else {
				int index = Static.getInt32(args[1], t);
				Mixed removed = array.remove(args[1]);
				//If the removed index is <= the current index, we need to decrement the counter.
				for(Iterator iterator : environment.getEnv(GlobalEnv.class).GetArrayAccessIteratorsFor(array)) {
					if(index <= iterator.getCurrent()) {
						iterator.decrementCurrent();
					}
				}
				return removed;
			}
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage",
				"assign(@array, array(1, 2, 3))\nmsg(array_remove(@array, 2))\nmsg(@array)"),
				new ExampleScript("With associative array",
				"assign(@array, array(one: 'a', two: 'b', three: 'c'))\nmsg(array_remove(@array, 'two'))\nmsg(@array)")};
		}
	}

	@api
	@seealso({StringHandling.split.class, Regex.reg_split.class, map_implode.class})
	public static class array_implode extends AbstractFunction {

		@Override
		public String getName() {
			return "array_implode";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "string {array, [glue]} Given an array and glue, to-strings all the elements in the array"
					+ " (just the values, not the keys), and joins them with the glue, defaulting to a space."
					+ " For instance array_implode(array(1, 2, 3), '-') will return \"1-2-3\".";
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
			if(!(args[0].isInstanceOf(ArrayAccess.TYPE))) {
				throw new CRECastException("Expecting argument 1 to be an ArrayAccess type object", t);
			}
			StringBuilder b = new StringBuilder();
			ArrayAccess ca = (ArrayAccess) args[0];
			String glue = " ";
			if(args.length == 2) {
				glue = Static.getPrimitive(args[1], t).val();
			}
			boolean first = true;
			for(Mixed key : ca.keySet()) {
				Mixed value = ca.get(key, t);
				if(!first) {
					b.append(glue).append(value.val());
				} else {
					b.append(value.val());
					first = false;
				}
			}
			return new CString(b.toString(), t);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "array_implode(array(1, 2, 3), '-')"),
				new ExampleScript("With associative array", "array_implode(array(one: 'a', two: 'b', three: 'c'), '-')")};
		}
	}

	@api
	@seealso({array_implode.class})
	public static class map_implode extends AbstractFunction {

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
			CArray array = ArgumentValidation.getArray(args[0], t);
			String innerGlue = ArgumentValidation.getString(args[1], t);
			String outerGlue = ArgumentValidation.getString(args[2], t);
			if(!array.isAssociative()) {
				throw new CRECastException("Expecting an associative array, but a normal array was provided.", t);
			}

			StringBuilder b = new StringBuilder();

			boolean first = true;
			for(Mixed key : array.keySet()) {
				Mixed value = array.get(key, t);
				if(!first) {
					b.append(outerGlue);
				}
				first = false;
				b.append(key.val()).append(innerGlue).append(value.val());
			}

			return new CString(b.toString(), t);
		}

		@Override
		public String getName() {
			return "map_implode";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{3};
		}

		@Override
		public String docs() {
			return "string {associativeArray, innerGlue, outerGlue} Implodes an associative array. The innerGlue is"
					+ " used to glue the key to the value, and then the outerGlue is used to glue those elements"
					+ " together. This only works with associative arrays, and will throw CastException if the array"
					+ " passed in isa  normal array.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Single element", "map_implode(array('a': 'b'), '=', '&')"),
				new ExampleScript("Multiple elements", "map_implode(array('a': '1', 'b': '2'), '=', '&')"),
			};
		}



	}

	@api
	public static class cslice extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "cslice";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "slice {from, to} Dynamically creates an array slice, which can be used with array_get"
					+ " (or the [bracket notation]) to get a range of elements. cslice(0, 5) is equivalent"
					+ " to 0..5 directly in code, however with this function you can also do cslice(@var, @var),"
					+ " or other more complex expressions, which are not possible in static code.";
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
			return new CSlice(Static.getInt(args[0], t), Static.getInt(args[1], t), t);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "array(1, 2, 3)[cslice(0, 1)]")};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.NO_SIDE_EFFECTS);
		}

	}

	@api
	public static class array_sort extends AbstractFunction implements Optimizable {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class};
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
			if(!(args[0].isInstanceOf(CArray.TYPE))) {
				throw new CRECastException("The first parameter to array_sort must be an array", t);
			}
			CArray ca = (CArray) args[0];
			CArray.ArraySortType sortType = CArray.ArraySortType.REGULAR;
			CClosure customSort = null;
			if(ca.size() <= 1) {
				return ca;
			}
			try {
				if(args.length == 2) {
					if(args[1].isInstanceOf(CClosure.TYPE)) {
						sortType = null;
						customSort = (CClosure) args[1];
					} else {
						sortType = ArgumentValidation.getEnum(args[1], CArray.ArraySortType.class, t);
					}
				}
			} catch (IllegalArgumentException e) {
				throw new CREFormatException("The sort type must be one of either: " + StringUtils.Join(CArray.ArraySortType.values(), ", ", " or "), t);
			}
			if(sortType == null) {
				// It's a custom sort, which we have implemented below.
				if(ca.isAssociative()) {
					throw new CRECastException("Associative arrays may not be sorted using a custom comparator.", t);
				}
				CArray sorted = customSort(ca, customSort, t);
				//Clear it out and re-apply the values, so this is in place.
				ca.clear();
				for(Mixed c : sorted.keySet()) {
					ca.set(c, sorted.get(c, t), t);
				}
			} else {
				ca.sort(sortType);
			}
			return ca;
		}

		private CArray customSort(CArray ca, CClosure closure, Target t) {
			if(ca.size() <= 1) {
				return ca;
			}

			CArray left = new CArray(t);
			CArray right = new CArray(t);
			int middle = (int) (ca.size() / 2);
			for(int i = 0; i < middle; i++) {
				left.push(ca.get(i, t), t);
			}
			for(int i = middle; i < ca.size(); i++) {
				right.push(ca.get(i, t), t);
			}

			left = customSort(left, closure, t);
			right = customSort(right, closure, t);

			return merge(left, right, closure, t);
		}

		private CArray merge(CArray left, CArray right, CClosure closure, Target t) {
			CArray result = new CArray(t);
			while(left.size() > 0 || right.size() > 0) {
				if(left.size() > 0 && right.size() > 0) {
					// Compare the first two elements of each side
					Mixed l = left.get(0, t);
					Mixed r = right.get(0, t);
					Mixed c = closure.executeCallable(null, t, l, r);
					int value;
					if(c instanceof CNull) {
						value = 0;
					} else if(c.isInstanceOf(CBoolean.TYPE)) {
						if(((CBoolean) c).getBoolean()) {
							value = 1;
						} else {
							value = -1;
						}
					} else {
						throw new CRECastException("The custom closure did not return a value (or returned an invalid"
								+ " type). It must always return true, false, or null.", t);
					}
					if(value <= 0) {
						result.push(left.get(0, t), t);
						left.remove(0);
					} else {
						result.push(right.get(0, t), t);
						right.remove(0);
					}
				} else if(left.size() > 0) {
					result.push(left.get(0, t), t);
					left.remove(0);
				} else if(right.size() > 0) {
					result.push(right.get(0, t), t);
					right.remove(0);
				}
			}
			return result;
		}

		@Override
		public String getName() {
			return "array_sort";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "array {array, [sortType]} Sorts an array in place, and also returns a reference to the array. ----"
					+ " The complexity of this sort algorithm is guaranteed to be no worse than n log n, as it uses"
					+ " merge sort. The array is sorted in place, a new array is not explicitly created, so if you sort"
					+ " an array that is passed in as a variable, the contents of that variable will be sorted, even if"
					+ " you don't re-assign the returned array back to the variable. If you really need the old array,"
					+ " you should create a copy of the array first, like so: assign(@sorted, array_sort(@array[]))."
					+ " The sort type may be one of the following: " + StringUtils.Join(CArray.ArraySortType.values(), ", ", " or ")
					+ ", or it may be a closure, if the sort should follow custom rules (explained below). A regular"
					+ " sort sorts the elements without changing types first. A numeric sort always converts numeric"
					+ " values to numbers first (so 001 becomes 1). A string sort compares values as strings, and a"
					+ " string_ic sort is the same as a string sort, but the comparision is case-insensitive. If the"
					+ " array contains array values, a CastException is thrown; inner arrays cannot be sorted against"
					+ " each other. If the array is associative, a warning will be raised if the General logging"
					+ " channel is set to verbose, because the array's keys will all be lost in the process. To avoid"
					+ " this warning, and to be more explicit, you can use array_normalize() to normalize the array"
					+ " first. Note that the reason this function is an in place sort instead of explicitly cloning the"
					+ " array is because in most cases, you may not need to actually clone the array, an expensive"
					+ " operation. Due to this, it has slightly different behavior than array_normalize, which could"
					+ " have also been implemented in place.\n\nIf the sortType is a closure, it will perform a"
					+ " custom sort type, and the array may contain any values, including sub array values. The closure"
					+ " should accept two values, @left and @right, and should return true if the left value is larger"
					+ " than the right, and false if the left value is smaller than the right, and null if they are"
					+ " equal. The array will then be re-ordered using a merge sort, using your custom comparator to"
					+ " determine the sort order.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.OPTIMIZE_DYNAMIC
			);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs, List<ParseTree> children,
				FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() == 2) {
				if(!Construct.IsDynamicHelper(children.get(1).getData())) {
					try {
						CArray.ArraySortType.valueOf(children.get(1).getData().val().toUpperCase());
					} catch (IllegalArgumentException e) {
						throw new ConfigCompileException("The sort type must be one of either: " + StringUtils.Join(CArray.ArraySortType.values(), ", ", " or "), t);
					}
				}
			}
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Regular sort",
				"@array = array('a', 2, 4, 'string');\narray_sort(@array, 'REGULAR');\nmsg(@array);"),
				new ExampleScript("Numeric sort",
				"@array = array('03', '02', '4', '1');\narray_sort(@array, 'NUMERIC');\nmsg(@array);"),
				new ExampleScript("String sort",
				"@array = array('03', '02', '4', '1');\narray_sort(@array, 'STRING');\nmsg(@array);"),
				new ExampleScript("String sort (with words)",
				"@array = array('Zeta', 'zebra', 'Minecraft', 'mojang', 'Appliance', 'apple');\narray_sort(@array, 'STRING');\nmsg(@array);"),
				new ExampleScript("Ignore case sort",
				"@array = array('Zeta', 'zebra', 'Minecraft', 'mojang', 'Appliance', 'apple');\narray_sort(@array, 'STRING_IC');\nmsg(@array);"),
				new ExampleScript("Custom sort", "@array = array(\n"
				+ "\tarray(name: 'Jack', age: 20),\n"
				+ "\tarray(name: 'Jill', age: 19)\n"
				+ ");\n"
				+ "msg(\"Before sort: @array\");\n"
				+ "array_sort(@array, closure(@left, @right){\n"
				+ "\t return(@left['age'] > @right['age']);\n"
				+ "});\n"
				+ "msg(\"After sort: @array\");")
			};
		}
	}

	@api
	public static class array_sort_async extends AbstractFunction {

		RunnableQueue queue = new RunnableQueue("MethodScript-arraySortAsync");
		boolean started = false;

		private void startup() {
			if(!started) {
				queue.invokeLater(null, new Runnable() {

					@Override
					public void run() {
						//This warms up the queue. Apparently.
					}
				});
				StaticLayer.GetConvertor().addShutdownHook(new Runnable() {

					@Override
					public void run() {
						queue.shutdown();
						started = false;
					}
				});
				started = true;
			}
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
			startup();
			final CArray array = Static.getArray(args[0], t);
			final CString sortType = new CString(args.length > 2 ? args[1].val() : CArray.ArraySortType.REGULAR.name(), t);
			final CClosure callback = Static.getObject((args.length == 2 ? args[1] : args[2]), t, CClosure.class);
			queue.invokeLater(environment.getEnv(GlobalEnv.class).GetDaemonManager(), new Runnable() {

				@Override
				public void run() {
					Mixed c = new array_sort().exec(Target.UNKNOWN, null, array, sortType);
					callback.executeCallable(environment, t, new Mixed[]{c});
				}
			});
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "array_sort_async";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {array, [sortType], closure(array)} Works like array_sort, but does the sort on another"
					+ " thread, then calls the closure and sends it the sorted array. This is useful if the array"
					+ " is large enough to actually \"stall\" the server when doing the sort. Sort type should be"
					+ " one of " + StringUtils.Join(CArray.ArraySortType.values(), ", ", " or ");
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	public static class array_remove_values extends AbstractFunction {

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
			CArray array = Static.getArray(args[0], t);
			//This needs to be in terms of array_remove, to ensure that the iteration
			//logic is followed. We will iterate backwards, however, to make the
			//process more efficient, unless this is an associative array.
			if(array.isAssociative()) {
				array.removeValues(args[1]);
			} else {
				for(long i = array.size() - 1; i >= 0; i--) {
					if(BasicLogic.equals.doEquals(array.get(i, t), args[1])) {
						new array_remove().exec(t, environment, array, new CInt(i, t));
					}
				}
			}

			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "array_remove_values";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {array, value} Removes all instances of value from the specified array."
					+ " For instance, array_remove_values(array(1, 2, 2, 3), 2) would produce the"
					+ " array(1, 3). Note that it returns void however, so it will simply in place"
					+ " modify the array passed in, much like array_remove.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage",
				"assign(@array, array(1, 2, 2, 3))\nmsg(@array)\narray_remove_values(@array, 2)\nmsg(@array)")};
		}

	}

	@api
	public static class array_indexes extends AbstractFunction implements Optimizable {

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
			if(!(args[0].isInstanceOf(CArray.TYPE))) {
				throw new CRECastException("Expected parameter 1 to be an array, but was " + args[0].val(), t);
			}
			return ((CArray) args[0]).indexesOf(args[1]);
		}

		@Override
		public String getName() {
			return "array_indexes";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "array {array, value} Returns an array with all the keys of the specified array"
					+ " at which the specified value is equal. That is, for the array(1, 2, 2, 3), if"
					+ " value were 2, would return array(1, 2). If the value cannot be found in the"
					+ " array at all, an empty array will be returned.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "assign(@array, array(1, 2, 2, 3))\nmsg(array_indexes(@array, 2))"),
				new ExampleScript("Not found", "assign(@array, array(1, 2, 2, 3))\nmsg(array_indexes(@array, 5))")};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.NO_SIDE_EFFECTS);
		}

	}

	@api
	public static class array_index extends AbstractFunction {

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
			CArray ca = (CArray) new array_indexes().exec(t, environment, args);
			if(ca.isEmpty()) {
				return CNull.NULL;
			} else {
				return ca.get(0, t);
			}
		}

		@Override
		public String getName() {
			return "array_index";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "mixed {array, value} Works exactly like array_indexes(array, value)[0], except in the case where"
					+ " the value is not found, returns null. That is to say, if the value is contained in an"
					+ " array (even multiple times) the index of the first element is returned.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "assign(@array, array(1, 2, 2, 3))\nmsg(array_index(@array, 2))"),
				new ExampleScript("Not found", "assign(@array, array(1, 2, 2, 3))\nmsg(array_index(@array, 5))")};
		}

	}

	@api
	public static class array_last_index extends AbstractFunction {

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
			CArray ca = (CArray) new array_indexes().exec(t, environment, args);
			if(ca.isEmpty()) {
				return CNull.NULL;
			} else {
				return ca.get(ca.size() - 1, t);
			}
		}

		@Override
		public String getName() {
			return "array_last_index";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "mixed {array, value} Finds the index in the array where value occurs last. If"
					+ " the value is not found, returns null. That is to say, if the value is contained in an"
					+ " array (even multiple times) the index of the last element is returned.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "assign(@array, array(1, 2, 2, 3))\nmsg(array_last_index(@array, 2))"),
				new ExampleScript("Not found", "assign(@array, array(1, 2, 2, 3))\nmsg(array_last_index(@array, 5))")};
		}

	}

	@api
	public static class array_reverse extends AbstractFunction {

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
			if(args[0].isInstanceOf(CArray.TYPE)) {
				((CArray) args[0]).reverse(t);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "array_reverse";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {array} Reverses an array in place. However, if the array is associative, throws a"
					+ " CastException, since associative arrays are more like a map.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "assign(@array, array(1, 2, 3))\nmsg(@array)\narray_reverse(@array)\nmsg(@array)"),
				new ExampleScript("Failure", "assign(@array, array(one: 1, two: 2))\narray_reverse(@array)")
			};
		}

	}

	@api
	public static class array_rand extends AbstractFunction implements Optimizable {

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

		Random r = new Random(System.currentTimeMillis());

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			long number = 1;
			boolean getKeys = true;
			CArray array = Static.getArray(args[0], t);
			CArray newArray = new CArray(t);
			if(array.isEmpty()) {
				return newArray;
			}
			if(args.length > 1) {
				number = Static.getInt(args[1], t);
			}
			if(number < 1) {
				throw new CRERangeException("number may not be less than 1.", t);
			}
			if(number > array.size()) {
				throw new CRERangeException("Number cannot be larger than array size", t);
			}
			if(args.length > 2) {
				getKeys = ArgumentValidation.getBoolean(args[2], t);
			}

			LinkedHashSet<Integer> randoms = new LinkedHashSet<>();
			while(randoms.size() < number) {
				randoms.add(java.lang.Math.abs(r.nextInt() % (int) array.size()));
			}
			List<Mixed> keySet = new ArrayList<>(array.keySet());
			for(Integer i : randoms) {
				if(getKeys) {
					newArray.push(keySet.get(i), t);
				} else {
					newArray.push(array.get(keySet.get(i), t), t);
				}
			}
			return newArray;
		}

		@Override
		public String getName() {
			return "array_rand";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		@Override
		public String docs() {
			return "array {array, [number, [getKeys]]} Returns a random selection of keys or values from an array."
					+ " The array may be either normal or associative. Number defaults to 1, and getKey defaults to true."
					+ " If number is greater than the size of the array, a RangeException is thrown. No value will be"
					+ " returned twice from the array however, one it is \"drawn\" from the array, it is not placed"
					+ " back in. The order of the elements in the array will also be random, if order is important,"
					+ " use array_sort().";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Usage with a normal array",
				"assign(@array, array('a', 'b', 'c', 'd', 'e'))\nmsg(array_rand(@array))", "{1}"),
				new ExampleScript("Usage with a normal array, using getKeys false, and returning 2 results",
				"assign(@array, array('a', 'b', 'c', 'd', 'e'))\nmsg(array_rand(@array, 2, false))", "{b, c}"),
				new ExampleScript("Usage with an associative array",
				"assign(@array, array(one: 'a', two: 'b', three: 'c', four: 'd', five: 'e'))\nmsg(array_rand(@array))", "two")};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.NO_SIDE_EFFECTS);
		}
	}

	@api
	public static class array_unique extends AbstractFunction implements Optimizable {

		// Variable is more clear when named after the function it represents.
		@SuppressWarnings("checkstyle:constantname")
		private static final equals equals = new equals();

		// Variable is more clear when named after the function it represents.
		@SuppressWarnings("checkstyle:constantname")
		private static final BasicLogic.sequals sequals = new BasicLogic.sequals();

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
		public CArray exec(final Target t, final Environment environment, Mixed... args) throws ConfigRuntimeException {
			CArray array = Static.getArray(args[0], t);
			boolean compareTypes = true;
			if(args.length == 2) {
				compareTypes = ArgumentValidation.getBoolean(args[1], t);
			}
			final boolean fCompareTypes = compareTypes;
			if(array.inAssociativeMode()) {
				return array.clone();
			} else {
				List<Mixed> asList = array.asList();
				CArray newArray = new CArray(t);
				Set<Mixed> set = new LinkedComparatorSet<>(asList, new LinkedComparatorSet.EqualsComparator<Mixed>() {

					@Override
					public boolean checkIfEquals(Mixed item1, Mixed item2) {
						return (fCompareTypes && ArgumentValidation.getBoolean(sequals.exec(t, environment, item1, item2), t))
								|| (!fCompareTypes && ArgumentValidation.getBoolean(equals.exec(t, environment, item1, item2), t));
					}
				});
				for(Mixed c : set) {
					newArray.push(c, t);
				}
				return newArray;
			}
		}

		@Override
		public String getName() {
			return "array_unique";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "array {array, [compareTypes]} Removes all non-unique values from an array. ----"
					+ " compareTypes is true by default, which means that in the array array(1, '1'), nothing would be"
					+ " removed from the array, since both values are different data types. However, if compareTypes is"
					+ " false, then the first value would remain, but the second value would be removed. A new array is"
					+ " returned. If the array is associative, by definition, there are no unique values, so a clone of"
					+ " the array is returned.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "array_unique(array(1, 2, 2, 3, 4))"),
				new ExampleScript("No removal of different datatypes", "array_unique(array(1, '1'))"),
				new ExampleScript("Removal of different datatypes, by setting compareTypes to false",
				"array_unique(array(1, '1'), false)")};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.NO_SIDE_EFFECTS);
		}

	}

	@api
	public static class array_filter extends AbstractFunction {

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
			com.laytonsmith.core.natives.interfaces.Iterable array;
			CClosure closure;
			if(!(args[0] instanceof com.laytonsmith.core.natives.interfaces.Iterable)) {
				throw new CRECastException("Expecting an array for argument 1", t);
			}
			if(!(args[1].isInstanceOf(CClosure.TYPE))) {
				throw new CRECastException("Expecting a closure for argument 2", t);
			}
			array = (com.laytonsmith.core.natives.interfaces.Iterable) args[0];
			closure = (CClosure) args[1];
			CArray newArray;
			if(array.isAssociative()) {
				newArray = CArray.GetAssociativeArray(t);
				for(Mixed key : array.keySet()) {
					Mixed value = array.get(key, t);
					Mixed ret = closure.executeCallable(environment, t, key, value);
					boolean bret = ArgumentValidation.getBooleanish(ret, t);
					if(bret) {
						newArray.set(key, value, t);
					}
				}
			} else {
				newArray = new CArray(t);
				for(int i = 0; i < array.size(); i++) {
					Mixed key = new CInt(i, t);
					Mixed value = array.get(i, t);
					Mixed ret = closure.executeCallable(environment, t, key, value);
					if(ret == CNull.NULL) {
						ret = CBoolean.FALSE;
					}
					boolean bret = ArgumentValidation.getBooleanish(ret, t);
					if(bret) {
						newArray.push(value, t);
					}
				}
			}
			return newArray;
		}

		@Override
		public String getName() {
			return "array_filter";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "array {array, boolean closure(key, value)} Filters an array by callback. The items in the array are"
					+ " iterated over, each one sent to the closure one at a time, as key, value. The closure should"
					+ " return true if the item should be included in the array, or false if not. The filtered array is"
					+ " then returned by the function. If the array is associative, the keys will continue to map to"
					+ " the same values, however a normal array, the values are simply pushed onto the new array, and"
					+ " won't correspond to the same values per se.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Pulls out only the odd numbers", "@array = array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);\n"
				+ "@newArray = array_filter(@array, closure(@key, @value){\n"
				+ "\treturn(@value % 2 == 1);\n"
				+ "});\n"
				+ "msg(@newArray);\n"),
				new ExampleScript("Pulls out only the odd numbers in an associative array",
				"@array = array('one': 1, 'two': 2, 'three': 3, 'four': 4);\n"
				+ "@newArray = array_filter(@array, closure(@key, @value){\n"
				+ "\treturn(@value % 2 == 1);\n"
				+ "});\n"
				+ "msg(@newArray);\n")
			};
		}

	}

	@api
	public static class array_deep_clone extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREInsufficientArgumentsException.class};
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
			if(args.length != 1) {
				throw new CREInsufficientArgumentsException("Expecting exactly one argument", t);
			}
			if(!(args[0].isInstanceOf(CArray.TYPE))) {
				throw new CRECastException("Expecting argument 1 to be an array", t);
			}
			return ((CArray) args[0]).deepClone(t);
		}

		@Override
		public String getName() {
			return "array_deep_clone";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "array {array} Performs a deep clone on an array (as opposed to a shallow clone). This is useful"
					+ " for multidimensional arrays. See the examples for more info.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Demonstrates that the array is cloned.",
				"@array = array(1, 2, 3, 4)\n"
				+ "@deepClone = array_deep_clone(@array)\n"
				+ "@deepClone[1] = 'newValue'\n"
				+ "msg(@array)\nmsg(@deepClone)"),
				new ExampleScript("Demonstrated that arrays within the array are also cloned by a deep clone.",
				"@array = array(array('value'))\n"
				+ "@deepClone = array_deep_clone(@array)\n"
				+ "@deepClone[0][0] = 'newValue'\n"
				+ "msg(@array)\nmsg(@deepClone)")
			};
		}

	}

	@api
	public static class array_shallow_clone extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREInsufficientArgumentsException.class};
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
			if(args.length != 1) {
				throw new CREInsufficientArgumentsException("Expecting exactly one argument", t);
			}
			if(!(args[0].isInstanceOf(CArray.TYPE))) {
				throw new CRECastException("Expecting argument 1 to be an array", t);
			}
			CArray array = (CArray) args[0];
			CArray shallowClone = (array.isAssociative() ? CArray.GetAssociativeArray(t) : new CArray(t));
			for(Mixed key : array.keySet()) {
				shallowClone.set(key, array.get(key, t), t);
			}
			return shallowClone;
		}

		@Override
		public String getName() {
			return "array_shallow_clone";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "array {array} Performs a shallow clone on an array (as opposed to a deep clone)."
					+ " See the examples for more info.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Demonstrates that the array is cloned.",
				"@array = array(1, 2, 3, 4)\n"
				+ "@shallowClone = array_shallow_clone(@array)\n"
				+ "@shallowClone[1] = 'newValue'\n"
				+ "msg(@array)\nmsg(@shallowClone)"),
				new ExampleScript("Demonstrated that arrays within the array are not cloned by a shallow clone.",
				"@array = array(array('value'))\n"
				+ "@shallowClone = array_shallow_clone(@array)\n"
				+ "@shallowClone[0][0] = 'newValue'\n"
				+ "msg(@array)\nmsg(@shallowClone)")
			};
		}

	}

	@api
	public static class array_iterate extends AbstractFunction {

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
			CArray array = Static.getArray(args[0], t);
			CClosure closure = Static.getObject(args[1], t, CClosure.class);
			for(Mixed key : array.keySet()) {
				try {
					closure.executeCallable(environment, t, key, array.get(key, t));
				} catch (ProgramFlowManipulationException ex) {
					// Ignored
				}
			}
			return array;
		}

		@Override
		public String getName() {
			return "array_iterate";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "array {array, closure} Iterates across an array, calling the closure for each value of the array."
					+ " The closure should accept two arguments, the key and the value. This method can be used in some"
					+ " code to increase readability, to increase re-usability, or keep variables created in a loop in"
					+ " an isolated scope. Note that this runs at approximately the same speed as a for loop, which is"
					+ " probably slower than a foreach loop. Any values returned from the closure are silently ignored."
					+ " Returns a reference to the original array.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic use with normal arrays", "@array = array(1, 2, 3);\n"
				+ "array_iterate(@array, closure(@key, @value){\n"
				+ "\tmsg(@value);\n"
				+ "});"),
				new ExampleScript("Use with associative arrays", "@array = array(one: 1, two: 2, three: 3);\n"
				+ "array_iterate(@array, closure(@key, @value){\n"
				+ "\tmsg(\"@key: @value\");\n"
				+ "});")
			};
		}

	}

	@api
	public static class array_reduce extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREIllegalArgumentException.class};
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
			CArray array = Static.getArray(args[0], t);
			CClosure closure = Static.getObject(args[1], t, CClosure.class);
			if(array.isEmpty()) {
				return CNull.NULL;
			}
			if(array.size() == 1) {
				// This line looks bad, but all it does is return the first (and since we know only) value in the array,
				// whether or not it is associative or normal.
				return array.get(array.keySet().toArray(new Mixed[0])[0], t);
			}
			List<Mixed> keys = new ArrayList<>(array.keySet());
			Mixed lastValue = array.get(keys.get(0), t);
			for(int i = 1; i < keys.size(); ++i) {
				lastValue = closure.executeCallable(environment, t, lastValue, array.get(keys.get(i), t));
				if(lastValue instanceof CVoid) {
					throw new CREIllegalArgumentException("The closure passed to " + getName() + " cannot return void.", t);
				}
			}
			return lastValue;
		}

		@Override
		public String getName() {
			return "array_reduce";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "mixed {array, closure} Reduces an array to a single value. This is useful for, for instance,"
					+ " summing the values of an array. The previously calculated value, then the next value of the"
					+ " array are sent to the closure, which is expected to return a value, based on the two values,"
					+ " which will be sent again to the closure as the new calculated value. If the array is empty,"
					+ " null is returned, and if the array has exactly one value in it, only that value is returned."
					+ " Associative arrays are supported, but the order is based on the key order, which may not be as"
					+ " expected. The keys of the array are ignored.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Summing the values of an array", "@array = array(1, 2, 4, 8);\n"
				+ "@sum = array_reduce(@array, closure(@soFar, @next){\n"
				+ "\treturn(@soFar + @next);\n"
				+ "});\n"
				+ "msg(@sum);"),
				new ExampleScript("Combining the strings in an array", "@array = array('a', 'b', 'c');\n"
				+ "@string = array_reduce(@array, closure(@soFar, @next){\n"
				+ "\treturn(@soFar . @next);\n"
				+ "});\n"
				+ "msg(@string);")
			};
		}

	}

	@api
	public static class array_reduce_right extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREIllegalArgumentException.class};
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
			CArray array = Static.getArray(args[0], t);
			CClosure closure = Static.getObject(args[1], t, CClosure.class);
			if(array.isEmpty()) {
				return CNull.NULL;
			}
			if(array.size() == 1) {
				// This line looks bad, but all it does is return the first (and since we know only) value in the array,
				// whether or not it is associative or normal.
				return array.get(array.keySet().toArray(new Mixed[0])[0], t);
			}
			List<Mixed> keys = new ArrayList<>(array.keySet());
			Mixed lastValue = array.get(keys.get(keys.size() - 1), t);
			for(int i = keys.size() - 2; i >= 0; --i) {
				lastValue = closure.executeCallable(environment, t, lastValue, array.get(keys.get(i), t));
				if(lastValue instanceof CVoid) {
					throw new CREIllegalArgumentException("The closure passed to " + getName() + " cannot return void.", t);
				}
			}
			return lastValue;
		}

		@Override
		public String getName() {
			return "array_reduce_right";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "mixed {array, closure} Reduces an array to a single value. This works in reverse of array_reduce()."
					+ " This is useful for, for instance, summing the values of an array. The previously calculated"
					+ " value, then the previous value of the array are sent to the closure, which is expected to"
					+ " return a value, based on the two values, which will be sent again to the closure as the new"
					+ " calculated value. If the array is empty, null is returned, and if the array has exactly one"
					+ " value in it, only that value is returned. Associative arrays are supported, but the order is"
					+ " based on the key order, which may not be as expected. The keys of the array are ignored.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Summing the values of an array", "@array = array(1, 2, 4, 8);\n"
				+ "@sum = array_reduce_right(@array, closure(@soFar, @next){\n"
				+ "\treturn(@soFar + @next);\n"
				+ "});\n"
				+ "msg(@sum);"),
				new ExampleScript("Combining the strings in an array", "@array = array('a', 'b', 'c');\n"
				+ "@string = array_reduce_right(@array, closure(@soFar, @next){\n"
				+ "\treturn(@soFar . @next);\n"
				+ "});\n"
				+ "msg(@string);")
			};
		}

	}

	@api
	public static class array_every extends AbstractFunction {

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
			CArray array = Static.getArray(args[0], t);
			CClosure closure = Static.getObject(args[1], t, CClosure.class);
			for(Mixed c : array.keySet()) {
				Mixed fr = closure.executeCallable(environment, t, array.get(c, t));
				boolean ret = ArgumentValidation.getBooleanish(fr, t);
				if(ret == false) {
					return CBoolean.FALSE;
				}
			}
			return CBoolean.TRUE;
		}

		@Override
		public String getName() {
			return "array_every";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {array, closure} Returns true if every value in the array meets some test, which the closure"
					+ " should return true or false about. Not all values will necessarily be checked, once a value is"
					+ " determined to fail the check, execution is stopped, and false is returned. The closure will be"
					+ " passed each value in the array, one at a time, and must return a boolean.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "@array = array(1, 3, 5);\n"
				+ "@arrayIsAllOdds = array_every(@array, closure(@value){\n"
				+ "\treturn(@value % 2 == 1);\n"
				+ "});\n"
				+ "msg(@arrayIsAllOdds);"),
				new ExampleScript("Basic usage, with false condition", "@array = array(1, 3, 4);\n"
				+ "@arrayIsAllOdds = array_every(@array, closure(@value){\n"
				+ "\treturn(@value % 2 == 1);\n"
				+ "});\n"
				+ "msg(@arrayIsAllOdds);")
			};
		}

	}

	@api
	public static class array_some extends AbstractFunction {

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
			CArray array = Static.getArray(args[0], t);
			CClosure closure = Static.getObject(args[1], t, CClosure.class);
			for(Mixed c : array.keySet()) {
				Mixed fr = closure.executeCallable(environment, t, array.get(c, t));
				boolean ret = ArgumentValidation.getBooleanish(fr, t);
				if(ret == true) {
					return CBoolean.TRUE;
				}
			}
			return CBoolean.FALSE;
		}

		@Override
		public String getName() {
			return "array_some";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {array, closure} Returns true if any value in the array meets some test, which the closure"
					+ " should return true or false about. Not all values will necessarily be checked, once a value is"
					+ " determined to pass the check, execution is stopped, and true is returned. The closure will be"
					+ " passed each value in the array, one at a time, and must return a boolean.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "@array = array(2, 4, 8);\n"
				+ "@arrayHasOdds = array_some(@array, closure(@value){\n"
				+ "\treturn(@value % 2 == 1);\n"
				+ "});\n"
				+ "msg(@arrayHasOdds);"),
				new ExampleScript("Basic usage, with true condition", "@array = array(2, 3, 4);\n"
				+ "@arrayHasOdds = array_some(@array, closure(@value){\n"
				+ "\treturn(@value % 2 == 1);\n"
				+ "});\n"
				+ "msg(@arrayHasOdds);")
			};
		}

	}

	@api
	public static class array_map extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREIllegalArgumentException.class};
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
			CArray array = Static.getArray(args[0], t);
			CClosure closure = Static.getObject(args[1], t, CClosure.class);
			CArray newArray = (array.isAssociative() ? CArray.GetAssociativeArray(t) : new CArray(t, (int) array.size()));

			for(Mixed c : array.keySet()) {
				Mixed fr = closure.executeCallable(environment, t, array.get(c, t));
				if(fr.isInstanceOf(CVoid.TYPE)) {
					throw new CREIllegalArgumentException("The closure passed to " + getName()
							+ " must return a value.", t);
				}
				newArray.set(c, fr, t);
			}

			return newArray;
		}

		@Override
		public String getName() {
			return "array_map";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "array {array, closure} Calls the closure on each element of an array,"
					+ " and returns an array that contains the results.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "@areaOfSquare = closure(@sideLength){\n"
				+ "\treturn(@sideLength ** 2);\n"
				+ "};\n"
				+ "// A collection of square sides\n"
				+ "@squares = array(1, 4, 8);\n"
				+ "@areas = array_map(@squares, @areaOfSquare);\n"
				+ "msg(@areas);"),
				new ExampleScript("Parsing a csv file with minimal code",
				"string @file = 'a, b, c\\nz, y, x\\n1, 2, 3\\n99, 98, 97'; // Could be a read()\n"
				+ "array @list = array_map(split('\\n', @file), closure(@line) {\n"
				+ "        return(split(',', @line));\n"
				+ "});\n"
				+ "msg(@list);")
			};
		}

	}

	@api
	@seealso({array_merge.class})
	public static class array_intersect extends AbstractFunction {

		@MEnum("ms.lang.ArrayIntersectComparisonMode")
		public static enum ArrayIntersectComparisonMode {
			EQUALS(new equals()),
			STRICT_EQUALS(new sequals()),
			HASH(null);

			private final Function comparisonFunction;
			private ArrayIntersectComparisonMode(Function f) {
				this.comparisonFunction = f;
			}

			public Function getComparisonFunction() {
				return comparisonFunction;
			}
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREIllegalArgumentException.class};
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
			CArray one = Static.getArray(args[0], t);
			CArray two = Static.getArray(args[1], t);
			CClosure closure = null;
			ArrayIntersectComparisonMode mode = ArrayIntersectComparisonMode.HASH;
			boolean associativeMode = one.isAssociative() || two.isAssociative();
			if(args.length > 2) {
				if(associativeMode) {
					throw new CREIllegalArgumentException("For associative arrays, only 2 parameters may be provided,"
							+ " the comparison mode value is not used.", t);
				}
				if(args[2].isInstanceOf(CClosure.TYPE)) {
					closure = Static.getObject(args[2], t, CClosure.class);
				} else {
					mode = ArgumentValidation.getEnum(args[2], ArrayIntersectComparisonMode.class, t);
				}
			}
			CArray ret = new CArray(t);

			if(!associativeMode && closure == null && mode == ArrayIntersectComparisonMode.HASH) {
				// Optimize for O(n log n) method
				Set<Integer> a2Set = new TreeSet<>();
				for(Mixed c : two) {
					a2Set.add(c.hashCode());
				}

				// Iterate one, and check if the hash of each value is in the set. If so, add it.
				for(Mixed c : one) {
					if(a2Set.contains(c.hashCode())) {
						ret.push(c, t);
					}
				}
			} else {
				Mixed[] k1 = new Mixed[(int) one.size()];
				Mixed[] k2 = new Mixed[(int) two.size()];
				one.keySet().toArray(k1);
				two.keySet().toArray(k2);
				equals equals = new equals();
				Function comparisonFunction = mode.getComparisonFunction();
				i: for(int i = 0; i < k1.length; i++) {
					for(int j = 0; j < k2.length; j++) {
						if(associativeMode) {
							if(equals.exec(t, environment, k1[i], k2[j]).getBoolean()) {
								ret.set(k1[i], one.get(k1[i], t), t);
								continue i;
							}
						} else {
							if(closure == null) {
								if(comparisonFunction != null) {
									if(ArgumentValidation.getBoolean(comparisonFunction.exec(t, environment,
											one.get(k1[i], t), two.get(k2[j], t)
									), t)) {
										ret.push(one.get(k1[i], t), t);
										continue i;
									}
								} else {
									throw new Error();
								}
							} else {
								Mixed fre = closure.executeCallable(environment, t, one.get(k1[i], t), two.get(k2[j], t));
								boolean res = ArgumentValidation.getBoolean(fre, fre.getTarget());
								if(res) {
									ret.push(one.get(k1[i], t), t);
									continue i;
								}
							}
						}
					}
				}
			}
			return ret;
		}

		@Override
		public String getName() {
			return "array_intersect";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "array {array1, array2, [comparisonMode]|array1, array2, comparisonClosure} Returns an array that is"
					+ " the intersection of the two provided arrays. If either"
					+ " array is associative, it puts the function in associative mode. For normal arrays, the values"
					+ " are compared, and for associative arrays, the keys are compared, but the values are taken from"
					+ " the left array. comparisonMode is only applicable for normal arrays, and defaults to HASH, but"
					+ " determines the mode in which the system decides"
					+ " if two values are equal or not. A closure may be sent"
					+ " instead, which should return true if the two values are considered equals or not. Using the HASH"
					+ " mode is fastest, as this puts the function in an optimizing mode, and it can run at O(n log n)."
					+ " Otherwise, the runtime is O(n**2). The results between HASH and STRICT_EQUALS should almost never"
					+ " be different, and so in that case using STRICT_EQUALS has a lower performance for no gain,"
					+ " but there may be some cases where using"
					+ " the hash code is not desirable. EQUALS is necessary if you wish to disregard typing, so that"
					+ " array(1, 2, 3) and array('1', '2', '3') are considered equal. Duplicate values in the left"
					+ " array are duplicated, but duplicates in the right are not.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_3;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Usage with associative array",
						"array_intersect(array(one: 1, five: 5), array(one: 1, three: 3))"),
				new ExampleScript("Usage with normal arrays. The default comparison method is HASH",
						"array_intersect(array(1, 2, 3), array(2, 3, 4))"),
				new ExampleScript("Demonstrates that STRICT_EQUALS does not consider different types to be equal",
						"array_intersect(array('1', '2', '3'), array(1, 2, 3), 'STRICT_EQUALS')"),
				new ExampleScript("Note that the results of this method are the same as the previous example,"
						+ " but this version would be faster, and is preferred in all but the most exceptional cases.",
						"array_intersect(array('1', '2', '3'), array(1, 2, 3), 'HASH')"),
				new ExampleScript("Demonstrates usage with equals. Note that '1' == 1 (but does not === 1) but since"
						+ " the comparison method uses equals, not sequals, these arrays are considered equivalent.",
						"array_intersect(array('1', '2', '3'), array(1, 2, 3), 'EQUALS')"),
				new ExampleScript("Usage with a custom closure", "array_intersect(\n"
						+ "\tarray(array(id: 1, qty: 2), array(id: 2, qty: 5)),\n"
						+ "\tarray(array(id: 1, qty: 2), array(id: 5, qty: 10)),\n"
						+ "\tclosure(@a, @b) {\n"
						+ "\t\treturn(@a['id'] == @b['id']);\n"
						+ "})"),
				new ExampleScript("The value is taken from the left array. This is not important for primitives, but"
						+ " when using arrays and a custom closure, it may make a difference.", "array_intersect(\n"
						+ "\tarray(array(id: 1, pos: 'left')),\n"
						+ "\tarray(array(id: 1, pos: 'right')),\n"
						+ "\tclosure(@a, @b) {\n"
						+ "\t\treturn(@a['id'] == @b['id']);\n"
						+ "})"),
				new ExampleScript("Demonstrates behavior with duplicate values", "msg(array_intersect(\n"
						+ "\tarray(1, 1, 1, 2, 3),\n"
						+ "\tarray(1, 2)));\n"
						+ "msg(array_intersect(\n"
						+ "\tarray(1, 2, 3),\n"
						+ "\tarray(1, 1, 1)));")
			};
		}


	}

	@api
	public static class array_subset_of extends AbstractFunction {

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public String getName() {
			return "array_subset_of";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIllegalArgumentException.class};
		}

		@Override
		public String docs() {
			return "boolean {array, array} "
					+ "Returns true if first array is a subset of second array.";
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
			Mixed constA = args[0];
			Mixed constB = args[1];
			if(!(constA.isInstanceOf(CArray.TYPE))) {
				throw new CREIllegalArgumentException("Expecting an array, but received " + constA, t);
			}
			if(!(constB.isInstanceOf(CArray.TYPE))) {
				throw new CREIllegalArgumentException("Expecting an array, but received " + constB, t);
			}
			return CBoolean.get(subsetOf(constA, constB, t));
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage",
				"@arrayA = array(0, 1)\n"
				+ "@arrayB = array(0, 1, 5, 9)\n"
				+ "array_subset_of(@arrayA, @arrayB)"),
				new ExampleScript("Basic usage",
				"@arrayA = array(0, 1)\n"
				+ "@arrayB = array(0, 2, 5, 9)\n"
				+ "array_subset_of(@arrayA, @arrayB)"),
				new ExampleScript("Mix array",
				"@arrayA = array(a: 1, b: array('one', 'two'))\n"
				+ "@arrayB = array(a: 1, b: array('one', 'two', 'three'), c: 3)\n"
				+ "array_subset_of(@arrayA, @arrayB)"),
				new ExampleScript("Mix array",
				"@arrayA = array(a: 1, b: array('one', 'two'))\n"
				+ "@arrayB = array(a: 1, b: array('two', 'one', 'three'), c: 3)\n"
				+ "array_subset_of(@arrayA, @arrayB)")
			};
		}

		public boolean subsetOf(Mixed constA, Mixed constB, Target t) {
			if(!constA.typeof().equals(constB.typeof())) {
				return false;
			}
			if(constA.isInstanceOf(CArray.TYPE)) {
				CArray arrA = (CArray) constA;
				CArray arrB = (CArray) constB;
				if(arrA.isAssociative() != arrB.isAssociative()) {
					return false;
				}
				if(arrA.isAssociative()) {
					for(String key : arrA.stringKeySet()) {
						if(!arrB.containsKey(key)) {
							return false;
						}
						Mixed eltA = arrA.get(key, t);
						Mixed eltB = arrB.get(key, t);
						if(!subsetOf(eltA, eltB, t)) {
							return false;
						}
					}
				} else {
					for(int i = 0; i < arrA.size(); i++) {
						if(!arrB.containsKey(i)) {
							return false;
						}
						Mixed eltA = arrA.get(i, t);
						Mixed eltB = arrB.get(i, t);
						if(!subsetOf(eltA, eltB, t)) {
							return false;
						}
					}
				}
			} else if(!equals.doEquals(constA, constB)) {
				return false;
			}
			return true;
		}
	}

	@api
	@seealso(array_push.class)
	public static class array_push_all extends CompositeFunction {

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
		public String getName() {
			return "array_push_all";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {array, values} Pushes all the values of an array individually. If you try to push an array"
					+ " onto array_push, this will give you a two dimensional array, this method pushes the sub values"
					+ " of the values array into the destination array.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		protected String script() {
			return getBundledCode();
		}

	}

	@api
	@seealso(array_rand.class)
	public static class array_get_rand extends AbstractFunction {

		@Override
		public String getName() {
			return "array_get_rand";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "mixed {array} Returns a random value from an array."
					+ " If the array is empty a LengthException is thrown.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRELengthException.class};
		}

		Random rand = new Random();

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			CArray array = Static.getArray(args[0], t);
			if(array.isEmpty()) {
				throw new CRELengthException("Array is empty", t);
			}
			List<Mixed> keySet = new ArrayList<>(array.keySet());
			return array.get(keySet.get(java.lang.Math.abs(rand.nextInt() % (int) array.size())), t);
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
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
					new ExampleScript("Normal array usage",
							"array_get_rand(array(1, 2, 3, 4, 5))"),
					new ExampleScript("Associative array usage",
							"array_get_rand(array(one: 1, two: 2, three: 3, four: 4, five: 5))"),
			};
		}
	}
}
