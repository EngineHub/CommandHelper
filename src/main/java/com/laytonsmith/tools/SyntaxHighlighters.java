/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.tools;

import com.laytonsmith.PureUtilities.ClassDiscovery;
import com.laytonsmith.abstraction.MCChatColor;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.api;
import com.laytonsmith.core.docs;
import com.laytonsmith.core.docs.type;
import com.laytonsmith.core.events.Event;
import com.laytonsmith.core.events.EventHandlerInterface;
import com.laytonsmith.core.functions.Function;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author layton
 */
public class SyntaxHighlighters {

    public static String generate(String type, String theme) {
        if("npp".equals(type) || "notepad++".equals(type)){

            if("default".equals(theme)){
                return template("/syntax-templates/notepad++/default.xml");
            }
            if("obsidian".equals(theme)){
                return template("/syntax-templates/notepad++/obsidian.xml");                
            }

            return "Available themes for Notepad++: default, obsidian";            
        }
        if("textwrangler".equals(type)){
            return template("/syntax-templates/text-wrangler/default.plist");
        }
        if("geshi".equals(type)){
            return template("/syntax-templates/geshi/default.php");
        }

        return "File for the following syntax highlighters are currently available:\n"
                + "\tNotepad++ - Use type \"npp\". You may also select a theme, either \"default\" or \"obsidian\"\n"
                + "\tTextWrangler - Use type \"textwrangler\". Only the default theme is available.\n"
                + "\tGeSHi - Use type\"geshi\". Only the default theme is available.\n"
                + "\n\n"
                + "Know how to write a syntax highlighter file for your favorite text editor? Let me know, and we\n"
                + "can work to get it included in CommandHelper!";
    }

    /**
     * Available macros are listed in the code below.
     * @param location
     * @return 
     */
    private static String template(String location) {
        String template = Static.GetStringResource(location);
        return template.replaceAll("%%FUNCTIONS_SPACE_SEPARATED%%", getAllFunctionsSpaceSeparated())
                       .replaceAll("%%EVENTS_SPACE_SEPARATED", getEventsSpaceSeparated())
                       .replaceAll("%%FUNCTIONS_QUOTED_COMMA_SEPARATED_UNRESTRICTED%%", getFunctionsQuotedCommaSeparatedUnrestricted())
                       .replaceAll("%%FUNCTIONS_QUOTED_COMMA_SEPARATED_RESTRICTED%%", getFunctionsQuotedCommaSeparatedRestricted())
                       .replaceAll("%%EVENTS_QUOTED_COMMA_SEPARATED%%", getEventsQuotedCommaSeparated())
                       .replaceAll("%%FUNCTIONS_XML_LIST_STRING%%", getAllFunctionsXML("string"))
                       .replaceAll("%%EVENTS_XML_LIST_STRING%%", getEventsXML("string"))
                       .replaceAll("%%COLOR_LIST_QUOTED_COMMA_SEPARATED%%", getColorListQuotedCommaSeparated());
    }

    private static String getAllFunctionsSpaceSeparated() {
        List<String> l = new ArrayList<String>();
        for(Function f : GetFunctions()){
            l.add(f.getName());
        }
        return Join(l, " ");
    }
    
    private static String getAllFunctionsXML(String element){
        List<String> l = new ArrayList<String>();
        for(Documentation f : GetFunctions()){
            l.add("<" + element + ">" + f.getName() + "</" + element + ">");
        }
        return Join(l, "\n");
    }
    
    private static String getEventsXML(String element){
        List<String> l = new ArrayList<String>();
        for(Documentation f : GetEvents()){
            l.add("<" + element + ">" + f.getName() + "</" + element + ">");
        }
        return Join(l, "\n");
    }
    
    private static String getFunctionsQuotedCommaSeparatedUnrestricted(){
        List<String> l = new ArrayList<String>();
        for(Function f : GetFunctions()){
            if(!f.isRestricted()){
                l.add("'" + f.getName() + "'");
            }
        }
        return Join(l, ", ");
    }
    
    private static String getFunctionsQuotedCommaSeparatedRestricted(){
        List<String> l = new ArrayList<String>();
        for(Function f : GetFunctions()){
            if(f.isRestricted()){
                l.add("'" + f.getName() + "'");
            }
        }
        return Join(l, ", ");
    }
    
    private static String getEventsQuotedCommaSeparated(){
        List<String> l = new ArrayList<String>();
        for(Documentation e : GetEvents()){
            l.add("'" + e.getName() + "'");
        }
        return Join(l, ", ");
    }

    private static String getEventsSpaceSeparated() {
        List<String> l = new ArrayList<String>();
        for(Documentation e : GetEvents()){
            l.add(e.getName());
        }
        return Join(l, " ");
    }
    
    private static List<Documentation> GetEvents(){
        List<Documentation> l = new ArrayList<Documentation>();
        Class[] classes = ClassDiscovery.GetClassesWithAnnotation(docs.class);
        for(Class c : classes){
            if (Documentation.class.isAssignableFrom(c)) {
                docs d = (docs) c.getAnnotation(docs.class);
                if(d.type().equals(type.EVENT)){
                    try {
                        Constructor m = c.getConstructor(EventHandlerInterface.class);
                        Documentation e = (Documentation)m.newInstance((EventHandlerInterface)null);
                        l.add(e);
                    } catch (Exception ex) {
                        System.err.println(ex.getMessage());
                    }
                }
            }           
        }
        return l;
    }
    
    private static List<Function> GetFunctions(){
        List<Function> fl = new ArrayList<Function>();
        Class[] functions = ClassDiscovery.GetClassesWithAnnotation(api.class);
        for(Class c : functions){
            if(Function.class.isAssignableFrom(c)){
                try {
                    fl.add((Function)c.newInstance());
                } catch (InstantiationException ex) {
                    Logger.getLogger(SyntaxHighlighters.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(SyntaxHighlighters.class.getName()).log(Level.SEVERE, null, ex);
                } catch(NoClassDefFoundError e){
                    //Hmm. No real good way to handle this... echo out to stderr, I guess.
                    System.err.println(e.getMessage());
                }
                
            }
        }
        return fl;
    }
    
    private static String getColorListQuotedCommaSeparated(){
        List<String> l = new ArrayList<String>();
        for(MCChatColor c : MCChatColor.values()){
            l.add("'" + c.name() + "'");
        }
        return Join(l, ", ");
    }
    
    private static String Join(List l, String joiner){
        StringBuilder b = new StringBuilder();
        for(int i = 0; i < l.size(); i++){
            if(i == 0){
                b.append(l.get(i).toString());
            } else {
                b.append(joiner).append(l.get(i).toString());
            }
        }
        return b.toString();
    }
    
}
