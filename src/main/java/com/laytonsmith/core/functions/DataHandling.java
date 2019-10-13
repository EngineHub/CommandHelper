package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.annotations.DocumentLink;
import com.laytonsmith.annotations.OperatorPreferred;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.annotations.nolinking;
import com.laytonsmith.annotations.noprofile;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.annotations.unbreakable;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Globals;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Procedure;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Security;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.BranchStatement;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.VariableScope;
import com.laytonsmith.core.constructs.Auto;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CByteArray;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CIClosure;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CLabel;
import com.laytonsmith.core.constructs.CMutablePrimitive;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CSlice;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.IVariableList;
import com.laytonsmith.core.constructs.InstanceofUtil;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.AbstractCREException;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.CRE.CREIncludeException;
import com.laytonsmith.core.exceptions.CRE.CREIndexOverflowException;
import com.laytonsmith.core.exceptions.CRE.CREInsufficientPermissionException;
import com.laytonsmith.core.exceptions.CRE.CREInvalidProcedureException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREStackOverflowError;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.StackTraceManager;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.tools.docgen.templates.ArrayIteration;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
@core
public class DataHandling {

	// Variable is more clear when named after the function it represents.
	@SuppressWarnings("checkstyle:constantname")
	private static final String array_get = new ArrayHandling.array_get().getName();

	// Variable is more clear when named after the function it represents.
	@SuppressWarnings("checkstyle:constantname")
	private static final String array_set = new ArrayHandling.array_set().getName();

	// Variable is more clear when named after the function it represents.
	@SuppressWarnings("checkstyle:constantname")
	private static final String array_push = new ArrayHandling.array_push().getName();

	public static String docs() {
		return "This class provides various methods to control script data and program flow.";
	}

