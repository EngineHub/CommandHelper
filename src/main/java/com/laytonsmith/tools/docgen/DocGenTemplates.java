package com.laytonsmith.tools.docgen;

import com.laytonsmith.PureUtilities.ArgumentParser;
import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassMirror;
import com.laytonsmith.PureUtilities.Common.ArrayUtils;
import com.laytonsmith.PureUtilities.Common.HTMLUtils;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.MSP.Burst;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.annotations.datasource;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CommandLineTool;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Main;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.SimpleDocumentation;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;
import com.laytonsmith.core.functions.Scheduling;
import com.laytonsmith.core.tool;
import com.laytonsmith.persistence.DataSource;
import com.laytonsmith.persistence.MySQLDataSource;
import com.laytonsmith.persistence.SQLiteDataSource;
import com.laytonsmith.tools.Manager;
import com.laytonsmith.tools.SimpleSyntaxHighlighter;
import com.laytonsmith.tools.docgen.DocGenTemplates.Generator.GenerateException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class DocGenTemplates {

	public static interface Generator {

		public static class GenerateException extends Exception {

			public GenerateException() {
				super();
			}

			public GenerateException(String message) {
				super(message);
			}

			public GenerateException(String message, Throwable cause) {
				super(message, cause);
			}
		}

		public String generate(String... args) throws GenerateException;
	}

	public static void main(String[] args) throws Exception {
		Implementation.setServerType(Implementation.Type.SHELL);
		Map<String, Generator> g = new HashMap<>();
		g.put("A", new Generator() {
			@Override
			public String generate(String... args) throws GenerateException {
				return "(" + args[0] + ")";
			}
		});
		g.put("B", new Generator() {
			@Override
			public String generate(String... args) throws GenerateException {
				return "<text>";
			}
		});
		g.putAll(DocGenTemplates.GetGenerators());
		String t = "<%SYNTAX|html|\n<%MYSQL_CREATE_TABLE_QUERY%>\n%>";
		StreamUtils.GetSystemOut().println(DoTemplateReplacement(t, g));
	}

	public static String Generate(String forPage) {
		return Generate(forPage, new HashMap<String, String>());
	}

	public static String Generate(String forPage, Map<String, String> customTemplates) {
		try {
			Prefs.init(null);
		} catch (IOException ex) {
			Logger.getLogger(DocGenTemplates.class.getName()).log(Level.SEVERE, null, ex);
		}
		ClassDiscovery.getDefaultInstance().addDiscoveryLocation(ClassDiscovery.GetClassContainer(DocGenTemplates.class));
		//Grab the template from the resources
		String template = StreamUtils.GetString(DocGenTemplates.class.getResourceAsStream("/docs/" + forPage));
		//Find all the %%templates%% in the template
		Matcher m = Pattern.compile("%%([^\\|%]+)([^%]*?)%%").matcher(template);
		StringBuilder templateBuilder = new StringBuilder();
		int lastMatch = 0;
		boolean appended = false;
		while(m.find()) {
			if(!appended) {
				templateBuilder.append(template.substring(lastMatch, m.start()));
				appended = true;
			}
			String name = m.group(1);
			for(String templateName : customTemplates.keySet()) {
				if(templateName.equals(name)) {
					templateBuilder.append(customTemplates.get(name));
					lastMatch = m.end();
					appended = false;
					//template = template.replaceAll("%%" + Pattern.quote(name) + ".*?%%", customTemplates.get(name));
				}
			}
			try {
				Field f = DocGenTemplates.class.getDeclaredField(name);
				f.setAccessible(true);
				if(Generator.class.isAssignableFrom(f.getType()) && Modifier.isStatic(f.getModifiers())) {
					String[] tmplArgs = ArrayUtils.EMPTY_STRING_ARRAY;
					if(m.group(2) != null && !m.group(2).isEmpty()) {
						//We have arguments
						//remove the initial |, then split (noting that empty arguments is still an argument)
						tmplArgs = m.group(2).substring(1).split("\\|", -1);
					}
					String templateValue = ((Generator) f.get(null)).generate(tmplArgs);
					templateBuilder.append(templateValue);
					lastMatch = m.end();
					appended = false;
					//template = template.replaceAll("%%" + Pattern.quote(name) + "%%", templateValue);
				} else {
					throw new Error(DocGenTemplates.class.getSimpleName() + "." + f.getName()
							+ " is not an instance of " + Generator.class.getSimpleName()
							+ ", or is not static. Please correct this error to use it as a template.");
				}
			} catch (Exception e) {
				System.out.println(e);
				//Oh well, skip it.
			}
		}
		if(!appended) {
			templateBuilder.append(template.substring(lastMatch));
		}
		return templateBuilder.toString();
	}

	/**
	 * Returns all the generators defined in this class.
	 *
	 * @return
	 */
	public static Map<String, Generator> GetGenerators() {
		Map<String, Generator> generators = new HashMap<>();
		for(Field f : DocGenTemplates.class.getDeclaredFields()) {
			if(Generator.class.isAssignableFrom(f.getType())) {
				try {
					generators.put(f.getName(), (Generator) f.get(null));
				} catch (IllegalArgumentException | IllegalAccessException ex) {
					Logger.getLogger(DocGenTemplates.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
		return generators;
	}

	/**
	 * A utility method to replace all template methods in a generic template string.
	 *
	 * @param template The template string on which to perform the replacements
	 * @param generators The list of String-Generator entries, where the String is the template name, and the Generator
	 * is the replacement to use.
	 * @return
	 */
	public static String DoTemplateReplacement(String template, Map<String, Generator> generators) throws GenerateException {
		try {
			if(Implementation.GetServerType() != Implementation.Type.BUKKIT) {
				Prefs.init(null);
			}
		} catch (IOException ex) {
			Logger.getLogger(DocGenTemplates.class.getName()).log(Level.SEVERE, null, ex);
		}
		ClassDiscovery.getDefaultInstance().addDiscoveryLocation(ClassDiscovery.GetClassContainer(DocGenTemplates.class));

		// Find all the <%templates%> (which are the same as %%templates%%, but are nestable)
		int templateStack = 0;
		StringBuilder tBuilder = new StringBuilder();
		StringBuilder tArgument = new StringBuilder();
		for(int i = 0; i < template.length(); i++) {
			Character c1 = template.charAt(i);
			Character c2 = '\0';
			if(i < template.length() - 1) {
				c2 = template.charAt(i + 1);
			}
			if(c1 == '<' && c2 == '%') {
				// start template
				templateStack++;
				if(templateStack == 1) {
					i++;
					continue;
				}
			}

			if(c1 == '%' && c2 == '>') {
				// end template
				templateStack--;
				if(templateStack == 0) {
					// Process the template now
					String[] args = tArgument.toString().split("\\|");
					String name = args[0];
					if(args.length > 1) {
						args = ArrayUtils.slice(args, 1, args.length - 1);
					}
					// Loop over the arguments and resolve them first
					for(int j = 0; j < args.length; j++) {
						args[j] = DoTemplateReplacement(args[j], generators);
					}
					if(generators.containsKey(name)) {
						String result = generators.get(name).generate(args);
						tBuilder.append(result);
					}
					tArgument = new StringBuilder();
				}
				if(templateStack == 0) {
					i++;
					continue;
				}
			}
			if(templateStack == 0) {
				tBuilder.append(c1);
			} else {
				tArgument.append(c1);
			}
		}
		template = tBuilder.toString();
		//Find all the %%templates%% in the template
		Matcher m = Pattern.compile("(?:%|<)%([^\\|%]+)([^%]*?)%(?:%|>)").matcher(template);
		StringBuilder templateBuilder = new StringBuilder();
		int lastMatch = 0;
		boolean appended = false;
		while(m.find()) {
			if(!appended) {
				templateBuilder.append(template.substring(lastMatch, m.start()));
				appended = true;
			}
			String name = m.group(1);
			try {
				if(generators.containsKey(name)) {
					String[] tmplArgs = ArrayUtils.EMPTY_STRING_ARRAY;
					if(m.group(2) != null && !m.group(2).isEmpty()) {
						//We have arguments
						//remove the initial |, then split
						tmplArgs = m.group(2).substring(1).split("\\|");
					}
					for(int i = 0; i < tmplArgs.length; i++) {
						tmplArgs[i] = DoTemplateReplacement(tmplArgs[i], generators);
					}
					String templateValue = generators.get(name).generate(tmplArgs);
					templateBuilder.append(templateValue);
					//template = template.replaceAll("%%" + Pattern.quote(name) + "%%", templateValue);
				}
				lastMatch = m.end();
				appended = false;
			} catch (GenerateException e) {
				throw e;
			} catch (Exception e) {
				//Oh well, skip it.
				e.printStackTrace();
			}
		}
		if(!appended) {
			templateBuilder.append(template.substring(lastMatch));
		}
		return templateBuilder.toString();
	}

	/**
	 * Returns a wiki table of the data source modifier list, along with the docs for each one.
	 */
	public static final Generator DATA_SOURCE_MODIFIERS = new Generator() {

		@Override
		public String generate(String... args) {
			StringBuilder b = new StringBuilder();
			for(DataSource.DataSourceModifier mod : DataSource.DataSourceModifier.values()) {
				b.append("|-\n| ").append(mod.getName().toLowerCase()).append(" || ").append(HTMLUtils.escapeHTML(mod.docs())).append("\n");

			}
			return b.toString();
		}
	};

	/**
	 * Returns a wiki table with documentation for each supported persistence data type.
	 */
	public static final Generator PERSISTENCE_CONNECTIONS = new Generator() {

		@Override
		public String generate(String... args) {
			Set<Class<?>> classes = ClassDiscovery.getDefaultInstance().loadClassesWithAnnotation(datasource.class);
			Pattern p = Pattern.compile("(?s)\\s*(.*?)\\s*\\{\\s*(.*?)\\s*\\}\\s*(.*)\\s*$");
			SortedSet<String> set = new TreeSet<String>();
			for(Class c : classes) {
				if(DataSource.class.isAssignableFrom(c)) {
					try {
						Constructor constructor;
						try {
							constructor = c.getDeclaredConstructor();
							constructor.setAccessible(true);
						} catch (NoSuchMethodException e) {
							throw new RuntimeException("No no-argument constructor was found for " + c.getName() + ". A no-arg constructor must be provided, even if it is"
									+ " private, so that the documentation functions can be accessed.", e);
						}
						DataSource ds = (DataSource) constructor.newInstance();
						String docs = ds.docs();
						Matcher m = p.matcher(docs);
						String name = null;
						String example = null;
						String description = null;
						if(m.find()) {
							name = m.group(1);
							example = m.group(2);
							description = m.group(3);
						}
						if(name == null || example == null || description == null) {
							throw new Error("Invalid documentation for " + c.getSimpleName()
									+ (name == null ? " name was null;" : "")
									+ (example == null ? " example was null;" : "")
									+ (description == null ? " description was null;" : ""));
						}
						StringBuilder b = new StringBuilder();
						b.append("|-\n| ").append(name).append("\n| ").append(description)
								.append("\n| ").append(example).append("\n| ").append(ds.since().toString()).append("\n");
						set.add(b.toString());
					} catch (Exception e) {
						throw new Error(e);
					}
				} else {
					throw new Error("@datasource implementations must implement DataSource.");
				}
			}
			return StringUtils.Join(set, "");
		}

	};

	/**
	 * Returns header listing of all the compiler optimization types
	 */
	public static final Generator OPTIMIZATION_EXPLANATIONS = new Generator() {

		@Override
		public String generate(String... args) {
			StringBuilder b = new StringBuilder();
			for(Optimizable.OptimizationOption option : Optimizable.OptimizationOption.values()) {
				b.append("=== ").append(option.getName()).append(" ===\n");
				b.append(option.docs()).append("\n\n");
				b.append("Since ").append(option.since()).append("\n\n");
			}
			return b.toString();
		}

	};

	public static final Generator BURST_VALUE_TYPES = new Generator() {

		@Override
		public String generate(String... args) {
			return StringUtils.Join(Burst.BurstType.values(), ", ", ", or ", " or ");
		}
	};

	public static final Generator BURST_TYPE_DOCS = new Generator() {

		@Override
		public String generate(String... args) {
			//TODO
			return "TODO";
		}
	};

	/**
	 * Returns a header list of all known exception types, along with their docs
	 */
	public static final Generator EXCEPTION_TYPES = new Generator() {

		@Override
		public String generate(String... args) {
			StringBuilder b = new StringBuilder();
			SortedSet<Class<? extends CREThrowable>> set = new TreeSet<>(new Comparator<Class<? extends CREThrowable>>() {
				@Override
				public int compare(Class<? extends CREThrowable> o1, Class<? extends CREThrowable> o2) {
					return o1.getCanonicalName().compareTo(o2.getCanonicalName());
				}
			});
			set.addAll(ClassDiscovery.getDefaultInstance().loadClassesWithAnnotationThatExtend(typeof.class, CREThrowable.class));
			for(Class<? extends CREThrowable> c : set) {
				// This is suuuuuuper evil, but we don't want to have to deal with the exception constructors, we're
				// just after the documentation stuff.
				SimpleDocumentation d = (SimpleDocumentation) ReflectionUtils.instantiateUnsafe(c);
				b.append("===").append(d.getName()).append("===\n");
				b.append(d.docs());
				b.append("\n\nSince: ").append(d.since().toString()).append("\n\n");
			}
			return b.toString();
		}
	};

	private static final String GITHUB_BASE_URL =
			"https://github.com/EngineHub/commandhelper/tree/master/src/main/java";

	/**
	 * Returns the fully qualified (and github linked) class name, given the package regex and complete class name. For
	 * instance: %%GET_CLASS|.*|DocGenTemplates%% would (likely) return [http://url.to.github.com/path/to/file/
	 * com/laytonsmith/tools/docgen/DocGenTemplates]
	 */
	public static final Generator GET_CLASS = new Generator() {

		@Override
		public String generate(String... args) {
			Class c = ClassDiscovery.getDefaultInstance().forFuzzyName(args[0], args[1]).loadClass();
			return "[" + GITHUB_BASE_URL + "/" + c.getName().replace('.', '/') + ".java " + c.getName() + "]";
		}
	};

	/**
	 * Returns the base github url for sources.
	 */
	public static final Generator GITHUB_URL = new Generator() {

		@Override
		public String generate(String... args) {
			return GITHUB_BASE_URL;
		}
	};

	/**
	 * Returns the (github linked) simple class name, given the package regex and complete class name. For instance:
	 * %%GET_CLASS|.*|DocGenTemplates%% would (likely) return [http://url.to.github.com/path/to/file/ DocGenTemplates]
	 */
	public static final Generator GET_SIMPLE_CLASS = new Generator() {

		@Override
		public String generate(String... args) {
			ClassMirror m = ClassDiscovery.getDefaultInstance().forFuzzyName(args[0], args[1]);
			if(m == null) {
				throw new NullPointerException("Could not find class " + args[0] + " " + args[1]);
			}
			Class c = m.loadClass();
			return "[" + GITHUB_BASE_URL + "/" + c.getName().replace('.', '/') + ".java " + c.getSimpleName() + "]";
		}
	};

	/**
	 * Returns a github link to the source file this function is in. Note that it is not possible to get more specific
	 * line information (at least generically). %%GET_FUNCTION_FILE|msg%%, for instance.
	 */
	public static final Generator GET_FUNCTION_FILE = new Generator() {

		@Override
		public String generate(String... args) {
			try {
				FunctionBase b = FunctionList.getFunction(args[0], null, Target.UNKNOWN);
				Class c = b.getClass();
				while(c.getEnclosingClass() != null) {
					c = c.getEnclosingClass();
				}
				return "[" + GITHUB_BASE_URL + "/" + c.getName().replace('.', '/') + ".java " + b.getName() + "]";
			} catch (ConfigCompileException ex) {
				return "Unknown function: " + args[0];
			}

		}
	};

	public static Generator cmdlinehelp = new Generator() {

		@Override
		public String generate(String... args) {
			StringBuilder b = new StringBuilder();
			boolean colorsDisabled = TermColors.ColorsDisabled();
			TermColors.DisableColors();
			Main.CmdlineToolCollection collection = Main.GetCommandLineTools();
			b.append("<pre style=\"white-space: pre-wrap;\">\n")
					.append(HTMLUtils.escapeHTML(collection.getSuite().getBuiltDescription())).append("\n</pre>\n");
			if(!colorsDisabled) {
				TermColors.EnableColors();
			}
			for(Map.Entry<ArgumentParser, CommandLineTool> e : collection.getDynamicTools().entrySet()) {
				b.append("==== ")
						.append(e.getValue().getClass().getAnnotation(tool.class).value())
						.append(" ====\n<pre style=\"white-space: pre-wrap;\">");
				b.append(HTMLUtils.escapeHTML(e.getKey().getBuiltDescription())).append("</pre>\n\n");
			}
			return b.toString();
		}
	};

	public static Generator dataManagerTools = new Generator() {

		@Override
		public String generate(String... args) {
			try {
				boolean colorsDisabled = TermColors.ColorsDisabled();
				TermColors.DisableColors();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(baos);
				Manager.out = ps;
				Manager.help(ArrayUtils.EMPTY_STRING_ARRAY);
				String initial = HTMLUtils.escapeHTML(baos.toString("UTF-8")).replace("\n", "<br />").replace("\t", "&nbsp;&nbsp;&nbsp;");
				baos.reset();
				for(String option : Manager.OPTIONS) {
					Manager.out.println("\n===" + option + "===");
					Manager.help(new String[]{option});
				}
				if(!colorsDisabled) {
					TermColors.EnableColors();
				}
				return initial + HTMLUtils.escapeHTML(baos.toString("UTF-8"));
			} catch (UnsupportedEncodingException ex) {
				throw new Error(ex);
			}
		}
	};

	/**
	 * This is for msa code, use CODE for pure mscript code
	 */
	public static final Generator ALIAS = new Generator() {
		@Override
		public String generate(String... args) throws GenerateException {
			String code = StringUtils.Join(args, "|");
			if(code.endsWith("\n")) {
				code = code.replaceAll("\n$", "");
			}
			String out;
			try {
				out = SimpleSyntaxHighlighter.Highlight(code, false);
			} catch (ConfigCompileException ex) {
				throw new GenerateException(ex.getMessage() + "\nFor code: " + code, ex);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
			return out;
		}

	};

	/**
	 * This is for pure mscript code, use ALIAS for msa code.
	 */
	public static final Generator CODE = new Generator() {

		@Override
		public String generate(String... args) throws GenerateException {
			String code = StringUtils.Join(args, "|");
			code = code.replace("\r\n", "\n");
			if(code.endsWith("\n")) {
				code = code.replaceAll("\n$", "");
			}
			String out;
			try {
				out = SimpleSyntaxHighlighter.Highlight(code, true);
			} catch (ConfigCompileException ex) {
				throw new GenerateException(ex.getMessage() + "\nFor code: " + code, ex);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
			return out;
		}

	};

	public static String escapeWiki(String code) {
		code = HTMLUtils.escapeHTML(code);
		code = code.replaceAll("\\[", "&lsqb;");
		code = code.replaceAll("\\]", "&rsqb;");
		code = code.replaceAll("\\(", "&lpar;");
		code = code.replaceAll("\\)", "&rpar;");
		code = code.replaceAll("\\{", "&lcub;");
		code = code.replaceAll("\\}", "&rcub;");
		code = code.replaceAll("\\*", "&ast;");
		code = code.replaceAll("\\|", "&verbar;");
		code = code.replaceAll("=", "&equals;");
		code = code.replaceAll("#", "&num;");

		return code;
	}

	/**
	 * Escapes all &lt;nowiki&gt; blocks' contents with html entities.
	 */
	public static final Generator NOWIKI = new Generator() {
		@Override
		public String generate(String... args) throws GenerateException {
			String code = StringUtils.Join(args, "|");
			// We have to replace all html entities, but there's more that we have to do as well.
			return escapeWiki(code);
		}

	};

	/**
	 * Creates a &lt;pre&gt; block, escaping all special characters within it.
	 */
	public static final Generator PRE = new Generator() {
		@Override
		public String generate(String... args) {
			String code = StringUtils.Join(args, "|");
			String out = escapeWiki(code);
			if(out.startsWith("\n")) {
				out = out.substring(1);
			}
			if(out.endsWith("\n")) {
				out = out.substring(0, out.length() - 1);
			}
			return "<pre class=\"pre\">" + out + "</pre>";
		}

	};

	/**
	 * Creates a syntax highlighting block. The first argument is the type, and the second argument is the code itself.
	 */
	public static final Generator SYNTAX = new Generator() {
		@Override
		public String generate(String... args) {
			String code = StringUtils.Join(ArrayUtils.slice(args, 1, args.length - 1), "|");
			String out = code;
			out = escapeWiki(out);
			out = out.replaceAll("<", "&lt;");
			out = out.replaceAll(">", "&gt;");
			if(out.startsWith("\n")) {
				out = out.substring(1);
			}
			return "<pre><code class=\"" + args[0] + "\">" + out + "</code></pre>";
		}

	};

	/**
	 * Creates a special "warning" section, to draw attention to that message.
	 */
	public static final Generator NOTE = new Generator() {
		@Override
		public String generate(String... args) {
			String note = StringUtils.Join(args, "|");
			note = note.replaceAll("\n", " ");
			return "<div class=\"TakeNote\"><strong>Note:</strong> " + note + "</div>";
		}

	};

	public static final Generator MYSQL_CREATE_TABLE_QUERY = new Generator() {

		@Override
		public String generate(String... args) {
			MySQLDataSource ds = ReflectionUtils.newInstance(MySQLDataSource.class);
			return ds.getTableCreationQuery("tableName");
		}

	};

	public static final Generator SQLITE_CREATE_TABLE_QUERY = new Generator() {

		@Override
		public String generate(String... args) {
			SQLiteDataSource ds = ReflectionUtils.newInstance(SQLiteDataSource.class);
			return ds.getTableCreationQuery();
		}

	};

	/**
	 * Returns the value of a constant defined in a java class. Given the following java class:
	 *
	 * <code>
	 * package foo.bar;
	 *
	 * class Baz {
	 *	 public static int BING = 42;
	 * }
	 * </code>
	 *
	 * then the following would return 42: %%CONST|foo.bar.Baz.BING%%
	 */
	public static final Generator CONST = new Generator() {

		@Override
		public String generate(String... args) {
			String value = args[0];
			String[] v = value.split("\\.");
			StringBuilder b = new StringBuilder();
			for(int i = 0; i < v.length - 1; i++) {
				if(i != 0) {
					b.append(".");
				}
				b.append(v[i]);
			}
			String clazz = b.toString();
			String constant = v[v.length - 1];
			Class c;
			try {
				c = Class.forName(clazz);
			} catch (ClassNotFoundException ex) {
				throw new RuntimeException(ex);
			}
			Field f;
			try {
				f = c.getField(constant);
			} catch (NoSuchFieldException | SecurityException ex) {
				throw new RuntimeException(ex);
			}
			try {
				return f.get(null).toString();
			} catch (IllegalArgumentException | IllegalAccessException ex) {
				throw new RuntimeException(ex);
			}
		}
	};

	/**
	 * Returns the current date. This accepts a template in the same structure as simple_date()
	 */
	public static final Generator DATE = new Generator() {

		@Override
		public String generate(String... args) {
			String template = args[0];
			return new Scheduling.simple_date().exec(Target.UNKNOWN, null, new CString(template, Target.UNKNOWN)).val();
		}
	};

	/**
	 * Returns a wikified version to link to the given doc page. Optionally, you may include the text for the link,
	 * otherwise the link itself is used as the text. arg 0 is the documentation page, arg 1 is the text
	 */
	public static final Generator DOCLINK = new Generator() {

		@Override
		public String generate(String... args) {
			String page = args[0];
			String text = null;
			if(args.length >= 2) {
				text = args[1];
			}
			return "[[" + page + (text != null ? "|" + text : "") + "]]";
		}
	};

	/**
	 * Equivalent to %%NOTE%%
	 */
	public static final Generator TAKENOTE = new Generator() {
		@Override
		public String generate(String... args) throws GenerateException {
			return "{{TakeNote|text=" + StringUtils.Join(args, "|") + "}}";
		}
	};

	/**
	 * Answers the question: WHAT YEAR IS IT?
	 */
	public static final Generator CURRENTYEAR = new Generator() {
		@Override
		public String generate(String... args) throws GenerateException {
			return new Scheduling.simple_date().exec(Target.UNKNOWN, null, new CString("yyyy", Target.UNKNOWN)).val();
		}
	};

	public static final Generator CURRENT_VERSION = (String... args) -> MSVersion.LATEST.toString();

	/**
	 * Returns the standard {{unimplemented}} template
	 */
	public static final Generator UNIMPLEMENTED = (args) -> {
		return "{{Warning|text=THESE FEATURES ARE NOT IMPLEMENTED YET. This page only serves as a preview of how the"
				+ " shown features will work, and as a guide for how the implementation will occur}}";
	};

	/**
	 * Returns a Q&A style table arg 0 is the question, arg 1 is the answer
	 */
	public static final Generator QA = (args) -> {
		return "{| width=\"100%\" cellspacing=\"1\" cellpadding=\"1\" border=\"1\" class=\"wikitable\"\n"
				+ "|-\n"
				+ "! scope=\"col\" width=\"3%\" |\n"
				+ "! scope=\"col\" |\n"
				+ "|-\n"
				+ "| '''Q:'''\n"
				+ "| '''" + args[0].replaceAll("\n", " ") + "'''\n"
				+ "|- \n"
				+ "| '''A:''' \n"
				+ "| " + args[1].replaceAll("\n", " ") + "\n"
				+ "|}";
	};

	public static final Generator SUPPRESS_WARNINGS_LIST = (args) -> {
		StringBuilder b = new StringBuilder();
		for(FileOptions.SuppressWarning s : FileOptions.SuppressWarning.values()) {
			b.append("* ")
					.append(s.getName())
					.append(" - ")
					.append(s.docs())
					.append(" (added ")
					.append(s.since())
					.append(")\n");
		}
		return b.toString();
	};

	public static final Generator COMPILER_OPTIONS_LIST = (args) -> {
		StringBuilder b = new StringBuilder();
		for(FileOptions.CompilerOption s : FileOptions.CompilerOption.values()) {
			b.append("* ")
					.append(s.getName())
					.append(" - ")
					.append(s.docs())
					.append(" (added ")
					.append(s.since())
					.append(")\n");
		}
		return b.toString();
	};
}
