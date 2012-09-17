package com.laytonsmith.tools.docgen;

import com.laytonsmith.PureUtilities.ClassDiscovery;
import com.laytonsmith.PureUtilities.StreamUtils;
import com.laytonsmith.annotations.datasource;
import com.laytonsmith.persistance.DataSource;
import com.laytonsmith.persistance.io.ConnectionMixinFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author lsmith
 */
public class DocGenTemplates {
	public static interface Generator{
		public String generate();
	}
	
	public static String Generate(String forPage){
		return Generate(forPage, new HashMap<String, String>());
	}
	public static String Generate(String forPage, Map<String, String> customTemplates){
		//Grab the template from the resources
		String template = StreamUtils.GetString(DocGenTemplates.class.getResourceAsStream("/docs/" + forPage));
		//Find all the %%templates%% in the template
		Matcher m = Pattern.compile("%%(.*?)%%").matcher(template);
		while(m.find()){
			String name = m.group(1);
			for(String templateName : customTemplates.keySet()){
				if(templateName.equals(name)){
					template = template.replaceAll("%%" + Pattern.quote(name) + "%%", customTemplates.get(name));
				}
			}
			try{
				Field f = DocGenTemplates.class.getDeclaredField(name);
				f.setAccessible(true);
				if(Generator.class.isAssignableFrom(f.getType()) && Modifier.isStatic(f.getModifiers())){
					String templateValue = ((Generator)f.get(null)).generate();
					template = template.replaceAll("%%" + Pattern.quote(name) + "%%", templateValue);
				} else {
					throw new Error(DocGenTemplates.class.getSimpleName() + "." + f.getName() 
						+ " is not an instance of " + Generator.class.getSimpleName() 
						+ ", or is not static. Please correct this error to use it as a template.");
				}
			} catch(Exception e){
				//Oh well, skip it.
			}
		}
		return template;
	}
	
	public static Generator data_source_modifiers = new Generator() {

		public String generate() {
			StringBuilder b = new StringBuilder();
			for(DataSource.DataSourceModifier mod : DataSource.DataSourceModifier.values()){
				b.append("|-\n| ").append(mod.getName().toLowerCase()).append(" || ").append(mod.docs()).append("\n");
				
			}
			return b.toString();
		}
	};
	
	public static Generator persistance_connections = new Generator(){

		public String generate() {
			StringBuilder b = new StringBuilder();
			Class [] classes = ClassDiscovery.GetClassesWithAnnotation(datasource.class);
			Pattern p = Pattern.compile("\\s*(.*?)\\s*\\{\\s*(.*?)\\s*\\}\\s*(.*?)\\s*$");
			for(Class c : classes){
				if(DataSource.class.isAssignableFrom(c)){
					try{
						DataSource ds = (DataSource)c.getConstructor(URI.class, ConnectionMixinFactory.ConnectionMixinOptions.class)
							.newInstance(new URI(""), new ConnectionMixinFactory.ConnectionMixinOptions());
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
}
