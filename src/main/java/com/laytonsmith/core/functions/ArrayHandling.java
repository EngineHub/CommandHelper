package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.LinkedComparatorSet;
import com.laytonsmith.PureUtilities.RunnableQueue;
import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.*;
import com.laytonsmith.core.arguments.ArgList;
import com.laytonsmith.core.arguments.Argument;
import com.laytonsmith.core.arguments.ArgumentBuilder;
import com.laytonsmith.core.arguments.Generic;
import com.laytonsmith.core.compiler.Optimizable;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.BasicLogic.equals;
import com.laytonsmith.core.functions.BasicLogic.equals_ic;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.annotations.NonNull;
import com.laytonsmith.core.natives.annotations.Ranged;
import com.laytonsmith.core.natives.interfaces.ArrayAccess;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author Layton
 */
public class ArrayHandling {

	public static String docs() {
		return "This class contains functions that provide a way to manipulate arrays. To create an array, use the <code>array</code> function."
				+ " For more detailed information on array usage, see the page on [[CommandHelper/Arrays|arrays]]";
	}

	@api
	public static class array_size extends AbstractFunction {

		public String getName() {
			return "array_size";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public CInt exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			return new CInt(((CArray) getBuilder().parse(args, this, t).get("array")).size(), t);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "Returns the size of this array as an integer.";
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
						new ExampleScript("Demonstrates usage", "array_size(array(1, 2, 3, 4, 5))"),};
		}

		public Argument returnType() {
			return new Argument("The size of the specified array", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(new Argument("", CArray.class, "array"));
		}
	}

	@api(environments = {GlobalEnv.class})
	public static class array_get extends AbstractFunction implements Optimizable {

		public String getName() {
			return "array_get";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			ArrayAccess array = list.get("array");
			Construct index = list.get("index");
			Construct defaultConstruct = list.get("default");

			if (env.getEnv(GlobalEnv.class).HasFlag("array_get_alt_mode")) {
				return new CArrayReference((Construct) array, index, env);
			}

			if (index instanceof CSlice) {
				CSlice slice = ((CSlice) index);
				return array.slice(slice.getStart(), slice.getFinish(), t);
			} else {
				if (array.containsKey(index.val())) {
					return array.get(index.val(), t);
				} else {
					return defaultConstruct;
				}
			}


//			if (array instanceof CArray) {
//				CArray ca = (CArray) args[0];
//				if (index instanceof CSlice) {
//					if (ca.inAssociativeMode()) {
//						if (((CSlice) index).getStart() == 0 && ((CSlice) index).getFinish() == -1) {
//							//Special exception, we want to clone the whole array
//							CArray na = CArray.GetAssociativeArray(t);
//							for (String key : ca.keySet()) {
//								try {
//									na.set(key, ca.get(key, t).clone(), t);
//								} catch (CloneNotSupportedException ex) {
//									na.set(key, ca.get(key, t), t);
//								}
//							}
//							return na;
//						}
//						throw new ConfigRuntimeException("Array slices are not allowed with an associative array", ExceptionType.CastException, t);
//					}
//					//It's a range
//					long start = ((CSlice) index).getStart();
//					long finish = ((CSlice) index).getFinish();
//					try {
//						//Convert negative indexes 
//						if (start < 0) {
//							start = ca.size() + start;
//						}
//						if (finish < 0) {
//							finish = ca.size() + finish;
//						}
//						CArray na = new CArray(t);
//						if (finish < start) {
//							//return an empty array in cases where the indexes don't make sense
//							return na;
//						}
//						for (long i = start; i <= finish; i++) {
//							try {
//								na.push(ca.get((int) i, t).clone());
//							} catch (CloneNotSupportedException e) {
//								na.push(ca.get((int) i, t));
//							}
//						}
//						return na;
//					} catch (NumberFormatException e) {
//						throw new ConfigRuntimeException("Ranges must be integer numbers, i.e., [0..5]", ExceptionType.CastException, t);
//					}
//				} else {
//					try {
//						if (!ca.inAssociativeMode()) {
//							long iindex = Static.getInt(args[1], t);
//							if (iindex < 0) {
//								//negative index, convert to positive index
//								iindex = ca.size() + iindex;
//							}
//							return ca.get(iindex, t);
//						} else {
//							return ca.get(args[1], t);
//						}
//					} catch (ConfigRuntimeException e) {
//						if (e.getExceptionType() == ExceptionType.IndexOverflowException) {
//							if(defaultConstruct != null){
//								return defaultConstruct;
//							}
//						}
//						throw e;
//					}
//				}
//			} else if (args[0] instanceof CString) {
//				if (index instanceof CSlice) {
//					ArrayAccess aa = (ArrayAccess) args[0];
//					//It's a range
//					long start = ((CSlice) index).getStart();
//					long finish = ((CSlice) index).getFinish();
//					try {
//						//Convert negative indexes 
//						if (start < 0) {
//							start = aa.toString().length() + start;
//						}
//						if (finish < 0) {
//							finish = aa.toString().length() + finish;
//						}
//						CArray na = new CArray(t);
//						if (finish < start) {
//							//return an empty array in cases where the indexes don't make sense
//							return new CString("", t);
//						}
//						StringBuilder b = new StringBuilder();
//						String val = aa.toString();
//						for (long i = start; i <= finish; i++) {
//							try{
//							b.append(val.charAt((int) i));
//							} catch(StringIndexOutOfBoundsException e){
//								throw new Exceptions.RangeException("String bounds out of range. Tried to get character at index " + i + ", but indicies only go up to " + (val.length() - 1), t);
//							}
//						}
//						return new CString(b.toString(), t);
//					} catch (NumberFormatException e) {
//						throw new ConfigRuntimeException("Ranges must be integer numbers, i.e., [0..5]", ExceptionType.CastException, t);
//					}
//				} else {
//					try {
//						return new CString(args[0].val().charAt(Static.getInt32(index, t)), t);
//					} catch (ConfigRuntimeException e) {
//						if (e.getExceptionType() == ExceptionType.CastException) {
//							throw new ConfigRuntimeException("Expecting an integer index for the array, but found \"" + index
//									+ "\". (Array is not associative, and cannot accept string keys here.)", ExceptionType.CastException, t);
//						} else {
//							throw e;
//						}
//					} catch (StringIndexOutOfBoundsException e) {
//						throw new ConfigRuntimeException("No index at " + index, ExceptionType.RangeException, t);
//					}
//				}
//			} else if (args[0] instanceof ArrayAccess) {
//				throw new ConfigRuntimeException("Wat. How'd you get here? This isn't supposed to be implemented yet.", t);
//			} else {
//				throw new ConfigRuntimeException("Argument 1 of array_get must be an array", ExceptionType.CastException, t);
//			}
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.IndexOverflowException};
		}

