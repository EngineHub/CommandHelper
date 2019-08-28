package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.ArgumentParser;
import com.laytonsmith.PureUtilities.ArgumentParser.ArgumentBuilder;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.enums.MCChatColor;
import com.laytonsmith.commandhelper.CommandHelperFileLocations;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.events.EventUtils;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.ProgramFlowManipulationException;
import com.laytonsmith.core.extensions.ExtensionManager;
import com.laytonsmith.core.functions.IncludeCache;
import com.laytonsmith.core.functions.Scheduling;
import com.laytonsmith.core.profiler.ProfilePoint;
import com.laytonsmith.core.profiler.Profiler;
import com.laytonsmith.core.taskmanager.TaskManagerImpl;
import com.laytonsmith.persistence.DataSourceFactory;
import com.laytonsmith.persistence.MemoryDataSource;
import com.laytonsmith.persistence.PersistenceNetworkImpl;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This class contains all the handling code. It only deals with built-in Java Objects, so that if the Minecraft API
 * Hook changes, porting the code will only require changing the API specific portions, not this core file.
 *
 *
 */
public class AliasCore {

	private final File aliasConfig;
	private final File auxAliases;
	private final File prefFile;
	private final File mainFile;
	private List<Script> scripts;
	private final Set<String> echoCommand = new HashSet<>();
	private CompilerEnvironment compilerEnv;
	public List<File> autoIncludes;
	public static CommandHelperPlugin parent;

	/**
	 * This constructor accepts the configuration settings for the plugin, and ensures that the manager uses these
	 * settings.
	 *
	 * @param aliasConfig
	 * @param auxAliases
	 * @param prefFile
	 * @param mainFile
	 * @param parent
	 */
	public AliasCore(File aliasConfig, File auxAliases, File prefFile, File mainFile, CommandHelperPlugin parent) {
		this.aliasConfig = aliasConfig;
		this.auxAliases = auxAliases;
		this.prefFile = prefFile;
		AliasCore.parent = parent;
		this.mainFile = mainFile;
	}

	public List<Script> getScripts() {
		return new ArrayList<>(scripts);
	}

	/**
	 * This takes a given command, then if the command maps to a defined alias, it will run the specified alias.
	 *
	 * @param command The full command string sent, including the forward slash
	 * @param sender The command sender
	 * @return True if the command was handled by an alias
	 */
	public boolean alias(String command, final MCCommandSender sender) {
		if(scripts == null) {
			throw ConfigRuntimeException.CreateUncatchableException("Cannot run alias commands."
					+ " No alias files are loaded.", Target.UNKNOWN);
		}

		if(sender instanceof MCPlayer && echoCommand.contains(sender.getName())) {
			// We are already running an alias, so exit to prevent infinite loops from macros.
			// This only needs to be checked for players due to use of chat output for macros.
			return false;
		}

		Script script = null;
		for(Script s : scripts) {
			if(s.match(command)) {
				script = s;
				break;
			}
		}
		if(script == null) {
			// No matching alias definition found
			return false;
		}

		if(Prefs.ConsoleLogCommands() && script.doLog()) {
			Static.getLogger().log(Level.INFO, "Running alias on " + sender.getName() + " ---> " + command);
		}

		GlobalEnv gEnv = new GlobalEnv(parent.executionQueue, parent.profiler, parent.persistenceNetwork,
				MethodScriptFileLocations.getDefault().getConfigDirectory(),
				parent.profiles, new TaskManagerImpl());
		CommandHelperEnvironment cEnv = new CommandHelperEnvironment();
		cEnv.SetCommandSender(sender);
		cEnv.SetCommand(command);
		Environment env = Environment.createEnvironment(gEnv, cEnv, compilerEnv);

		this.addPlayerReference(sender);
		ProfilePoint alias = gEnv.GetProfiler().start("Alias - \"" + command + "\"", LogLevel.ERROR);
		try {
			script.run(script.getVariables(command), env, output -> {
				// If this is a macro, we need to run the output as a command
				if(output == null) {
					return;
				}
				output = output.trim();
				if(!output.isEmpty() && output.startsWith("/")) {
					if(Prefs.DebugMode()) {
						Static.getLogger().log(Level.INFO, "Executing command on " + sender.getName() + ": " + output);
					}
					if(sender instanceof MCPlayer) {
						// Using chat method ensures that a PlayerCommandPreprocessEvent fires
						((MCPlayer) sender).chat(output);
					} else {
						Static.getServer().dispatchCommand(sender, output.substring(1));
					}
				}
			});
		} catch (ConfigRuntimeException ex) {
			ex.setEnv(env);
			ConfigRuntimeException.HandleUncaughtException(ex, env);
		} finally {
			alias.stop();
			this.removePlayerReference(sender);
		}
		return true;
	}

