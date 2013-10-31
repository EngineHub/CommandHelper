

package com.laytonsmith.tools;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassMirror;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.MCChatColor;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.events.Event;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.functions.Function;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author layton
 */
public class SyntaxHighlighters {

    public static String generate(String type, String theme) {
		Implementation.useAbstractEnumThread(false);
		Implementation.setServerType(Implementation.Type.BUKKIT);
        if("npp".equals(type) || "notepad++".equals(type)){

            if("default".equals(theme)){
                return template("/syntax-templates/notepad++/default.xml");
            }
            if("obsidian".equals(theme)){
                return template("/syntax-templates/notepad++/obsidian.xml");
            }
            if("solarized-dark".equals(theme)){
                return template("/syntax-templates/notepad++/solarized_dark.xml");
            }
            if("solarized-light".equals(theme)){
                return template("/syntax-templates/notepad++/solarized_light.xml");
            }

            return "Available themes for Notepad++: default, obsidian, solarized-dark, solarized-light";            
        }
        if("textwrangler".equals(type)){
            return template("/syntax-templates/text-wrangler/default.plist");
        }
        if("geshi".equals(type)){
            return template("/syntax-templates/geshi/default.php");
        }
        if("vim".equals(type)){
            return template("/syntax-templates/vim/default.vim");
        }
        if("nano".equals(type)){
            return template("/syntax-templates/nano/default.txt");
        }

        return "File for the following syntax highlighters are currently available:\n"
                + "\tNotepad++ - Use type \"npp\". You may also select a theme, either \"default\" or \"obsidian\"\n"
                + "\tTextWrangler - Use type \"textwrangler\". Only the default theme is available.\n"
                + "\t\tTo install: put the generated file in ~/Library/Application Support/TextWrangler/Language Modules/\n"
				+ "\t\tNote that this output file can also be used for BBEdit.\n"
                + "\tGeSHi - Use type \"geshi\". Only the default theme is available.\n"
                + "\tViM - Use type \"vim\". Only the default theme is available.\n"
                + "\t\tTo install: put in ~/.vim/syntax/commandhelper.vim then edit\n"
                + "\t\t~/.vim/ftdetect/commandhelper.vim and add the line \n"
                + "\t\tau BufRead,BufNewFile *.ms set filetype=commandhelper\n"
				+ "\t\tThen, if you're on linux and use cmdline mode, in ~/.vim/scripts.vim, add the following lines:\n"
				+ "\t\t\tif did_filetype()\n"
				+ "\t\t\t\tfinish\n"
				+ "\t\t\tendif\n"
				+ "\t\t\tif getline(1) =~# '^#!.*\\(/bin/env\\s\\+mscript\\|/bin/mscript\\)\\>'\n"
				+ "\t\t\t\tsetfiletype commandhelper\n"
				+ "\t\t\tendif"
                + "\t\t(Create directories and files as needed)\n"
                + "\tnano - Use type\"nano\". Only the default theme is available.\n"
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
        Pattern p = Pattern.compile("%%(.*?)%%");
        Matcher m = p.matcher(template);
        while(m.find()){
            template = template.replaceAll("%%" + m.group(1) + "%%", macro(m.group(1)));
        }
        return template;
    }
    
    private static String macro(String macroName){
        String[] split = macroName.split(":");
        String type = split[0];
        String datalist = split[1];
        List<String> params = new ArrayList<String>();
        for(int i = 2; i < split.length; i++){
            params.add(split[i].toLowerCase());
        }
        List<String> base = new ArrayList<String>();
        if(datalist.equalsIgnoreCase("colors")){
            for(MCChatColor c : MCChatColor.values()){
                base.add(c.name());
            }
        } else if(datalist.equalsIgnoreCase("keywords")){
            base.add("null");
            base.add("false");
            base.add("true");
        } else if(datalist.equalsIgnoreCase("functions")){
            for(Function f : GetFunctions()){
                if(!f.appearInDocumentation()){
                    continue;
                }
                if(params.contains("restricted") || params.contains("unrestricted")){
                    if(params.contains("restricted") && f.isRestricted()){
                        base.add(f.getName());
                    } else if(params.contains("unrestricted") && !f.isRestricted()){
                        base.add(f.getName());
                    }
                } else {
                    base.add(f.getName());
                }
            }
        } else if(datalist.equalsIgnoreCase("events")){
            for(Documentation d : GetEvents()){
                base.add(d.getName());
            }
        } else if(datalist.equalsIgnoreCase("exceptions")){
            for(Exceptions.ExceptionType e : Exceptions.ExceptionType.values()){
                base.add(e.name());
            }
        }
        String header = "";
        String spliter = "IMPROPER FORMATTING";
        String footer = "";
        if(type.equalsIgnoreCase("space")){
            if(params.contains("quoted")){
                header = "'";
                spliter = "' '";
                footer = "'";
            } else {
                spliter = " ";
            }
        } else if(type.equalsIgnoreCase("comma")){
            if(params.contains("quoted")){
                header = "'";
                spliter = "', '";
                footer = "'";
            } else {
                spliter = ", ";
            }
        } else if(type.equalsIgnoreCase("pipe")){
            if(params.contains("quoted")){
                header = "'";
                spliter = "|";
                footer = "'";
            } else {
                spliter = "|";
            }
        } else if(type.equalsIgnoreCase("xml")){
            String tag = "PLEASE INCLUDE THE TAG NAME USING tag=tagname AS A PARAMETER";
            for(String param : params){
                //Find the tag name
                if(param.matches("tag=.*")){
                    tag = param.substring(4);
                    break;
                }
            }
            if(params.contains("quoted")){
                header = "<" + tag + ">'";
                spliter = "'</" + tag + "><" + tag + ">'";
                footer = "'</" + tag + ">";
            } else {
                header = "<" + tag + ">";
                spliter = "</" + tag + "><" + tag + ">";
                footer = "</" + tag + ">";
            }
        }
        return header + Join(base, spliter) + footer;
        
    }

    
    
    private static List<Documentation> GetEvents(){
        List<Documentation> l = new ArrayList<Documentation>();
        Set<Class> classes = ClassDiscovery.getDefaultInstance().loadClassesWithAnnotation(api.class);
        for(Class c : classes){
            if (Event.class.isAssignableFrom(c) && Documentation.class.isAssignableFrom(c)) {
                try {
                    Constructor m = c.getConstructor();
                    Documentation e = (Documentation)m.newInstance();
                    l.add(e);
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                }
            }           
        }
        return l;
    }
    
    private static List<Function> GetFunctions(){
        List<Function> fl = new ArrayList<Function>();
        Set<Class> functions = ClassDiscovery.getDefaultInstance().loadClassesWithAnnotation(api.class);
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
