package com.laytonsmith.tools.docgen;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscoveryCache;
import com.laytonsmith.PureUtilities.ClassLoading.DynamicClassLoader;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.annotations.api;
import com.laytonsmith.commandhelper.CommandHelperFileLocations;
import com.laytonsmith.core.events.Event;
import com.laytonsmith.core.functions.Function;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
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
		ClassDiscoveryCache cache = new ClassDiscoveryCache(CommandHelperFileLocations.getDefault().getCacheDirectory());
		customDiscovery.setClassDiscoveryCache(cache);
		URL url = new URL("jar:" + inputExtension.toURI().toURL() + "!/");
		customDiscovery.addDiscoveryLocation(url);
		customDiscovery.setDefaultClassLoader(ExtensionDocGen.class.getClassLoader());
		StringBuilder fdocs = new StringBuilder();
		DynamicClassLoader classloader = new DynamicClassLoader();
		classloader.addJar(url);
		//functions
		HashMap<Class<?>, ArrayList<Class<? extends Function>>> functionMap = new HashMap<>();
		for(Class<? extends Function> cf : customDiscovery.loadClassesWithAnnotationThatExtend(api.class, Function.class, classloader, true)) {
			Class enclosing = cf.getEnclosingClass();
			if(functionMap.containsKey(enclosing)) {
				functionMap.get(enclosing).add(cf);
			} else {
				functionMap.put(enclosing, new ArrayList<Class<? extends Function>>());
				functionMap.get(enclosing).add(cf);
			}
		}
		ArrayList<Entry<Class<?>, ArrayList<Class<? extends Function>>>> functionEntryList = new ArrayList<>(functionMap.entrySet());
		Collections.sort(functionEntryList, new Comparator<Entry<Class<?>, ArrayList<Class<? extends Function>>>>() {
			@Override
			public int compare(Entry<Class<?>, ArrayList<Class<? extends Function>>> o1,
					Entry<Class<?>, ArrayList<Class<? extends Function>>> o2) {
				return o1.getKey().getName().compareTo(o2.getKey().getName());
			}
		});
		for(Entry<Class<?>, ArrayList<Class<? extends Function>>> e : functionEntryList) {
			Collections.sort(e.getValue(), new Comparator<Class<? extends Function>>() {
				@Override
				public int compare(Class<? extends Function> o1, Class<? extends Function> o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
		}
		if(!functionEntryList.isEmpty()) {
			fdocs.append("# Functions").append(nl);
		}
		for(Entry<Class<?>, ArrayList<Class<? extends Function>>> entry : functionEntryList) {
			Class enclosingClass = entry.getKey();
			String[] split = enclosingClass.getName().split("\\.");
			fdocs.append("## ").append(split[split.length - 1]).append(nl);
			try {
				Method docsMethod = enclosingClass.getMethod("docs", (Class[]) null);
				Object o = enclosingClass.newInstance();
				fdocs.append((String) docsMethod.invoke(o, (Object[]) null)).append(nl).append(nl);
			} catch(NoSuchMethodException
					| SecurityException
					| InstantiationException
					| IllegalAccessException
					| IllegalArgumentException
					| InvocationTargetException exception) {
			}
			for(Class<? extends Function> cf : entry.getValue()) {
				Function f = cf.newInstance();
				if(f.appearInDocumentation()) {
					DocGen.DocInfo di = new DocGen.DocInfo(f.docs());
					String d = "### " + markdownEscape(di.ret) + " " + markdownEscape(f.getName()) + "(" + markdownEscape(di.originalArgs) + "):" + nl
							+ convertWiki(di.topDesc != null ? di.topDesc : di.desc) + nl
							+ convertWiki(di.extendedDesc != null ? nl + di.extendedDesc + nl : "");
					fdocs.append(d).append(nl);
				}
			}
		}
		//events
		HashMap<Class<?>, ArrayList<Class<? extends Event>>> eventMap = new HashMap<>();
		for(Class<? extends Event> ce : customDiscovery.loadClassesWithAnnotationThatExtend(api.class, Event.class, classloader, true)) {
			Class<?> enclosing = ce.getEnclosingClass();
			if(eventMap.containsKey(enclosing)) {
				eventMap.get(enclosing).add(ce);
			} else {
				eventMap.put(enclosing, new ArrayList<Class<? extends Event>>());
				eventMap.get(enclosing).add(ce);
			}
		}
		ArrayList<Entry<Class<?>, ArrayList<Class<? extends Event>>>> eventEntryList = new ArrayList<>(eventMap.entrySet());
		Collections.sort(eventEntryList, new Comparator<Entry<Class<?>, ArrayList<Class<? extends Event>>>>() {
			@Override
			public int compare(Entry<Class<?>, ArrayList<Class<? extends Event>>> o1,
					Entry<Class<?>, ArrayList<Class<? extends Event>>> o2) {
				return o1.getKey().getName().compareTo(o2.getKey().getName());
			}
		});
		for(Entry<Class<?>, ArrayList<Class<? extends Event>>> e : eventEntryList) {
			Collections.sort(e.getValue(), new Comparator<Class<? extends Event>>() {
				@Override
				public int compare(Class<? extends Event> o1, Class<? extends Event> o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
		}
		if(!eventEntryList.isEmpty()) {
			fdocs.append("# Events").append(nl);
		}
		for(Entry<Class<?>, ArrayList<Class<? extends Event>>> entry : eventEntryList) {
			Class enclosingClass = entry.getKey();
			String[] split = enclosingClass.getName().split("\\.");
			fdocs.append("## ").append(split[split.length - 1]).append(nl);
			try {
				Method docsMethod = enclosingClass.getMethod("docs", (Class[]) null);
				Object o = enclosingClass.newInstance();
				fdocs.append((String) docsMethod.invoke(o, (Object[]) null)).append(nl).append(nl);
			} catch(NoSuchMethodException
					| SecurityException
					| InstantiationException
					| IllegalAccessException
					| IllegalArgumentException
					| InvocationTargetException exception) {
			}
			for(Class<? extends Event> ce : entry.getValue()) {
				Event e = ce.newInstance();
				Pattern p = Pattern.compile("\\{(.*?)\\} *?(.*?) *?\\{(.*?)\\} *?\\{(.*?)\\}");
				Matcher m = p.matcher(e.docs());
				if(m.find()) {
					String name = e.getName();
					String description = m.group(2).trim();
					String prefilter = DocGen.PrefilterData.Get(m.group(1).split("\\|"), DocGen.MarkupType.MARKDOWN);
					String eventData = DocGen.EventData.Get(m.group(3).split("\\|"), DocGen.MarkupType.MARKDOWN);
					String mutability = DocGen.MutabilityData.Get(m.group(4).split("\\|"), DocGen.MarkupType.MARKDOWN);
					//String manualTrigger = ManualTriggerData.Get(m.group(5).split("\\|"), DocGen.MarkupType.MARKDOWN);
					//String since = e.since().toString();
					fdocs.append("### ").append(markdownEscape(name)).append(nl);
					fdocs.append(description).append(nl);
					fdocs.append("#### Prefilters").append(nl).append(prefilter).append(nl);
					fdocs.append("#### Event Data").append(nl).append(eventData).append(nl);
					fdocs.append("#### Mutable Fields").append(nl).append(mutability).append(nl);
				}
			}
		}
		outputStream.write(fdocs.toString().getBytes("UTF-8"));
	}

	/**
	 * Converts selected wiki markup into markdown.
	 *
	 * @param input
	 * @return
	 */
	private static String convertWiki(String input) {
		String output = input;
		Matcher m = WIKI_LINK.matcher(input);
		if(m.find()) {
			output = m.replaceAll("[$2]($1)");
		}
		return output;
	}

	private static String markdownEscape(String input) {
		return input.replace("*", "\\*")
				.replace("_", "\\_");
	}
}
