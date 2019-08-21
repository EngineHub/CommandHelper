package com.laytonsmith.tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.MCChatColor;
import com.laytonsmith.annotations.MEnum;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.constructs.NativeTypeList;
import com.laytonsmith.core.events.Event;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.functions.Function;
import java.util.stream.Collectors;

public class SyntaxHighlighters {

	public static final String HELP_TEXT = "File for the following syntax highlighters are currently available:\n"
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
				+ "\tnano - Use type \"nano\". Only the default theme is available.\n"
				+ "\tSublime Text - Use type \"sublime\". Only the default theme is available.\n"
				+ "\t\tTo install: Place in Sublime Text's ./SublimeText/data/Packages/User folder.\n"
				+ "\tSublime Text 3 - Use type \"sublime3\".\n"
				+ "\tAtom - Use type \"atom\". Only the default theme is available.\n"
				+ "\t\tTo install: Install package language-mscript from the Atom package manager."
				+ "\n\n"
				+ "Know how to write a syntax highlighter file for your favorite text editor? Let me know, and we\n"
				+ "can work to get it included in CommandHelper!";

	public static String generate(String type, String theme) {
		Implementation.forceServerType(Implementation.Type.BUKKIT);
		if("npp".equals(type) || "notepad++".equals(type)) {
			if("default".equals(theme)) {
				return template("/syntax-templates/notepad++/default.xml");
			}
			if("obsidian".equals(theme)) {
				return template("/syntax-templates/notepad++/obsidian.xml");
			}
			if("solarized-dark".equals(theme)) {
				return template("/syntax-templates/notepad++/solarized_dark.xml");
			}
			if("solarized-light".equals(theme)) {
				return template("/syntax-templates/notepad++/solarized_light.xml");
			}

			return "Available themes for Notepad++: default, obsidian, solarized-dark, solarized-light";
		}
		if("textwrangler".equals(type)) {
			return template("/syntax-templates/text-wrangler/default.plist");
		}
		if("geshi".equals(type)) {
			return template("/syntax-templates/geshi/default.php");
		}
		if("vim".equals(type)) {
			return template("/syntax-templates/vim/default.vim");
		}
		if("nano".equals(type)) {
			return template("/syntax-templates/nano/default.txt");
		}
		if("atom".equals(type)) {
			return template("/syntax-templates/atom/default.cson");
		}
		if("sublime".equals(type)) {
			return template("/syntax-templates/sublime/default.xml");
		}
		if("sublime3".equals(type)) {
			return template("/syntax-templates/sublime3/default.sublime-syntax");
		}

		return HELP_TEXT;
	}

	/**
	 * Available macros are listed in the code below.
	 *
	 * @param location
	 * @return
	 */
	private static String template(String location) {
		String template = Static.GetStringResource(location);
		//Replace all instances of ///! with nothing.
		template = template.replace("///!", "");
		Pattern p = Pattern.compile("%%(.*?)%%");
		Matcher m = p.matcher(template);
		while(m.find()) {
			template = template.replaceAll("%%" + m.group(1) + "%%", macro(m.group(1)));
		}
		return template;
	}

