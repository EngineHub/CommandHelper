
package com.laytonsmith.tools.docgen;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.events.Event;
import com.laytonsmith.core.functions.Function;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.simple.JSONValue;

/**
 *
 */
public class DocGenExportTool {
	private final ClassDiscovery classDiscovery;
	private final OutputStream out;
	
	/**
	 * Creates a new instance of the DocGenExportTool
	 * @param classDiscovery
	 * @param extensionDir
	 * @param out 
	 */
	public DocGenExportTool(ClassDiscovery classDiscovery, OutputStream out){
		this.classDiscovery = classDiscovery;
		this.out = out;
	}
	
	/**
	 * Triggers the export tool
	 */
	public void export(){
		Set<Class<Function>> functions = classDiscovery
				.loadClassesWithAnnotationThatExtend(api.class, Function.class, this.getClass().getClassLoader(), false);
		Set<Class<Event>> events = classDiscovery
				.loadClassesWithAnnotationThatExtend(api.class, Event.class, this.getClass().getClassLoader(), false);
		Map<String, Object> topLevel = new HashMap<String, Object>();
		List<Map<String, Object>> functionList = new ArrayList<Map<String, Object>>();
		topLevel.put("functions", functionList);
		List<Map<String, Object>> eventList = new ArrayList<Map<String, Object>>();
		topLevel.put("events", eventList);
		for(Class<Function> functionC : functions){
			Map<String, Object> function = new HashMap<String, Object>();
			Function f;
			try {
				f = ReflectionUtils.newInstance(functionC);
			} catch(NoClassDefFoundError ex){
				System.err.println("While attempting to load: " + functionC.getName() + ": " + ex.getMessage());
				continue;
			}
			DocGen.DocInfo di = new DocGen.DocInfo(f.docs());
			function.put("name", f.getName());
			function.put("ret", di.ret);
			function.put("args", di.originalArgs);
			function.put("desc", di.desc);
			functionList.add(function);
		}
		
		Pattern eventPattern = Pattern.compile("\\{(.*?)\\} *?(.*?) *?\\{(.*?)\\} *?\\{(.*?)\\}");
		DocGen.MarkupType type = DocGen.MarkupType.TEXT;
		for(Class<Event> eventC : events){
			Map<String, Object> event = new HashMap<String, Object>();
			Event e = ReflectionUtils.newInstance(eventC);
			Matcher m = eventPattern.matcher(e.docs());
			if(m.find()){
				String name = e.getName();
				String description = m.group(2).trim();
				String prefilter = DocGen.PrefilterData.Get(m.group(1).split("\\|"), type);
				String eventData = DocGen.EventData.Get(m.group(3).split("\\|"), type);
				String mutability = DocGen.MutabilityData.Get(m.group(4).split("\\|"), type);
				event.put("name", name);
				event.put("desc", description);
				event.put("prefilter", prefilter);
				event.put("eventData", eventData);
				event.put("mutability", mutability);
				eventList.add(event);
			}
		}
		
		String output = JSONValue.toJSONString(topLevel) + StringUtils.nl();
		try {
			out.write(output.getBytes("UTF-8"));
			out.flush();
		} catch (IOException ex) {
			Logger.getLogger(DocGenExportTool.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
