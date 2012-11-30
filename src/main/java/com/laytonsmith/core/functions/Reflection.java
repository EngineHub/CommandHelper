package com.laytonsmith.core.functions;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author layton
 */
public class Reflection {

	public static String docs() {
		return "This class of functions allows scripts to hook deep into the interpreter itself,"
				+ " and get meta information about the operations of a script. This is useful for"
				+ " debugging, testing, and ultra dynamic scripting. See the"
				+ " [[CommandHelper/Reflection|guide to reflection]] on the wiki for more"
				+ " details. In order to make the most of these functions, you should familiarize"
				+ " yourself with the general workings of the language. These functions explore"
				+ " extremely advanced concepts, and should normally not be used; especially"
				+ " if you are not familiar with the language.";
	}

	@api(environments={CommandHelperEnvironment.class})
	public static class reflect_pull extends AbstractFunction {

		public String getName() {
			return "reflect_pull";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public String docs() {
			return "mixed {param, [args, ...]} Returns information about the runtime in a usable"
					+ " format. Depending on the information returned, it may be useable directly,"
					+ " or it may be more of a referential format. ---- The following items can be retrieved:"
					+ "<table><tr><th>param</th><th>args</th><th>returns/description</th></tr>"
					+ "<tr><td>label</td><td></td><td>Return the label that the script is currently running under</td></tr>"
					+ "<tr><td>command</td><td></td><td>Returns the command that was used to fire off this script (if applicable)</td></tr>"
					+ "<tr><td>varlist</td><td>[name]</td><td>Returns a list of currently in scope variables. If name"
					+ " is provided, the currently set value is instead returned.</td></tr>"
					+ "<tr><td>line_num</td><td></td><td>The current line number</td></tr>"
					+ "<tr><td>file</td><td></td><td>The absolute path to the current file</td></tr>"
					+ "<tr><td>col</td><td></td><td>The current column number</td></tr>"
					+ "</table>";
			//+ "<tr><td></td><td></td><td></td></tr>"
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			if (args.length < 1) {
				throw new ConfigRuntimeException("Not enough parameters was sent to " + getName(), ExceptionType.InsufficientArgumentsException, t);
			}

			String param = args[0].val();
			if ("label".equalsIgnoreCase(param)) {
				return new CString(env.getEnv(GlobalEnv.class).GetLabel(), t);
			} else if ("command".equalsIgnoreCase(param)) {
				return new CString(env.getEnv(CommandHelperEnvironment.class).GetCommand(), t);
			} else if ("varlist".equalsIgnoreCase(param)) {
				if (args.length == 1) {
					//No name provided
					CArray ca = new CArray(t);
					for (String name : env.getEnv(GlobalEnv.class).GetVarList().keySet()) {
						ca.push(new CString(name, t));
					}
					return ca;
				} else if (args.length == 2) {
					//The name was provided
					String name = args[1].val();
					return env.getEnv(GlobalEnv.class).GetVarList().get(name, t).ival();
				}
			} else if ("line_num".equalsIgnoreCase(param)) {
				return new CInt(t.line(), t);
			} else if ("file".equalsIgnoreCase(param)) {
				if (t.file() == null) {
					return new CString("Unknown (maybe the interpreter?)", t);
				} else {
					return new CString(t.file().getAbsolutePath(), t);
				}
			} else if ("col".equalsIgnoreCase(param)) {
				return new CInt(t.col(), t);
			}

			throw new ConfigRuntimeException("The arguments passed to " + getName() + " are incorrect. Please check them and try again.",
					ExceptionType.FormatException, t);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class reflect_docs extends AbstractFunction implements Optimizable {

		public static enum DocField {

			TYPE,
			RETURN,
			ARGS,
			DESCRIPTION;

			public String getName() {
				return name().toLowerCase();
			}

			public static DocField getValue(String value) {
				return DocField.valueOf(value.toUpperCase());
			}
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			String element = args[0].val();
			DocField docField;
			try {
				docField = DocField.getValue(args[1].val());
			} catch (IllegalArgumentException e) {
				throw new ConfigRuntimeException("Invalid docField provided: " + args[1].val(), ExceptionType.FormatException, t);
			}
			//For now, we have special handling, since functions are actually the only thing that will work,
			//but eventually this will be a generic interface.
			if (element.startsWith("@")) {
				IVariable var = environment.getEnv(GlobalEnv.class).GetVarList().get(element, t);
				if (var == null) {
					throw new ConfigRuntimeException("Invalid variable provided: " + element + " does not exist in the current scope", ExceptionType.FormatException, t);
				}
			} else if (element.startsWith("_")) {
				if (!environment.getEnv(GlobalEnv.class).GetProcs().containsKey(element)) {
					throw new ConfigRuntimeException("Invalid procedure name provided: " + element + " does not exist in the current scope", ExceptionType.FormatException, t);
				}
			} else {
				try {
					Function f = (Function) FunctionList.getFunction(new CFunction(element, t));
					return new CString(formatFunctionDoc(f.docs(), docField), t);
				} catch (ConfigCompileException ex) {
					throw new ConfigRuntimeException("Unknown function: " + element, ExceptionType.FormatException, t);
				}
			}
			return new CNull(t);
		}

		public String formatFunctionDoc(String docs, DocField field) {
			Pattern p = Pattern.compile("\\s*(.*?)\\s*\\{(.*?)\\}\\s*(.*)\\s*");
			Matcher m = p.matcher(docs);
			if (!m.find()) {
				throw new Error("An error has occured in " + getName() + ". While trying to get the documentation"
						+ ", it was unable to parse this: " + docs);
			}
			if (field == DocField.RETURN || field == DocField.TYPE) {
				return m.group(1);
			} else if (field == DocField.ARGS) {
				return m.group(2);
			} else if (field == DocField.DESCRIPTION) {
				return m.group(3);
			}
			throw new Error("Unhandled case in formatFunctionDoc!");
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			if(children.isEmpty()){
				//They are requesting this function's documentation. We can just return a string,
				//and then it will never actually get called, so we handle it entirely in here.
				return new ParseTree(new CString(docs(), t), null);
			}
			if (children.get(0).isConst()) {
				//If it's a function, we can check to see if it actually exists,
				//and make it a compile error if it doesn't, even if parameter 2 is dynamic
				String value = children.get(0).getData().val();
				if (!value.startsWith("_") && !value.startsWith("@")) {
					//It's a function
					FunctionList.getFunction(new CFunction(value, t));
				}
			}
			if (children.get(1).isConst()) {
				try {
					DocField.getValue(children.get(1).getData().val());
				} catch (IllegalArgumentException e) {
					throw new ConfigCompileException("Invalid docField provided: " + children.get(1).getData().val(), t);
				}
			}
			return null;
		}
		
		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.OPTIMIZE_DYNAMIC
			);
		}

		public String getName() {
			return "reflect_docs";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 2};
		}

		public String docs() {
			return "string { | element, docField} Returns the documentation for an element. There are 4 things that an element might have,"
					+ " and one of these should be passed as the docField argument: type, return, args, description. A valid element is either"
					+ " the name of an ivariable, or a function/proc. For instance, reflect_docs('reflect_docs', 'description') would return"
					+ " what you are reading right now. User defined variables and procs may not have any documentation, in which case null"
					+ " is returned. If the specified argument cannot be found, a FormatException is thrown. If no arguments are passed in,"
					+ " it returns the documentation for " + getName() + ", that is, what you're reading right now.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
}