	/**
	 * Loads the global alias file in from the file system. If a player is running the command, send a reference to
	 * them, and they will see compile errors, otherwise, null.
	 *
	 * @param player
	 * @param settings The argument list for the settings.
	 * @param firstLoad Indicates that CH is loading
	 */
	public final void reload(MCPlayer player, String[] settings, boolean firstLoad) {
		ReloadOptions options;
		try {
			options = new ReloadOptions(settings);
		} catch (Exception ex) {
			if(player != null) {
				player.sendMessage(ex.getMessage());
			} else {
				Static.getLogger().log(Level.SEVERE, ex.getMessage());
			}
			return;
		}
		try {
			// TODO: Maybe consider adding a ReloadOption for this? Probably not though, that would cause
			// a huge headache.
			compilerEnv = new CompilerEnvironment();
			if(Prefs.AllowDynamicShell()) {
				MSLog.GetLogger().Log(MSLog.Tags.GENERAL, LogLevel.WARNING, "allow-dynamic-shell is set to true in "
						+ CommandHelperFileLocations.getDefault().getProfilerConfigFile().getName()
						+ " you should set this to false, except during development.", Target.UNKNOWN);
			}

			if(parent.profiler == null || options.reloadProfiler()) {
				parent.profiler = new Profiler(MethodScriptFileLocations.getDefault().getProfilerConfigFile());
			}

			ProfilePoint extensionPreReload = parent.profiler.start("Extension PreReloadAliases call", LogLevel.VERBOSE);
			try {
				// Allow new-style extensions know we are about to reload aliases.
				ExtensionManager.PreReloadAliases(options);
			} finally {
				extensionPreReload.stop();
			}

			ProfilePoint shutdownHooks = parent.profiler.start("Shutdown hooks call", LogLevel.VERBOSE);
			try {
				StaticLayer.GetConvertor().runShutdownHooks();
			} finally {
				shutdownHooks.stop();
			}

			if(!firstLoad && options.reloadExtensions()) {
				ProfilePoint extensionManagerShutdown = parent.profiler.start("Extension manager shutdown", LogLevel.VERBOSE);
				try {
					ExtensionManager.Shutdown();
				} finally {
					extensionManagerShutdown.stop();
				}
			}

			MSLog.initialize(MethodScriptFileLocations.getDefault().getConfigDirectory());

			//Clear out the data source cache
			DataSourceFactory.DisconnectAll();

			// PacketJumper.startup(); we're not using this yet
			if(options.reloadExtensions()) {
				ProfilePoint extensionManagerStartup = parent.profiler.start("Extension manager startup", LogLevel.VERBOSE);
				try {
					ExtensionManager.Startup();
				} finally {
					extensionManagerStartup.stop();
				}
			}
			MSLog.GetLogger().Log(MSLog.Tags.GENERAL, LogLevel.VERBOSE, "Scripts reloading...", Target.UNKNOWN);
			if(parent.persistenceNetwork == null || options.reloadPersistenceConfig()) {
				ProfilePoint persistenceConfigReload = parent.profiler.start("Reloading persistence configuration", LogLevel.VERBOSE);
				try {
					MemoryDataSource.ClearDatabases();
					ConnectionMixinFactory.ConnectionMixinOptions mixinOptions = new ConnectionMixinFactory.ConnectionMixinOptions();
					mixinOptions.setWorkingDirectory(MethodScriptFileLocations.getDefault().getConfigDirectory());
					parent.persistenceNetwork = new PersistenceNetworkImpl(MethodScriptFileLocations.getDefault().getPersistenceConfig(),
							new URI("sqlite:/" + MethodScriptFileLocations.getDefault().getDefaultPersistenceDBFile()
									.getCanonicalFile().toURI().getRawSchemeSpecificPart().replace('\\', '/')), mixinOptions);
				} finally {
					persistenceConfigReload.stop();
				}
			}
			try {
				parent.profiles = new ProfilesImpl(MethodScriptFileLocations.getDefault().getProfilesFile());
			} catch (IOException | Profiles.InvalidProfileException ex) {
				MSLog.GetLogger().e(MSLog.Tags.GENERAL, ex.getMessage(), Target.UNKNOWN);
				return;
			}
			GlobalEnv gEnv = new GlobalEnv(parent.executionQueue, parent.profiler, parent.persistenceNetwork,
					MethodScriptFileLocations.getDefault().getConfigDirectory(),
					parent.profiles, new TaskManagerImpl());
			gEnv.SetLabel(Static.GLOBAL_PERMISSION);
			if(options.reloadExecutionQueue()) {
				ProfilePoint stoppingExecutionQueue = parent.profiler.start("Stopping execution queues", LogLevel.VERBOSE);
				try {
					parent.executionQueue.stopAllNow();
				} finally {
					stoppingExecutionQueue.stop();
				}
			}
			CommandHelperEnvironment cEnv = new CommandHelperEnvironment();
			Environment env = Environment.createEnvironment(gEnv, cEnv, compilerEnv);
			if(options.reloadGlobals()) {
				ProfilePoint clearingGlobals = parent.profiler.start("Clearing globals", LogLevel.VERBOSE);
				try {
					Globals.clear();
				} finally {
					clearingGlobals.stop();
				}
			}
			if(options.reloadTimeouts()) {
				ProfilePoint clearingTimeouts = parent.profiler.start("Clearing timeouts/intervals", LogLevel.VERBOSE);
				try {
					Scheduling.ClearScheduledRunners();
				} finally {
					clearingTimeouts.stop();
				}
			}
			if(!aliasConfig.exists()) {
				aliasConfig.getParentFile().mkdirs();
				aliasConfig.createNewFile();
				try {
					String sampAliases = getStringResource(AliasCore.class.getResourceAsStream("/samp_aliases.txt"));
					//Because the sample config may have been written an a machine that isn't this type, replace all
					//line endings
					sampAliases = sampAliases.replaceAll("\n|\r\n", System.getProperty("line.separator"));
					file_put_contents(aliasConfig, sampAliases, "o");
				} catch (Exception e) {
					Static.getLogger().log(Level.WARNING, "Could not write sample config file");
				}
			}

			if(!mainFile.exists()) {
				mainFile.getParentFile().mkdirs();
				mainFile.createNewFile();
				try {
					String sampMain = getStringResource(AliasCore.class.getResourceAsStream("/samp_main.txt"));
					sampMain = sampMain.replaceAll("\n|\r\n", System.getProperty("line.separator"));
					file_put_contents(mainFile, sampMain, "o");
				} catch (Exception e) {
					Static.getLogger().log(Level.WARNING, "Could not write sample main file");
				}
			}

			if(!Prefs.isInitialized()) {
				Prefs.init(prefFile);
			}

			if(options.reloadScripts()) {
				ProfilePoint unregisteringEvents = parent.profiler.start("Unregistering events", LogLevel.VERBOSE);
				try {
					EventUtils.UnregisterAll();
				} finally {
					unregisteringEvents.stop();
				}
				ProfilePoint runningExtensionHooks = parent.profiler.start("Running event hooks", LogLevel.VERBOSE);
				try {
					ExtensionManager.RunHooks();
				} finally {
					runningExtensionHooks.stop();
				}

				//Clear the include cache, so it re-pulls files.
				IncludeCache.clearCache();

				// Close all channel messenger channels registered by CH.
				Static.getServer().getMessenger().closeAllChannels();

				scripts = new ArrayList<>();

				LocalPackage localPackages = new LocalPackage();

				//Run the main file once
				String main = file_get_contents(mainFile.getAbsolutePath());
				localPackages.appendMS(main, mainFile);

				String aliasConfigStr = file_get_contents(aliasConfig.getAbsolutePath()); //get the file again
				localPackages.appendMSA(aliasConfigStr, aliasConfig);

				File autoInclude = new File(env.getEnv(GlobalEnv.class).GetRootFolder(), "auto_include.ms");
				if(autoInclude.exists()) {
					localPackages.addAutoInclude(autoInclude);
				}

				//Now that we've included the default files, search the local_packages directory
				GetAuxAliases(auxAliases, localPackages);

				autoIncludes = localPackages.getAutoIncludes();

				ProfilePoint compilerMS = parent.profiler.start("Compilation of MS files in Local Packages", LogLevel.VERBOSE);
				try {
					env.getEnv(CommandHelperEnvironment.class).SetCommandSender(Static.getServer().getConsole());
					MethodScriptCompiler.registerAutoIncludes(env, null);
					localPackages.compileMS(player, env);
				} finally {
					env.getEnv(CommandHelperEnvironment.class).SetCommandSender(null);
					compilerMS.stop();
				}
				ProfilePoint compilerMSA = parent.profiler.start("Compilation of MSA files in Local Packages", LogLevel.VERBOSE);
				try {
					localPackages.compileMSA(scripts, player, env.getEnvClasses());
				} finally {
					compilerMSA.stop();
				}
			}
		} catch (IOException ex) {
			Static.getLogger().log(Level.SEVERE, "Path to config file is not correct/accessible."
					+ " Please check the location and try loading the plugin again.");
		} catch (Throwable t) {
			t.printStackTrace();
		}

		ProfilePoint postReloadAliases = parent.profiler.start("Extension manager post reload aliases", LogLevel.VERBOSE);
		try {
			ExtensionManager.PostReloadAliases();
		} finally {
			postReloadAliases.stop();
		}
	}

