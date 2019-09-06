package com.laytonsmith.tools.docgen.sitedeploy;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.annotations.api.Platforms;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.MethodScriptFileLocations;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.compiler.KeywordList;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.NativeTypeList;
import com.laytonsmith.core.events.Event;
import com.laytonsmith.core.events.EventList;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.extensions.ExtensionManager;
import com.laytonsmith.core.extensions.ExtensionTracker;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.tools.docgen.DocGen;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONValue;

/**
 * This class builds the JSON API file
 *
 * @author cailin
 */
public class APIBuilder {

	/**
	 * Can be run independently, or by the programmer, but this will always print out to System.out the json. Extensions
	 * are loaded first.
	 * @param args Ignored.
	 */
	public static void main(String[] args) {
		try {
			Implementation.setServerType(Implementation.Type.SHELL);
		} catch (RuntimeException e) {
			// Eh.. in most cases this is wrong, but this will happen when json-api is called, and that's ok.
		}
		ClassDiscovery.getDefaultInstance().addDiscoveryLocation(ClassDiscovery.GetClassContainer(APIBuilder.class));
		ExtensionManager.AddDiscoveryLocation(MethodScriptFileLocations.getDefault().getExtensionsDirectory());
		ExtensionManager.Cache(MethodScriptFileLocations.getDefault().getExtensionCacheDirectory());
		ExtensionManager.Initialize(ClassDiscovery.getDefaultInstance());
		StreamUtils.GetSystemOut().println(JSONValue.toJSONString(new APIBuilder().build()));
	}

