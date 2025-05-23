package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassMirror;
import com.laytonsmith.PureUtilities.ClassLoading.DynamicEnum;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.MDynamicEnum;
import com.laytonsmith.annotations.MEnum;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Procedure;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.compiler.KeywordList;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.NativeTypeList;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.environments.StaticRuntimeEnv;
import com.laytonsmith.core.events.Event;
import com.laytonsmith.core.events.EventList;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIOException;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.CRE.CREInsufficientArgumentsException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.MEnumTypeValue;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.objects.ObjectDefinition;
import com.laytonsmith.core.objects.ObjectDefinitionTable;
import com.laytonsmith.core.telemetry.Telemetry;
import com.laytonsmith.persistence.DataSourceFactory;
import com.laytonsmith.persistence.PersistenceNetwork;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
@core
public class Reflection {

	public static String docs() {
		return "This class of functions allows scripts to hook deep into the interpreter itself,"
				+ " and get meta information about the operations of a script. This is useful for"
				+ " debugging, testing, and ultra dynamic scripting. See the"
				+ " [[Reflection|guide to reflection]] on the wiki for more"
				+ " details. In order to make the most of these functions, you should familiarize"
				+ " yourself with the general workings of the language. These functions explore"
				+ " extremely advanced concepts, and should normally not be used; especially"
				+ " if you are not familiar with the language.";
	}

	@api
	public static class reflect_pull extends AbstractFunction {

		private static Set<Mixed> protocols;

		@Override
		public String getName() {
			return "reflect_pull";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "mixed {param, [args, ...]} Returns information about the runtime in a usable"
					+ " format. Depending on the information returned, it may be usable directly,"
					+ " or it may be more of a referential format. ---- The following items can be retrieved:"
					+ "{|\n"
					+ "|-\n"
					+ "! param\n"
					+ "! args\n"
					+ "! returns/description\n"
					+ "|-\n"
					+ "| label\n"
					+ "| \n"
					+ "| Return the label that the script is currently running under\n"
					+ "|-\n"
					+ "| command\n"
					+ "|\n"
					+ "| Returns the command that was used to fire off this script (if applicable)\n"
					+ "|-\n"
					+ "| varlist\n"
					+ "| [name]\n"
					+ "| Returns a list of currently in scope variables. If name"
					+ " is provided, the currently set value is instead returned.\n"
					+ "|-\n"
					+ "| line_num\n"
					+ "|\n"
					+ "| The current line number\n"
					+ "|-\n"
					+ "| file\n"
					+ "|\n"
					+ "| The absolute path to the current file\n"
					+ "|-\n"
					+ "| dir\n"
					+ "|\n"
					+ "| The absolute path to the directory that the current file is in\n"
					+ "|-\n"
					+ "| col\n"
					+ "|\n"
					+ "| The current column number\n"
					+ "|-\n"
					+ "| datasources\n"
					+ "|\n"
					+ "| An array of data source protocols available\n"
					+ "|-\n"
					+ "| enum\n"
					+ "| [enum name]\n"
					+ "| An array of enum names, or if one is provided, a list of all"
					+ " the values in that enum\n"
					+ "|-\n"
					+ "| keywords\n"
					+ "| [keyword name]\n"
					+ "| Lists the keywords, if no parameter is provided, otherwise"
					+ " provides the documentation for the specified keyword\n"
					+ "|-\n"
					+ "| telemetry_session\n"
					+ "|\n"
					+ "| The session id used in telemetry for the current session. This will return null if"
					+ " telemetry is disabled. In general, this key is used to track individual sessions. If"
					+ " needed, this can be provided to the maintainers to associate your data with you. If"
					+ " you wish for your telemetry data to remain anonymous, do not provide this key to the"
					+ " maintainers. (People without access to the telemetry system cannot do anything with"
					+ " the key anyways.)\n"
					+ "|-\n"
					+ "| file_options_author\n"
					+ "|\n"
					+ "| Returns the author name, as set in the file options. Empty string is returned if it"
					+ " is not set.\n"
					+ "|-\n"
					+ "| file_options_created\n"
					+ "|\n"
					+ "| Returns the created date, as set in the file options. Empty string is returned if it"
					+ " is not set.\n"
					+ "|-\n"
					+ "| file_options_description\n"
					+ "|\n"
					+ "| Returns the file description, as set in the file options. Empty string is returned if it"
					+ " is not set.\n"
					+ "|-\n"
					+ "| file_options_copyright\n"
					+ "|\n"
					+ "| Returns the copyright information, as set in the file options. Empty string is returned if it"
					+ " is not set.\n"
					+ "|-\n"
					+ "| file_options_license\n"
					+ "|\n"
					+ "| Returns the code license, as set in the file options. Empty string is returned if it"
					+ " is not set.\n"
					+ "|}";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CREIOException.class};
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
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			if(args.length < 1) {
				throw new CREInsufficientArgumentsException("Not enough parameters was sent to " + getName(), t);
			}

