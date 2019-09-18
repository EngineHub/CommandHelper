package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Version;
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
import com.laytonsmith.core.compiler.OptimizationUtilities;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CSymbol;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREInsufficientArgumentsException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 */
@core
public class BasicLogic {

	public static String docs() {
		return "These functions provide basic logical operations.";
	}

	@api
	@seealso({nequals.class, sequals.class, snequals.class})
	@OperatorPreferred("==")
	public static class equals extends AbstractFunction implements Optimizable {

		private static final equals SELF = new equals();

		/**
		 * Returns the results that this function would provide, but in a java specific manner, so other code may easily
		 * determine how this method would respond.
		 *
		 * @param one
		 * @param two
		 * @return
		 */
		public static boolean doEquals(Mixed one, Mixed two) {
			CBoolean ret = SELF.exec(Target.UNKNOWN, null, one, two);
			return ret.getBoolean();
		}

		@Override
		public String getName() {
			return "equals";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public CBoolean exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			if(args.length <= 1) {
				throw new CREInsufficientArgumentsException("At least two arguments must be passed to equals", t);
			}
			boolean referenceMatch = true;
			for(int i = 0; i < args.length - 1; i++) {
				if(args[i] != args[i + 1]) {
					referenceMatch = false;
					break;
				}
			}
			if(referenceMatch) {
				return CBoolean.TRUE;
			}
			if(Static.anyNulls(args)) {
				boolean equals = true;
				for(Mixed c : args) {
					if(!(c instanceof CNull)) {
						equals = false;
					}
				}
				return CBoolean.get(equals);
			}
			if(Static.anyBooleans(args)) {
				boolean equals = true;
				for(int i = 1; i < args.length; i++) {
					boolean arg1 = ArgumentValidation.getBoolean(args[i - 1], t);
					boolean arg2 = ArgumentValidation.getBoolean(args[i], t);
					if(arg1 != arg2) {
						equals = false;
						break;
					}
				}
				return CBoolean.get(equals);
			}

			{
				boolean equals = true;
				for(int i = 1; i < args.length; i++) {
					if(!args[i - 1].val().equals(args[i].val())) {
						equals = false;
						break;
					}
				}
				if(equals) {
					return CBoolean.TRUE;
				}
			}
			try {
				// Validate that these are numbers, so that getNumber doesn't throw an exception.
				if(!ArgumentValidation.isNumber(args[0])) {
					return CBoolean.FALSE;
				}
				for(int i = 1; i < args.length; i++) {
					if(!ArgumentValidation.isNumber(args[i])) {
						return CBoolean.FALSE;
					}
					double arg1 = Static.getNumber(args[i - 1], t);
					double arg2 = Static.getNumber(args[i], t);
					if(arg1 != arg2) {
						return CBoolean.FALSE;
					}
				}
				return CBoolean.TRUE;
			} catch (ConfigRuntimeException e) {
				return CBoolean.FALSE;
			}
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInsufficientArgumentsException.class};
		}

