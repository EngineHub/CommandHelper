package com.laytonsmith.core.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class FileOptions {

	private boolean strict;
	private List<String> supressWarnings;
	private String name;
	private String author;
	private String created;
	private String description;
	//TODO: Make this non-public once this is all finished.
	public FileOptions(Map<String, String> parsedOptions) {
		strict = parseBoolean(getDefault(parsedOptions, "strict", "false"));
		supressWarnings = parseList(getDefault(parsedOptions, "supresswarnings", ""));
		name = getDefault(parsedOptions, "name", "");
		author = getDefault(parsedOptions, "author", "");
		created = getDefault(parsedOptions, "created", "");
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

	public String getName() {
	    return name;
	}

	public String getAuthor() {
	    return author;
	}

	public String getCreated() {
	    return created;
	}

	public String getDescription(){
		return description;
	}

	@Override
	public String toString() {
		return (strict ? "Strict Mode on" : "") + "\n" +
			   (supressWarnings.isEmpty() ? "" : "Suppressed Warnings: " + supressWarnings.toString() + "\n") +
			   (name.isEmpty() ? "" : "File name: " + name + "\n") +
			   (author.isEmpty() ? "" : "Author: " + author + "\n") +
			   (created.isEmpty() ? "" : "Creation Date: " + created + "\n") +
			   (description == null ? "" : "File description: " + description + "\n");

	}

}