	@api
	@seealso({com.laytonsmith.tools.docgen.templates.Arrays.class, ArrayIteration.class})
	public static class array extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "array";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			return new CArray(t, args);
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public String docs() {
			return "array {[var1, [var2...]]} Creates an array of values.";
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
				new ExampleScript("Basic usage", "assign(@array, array(1, 2, 3))\nmsg(@array)"),
				new ExampleScript("Associative array creation", "assign(@array, array(one: 'apple', two: 'banana'))\nmsg(@array)")};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		FileOptions lastFileOptions = null;

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs, List<ParseTree> children,
				FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			//We need to check here to ensure that
			//we aren't getting a slice in a label, which is used in switch
			//statements, but doesn't make sense here.
			//Also check for dynamic labels
			for(ParseTree child : children) {
				if(child.getData() instanceof CFunction && new Compiler.centry().getName().equals(child.getData().val())) {
					if(((CLabel) child.getChildAt(0).getData()).cVal() instanceof CSlice) {
						throw new ConfigCompileException("Slices cannot be used as array indices", child.getChildAt(0).getTarget());
					}
					if(((CLabel) child.getChildAt(0).getData()).cVal() instanceof IVariable) {
						String array = "@a";
						String valueName = ((IVariable) ((CLabel) child.getChildAt(0).getData()).cVal()).getVariableName();
						Mixed value = child.getChildAt(1).getData();
						String v;
						if(value instanceof IVariable) {
							v = ((IVariable) value).getVariableName();
						} else if(value.isInstanceOf(CString.TYPE)) {
							v = ((CString) value).getQuote();
						} else {
							v = "@value";
						}
						if("@a".equals(valueName)) {
							array = "@myArray";
						}
						throw new ConfigCompileException("Dynamic values cannot be used as indices in array construction."
								+ "\nTo make dynamic indicies, do the following: "
								+ "array " + array + " = array(); " + array + "[" + valueName + "] = " + v + ";", t);
					}
				}
			}
			return null;
		}

	}

	@api
	@seealso({com.laytonsmith.tools.docgen.templates.Arrays.class, ArrayIteration.class})
	public static class associative_array extends AbstractFunction {

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
			CArray array = CArray.GetAssociativeArray(t, args);
			return array;
		}

		@Override
		public String getName() {
			return "associative_array";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "array {[args...]} Works exactly like array(), except the array created will be an associative array, even"
					+ " if the array has been created with no elements. This is the only use case where this is neccessary, vs"
					+ " using the normal array() function, or in the case where you assign sequential keys anyways, and the same"
					+ " array could have been created using array().";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Usage with an empty array", "assign(@array, associative_array())\nmsg(is_associative(@array))"),
				new ExampleScript("Usage with an array with sequential keys", "assign(@array, array(0: '0', 1: '1'))\nmsg(is_associative(@array))\n"
				+ "assign(@array, associative_array(0: '0', 1: '1'))\nmsg(is_associative(@array))")};
		}

	}

	@api
	@seealso({com.laytonsmith.tools.docgen.templates.Variables.class})
	@OperatorPreferred("=")
	public static class assign extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "assign";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			IVariableList list = env.getEnv(GlobalEnv.class).GetVarList();
			int offset;
			CClassType type;
			String name;
			if(args.length == 3) {
				offset = 1;
				if(!(args[offset] instanceof IVariable)) {
					throw new CRECastException(getName() + " with 3 arguments only accepts an ivariable as the second argument.", t);
				}
				name = ((IVariable) args[offset]).getVariableName();
				if(list.has(name) && env.getEnv(GlobalEnv.class).GetFlag("no-check-duplicate-assign") == null) {
					if(env.getEnv(GlobalEnv.class).GetFlag("closure-warn-overwrite") != null) {
						MSLog.GetLogger().Log(MSLog.Tags.RUNTIME, LogLevel.ERROR,
								"The variable " + name + " is hiding another value of the"
								+ " same name in the main scope.", t);
					} else {
						MSLog.GetLogger().Log(MSLog.Tags.RUNTIME, LogLevel.ERROR, name + " was already defined at "
								+ list.get(name, t, true, env).getDefinedTarget() + " but is being redefined.", t);
					}
				}
				type = ArgumentValidation.getClassType(args[0], t);
			} else {
				offset = 0;
				if(!(args[offset] instanceof IVariable)) {
					throw new CRECastException(getName() + " with 2 arguments only accepts an ivariable as the first argument.", t);
				}
				name = ((IVariable) args[offset]).getVariableName();
				type = list.get(name, t, true, env).getDefinedType();
			}
			Mixed c = args[offset + 1];
			while(c instanceof IVariable) {
				IVariable cur = (IVariable) c;
				c = list.get(cur.getVariableName(), cur.getTarget(), env).ival();
			}
			IVariable v = new IVariable(type, name, c, t, env);
			list.set(v);
			return v;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public String docs() {
			return "ivariable {[type], ivar, mixed} Accepts an ivariable ivar as a parameter, and puts the specified value mixed in it."
					+ " Returns the variable that was assigned. Operator syntax is also supported: <code>@a = 5;</code>."
					+ " Other forms are supported as well, +=, -=, *=, /=, .=, which do multiple operations at once. Array assigns"
					+ " are also supported: @array[5] = 'new value in index 5';";
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
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.OPTIMIZE_CONSTANT,
					OptimizationOption.OPTIMIZE_DYNAMIC
			);
		}

		@Override
		public Mixed optimize(Target t, Environment env, Mixed... args) throws ConfigCompileException {
			//We can't really optimize, but we can check that we are
			//getting an ivariable.
			int offset = 0;
			if(args.length == 3) {
				offset = 1;
				if(!(args[0].isInstanceOf(CClassType.TYPE))) {
					throw new ConfigCompileException("Expecting a ClassType for parameter 1 to assign", t);
				}
			}
			if(args.length > 0 && !(args[offset] instanceof IVariable)) {
				throw new ConfigCompileException("Expecting an ivar for argument 1 to assign", t);
			}
			return null;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			//Check for too few arguments
			if(children.size() < 2) {
				return null;
			}
			if(children.get(0).getData() instanceof IVariable
					&& children.get(1).getData() instanceof IVariable) {
				if(((IVariable) children.get(0).getData()).getVariableName().equals(
						((IVariable) children.get(1).getData()).getVariableName())) {
					String msg = "Assigning a variable to itself";
					env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions,
							new CompilerWarning(msg, t, null));
				}
			}
			if(children.get(0).getData() instanceof CFunction && array_get.equals(children.get(0).getData().val())) {
				if(children.get(0).getChildAt(1).getData() instanceof CSlice) {
					CSlice cs = (CSlice) children.get(0).getChildAt(1).getData();
					if(cs.getStart() == 0 && cs.getFinish() == -1) {
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
				new ExampleScript("Basic usage", "assign(@variable, 5);\nmsg(@variable);"),
				new ExampleScript("Array assignment", "assign(@variable, associative_array());\nassign(@variable['associative'], 5);\nmsg(@variable);"),
				new ExampleScript("String assignment with type", "assign(string, @s, 'string');"),
				new ExampleScript("String assignment with invalid type", "assign(int, @i, 'string');", true),
				new ExampleScript("Operator syntax", "@variable = 5;\nmsg(@variable);"),
				new ExampleScript("Operator syntax with type", "string @s = 'string';"),
				new ExampleScript("Operator syntax using combined operators", "@variable = 'string';\n@variable .= ' more string';\nmsg(@variable);"),
				new ExampleScript("Operator syntax using combined operators", "@variable = 5;\n@variable += 10;\nmsg(@variable);"),
				new ExampleScript("Operator syntax using combined operators", "@variable = 5;\n@variable -= 10;\nmsg(@variable);"),
				new ExampleScript("Operator syntax using combined operators", "@variable = 5;\n@variable *= 10;\nmsg(@variable);"),
				new ExampleScript("Operator syntax using combined operators", "@variable = 5;\n@variable /= 10;\nmsg(@variable);")};
		}
	}

	@api
	public static class is_stringable extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_stringable";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns whether or not the item is convertable to a string. Everything but arrays can be used as strings.";
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
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(!(args[0].isInstanceOf(CArray.TYPE)));
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
				new ExampleScript("False condition", "is_stringable(array(1))")};
		}
	}

	@api
	public static class is_string extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_string";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns whether or not the item is actually a string datatype. If you just care if some data can be used as a string,"
					+ " use is_stringable().";
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
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(args[0].isInstanceOf(CString.TYPE));
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
				new ExampleScript("False condition", "is_string(1) #is_stringable() would return true here")};
		}
	}

	@api
	public static class is_bytearray extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_bytearray";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns whether or not the item is actually a ByteArray datatype.";
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
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(args[0].isInstanceOf(CByteArray.TYPE));
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
				new ExampleScript("False condition", "is_bytearray(123)")};
		}
	}

	@api
	public static class is_array extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_array";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns whether or not the item is an array";
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
		public MSVersion since() {
			return MSVersion.V3_1_2;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(args[0].isInstanceOf(CArray.TYPE));
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
				new ExampleScript("False condition", "is_array('no')")};
		}
	}

	@api
	public static class is_number extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_number";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns whether or not the given item is an integer or a double. Note that numeric strings can usually be used as integers and doubles,"
					+ " however this function checks the actual datatype of the item. If you just want to see if an item can be used as a number,"
					+ " use is_integral() or is_numeric() instead.";
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
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(args[0].isInstanceOf(CInt.TYPE) || args[0].isInstanceOf(CDouble.TYPE));
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
				new ExampleScript("True condition", "is_number(1)"),
				new ExampleScript("True condition", "is_number(1.0)"),
				new ExampleScript("False condition", "is_number('1')"),
				new ExampleScript("False condition", "is_number('1.0')")};
		}
	}

	@api
	public static class is_double extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_double";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns whether or not the given item is a double. Note that numeric strings and integers"
					+ " can usually be used as a double, however this function checks the actual datatype of the item. If"
					+ " you just want to see if an item can be used as a number, use is_numeric() instead.";
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
		public MSVersion since() {
			return MSVersion.V3_1_2;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(args[0].isInstanceOf(CDouble.TYPE));
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
				new ExampleScript("False condition", "is_double(1)")};
		}
	}

	@api
	public static class is_integer extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_integer";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns whether or not the given item is an integer. Note that numeric strings can usually be used as integers,"
					+ " however this function checks the actual datatype of the item. If you just want to see if an item can be used as a number,"
					+ " use is_integral() or is_numeric() instead.";
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
		public MSVersion since() {
			return MSVersion.V3_1_2;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(args[0].isInstanceOf(CInt.TYPE));
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
				new ExampleScript("False condition", "is_integer(1.0)")};
		}
	}

	@api
	public static class is_boolean extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_boolean";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns whether the given item is of the boolean datatype. Note that all datatypes can be used as booleans, however"
					+ " this function checks the specific datatype of the given item.";
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
		public MSVersion since() {
			return MSVersion.V3_1_2;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(args[0].isInstanceOf(CBoolean.TYPE));
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
				new ExampleScript("False condition", "is_boolean(0)")};
		}
	}

	@api
	public static class is_null extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_null";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns whether or not the given item is null.";
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
		public MSVersion since() {
			return MSVersion.V3_1_2;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(args[0] instanceof CNull);
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
				new ExampleScript("False condition", "is_null(0)")};
		}
	}

	@api
	public static class is_numeric extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_numeric";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns false if the item would fail if it were used as a numeric value."
					+ " If it can be parsed or otherwise converted into a numeric value, true is returned.";
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
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			boolean b = true;
			try {
				Static.getNumber(args[0], t);
			} catch (ConfigRuntimeException e) {
				b = false;
			}
			return CBoolean.get(b);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
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
				new ExampleScript("True condition, because null is coerced to 0.0, which is numeric.", "is_numeric(null)")};
		}
	}

	@api
	public static class is_integral extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_integral";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns true if the numeric value represented by "
					+ " a given double or numeric string could be cast to an integer"
					+ " without losing data (or if it's an integer). For instance,"
					+ " is_numeric(4.5) would return true, and integer(4.5) would work,"
					+ " however, equals(4.5, integer(4.5)) returns false, because the"
					+ " value was narrowed to 4.";
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
			double d;
			try {
				d = Static.getDouble(args[0], t);
			} catch (ConfigRuntimeException e) {
				return CBoolean.FALSE;
			}
			return CBoolean.get((long) d == d);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
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
				new ExampleScript("True condition, because null is coerced to 0, which is integral", "is_integral(null)")};
		}
	}

	@api
	@unbreakable
	public static class proc extends AbstractFunction implements BranchStatement, VariableScope {

		@Override
		public String getName() {
			return "proc";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "void {procName, [params...], procCode} Creates a new user defined procedure (also known as"
					+ " \"function\"), with the given name and parameters, that can be called later in code."
					+ " The name of the procedure must be a constant and its parameters must be variables."
					+ " Please see the more detailed documentation on procedures for more information."
					+ " In general, brace syntax and keyword usage is preferred:"
					+ " proc _myProc(@a, @b){ procCode(@a, @b); }";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class};
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
			return MSVersion.V3_1_3;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			Procedure myProc = getProcedure(t, env, parent, nodes);
			env.getEnv(GlobalEnv.class).GetProcs().put(myProc.getName(), myProc);
			return CVoid.VOID;
		}

		public static Procedure getProcedure(Target t, Environment env, Script parent, ParseTree... nodes) {
			String name = "";
			List<IVariable> vars = new ArrayList<>();
			ParseTree tree = null;
			List<String> varNames = new ArrayList<>();
			boolean usesAssign = false;
			CClassType returnType = Auto.TYPE;
			if(nodes[0].getData().equals(CVoid.VOID) || nodes[0].getData().isInstanceOf(CClassType.TYPE)) {
				if(nodes[0].getData().equals(CVoid.VOID)) {
					returnType = CVoid.TYPE;
				} else {
					returnType = (CClassType) nodes[0].getData();
				}
				ParseTree[] newNodes = new ParseTree[nodes.length - 1];
				for(int i = 1; i < nodes.length; i++) {
					newNodes[i - 1] = nodes[i];
				}
				nodes = newNodes;
			}
			// We have to restore the variable list once we're done
			IVariableList originalList = env.getEnv(GlobalEnv.class).GetVarList().clone();
			for(int i = 0; i < nodes.length; i++) {
				if(i == nodes.length - 1) {
					tree = nodes[i];
				} else {
					boolean thisNodeIsAssign = false;
					if(nodes[i].getData() instanceof CFunction) {
						if((nodes[i].getData()).val().equals("assign")) {
							thisNodeIsAssign = true;
							if((nodes[i].getChildren().size() == 3 && Construct.IsDynamicHelper(nodes[i].getChildAt(0).getData()))
									|| Construct.IsDynamicHelper(nodes[i].getChildAt(1).getData())) {
								usesAssign = true;
							}
						} else if((nodes[i].getData()).val().equals("__autoconcat__")) {
							throw new CREInvalidProcedureException("Invalid arguments defined for procedure", t);
						}
					}
					env.getEnv(GlobalEnv.class).SetFlag("no-check-duplicate-assign", true);
					Mixed cons = parent.eval(nodes[i], env);
					env.getEnv(GlobalEnv.class).ClearFlag("no-check-duplicate-assign");
					if(i == 0) {
						if(cons instanceof IVariable) {
							throw new CREInvalidProcedureException("Anonymous Procedures are not allowed", t);
						}
						name = cons.val();
					} else {
						if(!(cons instanceof IVariable)) {
							throw new CREInvalidProcedureException("You must use IVariables as the arguments", t);
						}
						IVariable ivar = null;
						try {
							Mixed c = cons;
							String varName = ((IVariable) c).getVariableName();
							if(varNames.contains(varName)) {
								throw new CREInvalidProcedureException("Same variable name defined twice in " + name, t);
							}
							varNames.add(varName);
							while(c instanceof IVariable) {
								c = env.getEnv(GlobalEnv.class).GetVarList().get(((IVariable) c).getVariableName(), t,
										true, env).ival();
							}
							if(!thisNodeIsAssign) {
								//This is required because otherwise a default value that's already in the environment
								//would end up getting set to the existing value, thereby leaking in the global env
								//into this proc, if the call to the proc didn't have a value in this slot.
								c = new CString("", t);
							}
							ivar = new IVariable(((IVariable) cons).getDefinedType(),
									((IVariable) cons).getVariableName(), c.clone(), t, env);
						} catch (CloneNotSupportedException ex) {
							//
						}
						vars.add(ivar);
					}
				}
			}
			env.getEnv(GlobalEnv.class).SetVarList(originalList);
			Procedure myProc = new Procedure(name, returnType, vars, tree, t);
			if(usesAssign) {
				myProc.definitelyNotConstant();
			}
			return myProc;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return CVoid.VOID;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		/**
		 * Returns either null to indicate that the procedure is not const, or returns a single Mixed, which should
		 * replace the call to the procedure.
		 *
		 * @param t
		 * @param myProc
		 * @param children
		 * @return
		 * @throws ConfigRuntimeException
		 */
		public static Mixed optimizeProcedure(Target t, Procedure myProc, List<ParseTree> children) throws ConfigRuntimeException {
			if(myProc.isPossiblyConstant()) {
				//Oooh, it's possibly constant. So, let's run it with our children.
				try {
					FileOptions options = new FileOptions(new HashMap<>());
					if(!children.isEmpty()) {
						options = children.get(0).getFileOptions();
					}
					ParseTree root = new ParseTree(new CFunction("__autoconcat__", Target.UNKNOWN), options);
					Script fakeScript = Script.GenerateScript(root, Static.GLOBAL_PERMISSION);
					Environment env = Static.GenerateStandaloneEnvironment();
					env.getEnv(GlobalEnv.class).SetScript(fakeScript);
					Mixed c = myProc.cexecute(children, env, t);
					//Yup! It worked. It's a const proc.
					return c;
				} catch (ConfigRuntimeException e) {
					if(e instanceof CREThrowable
							&& ((CREThrowable) e).isInstanceOf(CREInvalidProcedureException.TYPE)) {
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

//		@Override
//		public boolean canOptimizeDynamic() {
//			return true;
//		}
//
//		@Override
//		public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
//			//We seriously lose out on the ability to optimize this procedure
//			//if we are assigning a dynamic value as a default, but we have to check
//			//that here. If we don't, we lose the information
//			return ;
//		}

		@Override
		public List<Boolean> isBranch(List<ParseTree> children) {
			List<Boolean> ret = new ArrayList<>(children.size());
			for(int i = 0; i < children.size() - 1; i++) {
				ret.add(false);
			}
			ret.add(true);
			return ret;
		}

		@Override
		public List<Boolean> isScope(List<ParseTree> children) {
			List<Boolean> ret = new ArrayList<>(children.size());
			for(ParseTree child : children) {
				ret.add(true);
			}
			return ret;
		}
	}

	@api
	@DocumentLink(0)
	public static class include extends AbstractFunction implements Optimizable, DocumentLinkProvider {

		@Override
		public String getName() {
			return "include";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {path} Includes external code at the specified path.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIncludeException.class};
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
			return true;
		}

		@Override
		public CVoid exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return CVoid.VOID;
		}

		@Override
		public CVoid execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			ParseTree tree = nodes[0];
			Mixed arg = parent.seval(tree, env);
			String location = arg.val();
			File file = Static.GetFileFromArgument(location, env, t, null);
			ParseTree include = IncludeCache.get(file, env, t);
			if(include != null) {
				// It could be an empty file
				StackTraceManager stManager = env.getEnv(GlobalEnv.class).GetStackTraceManager();
				stManager.addStackTraceElement(new ConfigRuntimeException.StackTraceElement("<<include " + arg.val() + ">>", t));
				try {
					parent.eval(include.getChildAt(0), env);
				} catch (AbstractCREException e) {
					e.freezeStackTraceElements(stManager);
					throw e;
				} catch (StackOverflowError e) {
					throw new CREStackOverflowError(null, t, e);
				} finally {
					stManager.popStackTraceElement();
				}
			}
			return CVoid.VOID;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
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
			if(children.isEmpty()) {
				throw new ConfigCompileException("include() expects 1 argument.", t);
			}
			//We can't optimize per se, but if the path is constant, and the code is uncompilable, we
			//can give a warning, and go ahead and cache the tree.
			if(children.get(0).isConst()) {
				String path = children.get(0).getData().val();
				File file = Static.GetFileFromArgument(path, env, t, null);
				if(!file.exists()) {
					env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions,
							new CompilerWarning("File doesn't exist, this will be an error at runtime.",
									children.get(0).getTarget(), FileOptions.SuppressWarning.IncludedFileNotFound));
				}
				try {
					if(!Security.CheckSecurity(file)) {
						throw new ConfigCompileException("Included file is inaccessible due to the base-dir setting",
								children.get(0).getTarget());
					}
				} catch (IOException ex) {
					// Just ignore it. This is not something we can deal with anyways, and if it's still a problem
					// at runtime, it will be reported through existing means.
				}
				// Some users have dynamic inclusion solutions, because for larger codebases, compilation is a non
				// trival amount of time, and currently this happens on the main thread. Once compilation happens on
				// a background thread, (or at least recompiles on a background thread) this code can be revisted, and
				// re-added if needed. Having said that, a code ecosystem that determines inter-script dependencies
				// would likely obsolete the need for this anyways.
//				try {
//					IncludeCache.get(file, env, t);
//				} catch (CREIOException ex) {
//					// This is thrown if a file doesn't exist. When it actually runs, this is definitely an error,
//					// for now we just want it to be a warning.
//					env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions,
//							new CompilerWarning(ex.getMessage(), children.get(0).getTarget(), null));
//				}
			}
			return null;
		}

		@Override
		public LogLevel profileAt() {
			return LogLevel.ERROR;
		}

		@Override
		public String profileMessageS(List<ParseTree> args) {
			String m = "Executing function: include(";
			if(args.get(0).isConst()) {
				m += args.get(0).getData().val();
			} else {
				m += "<dynamic input>";
			}
			return m + ")";
		}

	}

	@api(environments = CommandHelperEnvironment.class)
	public static class is_proc extends AbstractFunction {

		@Override
		public String getName() {
			return "is_proc";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {procName} Returns whether or not the given procName is currently defined, i.e. if calling this proc wouldn't"
					+ " throw an exception.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return true;
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
		public Mixed exec(Target t, Environment env, Mixed... args) {
			return CBoolean.get(env.getEnv(GlobalEnv.class).GetProcs().get(args[0].val()) != null);
		}
	}

	@api
	public static class is_associative extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_associative";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {array} Returns whether or not the array is associative. If the parameter is not an array, throws a CastException.";
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
				return CBoolean.get(((CArray) args[0]).inAssociativeMode());
			} else {
				throw new CRECastException(this.getName() + " expects argument 1 to be an array", t);
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
				new ExampleScript("False condition", "is_associative(array(1, 2, 3))")};
		}
	}

	@api
	public static class is_closure extends AbstractFunction {

		@Override
		public String getName() {
			return "is_closure";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {arg} Returns true if the argument is a closure (could be executed)"
					+ " or false otherwise";
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
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(args[0].isInstanceOf(CClosure.TYPE));
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("True condition", "is_closure(closure(msg('code')))"),
				new ExampleScript("False condition", "is_closure('a string')")};
		}
	}

	@api
	@seealso({_export.class})
	public static class _import extends AbstractFunction {

		@Override
		public String getName() {
			return "import";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "mixed {key, [default]} This function imports a value from the global value register. It looks for a"
					+ " value stored with the specified key (using the export function), and returns that value."
					+ " If specified key doesn't exist, it will return either null or the default value if specified."
					+ " An array may be used as a key. It is converted into a string with the array values separated by"
					+ " dots. import() is threadsafe.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIllegalArgumentException.class, CREIndexOverflowException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
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
			String key;
			if(args[0].isInstanceOf(CString.TYPE)) {
				key = args[0].val();
			} else if(args[0].isInstanceOf(CArray.TYPE)) {
				if(((CArray) args[0]).isAssociative()) {
					throw new CREIllegalArgumentException("Associative arrays may not be used as keys in " + getName(), t);
				}
				key = GetNamespace((CArray) args[0], t);
			} else {
				throw new CREIllegalArgumentException("Argument 1 in " + this.getName() + " must be a string or array.", t);
			}
			Mixed c = Globals.GetGlobalConstruct(key);
			if(args.length == 2 && c instanceof CNull) {
				c = args[1];
			}
			return c;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new _export().examples();
		}
	}

	@api
	@seealso({_import.class})
	public static class _export extends AbstractFunction {

		@Override
		public String getName() {
			return "export";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {key, value} Stores a value in the global storage register."
					+ " An arbitrary value is stored with the given key, and can be retreived using import."
					+ " If the value is already stored, it is overwritten. See {{function|import}}."
					+ " The reference to the value is stored, not a copy of the value, so in the case of"
					+ " arrays, manipulating the contents of the array will manipulate the stored value. An array may"
					+ " be used as a key. It is converted into a string with the array values separated by dots."
					+ " export() is threadsafe.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIllegalArgumentException.class, CREIndexOverflowException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
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
			String key;
			if(args[0].isInstanceOf(CString.TYPE)) {
				key = args[0].val();
			} else if(args[0].isInstanceOf(CArray.TYPE)) {
				if(((CArray) args[0]).isAssociative()) {
					throw new CREIllegalArgumentException("Associative arrays may not be used as keys in " + getName(), t);
				}
				key = GetNamespace((CArray) args[0], t);
			} else {
				throw new CREIllegalArgumentException("Argument 1 in " + this.getName() + " must be a string or array.", t);
			}
			Mixed c = args[1];
			Globals.SetGlobal(key, c);
			return CVoid.VOID;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "@var = 2;\n"
				+ "export('custom.name', @var);\n"
				+ "@var2 = import('custom.name');\n"
				+ "msg(@var2);"),
				new ExampleScript("Storage of references", "@array = array(1, 2, 3);\n"
				+ "export('array', @array);\n"
				+ "@array[0] = 4;\n"
				+ "@array2 = import('array');\n"
				+ "msg(@array2);"),
				new ExampleScript("Array key usage", "@key = array('custom', 'name');\n"
				+ "export(@key, 'value');\n"
				+ "@value = import(@key);\n"
				+ "msg(@value);"),
				new ExampleScript("Default value usage", "export('custom.name', null);\n"
				+ "@value = import('custom.name', 'default value');\n"
				+ "msg(@value);")
			};
		}
	}

	@api
	@unbreakable
	@seealso({com.laytonsmith.tools.docgen.templates.Closures.class})
	public static class closure extends AbstractFunction implements BranchStatement, VariableScope {

		@Override
		public String getName() {
			return "closure";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "closure {[params...], code} Returns a closure on the provided code. A closure is"
					+ " a datatype that represents some code as code, not the results of some"
					+ " code after it is run. Code placed in a closure can be used as"
					+ " a string, or executed by other functions using the execute() function."
					+ " If a closure is \"to string'd\" it will not necessarily look like"
					+ " the original code, but will be functionally equivalent. The current environment"
					+ " is \"snapshotted\" and stored with the closure, however, this information is"
					+ " only stored in memory, it isn't retained during a serialization operation."
					+ " Also, the special variable @arguments is automatically created for you, and contains"
					+ " an array of all the arguments passed to the closure, much like procedures."
					+ " See the wiki article on [[Closures|closures]] for more details"
					+ " and examples.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public boolean preResolveVariables() {
			return false;
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
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			if(nodes.length == 0) {
				//Empty closure, do nothing.
				return new CClosure(null, env, Auto.TYPE, new String[]{}, new Mixed[]{}, new CClassType[]{}, t);
			}
			// Handle the closure type first thing
			CClassType returnType = Auto.TYPE;
			if(nodes[0].getData().isInstanceOf(CClassType.TYPE)) {
				returnType = (CClassType) nodes[0].getData();
				ParseTree[] newNodes = new ParseTree[nodes.length - 1];
				for(int i = 1; i < nodes.length; i++) {
					newNodes[i - 1] = nodes[i];
				}
				nodes = newNodes;
			}
			String[] names = new String[nodes.length - 1];
			Mixed[] defaults = new Mixed[nodes.length - 1];
			CClassType[] types = new CClassType[nodes.length - 1];
			// We clone the enviornment at this point, because we don't want the values
			// that are assigned here to overwrite values in the main scope.
			Environment myEnv;
			try {
				myEnv = env.clone();
			} catch (CloneNotSupportedException ex) {
				myEnv = env;
			}
			for(int i = 0; i < nodes.length - 1; i++) {
				ParseTree node = nodes[i];
				ParseTree newNode = new ParseTree(new CFunction("g", t), node.getFileOptions());
				List<ParseTree> children = new ArrayList<>();
				children.add(node);
				newNode.setChildren(children);
				Script fakeScript = Script.GenerateScript(newNode, myEnv.getEnv(GlobalEnv.class).GetLabel());
				myEnv.getEnv(GlobalEnv.class).SetFlag("closure-warn-overwrite", true);
				Mixed ret = MethodScriptCompiler.execute(newNode, myEnv, null, fakeScript);
				myEnv.getEnv(GlobalEnv.class).ClearFlag("closure-warn-overwrite");
				if(!(ret instanceof IVariable)) {
					throw new CRECastException("Arguments sent to " + getName() + " barring the last) must be ivariables", t);
				}
				names[i] = ((IVariable) ret).getVariableName();
				try {
					defaults[i] = ((IVariable) ret).ival().clone();
					types[i] = ((IVariable) ret).getDefinedType();
				} catch (CloneNotSupportedException ex) {
					Logger.getLogger(DataHandling.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			CClosure closure = new CClosure(nodes[nodes.length - 1], myEnv, returnType, names, defaults, types, t);
			return closure;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Creates a closure", "closure(){\n"
				+ "\tmsg('Hello World!');\n"
				+ "};"),
				new ExampleScript("Executes a closure", "execute(closure(){\n"
				+ "\tmsg('Hello World!');\n"
				+ "});")
			};
		}

		@Override
		public List<Boolean> isBranch(List<ParseTree> children) {
			List<Boolean> ret = new ArrayList<>(children.size());
			if(children.isEmpty()) {
				// This is the "closure()" usage, and does not have any branches.
				return ret;
			}
			for(int i = 0; i < children.size() - 1; i++) {
				ret.add(false);
			}
			ret.add(true);
			return ret;
		}

		@Override
		public List<Boolean> isScope(List<ParseTree> children) {
			return isBranch(children);
		}

	}

	@api
	@unbreakable
	@seealso({com.laytonsmith.tools.docgen.templates.Closures.class})
	public static class iclosure extends closure {

		@Override
		public String getName() {
			return "iclosure";
		}

		@Override
		public String docs() {
			return "iclosure {[params...], code} Returns a scope isolated closure on the provided code. An iclosure is"
					+ " a datatype that represents some code as code, not the results of some"
					+ " code after it is run. Code placed in an iclosure can be used as"
					+ " a string, or executed by other functions using the execute() function."
					+ " If a closure is \"to string'd\" it will not necessarily look like"
					+ " the original code, but will be functionally equivalent. The current environment"
					+ " is \"snapshotted\" and stored with the closure, however, this information is"
					+ " only stored in memory, it isn't retained during a serialization operation. However,"
					+ " the variable table of the parent scope is not retained, thus making this closure \"isolated\""
					+ " from the parent code."
					+ " The special variable @arguments is automatically created for you, and contains"
					+ " an array of all the arguments passed to the closure, much like procedures."
					+ " See the wiki article on [[Closures|closures]] for more details"
					+ " and examples.";
		}

		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			if(nodes.length == 0) {
				//Empty closure, do nothing.
				return new CIClosure(null, env, Auto.TYPE, new String[]{}, new Mixed[]{}, new CClassType[]{}, t);
			}
			// Handle the closure type first thing
			CClassType returnType = Auto.TYPE;
			if(nodes[0].getData().isInstanceOf(CClassType.TYPE)) {
				returnType = (CClassType) nodes[0].getData();
				ParseTree[] newNodes = new ParseTree[nodes.length - 1];
				for(int i = 1; i < nodes.length; i++) {
					newNodes[i - 1] = nodes[i];
				}
				nodes = newNodes;
			}
			String[] names = new String[nodes.length - 1];
			Mixed[] defaults = new Mixed[nodes.length - 1];
			CClassType[] types = new CClassType[nodes.length - 1];
			// We clone the enviornment at this point, because we don't want the values
			// that are assigned here to overwrite values in the main scope.
			Environment myEnv;
			try {
				myEnv = env.clone();
			} catch (CloneNotSupportedException ex) {
				myEnv = env;
			}
			for(int i = 0; i < nodes.length - 1; i++) {
				ParseTree node = nodes[i];
				ParseTree newNode = new ParseTree(new CFunction("g", t), node.getFileOptions());
				List<ParseTree> children = new ArrayList<>();
				children.add(node);
				newNode.setChildren(children);
				Script fakeScript = Script.GenerateScript(newNode, myEnv.getEnv(GlobalEnv.class).GetLabel());
				myEnv.getEnv(GlobalEnv.class).SetFlag("closure-warn-overwrite", true);
				Mixed ret = MethodScriptCompiler.execute(newNode, myEnv, null, fakeScript);
				myEnv.getEnv(GlobalEnv.class).ClearFlag("closure-warn-overwrite");
				if(!(ret instanceof IVariable)) {
					throw new CRECastException("Arguments sent to " + getName() + " barring the last) must be ivariables", t);
				}
				names[i] = ((IVariable) ret).getVariableName();
				try {
					defaults[i] = ((IVariable) ret).ival().clone();
					types[i] = ((IVariable) ret).getDefinedType();
				} catch (CloneNotSupportedException ex) {
					Logger.getLogger(DataHandling.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			CIClosure closure = new CIClosure(nodes[nodes.length - 1], myEnv, returnType, names, defaults, types, t);
			return closure;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Creates an iclosure", "iclosure(){\n"
				+ "\tmsg('Hello World!');\n"
				+ "};"),
				new ExampleScript("Executes an iclosure", "execute(iclosure(){\n"
				+ "\tmsg('Hello World!');\n"
				+ "});"),
				new ExampleScript("Shows scoping", "@a = \'variable\';\n"
				+ "msg('Outside of iclosure: '.reflect_pull('varlist'));\n"
				+ "// Note that this is an iclosure\n"
				+ "execute('val1', iclosure(@b){\n"
				+ "\tmsg('Inside of iclosure: '.reflect_pull('varlist'));\n"
				+ "});\n"
				+ "// Note that this is a regular closure\n"
				+ "execute('val2', closure(@c){\n"
				+ "\tmsg('Insider of closure: '.reflect_pull('varlist'));\n"
				+ "});")
			};
		}

	}

	@api
	@hide("Until the Federation system is finished, this is hidden")
	@unbreakable
	@nolinking
	public static class rclosure extends closure {

		@Override
		public String getName() {
			return "rclosure";
		}

		@Override
		public String docs() {
			return "closure {[params...], code} Returns a non-linking closure on the provided code. The same rules apply"
					+ " for closures, except the top level internal code does not check for proper linking at compile time,"
					+ " and instead links at runtime. Lexer errors and some other compile time checks ARE done however, but"
					+ " functions are not optimized or linked. This is used for remote code execution, since the remote platform"
					+ " may have some functionality unavailable on this current platform.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	@seealso({com.laytonsmith.tools.docgen.templates.Closures.class, execute_array.class, executeas.class})
	public static class execute extends AbstractFunction {

		@Override
		public String getName() {
			return "execute";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "mixed {[values...], closure} Executes the given closure. You can also send arguments"
					+ " to the closure, which it may or may not use, depending on the particular closure's"
					+ " definition. If the closure returns a value with return(), then that value will"
					+ " be returned with execute. Otherwise, void is returned.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
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
			if(args[args.length - 1].isInstanceOf(CClosure.TYPE)) {
				Mixed[] vals = new Mixed[args.length - 1];
				System.arraycopy(args, 0, vals, 0, args.length - 1);
				CClosure closure = (CClosure) args[args.length - 1];
				return closure.executeCallable(vals);
			} else {
				throw new CRECastException("Only a closure (created from the closure function) can be sent to execute()", t);
			}
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	@seealso({com.laytonsmith.tools.docgen.templates.Closures.class, execute.class})
	public static class execute_array extends AbstractFunction {
		@Override
		public String getName() {
			return "execute_array";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "mixed {valueArray, closure} Executes the given closure, expanding the value"
					+ " array as individual arguments to the closure. If there are no arguments to be"
					+ " sent to the closure, an empty array can be sent."
					+ " If the closure returns a value with return(), then that value will"
					+ " be returned with execute. Otherwise, void is returned.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
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
			Mixed[] vals = ArgumentValidation.getArray(args[0], t).asList().toArray(new Mixed[0]);
			CClosure closure = ArgumentValidation.getObject(args[1], t, CClosure.class);
			return closure.executeCallable(vals);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_4;
		}
	}

	@api
	@seealso({com.laytonsmith.tools.docgen.templates.Closures.class})
	public static class executeas extends AbstractFunction {

		@Override
		public String getName() {
			return "executeas";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "mixed {player, label, [values...], closure} Executes the given closure in the context of a given"
					+ " player or " + Static.getConsoleName() + ". A closure that runs player(), for instance,"
					+ " would return the specified player's name."
					+ " The label argument sets the permission label that this closure will use. If null is given,"
					+ " the current label will be used, like with execute().";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(!(args[args.length - 1].isInstanceOf(CClosure.TYPE))) {
				throw new CRECastException("Only a closure (created from the closure function) can be sent to executeas()", t);
			}
			Mixed[] vals = new Mixed[args.length - 3];
			System.arraycopy(args, 2, vals, 0, args.length - 3);
			CClosure closure = (CClosure) args[args.length - 1];
			CommandHelperEnvironment cEnv = closure.getEnv().getEnv(CommandHelperEnvironment.class);
			GlobalEnv gEnv = closure.getEnv().getEnv(GlobalEnv.class);

			MCCommandSender originalSender = cEnv.GetCommandSender();
			MCCommandSender sender;
			if(args[0].val().equals(Static.getConsoleName())) {
				sender = Static.getServer().getConsole();
			} else {
				sender = Static.GetPlayer(args[0].val(), t);
			}
			cEnv.SetCommandSender(sender);

			String originalLabel = gEnv.GetLabel();
			if(!(args[1] instanceof CNull)) {
				gEnv.SetLabel(args[1].val());
			}

			try {
				return closure.executeCallable(vals);
			} finally {
				cEnv.SetCommandSender(originalSender);
				gEnv.SetLabel(originalLabel);
			}
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}
	}

	@api
	public static class _boolean extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "boolean";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns a new construct that has been cast to a boolean. The item is cast according to"
					+ " the boolean conversion rules. Since all data types can be cast to a"
					+ " a boolean, this function will never throw an exception.";
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
			return CBoolean.get(ArgumentValidation.getBoolean(args[0], t));
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
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
				new ExampleScript("Basic usage", "boolean('')")};
		}
	}

	@api
	public static class _integer extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "integer";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "integer {item} Returns a new construct that has been cast to an integer."
					+ " This function will throw a CastException if is_numeric would return"
					+ " false for this item, but otherwise, it will be cast properly. Data"
					+ " may be lost in this conversion. For instance, 4.5 will be converted"
					+ " to 4, by using integer truncation. You can use is_integral to see"
					+ " if this data loss would occur.";
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
			return new CInt((long) Static.getDouble(args[0], t), t);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
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
				new ExampleScript("Failure", "assign(@var, 'string')\ninteger(@var)")};
		}
	}

	@api
	public static class _double extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "double";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "double {item} Returns a new construct that has been cast to an double."
					+ " This function will throw a CastException if is_numeric would return"
					+ " false for this item, but otherwise, it will be cast properly.";
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
			return new CDouble(Static.getDouble(args[0], t), t);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
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
				new ExampleScript("Failure", "@var = 'string';\ndouble(@var);")};
		}
	}

	@api
	public static class _string extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "string";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {item} Creates a new construct that is the \"toString\" of an item."
					+ " For arrays, an human readable version is returned; this should not be"
					+ " used directly, as the format is not guaranteed to remain consistent. Booleans return \"true\""
					+ " or \"false\" and null returns \"null\". Strings (and subclasses of strings) are simply returned"
					+ " as is.";
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
			if(args[0].isInstanceOf(CString.TYPE)) {
				return args[0];
			}
			return new CString(args[0].val(), t);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
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
				new ExampleScript("Basic usage", "string(array(one: 'one', two: 'two'))")};
		}
	}

	@api
	@seealso(parse_int.class)
	public static class to_radix extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRERangeException.class, CREFormatException.class};
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
			int radix = Static.getInt32(args[1], t);
			if(radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
				throw new CRERangeException("The radix must be between " + Character.MIN_RADIX + " and " + Character.MAX_RADIX + ", inclusive.", t);
			}
			return new CString(Long.toString(Static.getInt(args[0], t), radix), t);
		}

		@Override
		public String getName() {
			return "to_radix";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
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

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("To a hex string", "to_radix(15, 16)"),
				new ExampleScript("To a binary string", "to_radix(15, 2)"),
				new ExampleScript("Using hex value in source", "to_radix(0xff, 16)"),
				new ExampleScript("Using binary value in source", "to_radix(0b10101010, 2)")
			};
		}

	}

	@api
	@seealso(to_radix.class)
	public static class parse_int extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRERangeException.class, CREFormatException.class};
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
			String value = args[0].val();
			int radix = Static.getInt32(args[1], t);
			if(radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
				throw new CRERangeException("The radix must be between " + Character.MIN_RADIX + " and " + Character.MAX_RADIX + ", inclusive.", t);
			}
			long ret;
			try {
				ret = Long.parseLong(value, radix);
			} catch (NumberFormatException ex) {
				throw new CREFormatException("The input string: \"" + value + "\" is improperly formatted. (Perhaps you're using a character greater than"
						+ " the radix specified?)", t);
			}
			return new CInt(ret, t);
		}

		@Override
		public String getName() {
			return "parse_int";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "int {value, radix} Converts a string representation of an integer to a real integer, given the value's"
					+ " radix (base). See {{function|to_radix}} for a more detailed explanation of number theory. Radix must be"
					+ " between " + Character.MIN_RADIX + " and " + Character.MAX_RADIX + ", inclusive.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
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
	 * Generates the namespace for this value, given an array.
	 *
	 * @param array
	 * @return
	 */
	private static String GetNamespace(CArray array, Target t) {
		boolean first = true;
		StringBuilder b = new StringBuilder();
		for(int i = 0; i < array.size(); i++) {
			if(!first) {
				b.append(".");
			}
			first = false;
			b.append(array.get(i, t).val());
		}
		return b.toString();
	}

	@api
	public static class typeof extends AbstractFunction implements Optimizable {

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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			try {
				return args[0].typeof();
			} catch (IllegalArgumentException ex) {
				throw new Error("Class " + args[0].getClass().getName() + " is not annotated with @typeof. Please report this"
						+ " error to the developers.");
			}
		}

		@Override
		public String getName() {
			return "typeof";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "ClassType {arg} Returns a ClassType value of the typeof a value. For instance 'array' is returned"
					+ " for typeof(array()). This is a generic replacement for the is_* series of functions.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage, typeof string", "typeof('value')"),
				new ExampleScript("Basic usage, typeof int", "typeof(1)"),
				new ExampleScript("Basic usage, typeof double", "typeof(1.0)"),
				new ExampleScript("Basic usage, typeof closure", "typeof(closure(){ msg('test') })")};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.CONSTANT_OFFLINE);
		}

	}

	@api
	public static class eval extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "eval";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {script_string} Executes arbitrary MethodScript. Note that this function is very experimental, and is subject to changing or "
					+ "removal. To globally disable use of eval, set the runtime setting \"function.eval.disable\" to"
					+ " true, which will cause use of the function to throw an exception.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREInsufficientPermissionException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_0;
		}

		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			if(ArgumentValidation.getBooleanish(env.getEnv(GlobalEnv.class).GetRuntimeSetting("function.eval.disable",
					CBoolean.FALSE), t)) {
				throw new CREInsufficientPermissionException("eval is disabled", t);
			}
			boolean oldDynamicScriptMode = env.getEnv(GlobalEnv.class).GetDynamicScriptingMode();
			ParseTree node = nodes[0];
			try {
				env.getEnv(GlobalEnv.class).SetDynamicScriptingMode(true);
				Mixed script = parent.seval(node, env);
				if(script.isInstanceOf(CClosure.TYPE)) {
					throw new CRECastException("Closures cannot be eval'd directly. Use execute() instead.", t);
				}
				ParseTree root = MethodScriptCompiler.compile(MethodScriptCompiler.lex(script.val(), env, t.file(), true),
						env, env.getEnvClasses());
				StringBuilder b = new StringBuilder();
				int count = 0;
				for(ParseTree child : root.getChildren()) {
					Mixed s = parent.seval(child, env);
					if(!s.val().trim().isEmpty()) {
						if(count > 0) {
							b.append(" ");
						}
						b.append(s.val());
					}
					count++;
				}
				return new CString(b.toString(), t);
			} catch (ConfigCompileException e) {
				throw new CREFormatException("Could not compile eval'd code: " + e.getMessage(), t);
			} catch (ConfigCompileGroupException ex) {
				StringBuilder b = new StringBuilder();
				b.append("Could not compile eval'd code: ");
				for(ConfigCompileException e : ex.getList()) {
					b.append(e.getMessage()).append("\n");
				}
				throw new CREFormatException(b.toString(), t);
			} finally {
				env.getEnv(GlobalEnv.class).SetDynamicScriptingMode(oldDynamicScriptMode);
			}
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			return CVoid.VOID;
		}
		//Doesn't matter, run out of state anyways

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public Set<Optimizable.OptimizationOption> optimizationOptions() {
			return EnumSet.of(Optimizable.OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs, List<ParseTree> children,
				FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() != 1) {
				throw new ConfigCompileException(getName() + " expects only one argument", t);
			}
			if(children.get(0).isConst()) {
				String msg = "Eval'd code is hardcoded, consider simply using the code directly, as wrapping"
						+ " hardcoded code in " + getName() + " is much less efficient.";
				env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions,
						new CompilerWarning(msg, t, FileOptions.SuppressWarning.HardcodedDynamicParameter));
			}
			return null;
		}

	}

	@api
	@noprofile
	@hide("This will eventually be replaced by ; statements.")
	public static class g extends AbstractFunction {

		@Override
		public String getName() {
			return "g";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			for(int i = 0; i < args.length; i++) {
				args[i].val();
			}
			return CVoid.VOID;
		}

		@Override
		public String docs() {
			return "string {func1, [func2...]} Groups any number of functions together, and returns void. ";
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
			return MSVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}
	}

	/**
	 * For now, this feature works as is. However, I'm debating on whether or not I should just override assign()
	 * instead. The only issue with this is that if assign is overwritten, then a mutable_primitive will be "stuck" in
	 * the variable. So if later you wanted to make a value not a mutable primitive, there would be no way to do so.
	 * Another method could be introduced to "clear" the value out, but then there would be no way to tell if the value
	 * were actually mutable or not, so a third function would have to be added. The other point of concern is how to
	 * handle typeof() for a CMutablePrimitive. Should it return the underlying type, or mutable_primitive? If
	 * assignments are "sticky", then it would make sense to have it return the underlying type, but there's an issue
	 * with that, because then typeof wouldn't be useable for debug type situations. Given all these potential issues,
	 * it is still hidden, but available for experimental cases.
	 */
	@api
	@hide("This is still experimental")
	public static class mutable_primitive extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class};
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
			Mixed val = CNull.NULL;
			if(args.length > 0) {
				val = args[0];
			}
			return new CMutablePrimitive(val, t);
		}

		@Override
		public String getName() {
			return "mutable_primitive";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "mutable_primitive {[primitive_value]} Creates a mutable primitive object, initially setting the value of the object to"
					+ " null, or the specified value. The value must be a primitive value, and cannot be an array or object. ----"
					+ " The underlying primitive value is used in all cases where a value can be inferred. In all other cases, you must convert"
					+ " the primitive to the desired type, e.g. double(@mutable_primitive). Mutable primitives work like an array as well,"
					+ " in some cases, but not others. In general, setting of the underlying values may be done with array_push(). Assigning"
					+ " a new value to the variable works the same as assigning a new value to any other value, it overwrites the value with"
					+ " the new type. Most array functions will work with the mutable primitive, however, they will return useless data, for"
					+ " instance, array_resize() will simply set the value to the default value shown. array_size() is an exception to this"
					+ " rule, it will not work, and will throw an exception. See the examples for more use cases. In general, this is meant"
					+ " as a convenience feature for values that are passed to closures or procs, but should be passed by reference. Cloning the"
					+ " mutable primitive with the array clone operation creates a distinct copy.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "@val = mutable_primitive(0);\n"
				+ "msg('typeof: ' . typeof(@val));\n"
				+ "msg('value: ' . @val);\n"
				+ "msg('@val + 5: ' . (@val + 5)); // Works as if it were a primitive with most functions\n"
				+ "(++@val); // As a special exception to how assignments work, increment/decrement works as well\n"
				+ "msg(@val); // 1\n"),
				new ExampleScript("Basic usage with procs", "proc _testWithMutable(@a){\n"
				+ "\t@a[] = 5;\n"
				+ "}\n\n"
				+ ""
				+ "proc _testWithoutMutable(@a){\n"
				+ "\t@a = 10;\n"
				+ "}\n\n"
				+ ""
				+ "@a = mutable_primitive(0);\n"
				+ "msg(@a); // The value starts out as 0\n"
				+ "_testWithMutable(@a); // This will actually change the value\n"
				+ "msg(@a); // Here, the value is 5\n"
				+ "_testWithoutMutable(@a); // This will not change the value\n"
				+ "msg(@a); // Still the value is 5\n"),
				new ExampleScript("Basic usage with closure", "@a = mutable_primitive(0);\n"
				+ "execute(closure(){\n"
				+ "\t@a++;\n"
				+ "});\n"
				+ "msg(@a); // 1\n"),
				new ExampleScript("Cloning the value", "@a = mutable_primitive(0);\n"
				+ "@b = @a[];\n"
				+ "@a[] = 5;\n"
				+ "msg(@a);\n"
				+ "msg(@b);\n")
			};
		}

	}

	@api
	public static class _instanceof extends AbstractFunction implements Optimizable {

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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(args[0] instanceof CNull) {
				return CBoolean.FALSE;
			}
			CClassType type;
			if(args[1].isInstanceOf(CClassType.TYPE)) {
				type = (CClassType) args[1];
			} else {
				throw new RuntimeException("This should have been optimized out, this is a bug in instanceof,"
						+ " please report it");
			}
			boolean b = InstanceofUtil.isInstanceof(args[0], type, environment);
			return CBoolean.get(b);
		}

		@Override
		public String getName() {
			return "instanceof";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {value, type} Checks to see if the value is, extends, or implements the given type. Keyword usage is preferred:"
					+ " <code>@value instanceof int</code>. The opposite operation is <code>@value notinstanceof int</code>. ---- Null is a special value, while any type may be assigned null, it does not extend"
					+ " any type, and therefore \"null instanceof AnyType\" will always return false. Likewise, other than null, all"
					+ " values extend \"mixed\", and therefore \"anyNonNullValue instanceof mixed\" will always return true. There is no"
					+ " (single) functional equivalent to the notinstanceof keyword. <code>@value notinstanceof int</code> simply compiles to not(instanceof(@value, int)).";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs, List<ParseTree> children,
				FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			// There are two specific cases here where we will give more precise error messages.
			// If it's a string, yell at them
			if(children.get(1).getData().isInstanceOf(CString.TYPE)) {
				throw new ConfigCompileException("Unexpected string type passed to \"instanceof\"", t);
			}
			// If it's a variable, also yell at them
			if(children.get(1).getData() instanceof IVariable) {
				throw new ConfigCompileException("Variable types are not allowed in \"instanceof\"", t);
			}
			// Unknown error, but this is still never valid.
			if(!(children.get(1).getData().isInstanceOf(CClassType.TYPE))) {
				throw new ConfigCompileException("Unexpected type for \"instanceof\": " + children.get(1).getData(), t);
			}
			// null is technically a type, but instanceof shouldn't work with that
			if(children.get(1).getData().val().equals("null")) {
				throw new ConfigCompileException("\"null\" cannot be compared against with instanceof. Use <value> === null.", t);
			}
			// It's hardcoded, allow it, but optimize it out.
			if(children.get(0).isConst()) {
				return new ParseTree(exec(t, null, children.get(0).getData(), children.get(1).getData()), fileOptions);
			}
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "mixed @a = 5; // Actually an int\n"
				+ "msg(@a instanceof int); // true\n"
				+ "msg(@a instanceof string); // false\n"),
				new ExampleScript("Functional usage", "instanceof(5, int)"),
				new ExampleScript("Inverted usage", "mixed @a = 5;\n"
				+ "msg(@a notinstanceof int); // false\n"
				+ "msg(@a notinstanceof string); // true\n"),
				new ExampleScript("Inverted functional usage", "!instanceof(5, int)")
			};
		}

	}

}
