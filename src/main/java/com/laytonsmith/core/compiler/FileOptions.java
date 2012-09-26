package com.laytonsmith.core.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author lsmith
 */
public class FileOptions {

	private boolean strict;
	private List<String> supressWarnings;
	private String description;
	//TODO: Make this non-public once this is all finished.
	public FileOptions(Map<String, String> parsedOptions) {
		strict = parseBoolean(getDefault(parsedOptions, "strict", "false"));
		supressWarnings = parseList(getDefault(parsedOptions, "supresswarnings", ""));
		description = getDefault(parsedOptions, "description", null);
	}
	
	private String getDefault(Map<String, String> map, String key, String defaultIfNone){
		if(map.containsKey(key)){
			return map.get(key);
		} else {
			return defaultIfNone;
		}
	}
	
	private boolean parseBoolean(String bool){
		if(bool.equalsIgnoreCase("false") || bool.equalsIgnoreCase("off")){
			return false;
		} else {
			return true;
		}
	}
	
	private List<String> parseList(String list){
		List<String> l = new ArrayList<String>();
		for(String part : list.split(",")){
			if(!part.trim().isEmpty()){
				l.add(part.trim().toLowerCase());
			}
		}
		return l;
	}
	
	public boolean isStrict(){
		return strict;
	}
	
	public boolean isWarningSupressed(String warning){
		return warning.trim().contains(warning.toLowerCase());
	}
	
	public String getDescription(){
		return description;
	}

	@Override
	public String toString() {
		return (strict ? "Strict Mode on" : "") + "\n" +
			   (supressWarnings.isEmpty() ? "" : "Suppressed Warnings: " + supressWarnings.toString() + "\n") +
			   (description == null ? "" : "File description: " + description + "\n");
				
	}		
	
}