		public String docs() {
			return "Returns the element specified at the index of the array. ---- If the element doesn't exist, an exception is thrown. "
					+ "array_get(array, index). Note also that as of 3.1.2, you can use a more traditional method to access elements in an array: "
					+ "array[index] is the same as array_get(array, index), where array is a variable, or function that is an array. In fact, the compiler"
					+ " does some magic under the covers, and literally converts array[index] into array_get(array, index), so if there is a problem "
					+ "with your code, you will get an error message about a problem with the array_get function, even though you may not be using "
					+ "that function directly. If using the plain function access, then if a default is provided, the function will always return that value if the"
					+ " array otherwise doesn't have a value there. This is opposed to throwing an exception or returning null.";
		}

		public Argument returnType() {
			return new Argument("The element at the specified index", Mixed.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The array to get the element from", ArrayAccess.class, "array"),
					new Argument("The index of the array", CString.class, CInt.class, "index").setOptionalDefault(new CSlice(0, -1, Target.UNKNOWN)),
					new Argument("The default value to return, should this array not contain the specified key", Mixed.class, "default").setOptional());
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
		public Construct optimize(Target t, Environment env, Mixed... args) throws ConfigCompileException {
			if (args[0] instanceof ArrayAccess) {
				ArrayAccess aa = (ArrayAccess) args[0];
				if (!aa.canBeAssociative()) {
					if (!(args[1] instanceof CInt) && !(args[1] instanceof CSlice)) {
						throw new ConfigCompileException("Accessing an element as an associative array, when it can only accept integers.", t);
					}
				}
				return null;
			} else {
				throw new ConfigCompileException("Trying to access an element like an array, but it does not support array access.", t);
			}

		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Demonstrates basic usage", "array_get(array(1, 2, 3), 2)"),
						new ExampleScript("Demonstrates exception", "array_get(array(), 1)"),
						new ExampleScript("Demonstrates default", "array_get(array(), 1, 'default')"),
						new ExampleScript("Demonstrates bracket notation", "array(0, 1, 2)[2]"),};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.OPTIMIZE_CONSTANT);
		}
	}

	@api
	public static class array_set extends AbstractFunction {

		public String getName() {
			return "array_set";
		}

		public Integer[] numArgs() {
			return new Integer[]{3};
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			CArray array = list.get("array");
			try {
				array.set(list.get("index").val(), (Construct) list.get("value"), t);
			} catch (IndexOutOfBoundsException e) {
				throw new ConfigRuntimeException("The index " + args[1].val() + " is out of bounds", ExceptionType.IndexOverflowException, t);
			}
			return new CVoid(t);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.IndexOverflowException};
		}

		public String docs() {
			return "Sets the value of the array at the specified index. array_set(array, index, value). Returns void. If"
					+ " the element at the specified index isn't already set, throws an exception, if this is a normal array. Use array_push to avoid this.";
		}

		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The array to set", CArray.class, "array"),
					new Argument("The index of the value which should be set", Mixed.class, "index"),
					new Argument("The value to actually set in the array", Mixed.class, "value"));
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
						new ExampleScript("Demonstrates usage", "assign(@array, array(null))\nmsg(@array)\narray_set(@array, 0, 'value0')\nmsg(@array)"),
						new ExampleScript("Demonstrates using assign", "assign(@array, array(null))\nmsg(@array)\nassign(@array[0], 'value0')\nmsg(@array)"),};
		}
	}

	@api
	public static class array_push extends AbstractFunction {

		public String getName() {
			return "array_push";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			if (args[0] instanceof CArray) {
				if (args.length < 2) {
					throw new ConfigRuntimeException("At least 2 arguments must be provided to array_push", ExceptionType.InsufficientArgumentsException, t);
				}
				for (int i = 1; i < args.length; i++) {
					((CArray) args[0]).push(args[i]);
				}
				return new CVoid(t);
			}
			throw new ConfigRuntimeException("Argument 1 of array_push must be an array", ExceptionType.CastException, t);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "Pushes the specified value(s) onto the end of the array";
		}

		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The array to push onto", CArray.class, "array"),
					new Argument("The value to push", Mixed.class, "value"),
					new Argument("More values, if pushing on more than one at once", CArray.class, "value2").setGenerics(new Generic(Mixed.class)).setVarargs());
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
						new ExampleScript("Demonstrates usage", "assign(@array, array())\nmsg(@array)\narray_push(@array, 0)\nmsg(@array)"),
						new ExampleScript("Demonstrates pushing multiple values", "assign(@array, array())\nmsg(@array)\narray_push(@array, 0, 1, 2)\nmsg(@array)"),};
		}
	}
	
	@api
	public static class array_insert extends AbstractFunction{

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.IndexOverflowException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return null;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			CArray array = list.get("array");
			Construct value = list.get("item");
			int index = list.getInt("index", t);
			array.push(value, index);
			return new CVoid(t);
		}

		public String getName() {
			return "array_insert";
		}

		public Integer[] numArgs() {
			return new Integer[]{3};
		}

		public String docs() {
			return "Inserts an item at the specified index, and shifts all other items in the array to the right one."
					+ " If index is greater than the size of the array, an IndexOverflowException is thrown, though the index may be equal"
					+ " to the size, in which case this works just like array_push. The array must be normal though, associative arrays"
					+ " are not supported.";
		}

		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The array to insert into", CArray.class, "array").setGenerics(Generic.ANY),
						new Argument("The item to insert", Mixed.class, "item"),
						new Argument("The index to insert at, less than or equal to the array's size", CInt.class, "index")
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "@array = array(1, 3, 4)\n"
					+ "array_insert(@array, 2, 1)\n"
					+ "msg(@array)"),
				new ExampleScript("Usage as if it were array_push", "@array = array(1, 2, 3)\n"
					+ "array_insert(@array, 4, array_size(@array))\n"
					+ "msg(@array)")
			};
		}		
		
	}

	@api
	public static class array_contains extends AbstractFunction {

		public String getName() {
			return "array_contains";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			equals e = new equals();
			if (args[0] instanceof CArray) {
				CArray ca = (CArray) args[0];
				for (int i = 0; i < ca.size(); i++) {
					if (((CBoolean) e.exec(t, env, ca.get(i, t), args[1])).castToBoolean()) {
						return new CBoolean(true, t);
					}
				}
				return new CBoolean(false, t);
			} else {
				throw new ConfigRuntimeException("Argument 1 of array_contains must be an array", ExceptionType.CastException, t);
			}
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "Checks to see if testValue is in array. If the array contains non-string values,"
					+ " the item's 'toString' value is compared.";
		}

		public Argument returnType() {
			return new Argument("true if the array contains the test value, false otherwise", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The array to check in", CArray.class, "array"),
					new Argument("The value to check for", CString.class, "testValue"));
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
						new ExampleScript("Demonstrates finding a value", "array_contains(array(0, 1, 2), 2)"),
						new ExampleScript("Demonstrates not finding a value", "array_contains(array(0, 1, 2), 5)"),
						new ExampleScript("Demonstrates finding a value listed multiple times", "array_contains(array(1, 1, 1), 1)"),
						new ExampleScript("Demonstrates finding a string", "array_contains(array('a', 'b', 'c'), 'b')"),};
		}
	}

	@api
	public static class array_contains_ic extends AbstractFunction {

		public String getName() {
			return "array_contains_ic";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "Works like array_contains, except the comparison ignores case. If the array"
					+ " contains non-string values, the item's 'toString' value is compared.";
		}

		public Argument returnType() {
			return new Argument("true if the array contains the test value (case-insensitive), false otherwise", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The array to check in", CArray.class, "array"),
					new Argument("The value to check for", CString.class, "testValue"));
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

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			equals_ic e = new equals_ic();
			if (args[0] instanceof CArray) {
				CArray ca = (CArray) args[0];
				for (int i = 0; i < ca.size(); i++) {
					if (((CBoolean) e.exec(t, environment, ca.get(i, t), args[1])).castToBoolean()) {
						return new CBoolean(true, t);
					}
				}
				return new CBoolean(false, t);
			} else {
				throw new ConfigRuntimeException("Argument 1 of array_contains_ic must be an array", ExceptionType.CastException, t);
			}
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Demonstrates usage", "array_contains_ic(array('A', 'B', 'C'), 'A')"),
						new ExampleScript("Demonstrates usage", "array_contains_ic(array('A', 'B', 'C'), 'a')"),
						new ExampleScript("Demonstrates usage", "array_contains_ic(array('A', 'B', 'C'), 'd')"),};
		}
	}

	@api
	public static class array_index_exists extends AbstractFunction {

		public String getName() {
			return "array_index_exists";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "Checks to see if the specified array has an element at index";
		}

		public Argument returnType() {
			return new Argument("true if the array has the specified index", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The array to search in", ArrayAccess.class, "array"),
					new Argument("The array index to search for", CString.class, CInt.class, "index"));
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

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			ArrayAccess array = list.get("array");
			Mixed index = list.get("index");
			return new CBoolean(array.containsKey(index.val()), t);
//			if (args[0] instanceof CArray) {
//				if (!((CArray) args[0]).inAssociativeMode()) {
//					try {
//						int index = Static.getInt32(args[1], t);
//						CArray ca = (CArray) args[0];
//						return new CBoolean(index <= ca.size() - 1, t);
//					} catch (ConfigRuntimeException e) {
//						//They sent a key that is a string. Obviously it doesn't exist.
//						return new CBoolean(false, t);
//					}
//				} else {
//					CArray ca = (CArray) args[0];
//					return new CBoolean(ca.containsKey(args[1].val()), t);
//				}
//			} else {
//				throw new ConfigRuntimeException("Expecting argument 1 to be an array", ExceptionType.CastException, t);
//			}
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Demonstrates a true condition", "array_index_exists(array(0, 1, 2), 0)"),
						new ExampleScript("Demonstrates a false condition", "array_index_exists(array(0, 1, 2), 3)"),
						new ExampleScript("Demonstrates an associative array", "array_index_exists(array(a: 'A', b: 'B'), 'a')"),
						new ExampleScript("Demonstrates an associative array", "array_index_exists(array(a: 'A', b: 'B'), 'c')"),};
		}
	}

	@api
	public static class array_resize extends AbstractFunction {

		public String getName() {
			return "array_resize";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		public String docs() {
			return "Resizes the given array so that it is at least of size size, filling the blank spaces with"
					+ " fill, or null by default. If the size of the array is already at least size, nothing happens; in other words this"
					+ " function can only be used to increase the size of the array.";
			//+ " If the array is an associative array, the non numeric values are simply copied over.";
		}

		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The array to fill", CArray.class, "array"),
					new Argument("The desired size of the array", CInt.class, "size"),
					new Argument("The fill value", Mixed.class, "fill").setOptionalDefaultNull());
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
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

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			CArray array = list.get("array");
			int size = list.getInt("size", t);
			Mixed fill = list.get("fill");
			for (long i = array.size(); i < size; i++) {
				array.push((Construct) fill);
			}
			return new CVoid(t);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Demonstrates basic usage", "assign(@array, array())\nmsg(@array)\narray_resize(@array, 2)\nmsg(@array)"),
						new ExampleScript("Demonstrates custom fill", "assign(@array, array())\nmsg(@array)\narray_resize(@array, 2, 'a')\nmsg(@array)"),};
		}
	}

	@api
	public static class range extends AbstractFunction {

		public String getName() {
			return "range";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		public String docs() {
			return "Returns an array of numbers from start to (finish - 1)"
					+ " skipping increment integers per count. start defaults to 0, and increment defaults to 1. All inputs"
					+ " must be integers. If the input doesn't make sense, it will reasonably degrade, and return an empty array.";
		}

		public Argument returnType() {
			return new Argument("An array populated with the ranged values.", CArray.class).setGenerics(Generic.ANY);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The starting value, inclusive (this number will be included in the results)", CInt.class, "start").setOptionalDefault(0),
					new Argument("The upper range, exclusive (this number will not be included in the results)", CInt.class, "finish"),
					new Argument("The amount to increment each step", CInt.class, "increment").setOptionalDefault(1));
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
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

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			long start = list.getLong("start", t);
			long finish = list.getLong("finish", t);
			long increment = list.getLong("increment", t);
			if (start < finish && increment < 0 || start > finish && increment > 0 || increment == 0) {
				return new CArray(t);
			}
			CArray ret = new CArray(t);
			for (long i = start; (increment > 0 ? i < finish : i > finish); i = i + increment) {
				ret.push(new CInt(i, t));
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
						new ExampleScript("In reverse", "range(10, 0, -1)"),};
		}
	}

	@api
	public static class array_keys extends AbstractFunction {

		public String getName() {
			return "array_keys";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Returns the keys in this array as a normal array. If the array passed in is already a normal array,"
					+ " the keys will be 0 -> (array_size(array) - 1)";
		}

		public Argument returnType() {
			return new Argument("An array of the keys", CArray.class).setGenerics(new Generic(CString.class, CInt.class));
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The array to pull the keys from", CArray.class, "array"));
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
				CArray ca = (CArray) args[0];
				CArray ca2 = new CArray(t);
				for (String c : ca.keySet()) {
					ca2.push(new CString(c, t));
				}
				return ca2;
			} else {
				throw new ConfigRuntimeException(this.getName() + " expects arg 1 to be an array", ExceptionType.CastException, t);
			}
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Basic usage", "array_keys(array('a', 'b', 'c'))"),
						new ExampleScript("With associative array", "array_keys(array(one: 'a', two: 'b', three: 'c'))"),};
		}
	}

	@api
	public static class array_normalize extends AbstractFunction {

		public String getName() {
			return "array_normalize";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Returns a new normal array, given an associative array. (If the array passed in is not associative, a copy of the "
					+ " array is returned).";
		}

		public Argument returnType() {
			return new Argument("A new, normalized array", CArray.class).setGenerics(Generic.ANY);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The array to use for normalization", CArray.class, "array"));
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
				CArray ca = (CArray) args[0];
				CArray ca2 = new CArray(t);
				for (String c : ca.keySet()) {
					ca2.push(ca.get(c, t));
				}
				return ca2;
			} else {
				throw new ConfigRuntimeException(this.getName() + " expects arg 1 to be an array", ExceptionType.CastException, t);
			}
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Basic usage", "array_normalize(array(one: 'a', two: 'b', three: 'c'))"),
						new ExampleScript("Usage with normal array", "array_normalize(array(1, 2, 3))"),};
		}
	}

	@api
	public static class array_merge extends AbstractFunction {

		public String getName() {
			return "array_merge";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public String docs() {
			return "Merges the specified arrays from left to right, and returns a new array. If the array"
					+ " merged is associative, it will overwrite the keys from left to right, but if the arrays are normal, the keys are ignored,"
					+ " and values are simply pushed.";
		}

		public Argument returnType() {
			return new Argument("", CArray.class).setGenerics(new Generic("?", Mixed.class));
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The first array", CArray.class, "array1"),
					new Argument("The second array", CArray.class, "array2"),
					new Argument("Additonal arrays", CArray.class, "arrayN").setGenerics(Generic.ANY).setVarargs());
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InsufficientArgumentsException, ExceptionType.CastException};
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

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			CArray newArray = new CArray(t);
			if (args.length < 2) {
				throw new ConfigRuntimeException("array_merge must be called with at least two parameters", ExceptionType.InsufficientArgumentsException, t);
			}
			for (int i = 0; i < args.length; i++) {
				if (args[i] instanceof CArray) {
					CArray cur = (CArray) args[i];
					if (!cur.inAssociativeMode()) {
						for (int j = 0; j < cur.size(); j++) {
							newArray.push(cur.get(j, t));
						}
					} else {
						for (String key : cur.keySet()) {
							newArray.set(key, cur.get(key, t), t);
						}
					}
				} else {
					throw new ConfigRuntimeException("All arguments to array_merge must be arrays", ExceptionType.CastException, t);
				}
			}
			return newArray;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Basic usage", "array_merge(array(1), array(2), array(3))"),
						new ExampleScript("With associative arrays", "array_merge(array(one: 1), array(two: 2), array(three: 3))"),
						new ExampleScript("With overwrites", "array_merge(array(one: 1), array(one: 2), array(one: 3))"),};
		}
	}

	@api
	public static class array_remove extends AbstractFunction {

		public String getName() {
			return "array_remove";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "Removes an index from an array. If the array is a normal"
					+ " array, all values' indicies are shifted left one. If the array is associative,"
					+ " the index is simply removed. If the index doesn't exist, the array remains"
					+ " unchanged. The value removed is returned.";
		}

		public Argument returnType() {
			return new Argument("The value removed", Mixed.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("", CArray.class, ""),
					new Argument("", CInt.class, CString.class, ""));
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.RangeException, ExceptionType.CastException, ExceptionType.PluginInternalException};
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

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if (args[0] instanceof CArray) {
				CArray ca = (CArray) args[0];
				return ca.remove(args[1].val(), t);
			} else {
				throw new ConfigRuntimeException("Argument 1 of array_remove should be an array", ExceptionType.CastException, t);
			}
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Basic usage", "assign(@array, array(1, 2, 3))\nmsg(array_remove(@array, 2))\nmsg(@array)"),
						new ExampleScript("With associative array", "assign(@array, array(one: 'a', two: 'b', three: 'c'))\nmsg(array_remove(@array, 'two'))\nmsg(@array)"),};
		}
	}

	@api
	public static class array_implode extends AbstractFunction {

		public String getName() {
			return "array_implode";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Given an array and glue, to-strings all the elements"
					+ " in the array (just the values, not the keys), and joins them with the glue, defaulting to a space. For instance"
					+ " array_implode(array(1, 2, 3), '-') will return \"1-2-3\".";
		}

		public Argument returnType() {
			return new Argument("The glued together array", CString.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The array of elements to glue together", CArray.class, "array"),
					new Argument("The glue to use to glue the elements together with", CString.class, "glue").setOptionalDefault(" "));
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
			if (!(args[0] instanceof CArray)) {
				throw new ConfigRuntimeException("Expecting argument 1 to be an array", ExceptionType.CastException, t);
			}
			StringBuilder b = new StringBuilder();
			CArray ca = (CArray) args[0];
			String glue = " ";
			if (args.length == 2) {
				glue = args[1].val();
			}
			boolean first = true;
			for (String key : ca.keySet()) {
				Mixed value = ca.get(key, t);
				if (!first) {
					b.append(glue).append(value.val());
				} else {
					b.append(value.val());
					first = false;
				}
			}
			return new CString(b.toString(), t);
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Basic usage", "array_implode(array(1, 2, 3), '-')"),
						new ExampleScript("With associative array", "array_implode(array(one: 'a', two: 'b', three: 'c'), '-')"),};
		}
	}

	@api
	public static class cslice extends AbstractFunction {

		public String getName() {
			return "cslice";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "Dynamically creates an array slice, which can be used with array_get"
					+ " (or the [bracket notation]) to get a range of elements. cslice(0, 5) is equivalent"
					+ " to 0..5 directly in code, however with this function you can also do cslice(@var, @var),"
					+ " or other more complex expressions, which are not possible in static code.";
		}

		public Argument returnType() {
			return new Argument("The dynamically created slice", CSlice.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The value to start with, inclusive (it will include this index)", CInt.class, "from"),
					new Argument("The value to end with, inclusive (it will include this index)", CInt.class, "to"));
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
			ArgList list = getBuilder().parse(args, this, t);
			return new CSlice(list.getInt("from", t), list.getInt("to", t), t);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Basic usage", "array(1, 2, 3)[cslice(0, 1)]"),};
		}
	}

	@api
	public static class array_sort extends AbstractFunction implements Optimizable {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return null;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if (!(args[0] instanceof CArray)) {
				throw new ConfigRuntimeException("The first parameter to array_sort must be an array", ExceptionType.CastException, t);
			}
			CArray ca = (CArray) args[0];
			CArray.SortType sortType = CArray.SortType.REGULAR;
			try {
				if (args.length == 2) {
					sortType = CArray.SortType.valueOf(args[1].val().toUpperCase());
				}
			} catch (IllegalArgumentException e) {
				throw new ConfigRuntimeException("The sort type must be one of either: " + StringUtils.Join(CArray.SortType.values(), ", ", " or "),
						ExceptionType.FormatException, t);
			}
			ca.sort(sortType, t);
			return ca;
		}

		public String getName() {
			return "array_sort";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Sorts an array in place, and also returns a reference to the array. ---- The"
					+ " complexity of this sort algorithm is guaranteed to be no worse than n log n, as it uses merge sort."
					+ " The array is sorted in place, a new array is not explicitly created, so if you sort an array that"
					+ " is passed in as a variable, the contents of that variable will be sorted, even if you don't re-assign"
					+ " the returned array back to the variable. If you really need the old array, you should create a copy of"
					+ " the array first, like so: assign(@sorted, array_sort(@array[])). The sort type may be one of the following:"
					+ " " + StringUtils.Join(CArray.SortType.values(), ", ", " or ") + ". A regular sort sorts the elements without changing types first. A"
					+ " numeric sort always converts numeric values to numbers first (so 001 becomes 1). A string sort compares"
					+ " values as strings, and a string_ic sort is the same as a string sort, but the comparision is case-insensitive."
					+ " If the array contains array values, a CastException is thrown; inner arrays cannot be sorted against each"
					+ " other. If the array is associative, a warning will be raised if the General logging channel is set to verbose,"
					+ " because the array's keys will all be lost in the process. To avoid this warning, and to be more explicit,"
					+ " you can use array_normalize() to normalize the array first. Note that the reason this function is an"
					+ " in place sort instead of explicitely cloning the array is because in most cases, you may not need"
					+ " to actually clone the array, an expensive operation. Due to this, it has slightly different behavior"
					+ " than array_normalize, which could have also been implemented in place.";
		}

		public Argument returnType() {
			return new Argument("A reference to the array passed in", CArray.class).setGenerics(Generic.ANY);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The array to sort", CArray.class, "array"),
					new Argument("The sort type", CArray.SortType.class, "sortType").setOptionalDefault(CArray.SortType.REGULAR));
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			if (children.size() == 2) {
				if (!children.get(1).getData().isDynamic()) {
					try {
						CArray.SortType.valueOf(children.get(1).getData().val().toUpperCase());
					} catch (IllegalArgumentException e) {
						throw new ConfigCompileException("The sort type must be one of either: " + StringUtils.Join(CArray.SortType.values(), ", ", " or "), t);
					}
				}
			}
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Regular sort", "assign(@array, array('a', 2, 4, 'string'))\narray_sort(@array, 'REGULAR')\nmsg(@array)"),
						new ExampleScript("Numeric sort", "assign(@array, array('03', '02', '4', '1'))\narray_sort(@array, 'NUMERIC')\nmsg(@array)"),
						new ExampleScript("String sort", "assign(@array, array('03', '02', '4', '1'))\narray_sort(@array, 'STRING')\nmsg(@array)"),
						new ExampleScript("String sort (with words)", "assign(@array, array('Zeta', 'zebra', 'Minecraft', 'mojang', 'Appliance', 'apple'))\narray_sort(@array, 'STRING')\nmsg(@array)"),
						new ExampleScript("Ignore case sort", "assign(@array, array('Zeta', 'zebra', 'Minecraft', 'mojang', 'Appliance', 'apple'))\narray_sort(@array, 'STRING_IC')\nmsg(@array)"),};
		}
	}

	@api
	public static class array_sort_async extends AbstractFunction {

		static RunnableQueue queue;
		static boolean initialized = false;

		public array_sort_async() {
			if (!initialized) {
				queue = new RunnableQueue("MethodScript-arraySortAsync");
				queue.invokeLater(null, new Runnable() {
					public void run() {
						//This warms up the queue. Apparently.
					}
				});
				if (StaticLayer.GetConvertor() != null) {
					StaticLayer.GetConvertor().addShutdownHook(new Runnable() {
						public void run() {
							synchronized (array_sort_async.this) {
								initialized = false;
							}
							queue.shutdown();
						}
					});
					synchronized (this) {
						initialized = true;
					}
				}
			}
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
			ArgList list = getBuilder().parse(args, this, t);
			final CArray array = list.get("array");
			final CArray.SortType sortType = list.getEnum("sortType", CArray.SortType.class);
			final CClosure callback = list.get("closure");
			queue.invokeLater(environment.getEnv(GlobalEnv.class).GetDaemonManager(), new Runnable() {
				public void run() {
					Mixed c = new array_sort().exec(Target.UNKNOWN, null, array, new CString(sortType.name(), Target.UNKNOWN));
					callback.execute(new Mixed[]{c});
				}
			});
			return new CVoid(t);
		}

		public String getName() {
			return "array_sort_async";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		public String docs() {
			return "Works like array_sort, but does the sort on another"
					+ " thread, then calls the closure and sends it the sorted array. This is useful if the array"
					+ " is large enough to actually \"stall\" the server when doing the sort.";
		}

		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The array to be sorted", CArray.class, "array").setGenerics(Generic.ANY),
					new Argument("The sort type", CArray.SortType.class, "sortType").setOptionalDefault(CArray.SortType.REGULAR).addAnnotation(new NonNull()),
					new Argument("The closure that recieves the sorted array once finished", CClosure.class, "closure").setGenerics(new Generic("?", CArray.class)));
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class array_remove_values extends AbstractFunction {

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
			if (!(args[0] instanceof CArray)) {
				throw new ConfigRuntimeException("Expected parameter 1 to be an array, but was " + args[0].val(), ExceptionType.CastException, t);
			}
			((CArray) args[0]).removeValues(args[1]);
			return new CVoid(t);
		}

		public String getName() {
			return "array_remove_values";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "Removes all instances of value from the specified array."
					+ " For instance, array_remove_values(array(1, 2, 2, 3), 2) would produce the"
					+ " array(1, 3). Note that it returns void however, so it will simply in place"
					+ " modify the array passed in, much like array_remove.";
		}

		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The array that will have values removed from", CArray.class, "array").setGenerics(Generic.ANY),
					new Argument("The value to remove", Mixed.class, "value"));
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Basic usage", "assign(@array, array(1, 2, 2, 3))\nmsg(@array)\narray_remove_values(@array, 2)\nmsg(@array)"),};
		}
	}

	@api
	public static class array_indexes extends AbstractFunction {

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
			if (!(args[0] instanceof CArray)) {
				throw new ConfigRuntimeException("Expected parameter 1 to be an array, but was " + args[0].val(), ExceptionType.CastException, t);
			}
			return ((CArray) args[0]).indexesOf(args[1]);
		}

		public String getName() {
			return "array_indexes";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "Returns an array with all the keys of the specified array"
					+ " at which the specified value is equal. That is, for the array(1, 2, 2, 3), if"
					+ " value were 2, would return array(1, 2). If the value cannot be found in the"
					+ " array at all, an empty array will be returned.";
		}

		public Argument returnType() {
			return new Argument("The list of keys", CArray.class).setGenerics(new Generic(CString.class, CInt.class));
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The array to check in", CArray.class, "array").setGenerics(Generic.ANY),
					new Argument("The value to check for", Mixed.class, "value"));
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Basic usage", "assign(@array, array(1, 2, 2, 3))\nmsg(array_indexes(@array, 2))"),
						new ExampleScript("Not found", "assign(@array, array(1, 2, 2, 3))\nmsg(array_indexes(@array, 5))"),};
		}
	}

	@api
	public static class array_index extends AbstractFunction {

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
			CArray ca = (CArray) new array_indexes().exec(t, environment, args);
			if (ca.isEmpty()) {
				return null;
			} else {
				return ca.get(0);
			}
		}

		public String getName() {
			return "array_index";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "Works exactly like array_indexes(array, value)[0], except in the case where"
					+ " the value is not found, returns null. That is to say, if the value is contained in an"
					+ " array (even multiple times) the index of the first element is returned.";
		}

		public Argument returnType() {
			return new Argument("The index of the array, or null if the value is not found", CString.class, CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The array to search in", CArray.class, "array"),
					new Argument("The value to search for", Mixed.class, "value"));
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Basic usage", "assign(@array, array(1, 2, 2, 3))\nmsg(array_index(@array, 2))"),
						new ExampleScript("Not found", "assign(@array, array(1, 2, 2, 3))\nmsg(array_index(@array, 5))"),};
		}
	}

	@api
	public static class array_reverse extends AbstractFunction {

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
			if (args[0] instanceof CArray) {
				((CArray) args[0]).reverse();
			}
			return new CVoid(t);
		}

		public String getName() {
			return "array_reverse";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Reverses an array in place. However, if the array is associative, throws a CastException, since associative"
					+ " arrays are more like a map.";
		}

		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The array to reverse, in place", CArray.class, "array").setGenerics(Generic.ANY));
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
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
	public static class array_rand extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.RangeException, ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return null;
		}
		Random r = new Random(System.currentTimeMillis());

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			ArrayAccess array = list.get("array");
			long number = list.getLong("number", t);
			boolean getKeys = list.getBoolean("getKeys", t);

			CArray newArray = new CArray(t);
			LinkedHashSet<Integer> randoms = new LinkedHashSet<Integer>();
			while (randoms.size() < number) {
				randoms.add(java.lang.Math.abs(r.nextInt() % (int) array.size()));
			}
			List<String> keySet = new ArrayList<String>(array.keySet());
			for (Integer i : randoms) {
				if (getKeys) {
					newArray.push(new CString(keySet.get(i), t));
				} else {
					newArray.push(array.get(keySet.get(i), t));
				}
			}
			return newArray;
		}

		public String getName() {
			return "array_rand";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		public String docs() {
			return "Returns a random selection of keys or values from an array. The array may be"
					+ " either normal or associative. Number defaults to 1, and getKey defaults to true. If number is greater than"
					+ " the size of the array, a RangeException is thrown. No value will be returned twice from the array however, one it"
					+ " is \"drawn\" from the array, it is not placed back in. The order of the elements in the array will also be random,"
					+ " if order is important, use array_sort().";
		}

		public Argument returnType() {
			return new Argument("A new array with the selected random values", CArray.class).setGenerics(Generic.ANY);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("The array to select from", ArrayAccess.class, "array").setGenerics(Generic.ANY),
					new Argument("The number of items to select from the array", CInt.class, "number").setOptionalDefault(1).addAnnotation(new Ranged(1, Integer.MAX_VALUE)),
					new Argument("If true, it will select from the keys instead of the values", CBoolean.class, "getKeys").setOptionalDefault(true));
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
						new ExampleScript("Usage with a normal array", "assign(@array, array('a', 'b', 'c', 'd', 'e'))\nmsg(array_rand(@array))"),
						new ExampleScript("Usage with a normal array, using getKeys false, and returning 2 results",
						"assign(@array, array('a', 'b', 'c', 'd', 'e'))\nmsg(array_rand(@array, 2, false))"),
						new ExampleScript("Usage with an associative array",
						"assign(@array, array(one: 'a', two: 'b', three: 'c', four: 'd', five: 'e'))\nmsg(array_rand(@array))"),};
		}
	}
	
	@api
	public static class array_unique extends AbstractFunction{

		private final static equals equals = new equals();
		private final static BasicLogic.sequals sequals = new BasicLogic.sequals();
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(final Target t, final Environment environment, Mixed... args) throws ConfigRuntimeException {
			CArray array = Static.getArray(args[0], t);
			boolean compareTypes = true;
			if(args.length == 2){
				compareTypes = args[1].primitive(t).castToBoolean();
			}
			final boolean fCompareTypes = compareTypes;
			if(array.inAssociativeMode()){
				return array.doClone();
			} else {
				List<Mixed> asList = array.asList();
				CArray newArray = new CArray(t);
				Set<Mixed> set = new LinkedComparatorSet<Mixed>(asList, new LinkedComparatorSet.EqualsComparator<Mixed>() {

					public boolean checkIfEquals(Mixed item1, Mixed item2) {
						return (fCompareTypes && sequals.exec(t, environment, item1, item2).castToBoolean())
								|| (!fCompareTypes && equals.exec(t, environment, item1, item2).castToBoolean());
					}
				});
				for(Mixed c : set){
					newArray.push(c);
				}
				return newArray;
			}
		}

		public String getName() {
			return "array_unique";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Removes all non-unique values from an array. ---- compareTypes is true by default, which means that in the array"
					+ " array(1, '1'), nothing would be removed from the array, since both values are different data types. However, if compareTypes is false,"
					+ " then the first value would remain, but the second value would be removed. A new array is returned. If the array is associative, by definition,"
					+ " there are no unique values, so a clone of the array is returned.";
		}
		
		public Argument returnType() {
			return new Argument("", CArray.class).setGenerics(Generic.ANY);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CArray.class, "array"),
						new Argument("", CBoolean.class, "compareTypes").setOptionalDefault(true)
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "array_unique(array(1, 2, 2, 3, 4))"),
				new ExampleScript("No removal of different datatypes", "array_unique(array(1, '1'))"),
				new ExampleScript("Removal of different datatypes, by setting compareTypes to false", "array_unique(array(1, '1'), false)"),
			};
		}				
		
	}
}
