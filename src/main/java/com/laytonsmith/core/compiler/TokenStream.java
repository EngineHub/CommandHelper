package com.laytonsmith.core.compiler;

import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Token;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Layton
 */
public class TokenStream extends ArrayList<Token> {

	FileOptions fileOptions;

	public TokenStream(List<Token> list, FileOptions options) {
		super(list);
		this.fileOptions = options;
	}

	public TokenStream(List<Token> list, String fileOptions, Target fileOptionsTarget) throws ConfigCompileException {
		super(list);
		this.fileOptions = parseFileOptions(fileOptions, fileOptionsTarget);
	}

	public FileOptions getFileOptions() {
		return fileOptions;
	}

	private static FileOptions parseFileOptions(String options, Target fileOptionsTarget) throws ConfigCompileException {
		//Only ; needs escaping. Everything else is just trimmed, and added to the map.
		Map<FileOptions.Directive, String> map = new EnumMap<FileOptions.Directive, String>(FileOptions.Directive.class);
		boolean inKey = true;
		StringBuilder buffer = new StringBuilder();
		String keyName = "";
		for (int i = 0; i < options.length(); i++) {
			Character c = options.charAt(i);
			Character c2 = null;
			if (i < options.length() - 1) {
				c2 = options.charAt(i + 1);
			}
			if (inKey) {
				if (c == ':') {
					keyName = buffer.toString();
					buffer = new StringBuilder();
					inKey = false;
				} else if (c == ';') {
					//Self closed
					try {
						map.put(FileOptions.Directive.valueOf(buffer.toString().trim()), "true");
					} catch (IllegalArgumentException e) {
						throw new ConfigCompileException("Unknown file option directive: " + buffer.toString().trim(), fileOptionsTarget);
					}
					buffer = new StringBuilder();
					keyName = "";
					//We don't reset the inKey parameter
				} else {
					buffer.append(c);
				}
			} else {
				if (c == '\\' && c2 == ';') {
					buffer.append(';');
					i++;
				} else if (c == ';') {
					//We're done
					inKey = true;
					try {
						map.put(FileOptions.Directive.valueOf(keyName.trim()), buffer.toString());
					} catch (IllegalArgumentException e) {
						throw new ConfigCompileException("Unknown file option directive: " + buffer.toString().trim(), fileOptionsTarget);
					}
					buffer = new StringBuilder();
				} else {
					buffer.append(c);
				}
			}
		}
		if (buffer.length() > 0) {
			if (!inKey) {
				try {
					map.put(FileOptions.Directive.valueOf(keyName.trim()), buffer.toString());
				} catch (IllegalArgumentException e) {
					throw new ConfigCompileException("Unknown file option directive: " + keyName.toString().trim(), fileOptionsTarget);
				}
			} else {
				if (!buffer.toString().trim().isEmpty()) {
					try {
						map.put(FileOptions.Directive.valueOf(buffer.toString().trim()), "true");
					} catch (IllegalArgumentException e) {
						throw new ConfigCompileException("Unknown file option directive: " + buffer.toString().trim(), fileOptionsTarget);
					}
				}
			}
		}
		FileOptions fo = new FileOptions(map, fileOptionsTarget);
		return fo;
	}
}
