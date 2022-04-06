package com.laytonsmith.core.functions;

import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Security;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.analysis.Scope;
import com.laytonsmith.core.compiler.analysis.StaticAnalysis;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.environments.StaticRuntimeEnv;
import com.laytonsmith.core.exceptions.CRE.CREIOException;
import com.laytonsmith.core.exceptions.CRE.CREIncludeException;
import com.laytonsmith.core.exceptions.CRE.CRESecurityException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.ProgramFlowManipulationException;
import com.laytonsmith.core.profiler.ProfilePoint;
import com.laytonsmith.core.profiler.Profiler;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The IncludeCache is used for the include function and auto includes to store and retrieve related cached objects.
 */
public class IncludeCache {

	private static final MSLog.Tags TAG = MSLog.Tags.INCLUDES;
	private final List<File> autoIncludes = new ArrayList<>();
	private final Map<File, ParseTree> cache = new HashMap<>();
	private final Map<File, StaticAnalysis> analysisCache = new HashMap<>();
	private final Map<Target, Scope> dynamicAnalysisParentScopeCache = new HashMap<>();

	/**
	 * Adds a single file and its associated compiled {@link ParseTree} to this cache. To ensure only one ParseTree is
	 * stored per file, use a canonical file path.
	 *
	 * @param file The {@link File} that was compiled
	 * @param tree The {@link ParseTree} to be cached
	 */
	public void add(File file, ParseTree tree) {
		this.cache.put(file, tree);
	}

	/**
	 * Adds multiple compiled files to this cache.
	 *
	 * @param files The map of compiled files to be added
	 */
	public void addAll(Map<File, ParseTree> files) {
		this.cache.putAll(files);
	}

	/**
	 * Checks if this cache contains the compiled {@link ParseTree} for a file. A canonical file path should be used.
	 *
	 * @param file The {@link File} to check
	 * @return {@code true} if the cache contains this file
	 */
	public boolean has(File file) {
		return this.cache.containsKey(file);
	}

	/**
	 * Returns the number of {@link ParseTree} entries stored in this cache.
	 *
	 * @return The number of {@link ParseTree} entries stored in this cache
	 */
	public int size() {
		return this.cache.size();
	}

	/**
	 * Equivalent to calling {@code get(file, env, envs, new StaticAnalysis(false), t, null)}.
	 * @see IncludeCache#get(File, Environment, Set, StaticAnalysis, Target, Set)
	 */
	public static ParseTree get(File file, com.laytonsmith.core.environments.Environment env,
			Set<Class<? extends Environment.EnvironmentImpl>> envs, Target t) {
		return get(file, env, envs, new StaticAnalysis(false), t, null);
	}

	/**
	 * Equivalent to calling {@code get(file, env, envs, staticAnalysis, t, null)}.
	 * @see IncludeCache#get(File, Environment, Set, StaticAnalysis, Target, Set)
	 */
	public static ParseTree get(File file, com.laytonsmith.core.environments.Environment env,
			Set<Class<? extends Environment.EnvironmentImpl>> envs, StaticAnalysis staticAnalysis, Target t) {
		return get(file, env, envs, staticAnalysis, t, null);
	}