	/**
	 * Holder for recompile command options
	 */
	public class ReloadOptions {

		boolean globals;
		boolean timeouts;
		boolean executionQueue;
		boolean persistenceConfig;
		boolean profiler;
		boolean scripts;
		boolean extensions;

		private final ArgumentParser options = ArgumentParser.GetParser()
				.addArgument(new ArgumentBuilder().setDescription("Sets the list of arguments to be a whitelist, that is,"
						+ " only the specified modules get reloaded, the rest will be skipped. Without this option,"
						+ " the specified modules don't get reloaded.")
						.asFlag().setName("whitelist"))
				.addArgument(new ArgumentBuilder().setDescription("Specifies that globals memory (values stored with"
						+ " export/import) should be preserved.")
						.asFlag().setName('g', "globals"))
				.addArgument(new ArgumentBuilder().setDescription("Specifies that tasks registered with"
						+ " set_interval/set_timeout should be preserved.")
						.asFlag().setName('t', "tasks"))
				.addArgument(new ArgumentBuilder().setDescription("Specifies that tasks registered in execution queues"
						+ " should be preserved.")
						.asFlag().setName('e', "execution-queue"))
				.addArgument(new ArgumentBuilder().setDescription("Specifies that the persistence config file should"
						+ " not be reloaded.")
						.asFlag().setName('r', "persistence-config"))
				.addArgument(new ArgumentBuilder().setDescription("Specifies that the profiler config should not be"
						+ " reloaded.")
						.asFlag().setName('f', "profiler"))
				.addArgument(new ArgumentBuilder().setDescription("Specifies that scripts should not be reloaded.")
						.asFlag().setName('s', "scripts"))
				.addArgument(new ArgumentBuilder().setDescription("Specifies that extensions should not be reloaded.")
						.asFlag().setName('x', "extensions"))
				.addArgument(new ArgumentBuilder().setDescription("Prints this list and returns. Nothing is reloaded"
						+ " if this option is set.")
						.asFlag().setName('h', "help"));