		@Override
		public String docs() {
			return "boolean {var1, var2[, varX...]} Returns true or false if all the arguments are equal. Operator syntax is"
					+ " also supported: @a == @b";
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
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Functional usage", "equals(1, 1.0, '1')"),
				new ExampleScript("Operator syntax", "1 == 1"),
				new ExampleScript("Not equivalent", "'one' == 'two'")};
		}
	}

	@api
	@seealso({equals.class, nequals.class, snequals.class})
	@OperatorPreferred("===")
	public static class sequals extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "sequals";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {val1, val2} Uses a strict equals check, which determines if"
					+ " two values are not only equal, but also the same type. So, while"
					+ " equals('1', 1) returns true, sequals('1', 1) returns false, because"
					+ " the first one is a string, and the second one is an int. More often"
					+ " than not, you want to use plain equals(). In addition, type juggling is"
					+ " explicitely not performed on strings. Thus '2' !== '2.0', despite those"
					+ " being ==. Operator syntax is also"
					+ " supported: @a === @b";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
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
		public CBoolean exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(args.length != 2) {
				throw new CREFormatException(this.getName() + " expects 2 arguments.", t);
			}
			if(args[1].typeof().equals(args[0].typeof())) {
				if(args[0].isInstanceOf(CString.TYPE) && args[1].isInstanceOf(CString.TYPE)) {
					// Check for actual string equality, so we don't do type massaging
					// for numeric strings. Thus '2' !== '2.0'
					return CBoolean.get(args[0].val().equals(args[1].val()));
				}
				return new equals().exec(t, environment, args);
			} else {
				return CBoolean.FALSE;
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
				new ExampleScript("Functional usage", "sequals('1', 1)"),
				new ExampleScript("Symbolic usage", "'1' === 1"),
				new ExampleScript("Symbolic usage", "'1' === '1'")};
		}
	}

	@api
	@seealso({sequals.class})
	@OperatorPreferred("!==")
	public static class snequals extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "snequals";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {val1, val2} Equivalent to not(sequals(val1, val2)). Operator syntax"
					+ " is also supported: @a !== @b";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return new sequals().exec(t, environment, args).not();
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

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "snequals('1', 1)"),
				new ExampleScript("Basic usage", "snequals('1', '1')"),
				new ExampleScript("Operator syntax", "'1' !== '1'"),
				new ExampleScript("Operator syntax", "'1' !== 1")};
		}
	}

	@api
	@seealso({equals.class, sequals.class, snequals.class})
	@OperatorPreferred("!=")
	public static class nequals extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "nequals";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {val1, val2} Returns true if the two values are NOT equal, or false"
					+ " otherwise. Equivalent to not(equals(val1, val2)). Operator syntax is also"
					+ " supported: @a != @b";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
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
		public CBoolean exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return new equals().exec(t, env, args).not();
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
				new ExampleScript("Basic usage", "nequals('one', 'two')"),
				new ExampleScript("Basic usage", "nequals(1, 1)"),
				new ExampleScript("Operator syntax", "1 != 1"),
				new ExampleScript("Operator syntax", "1 != 2")};
		}
	}

	@api
	public static class equals_ic extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "equals_ic";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "boolean {val1, val2[, valX...]} Returns true if all the values are equal to each other, while"
					+ " ignoring case.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInsufficientArgumentsException.class};
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
		public CBoolean exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			if(args.length <= 1) {
				throw new CREInsufficientArgumentsException("At least two arguments must be passed to equals_ic", t);
			}
			if(Static.anyBooleans(args)) {
				boolean equals = true;
				for(int i = 1; i < args.length; i++) {
					boolean arg1 = ArgumentValidation.getBoolean(args[i - 1], t);
					boolean arg2 = ArgumentValidation.getBoolean(args[i], t);
					if(arg1 != arg2) {
						equals = false;
						break;
					}
				}
				return CBoolean.get(equals);
			}

			{
				boolean equals = true;
				for(int i = 1; i < args.length; i++) {
					if(!args[i - 1].val().equalsIgnoreCase(args[i].val())) {
						equals = false;
						break;
					}
				}
				if(equals) {
					return CBoolean.TRUE;
				}
			}
			try {
				// Validate that these are numbers, so that getNumber doesn't throw an exception.
				if(!ArgumentValidation.isNumber(args[0])) {
					return CBoolean.FALSE;
				}
				boolean equals = true;
				for(int i = 1; i < args.length; i++) {
					if(!ArgumentValidation.isNumber(args[i])) {
						return CBoolean.FALSE;
					}
					double arg1 = Static.getNumber(args[i - 1], t);
					double arg2 = Static.getNumber(args[i], t);
					if(arg1 != arg2) {
						return CBoolean.FALSE;
					}
				}
				return CBoolean.TRUE;
			} catch (ConfigRuntimeException e) {
				return CBoolean.FALSE;
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
				new ExampleScript("Basic usage", "equals_ic('test', 'TEST')"),
				new ExampleScript("Basic usage", "equals_ic('completely', 'DIFFERENT')")};
		}
	}

	@api
	public static class sequals_ic extends AbstractFunction implements Optimizable {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
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
		public CBoolean exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Mixed v1 = args[0];
			Mixed v2 = args[1];
			if(!v2.getClass().equals(v1.getClass())) {
				return CBoolean.FALSE;
			}
			return new equals_ic().exec(t, environment, v1, v2);
		}

		@Override
		public String getName() {
			return "sequals_ic";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {value1, value2} Returns true if the values are the same type, as well as equal, according to equals_ic."
					+ " Generally, equals_ic will suffice, because usually you will be comparing two strings, however, this function"
					+ " may be useful in various other cases, perhaps where the datatypes are unknown, but could be strings.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
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
				new ExampleScript("Basic usage", "sequals_ic(1, 1)"),
				new ExampleScript("False result", "sequals_ic('1', 1)"),
				new ExampleScript("False result", "sequals_ic('false', true)")
			};
		}

	}

	@api
	@seealso({equals_ic.class})
	public static class nequals_ic extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "nequals_ic";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {val1, val2} Returns true if the two values are NOT equal to each other, while"
					+ " ignoring case.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
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
		public CBoolean exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return new equals_ic().exec(t, environment, args).not();
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
				new ExampleScript("Basic usage", "equals_ic('test', 'TEST')"),
				new ExampleScript("Basic usage", "equals_ic('completely', 'DIFFERENT')")};
		}
	}

	@api
	public static class ref_equals extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
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
		public CBoolean exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(args[0].isInstanceOf(CArray.TYPE) && args[1].isInstanceOf(CArray.TYPE)) {
				return CBoolean.get(args[0] == args[1]);
			} else {
				return new equals().exec(t, environment, args);
			}
		}

		@Override
		public String getName() {
			return "ref_equals";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {val1, val2} Returns true if and only if the two values are actually the same reference."
					+ " Primitives that are equal will always be the same reference, this method is only useful for"
					+ " object/array comparisons.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Usage with primitives", "msg(ref_equals(1, 1))\n"
				+ "msg(ref_equals(1, 2))"),
				new ExampleScript("Usage with arrays that are the same reference", "@a = array(1, 2, 3)\n"
				+ "@b = @a\n"
				+ "msg(ref_equals(@a, @b)) # Note that an assignment simply sets it to reference the same underlying object, so this is true"),
				new ExampleScript("Usage with a cloned array", "@a = array(1, 2, 3)\n"
				+ "@b = @a[] # Clone the array\n"
				+ "msg(ref_equals(@a, @b)) # False, because although the arrays are == (and ===) they are different references"),
				new ExampleScript("Usage with a duplicated array", "@a = array(1, 2, 3)\n"
				+ "@b = array(1, 2, 3) # New array with duplicate content\n"
				+ "msg(ref_equals(@a, @b)) # Again, even though @a == @b and @a === @b, this is false, because they are two different references")};
		}

	}

	@api
	@seealso({gt.class, lte.class, gte.class})
	@OperatorPreferred("<")
	public static class lt extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "lt";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public CBoolean exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			if(args.length != 2) {
				throw new CREFormatException(this.getName() + " expects 2 arguments.", t);
			}
			double arg1 = Static.getNumber(args[0], t);
			double arg2 = Static.getNumber(args[1], t);
			return CBoolean.get(arg1 < arg2);
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public String docs() {
			return "boolean {var1, var2} Returns the results of a less than operation. Operator syntax"
					+ " is also supported: @a &lt; @b";
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
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Functional usage", "lt(4, 5)"),
				new ExampleScript("Operator syntax, true condition", "4 < 5"),
				new ExampleScript("Operator syntax, false condition", "5 < 4")};
		}
	}

	@api
	@seealso({lt.class, lte.class, gte.class})
	@OperatorPreferred(">")
	public static class gt extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "gt";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public CBoolean exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			if(args.length != 2) {
				throw new CREFormatException(this.getName() + " expects 2 arguments.", t);
			}
			double arg1 = Static.getNumber(args[0], t);
			double arg2 = Static.getNumber(args[1], t);
			return CBoolean.get(arg1 > arg2);
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public String docs() {
			return "boolean {var1, var2} Returns the result of a greater than operation. Operator syntax is also supported:"
					+ " @a &gt; @b";
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
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Functional usage", "gt(5, 4)"),
				new ExampleScript("Operator syntax, true condition", "5 > 4"),
				new ExampleScript("Operator syntax, false condition", "4 > 5")};
		}
	}

	@api
	@seealso({lt.class, gt.class, gte.class})
	@OperatorPreferred("<=")
	public static class lte extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "lte";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public CBoolean exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			if(args.length != 2) {
				throw new CREFormatException(this.getName() + " expects 2 arguments.", t);
			}
			double arg1 = Static.getNumber(args[0], t);
			double arg2 = Static.getNumber(args[1], t);
			return CBoolean.get(arg1 <= arg2);
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public String docs() {
			return "boolean {var1, var2} Returns the result of a less than or equal to operation. Operator"
					+ " syntax is also supported: @a &lt;= @b";
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
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Functional usage", "lte(4, 5)"),
				new ExampleScript("Operator syntax, true condition", "4 <= 5"),
				new ExampleScript("Operator syntax, true condition", "5 <= 5"),
				new ExampleScript("Operator syntax, false condition", "5 <= 4")};
		}
	}

	@api
	@seealso({lt.class, gt.class, lte.class})
	@OperatorPreferred(">=")
	public static class gte extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "gte";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public CBoolean exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			if(args.length != 2) {
				throw new CREFormatException(this.getName() + " expects 2 arguments.", t);
			}
			double arg1 = Static.getNumber(args[0], t);
			double arg2 = Static.getNumber(args[1], t);
			return CBoolean.get(arg1 >= arg2);
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public String docs() {
			return "boolean {var1, var2} Returns the result of a greater than or equal to operation. Operator"
					+ " sytnax is also supported: @a &gt;= @b";
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
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Functional usage", "gte(5, 4)"),
				new ExampleScript("Operator syntax, true condition", "4 >= 4"),
				new ExampleScript("Operator syntax, false condition", "4 >= 5")};
		}
	}

	@api(environments = {GlobalEnv.class})
	@seealso({or.class})
	@OperatorPreferred("&&")
	public static class and extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "and";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public CBoolean exec(Target t, Environment env, Mixed... args) {
			//This will only happen if they hardcode true/false in, but we still
			//need to handle it appropriately.
			for(Mixed c : args) {
				if(!ArgumentValidation.getBoolean(c, t)) {
					return CBoolean.FALSE;
				}
			}
			return CBoolean.TRUE;
		}

		@Override
		public CBoolean execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			for(ParseTree tree : nodes) {
				Mixed c = env.getEnv(GlobalEnv.class).GetScript().seval(tree, env);
				boolean b = ArgumentValidation.getBoolean(c, t);
				if(b == false) {
					return CBoolean.FALSE;
				}
			}
			return CBoolean.TRUE;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public String docs() {
			return "boolean {var1, [var2...]} Returns the boolean value of a logical AND across all arguments. Uses lazy determination, so once "
					+ "an argument returns false, the function returns. Operator syntax is supported:"
					+ " @a && @b";
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
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			OptimizationUtilities.pullUpLikeFunctions(children, getName());
			Iterator<ParseTree> it = children.iterator();
			boolean foundFalse = false;
			while(it.hasNext()) {
				//Remove hard coded true values, they won't affect the calculation at all
				//Also walk through the children, and if we find a hardcoded false, discard all the following values.
				//If we do find a hardcoded false, though we can know ahead of time that this statement as a whole
				//will be false, we can't remove everything, as the parameters beforehand may have side effects, so
				//we musn't remove them.
				ParseTree child = it.next();
				if(foundFalse) {
					it.remove();
					continue;
				}
				if(child.isConst()) {
					if(ArgumentValidation.getBoolean(child.getData(), t) == true) {
						it.remove();
					} else {
						foundFalse = true;
					}
				}
			}
			// TODO: Can't do this yet, because children of side effect free functions may still have side effects that
			// we need to maintain. However, with complications introduced by code branch functions, we can't process
			// this yet.
//			if(foundFalse){
//				//However, we can remove any functions that have no side effects that come before the false.
//				it = children.iterator();
//				while(it.hasNext()){
//					Mixed data = it.next().getData();
//					if(data instanceof CFunction && ((CFunction)data).getFunction() instanceof Optimizable){
//						if(((Optimizable)((CFunction)data).getFunction()).optimizationOptions().contains(OptimizationOption.NO_SIDE_EFFECTS)){
//							it.remove();
//						}
//					}
//				}
//			}
			// At this point, it could be that there are some conditions with side effects, followed by a final false. However,
			// if false is the only remaining condition (which could be) then we can simply return false here.
			if(children.size() == 1 && children.get(0).isConst() && ArgumentValidation.getBoolean(children.get(0).getData(), t) == false) {
				return new ParseTree(CBoolean.FALSE, fileOptions);
			}
			if(children.isEmpty()) {
				//We've removed all the children, so return true, because they were all true.
				return new ParseTree(CBoolean.TRUE, fileOptions);
			}
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Functional usage", "and(true, true)"),
				new ExampleScript("Operator syntax, true condition", "true && true"),
				new ExampleScript("Operator syntax, false condition", "true && false"),
				new ExampleScript("Short circuit", "false && msg('This will not show')")};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC, OptimizationOption.CONSTANT_OFFLINE);
		}
	}

	@api
	public static class dand extends AbstractFunction implements Optimizable {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return CVoid.VOID;
		}

		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			for(ParseTree tree : nodes) {
				Mixed c = env.getEnv(GlobalEnv.class).GetScript().seval(tree, env);
				if(!ArgumentValidation.getBoolean(c, t)) {
					return c;
				}
			}
			return CBoolean.TRUE;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			OptimizationUtilities.pullUpLikeFunctions(children, getName());
			Iterator<ParseTree> it = children.iterator();
			boolean foundFalse = false;
			while(it.hasNext()) {
				//Remove hard coded true values, they won't affect the calculation at all
				//Also walk through the children, and if we find a hardcoded false, discard all the following values.
				//If we do find a hardcoded false, though we can know ahead of time that this statement as a whole
				//will be false, we can't remove everything, as the parameters beforehand may have side effects, so
				//we musn't remove them.
				ParseTree child = it.next();
				if(foundFalse) {
					it.remove();
					continue;
				}
				if(child.isConst()) {
					if(ArgumentValidation.getBoolean(child.getData(), t) == true) {
						it.remove();
					} else {
						foundFalse = true;
					}
				}
			}
			// TODO: Can't do this yet, because children of side effect free functions may still have side effects that
			// we need to maintain. However, with complications introduced by code branch functions, we can't process
			// this yet.
