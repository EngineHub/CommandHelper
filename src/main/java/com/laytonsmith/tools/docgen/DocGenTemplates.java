package com.laytonsmith.tools.docgen;

import com.laytonsmith.PureUtilities.ArgumentParser;
import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.MSP.Burst;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.annotations.datasource;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.Main;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.SimpleDocumentation;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;
import com.laytonsmith.core.functions.Scheduling;
import com.laytonsmith.persistence.DataSource;
import com.laytonsmith.persistence.MySQLDataSource;
import com.laytonsmith.persistence.SQLiteDataSource;
import com.laytonsmith.tools.Manager;
import com.laytonsmith.tools.SimpleSyntaxHighlighter;
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
	public static interface Generator{
		public String generate(String ... args);
	}

	public static void main(String[] args){
		Implementation.setServerType(Implementation.Type.SHELL);
		StreamUtils.GetSystemOut().println(Generate("Exceptions"));
	}

	public static String Generate(String forPage){
		return Generate(forPage, new HashMap<String, String>());
	}
	public static String Generate(String forPage, Map<String, String> customTemplates){
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
		while(m.find()){
			if(!appended){
				templateBuilder.append(template.substring(lastMatch, m.start()));
				appended = true;
			}
			String name = m.group(1);
			for(String templateName : customTemplates.keySet()){
				if(templateName.equals(name)){
					templateBuilder.append(customTemplates.get(name));
					lastMatch = m.end();
					appended = false;
					//template = template.replaceAll("%%" + Pattern.quote(name) + ".*?%%", customTemplates.get(name));
				}
			}
			try{
				Field f = DocGenTemplates.class.getDeclaredField(name);
				f.setAccessible(true);
				if(Generator.class.isAssignableFrom(f.getType()) && Modifier.isStatic(f.getModifiers())){
					String [] tmplArgs = new String[0];
					if(m.group(2) != null && !m.group(2).equals("")){
						//We have arguments
						//remove the initial |, then split
						tmplArgs = m.group(2).substring(1).split("\\|");
					}
					String templateValue = ((Generator)f.get(null)).generate(tmplArgs);
					templateBuilder.append(templateValue);
					lastMatch = m.end();
					appended = false;
					//template = template.replaceAll("%%" + Pattern.quote(name) + "%%", templateValue);
				} else {
					throw new Error(DocGenTemplates.class.getSimpleName() + "." + f.getName()
						+ " is not an instance of " + Generator.class.getSimpleName()
						+ ", or is not static. Please correct this error to use it as a template.");
				}
			} catch(Exception e){
				System.out.println(e);
				//Oh well, skip it.
			}
		}
		if(!appended){
			templateBuilder.append(template.substring(lastMatch));
		}
		return templateBuilder.toString();
	}

	/**
	 * Returns all the generators defined in this class.
	 * @return
	 */
	public static Map<String, Generator> GetGenerators(){
		Map<String, Generator> generators = new HashMap<>();
		for(Field f : DocGenTemplates.class.getDeclaredFields()){
			if(Generator.class.isAssignableFrom(f.getType())){
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
	 * @param template The template string on which to perform the replacements
	 * @param generators The list of String-Generator entries, where the String is the template
	 * name, and the Generator is the replacement to use.
	 * @return
	 */
	public static String DoTemplateReplacement(String template, Map<String, Generator> generators){
		try {
			if(Implementation.GetServerType() != Implementation.Type.BUKKIT){
				Prefs.init(null);
			}
		} catch (IOException ex) {
			Logger.getLogger(DocGenTemplates.class.getName()).log(Level.SEVERE, null, ex);
		}
		ClassDiscovery.getDefaultInstance().addDiscoveryLocation(ClassDiscovery.GetClassContainer(DocGenTemplates.class));

		//Find all the %%templates%% in the template
		Matcher m = Pattern.compile("%%([^\\|%]+)([^%]*?)%%").matcher(template);
		StringBuilder templateBuilder = new StringBuilder();
		int lastMatch = 0;
		boolean appended = false;
		while(m.find()){
			if(!appended){
				templateBuilder.append(template.substring(lastMatch, m.start()));
				appended = true;
			}
			String name = m.group(1);
			try{
				if(generators.containsKey(name)){
					String [] tmplArgs = new String[0];
					if(m.group(2) != null && !m.group(2).equals("")){
						//We have arguments
						//remove the initial |, then split
						tmplArgs = m.group(2).substring(1).split("\\|");
					}
					String templateValue = generators.get(name).generate(tmplArgs);
					templateBuilder.append(templateValue);
					lastMatch = m.end();
					appended = false;
					//template = template.replaceAll("%%" + Pattern.quote(name) + "%%", templateValue);
				}
			} catch(Exception e){
				//Oh well, skip it.
				e.printStackTrace();
			}
		}
		if(!appended){
			templateBuilder.append(template.substring(lastMatch));
		}
		return templateBuilder.toString();
	}

	public static Generator data_source_modifiers = new Generator() {

		@Override
		public String generate(String ... args) {
			StringBuilder b = new StringBuilder();
			for(DataSource.DataSourceModifier mod : DataSource.DataSourceModifier.values()){
				b.append("|-\n| ").append(mod.getName().toLowerCase()).append(" || ").append(mod.docs()).append("\n");

			}
			return b.toString();
		}
	};

	public static Generator persistence_connections = new Generator(){

		@Override
		public String generate(String ... args) {
			Set<Class> classes = ClassDiscovery.getDefaultInstance().loadClassesWithAnnotation(datasource.class);
			Pattern p = Pattern.compile("(?s)\\s*(.*?)\\s*\\{\\s*(.*?)\\s*\\}\\s*(.*)\\s*$");
			SortedSet<String> set = new TreeSet<String>();
			for(Class c : classes){
				if(DataSource.class.isAssignableFrom(c)){
					try{
						Constructor constructor;
						try{
							constructor = c.getDeclaredConstructor();
							constructor.setAccessible(true);
						} catch(NoSuchMethodException e){
							throw new RuntimeException("No no-argument constructor was found for " + c.getName() + ". A no-arg constructor must be provided, even if it is"
									+ " private, so that the documentation functions can be accessed.", e);
						}
						DataSource ds = (DataSource)constructor.newInstance();
						String docs = ds.docs();
						Matcher m = p.matcher(docs);
						String name = null;
						String example = null;
						String description = null;
						if(m.find()){
							name = m.group(1);
							example = m.group(2);
							description = m.group(3);
						}
						if(name == null || example == null || description == null){
							throw new Error("Invalid documentation for " + c.getSimpleName()
								+ (name==null?" name was null;":"")
								+ (example==null?" example was null;":"")
								+ (description==null?" description was null;":""));
						}
						StringBuilder b = new StringBuilder();
						b.append("|-\n| ").append(name).append(" || ").append(description)
							.append(" || ").append(example).append(" || ").append(ds.since().toString()).append("\n");
						set.add(b.toString());
					} catch(Exception e){
						throw new Error(e);
					}
				} else {
					throw new Error("@datasource implementations must implement DataSource.");
				}
			}
			return StringUtils.Join(set, "");
		}

	};

	public static Generator optimization_explanations = new Generator(){

		@Override
		public String generate(String ... args) {
			StringBuilder b = new StringBuilder();
			for(Optimizable.OptimizationOption option : Optimizable.OptimizationOption.values()){
				b.append("=== ").append(option.getName()).append(" ===\n");
				b.append(option.docs()).append("\n\n");
				b.append("Since ").append(option.since()).append("\n\n");
			}
			return b.toString();
		}

	};

	public static Generator BURST_VALUE_TYPES = new Generator() {

		@Override
		public String generate(String ... args) {
			return StringUtils.Join(Burst.BurstType.values(), ", ", ", or ", " or ");
		}
	};

	public static Generator BURST_TYPE_DOCS = new Generator() {

		@Override
		public String generate(String ... args) {
			//TODO
			return "TODO";
		}
	};

	public static Generator EXCEPTION_TYPES = new Generator() {

		@Override
		public String generate(String ... args) {
			StringBuilder b = new StringBuilder();
			SortedSet<Class<CREThrowable>> set = new TreeSet<>(new Comparator<Class<CREThrowable>>(){
				@Override
				public int compare(Class<CREThrowable> o1, Class<CREThrowable> o2) {
					return o1.getCanonicalName().compareTo(o2.getCanonicalName());
				}
			});
			set.addAll(ClassDiscovery.getDefaultInstance().loadClassesWithAnnotationThatExtend(typeof.class, CREThrowable.class));
			for(Class<CREThrowable> c : set){
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

	private static final String githubBaseURL = "https://github.com/EngineHub/commandhelper/tree/master/src/main/java";

	/**
	 * Returns the fully qualified (and github linked) class name, given the package
	 * regex and complete class name. For instance: %%GET_CLASS|.*|DocGenTemplates%% would
	 * (likely) return [http://url.to.github.com/path/to/file/ com.laytonsmith.tools.docgen.DocGenTemplates]
	 */
	public static Generator GET_CLASS = new Generator() {

		@Override
		public String generate(String... args) {
			Class c = ClassDiscovery.getDefaultInstance().forFuzzyName(args[0], args[1]).loadClass();
			return "[" + githubBaseURL + "/" + c.getName().replace('.', '/') + ".java " + c.getName() + "]";
		}
	};

	/**
	 * Returns the base github url for sources.
	 */
	public static Generator GITHUB_URL = new Generator() {

		@Override
		public String generate(String... args) {
			return githubBaseURL;
		}
	};

	/**
	 * Returns the (github linked) simple class name, given the package
	 * regex and complete class name. For instance: %%GET_CLASS|.*|DocGenTemplates%% would
	 * (likely) return [http://url.to.github.com/path/to/file/ DocGenTemplates]
	 */
	public static Generator GET_SIMPLE_CLASS = new Generator() {

		@Override
		public String generate(String... args) {
			Class c = ClassDiscovery.getDefaultInstance().forFuzzyName(args[0], args[1]).loadClass();
			return "[" + githubBaseURL + "/" + c.getName().replace('.', '/') + ".java " + c.getSimpleName() + "]";
		}
	};

	/**
	 * Returns a github link to the source file this function is in. Note that it is not possible
	 * to get more specific line information (at least generically).
	 * %%GET_FUNCTION_FILE|msg%%, for instance.
	 */
	public static Generator GET_FUNCTION_FILE = new Generator() {

		@Override
		public String generate(String... args) {
			try {
				FunctionBase b = FunctionList.getFunction(args[0], Target.UNKNOWN);
				Class c = b.getClass();
				while(c.getEnclosingClass() != null){
					c = c.getEnclosingClass();
				}
				return "[" + githubBaseURL + "/" + c.getName().replace('.', '/') + ".java " + b.getName() + "]";
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
			b.append("<pre style=\"white-space: pre-wrap;\">\n").append(Main.ARGUMENT_SUITE.getBuiltDescription()).append("\n</pre>\n");
			if(!colorsDisabled){
				TermColors.EnableColors();
			}
			for(Field f : Main.class.getDeclaredFields()){
				if(f.getType() == ArgumentParser.class){
					b.append("==== ").append(StringUtils.replaceLast(f.getName(), "(?i)mode", "")).append(" ====\n<pre style=\"white-space: pre-wrap;\">");
					ArgumentParser parser = (ArgumentParser)ReflectionUtils.get(Main.class, f.getName());
					b.append(parser.getBuiltDescription()).append("</pre>\n\n");
				}
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
				Manager.help(new String[0]);
				String initial = baos.toString("UTF-8").replace("\n", "<br />").replace("\t", "&nbsp;&nbsp;&nbsp;");
				baos.reset();
				for(String option : Manager.options){
					Manager.out.println("\n===" + option + "===");
					Manager.help(new String[]{option});
				}
				if(!colorsDisabled){
					TermColors.EnableColors();
				}
				return initial + baos.toString("UTF-8");
			} catch (UnsupportedEncodingException ex) {
				throw new Error(ex);
			}
		}
	};


	public static Generator CODE = new Generator(){

		@Override
		public String generate(String... args) {
			String code = StringUtils.Join(args, "|");
			String out = SimpleSyntaxHighlighter.Highlight(code);
			return out;
		}

	};

	public static Generator MySQL_CREATE_TABLE_QUERY = new Generator(){

		@Override
		public String generate(String... args) {
			MySQLDataSource ds = ReflectionUtils.newInstance(MySQLDataSource.class);
			return ds.getTableCreationQuery("tableName");
		}

	};

	public static Generator SQLite_CREATE_TABLE_QUERY = new Generator(){

		@Override
		public String generate(String... args) {
			SQLiteDataSource ds = ReflectionUtils.newInstance(SQLiteDataSource.class);
			return ds.getTableCreationQuery();
		}

	};

	public static Generator CONST = new Generator() {

		@Override
		public String generate(String... args) {
			String value = args[0];
			String[] v = value.split("\\.");
			StringBuilder b = new StringBuilder();
			for(int i = 0; i < v.length - 1; i++){
				if(i != 0){
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

	public static Generator DATE = new Generator() {

		@Override
		public String generate(String... args) {
			String template = args[0];
			return new Scheduling.simple_date().exec(Target.UNKNOWN, null, new CString(template, Target.UNKNOWN)).val();
		}
	};

	public static Generator DOCLINK = new Generator() {

		@Override
		public String generate(String... args) {
			String page = args[0];
			String text = null;
			if(args.length >= 2){
				text = args[1];
			}
			return "[[CommandHelper/Staged/" + page + (text != null ? "|" + text : "") + "]]";
		}
	};
}
