package com.laytonsmith.tools.docgen;

import com.laytonsmith.PureUtilities.ArgumentParser;
import com.laytonsmith.PureUtilities.ClassDiscovery;
import com.laytonsmith.PureUtilities.MSP.Burst;
import com.laytonsmith.PureUtilities.ReflectionUtils;
import com.laytonsmith.PureUtilities.StreamUtils;
import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.annotations.datasource;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.Main;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;
import com.laytonsmith.persistance.DataSource;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author lsmith
 */
public class DocGenTemplates {
	public static interface Generator{
		public String generate(String ... args);
	}
	
	public static void main(String[] args){
		Implementation.setServerType(Implementation.Type.SHELL);
		System.out.println(Generate("CommandLineTools"));
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
		ClassDiscovery.InstallDiscoveryLocation(ClassDiscovery.GetClassPackageHierachy(DocGenTemplates.class));
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
				//Oh well, skip it.
			}
		}
		if(!appended){
			templateBuilder.append(template.substring(lastMatch));
		}
		return templateBuilder.toString();
	}
	
	public static Generator data_source_modifiers = new Generator() {

		public String generate(String ... args) {
			StringBuilder b = new StringBuilder();
			for(DataSource.DataSourceModifier mod : DataSource.DataSourceModifier.values()){
				b.append("|-\n| ").append(mod.getName().toLowerCase()).append(" || ").append(mod.docs()).append("\n");
				
			}
			return b.toString();
		}
	};
	
	public static Generator persistance_connections = new Generator(){

		public String generate(String ... args) {
			StringBuilder b = new StringBuilder();
			Class [] classes = ClassDiscovery.GetClassesWithAnnotation(datasource.class);
			Pattern p = Pattern.compile("\\s*(.*?)\\s*\\{\\s*(.*?)\\s*\\}\\s*(.*?)\\s*$");
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
							throw new Error("Invalid documentation for " + c.getSimpleName());
						}
						b.append("|-\n| ").append(name).append(" || ").append(description)
							.append(" || ").append(example).append(" || ").append(ds.since().getVersionString()).append("\n");
					} catch(Exception e){
						throw new Error(e);
					}
				} else {
					throw new Error("@datasource implementations must implement DataSource.");
				}
			}
			return b.toString();
		}
		
	};
	
	public static Generator optimization_explanations = new Generator(){

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

		public String generate(String ... args) {
			return StringUtils.Join(Burst.BurstType.values(), ", ", ", or ", " or ");
		}
	};
	
	public static Generator BURST_TYPE_DOCS = new Generator() {

		public String generate(String ... args) {
			//TODO
			return "TODO";
		}
	};
	
	public static Generator EXCEPTION_TYPES = new Generator() {

		public String generate(String ... args) {
			StringBuilder b = new StringBuilder();
			for(Documentation d : ExceptionType.values()){
				b.append("===").append(d.getName()).append("===\n");
				b.append(d.docs());
				b.append("\n\nSince: ").append(d.since().getVersionString()).append("\n\n");
			}
			return b.toString();
		}
	};
	
	private static final String githubBaseURL = "https://github.com/sk89q/commandhelper/tree/master/src/main/java";
	
	/**
	 * Returns the fully qualified (and github linked) class name, given the package
	 * regex and complete class name. For instance: %%GET_CLASS|.*|DocGenTemplates%% would
	 * (likely) return [http://url.to.github.com/path/to/file/ com.laytonsmith.tools.docgen.DocGenTemplates]
	 */
	public static Generator GET_CLASS = new Generator() {

		public String generate(String... args) {
			Class c = ClassDiscovery.forFuzzyName(args[0], args[1]);
			return "[" + githubBaseURL + "/" + c.getName().replace(".", "/") + ".java " + c.getName() + "]";
		}
	};
	
	/**
	 * Returns the (github linked) simple class name, given the package
	 * regex and complete class name. For instance: %%GET_CLASS|.*|DocGenTemplates%% would
	 * (likely) return [http://url.to.github.com/path/to/file/ DocGenTemplates]
	 */
	public static Generator GET_SIMPLE_CLASS = new Generator() {

		public String generate(String... args) {
			Class c = ClassDiscovery.forFuzzyName(args[0], args[1]);
			return "[" + githubBaseURL + "/" + c.getName().replace(".", "/") + ".java " + c.getSimpleName() + "]";
		}
	};
	
	/**
	 * Returns a github link to the source file this function is in. Note that it is not possible
	 * to get more specific line information (at least generically).
	 * %%GET_FUNCTION_FILE|msg%%, for instance.
	 */
	public static Generator GET_FUNCTION_FILE = new Generator() {

		public String generate(String... args) {
			try {
				FunctionBase b = FunctionList.getFunction(args[0]);
				Class c = b.getClass();
				while(c.getEnclosingClass() != null){
					c = c.getEnclosingClass();
				}
				return "[" + githubBaseURL + "/" + c.getName().replace(".", "/") + ".java " + b.getName() + "]";
			} catch (ConfigCompileException ex) {
				return "Unknown function: " + args[0];
			}
			
		}
	};
	
	public static Generator cmdlinehelp = new Generator() {

		public String generate(String... args) {
			StringBuilder b = new StringBuilder();
			b.append("<pre style=\"white-space: pre-wrap;\">\n").append(Main.ARGUMENT_SUITE.getBuiltDescription()).append("\n</pre>\n");
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
}
