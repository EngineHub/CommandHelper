package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.ZipReader;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Security;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CREIOException;
import com.laytonsmith.core.exceptions.CRE.CREIncludeException;
import com.laytonsmith.core.exceptions.CRE.CRESecurityException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.profiler.ProfilePoint;
import com.laytonsmith.core.profiler.Profiler;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class IncludeCache {

	private static final MSLog.Tags TAG = MSLog.Tags.INCLUDES;
	private static final HashMap<File, ParseTree> CACHE = new HashMap<>();

	static void add(File file, ParseTree tree) {
		CACHE.put(file, tree);
	}

	static void addAll(HashMap<File, ParseTree> files) {
		CACHE.putAll(files);
	}

	static boolean has(File file) {
		return CACHE.containsKey(file);
	}

	public static ParseTree get(File file, com.laytonsmith.core.environments.Environment env, Target t) {
		MSLog.GetLogger().Log(TAG, LogLevel.DEBUG, "Loading " + file, t);
		if(CACHE.containsKey(file)) {
			MSLog.GetLogger().Log(TAG, LogLevel.INFO, "Returning " + file + " from cache", t);
			return CACHE.get(file);
		}
		MSLog.GetLogger().Log(TAG, LogLevel.VERBOSE, "Cache does not already contain file. Compiling and caching.", t);
		//We have to pull the file from the FS, and compile it.
		MSLog.GetLogger().Log(TAG, LogLevel.VERBOSE, "Security check passed", t);
		Profiler profiler = env.getEnv(GlobalEnv.class).GetProfiler();
		try {
			if(!Security.CheckSecurity(file)) {
				throw new CRESecurityException("The script cannot access " + file
						+ " due to restrictions imposed by the base-dir setting.", t);
			}
			String s = new ZipReader(file).getFileContents();
			ProfilePoint p = profiler.start("Compiling " + file, LogLevel.WARNING);
			ParseTree tree;
			try {
				tree = MethodScriptCompiler.compile(MethodScriptCompiler.lex(s, env, file, true), env, env.getEnvClasses());
			} finally {
				p.stop();
			}
			MSLog.GetLogger().Log(TAG, LogLevel.VERBOSE, "Compilation succeeded, adding to cache.", t);
			IncludeCache.add(file, tree);
			return tree;
		} catch (ConfigCompileException ex) {
			throw new CREIncludeException("There was a compile error when trying to include the script at " + file
					+ "\n" + ex.getMessage() + " :: " + file.getName() + ":" + ex.getLineNum(), t);
		} catch (ConfigCompileGroupException ex) {
			StringBuilder b = new StringBuilder();
			b.append("There were compile errors when trying to include the script at ").append(file).append("\n");
			for(ConfigCompileException e : ex.getList()) {
				b.append(e.getMessage()).append(" :: ").append(e.getFile().getName()).append(":")
						.append(e.getLineNum()).append("\n");
			}
			throw new CREIncludeException(b.toString(), t);
		} catch (IOException ex) {
			throw new CREIOException("The script at " + file + " could not be found or read in.", t, ex);
		}
	}

	public static void clearCache() {
		MSLog.GetLogger().Log(TAG, LogLevel.INFO, "Clearing include cache", Target.UNKNOWN);
		CACHE.clear();
	}
}
