

package com.laytonsmith.tools.docgen;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.annotations.api;
import com.laytonsmith.commandhelper.CommandHelperFileLocations;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.Installer;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.events.Event;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.ExampleScript;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Layton
 */
public class DocGen {

    public static void main(String[] args) throws Exception {
		try{
			//Boilerplate startup stuff
			Implementation.setServerType(Implementation.Type.BUKKIT);
			Installer.Install(CommandHelperFileLocations.getDefault().getConfigDirectory());
			Prefs.init(CommandHelperFileLocations.getDefault().getPreferencesFile());
			CHLog.initialize(CommandHelperFileLocations.getDefault().getConfigDirectory());
			
			//System.out.println(functions("wiki", api.Platforms.INTERPRETER_JAVA, true));
			System.out.println(examples("string_append", true));
			//System.exit(0);
			//events("wiki");
			//System.out.println(Template("persistance_network"));
		} catch(Throwable t){
			t.printStackTrace();
			System.exit(1);
		} finally {
			System.exit(0);
		}
    }
	
	public static String examples(String function, boolean staged) throws Exception {
		FunctionBase fb = FunctionList.getFunction(new CFunction(function, Target.UNKNOWN));
		if(fb instanceof Function){
			Function f = (Function)fb;
			String restricted = (f instanceof Function && ((Function)f).isRestricted()) ? "<div style=\"background-color: red; font-weight: bold; text-align: center;\">Yes</div>"
                        : "<div style=\"background-color: green; font-weight: bold; text-align: center;\">No</div>";
			String optimizationMessage = "None";
			if(f instanceof Optimizable){
				Set<Optimizable.OptimizationOption> options = ((Optimizable)f).optimizationOptions();
				List<String> list = new ArrayList<String>();
				for(Optimizable.OptimizationOption option : options){
					list.add("[[CommandHelper/" + (staged?"Staged/":"") + "Optimizer#" + option.name() + "|" + option.name() + "]]");
				}
				optimizationMessage = StringUtils.Join(list, "<br />");
			}
			DocInfo di = new DocInfo(f.docs());
			StringBuilder thrown = new StringBuilder();
			if (f instanceof Function && ((Function)f).thrown() != null) {
				List thrownList = Arrays.asList(((Function)f).thrown());
				for (int i = 0; i < thrownList.size(); i++) {
					ExceptionType t = (ExceptionType) thrownList.get(i);
					if (i != 0) {
						thrown.append("<br />\n");
					}
					thrown.append("[[CommandHelper/Exceptions#").append(t.toString()).append("|").append(t.toString()).append("]]");
				}
			}
			String tableUsages = di.originalArgs.replace("|", "<hr />");
			String [] usages = di.originalArgs.split("\\|");
			StringBuilder usageBuilder = new StringBuilder();
			for(String usage : usages){
				usageBuilder.append("<pre>\n").append(f.getName()).append("(").append(usage.trim()).append(")\n</pre>");
			}
			StringBuilder exampleBuilder = new StringBuilder();
			if(f.examples() != null && f.examples().length > 0){
				int count = 1;
				//If the output was automatically generated, change the color of the pre
				for(ExampleScript es : f.examples()){
					exampleBuilder.append("====Example ").append(count).append("====\n")
							.append(es.getDescription()).append("\n\n"
							+ "Given the following code:\n<pre>");
					exampleBuilder.append(es.getScript()).append("\n</pre>");
					String style = "";
					if(es.isAutomatic()){
						style = " style=\"background-color: #BDC7E9\"";
						exampleBuilder.append("\n\nThe output would be:\n<pre");
					} else {
						exampleBuilder.append("\n\nThe output might be:\n<pre");
					}
					exampleBuilder.append(style).append(">").append(es.getOutput()).append("</pre>\n\n");
					count++;
				}
			} else {
				exampleBuilder.append("Sorry, there are no examples for this function! :(");
			}
			
			
			Map<String, String> templateFields = new HashMap<String, String>();
			templateFields.put("function_name", f.getName());
			templateFields.put("returns", di.ret);
			templateFields.put("tableUsages", tableUsages);
			templateFields.put("throws", thrown.toString());
			templateFields.put("since", f.since().toString());
			templateFields.put("restricted", restricted);
			templateFields.put("optimizationMessage", optimizationMessage);
			templateFields.put("description", di.extendedDesc==null?di.desc:di.topDesc + "\n\n" + di.extendedDesc);
			templateFields.put("usages", usageBuilder.toString());
			templateFields.put("examples", exampleBuilder.toString());
			templateFields.put("staged", staged?"Staged/":"");
					
					
			String template = StreamUtils.GetString(DocGenTemplates.class.getResourceAsStream("/templates/example_templates"));
			//Find all the %%templates%% in the template
			Matcher m = Pattern.compile("%%(.*?)%%").matcher(template);
			try{
				while(m.find()){
					String name = m.group(1);
					String templateValue = templateFields.get(name);
					template = template.replaceAll("%%" + Pattern.quote(name) + "%%", templateValue.replace("$", "\\$").replaceAll("\\'", "\\\\'"));
				}
				return template;
			} catch(RuntimeException e){
				throw new RuntimeException("Caught a runtime exception while generating template for " + function, e);
			}
		} else {
			throw new RuntimeException(function + " does not implement Function");
		}
	}