//			if(foundFalse){
//				//However, we can remove any functions that have no side effects that come before the false.
//				it = children.iterator();
//				while(it.hasNext()){
//					Mixed data = it.next().getData();
//					if(data instanceof CFunction && ((CFunction)data).getFunction() instanceof Optimizable){
//						if(((Optimizable)((CFunction)data).getFunction()).optimizationOptions().contains(OptimizationOption.NO_SIDE_EFFECTS)){
//							it.remove();
//						}
//					}
//				}
//			}
			// At this point, it could be that there are some conditions with side effects, followed by a final false. However,
			// if false is the only remaining condition (which could be) then we can simply return false here.
			if(children.size() == 1 && children.get(0).isConst() && ArgumentValidation.getBoolean(children.get(0).getData(), t) == false) {
				return new ParseTree(children.get(0).getData(), fileOptions);
			}
			if(children.isEmpty()) {
				//We've removed all the children, so return true, because they were all true.
				return new ParseTree(CBoolean.TRUE, fileOptions);
			}
			return null;
		}

		@Override
		public String getName() {
			return "dand";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "mixed {...} Returns the first false value. The arguments to this function are lazily evaluated, so"
					+ " if the first value evaluates to false, the rest of the arguments will not be evaluated."
					+ " If none of the values are false, true is returned. Usage of"
					+ " the operator is preferred: &&&";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC, OptimizationOption.CONSTANT_OFFLINE);
		}

	}

	@api(environments = {GlobalEnv.class})
	@seealso({and.class})
	@OperatorPreferred("||")
	public static class or extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "or";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public CBoolean exec(Target t, Environment env, Mixed... args) {
			//This will only happen if they hardcode true/false in, but we still
			//need to handle it appropriately.
			for(Mixed c : args) {
				if(ArgumentValidation.getBoolean(c, t)) {
					return CBoolean.TRUE;
				}
			}
			return CBoolean.FALSE;
		}

		@Override
		public CBoolean execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			for(ParseTree tree : nodes) {
				Mixed c = env.getEnv(GlobalEnv.class).GetScript().seval(tree, env);
				if(ArgumentValidation.getBoolean(c, t)) {
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
			return "boolean {var1, [var2...]} Returns the boolean value of a logical OR across all arguments. Uses lazy"
					+ " determination, so once an argument resolves to true, the function returns. Operator syntax is also"
					+ " supported: @a || @b";
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
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			OptimizationUtilities.pullUpLikeFunctions(children, getName());
			Iterator<ParseTree> it = children.iterator();
			boolean foundTrue = false;
			while(it.hasNext()) {
				//Remove hard coded false values, they won't affect the calculation at all
				//Also walk through the children, and if we find a hardcoded true, discard all the following values.
				//If we do find a hardcoded true, though we can know ahead of time that this statement as a whole
				//will be true, we can't remove everything, as the parameters beforehand may have side effects, so
				//we musn't remove them.
				ParseTree child = it.next();
				if(foundTrue) {
					it.remove();
					continue;
				}
				if(child.isConst()) {
					if(child.getData() instanceof CSymbol) {
						throw new ConfigCompileException("Unexpected symbol: \"" + child.getData().val() + "\"", t);
					}
					if(ArgumentValidation.getBoolean(child.getData(), t) == false) {
						it.remove();
					} else {
						foundTrue = true;
					}
				}
			}
			// TODO: Can't do this yet, because children of side effect free functions may still have side effects that
			// we need to maintain. However, with complications introduced by code branch functions, we can't process
			// this yet.
//			if(foundTrue){
//				//However, we can remove any functions that have no side effects that come before the true.
//				it = children.iterator();
//				while(it.hasNext()){
//					Mixed data = it.next().getData();
//					if(data instanceof CFunction && ((CFunction)data).getFunction() instanceof Optimizable){
//						if(((Optimizable)((CFunction)data).getFunction()).optimizationOptions().contains(OptimizationOption.NO_SIDE_EFFECTS)){
//							it.remove();
//						}
//					}
//				}
//			}
			// At this point, it could be that there are some conditions with side effects, followed by a final true. However,
			// if true is the only remaining condition (which could be) then we can simply return true here.
			if(children.size() == 1 && children.get(0).isConst() && ArgumentValidation.getBoolean(children.get(0).getData(), t) == true) {
				return new ParseTree(CBoolean.TRUE, fileOptions);
			}
			if(children.isEmpty()) {
				//We've removed all the children, so return false, because they were all false.
				return new ParseTree(CBoolean.FALSE, fileOptions);
			}
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Functional usage", "or(false, true)"),
				new ExampleScript("Operator syntax, true condition", "true || false"),
				new ExampleScript("Operator syntax, false condition", "false || false"),
				new ExampleScript("Short circuit", "true || msg('This will not show')")};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC, OptimizationOption.CONSTANT_OFFLINE);
		}
	}

	@api
	public static class dor extends AbstractFunction implements Optimizable {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return CVoid.VOID;
		}

		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			for(ParseTree tree : nodes) {
				Mixed c = env.getEnv(GlobalEnv.class).GetScript().seval(tree, env);
				if(ArgumentValidation.getBooleanish(c, t)) {
					return c;
				}
			}
			return env.getEnv(GlobalEnv.class).GetScript().seval(nodes[nodes.length - 1], env);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			OptimizationUtilities.pullUpLikeFunctions(children, getName());
			return null;
		}

		@Override
		public String getName() {
			return "dor";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "mixed {...} Returns the first true value. The arguments to this function are lazily evaluated, so"
					+ " if the first value evaluates to true, the rest of the arguments will not be evaluated."
					+ " If none of the values are true, the last value is returned (which will be falsy). Usage of"
					+ " the operator is preferred: |||";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage with default used", "@a = '';\n@b = @a ||| 'default';\nmsg(@b);"),
				new ExampleScript("Basic usage with first value used", "@a = 'value is set';\n@b = @a ||| 'default';\nmsg(@b);")
			};
		}

	}

	@api
	@OperatorPreferred("!")
	public static class not extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "not";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public CBoolean exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			if(args.length != 1) {
				throw new CREFormatException(this.getName() + " expects 1 argument.", t);
			}
			return CBoolean.get(!ArgumentValidation.getBoolean(args[0], t));
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public String docs() {
			return "boolean {var1} Returns the boolean value of a logical NOT for this argument. Operator syntax is also supported: !@var";
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
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN,
					OptimizationOption.OPTIMIZE_DYNAMIC
			);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env, Set<Class<? extends Environment.EnvironmentImpl>> envs, List<ParseTree> children, FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			if(CFunction.IsFunction(children.get(0), not.class)) {
				// not(not(val)) == val
				return children.get(0).getChildAt(0);
			}
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Functional usage", "not(false)"),
				new ExampleScript("Operator syntax, true condition", "!false"),
				new ExampleScript("Operator syntax, false condition", "!true"),
				new ExampleScript("Operator syntax, using variable", "boolean @var = false;\nmsg(!@var);")};
		}
	}

	@api
	public static class xor extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "xor";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {val1, val2} Returns the xor of the two values.";
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
		public CBoolean exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(args.length != 2) {
				throw new CREFormatException(this.getName() + " expects 2 arguments.", t);
			}
			boolean val1 = ArgumentValidation.getBoolean(args[0], t);
			boolean val2 = ArgumentValidation.getBoolean(args[1], t);
			return CBoolean.get(val1 ^ val2);
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
				new ExampleScript("One of each", "xor(true, false)"),
				new ExampleScript("One of each", "xor(false, true)"),
				new ExampleScript("Both true", "xor(true, true)"),
				new ExampleScript("Both false", "xor(false, false)")
			};
		}
	}

	@api
	@seealso({and.class})
	public static class nand extends AbstractFunction {

		@Override
		public String getName() {
			return "nand";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "boolean {val1, [val2...]} Return the equivalent of not(and())";
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
		public Mixed exec(Target t, Environment environment, Mixed... args) {
			return CNull.NULL;
		}

		@Override
		public CBoolean execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			return new and().execs(t, env, parent, nodes).not();
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "nand(true, true)")};
		}
	}

	@api
	@seealso({or.class})
	public static class nor extends AbstractFunction {

		@Override
		public String getName() {
			return "nor";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "boolean {val1, [val2...]} Returns the equivalent of not(or())";
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
		public Mixed exec(Target t, Environment environment, Mixed... args) {
			return CNull.NULL;
		}

		@Override
		public CBoolean execs(Target t, Environment environment, Script parent, ParseTree... args) throws ConfigRuntimeException {
			return new or().execs(t, environment, parent, args).not();
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "nor(true, false)")};
		}
	}

	@api
	@seealso({xor.class})
	public static class xnor extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "xnor";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {val1, val2} Returns the xnor of the two values";
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
		public CBoolean exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(args.length != 2) {
				throw new CREFormatException(this.getName() + " expects 2 arguments.", t);
			}
			return new xor().exec(t, environment, args).not();
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
				new ExampleScript("Two true values", "xnor(true, true)"),
				new ExampleScript("One of each", "xnor(true, false)"),
				new ExampleScript("One of each", "xnor(false, true)"),
				new ExampleScript("Two false", "xnor(false, false)")
			};
		}
	}

	@api
	public static class bit_and extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "bit_and";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "int {int1, int2, [int3...]} Returns the bitwise AND of the values";
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
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public CInt exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(args.length < 2) {
				throw new CREFormatException(this.getName() + " expects at least 2 arguments.", t);
			}
			long val = Static.getInt(args[0], t);
			for(int i = 1; i < args.length; i++) {
				val = val & Static.getInt(args[i], t);
			}
			return new CInt(val, t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN,
					OptimizationOption.OPTIMIZE_DYNAMIC
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "bit_and(1, 2, 4)"),
				new ExampleScript("Usage in masking applications. Note that 5 in binary is 101 and 4 is 100. (See bit_or for a more complete example.)",
				"assign(@var, 5)\nif(bit_and(@var, 4),\n\tmsg('Third bit set')\n)")};
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() < 2) {
				throw new ConfigCompileException("bit_and() requires at least 2 arguments.", t);
			}
			return null;
		}
	}

	@api
	public static class bit_or extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "bit_or";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "int {int1, int2, [int3...]} Returns the bitwise OR of the specified values";
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
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public CInt exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(args.length < 2) {
				throw new CREFormatException(this.getName() + " expects at least 2 arguments.", t);
			}
			long val = Static.getInt(args[0], t);
			for(int i = 1; i < args.length; i++) {
				val = val | Static.getInt(args[i], t);
			}
			return new CInt(val, t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN,
					OptimizationOption.OPTIMIZE_DYNAMIC
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "bit_or(1, 2, 4)"),
				new ExampleScript("Usage in masking applications. (Used to create a mask)", "assign(@flag1, 1)\nassign(@flag2, 2)\nassign(@flag3, 4)\n"
				+ "assign(@flags, bit_or(@flag1, @flag3))\n"
				+ "if(bit_and(@flags, @flag1),\n\tmsg('Contains flag 1')\n)\n"
				+ "if(!bit_and(@flags, @flag2),\n\tmsg('Does not contain flag 2')\n)")};
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() < 2) {
				throw new ConfigCompileException("bit_or() requires at least 2 arguments.", t);
			}
			return null;
		}
	}

	@api
	public static class bit_xor extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "bit_xor";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "int {int1, int2, [int3...]} Returns the bitwise exclusive OR of the specified values";
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
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public CInt exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(args.length < 2) {
				throw new CREFormatException(this.getName() + " expects at least 2 arguments.", t);
			}
			long val = Static.getInt(args[0], t);
			for(int i = 1; i < args.length; i++) {
				val = val ^ Static.getInt(args[i], t);
			}
			return new CInt(val, t);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN,
					OptimizationOption.OPTIMIZE_DYNAMIC
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "bit_xor(1, 2, 4)")};
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() < 2) {
				throw new ConfigCompileException("bit_xor() requires at least 2 arguments.", t);
			}
			return null;
		}
	}

	@api
	public static class bit_not extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "bit_not";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "int {int1} Returns the bitwise NOT of the given value";
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
		public CInt exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(args.length != 1) {
				throw new CREFormatException(this.getName() + " expects 1 argument.", t);
			}
			return new CInt(~Static.getInt(args[0], t), t);
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
				new ExampleScript("Basic usage", "bit_not(1)")};
		}
	}

	@api
	public static class lshift extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "lshift";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "int {value, bitsToShift} Left shifts the value bitsToShift times";
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
		public CInt exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(args.length != 2) {
				throw new CREFormatException(this.getName() + " expects 2 arguments.", t);
			}
			long value = Static.getInt(args[0], t);
			long toShift = Static.getInt(args[1], t);
			return new CInt(value << toShift, t);
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
				new ExampleScript("Basic usage", "lshift(1, 1)")};
		}
	}

	@api
	public static class rshift extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "rshift";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "int {value, bitsToShift} Right shifts the value bitsToShift times";
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
		public CInt exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(args.length != 2) {
				throw new CREFormatException(this.getName() + " expects 2 arguments.", t);
			}
			long value = Static.getInt(args[0], t);
			long toShift = Static.getInt(args[1], t);
			return new CInt(value >> toShift, t);
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
				new ExampleScript("Basic usage", "rshift(2, 1)"),
				new ExampleScript("Basic usage", "rshift(-2, 1)")};
		}
	}

	@api
	public static class urshift extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "urshift";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "int {value, bitsToShift} Right shifts value bitsToShift times, pushing a 0, making"
					+ " this an unsigned right shift.";
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
		public CInt exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(args.length != 2) {
				throw new CREFormatException(this.getName() + " expects 2 arguments.", t);
			}
			long value = Static.getInt(args[0], t);
			long toShift = Static.getInt(args[1], t);
			return new CInt(value >>> toShift, t);
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
				new ExampleScript("Basic usage", "urshift(2, 1)"),
				new ExampleScript("Basic usage", "urshift(-2, 1)")};
		}
	}

	@api
	public static class compile_error extends AbstractFunction implements Optimizable {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "compile_error";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "nothing {message} Throws a compile error unconditionally at link time, if the function has not been fully compiled"
					+ " out with preprocessor directives. This is useful for causing a custom compile error if certain compilation environment"
					+ " settings are not correct.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.CUSTOM_LINK, OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(children.isEmpty()) {
				throw new CREFormatException(this.getName() + " expects at least 1 argument.", t);
			}
			if(!children.get(0).isConst()) {
				throw new ConfigCompileException(getName() + "'s argument must be a hardcoded string.", t);
			}
			return null;
		}

		@Override
		public void link(Target t, List<ParseTree> children) throws ConfigCompileException {
			if(children.isEmpty()) {
				throw new CREFormatException(this.getName() + " expects at least 1 argument.", t);
			}
			throw new ConfigCompileException(children.get(0).getData().val(), t);
		}

	}

	@api
	public static class hash extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
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
		public CInt exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return new CInt(args[0].hashCode(), t);
		}

		@Override
		public String getName() {
			return "hash";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "int {value} Hashes the value, and returns an int representing that value.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_3;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[] {
				new ExampleScript("", "hash(1);"),
				new ExampleScript("", "hash(2);"),
				new ExampleScript("", "hash(3);"),
				new ExampleScript("", "hash('Hello World!');"),
				new ExampleScript("", "hash(array(1, 2, 3));")
			};
		}

	}

}