		public ReloadOptions(String[] settings) throws ArgumentParser.ValidationException {
			globals = true;
			timeouts = true;
			executionQueue = true;
			persistenceConfig = true;
			profiler = true;
			scripts = true;
			extensions = true;

			if(settings != null) {
				ArgumentParser.ArgumentParserResults results;
				results = options.match(settings);
				if(results.isFlagSet('h')) {
					throw new CancelCommandException(options.getBuiltDescription(), Target.UNKNOWN);
				}
				if(results.isFlagSet("whitelist")) {
					//Invert the results
					globals = false;
					timeouts = false;
					executionQueue = false;
					persistenceConfig = false;
					profiler = false;
					scripts = false;
					extensions = false;
				}
				if(results.isFlagSet('g')) {
					globals = !globals;
				}
				if(results.isFlagSet('t')) {
					timeouts = !timeouts;
				}
				if(results.isFlagSet('e')) {
					executionQueue = !executionQueue;
				}
				if(results.isFlagSet('r') || results.isFlagSet("persistence-config")) {
					persistenceConfig = !persistenceConfig;
				}
				if(results.isFlagSet('f')) {
					profiler = !profiler;
				}
				if(results.isFlagSet('s')) {
					scripts = !scripts;
				}
				if(results.isFlagSet('x')) {
					extensions = !extensions;
				}
			}
		}