			String param = args[0].val();
			if("label".equalsIgnoreCase(param)) {
				return new CString(env.getEnv(GlobalEnv.class).GetLabel(), t);
			} else if("command".equalsIgnoreCase(param)) {
				return new CString(env.getEnv(CommandHelperEnvironment.class).GetCommand(), t);
			} else if("varlist".equalsIgnoreCase(param)) {
				if(args.length == 1) {
					//No name provided
					CArray ca = new CArray(t);
					for(String name : env.getEnv(GlobalEnv.class).GetVarList().keySet()) {
						ca.push(new CString(name, t), t);
					}
					return ca;
				} else if(args.length == 2) {
					//The name was provided
					String name = args[1].val();
					return env.getEnv(GlobalEnv.class).GetVarList().get(name, t, env).ival();
				}
			} else if("line_num".equalsIgnoreCase(param)) {
				return new CInt(t.line(), t);
			} else if("file".equalsIgnoreCase(param)) {
				if(t.file() == null) {
					return new CString("Unknown (maybe the interpreter?)", t);
				} else {
					try {
						return new CString(t.file().getCanonicalPath().replace('\\', '/'), t);
					} catch (IOException ex) {
						throw new CREIOException(ex.getMessage(), t);
					}
				}
			} else if("dir".equalsIgnoreCase(param)) {
				if(t.file() == null) {
					return new CString("Unknown (maybe the interpreter?)", t);
				} else {
					try {
						String dir = t.file().getParentFile().getCanonicalPath().replace('\\', '/') + "/";
						return new CString(dir, t);
					} catch (IOException ex) {
						throw new CREIOException(ex.getMessage(), t);
					}
				}
			} else if("col".equalsIgnoreCase(param)) {
				return new CInt(t.col(), t);
			} else if("datasources".equalsIgnoreCase(param)) {
				if(protocols == null) {
					protocols = new HashSet<>();
					for(String s : DataSourceFactory.GetSupportedProtocols()) {
						protocols.add(new CString(s, Target.UNKNOWN));
					}
				}
				return new CArray(t, protocols);
			} else if("enum".equalsIgnoreCase(param)) {
				CArray a = new CArray(t);
				Set<ClassMirror<? extends Enum>> enums = ClassDiscovery.getDefaultInstance().getClassesWithAnnotationThatExtend(MEnum.class, Enum.class);
				Set<ClassMirror<? extends DynamicEnum>> dEnums = ClassDiscovery.getDefaultInstance().getClassesWithAnnotationThatExtend(MDynamicEnum.class, DynamicEnum.class);
				if(args.length == 1) {
					try {
						//No name provided
						for(ClassMirror<? extends Enum> e : enums) {
							String name = (String) e.getAnnotation(MEnum.class).getValue("value");
							a.push(CClassType.get(FullyQualifiedClassName.forNativeEnum(e.loadClass())), t);
						}
						for(ClassMirror<? extends DynamicEnum> d : dEnums) {
							String name = (String) d.getAnnotation(MDynamicEnum.class).getValue("value");
							a.push(CClassType.get(FullyQualifiedClassName.forFullyQualifiedClass(name)), t);
						}
					} catch (ClassNotFoundException ex) {
						throw new Error(ex);
					}
				} else if(args.length == 2) {
					FullyQualifiedClassName enumName = FullyQualifiedClassName.forName(args[1].val(), t, env);
					try {
						for(MEnumTypeValue v : NativeTypeList.getNativeEnumType(enumName).values()) {
							a.push(v, t);
						}
					} catch (ClassNotFoundException ex) {
						// Actually, I don't think this can
						throw new CRECastException("Cannot find enum of type " + enumName, t, ex);
					}
				}
				return a;
			} else if("keywords".equalsIgnoreCase(param)) {
				if(args.length == 1) {
					CArray a = new CArray(t);
					List<String> l = new ArrayList<>(KeywordList.getKeywordNames());
					l.forEach((String t1) -> {
						a.push(new CString(t1, Target.UNKNOWN), Target.UNKNOWN);
					});
					return new ArrayHandling.array_sort().exec(t, env, a);
				} else if(args.length == 2) {
					Keyword k = KeywordList.getKeywordByName(args[1].val());
					if(k == null) {
						throw new CREIllegalArgumentException(args[1].val() + " is not a valid keyword", t);
					}
					return new CString(k.docs(), Target.UNKNOWN);
				}
			} else if("telemetry_session".equalsIgnoreCase(param)) {
				String session = Telemetry.GetDefault().getSessionId();
				if(session == null) {
					return CNull.NULL;
				}
				return new CString(session, t);
			} else if("file_options_author".equalsIgnoreCase(param)) {
				String author = env.getEnv(GlobalEnv.class).GetFileOptions().getAuthor();
				return new CString(author, t);
			} else if("file_options_created".equalsIgnoreCase(param)) {
				String created = env.getEnv(GlobalEnv.class).GetFileOptions().getCreated();
				return new CString(created, t);
			} else if("file_options_description".equalsIgnoreCase(param)) {
				String description = env.getEnv(GlobalEnv.class).GetFileOptions().getDescription();
				return new CString(description, t);
			} else if("file_options_copyright".equalsIgnoreCase(param)) {
				String copyright = env.getEnv(GlobalEnv.class).GetFileOptions().getCopyright();
				return new CString(copyright, t);
			} else if("file_options_license".equalsIgnoreCase(param)) {
				String license = env.getEnv(GlobalEnv.class).GetFileOptions().getLicense();
				return new CString(license, t);
			}

