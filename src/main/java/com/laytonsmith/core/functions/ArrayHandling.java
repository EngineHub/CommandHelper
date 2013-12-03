package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.LinkedComparatorSet;
import com.laytonsmith.PureUtilities.RunnableQueue;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.BasicLogic.equals;
import com.laytonsmith.core.functions.BasicLogic.equals_ic;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.interfaces.ArrayAccess;
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

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			if (args[0] instanceof CArray) {
				return new CInt(((CArray) args[0]).size(), t);
			}
			throw new ConfigRuntimeException("Argument 1 of array_size must be an array", ExceptionType.CastException, t);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public String docs() {
			return "int {array} Returns the size of this array as an integer.";
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
				new ExampleScript("Demonstrates usage", "array_size(array(1, 2, 3, 4, 5))"),				
			};
		}
				
	}

	@api(environments={GlobalEnv.class})
	public static class array_get extends AbstractFunction implements Optimizable {

		public String getName() {
			return "array_get";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			Construct index = new CSlice(0, -1, t);
			Construct defaultConstruct = null;
			if (args.length >= 2) {
				index = args[1];
			}
			if (args.length >= 3) {
				defaultConstruct = args[2];
			}

			if (args[0] instanceof CArray) {
				CArray ca = (CArray) args[0];
				if (index instanceof CSlice) {
					if (ca.inAssociativeMode()) {
						if (((CSlice) index).getStart() == 0 && ((CSlice) index).getFinish() == -1) {
							//Special exception, we want to clone the whole array
							CArray na = CArray.GetAssociativeArray(t);
							for (String key : ca.keySet()) {
								try {
									na.set(key, ca.get(key, t).clone(), t);
								} catch (CloneNotSupportedException ex) {
									na.set(key, ca.get(key, t), t);
								}
							}
							return na;
						}
						throw new ConfigRuntimeException("Array slices are not allowed with an associative array", ExceptionType.CastException, t);
					}
					//It's a range
					long start = ((CSlice) index).getStart();
					long finish = ((CSlice) index).getFinish();
					try {
						//Convert negative indexes 
						if (start < 0) {
							start = ca.size() + start;
						}
						if (finish < 0) {
							finish = ca.size() + finish;
						}
						CArray na = new CArray(t);
						if (finish < start) {
							//return an empty array in cases where the indexes don't make sense
							return na;
						}
						for (long i = start; i <= finish; i++) {
							try {
								na.push(ca.get((int) i, t).clone());
							} catch (CloneNotSupportedException e) {
								na.push(ca.get((int) i, t));
							}
						}
						return na;
					} catch (NumberFormatException e) {
						throw new ConfigRuntimeException("Ranges must be integer numbers, i.e., [0..5]", ExceptionType.CastException, t);
					}
				} else {
					try {
						if (!ca.inAssociativeMode()) {
							long iindex = Static.getInt(args[1], t);
							if (iindex < 0) {
								//negative index, convert to positive index
								iindex = ca.size() + iindex;
							}
							return ca.get(iindex, t);
						} else {
							return ca.get(args[1], t);
						}
					} catch (ConfigRuntimeException e) {
						if (e.getExceptionType() == ExceptionType.IndexOverflowException) {
							if(defaultConstruct != null){
								return defaultConstruct;
							}
						}
						if(env.getEnv(GlobalEnv.class).GetFlag("array-special-get") != null){
							//They are asking for an array that doesn't exist yet, so let's create it now.
							CArray c;
							if(ca.inAssociativeMode()){
								c = CArray.GetAssociativeArray(t);
							} else {
								c = new CArray(t);
							}
							ca.set(args[1], c, t);
							return c;
						}
						throw e;
					}
				}
			} else if (args[0] instanceof CString) {
				if (index instanceof CSlice) {
					ArrayAccess aa = (ArrayAccess) args[0];
					//It's a range
					long start = ((CSlice) index).getStart();
					long finish = ((CSlice) index).getFinish();
					try {
						//Convert negative indexes 
						if (start < 0) {
							start = aa.val().length() + start;
						}
						if (finish < 0) {
							finish = aa.val().length() + finish;
						}
						CArray na = new CArray(t);
						if (finish < start) {
							//return an empty array in cases where the indexes don't make sense
							return new CString("", t);
						}
						StringBuilder b = new StringBuilder();
						String val = aa.val();
						for (long i = start; i <= finish; i++) {
							try{
							b.append(val.charAt((int) i));
							} catch(StringIndexOutOfBoundsException e){
								throw new Exceptions.RangeException("String bounds out of range. Tried to get character at index " + i + ", but indicies only go up to " + (val.length() - 1), t);
							}
						}
						return new CString(b.toString(), t);
					} catch (NumberFormatException e) {
						throw new ConfigRuntimeException("Ranges must be integer numbers, i.e., [0..5]", ExceptionType.CastException, t);
					}
				} else {
					try {
						return new CString(args[0].val().charAt(Static.getInt32(index, t)), t);
					} catch (ConfigRuntimeException e) {
						if (e.getExceptionType() == ExceptionType.CastException) {
							if(args[0] instanceof CArray){
								throw new ConfigRuntimeException("Expecting an integer index for the array, but found \"" + index
										+ "\". (Array is not associative, and cannot accept string keys here.)", ExceptionType.CastException, t);
							} else {
								throw new ConfigRuntimeException("Expecting an array, but \"" + args[0] + "\" was found.", ExceptionType.CastException, t);
							}
						} else {
							throw e;
						}
					} catch (StringIndexOutOfBoundsException e) {
						throw new ConfigRuntimeException("No index at " + index, ExceptionType.RangeException, t);
					}
				}
			} else if (args[0] instanceof ArrayAccess) {
				throw ConfigRuntimeException.CreateUncatchableException("Wat. How'd you get here? This isn't supposed to be implemented yet.", t);
			} else {
				throw new ConfigRuntimeException("Argument 1 of array_get must be an array", ExceptionType.CastException, t);
			}
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.IndexOverflowException};
		}

		public String docs() {
			return "mixed {array, index, [default]} Returns the element specified at the index of the array. ---- If the element doesn't exist, an exception is thrown. "
					+ "array_get(array, index). Note also that as of 3.1.2, you can use a more traditional method to access elements in an array: "
					+ "array[index] is the same as array_get(array, index), where array is a variable, or function that is an array. In fact, the compiler"
					+ " does some magic under the covers, and literally converts array[index] into array_get(array, index), so if there is a problem "
					+ "with your code, you will get an error message about a problem with the array_get function, even though you may not be using "
					+ "that function directly. If using the plain function access, then if a default is provided, the function will always return that value if the"
					+ " array otherwise doesn't have a value there. This is opposed to throwing an exception or returning null.";
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
		public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
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
				new ExampleScript("Demonstrates bracket notation", "array(0, 1, 2)[2]"),
			};
		}
		
		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
				OptimizationOption.OPTIMIZE_CONSTANT
			);
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

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			env.getEnv(GlobalEnv.class).SetFlag("array-special-get", true);
			Construct array = parent.seval(nodes[0], env);
			env.getEnv(GlobalEnv.class).ClearFlag("array-special-get");
			Construct index = parent.seval(nodes[1], env);
			Construct value = parent.seval(nodes[2], env);
			if(!(array instanceof CArray)){
				throw new ConfigRuntimeException("Argument 1 of array_set must be an array", ExceptionType.CastException, t);
			}
			try {
				((CArray)array).set(index, value, t);
			} catch (IndexOutOfBoundsException e) {
				throw new ConfigRuntimeException("The index " + index.asString().getQuote() + " is out of bounds", ExceptionType.IndexOverflowException, t);
			}
			return new CVoid(t);
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			if (args[0] instanceof CArray) {
				try {
					((CArray) args[0]).set(args[1], args[2], t);
				} catch (IndexOutOfBoundsException e) {
					throw new ConfigRuntimeException("The index " + args[1].val() + " is out of bounds", ExceptionType.IndexOverflowException, t);
				}
				return new CVoid(t);
			}
			throw new ConfigRuntimeException("Argument 1 of array_set must be an array", ExceptionType.CastException, t);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.IndexOverflowException};
		}

		public String docs() {
			return "void {array, index, value} Sets the value of the array at the specified index. array_set(array, index, value). Returns void. If"
					+ " the element at the specified index isn't already set, throws an exception. Use array_push to avoid this.";
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
				new ExampleScript("Demonstrates using assign", "assign(@array, array(null))\nmsg(@array)\nassign(@array[0], 'value0')\nmsg(@array)"),
			};
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

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
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
			return "void {array, value, [value2...]} Pushes the specified value(s) onto the end of the array";
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
				new ExampleScript("Demonstrates pushing multiple values", "assign(@array, array())\nmsg(@array)\narray_push(@array, 0, 1, 2)\nmsg(@array)"),
			};
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

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CArray array = Static.getArray(args[0], t);
			Construct value = args[1];
			int index = Static.getInt32(args[2], t);
			try{
				array.push(value, index);
			} catch(IllegalArgumentException e){
				throw new Exceptions.CastException(e.getMessage(), t);
			} catch(IndexOutOfBoundsException ex){
				throw new ConfigRuntimeException(ex.getMessage(), ExceptionType.IndexOverflowException, t);
			}
			return new CVoid(t);
		}

		public String getName() {
			return "array_insert";
		}

		public Integer[] numArgs() {
			return new Integer[]{3};
		}

		public String docs() {
			return "void {array, item, index} Inserts an item at the specified index, and shifts all other items in the array to the right one."
					+ " If index is greater than the size of the array, an IndexOverflowException is thrown, though the index may be equal"
					+ " to the size, in which case this works just like array_push. The array must be normal though, associative arrays"
					+ " are not supported.";
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

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			equals e = new equals();
			if (args[0] instanceof CArray) {
				CArray ca = (CArray) args[0];
				for (int i = 0; i < ca.size(); i++) {
					if (((CBoolean) e.exec(t, env, ca.get(i, t), args[1])).getBoolean()) {
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
			return "boolean {array, testValue} Checks to see if testValue is in array.";
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
				new ExampleScript("Demonstrates finding a string", "array_contains(array('a', 'b', 'c'), 'b')"),
			};
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
			return "boolean {array, testValue} Works like array_contains, except the comparison ignores case.";
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

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			equals_ic e = new equals_ic();
			if (args[0] instanceof CArray) {
				CArray ca = (CArray) args[0];
				for (int i = 0; i < ca.size(); i++) {
					if (((CBoolean) e.exec(t, environment, ca.get(i, t), args[1])).getBoolean()) {
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
				new ExampleScript("Demonstrates usage", "array_contains_ic(array('A', 'B', 'C'), 'd')"),
			};
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
			return "boolean {array, index} Checks to see if the specified array has an element at index";
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
			if (args[0] instanceof CArray) {
				if (!((CArray) args[0]).inAssociativeMode()) {
					try {
						int index = Static.getInt32(args[1], t);
						CArray ca = (CArray) args[0];
						return new CBoolean(index <= ca.size() - 1, t);
					} catch (ConfigRuntimeException e) {
						//They sent a key that is a string. Obviously it doesn't exist.
						return new CBoolean(false, t);
					}
				} else {
					CArray ca = (CArray) args[0];
					return new CBoolean(ca.containsKey(args[1].val()), t);
				}
			} else {
				throw new ConfigRuntimeException("Expecting argument 1 to be an array", ExceptionType.CastException, t);
			}
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Demonstrates a true condition", "array_index_exists(array(0, 1, 2), 0)"),
				new ExampleScript("Demonstrates a false condition", "array_index_exists(array(0, 1, 2), 3)"),
				new ExampleScript("Demonstrates an associative array", "array_index_exists(array(a: 'A', b: 'B'), 'a')"),
				new ExampleScript("Demonstrates an associative array", "array_index_exists(array(a: 'A', b: 'B'), 'c')"),
			};
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
			return "void {array, size, [fill]} Resizes the given array so that it is at least of size size, filling the blank spaces with"
					+ " fill, or null by default. If the size of the array is already at least size, nothing happens; in other words this"
					+ " function can only be used to increase the size of the array.";
			//+ " If the array is an associative array, the non numeric values are simply copied over.";
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

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			if (args[0] instanceof CArray && args[1] instanceof CInt) {
				CArray original = (CArray) args[0];
				int size = (int) ((CInt) args[1]).getInt();
				Construct fill = new CNull(t);
				if (args.length == 3) {
					fill = args[2];
				}
				for (long i = original.size(); i < size; i++) {
					original.push(fill);
				}
			} else {
				throw new ConfigRuntimeException("Argument 1 must be an array, and argument 2 must be an integer in array_resize", ExceptionType.CastException, t);
			}
			return new CVoid(t);
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Demonstrates basic usage", "assign(@array, array())\nmsg(@array)\narray_resize(@array, 2)\nmsg(@array)"),
				new ExampleScript("Demonstrates custom fill", "assign(@array, array())\nmsg(@array)\narray_resize(@array, 2, 'a')\nmsg(@array)"),
			};
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
			return "array {start, finish, [increment] | finish} Returns an array of numbers from start to (finish - 1)"
					+ " skipping increment integers per count. start defaults to 0, and increment defaults to 1. All inputs"
					+ " must be integers. If the input doesn't make sense, it will reasonably degrade, and return an empty array.";
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

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			long start = 0;
			long finish = 0;
			long increment = 1;
			if (args.length == 1) {
				finish = Static.getInt(args[0], t);
			} else if (args.length == 2) {
				start = Static.getInt(args[0], t);
				finish = Static.getInt(args[1], t);
			} else if (args.length == 3) {
				start = Static.getInt(args[0], t);
				finish = Static.getInt(args[1], t);
				increment = Static.getInt(args[2], t);
			}
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
				new ExampleScript("In reverse", "range(10, 0, -1)"),
			};
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
			return "array {array} Returns the keys in this array as a normal array. If the array passed in is already a normal array,"
					+ " the keys will be 0 -> (array_size(array) - 1)";
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
				new ExampleScript("With associative array", "array_keys(array(one: 'a', two: 'b', three: 'c'))"),
			};
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
			return "array {array} Returns a new normal array, given an associative array. (If the array passed in is not associative, a copy of the "
					+ " array is returned).";
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
				new ExampleScript("Usage with normal array", "array_normalize(array(1, 2, 3))"),
			};
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
			return "array {array1, array2, [arrayN...]} Merges the specified arrays from left to right, and returns a new array. If the array"
					+ " merged is associative, it will overwrite the keys from left to right, but if the arrays are normal, the keys are ignored,"
					+ " and values are simply pushed.";
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

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
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
				new ExampleScript("With overwrites", "array_merge(array(one: 1), array(one: 2), array(one: 3))"),				
			};
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
			return "mixed {array, index} Removes an index from an array. If the array is a normal"
					+ " array, all values' indicies are shifted left one. If the array is associative,"
					+ " the index is simply removed. If the index doesn't exist, the array remains"
					+ " unchanged. The value removed is returned.";
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

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if (args[0] instanceof CArray) {
				CArray ca = (CArray) args[0];
				return ca.remove(args[1]);
			} else {
				throw new ConfigRuntimeException("Argument 1 of array_remove should be an array", ExceptionType.CastException, t);
			}
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "assign(@array, array(1, 2, 3))\nmsg(array_remove(@array, 2))\nmsg(@array)"),
				new ExampleScript("With associative array", "assign(@array, array(one: 'a', two: 'b', three: 'c'))\nmsg(array_remove(@array, 'two'))\nmsg(@array)"),
			};
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
			return "string {array, [glue]} Given an array and glue, to-strings all the elements"
					+ " in the array (just the values, not the keys), and joins them with the glue, defaulting to a space. For instance"
					+ " array_implode(array(1, 2, 3), '-') will return \"1-2-3\".";
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
				Construct value = ca.get(key, t);
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
				new ExampleScript("With associative array", "array_implode(array(one: 'a', two: 'b', three: 'c'), '-')"),
			};
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
			return "slice {from, to} Dynamically creates an array slice, which can be used with array_get"
					+ " (or the [bracket notation]) to get a range of elements. cslice(0, 5) is equivalent"
					+ " to 0..5 directly in code, however with this function you can also do cslice(@var, @var),"
					+ " or other more complex expressions, which are not possible in static code.";
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
			return new CSlice(Static.getInt(args[0], t), Static.getInt(args[1], t), t);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "array(1, 2, 3)[cslice(0, 1)]"),
			};
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

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
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
			ca.sort(sortType);
			return ca;
		}

		public String getName() {
			return "array_sort";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "array {array, [sortType]} Sorts an array in place, and also returns a reference to the array. ---- The"
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

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
				OptimizationOption.OPTIMIZE_DYNAMIC
			);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
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
				new ExampleScript("Ignore case sort", "assign(@array, array('Zeta', 'zebra', 'Minecraft', 'mojang', 'Appliance', 'apple'))\narray_sort(@array, 'STRING_IC')\nmsg(@array)"),				
			};
		}
	}
	
	@api public static class array_sort_async extends AbstractFunction{
		
		RunnableQueue queue = new RunnableQueue("MethodScript-arraySortAsync");
		boolean started = false;
		
		private void startup(){
			if(!started){
				queue.invokeLater(null, new Runnable() {

					public void run() {
						//This warms up the queue. Apparently.
					}
				});
				StaticLayer.GetConvertor().addShutdownHook(new Runnable() {

					public void run() {
						queue.shutdown();
						started = false;
					}
				});
				started = true;
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

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			startup();
			final CArray array = Static.getArray(args[0], t);
			final CString sortType = new CString(args.length > 2?args[1].val():CArray.SortType.REGULAR.name(), t);
			final CClosure callback = Static.getObject((args.length==2?args[1]:args[2]), t, "closure", CClosure.class);
			queue.invokeLater(environment.getEnv(GlobalEnv.class).GetDaemonManager(), new Runnable() {

				public void run() {
					Construct c = new array_sort().exec(Target.UNKNOWN, null, array, sortType);
					callback.execute(new Construct[]{c});
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
			return "void {array, [sortType], closure(array)} Works like array_sort, but does the sort on another"
					+ " thread, then calls the closure and sends it the sorted array. This is useful if the array"
					+ " is large enough to actually \"stall\" the server when doing the sort. Sort type should be"
					+ " one of " + StringUtils.Join(CArray.SortType.values(), ", ", " or ");
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api public static class array_remove_values extends AbstractFunction{

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
			if(!(args[0] instanceof CArray)){
				throw new ConfigRuntimeException("Expected parameter 1 to be an array, but was " + args[0].val(), ExceptionType.CastException, t);
			}
			((CArray)args[0]).removeValues(args[1]);
			return new CVoid(t);
		}

		public String getName() {
			return "array_remove_values";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "void {array, value} Removes all instances of value from the specified array."
					+ " For instance, array_remove_values(array(1, 2, 2, 3), 2) would produce the"
					+ " array(1, 3). Note that it returns void however, so it will simply in place"
					+ " modify the array passed in, much like array_remove.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "assign(@array, array(1, 2, 2, 3))\nmsg(@array)\narray_remove_values(@array, 2)\nmsg(@array)"),				
			};
		}
		
	}
	
	@api public static class array_indexes extends AbstractFunction{

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
			if(!(args[0] instanceof CArray)){
				throw new ConfigRuntimeException("Expected parameter 1 to be an array, but was " + args[0].val(), ExceptionType.CastException, t);
			}
			return ((CArray)args[0]).indexesOf(args[1]);
		}

		public String getName() {
			return "array_indexes";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "array {array, value} Returns an array with all the keys of the specified array"
					+ " at which the specified value is equal. That is, for the array(1, 2, 2, 3), if"
					+ " value were 2, would return array(1, 2). If the value cannot be found in the"
					+ " array at all, an empty array will be returned.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "assign(@array, array(1, 2, 2, 3))\nmsg(array_indexes(@array, 2))"),				
				new ExampleScript("Not found", "assign(@array, array(1, 2, 2, 3))\nmsg(array_indexes(@array, 5))"),				
			};
		}
		
	}
	
	@api public static class array_index extends AbstractFunction{

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
			CArray ca = (CArray)new array_indexes().exec(t, environment, args);
			if(ca.isEmpty()){
				return new CNull(t);
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
			return "mixed {array, value} Works exactly like array_indexes(array, value)[0], except in the case where"
					+ " the value is not found, returns null. That is to say, if the value is contained in an"
					+ " array (even multiple times) the index of the first element is returned.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "assign(@array, array(1, 2, 2, 3))\nmsg(array_index(@array, 2))"),				
				new ExampleScript("Not found", "assign(@array, array(1, 2, 2, 3))\nmsg(array_index(@array, 5))"),				
			};
		}
		
	}
	
	@api
	public static class array_reverse extends AbstractFunction{

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
			if(args[0] instanceof CArray){
				((CArray)args[0]).reverse();
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
			return "void {array} Reverses an array in place. However, if the array is associative, throws a CastException, since associative"
					+ " arrays are more like a map.";
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
	
	@api public static class array_rand extends AbstractFunction{

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
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			long number = 1;
			boolean getKeys = true;
			CArray array = Static.getArray(args[0], t);
			CArray newArray = new CArray(t);
			if(array.isEmpty()){
				return newArray;
			}
			if(args.length > 1){
				number = Static.getInt(args[1], t);
			}
			if(number < 1){
				throw new ConfigRuntimeException("number may not be less than 1.", ExceptionType.RangeException, t);
			}
			if(number > Integer.MAX_VALUE){
				throw new ConfigRuntimeException("Overflow detected. Number cannot be larger than " + Integer.MAX_VALUE, ExceptionType.RangeException, t);
			}
			if(args.length > 2){
				getKeys = Static.getBoolean(args[2]);
			}
			
			LinkedHashSet<Integer> randoms = new LinkedHashSet<Integer>();
			while(randoms.size() < number){
				randoms.add(java.lang.Math.abs(r.nextInt() % (int)array.size()));
			}
			List<String> keySet = new ArrayList<String>(array.keySet());
			for(Integer i : randoms){
				if(getKeys){
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
			return "array {array, [number, [getKeys]]} Returns a random selection of keys or values from an array. The array may be"
					+ " either normal or associative. Number defaults to 1, and getKey defaults to true. If number is greater than"
					+ " the size of the array, a RangeException is thrown. No value will be returned twice from the array however, one it"
					+ " is \"drawn\" from the array, it is not placed back in. The order of the elements in the array will also be random,"
					+ " if order is important, use array_sort().";
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
					"assign(@array, array(one: 'a', two: 'b', three: 'c', four: 'd', five: 'e'))\nmsg(array_rand(@array))"),
			};
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

		public Construct exec(final Target t, final Environment environment, Construct... args) throws ConfigRuntimeException {
			CArray array = Static.getArray(args[0], t);
			boolean compareTypes = true;
			if(args.length == 2){
				compareTypes = Static.getBoolean(args[1]);
			}
			final boolean fCompareTypes = compareTypes;
			if(array.inAssociativeMode()){
				return array.clone();
			} else {
				List<Construct> asList = array.asList();
				CArray newArray = new CArray(t);
				Set<Construct> set = new LinkedComparatorSet<Construct>(asList, new LinkedComparatorSet.EqualsComparator<Construct>() {

					public boolean checkIfEquals(Construct item1, Construct item2) {
						return (fCompareTypes && Static.getBoolean(sequals.exec(t, environment, item1, item2)))
								|| (!fCompareTypes && Static.getBoolean(equals.exec(t, environment, item1, item2)));
					}
				});
				for(Construct c : set){
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
			return "array {array, [compareTypes]} Removes all non-unique values from an array. ---- compareTypes is true by default, which means that in the array"
					+ " array(1, '1'), nothing would be removed from the array, since both values are different data types. However, if compareTypes is false,"
					+ " then the first value would remain, but the second value would be removed. A new array is returned. If the array is associative, by definition,"
					+ " there are no unique values, so a clone of the array is returned.";
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
