package com.laytonsmith.core.compiler;

import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author lsmith
 */
public class FileOptions {

	private Target target;
	
	private boolean strict;
	private Set<CompilerWarning> supressWarnings;
	private String description;
	private boolean failOnWarning;

	//TODO: Make this non-public once this is all finished.
	public FileOptions(Map<Directive, String> parsedOptions, Target target) throws ConfigCompileException {
		this.target = target;
		strict = parseBoolean(getDefault(parsedOptions, Directive.STRICT, "false"));
		supressWarnings = parseList(getDefault(parsedOptions, Directive.SUPRESS_WARNINGS, ""));
		description = getDefault(parsedOptions, Directive.DESCRIPTION, null);
		failOnWarning = parseBoolean(getDefault(parsedOptions, Directive.FAIL_ON_WARNING, "false"));
	}
	
	private String getDefault(Map<Directive, String> map, Directive key, String defaultIfNone){
		if(map.containsKey(key)){
			return map.get(key);
		} else {
			return defaultIfNone;
		}
	}
	
	private boolean parseBoolean(String bool){
		if(bool.equalsIgnoreCase("false") || bool.equalsIgnoreCase("off") || bool.equalsIgnoreCase("0")){
			return false;
		} else {
			return true;
		}
	}
	
	private Set<CompilerWarning> parseList(String list) throws ConfigCompileException{
		Set<CompilerWarning> l = EnumSet.noneOf(CompilerWarning.class);
		for(String part : list.split(",")){
			if(!part.trim().isEmpty()){
				String name = part.trim();
				try{
					l.add(CompilerWarning.valueOf(name));
				} catch(IllegalArgumentException e){
					throw new ConfigCompileException("Unknown warning name: " + name + ". Check the suppresswarnings file options", target);
				}
			}
		}
		return l;
	}
	
	public boolean isStrict(){
		return strict;
	}
	
	public boolean isWarningSupressed(CompilerWarning warning){
		return supressWarnings.contains(warning);
	}
	
	public String getDescription(){
		return description;
	}
	
	public boolean failOnWarning(){
		return failOnWarning;
	}

	@Override
	public String toString() {
		return (strict ? "Strict Mode on" : "") + "\n" +
			   (supressWarnings.isEmpty() ? "" : "Suppressed Warnings: " + supressWarnings.toString() + "\n") +
			   (description == null ? "" : "File description: " + description + "\n");
				
	}
	
	public static enum Directive {
		STRICT("strict"),
		SUPRESS_WARNINGS("suppresswarnings"),
		DESCRIPTION("description"),
		FAIL_ON_WARNING("failonwarning"),
		;
		private String value;
		private Directive(String value){
			this.value = value;
		}

		@Override
		public String toString() {
			return value;
		}
		
		/**
		 * Returns the Directive given the specified directive toString value.
		 * @param name
		 * @return The specified Directive
		 * @throws IllegalArgumentException If the value cannot be found.
		 */
		public static Directive valueOfDirective(String name) throws IllegalArgumentException {
			for(Directive d : values()){
				if(d.toString().equals(name)){
					return d;
				}
			}
			throw new IllegalArgumentException();
		}
		
	}
	
}