		public boolean reloadGlobals() {
			return globals;
		}

		public boolean reloadTimeouts() {
			return timeouts;
		}

		public boolean reloadExecutionQueue() {
			return executionQueue;
		}

		public boolean reloadPersistenceConfig() {
			return persistenceConfig;
		}

		public boolean reloadProfiler() {
			return profiler;
		}

		public boolean reloadScripts() {
			return scripts;
		}

		public boolean reloadExtensions() {
			return extensions;
		}
	}

	/**
	 * Returns the contents of a file as a string. Accepts the file location as a string.
	 *
	 * @param fileLocation
	 * @return the contents of the file as a string
	 * @throws Exception if the file cannot be found
	 */
	public static String file_get_contents(String fileLocation) throws IOException {
		StringBuilder ret = new StringBuilder();
		try(BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileLocation),
				Charset.forName("UTF-8")))) {
			String str;
			while((str = in.readLine()) != null) {
				ret.append(str).append('\n');
			}
		}
		return ret.toString();
	}

	/**
	 * This function writes the contents of a string to a file.
	 *
	 * @param fileLocation the location of the file on the disk
	 * @param contents the string to be written to the file
	 * @param mode the mode in which to write the file: <br /> <ul> <li>"o" - overwrite the file if it exists, without
	 * asking</li> <li>"a" - append to the file if it exists, without asking</li> <li>"c" - cancel the operation if the
	 * file exists, without asking</li> </ul>
	 * @return true if the file was written, false if it wasn't. Throws an exception if the file could not be created,
	 * or if the mode is not valid.
	 * @throws Exception if the file could not be created
	 */
	public static boolean file_put_contents(File fileLocation, String contents, String mode)
			throws Exception {
		BufferedWriter out = null;
		File f = fileLocation;
		if(f.exists()) {
			//do different things depending on our mode
			if(mode.equalsIgnoreCase("o")) {
				out = new BufferedWriter(new FileWriter(fileLocation));
			} else if(mode.equalsIgnoreCase("a")) {
				out = new BufferedWriter(new FileWriter(fileLocation, true));
			} else if(mode.equalsIgnoreCase("c")) {
				return false;
			} else {
				throw new RuntimeException("Undefined mode in file_put_contents: " + mode);
			}
		} else {
			out = new BufferedWriter(new FileWriter(fileLocation));
		}
		//At this point, we are assured that the file is open, and ready to be written in
		//from this point in the file.
		if(out != null) {
			out.write(contents);
			out.close();
			return true;
		} else {
			return false;
		}
	}

	public static String getStringResource(InputStream is) throws IOException {
		Writer writer = new StringWriter();
		char[] buffer = new char[1024];
		try {
			Reader reader = new BufferedReader(new InputStreamReader(is));
			int n;
			while((n = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}
		} finally {
			if(is != null) {
				is.close();
			}
		}
		return writer.toString();
	}

	public void removePlayerReference(MCCommandSender p) {
		//If they're not a player, oh well.
		if(p instanceof MCPlayer) {
			echoCommand.remove(p.getName());
		}
	}

	public void addPlayerReference(MCCommandSender p) {
		if(p instanceof MCPlayer) {
			echoCommand.add(p.getName());
		}
	}

	public boolean hasPlayerReference(MCCommandSender p) {
		if(p instanceof MCPlayer) {
			return echoCommand.contains(p.getName());
		} else {
			return false;
		}
	}

	public static class LocalPackage {

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
		private final List<FileInfo> msa = new ArrayList<>();

		public List<FileInfo> getMSFiles() {
			return new ArrayList<>(ms);
		}

		public List<FileInfo> getMSAFiles() {
			return new ArrayList<>(msa);
		}

		private List<File> getAutoIncludes() {
			return autoIncludes;
		}

		private void addAutoInclude(File f) {
			autoIncludes.add(f);
		}

		public void appendMSA(String s, File path) {
			msa.add(new FileInfo(s, path));
		}

		public void appendMS(String s, File path) {
			ms.add(new FileInfo(s, path));
		}

		public void compileMSA(List<Script> scripts, MCPlayer player,
				Set<Class<? extends Environment.EnvironmentImpl>> envs) {
			for(FileInfo fi : msa) {
				List<Script> tempScripts;
				try {
					ProfilePoint p = parent.profiler.start("Compiling " + fi.file, LogLevel.WARNING);
					try {
						tempScripts = MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(fi.contents,
								null, fi.file, false), envs);
					} finally {
						p.stop();
					}
					for(Script s : tempScripts) {
						try {
							try {
								s.compile();
								s.checkAmbiguous(scripts);
								scripts.add(s);
							} catch (ConfigCompileException e) {
								ConfigRuntimeException.HandleUncaughtException(e, "Compile error in script."
										+ " Compilation will attempt to continue, however.", player);
							} catch (ConfigCompileGroupException ex) {
								for(ConfigCompileException e : ex.getList()) {
									ConfigRuntimeException.HandleUncaughtException(e, "Compile error in script."
											+ " Compilation will attempt to continue, however.", player);
								}
							}
						} catch (RuntimeException ee) {
							throw new RuntimeException("While processing a script, "
									+ "(" + fi.file() + ") an unexpected exception occurred. (No further information"
									+ " is available, unfortunately.)", ee);
						}
					}
				} catch (ConfigCompileException e) {
					ConfigRuntimeException.HandleUncaughtException(e, "Could not compile file " + fi.file
							+ ", so compilation will halt.", player);
					return;
				}
			}
			int errors = 0;
			for(Script s : scripts) {
				if(s.compilerError) {
					errors++;
				}
			}
			if(errors > 0) {
				Static.getLogger().log(Level.INFO, TermColors.YELLOW
						+ (scripts.size() - errors) + " aliases defined, " + TermColors.RED + "with "
						+ errors + " aliases with compile errors." + TermColors.reset());
				if(player != null) {
					player.sendMessage(MCChatColor.YELLOW + "[CommandHelper] "
							+ (scripts.size() - errors) + " aliases defined, " + MCChatColor.RED + "with "
							+ errors + " aliases with compile errors.");
				}
			} else {
				Static.getLogger().log(Level.INFO, TermColors.YELLOW + scripts.size() + " aliases defined."
						+ TermColors.reset());
				if(player != null) {
					player.sendMessage(MCChatColor.YELLOW + "[CommandHelper] " + scripts.size() + " aliases defined.");
				}
			}
		}

		public void compileMS(MCPlayer player, Environment env) {
			for(FileInfo fi : ms) {
				boolean exception = false;
				try {
					MethodScriptCompiler.execute(MethodScriptCompiler.compile(
							MethodScriptCompiler.lex(fi.contents, env, fi.file, true), env, env.getEnvClasses()),
							env, null, null);
				} catch (ConfigCompileGroupException e) {
					exception = true;
					ConfigRuntimeException.HandleUncaughtException(e, fi.file.getAbsolutePath()
							+ " could not be compiled, due to compile errors.", player);
				} catch (ConfigCompileException e) {
					exception = true;
					ConfigRuntimeException.HandleUncaughtException(e, fi.file.getAbsolutePath()
							+ " could not be compiled, due to a compile error.", player);
				} catch (ConfigRuntimeException e) {
					exception = true;
					ConfigRuntimeException.HandleUncaughtException(e, env);
				} catch (CancelCommandException e) {
					if(e.getMessage() != null && !"".equals(e.getMessage().trim())) {
						Static.getLogger().log(Level.INFO, e.getMessage());
					}
				} catch (ProgramFlowManipulationException e) {
					exception = true;
					ConfigRuntimeException.HandleUncaughtException(ConfigRuntimeException.CreateUncatchableException(
							"Cannot break program flow in main files.", e.getTarget()), env);
				}
				if(exception) {
					if(Prefs.HaltOnFailure()) {
						Static.getLogger().log(Level.SEVERE, TermColors.RED
								+ "Compilation halted due to unrecoverable failure." + TermColors.reset());
						return;
					}
				}
			}
			Static.getLogger().log(Level.INFO, TermColors.YELLOW
					+ (ms.size() + autoIncludes.size()) + " MethodScript files processed" + TermColors.reset());
			if(player != null) {
				player.sendMessage(MCChatColor.YELLOW + "[CommandHelper] "
						+  (ms.size() + autoIncludes.size())  + " MethodScript files processed");
			}
		}
	}

	public static void GetAuxAliases(File start, LocalPackage pack) {
		if(start.isDirectory() && !start.getName().endsWith(".disabled") && !start.getName().endsWith(".library")) {
			for(File f : start.listFiles()) {
				GetAuxAliases(f, pack);
			}
		} else if(start.isFile()) {
			if(start.getName().endsWith(".msa")) {
				try {
					pack.appendMSA(file_get_contents(start.getAbsolutePath()), start);
				} catch (IOException ex) {
					Logger.getLogger(AliasCore.class.getName()).log(Level.SEVERE, null, ex);
				}
			} else if(start.getName().endsWith(".ms")) {
				if(start.getName().equals("auto_include.ms")) {
					pack.addAutoInclude(start);
				} else {
					try {
						pack.appendMS(file_get_contents(start.getAbsolutePath()), start);
					} catch (IOException ex) {
						Logger.getLogger(AliasCore.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			} else if(start.getName().endsWith(".mslp")) {
				try {
					GetAuxZipAliases(new ZipFile(start), pack);
				} catch (IOException ex) {
					Logger.getLogger(AliasCore.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}

	private static void GetAuxZipAliases(ZipFile file, LocalPackage pack) {
		ZipEntry ze;
		Enumeration<? extends ZipEntry> entries = file.entries();
		while(entries.hasMoreElements()) {
			ze = entries.nextElement();
			if(ze.getName().endsWith(".ms")) {
				if(ze.getName().equals("auto_include.ms")) {
					pack.addAutoInclude(new File(file.getName() + File.separator + ze.getName()));
				} else {
					try {
						pack.appendMS(Installer.parseISToString(file.getInputStream(ze)), new File(file.getName()
								+ File.separator + ze.getName()));
					} catch (IOException ex) {
						Logger.getLogger(AliasCore.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			} else if(ze.getName().endsWith(".msa")) {
				try {
					pack.appendMSA(Installer.parseISToString(file.getInputStream(ze)), new File(file.getName()
							+ File.separator + ze.getName()));
				} catch (IOException ex) {
					Logger.getLogger(AliasCore.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}
}