	private static String macro(String macroName) {
		String[] split = macroName.split(":");
		String type = split[0];
		String datalist = split[1];
		List<String> params = new ArrayList<>();
		for(int i = 2; i < split.length; i++) {
			params.add(split[i].toLowerCase());
		}
		List<String> base = new ArrayList<>();
		if(datalist.equalsIgnoreCase("colors")) {
			for(MCChatColor c : MCChatColor.values()) {
				base.add(c.name());
			}
		} else if(datalist.equalsIgnoreCase("keywords")) {
			for(String keyword : SimpleSyntaxHighlighter.KEYWORDS) {
				base.add(keyword);
			}
		} else if(datalist.equalsIgnoreCase("functions")) {
			for(Function f : GetFunctions()) {
				if(SimpleSyntaxHighlighter.KEYWORDS.contains(f.getName())) {
					// Keywords override functions
					continue;
				}
				if(!f.appearInDocumentation()) {
					continue;
				}
				if(params.contains("restricted") || params.contains("unrestricted")) {
					if(params.contains("restricted") && f.isRestricted()) {
						base.add(f.getName());
					} else if(params.contains("unrestricted") && !f.isRestricted()) {
						base.add(f.getName());
					}
				} else {
					base.add(f.getName());
				}
			}
		} else if(datalist.equalsIgnoreCase("events")) {
			for(Documentation d : GetEvents()) {
				base.add(d.getName());
			}
		} else if(datalist.equalsIgnoreCase("exceptions")) {
			for(Class<? extends CREThrowable> c : ClassDiscovery.getDefaultInstance().loadClassesWithAnnotationThatExtend(typeof.class, CREThrowable.class)) {
				base.add(ClassDiscovery.GetClassAnnotation(c, typeof.class).value());
			}
		} else if(datalist.equalsIgnoreCase("types")) {
			base.addAll(NativeTypeList.getNativeTypeList().stream().map(e -> e.getFQCN()).collect(Collectors.toList()));
			base.addAll(NativeTypeList.getNativeTypeList().stream()
					.map(e -> e.getSimpleName()).collect(Collectors.toList()));
			base.remove("null"); // Null is technically in the list, but it shouldn't be added.
		} else if(datalist.equalsIgnoreCase("enums")) {
			Set<String> set = new HashSet<>();
			Set<Class<? extends Enum>> enums = ClassDiscovery.getDefaultInstance().loadClassesWithAnnotationThatExtend(MEnum.class, Enum.class);
			for(Class<? extends Enum> e : enums) {
				Enum[] es = e.getEnumConstants();
				for(Enum ee : es) {
					set.add(ee.name());
				}
			}
			base.addAll(set);
		} else if(datalist.equalsIgnoreCase("fileOptions")) {
			base.addAll(FileOptions.getKnownOptions());
		}
		String header = "";
		String spliter = "IMPROPER FORMATTING";
		String footer = "";
		if(type.equalsIgnoreCase("space")) {
			if(params.contains("quoted")) {
				header = "'";
				spliter = "' '";
				footer = "'";
			} else {
				spliter = " ";
			}
		} else if(type.equalsIgnoreCase("comma")) {
			if(params.contains("quoted")) {
				header = "'";
				spliter = "', '";
				footer = "'";
			} else {
				spliter = ", ";
			}
		} else if(type.equalsIgnoreCase("pipe")) {
			if(params.contains("quoted")) {
				header = "'";
				spliter = "|";
				footer = "'";
			} else {
				spliter = "|";
			}
		} else if(type.equalsIgnoreCase("xml")) {
			String tag = "PLEASE INCLUDE THE TAG NAME USING tag=tagname AS A PARAMETER";
			for(String param : params) {
				//Find the tag name
				if(param.matches("tag=.*")) {
					tag = param.substring(4);
					break;
				}
			}
			if(params.contains("quoted")) {
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

	private static List<Documentation> GetEvents() {
		List<Documentation> l = new ArrayList<>();
		Set<Class<?>> classes = ClassDiscovery.getDefaultInstance().loadClassesWithAnnotation(api.class);
		for(Class<?> c : classes) {
			if(Event.class.isAssignableFrom(c) && Documentation.class.isAssignableFrom(c)) {
				try {
					Constructor<?> m = c.getConstructor();
					Documentation e = (Documentation) m.newInstance();
					l.add(e);
				} catch (NoSuchMethodException | SecurityException | InstantiationException
						| IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
					StreamUtils.GetSystemErr().println(ex.getMessage());
				}
			}
		}
		return l;
	}

	private static List<Function> GetFunctions() {
		List<Function> fl = new ArrayList<>();
		Set<Class<?>> functions = ClassDiscovery.getDefaultInstance().loadClassesWithAnnotation(api.class);
		for(Class<?> c : functions) {
			if(Function.class.isAssignableFrom(c)) {
				try {
					fl.add((Function) c.newInstance());
				} catch (InstantiationException | IllegalAccessException ex) {
					Logger.getLogger(SyntaxHighlighters.class.getName()).log(Level.SEVERE, null, ex);
				} catch (NoClassDefFoundError e) {
					//Hmm. No real good way to handle this... echo out to stderr, I guess.
					StreamUtils.GetSystemErr().println(e.getMessage());
				}
			}
		}
		return fl;
	}

	private static String Join(List<?> l, String joiner) {
		StringBuilder b = new StringBuilder();
		for(int i = 0; i < l.size(); i++) {
			if(i == 0) {
				b.append(l.get(i).toString());
			} else {
				b.append(joiner).append(l.get(i).toString());
			}
		}
		return b.toString();
	}
}
