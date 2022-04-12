package com.laytonsmith.core;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.analysis.StaticAnalysis;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.StaticRuntimeEnv;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.ProgramFlowManipulationException;
import com.laytonsmith.core.functions.IncludeCache;
import com.laytonsmith.core.profiler.ProfilePoint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class LocalPackages {

	public static final class FileInfo {

		String contents;
		File file;

		private FileInfo(String contents, File file) {
			this.contents = contents;
			this.file = file;
		}

		public String contents() {
			return contents;
		}

		public File file() {
			return file;
		}
	}

	private final List<File> autoIncludes = new ArrayList<>();
	private final List<FileInfo> ms = new ArrayList<>();
	private final List<ParseTree> msCompiled = new ArrayList<>();
	private final List<FileInfo> msa = new ArrayList<>();
	private boolean compileErrors = false;

	boolean hasCompileErrors() {
		return compileErrors;
	}

	public List<FileInfo> getMSFiles() {
		return new ArrayList<>(ms);
	}

	public List<FileInfo> getMSAFiles() {
		return new ArrayList<>(msa);
	}

	public int getMSFileCount() {
		return ms.size();
	}

	public int getMSAFileCount() {
		return msa.size();
	}

	public List<File> getAutoIncludes() {
		return autoIncludes;
	}

	public void addAutoInclude(File f) {
		autoIncludes.add(f);
	}

	public void appendMSA(String s, File path) {
		msa.add(new FileInfo(s, path));
	}

	public void appendMS(String s, File path) {
		ms.add(new FileInfo(s, path));
	}

	/**
	 * Compiles the added alias files and returns them as a list of {@link Script} objects.
	 *
	 * @param player The player to send compile error messages
	 * @param env The {@link Environment} to compile with
	 * @param envs The environment types to compile with
	 * @return List of compiled {@link Script} objects for each alias
	 */
	public List<Script> compileMSA(MCPlayer player, Environment env, Set<Class<? extends Environment.EnvironmentImpl>> envs) {
		List<Script> scripts = new ArrayList<>();
		for(FileInfo fi : msa) {
			List<Script> tempScripts;
			try {
				ProfilePoint p = env.getEnv(StaticRuntimeEnv.class).GetProfiler().start("Compiling " + fi.file,
						LogLevel.WARNING);
				try {
					tempScripts = MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(fi.contents,
							null, fi.file, false), envs);
				} finally {
					p.stop();
				}
				for(Script s : tempScripts) {
					try {
						try {
							s.compile(env.clone());
							s.checkAmbiguous(scripts);
							scripts.add(s);
						} catch (ConfigCompileException e) {
							compileErrors = true;
							ConfigRuntimeException.HandleUncaughtException(e, "Compile error in script."
									+ " Compilation will attempt to continue, however.", player, env);
						} catch (ConfigCompileGroupException ex) {
							compileErrors = true;
							for(ConfigCompileException e : ex.getList()) {
								ConfigRuntimeException.HandleUncaughtException(e, "Compile error in script."
										+ " Compilation will attempt to continue, however.", player, env);
							}
						} catch (CloneNotSupportedException e) {
							throw new Error("Environment wasn't clonable, while it should be.", e);
						}
					} catch (RuntimeException ee) {
						throw new RuntimeException("While processing a script, "
								+ "(" + fi.file() + ") an unexpected exception occurred. (No further information"
								+ " is available, unfortunately.)", ee);
					}
				}
			} catch (ConfigCompileException e) {
				compileErrors = true;
				ConfigRuntimeException.HandleUncaughtException(e, "Could not compile alias definition in "
						+ fi.file, player, env);
			}
		}
		return scripts;
	}

	/**
	 * Compiles the added MethodScript files.
	 * Should be called before {@link #executeMS(Environment)}.
	 *
	 * @param player The player used to send compile error messages
	 * @param env The {@link Environment} to compile with
	 */
	public void compileMS(MCPlayer player, Environment env) {
		// Compile auto includes before main ms files
		Set<ConfigCompileException> compileExceptions = new HashSet<>();
		if(StaticAnalysis.enabled()) {
			StaticAnalysis.setAndAnalyzeAutoIncludes(
					autoIncludes, env, env.getEnvClasses(), compileExceptions);
		} else {
			// If StaticAnalysis is off, we have to compile auto includes here.
			for(File f : autoIncludes) {
				IncludeCache.get(f, env, env.getEnvClasses(), new StaticAnalysis(true),
						new Target(0, f, 0), compileExceptions);
			}
		}
		if(!compileExceptions.isEmpty()) {
			compileErrors = true;
			for(ConfigCompileException ex : compileExceptions) {
				ConfigRuntimeException.HandleUncaughtException(ex, "Compile error in script.", player, env);
			}
		}

		// Compile main ms files after auto includes
		for(FileInfo fi : ms) {
			try {
				StaticAnalysis analysis = new StaticAnalysis(true);
				msCompiled.add(MethodScriptCompiler.compile(
						MethodScriptCompiler.lex(fi.contents, env, fi.file, true),
						env, env.getEnvClasses(), analysis));
			} catch (ConfigCompileGroupException e) {
				compileErrors = true;
				ConfigRuntimeException.HandleUncaughtException(e, fi.file.getAbsolutePath()
						+ " could not be compiled, due to compile errors.", player, env);
			} catch (ConfigCompileException e) {
				compileErrors = true;
				ConfigRuntimeException.HandleUncaughtException(e, fi.file.getAbsolutePath()
						+ " could not be compiled, due to a compile error.", player, env);
			} catch (ConfigRuntimeException e) {
				compileErrors = true;
				ConfigRuntimeException.HandleUncaughtException(e, env);
			}
		}
	}

	/**
	 * Executes the compiled MethodScript files.
	 * Should be called after {@link #compileMS}.
	 *
	 * @param env The {@link Environment} to execute with
	 */
	public void executeMS(Environment env) {
		@SuppressWarnings("deprecation") // Remove as soon as static analysis is enforced.
		boolean staticAnalysisEnabled = StaticAnalysis.enabled()
				|| (env.hasEnv(CompilerEnvironment.class)
				&& env.getEnv(CompilerEnvironment.class).getStaticAnalysis() != null
				&& env.getEnv(CompilerEnvironment.class).getStaticAnalysis().isLocalEnabled());
		for(ParseTree pt : msCompiled) {
			try {
				// Clone base environment for this ms file to potentially make changes to.
				Environment msFileEnv;
				if(staticAnalysisEnabled) {
					try {
						msFileEnv = env.clone();
					} catch (CloneNotSupportedException e) {
						throw new Error("Environment wasn't clonable, while it should be.", e);
					}
				} else {
					// Compatibility behavior to not silently change code behavior. Static analysis does catch code
					// that relies on this behavior, so when static analysis is enforced, this case should be removed.
					msFileEnv = env;
				}
				MethodScriptCompiler.execute(pt, msFileEnv, null, null);
			} catch (ConfigRuntimeException e) {
				ConfigRuntimeException.HandleUncaughtException(e, env);
			} catch (CancelCommandException e) {
				if(e.getMessage() != null && !"".equals(e.getMessage().trim())) {
					Static.getLogger().log(Level.INFO, e.getMessage());
				}
			} catch (ProgramFlowManipulationException e) {
				ConfigRuntimeException.HandleUncaughtException(ConfigRuntimeException.CreateUncatchableException(
						"Cannot break program flow in main files.", e.getTarget()), env);
			}
		}
	}

	/**
	 * Recursively traverses a directory and adds MethodScript files to this LocalPackages object.
	 * Should be called before compiling and executing.
	 *
	 * @param start The directory to start recursively traversing
	 */
	public void search(File start) {
		if(start.isDirectory() && !start.getName().endsWith(".disabled") && !start.getName().endsWith(".library")) {
			for(File f : start.listFiles()) {
				search(f);
			}
		} else if(start.isFile()) {
			if(start.getName().endsWith(".msa")) {
				try {
					appendMSA(AliasCore.file_get_contents(start.getAbsolutePath()), start);
				} catch (IOException ex) {
					Logger.getLogger(AliasCore.class.getName()).log(Level.SEVERE, null, ex);
				}
			} else if(start.getName().endsWith(".ms")) {
				if(start.getName().equals("auto_include.ms")) {
					addAutoInclude(start);
				} else {
					try {
						appendMS(AliasCore.file_get_contents(start.getAbsolutePath()), start);
					} catch (IOException ex) {
						Logger.getLogger(AliasCore.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			} else if(start.getName().endsWith(".mslp")) {
				try {
					searchZip(new ZipFile(start));
				} catch (IOException ex) {
					Logger.getLogger(AliasCore.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}

	private void searchZip(ZipFile file) {
		ZipEntry ze;
		Enumeration<? extends ZipEntry> entries = file.entries();
		while(entries.hasMoreElements()) {
			ze = entries.nextElement();
			if(ze.getName().endsWith(".ms")) {
				if(ze.getName().equals("auto_include.ms")) {
					addAutoInclude(new File(file.getName() + File.separator + ze.getName()));
				} else {
					try {
						appendMS(Installer.parseISToString(file.getInputStream(ze)), new File(file.getName()
								+ File.separator + ze.getName()));
					} catch (IOException ex) {
						Logger.getLogger(AliasCore.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			} else if(ze.getName().endsWith(".msa")) {
				try {
					appendMSA(Installer.parseISToString(file.getInputStream(ze)), new File(file.getName()
							+ File.separator + ze.getName()));
				} catch (IOException ex) {
					Logger.getLogger(AliasCore.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}
}