	/**
	 * Gets a compiled {@link ParseTree} for a {@link File}.
	 * Returns a cached {@link ParseTree} if previously compiled in this {@link Environment},
	 * otherwise compiles the file and stores the result in the environment's {@link IncludeCache}.
	 * @param file - The {@link File} to get the cached {@link ParseTree}
	 * @param env - The {@link com.laytonsmith.core.environments.Environment} in which get or store the {@link ParseTree}
	 * @param envs - The environment types used when compiling
	 * @param staticAnalysis - The {@link StaticAnalysis} used for compilation and cached for this file
	 * @param t - The code {@link Target} where this method is called
	 * @param exceptions - The set to add compile exceptions to, in case the file is not cached and cannot be compiled.
	 * If {@code null} is supplied, then {@link ConfigRuntimeException}s are generated and thrown instead.
	 * @return The {@link ParseTree} for the given {@link File},
	 * or {@code null} if exceptions is non-null and compilation failed.
	 * @throws CRESecurityException When exceptions is {@code null}, not in cmd-line mode, and the file path
	 * is not within the base-dir of {@link com.laytonsmith.core.Prefs}.
	 * @throws CREIOException When exceptions is {@code null}, and the file does not exist or cannot be read.
	 */
	public static ParseTree get(File file, com.laytonsmith.core.environments.Environment env,
			Set<Class<? extends Environment.EnvironmentImpl>> envs,
			StaticAnalysis staticAnalysis, Target t, Set<ConfigCompileException> exceptions) {
		MSLog.GetLogger().Log(TAG, LogLevel.DEBUG, "Loading " + file, t);
		IncludeCache includeCache = env.getEnv(StaticRuntimeEnv.class).getIncludeCache();
		if(includeCache.cache.containsKey(file)) {
			MSLog.GetLogger().Log(TAG, LogLevel.INFO, "Returning " + file + " from cache", t);
			return includeCache.cache.get(file);
		}
		MSLog.GetLogger().Log(TAG, LogLevel.VERBOSE, "Cache does not already contain file. Compiling and caching.", t);
		Profiler profiler = env.getEnv(StaticRuntimeEnv.class).GetProfiler();
		try {
			if(!Static.InCmdLine(env, true) && !Security.CheckSecurity(file)) {
				if(exceptions != null) {
					exceptions.add(new ConfigCompileException("The script cannot access " + file
							+ " due to restrictions imposed by the base-dir setting.", t));
					return null;
				} else {
					throw new CRESecurityException("The script cannot access " + file
							+ " due to restrictions imposed by the base-dir setting.", t);
				}
			}
			MSLog.GetLogger().Log(TAG, LogLevel.VERBOSE, "Security check passed", t);
			String s = env.getEnv(GlobalEnv.class).GetScriptProvider().getScript(file);
			ProfilePoint p = profiler.start("Compiling " + file, LogLevel.WARNING);
			ParseTree tree;
			try {
				tree = MethodScriptCompiler.compile(
						MethodScriptCompiler.lex(s, env, file, true), env, envs, staticAnalysis);
			} finally {
				p.stop();
			}
			MSLog.GetLogger().Log(TAG, LogLevel.VERBOSE, "Compilation succeeded, adding to cache.", t);
			includeCache.cache.put(file, tree);
			includeCache.analysisCache.put(file, staticAnalysis);
			return tree;
		} catch (ConfigCompileException ex) {
			if(exceptions != null) {
				exceptions.add(ex);
				return null;
			} else {
				String fileName = (ex.getFile() == null ? "Unknown Source" : ex.getFile().getName());
				throw new CREIncludeException("There was a compile error when trying to include the script at " + file
						+ "\n" + ex.getMessage() + " :: " + fileName + ":" + ex.getLineNum(), t);
			}
		} catch (ConfigCompileGroupException exs) {
			if(exceptions != null) {
				exceptions.addAll(exs.getList());
				return null;
			} else {
				StringBuilder b = new StringBuilder();
				b.append("There were compile errors when trying to include the script at ").append(file).append("\n");
				for(ConfigCompileException ex : exs.getList()) {
					String fileName = (ex.getFile() == null ? "Unknown Source" : ex.getFile().getName());
					b.append(ex.getMessage()).append(" :: ").append(fileName).append(":")
							.append(ex.getLineNum()).append("\n");
				}
				throw new CREIncludeException(b.toString(), t);
			}
		} catch (IOException ex) {
			if(exceptions != null) {
				exceptions.add(new ConfigCompileException(
						"The script at " + file + " could not be found or read in.", t, ex));
				return null;
			} else {
				throw new CREIOException("The script at " + file + " could not be found or read in.", t, ex);
			}
		}
	}

	/**
	 * Gets the {@link StaticAnalysis} object for a file in this cache.
	 *
	 * @param file The file for which the {@link StaticAnalysis} is stored
	 * @return StaticAnalysis object for this file
	 */
	public StaticAnalysis getStaticAnalysis(File file) {
		return this.analysisCache.get(file);
	}

	/**
	 * Gets a map of parent scopes for dynamic include function code targets.
	 *
	 * @return
	 */
	public Map<Target, Scope> getDynamicAnalysisParentScopeCache() {
		return this.dynamicAnalysisParentScopeCache;
	}

	/**
	 * Adds a list of auto_include.ms files this object. These are executed when {@link #registerAutoIncludes} is
	 * called.
	 *
	 * @param autoIncludes The list of auto_include.ms files to be added
	 */
	public void addAutoIncludes(List<File> autoIncludes) {
		this.autoIncludes.addAll(autoIncludes);
	}

	/**
	 * Compiles and executes all the auto_include.ms files added to this object. This will use a cached
	 * {@link ParseTree} for each {@link File} when available. This effectively stores all the defined procedures within
	 * the given {@link Environment}.
	 *
	 * @param env The {@link Environment} to execute with
	 * @param s The {@link Script} to execute with (can be null)
	 */
	public void executeAutoIncludes(Environment env, Script s) {
		for(File f : this.autoIncludes) {
			try {
				MethodScriptCompiler.execute(
						IncludeCache.get(f, env, env.getEnvClasses(), new Target(0, f, 0)), env, null, s);
			} catch(ProgramFlowManipulationException e) {
				ConfigRuntimeException.HandleUncaughtException(ConfigRuntimeException.CreateUncatchableException(
						"Cannot break program flow in auto include files.", e.getTarget()), env);
			} catch(ConfigRuntimeException e) {
				e.setEnv(env);
				ConfigRuntimeException.HandleUncaughtException(e, env);
			}
		}
	}
}
