package com.laytonsmith.tools.docgen;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.DynamicClassLoader;
import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.events.Event;
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
		if(!functions.isEmpty()){
			fdocs.append("# Functions").append(nl);
		}
		for(Class<Function> cmf : functions) {
			Function f = cmf.newInstance();
			if(!f.appearInDocumentation()){
				continue;
			}
			DocGen.DocInfo di = new DocGen.DocInfo(f.docs());
			String d = "## " + markdownEscape(di.ret) + " " + markdownEscape(f.getName()) + "(" + markdownEscape(di.originalArgs) + "):" + nl
					+ convertWiki(di.topDesc != null?di.topDesc:di.desc) + nl
					+ convertWiki(di.extendedDesc != null?nl + di.extendedDesc + nl:"");
			fdocs.append(d);
		}
		Set<Class<Event>> events = customDiscovery.loadClassesWithAnnotationThatExtend(api.class, Event.class, classloader, true);
		if(!events.isEmpty()){
			fdocs.append("# Events").append(nl);
		}
		for(Class<Event> cme : events){
			Event e = cme.newInstance();
			Pattern p = Pattern.compile("\\{(.*?)\\} *?(.*?) *?\\{(.*?)\\} *?\\{(.*?)\\}");
            Matcher m = p.matcher(e.docs());
            if (m.find()) {
                String name = e.getName();
                String description = m.group(2).trim();
                String prefilter = DocGen.PrefilterData.Get(m.group(1).split("\\|"), DocGen.MarkupType.MARKDOWN);
                String eventData = DocGen.EventData.Get(m.group(3).split("\\|"), DocGen.MarkupType.MARKDOWN);
                String mutability = DocGen.MutabilityData.Get(m.group(4).split("\\|"), DocGen.MarkupType.MARKDOWN);
                //String manualTrigger = ManualTriggerData.Get(m.group(5).split("\\|"), DocGen.MarkupType.MARKDOWN);
                //String since = e.since().toString();
				fdocs.append("## ").append(markdownEscape(name)).append(nl);
				fdocs.append(description).append(nl);
				fdocs.append("### Prefilters").append(nl).append(prefilter).append(nl);
				fdocs.append("### Event Data").append(nl).append(eventData).append(nl);
				fdocs.append("### Mutable Fields").append(nl).append(mutability).append(nl);
			}
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
	
	private static String markdownEscape(String input){
		return input.replace("*", "\\*")
				.replace("_", "\\_");
	}
}