	/**
	 * Returns the documentation for a single function.
	 * @param type The type of output to use. May be one of: html, wiki, text
	 * @param platform The platform we're using
	 * @param staged Is this for the staged wiki?
	 * @return
	 * @throws ConfigCompileException 
	 */
    public static String functions(MarkupType type, api.Platforms platform, boolean staged) throws ConfigCompileException {
        List<FunctionBase> functions = FunctionList.getFunctionList(platform);
        HashMap<Class, ArrayList<FunctionBase>> functionlist = new HashMap<Class, ArrayList<FunctionBase>>();
		StringBuilder out = new StringBuilder();
        for (int i = 0; i < functions.size(); i++) {
            //Sort the functions into classes
            FunctionBase f = functions.get(i);
            Class apiClass = (f.getClass().getEnclosingClass() != null
                    ? f.getClass().getEnclosingClass()
                    : null);
            ArrayList<FunctionBase> fl = functionlist.get(apiClass);
            if (fl == null) {
                fl = new ArrayList<FunctionBase>();
                functionlist.put(apiClass, fl);
            }
            fl.add(f);
        }
        if (type == MarkupType.HTML) {			
            out.append("Command Helper uses a language called MethodScript, which greatly extend the capabilities of the plugin, "
                    + "and make the plugin a fully "
                    + "<a href=\"http://en.wikipedia.org/wiki/Turing_Complete\">Turing Complete</a> language. "
                    + "There are several functions defined, and they are grouped into \"classes\". \n");
        } else if (type == MarkupType.WIKI) {
            out.append("Command Helper uses a language called MethodScript, which greatly extend the capabilities of the plugin, "
                    + "and make the plugin a fully "
                    + "[http://en.wikipedia.org/wiki/Turing_Complete Turing Complete] language. "
                    + "There are several functions defined, and they are grouped into \"classes\". \n");
            out.append("<p>Each function has its own page for documentation, where you can put examples for how to use"
                    + " particular functions. Because this is a wiki, it is encouraged that you edit the pages if you see errors, "
                    + "or can think of a better example to show. Please copy over [[CommandHelper/API/Function Template|this template]]"
                    + " and use it.\n");
        } else if (type == MarkupType.TEXT) {
            out.append("Command Helper uses a language called MethodScript, which greatly extend the capabilities of the plugin, "
                    + "and make the plugin a fully "
                    + "Turing Complete language [http://en.wikipedia.org/wiki/Turing_Complete].\n"
                    + "There are several functions defined, and they are grouped into \"classes\".\n");
        }
        List<Map.Entry<Class, ArrayList<FunctionBase>>> entrySet = new ArrayList<Map.Entry<Class, ArrayList<FunctionBase>>>(functionlist.entrySet());
        Collections.sort(entrySet, new Comparator<Map.Entry<Class, ArrayList<FunctionBase>>>() {

            public int compare(Map.Entry<Class, ArrayList<FunctionBase>> o1, Map.Entry<Class, ArrayList<FunctionBase>> o2) {
                return o1.getKey().getName().compareTo(o2.getKey().getName());
            }
        });
        int total = 0;

		int workingExamples = 0;
        for (Map.Entry<Class, ArrayList<FunctionBase>> entry : entrySet) {
            Class apiClass = entry.getKey();
            String className = apiClass.getName().split("\\.")[apiClass.getName().split("\\.").length - 1];
            if (className.equals("Sandbox")) {
                continue; //Skip Sandbox functions
            }
            String classDocs = null;
            try {
                Method m = apiClass.getMethod("docs", (Class[]) null);
                Object o = null;
                if ((m.getModifiers() & Modifier.STATIC) == 0) {
                    try {
                        o = apiClass.newInstance();
                    } catch (InstantiationException ex) {
                    }
                }
                classDocs = (String) m.invoke(o, (Object[]) null);
            } catch (IllegalAccessException ex) {
            } catch (IllegalArgumentException ex) {
            } catch (InvocationTargetException ex) {
            } catch (NoSuchMethodException e) {
            } catch (Exception e){
				e.printStackTrace(System.err);
				System.err.println("Continuing however.");
			}
            StringBuilder intro = new StringBuilder();
            if (type == MarkupType.HTML) {
                if (className != null) {
                    intro.append("<h1>").append(className).append("</h1>" + "\n");
                    intro.append(classDocs == null ? "" : classDocs).append("\n");
                } else {
                    intro.append("<h1>Other Functions</h1>" + "\n");
                }
                intro.append("<table>" + "\n");
            } else if (type == MarkupType.WIKI) {
                if (className != null) {
                    intro.append("===").append(className).append("===" + "\n");
                    intro.append(classDocs == null ? "" : classDocs).append("\n");
                } else {
                    intro.append("===Other Functions===" + "\n");
                }
                intro.append("{| width=\"100%\" cellspacing=\"1\" cellpadding=\"1\" border=\"1\" class=\"wikitable\"\n"
                        + "|-\n"
                        + "! scope=\"col\" width=\"6%\" | Function Name\n"
                        + "! scope=\"col\" width=\"5%\" | Returns\n"
                        + "! scope=\"col\" width=\"10%\" | Arguments\n"
                        + "! scope=\"col\" width=\"10%\" | Throws\n"
                        + "! scope=\"col\" width=\"61%\" | Description\n"
                        + "! scope=\"col\" width=\"3%\" | Since\n"
                        + "! scope=\"col\" width=\"5%\" | Restricted" + "\n");
            } else if (type == MarkupType.TEXT) {
                intro.append("\n").append(className).append("\n");
                intro.append("**********************************************************************************************" + "\n");
                if (className != null) {
                    intro.append(classDocs == null ? "" : classDocs).append("\n");
                } else {
                    intro.append("Other Functions" + "\n");
                }
                intro.append("**********************************************************************************************" + "\n");
            }
			List<FunctionBase> documentableFunctions = new ArrayList<FunctionBase>();
			for(FunctionBase f : entry.getValue()){
				if(f.appearInDocumentation()){
					documentableFunctions.add(f);
				}
			}
            if(!documentableFunctions.isEmpty()){
                out.append(intro.toString() + "\n");
            }
            Collections.sort(documentableFunctions, new Comparator<FunctionBase>() {

                public int compare(FunctionBase o1, FunctionBase o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            for (FunctionBase f : documentableFunctions) {
                total++;
                String doc = f.docs();
                String restricted = (f instanceof Function && ((Function)f).isRestricted()) ? "<div style=\"background-color: red; font-weight: bold; text-align: center;\">Yes</div>"
                        : "<div style=\"background-color: green; font-weight: bold; text-align: center;\">No</div>";
                StringBuilder thrown = new StringBuilder();
                if (f instanceof Function && ((Function)f).thrown() != null) {
                    List thrownList = Arrays.asList(((Function)f).thrown());
                    for (int i = 0; i < thrownList.size(); i++) {
                        ExceptionType t = (ExceptionType) thrownList.get(i);
                        if (type == MarkupType.HTML || type == MarkupType.TEXT) {
                            if (i != 0) {
                                thrown.append((type == MarkupType.HTML ? "<br />\n" : " | "));
                            }
                            thrown.append(t.toString());
                        } else {
                            if (i != 0) {
                                thrown.append("<br />\n");
                            }
                            thrown.append("[[CommandHelper/Exceptions#").append(t.toString()).append("|").append(t.toString()).append("]]");
                        }
                    }
                }

                String since = (f instanceof Documentation ?((Documentation)f).since().toString():"0.0.0");
                DocInfo di = new DocInfo(doc);
				boolean hasExample = false;
				if(f instanceof Function && ((Function)f).examples() != null && ((Function)f).examples().length > 0){
					hasExample = true;
					workingExamples++;
				}
                if (di.ret == null || di.args == null || di.desc == null) {
                    out.append(f.getName() + "'s documentation is not correctly formatted. Please check it and try again.\n");
                }
                if (type == MarkupType.HTML) {
                    out.append("<tr><td>" + di.ret + "</td><td>" + di.args + "</td><td>" + thrown.toString() + "</td><td>" + di.desc + "</td><td>" + since + "</td><td>" + restricted + "</td></tr>\n");
                } else if (type == MarkupType.WIKI) {
                    //Turn args into a prettified version
                    out.append("|- id=\"" + f.getName() + "\"\n"
                            + "! scope=\"row\" | [[CommandHelper/" + (staged?"Staged/":"") + "API/" + f.getName() + "|" + f.getName() + "]]()\n"
                            + "| " + di.ret + "\n"
                            + "| " + di.args + "\n"
                            + "| " + thrown.toString() + "\n"
                            + "| " + (di.topDesc != null ? di.topDesc + " [[CommandHelper/" + (staged?"Staged/":"") + "API/" + f.getName() + "#Description|See More...]]" : di.desc) 
							+ (hasExample?"<br />([[CommandHelper/" + (staged?"Staged/":"") + "API/" + f.getName() + "#Examples|Examples...]])":"") + "\n"
                            + "| " + since + "\n"
                            + "| " + restricted + "\n");

                } else if (type == MarkupType.TEXT) {
                    out.append(di.ret + " " + f.getName() + "(" + di.args + ")" + " {" + thrown.toString() + "}\n\t" + di.desc + "\n\t" + since + ((f instanceof Function?((Function)f).isRestricted():false) ? "\n\tThis function is restricted"
                            : "\n\tThis function is not restricted\n"));
                }
            }
            if(!documentableFunctions.isEmpty()){
                if (type == MarkupType.HTML) {
                    out.append("</table>\n");
                } else if (type == MarkupType.WIKI) {
                    out.append("|}\n");
                } else if (type == MarkupType.TEXT) {
                    out.append("\n");
                }
            }
        }
        if (type == MarkupType.HTML) {
            out.append(""
                    + "<h2>Errors in documentation</h2>\n"
                    + "<em>Please note that this documentation is generated automatically,"
                    + " if you notice an error in the documentation, please file a bug report for the"
                    + " plugin itself!</em>"
                    + "<div style='text-size:small; text-decoration:italics; color:grey'>There are " + total + " functions in this API page</div>\n");
        } else if (type == MarkupType.WIKI) {
            out.append(""
                    + "===Errors in documentation===\n"
                    + "''Please note that this documentation is generated automatically,"
                    + " if you notice an error in the documentation, please file a bug report for the"
                    + " plugin itself!'' For information on undocumented functions, see [[CommandHelper/Sandbox|this page]]"
                    + "<div style='font-size:xx-small; font-style:italic; color:grey'>There are " + total + " functions in this API page, " + workingExamples + " of which"
					+ " have examples.</div>\n\n{{LearningTrail}}\n");
        }
		return out.toString();
    }
    
    public static String Template(String template, boolean staged){
		Map<String, String> customTemplates = new HashMap<String, String>();
		customTemplates.put("staged", staged?"Staged/":"");
	    return DocGenTemplates.Generate(template, customTemplates);
    }

    public static String events(MarkupType type) {
        Set<Class> classes = ClassDiscovery.getDefaultInstance().loadClassesWithAnnotation(api.class);
        Set<Documentation> list = new TreeSet<Documentation>();
        for (Class c : classes) {
            if (Event.class.isAssignableFrom(c)
                    && Documentation.class.isAssignableFrom(c)) {
                try {
                    //First, we have to instatiate the event.
                    Constructor<Event> cons = c.getConstructor();
                    Documentation docs = cons.newInstance();
                    list.add(docs);
                } catch (Exception ex) {
                    System.err.println("Could not get documentation for " + c.getSimpleName());
                }
            }
        }

        StringBuilder doc = new StringBuilder();
        if (type == MarkupType.HTML) {
            doc.append("Events allow you to trigger scripts not just on commands, but also on other actions, such as"
                    + " a player logging in, or a player breaking a block. See the documentation on events for"
                    + " more information"
                    + "<table><thead><tr><th>Name</th><th>Description</th><th>Prefilters</th>"
                    + "<th>Event Data</th><th>Mutable Fields</th><th>Since</th></thead><tbody>");
        } else if (type == MarkupType.WIKI) {
            doc.append("Events allow you to trigger scripts not just on commands, but also on other actions, such as"
                    + " a player logging in, or a player breaking a block. See the [[CommandHelper/Events|documentation on events]] for"
                    + " more information<br />\n\n");
            
            doc.append("{| width=\"100%\" cellspacing=\"1\" cellpadding=\"1\" border=\"1\" class=\"wikitable\"\n"
                        + "|-\n"
                        + "! scope=\"col\" width=\"7%\" | Event Name\n"
                        + "! scope=\"col\" width=\"36%\" | Description\n"
                        + "! scope=\"col\" width=\"18%\" | Prefilters\n"
                        + "! scope=\"col\" width=\"18%\" | Event Data\n"
                        + "! scope=\"col\" width=\"18%\" | Mutable Fields\n"
                        + "! scope=\"col\" width=\"3%\" | Since\n");
        } else if (type == MarkupType.TEXT) {
            doc.append("Events allow you to trigger scripts not just on commands, but also on other actions, such as"
                    + " a player logging in, or a player breaking a block. See the documentation on events for"
                    + " more information\n\n\n");
        }
        Pattern p = Pattern.compile("\\{(.*?)\\} *?(.*?) *?\\{(.*?)\\} *?\\{(.*?)\\}");
        for (Documentation d : list) {
            Matcher m = p.matcher(d.docs());
            if (m.find()) {
                String name = d.getName();
                String description = m.group(2).trim();
                String prefilter = PrefilterData.Get(m.group(1).split("\\|"), type);
                String eventData = EventData.Get(m.group(3).split("\\|"), type);
                String mutability = MutabilityData.Get(m.group(4).split("\\|"), type);
                //String manualTrigger = ManualTriggerData.Get(m.group(5).split("\\|"), type);
                String since = d.since().toString();

                if (type == MarkupType.HTML) {
                    doc.append("<tr><td style=\"vertical-align:top\">").append(name).append("</td><td style=\"vertical-align:top\">").append(description).append("</td><td style=\"vertical-align:top\">").append(prefilter).append("</td><td style=\"vertical-align:top\">").append(eventData).append("</td><td style=\"vertical-align:top\">").append(mutability).append("</td><td style=\"vertical-align:top\">").append(since).append("</td></tr>\n");
                } else if (type == MarkupType.WIKI) {
                    doc.append("|-\n" + "! scope=\"row\" | [[CommandHelper/Event API/").append(name).append("|").append(name).append("]]\n" + "| ").append(description).append("\n" + "| ").append(prefilter).append("\n" + "| ").append(eventData).append("\n" + "| ").append(mutability).append("\n" + "| ").append(since).append("\n");
                } else if (type == MarkupType.TEXT) {
                    doc.append("Name: ").append(name).append("\nDescription: ").append(description).append("\nPrefilters:\n").append(prefilter).append("\nEvent Data:\n").append(eventData).append("\nMutable Fields:\n").append(mutability).append("\nSince: ").append(since).append("\n\n");
                }
            }
        }

        if (type == MarkupType.HTML) {
            doc.append("</tbody></table>\n");
        } else if (type == MarkupType.WIKI) {
            doc.append("|}\n");
        }


        if (type == MarkupType.HTML) {
            doc.append(""
                    + "<h2>Errors in documentation</h2>\n"
                    + "<em>Please note that this documentation is generated automatically,"
                    + " if you notice an error in the documentation, please file a bug report for the"
                    + " plugin itself!</em>\n");
        } else if (type == MarkupType.WIKI) {
            doc.append(""
                    + "===Errors in documentation===\n"
                    + "''Please note that this documentation is generated automatically,"
                    + " if you notice an error in the documentation, please file a bug report for the"
                    + " plugin itself!'' For information on undocumented functions, see [[CommandHelper/Sandbox|this page]]\n\n{{LearningTrail}}\n");
        }
		return doc.toString();
    }

    public static class PrefilterData {

        public static String Get(String[] data, MarkupType type) {
            StringBuilder b = new StringBuilder();
            boolean first = true;
			if(data.length == 1 && "".equals(data[0].trim())){
				return "";
			}
            for (String d : data) {
                int split = d.indexOf(':');
                String name; 
                String description; 
				if(split == -1){                    
                    name = d;
                    description = "";
                } else {
                    name = d.substring(0, split).trim();
                    description = ExpandMacro(d.substring(split + 1).trim(), type);
                }
                if (type == MarkupType.HTML) {
                    b.append(first ? "" : "<br />").append("<strong>").append(name).append("</strong>: ").append(description);
                } else if (type == MarkupType.WIKI) {
                    b.append(first ? "" : "<br />").append("'''").append(name).append("''': ").append(description);
                } else if (type == MarkupType.TEXT) {
                    b.append(first ? "" : "\n").append("\t").append(name).append(": ").append(description);
                } else if(type == MarkupType.MARKDOWN){
					b.append(first ? "" : "  \n").append("**").append(name).append("**: ").append(description);
				}
                first = false;
            }
            return b.toString();
        }

        private static String ExpandMacro(String macro, MarkupType type) {
            if (type == MarkupType.HTML) {
                return "<em>" + macro
                    .replaceAll("<string match>", "&lt;String Match&gt;")
                    .replaceAll("<boolean match>", "&lt;Boolean Match&gt;")
                    .replaceAll("<regex>", "&lt;Regex&gt;")
                    .replaceAll("<item match>", "&lt;Item Match&gt;")
                    .replaceAll("<location match>", "&lt;Location Match&gt;")
                    .replaceAll("<math match>", "&lt;Math Match&gt;")
                    .replaceAll("<macro>", "&lt;Macro&gt;")
                    .replaceAll("<expression>", "&lt;Expression&gt;") + "</em>";
            } else if (type == MarkupType.WIKI) {
                return macro
                    .replaceAll("<string match>", "[[CommandHelper/Events/Prefilters#String Match|String Match]]")
                    .replaceAll("<boolean match>", "[[CommandHelper/Events/Prefilters#Boolean Match|Boolean Match]]")
                    .replaceAll("<regex>", "[[CommandHelper/Events/Prefilters#Regex|Regex]]")
                    .replaceAll("<item match>", "[[CommandHelper/Events/Prefilters#Item Match|Item Match]]")
                    .replaceAll("<location match>", "[[CommandHelper/Events/Prefilters#Location Match|Location Match]]")
                    .replaceAll("<math match>", "[[CommandHelper/Events/Prefilters#Math Match|Math Match]]")
                    .replaceAll("<macro>", "[[CommandHelper/Events/Prefilters#Macro|Macro]]")
                    .replaceAll("<expression>", "[[CommandHelper/Events/Prefilters#Expression|Expression]]");                
            } else if (type == MarkupType.TEXT || type == MarkupType.MARKDOWN) {
                return macro
                    .replaceAll("<string match>", "<String Match>")
                    .replaceAll("<boolean match>", "<Boolean Match>")
                    .replaceAll("<regex>", "<Regex>")
                    .replaceAll("<item match>", "<Item Match>")
                    .replaceAll("<location match>", "<Location Match>")
                    .replaceAll("<math match>", "<Math Match>")
                    .replaceAll("<macro>", "<Macro>")
                    .replaceAll("<expression>", "<Expression>");
            }
            return macro;
        }
    }

    public static class EventData {

        public static String Get(String[] data, MarkupType type) {
            StringBuilder b = new StringBuilder();
            boolean first = true;
			if(data.length == 1 && "".equals(data[0].trim())){
				return "";
			}
            for (String d : data) {
                int split = d.indexOf(':');
                String name;
                String description;
                if(split == -1){                    
                    name = d;
                    description = "";
                } else {
                    name = d.substring(0, split).trim();
                    description = d.substring(split + 1).trim();
                }
                if (type == MarkupType.HTML) {
                    b.append(first ? "" : "<br />").append("<strong>").append(name).append("</strong>: ").append(description);
                } else if (type == MarkupType.WIKI) {
                    b.append(first ? "" : "<br />").append("'''").append(name).append("''': ").append(description);
                } else if (type == MarkupType.TEXT) {
                    b.append(first ? "" : "\n").append("\t").append(name).append(": ").append(description);
                } else if (type == MarkupType.MARKDOWN){
					b.append(first ? "" : "  \n").append("**").append(name).append("**").append(": ").append(description);
				}
                first = false;
            }
            return b.toString();
        }
    }

    public static class MutabilityData {

        public static String Get(String[] data, MarkupType type) {
            StringBuilder b = new StringBuilder();
            boolean first = true;
			if(data.length == 1 && "".equals(data[0].trim())){
				return "";
			}
            for (String d : data) {
                int split = d.indexOf(':');
                if (split == -1) {
                    if (type == MarkupType.HTML) {
                        b.append(first ? "" : "<br />").append("<strong>").append(d.trim()).append("</strong>");
                    } else if (type == MarkupType.WIKI) {
                        b.append(first ? "" : "<br />").append("'''").append(d.trim()).append("'''");
                    } else if (type == MarkupType.TEXT) {
                        b.append(first ? "" : "\n").append("\t").append(d.trim());
                    } else if (type == MarkupType.MARKDOWN){
                        b.append(first ? "" : "  \n").append("**").append(d.trim()).append("**");
					}
                } else {
                    String name = d.substring(0, split).trim();
                    String description = d.substring(split).trim();
                    if (type == MarkupType.HTML) {
                        b.append(first ? "" : "<br />").append("<strong>").append(name).append("</strong>: ").append(description);
                    } else if (type == MarkupType.WIKI) {
                        b.append(first ? "" : "<br />").append("'''").append(name).append("''': ").append(description);
                    } else if (type == MarkupType.TEXT) {
                        b.append(first ? "" : "\n").append("\t").append(name).append(": ").append(description);
                    } else if (type == MarkupType.MARKDOWN){
                        b.append(first ? "" : "  \n").append("**").append(name).append("**: ").append(description);
					}
                }
                first = false;
            }
            return b.toString();
        }
    }
	
	public static class DocInfo{
		/**
		 * The return type
		 */
		public String ret;
		/**
		 * The args, with html styling in place
		 */
		public String args;
		/**
		 * The args, without html styling in place (but with [ brackets ] to denote optional arguments
		 */
		public String originalArgs;		
		/**
		 * The full description, if the ---- separator isn't present, or
		 * the top description if not present.
		 */
		public String desc;
		/**
		 * The top description, or null if the ---- separator isn't present.
		 */
		public String topDesc = null;
		/**
		 * The extended description, or null if the ---- separator isn't present.
		 */
		public String extendedDesc = null;
		public DocInfo(String doc){
			Pattern p = Pattern.compile("(?s)\\s*(.*?)\\s*\\{(.*?)\\}\\s*(.*)\\s*");
			Matcher m = p.matcher(doc);
			if (m.find()) {
				ret = m.group(1);
				originalArgs = m.group(2);
				desc = m.group(3);
				if(desc.contains("----")){
					String [] parts = desc.split("----", 2);
					desc = topDesc = parts[0].trim();
					extendedDesc = parts[1].trim();
				}
			}
			args = originalArgs.replaceAll("\\|", "<hr />").replaceAll("\\[(.*?)\\]", "<strong>[</strong>$1<strong>]</strong>");
		}
	}
	
	public static enum MarkupType {
		HTML, WIKI, TEXT, MARKDOWN;
	}
}
