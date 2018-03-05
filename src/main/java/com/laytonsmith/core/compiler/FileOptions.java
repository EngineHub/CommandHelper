package com.laytonsmith.core.compiler;

import com.laytonsmith.core.Prefs;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class FileOptions {

	/*
     These values are used in the syntax highlighter, and should remain the name they are in code.
	 */
	private final Boolean strict;
	private final List<String> suppressWarnings;
	private final String name;
	private final String author;
	private final String created;
	private final String description;
	//TODO: Make this non-public once this is all finished.

	public FileOptions(Map<String, String> parsedOptions) {
		strict = parseBoolean(getDefault(parsedOptions, "strict", null));
		suppressWarnings = parseList(getDefault(parsedOptions, "suppresswarnings", ""));
		name = getDefault(parsedOptions, "name", "");
		author = getDefault(parsedOptions, "author", "");
		created = getDefault(parsedOptions, "created", "");
		description = getDefault(parsedOptions, "description", null);
	}

	private String getDefault(Map<String, String> map, String key, String defaultIfNone) {
		if(map.containsKey(key)) {
			return map.get(key);
		} else {
			return defaultIfNone;
		}
	}

	private Boolean parseBoolean(String bool) {
		if(bool == null) {
			return null;
		}
		return !(bool.equalsIgnoreCase("false") || bool.equalsIgnoreCase("off"));
	}

	private List<String> parseList(String list) {
		List<String> l = new ArrayList<>();
		for(String part : list.split(",")) {
			if(!part.trim().isEmpty()) {
				l.add(part.trim().toLowerCase());
			}
		}
		return l;
	}

	/**
	 * Returns whether or not this file is in strict mode. Unlike most options, this one depends on both the file
	 * options and the config value. In the config, if strict mode is turned on or off, this value only serves as the
	 * default. File options will override the global setting.
	 *
	 * @return
	 */
	public boolean isStrict() {
		if(strict != null) {
			return strict;
		} else {
			return Prefs.StrictMode();
		}
	}

	public boolean isWarningSuppressed(String warning) {
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

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return (strict ? "Strict Mode on" : "") + "\n"
				+ (suppressWarnings.isEmpty() ? "" : "Suppressed Warnings: " + suppressWarnings.toString() + "\n")
				+ (name.isEmpty() ? "" : "File name: " + name + "\n")
				+ (author.isEmpty() ? "" : "Author: " + author + "\n")
				+ (created.isEmpty() ? "" : "Creation Date: " + created + "\n")
				+ (description == null ? "" : "File description: " + description + "\n");

	}

}
