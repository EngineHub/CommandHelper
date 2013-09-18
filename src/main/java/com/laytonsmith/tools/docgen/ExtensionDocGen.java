package com.laytonsmith.tools.docgen;

import com.laytonsmith.PureUtilities.ClassDiscovery;
import com.laytonsmith.PureUtilities.DynamicClassLoader;
import com.laytonsmith.PureUtilities.FileUtility;
import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.functions.Function;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author cgallarno
 */
public class ExtensionDocGen {
	
	private static final String nl = StringUtils.nl();
	private static final Pattern WIKI_LINK = Pattern.compile("\\[([a-zA-Z]+://[^ ]+) ([^\\]]+)]");

	public static void generate(File inputExtension, OutputStream outputStream) throws InstantiationException, IllegalAccessException, MalformedURLException, IOException {
		ClassDiscovery customDiscovery = new ClassDiscovery();
		URL url = new URL("jar:" + inputExtension.toURI().toURL() + "!/");
		customDiscovery.addDiscoveryLocation(url);
		StringBuilder fdocs = new StringBuilder();
		DynamicClassLoader classloader = new DynamicClassLoader();
		classloader.addJar(url);
		Set<Class<Function>> functions = customDiscovery.loadClassesWithAnnotationThatExtend(api.class, Function.class, classloader, true);
		for(Class<Function> cmf : functions) {
			Function f = cmf.newInstance();
			DocGen.DocInfo di = new DocGen.DocInfo(f.docs());
			String d = "## " + di.ret + " " + f.getName() + "(" + di.originalArgs + "):" + nl
					+ convertWiki(di.topDesc != null?di.topDesc:di.desc) + nl
					+ convertWiki(di.extendedDesc != null?nl + di.extendedDesc + nl:"");
			fdocs.append(d);
		}
		outputStream.write(fdocs.toString().getBytes("UTF-8"));
	}
	
	/**
	 * Converts selected wiki markup into markdown.
	 * @param input
	 * @return 
	 */
	private static String convertWiki(String input){
		String output = input;
		Matcher m = WIKI_LINK.matcher(input);
		if(m.find()){
			output = m.replaceAll("[$2]($1)");
		}
		return output;
	}
}