	/**
	 * Returns a Map that can be converted into a json (or used for other purposes), which provides various information
	 * about the entire API, including known extensions.
	 *
	 * @return
	 */
	public Map<String, Object> build() {
		Map<String, Object> json = new TreeMap<>();
		{
			// functions
			Map<String, Map<String, Object>> api = new TreeMap<>();
			for(FunctionBase f : FunctionList.getFunctionList(Platforms.INTERPRETER_JAVA, null)) {
				if(f instanceof Function) {
					Function ff = (Function) f;
					DocGen.DocInfo di;
					try {
						di = new DocGen.DocInfo(ff.docs());
					} catch (IllegalArgumentException ex) {
						continue;
					}
					Map<String, Object> function = new TreeMap<>();
					function.put("name", ff.getName());
					function.put("ret", di.ret);
					function.put("args", di.originalArgs);
					List<String> thrown = new ArrayList<>();
					try {
						if(ff.thrown() != null) {
							for(Class<? extends CREThrowable> c : ff.thrown()) {
								thrown.add(ClassDiscovery.GetClassAnnotation(c, typeof.class).value());
							}
						}
					} catch (Throwable t) {
						Logger.getLogger("default").log(Level.SEVERE, null, t);
					}
					function.put("thrown", thrown);
					function.put("desc", di.desc);
					function.put("extdesc", di.extendedDesc);
					function.put("shortdesc", di.topDesc);
					function.put("since", ff.since().toString());
					function.put("restricted", ff.isRestricted());
					function.put("coreFunction", ff.isCore());
					List<String> optimizations = new ArrayList<>();
					if(ff instanceof Optimizable) {
						for(Optimizable.OptimizationOption o : ((Optimizable) ff).optimizationOptions()) {
							optimizations.add(o.name());
						}
					}
					function.put("optimizations", optimizations);
					hide athide = ff.getClass().getAnnotation(hide.class);
					String hidden = athide == null ? null : athide.value();
					function.put("hidden", hidden);
					ExtensionTracker et = ExtensionManager.getTrackers().get(ff.getSourceJar());
					String extId;
					if(et != null) {
						extId = et.getIdentifier();
					} else {
						// Legacy extensions don't have an ExtensionTracker, so we need to come up with a name
						// for them. Just use the jar name, but remove the oldstyle- that is prepended by the
						// extension manager.
						extId = StringUtils.replaceLast(new java.io.File(ff.getSourceJar().getPath().replaceFirst("/", ""))
								.getName().replaceFirst("oldstyle-", ""), ".jar", "");
					}
					function.put("source", extId);
					api.put(ff.getName(), function);
				}
			}
			json.put("functions", api);
		}
		{
			// events
			Map<String, Map<String, Object>> events = new TreeMap<>();
			for(Event e : EventList.GetEvents()) {
				try {
					DocGen.EventDocInfo edi;
					try {
						edi = new DocGen.EventDocInfo(e.docs(), e.getName());
					} catch (IllegalArgumentException ex) {
						continue;
					}
					Map<String, Object> event = new TreeMap<>();
					event.put("name", e.getName());
					event.put("desc", edi.description);
					Map<String, String> ed = new TreeMap<>();
					for(DocGen.EventDocInfo.EventData edd : edi.eventData) {
						ed.put(edd.name, edd.description);
					}
					event.put("eventData", ed);
					Map<String, String> md = new TreeMap<>();
					for(DocGen.EventDocInfo.MutabilityData mdd : edi.mutability) {
						md.put(mdd.name, mdd.description);
					}
					event.put("mutability", md);
					Map<String, String> pd = new TreeMap<>();
					for(DocGen.EventDocInfo.PrefilterData pdd : edi.prefilter) {
						pd.put(pdd.name, pdd.formatDescription(DocGen.MarkupType.TEXT));
					}
					event.put("prefilters", pd);
					event.put("since", e.since().toString());
					String extId = ExtensionManager.getTrackers().get(e.getSourceJar()).getIdentifier();
					event.put("source", extId);
					events.put(e.getName(), event);
				} catch (Exception ex) {
					Logger.getLogger("default").log(Level.SEVERE, e.getName(), ex);
				}
			}
			json.put("events", events);
		}
		{
			Map<String, Map<String, String>> extensions = new TreeMap<>();
			// extensions
			for(ExtensionTracker t : ExtensionManager.getTrackers().values()) {
				Map<String, String> ext = new TreeMap<>();
				ext.put("id", t.getIdentifier());
				ext.put("version", t.getVersion().toString());
				extensions.put(t.getIdentifier(), ext);
			}
			json.put("extensions", extensions);
		}
		{
			// keywords
			Map<String, Map<String, String>> keywords = new TreeMap<>();
			for(Keyword keyword : KeywordList.getKeywordList()) {
				Map<String, String> keyw = new TreeMap<>();
				keyw.put("name", keyword.getKeywordName());
				keyw.put("docs", keyword.docs());
				keyw.put("since", keyword.since().toString());
				String extId = ExtensionManager.getTrackers().get(keyword.getSourceJar()).getIdentifier();
				keyw.put("source", extId);
				keywords.put(keyword.getKeywordName(), keyw);
			}
			json.put("keywords", keywords);
		}
		{
			//objects
			Map<String, Map<String, Object>> objects = new TreeMap<>();
			for(FullyQualifiedClassName t : NativeTypeList.getNativeTypeList()) {
				try {
					if("void".equals(t.getFQCN()) || "null".equals(t.getFQCN())) {
						// These are super special, and aren't really
						// "real" datatypes, so shouldn't be included.
						continue;
					}
					Map<String, Object> obj = new TreeMap<>();
					String name;
					String docs;
					Version since;
					URL source;
					CClassType[] interfaces;
					CClassType[] supers;

					Mixed m = NativeTypeList.getInvalidInstanceForUse(t);
					name = m.getName();
					docs = m.docs();
					since = m.since();
					source = m.getSourceJar();
					interfaces = m.getInterfaces();
					supers = m.getSuperclasses();
					obj.put("type", name);
					obj.put("docs", docs);
					obj.put("since", since.toString());
					String extId = ExtensionManager.getTrackers().get(source).getIdentifier();
					obj.put("source", extId);
					List<String> i = new ArrayList<>();
					List<String> s = new ArrayList<>();
					for(CClassType c : interfaces) {
						i.add(c.val());
					}
					for(CClassType c : supers) {
						s.add(c.val());
					}
					obj.put("interfaces", i);
					obj.put("superclasses", s);
					//obj.put("objectType", m.getObjectType().toString());
					//obj.put("containingClass", m.getContainingClass().getName());
					//obj.put("objectModifiers", m.getObjectModifiers());
					objects.put(name, obj);
				} catch (ClassNotFoundException ex) {
					// Pretty sure this isn't possible?
					Logger.getLogger(SiteDeploy.class.getName()).log(Level.SEVERE, null, ex);
				} catch (Exception ex) {
					Logger.getLogger(SiteDeploy.class.getName()).log(Level.SEVERE, "Could not instantiate " + t, ex);
				}
			}
			json.put("objects", objects);
		}
		return json;
	}
}
