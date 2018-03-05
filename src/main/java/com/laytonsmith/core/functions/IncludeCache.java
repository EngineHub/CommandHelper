package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.ZipReader;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Security;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CREIOException;
import com.laytonsmith.core.exceptions.CRE.CREIncludeException;
import com.laytonsmith.core.exceptions.CRE.CRESecurityException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class IncludeCache {

	private static final CHLog.Tags TAG = CHLog.Tags.INCLUDES;
	private static final HashMap<File, ParseTree> cache = new HashMap<>();

	static void add(File file, ParseTree tree) {
		cache.put(file, tree);
	}

	static void addAll(HashMap<File, ParseTree> files) {
		cache.putAll(files);
	}

	static boolean has(File file) {
		return cache.containsKey(file);
	}

	public static ParseTree get(File file, Target t) {
		CHLog.GetLogger().Log(TAG, LogLevel.DEBUG, "Loading " + file, t);
		if(cache.containsKey(file)) {
			CHLog.GetLogger().Log(TAG, LogLevel.INFO, "Returning " + file + " from cache", t);
			return cache.get(file);
		}
		CHLog.GetLogger().Log(TAG, LogLevel.VERBOSE, "Cache does not already contain file. Compiling and caching.", t);
		//We have to pull the file from the FS, and compile it.
		if(!Security.CheckSecurity(file)) {
			throw new CRESecurityException("The script cannot access " + file
					+ " due to restrictions imposed by the base-dir setting.", t);
		}
		CHLog.GetLogger().Log(TAG, LogLevel.VERBOSE, "Security check passed", t);
		try {
			String s = new ZipReader(file).getFileContents();
			ParseTree tree = MethodScriptCompiler.compile(MethodScriptCompiler.lex(s, file, true));
			CHLog.GetLogger().Log(TAG, LogLevel.VERBOSE, "Compilation succeeded, adding to cache.", t);
			IncludeCache.add(file, tree);
			return tree;
		} catch(ConfigCompileException ex) {
			throw new CREIncludeException("There was a compile error when trying to include the script at " + file
					+ "\n" + ex.getMessage() + " :: " + file.getName() + ":" + ex.getLineNum(), t);
		} catch(ConfigCompileGroupException ex) {
			StringBuilder b = new StringBuilder();
			b.append("There were compile errors when trying to include the script at ").append(file).append("\n");
			for(ConfigCompileException e : ex.getList()) {
				b.append(e.getMessage()).append(" :: ").append(e.getFile().getName()).append(":").append(e.getLineNum());
			}
			throw new CREIncludeException(b.toString(), t);
		} catch(IOException ex) {
			throw new CREIOException("The script at " + file + " could not be found or read in.", t);
		}
	}

	public static void clearCache() {
		CHLog.GetLogger().Log(TAG, LogLevel.INFO, "Clearing include cache", Target.UNKNOWN);
		cache.clear();
	}
}