			throw new CREFormatException("The arguments passed to " + getName() + " are incorrect. Please check them and try again.", t);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class reflect_type extends AbstractFunction {

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
			CClassType type = ArgumentValidation.getClassType(args[0], t);
			CArray ret = CArray.GetAssociativeArray(t);
			ret.set("fqcn", type.getFQCN().getFQCN());
			ret.set("name", type.getFQCN().getSimpleName());
			ret.set("interfaces", new CArray(t, type.getTypeInterfaces(environment)), t);
			ret.set("superclasses", new CArray(t, type.getTypeSuperclasses(environment)), t);

			CArray typeDocs = new CArray(t);
			// When type unions are a thing, this will need to be implemented slightly differently.
			for(CClassType m : Arrays.asList(type)) {
				CArray docs = CArray.GetAssociativeArray(t);
				docs.set("package", type.getPackage() == null ? CNull.NULL : type.getPackage(), t);
				docs.set("isNative", CBoolean.get(type.getNativeType() != null), t);
				docs.set("docs", m.getTypeDocs(t, environment));
				docs.set("since", m.getTypeSince(t, environment).toString());
				typeDocs.push(docs, t);
			}
			ret.set("typeDocs", typeDocs, t);
			return ret;
		}

		@Override
		public String getName() {
			return "reflect_type";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return getBundledDocs();
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("With ClassType references", "reflect_type(string)"),
				new ExampleScript("Via typeof()", "int @i = 1;\nmsg(reflect_type(typeof(@i)));")
			};
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

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class};
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
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			String element = args[0].val();
			DocField docField;
			try {
				docField = DocField.getValue(args[1].val());
			} catch (IllegalArgumentException e) {
				throw new CREFormatException("Invalid docField provided: " + args[1].val(), t);
			}
			//For now, we have special handling, since functions are actually the only thing that will work,
			//but eventually this will be a generic interface.
			if(element.startsWith("@")) {
				IVariable var = env.getEnv(GlobalEnv.class).GetVarList().get(element, t, env);
				if(var == null) {
					throw new CREFormatException("Invalid variable provided: " + element + " does not exist in the current scope", t);
				}
			} else if(element.startsWith("_")) {
				if(!env.getEnv(GlobalEnv.class).GetProcs().containsKey(element)) {
					throw new CREFormatException("Invalid procedure name provided: " + element + " does not exist in the current scope", t);
				}
			} else {
				try {
					Function f = (Function) FunctionList.getFunction(new CFunction(element, t), env.getEnvClasses());
					return new CString(formatFunctionDoc(f.docs(), docField), t);
				} catch (ConfigCompileException ex) {
					throw new CREFormatException("Unknown function: " + element, t);
				}
			}
			return CNull.NULL;
		}

		public String formatFunctionDoc(String docs, DocField field) {
			Pattern p = Pattern.compile("(?s)\\s*(.*?)\\s*\\{(.*?)\\}\\s*(.*)\\s*");
			Matcher m = p.matcher(docs);
			if(!m.find()) {
				throw new Error("An error has occurred in " + getName() + ". While trying to get the documentation"
						+ ", it was unable to parse this: " + docs);
			}
			if(field == DocField.RETURN || field == DocField.TYPE) {
				return m.group(1);
			} else if(field == DocField.ARGS) {
				return m.group(2);
			} else if(field == DocField.DESCRIPTION) {
				return m.group(3);
			}
			throw new Error("Unhandled case in formatFunctionDoc!");
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(children.isEmpty()) {
				//They are requesting this function's documentation. We can just return a string,
				//and then it will never actually get called, so we handle it entirely in here.
				return new ParseTree(new CString(docs(), t), null);
			}
			if(children.size() == 1) {
				return null; // not enough arguments, so an exception will be thrown later
			}
			if(children.get(0).isConst()) {
				//If it's a function, we can check to see if it actually exists,
				//and make it a compile error if it doesn't, even if parameter 2 is dynamic
				String value = children.get(0).getData().val();
				if(!value.startsWith("_") && !value.startsWith("@")) {
					//It's a function
					FunctionList.getFunction(new CFunction(value, t), env.getEnvClasses());
				}
			}
			if(children.get(1).isConst()) {
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

		@Override
		public String getName() {
			return "reflect_docs";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 2};
		}

		@Override
		public String docs() {
			return "string { | element, docField} Returns the documentation for an element. There are 4 things that an element might have,"
					+ " and one of these should be passed as the docField argument: type, return, args, description. A valid element is either"
					+ " the name of an ivariable, or a function/proc. For instance, reflect_docs('reflect_docs', 'description') would return"
					+ " what you are reading right now. User defined variables and procs may not have any documentation, in which case null"
					+ " is returned. If the specified argument cannot be found, a FormatException is thrown. If no arguments are passed in,"
					+ " it returns the documentation for " + getName() + ", that is, what you're reading right now.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Return type", "reflect_docs('array_contains', 'return'); // Using 'type' would also work"),
				new ExampleScript("Args", "reflect_docs('array_contains', 'args');"),
				new ExampleScript("Description", "reflect_docs('array_contains', 'description');")
			};
		}
	}

	@api
	public static class get_functions extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		private static final Map<String, List<String>> FUNCS = new HashMap<String, List<String>>();

		private void initf(Environment env) {
			for(FunctionBase f : FunctionList.getFunctionList(api.Platforms.INTERPRETER_JAVA, env.getEnvClasses())) {
				String[] pack = f.getClass().getEnclosingClass().getName().split("\\.");
				String clazz = pack[pack.length - 1];
				if(!FUNCS.containsKey(clazz)) {
					FUNCS.put(clazz, new ArrayList<String>());
				}
				FUNCS.get(clazz).add(f.getName());
			}
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			CArray ret = CArray.GetAssociativeArray(t);
			if(FUNCS.keySet().size() < 10) {
				initf(environment);
			}
			for(String cname : FUNCS.keySet()) {
				CArray fnames = new CArray(t);
				for(String fname : FUNCS.get(cname)) {
					fnames.push(new CString(fname, t), t);
				}
				ret.set(new CString(cname, t), fnames, t);
			}
			return ret;
		}

		@Override
		public String getName() {
			return "get_functions";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} Returns an associative array of all loaded functions. The keys of this array are the"
					+ " names of the classes containing the functions (which you know as the sections of the API page),"
					+ " and the values are arrays of the names of the functions within those classes.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class get_events extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment,
				Mixed... args) throws ConfigRuntimeException {
			CArray ret = new CArray(t);
			for(Event event : EventList.GetEvents()) {
				ret.push(new CString(event.getName(), t), t);
			}
			ret.sort(CArray.ArraySortType.STRING_IC);
			return ret;
		}

		@Override
		public String getName() {
			return "get_events";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} Returns an array of all registered event names.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_aliases extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			CArray ret = new CArray(t);
			for(Script s : Static.getAliasCore().getScripts()) {
				ret.push(new CString(s.getSignature(), t), t);
			}
			ret.sort(CArray.ArraySortType.STRING_IC);
			return ret;
		}

		@Override
		public String getName() {
			return "get_aliases";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} Returns an array of the defined alias signatures (The part left of the = sign).";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class reflect_value_source extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
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
			PersistenceNetwork pn = environment.getEnv(StaticRuntimeEnv.class).GetPersistenceNetwork();
			return new CString(pn.getKeySource(args[0].val().split("\\.")).toString(), t);
		}

		@Override
		public String getName() {
			return "reflect_value_source";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {persistenceKey} Returns the source file that this key will store a value to in the Persistence Network."
					+ " For instance, in your persistence.ini file, if you have the entry \"storage.test.**=json:///path/to/file.json\","
					+ " then reflect_value_source('storage.test.testing') would return 'json:///path/to/file.json'. This is useful for"
					+ " debugging, as it will definitively trace back the source/destination of a value. Note that unlike get and"
					+ " store_value, the \"storage\" prefix is required, if you're searching for values that are stored using"
					+ " these functions.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	public static class get_procedures extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			CArray ret = new CArray(t);
			for(Map.Entry<String, Procedure> p : environment.getEnv(GlobalEnv.class).GetProcs().entrySet()) {
				ret.push(new CString(p.getKey(), t), t);
			}
			ret.sort(CArray.ArraySortType.STRING_IC);
			return ret;
		}

		@Override
		public String getName() {
			return "get_procedures";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} Returns an array of procedures callable in the current scope.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Simple example", "msg(get_procedures());\n"
				+ "proc _testProc() {}\n"
				+ "msg(get_procedures());"),
				new ExampleScript("Example with procedures within procedures", "msg(get_procedures());\n"
				+ "proc _testProc() {\n"
				+ "\tproc _innerProc() {}\n"
				+ "\tmsg(get_procedures());\n"
				+ "}\n"
				+ "_testProc();\n"
				+ "msg(get_procedures());")
			};
		}
	}

	@api
	public static class get_classes extends AbstractFunction {

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
			CArray ret = new CArray(t);
			ObjectDefinitionTable odt = environment.getEnv(CompilerEnvironment.class).getObjectDefinitionTable();
			for(ObjectDefinition od : odt.getObjectDefinitionSet()) {
				try {
					ret.push(CClassType.get(FullyQualifiedClassName.forFullyQualifiedClass(od.getClassName())), t);
				} catch (ClassNotFoundException ex) {
					throw ConfigRuntimeException.CreateUncatchableException(ex.getMessage(), t);
				}
			}
//			for(FullyQualifiedClassName c : NativeTypeList.getNativeTypeList()) {
//				CClassType cct;
//				try {
//					cct = CClassType.get(c);
//				} catch (ClassNotFoundException ex) {
//					throw ConfigRuntimeException.CreateUncatchableException(ex.getMessage(), t);
//				}
//				if(cct == CNull.TYPE) {
//					continue;
//				}
//				ret.push(cct, t);
//			}
			return ret;
		}

		@Override
		public String getName() {
			return "get_classes";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} Returns a list of all known classes. This may not be completely exhaustive, but will"
					+ " at least contain all system defined classes. The returned value is an array of ClassType"
					+ " objects.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_3;
		}

	}

	@api
	public static class class_type extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRENotFoundException.class};
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
			String type = ArgumentValidation.getStringObject(args[0], t);
			try {
				return CClassType.get(FullyQualifiedClassName.forName(args[0].val(), t, environment));
			} catch (CRECastException | ClassNotFoundException ex) {
				throw new CRENotFoundException("Could not find type " + type, t);
			}
		}

		@Override
		public String getName() {
			return "class_type";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "ClassType {string name} Returns a ClassType object for the given fully qualified class name."
					+ " If the given class doesn't exist, a NotFoundException is thrown.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

	}
}
