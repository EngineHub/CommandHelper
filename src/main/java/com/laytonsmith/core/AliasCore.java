package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.ArgumentParser;
import com.laytonsmith.PureUtilities.ArgumentParser.ArgumentBuilder;
import com.laytonsmith.PureUtilities.ExecutionQueue;
import com.laytonsmith.PureUtilities.ExecutionQueueImpl;
import com.laytonsmith.PureUtilities.SmartComment;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCCommand;
import com.laytonsmith.abstraction.MCCommandMap;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.enums.MCChatColor;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.analysis.StaticAnalysis;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CNativeClosure;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Command;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.NativeTypeList;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Variable;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.environments.RuntimeMode;
import com.laytonsmith.core.environments.StaticRuntimeEnv;
import com.laytonsmith.core.events.EventUtils;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.extensions.ExtensionManager;
import com.laytonsmith.core.functions.Commands;
import com.laytonsmith.core.functions.IncludeCache;
import com.laytonsmith.core.functions.Scheduling;
import com.laytonsmith.core.natives.interfaces.MEnumType;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.profiler.ProfilePoint;
import com.laytonsmith.core.profiler.Profiler;
import com.laytonsmith.core.taskmanager.TaskManagerImpl;
import com.laytonsmith.persistence.DataSourceException;
import com.laytonsmith.persistence.DataSourceFactory;
import com.laytonsmith.persistence.PersistenceNetwork;
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
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * This class manages aliases as well as the reloading of core configurations, scripts, extensions,
 * and other managers within the installation directory.
 */
public class AliasCore {

	private final MethodScriptFileLocations fileLocations;
	private final Set<String> echoCommand = new HashSet<>();
	private CompilerEnvironment compilerEnv;
	private StaticRuntimeEnv staticRuntimeEnv;
	private List<Script> scripts;

	/**
	 * This constructor accepts the constant file locations object for MethodScript.
	 *
	 * @param fileLocations The {@link MethodScriptFileLocations} for this manager
	 */
	public AliasCore(MethodScriptFileLocations fileLocations) {
		this.fileLocations = fileLocations;
		if(Prefs.CheckForUpdates()) {
			new Thread(() -> {
				try {
					Thread.sleep(15000);
				} catch (InterruptedException ex) {
					//
				}
				Boolean updateAvailable = Updater.isUpdateAvailable();
				if(updateAvailable != null && updateAvailable) {
					MSLog.GetLogger().always(MSLog.Tags.GENERAL, "An update is available!", Target.UNKNOWN);
				}
			}, Implementation.GetServerType().getBranding() + "-update-check").start();
		}
	}

	/**
	 * Gets a list of currently defined alias scripts.
	 * Can be null if reload has not been successfully called.
	 *
	 * @return List of alias scripts
	 */
	public List<Script> getScripts() {
		return scripts;
	}

	/**
	 * Gets the current valid core StaticRuntimeEnv.
	 * Can be used to generate a new environment with globally shared managers.
	 * Can be null if reload has not been successfully called.
	 *
	 * @return Shared StaticRuntimeEnv
	 */
	public StaticRuntimeEnv getStaticRuntimeEnv() {
		return staticRuntimeEnv;
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

		GlobalEnv gEnv = new GlobalEnv(fileLocations.getConfigDirectory(), EnumSet.of(RuntimeMode.EMBEDDED));
		CommandHelperEnvironment cEnv = new CommandHelperEnvironment();
		cEnv.SetCommandSender(sender);
		cEnv.SetCommand(command);
		Environment env = Environment.createEnvironment(gEnv, staticRuntimeEnv, cEnv, compilerEnv);

		this.addPlayerReference(sender);
		ProfilePoint alias = env.getEnv(StaticRuntimeEnv.class).GetProfiler().start("Alias - \"" + command + "\"",
				LogLevel.ERROR);
		try {
			script.run(script.getVariables(command), env, output -> {
				// If this is a macro, we need to run the output as a command
				if(output == null) {
					return;
				}
				output = output.trim();
				if(output.startsWith("/")) {
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
	 * Loads or reloads configuration files, scripts, extensions, and managers.
	 * Takes optional settings arguments that controls what to reload. (everything by default)
	 * Configuration files like preferences and profiles are always reloaded first.
	 * If scripts are reloaded and there's a compile error, the reload process will halt
	 * and existing tasks, aliases, event binds, and queues will continue.
	 * However, when firstLoad is true and halt-on-failure is true in preferences.ini,
	 * the server will be shutdown immediately.
	 *
	 * @param player The player to send messages to (can be null)
	 * @param settings The argument list for the {@link ReloadOptions} (can be null)
	 * @param firstLoad {@code true} if CommandHelper is loading
	 */
	public final void reload(MCPlayer player, String[] settings, final boolean firstLoad) {
		final ReloadOptions options;
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

		// Prefs is expected to be loaded before reload is called on startup, as it's needed earlier.
		if(!firstLoad) {
			try {
				Prefs.init(fileLocations.getPreferencesFile());
			} catch (IOException ex) {
				Static.getLogger().log(Level.SEVERE, "Failed to reload preferences.", ex);
				return;
			}
		} else {
			if(Prefs.AllowDynamicShell()) {
				MSLog.GetLogger().Log(MSLog.Tags.GENERAL, LogLevel.WARNING, "allow-dynamic-shell is set to true in "
						+ fileLocations.getProfilerConfigFile().getName()
						+ " you should set this to false, except during development.", Target.UNKNOWN);
			}
		}

		MSLog.initialize(fileLocations.getConfigDirectory());

		Profiler profiler;
		if(options.reloadProfiler()) {
			try {
				profiler = new Profiler(fileLocations.getProfilerConfigFile());
			} catch (IOException ex) {
				Static.getLogger().log(Level.SEVERE, "Failed to create Profiler from config file.", ex);
				if(Prefs.HaltOnFailure() && firstLoad) {
					Static.getLogger().log(Level.SEVERE, "Shutting down server (halt-on-failure)");
					Static.getServer().shutdown();
				}
				return;
			}
		} else {
			// If not reloading profiler, keep the old one.
			profiler = staticRuntimeEnv.GetProfiler();
		}

		// Allow extensions know we are about to reload. Runs extension's onPreReloadAliases().
		ProfilePoint extensionPreReload = profiler.start("Extension PreReloadAliases call", LogLevel.VERBOSE);
		try {
			ExtensionManager.PreReloadAliases(options);
		} finally {
			extensionPreReload.stop();
		}

		Profiles profiles;
		try {
			profiles = new ProfilesImpl(fileLocations.getProfilesFile());
		} catch (IOException | Profiles.InvalidProfileException ex) {
			MSLog.GetLogger().e(MSLog.Tags.GENERAL, ex.getMessage(), Target.UNKNOWN);
			if(Prefs.HaltOnFailure() && firstLoad) {
				Static.getLogger().log(Level.SEVERE, "Shutting down server (halt-on-failure)");
				Static.getServer().shutdown();
			}
			return;
		}

		PersistenceNetwork persistenceNetwork;
		if(options.reloadPersistenceConfig()) {
			ProfilePoint persistenceConfigReload = profiler.start("Reloading persistence configuration", LogLevel.VERBOSE);
			try {
				DataSourceFactory.DisconnectAll();
				ConnectionMixinFactory.ConnectionMixinOptions opts = new ConnectionMixinFactory.ConnectionMixinOptions();
				opts.setWorkingDirectory(fileLocations.getConfigDirectory());
				persistenceNetwork = new PersistenceNetworkImpl(fileLocations.getPersistenceConfig(),
						new URI("sqlite://" + fileLocations.getDefaultPersistenceDBFile().getCanonicalFile().toURI()
								.getRawSchemeSpecificPart().replace('\\', '/')), opts);
			} catch (DataSourceException | URISyntaxException | IOException ex) {
				Static.getLogger().log(Level.SEVERE, null, ex);
				if(Prefs.HaltOnFailure() && firstLoad) {
					Static.getLogger().log(Level.SEVERE, "Shutting down server (halt-on-failure)");
					Static.getServer().shutdown();
				}
				return;
			} finally {
				persistenceConfigReload.stop();
			}
		} else {
			// If not reloading persistence config, keep the old one.
			persistenceNetwork = staticRuntimeEnv.GetPersistenceNetwork();
		}

		// If not reloading scripts, some objects should be kept from the previous StaticRuntimeEnv.
		// Objects generated from configurations should usually be recreated above (ReloadOptions permitting)
		IncludeCache includeCache;
		ExecutionQueue executionQueue;
		StaticAnalysis autoIncludeAnalysis;
		if(options.reloadScripts()) {
			includeCache = new IncludeCache();
			executionQueue = new ExecutionQueueImpl("MethodScriptExecutionQueue", "default");
			autoIncludeAnalysis = new StaticAnalysis(true);
		} else {
			includeCache = staticRuntimeEnv.getIncludeCache();
			executionQueue = staticRuntimeEnv.getExecutionQueue();
			autoIncludeAnalysis = staticRuntimeEnv.getAutoIncludeAnalysis();
		}

		StaticRuntimeEnv oldStaticRuntimeEnv = staticRuntimeEnv;
		StaticRuntimeEnv newStaticRuntimeEnv = new StaticRuntimeEnv(profiler, persistenceNetwork, profiles,
				new TaskManagerImpl(), executionQueue, includeCache, autoIncludeAnalysis);

		Environment env = null;
		LocalPackages localPackages = new LocalPackages();
		if(options.reloadScripts()) {
			// Create the new environment
			GlobalEnv globalEnv = new GlobalEnv(fileLocations.getConfigDirectory(), EnumSet.of(RuntimeMode.EMBEDDED));
			CommandHelperEnvironment commandHelperEnv = new CommandHelperEnvironment();
			CompilerEnvironment compilerEnv = new CompilerEnvironment();
			env = Environment.createEnvironment(globalEnv, newStaticRuntimeEnv, commandHelperEnv, compilerEnv);

			File aliasConfig = new File(fileLocations.getConfigDirectory(), Prefs.ScriptName());
			if(!aliasConfig.exists()) {
				try {
					aliasConfig.getParentFile().mkdirs();
					aliasConfig.createNewFile();
					String sampAliases = getStringResource(AliasCore.class.getResourceAsStream("/samp_aliases.txt"));
					//Because the sample config may have been written an a machine that isn't this type, replace all
					//line endings
					sampAliases = sampAliases.replaceAll("\n|\r\n", System.getProperty("line.separator"));
					file_put_contents(aliasConfig, sampAliases, "o");
				} catch (Exception e) {
					Static.getLogger().log(Level.WARNING, "Could not write sample aliases file");
				}
			}

			File mainFile = new File(fileLocations.getConfigDirectory(), Prefs.MainFile());
			if(!mainFile.exists()) {
				try {
					mainFile.getParentFile().mkdirs();
					mainFile.createNewFile();
					String sampMain = getStringResource(AliasCore.class.getResourceAsStream("/samp_main.txt"));
					sampMain = sampMain.replaceAll("\n|\r\n", System.getProperty("line.separator"));
					file_put_contents(mainFile, sampMain, "o");
				} catch (Exception e) {
					Static.getLogger().log(Level.WARNING, "Could not write sample main file");
				}
			}

			// Get the default files (main.ms, aliases.msa, auto_include.ms)
			try {
				String main = file_get_contents(mainFile.getAbsolutePath());
				localPackages.appendMS(main, mainFile);
			} catch (IOException e) {
				Static.getLogger().log(Level.WARNING, "Could not read main file");
			}

			try {
				String aliasConfigStr = file_get_contents(aliasConfig.getAbsolutePath());
				localPackages.appendMSA(aliasConfigStr, aliasConfig);
			} catch (IOException e) {
				Static.getLogger().log(Level.WARNING, "Could not read aliases file");
			}

			File autoInclude = new File(env.getEnv(GlobalEnv.class).GetRootFolder(), "auto_include.ms");
			if(autoInclude.exists()) {
				localPackages.addAutoInclude(autoInclude);
			}

			// Now that the default files are added, search the LocalPackages directory
			localPackages.search(fileLocations.getLocalPackagesDirectory());
			includeCache.addAutoIncludes(localPackages.getAutoIncludes());

			ProfilePoint compilerMS = profiler.start("Compilation of MS files in Local Packages", LogLevel.VERBOSE);
			try {
				localPackages.compileMS(player, env);
			} finally {
				compilerMS.stop();
			}

			ProfilePoint compilerMSA = profiler.start("Compilation of MSA files in Local Packages", LogLevel.VERBOSE);
			List<Script> newScripts;
			try {
				newScripts = localPackages.compileMSA(player, env, env.getEnvClasses());
				// Check for uniqueness among commands.
				for(int i = 0; i < newScripts.size(); i++) {
					Script s1 = newScripts.get(i);
					if(!s1.getSmartComment().getAnnotations("@command").isEmpty()) {
						for(int j = i + 1; j < newScripts.size(); j++) {
							Script s2 = newScripts.get(j);
							if(!s2.getSmartComment().getAnnotations("@command").isEmpty()) {
								if(s1.getCommandName().equalsIgnoreCase(s2.getCommandName())) {
									ConfigRuntimeException.HandleUncaughtException(
											new ConfigCompileException("Duplicate command defined. (First occurrence found at "
													+ s1.getTarget() + ")", s2.getTarget()), "Duplicate command.", player);
								}
							}
						}
					}
				}
			} finally {
				compilerMSA.stop();
			}

			if(localPackages.hasCompileErrors()) {
				Static.getLogger().log(Level.SEVERE, "Execution halted due to compile errors.");
				if(player != null) {
					player.sendMessage(MCChatColor.RED + "[CommandHelper] Execution halted due to compile errors.");
				}
				if(Prefs.HaltOnFailure() && firstLoad) {
					Static.getLogger().log(Level.SEVERE, "Shutting down server (halt-on-failure)");
					Static.getServer().shutdown();
				}
				return;
			}
			MSLog.GetLogger().Log(MSLog.Tags.GENERAL, LogLevel.VERBOSE, "Compilation completed", Target.UNKNOWN);

			// Now that script compilation was successful, reload things that don't have their own reload option.

			// Calls shutdown event and runs all shutdown hooks
			ProfilePoint shutdownHooks = profiler.start("Shutdown hooks call", LogLevel.VERBOSE);
			try {
				StaticLayer.GetConvertor().runShutdownHooks();
			} finally {
				shutdownHooks.stop();
			}

			// Clear all event binds after shutdown hooks
			ProfilePoint unregisteringEvents = profiler.start("Unregistering events", LogLevel.VERBOSE);
			try {
				EventUtils.UnregisterAll();
			} finally {
				unregisteringEvents.stop();
			}

			// Run hook() for all events after event unbinding
			ProfilePoint runningExtensionHooks = profiler.start("Running event hooks", LogLevel.VERBOSE);
			try {
				ExtensionManager.RunHooks();
			} finally {
				runningExtensionHooks.stop();
			}

			Static.getServer().getMessenger().closeAllChannels();

			// Set the scripts, CompilerEnvironment, and StaticRuntimeEnv to the new ones after shutdown hooks
			this.scripts = newScripts;
			this.compilerEnv = compilerEnv;
			this.staticRuntimeEnv = newStaticRuntimeEnv;
		} else {
			// We're proceeding, so use the new StaticRuntimeEnv even if we don't reload scripts
			this.staticRuntimeEnv = newStaticRuntimeEnv;
		}

		// Reload things here that have their own reload options, but don't need to run on startup.
		if(!firstLoad) {
			// Clear all queues
			if(options.reloadExecutionQueue() && oldStaticRuntimeEnv != null) {
				ProfilePoint stoppingExecutionQueue = profiler.start("Stopping execution queues", LogLevel.VERBOSE);
				try {
					oldStaticRuntimeEnv.getExecutionQueue().stopAllNow();
				} finally {
					stoppingExecutionQueue.stop();
				}
			}

			// Clear all delayed and repeating tasks
			if(options.reloadTimeouts()) {
				ProfilePoint clearingTimeouts = profiler.start("Clearing timeouts/intervals", LogLevel.VERBOSE);
				try {
					Scheduling.ClearScheduledRunners();
				} finally {
					clearingTimeouts.stop();
				}
			}

			// Clear all import/export globals
			if(options.reloadGlobals()) {
				ProfilePoint clearingGlobals = profiler.start("Clearing globals", LogLevel.VERBOSE);
				try {
					Globals.clear();
				} finally {
					clearingGlobals.stop();
				}
			}

			// Run extension's onShutdown()
			if(options.reloadExtensions()) {
				ProfilePoint extManagerShutdown = profiler.start("Extension manager shutdown", LogLevel.VERBOSE);
				try {
					ExtensionManager.Shutdown();
				} finally {
					extManagerShutdown.stop();
				}
			}
		}

		// Reload things here that have their own reload option and also run on startup.
		if(options.reloadExtensions()) {
			ProfilePoint extensionManagerStartup = profiler.start("Extension manager startup", LogLevel.VERBOSE);
			try {
				ExtensionManager.Startup();
			} finally {
				extensionManagerStartup.stop();
			}
		}

		// Everything else should be reloaded now, so execute successfully compiled scripts and register commands
		if(options.reloadScripts() && env != null) {
			ProfilePoint executeAutoIncludes = profiler.start("Execution of auto includes", LogLevel.VERBOSE);
			try {
				includeCache.executeAutoIncludes(env, null);
			} finally {
				executeAutoIncludes.stop();
			}
			ProfilePoint executeMS = profiler.start("Execution of MS files in Local Packages", LogLevel.VERBOSE);
			try {
				env.getEnv(GlobalEnv.class).SetLabel(Static.GLOBAL_PERMISSION);
				env.getEnv(CommandHelperEnvironment.class).SetCommandSender(Static.getServer().getConsole());
				localPackages.executeMS(env);
			} finally {
				env.getEnv(CommandHelperEnvironment.class).SetCommandSender(null);
				executeMS.stop();
			}

			ProfilePoint registerCommands = profiler.start("Registering of annotated commands", LogLevel.VERBOSE);
			try {
				// Register commands, and set up tabcompleters and things
				for(Script s1 : scripts) {
					if(!s1.getSmartComment().getAnnotations("@command").isEmpty()) {
						registerCommand(s1, env);
					}
				}
			} finally {
				registerCommands.stop();
			}
		}

		// Allow extensions know we are done reloading. Runs extension's onPostReloadAliases().
		ProfilePoint postReloadAliases = profiler.start("Extension manager post reload aliases", LogLevel.VERBOSE);
		try {
			ExtensionManager.PostReloadAliases();
		} finally {
			postReloadAliases.stop();
		}

		String output;
		if(firstLoad) {
			output = "Load complete. ";
		} else {
			output = "Reload complete. ";
		}
		if(options.reloadScripts()) {
			int count = localPackages.getMSFileCount() + localPackages.getMSAFileCount() + includeCache.size();
			output += count + " files processed.";
		}
		Static.getLogger().log(Level.INFO, output);
		if(player != null) {
			player.sendMessage(MCChatColor.YELLOW + "[CommandHelper] " + output);
		}
	}

	private void registerCommand(Script script, Environment env) {
		// This is only called on scripts that are commands
		MCCommand cmd = StaticLayer.GetConvertor().getNewCommand(script.getCommandName().toLowerCase());
		SmartComment comment = script.getSmartComment();
		String description = comment.getBody();
		cmd.setDescription(description);
		String usage = script.getSignatureWithoutLabel();
		if(!comment.getAnnotations("@usage").isEmpty()) {
			if(comment.getAnnotations("@usage").size() > 1) {
				MSLog.GetLogger().w(MSLog.Tags.COMPILER, "Duplicate usage annotation found. Will only use the first.",
						script.getTarget());
			}
			usage = comment.getAnnotations("@usage").get(0);
		}
		cmd.setUsage(usage);
		if(!comment.getAnnotations("@permission").isEmpty()) {
			if(comment.getAnnotations("@permission").size() > 1) {
				MSLog.GetLogger().e(MSLog.Tags.COMPILER, "Duplicate permissions annotations, only one is allowed."
						+ " Only the first is being used, but this is almost certainly not what you want, check your"
						+ " code immediately.",
						script.getTarget());
			}
			cmd.setPermission(comment.getAnnotations("@permission").get(0));
		}
		if(!comment.getAnnotations("@noPermMsg").isEmpty()) {
			if(comment.getAnnotations("@noPermMsg").size() > 1) {
				MSLog.GetLogger().w(MSLog.Tags.COMPILER, "Duplicate noPermMsg annotation found. Will only use the first.",
						script.getTarget());
			}
			cmd.setPermissionMessage(comment.getAnnotations("@noPermMsg").get(0));
		}
		cmd.setAliases(comment.getAnnotations("@alias"));

		// Tab completer is more complicated
		if(!comment.getAnnotations("@tabcompleter").isEmpty()) {
			if(comment.getAnnotations("@tabcompleter").size() > 1) {
				MSLog.GetLogger().w(MSLog.Tags.COMPILER, "Duplicate tabcompleter annotation found. Will only use the first.",
						script.getTarget());
			}
			String proc = comment.getAnnotations("@tabcompleter").get(0).trim();
			if("".equals(proc)) {
				// Default implementation
				// TODO: Need to fake closure instantiation.
				MSLog.GetLogger().i(MSLog.Tags.COMPILER, "Missing proc name in @tabcompleter annotation.",
						script.getTarget());
			} else {
				Procedure p = env.getEnv(GlobalEnv.class).GetProcs().get(proc);
				Mixed m = p.execute(new ArrayList<>(), env, script.getTarget());
				if(!(m instanceof CClosure)) {
					MSLog.GetLogger().e(MSLog.Tags.COMPILER, "Procedure " + proc + " returns a value other than"
							+ " a closure. It must unconditionally return a closure.",
						p.getTarget());
				} else {
					Commands.set_tabcompleter.customExec(script.getTarget(), env, cmd, m);
				}
			}
		} else {
			// Set up the default one based on param values.
			Map<String, String> paramTypes = new HashMap<>();
			for(String param : comment.getAnnotations("@param")) {
				String[] vals = param.split(" ");
				if(vals.length < 2) {
					CompilerWarning warning = new CompilerWarning("One of the @param values for this comment is malformed,"
							+ " and won't be autocompleted."
							+ " The general format should be \"@param $variableName <TYPE> [<DESCRIPTION>]\"",
							script.getTarget(), FileOptions.SuppressWarning.MalformedComment);
					env.getEnv(CompilerEnvironment.class).addCompilerWarning(script.getScriptFileOptions(), warning);
				} else {
					String name = vals[0];
					String type = vals[1];
					if(type.startsWith("[")) {
						for(int i = 2; i < vals.length; i++) {
							type += " " + vals[i];
							if(vals[i].endsWith("]")) {
								break;
							}
						}
					}
					paramTypes.put(name, type);
				}
			}
			final List<CompletionValues> completions = new ArrayList<>();
			boolean hasFinal = false;
			for(Construct parameter : script.getParameters()) {
				if(parameter instanceof Command) {
					continue;
				}

				if(parameter instanceof CString s) {
					completions.add(() -> Arrays.asList(s.val()));
				} else if(parameter instanceof Variable v) {
					if(v.isFinal()) {
						hasFinal = true;
					}
					String type = paramTypes.get(v.getVariableName());
					if(type == null) {
						if(v.getVariableName().toLowerCase().contains("player")) {
							type = "$Player";
						} else {
							completions.add(NONE);
							continue;
						}
					}
					if(type.startsWith("[") && type.endsWith("]")) {
						type = type.substring(1, type.length() - 1);
						String finalType = type;
						completions.add(() -> Arrays.asList(finalType.split(","))
								.stream().map(item -> item.trim()).toList());
					} else if(type.equalsIgnoreCase("$none") || type.equalsIgnoreCase("none")
							|| type.equalsIgnoreCase("string")) {
						completions.add(NONE);
					} else if(type.equalsIgnoreCase("$player")) {
						completions.add(new CompletionValues() {
							@Override
							public List<String> getCompletions() {
								return Static.getServer().getOnlinePlayers().stream()
										.map(player -> player.getName()).toList();
							}
						});
					} else if(type.equalsIgnoreCase("$offlineplayer")) {
						completions.add(() -> Arrays.asList(Static.getServer().getOfflinePlayers())
								.stream().map(player -> player.getName()).toList());
					} else if(type.equalsIgnoreCase("$boolean") || type.equalsIgnoreCase("boolean")) {
						completions.add(() -> Arrays.asList("true", "false"));
					} else {
						// MethodScript type. Check if it's an enum, and if so, pull the values from it.
						// Otherwise disable completions.
						try {
							FullyQualifiedClassName fqcn = FullyQualifiedClassName.forName(type, Target.UNKNOWN, env);
							CClassType t = CClassType.get(fqcn);
							if(t.isEnum()) {
								MEnumType enumType = NativeTypeList.getNativeEnumType(fqcn);
								completions.add(() -> enumType.values()
										.stream().map(value -> value.name()).toList());
								continue;
							}
						} catch(CRECastException | ClassNotFoundException ex) {
						}
						completions.add(NONE);
					}
				} else {
					// Unknown type, disable completions
					completions.add(NONE);
				}
			}

			boolean finalHasFinal = hasFinal;
			CNativeClosure.ClosureRunnable runnable = (Target t, Environment e, Mixed... args) -> {
				List<String> inputArgs = ((CArray) args[2]).asList().stream().map(val -> val.val()).toList();
				CompletionValues completion = null;
				if(inputArgs.size() <= completions.size()) {
					completion = completions.get(inputArgs.size() - 1);
				}
				if(completion == null) {
					if(finalHasFinal) {
						completion = completions.get(completions.size() - 1);
					} else {
						return new CArray(Target.UNKNOWN);
					}
				}
				List<String> list = completion.getCompletions();
				String comparison = inputArgs.get(inputArgs.size() - 1);
				Mixed[] toReturn = list.stream()
						.filter(item -> item.startsWith(comparison))
						.map(item -> new CString(item, Target.UNKNOWN))
						.toList().toArray(CString[]::new);
				return new CArray(t, toReturn);
			};

			CNativeClosure nativeClosure = new CNativeClosure(runnable, env);

			Commands.set_tabcompleter.customExec(script.getTarget(), env, cmd, nativeClosure);
		}
		MCCommandMap map = Static.getServer().getCommandMap();
		String prefix = Implementation.GetServerType().getBranding().toLowerCase(Locale.ENGLISH);
		map.register(prefix + ":" + script.getCommandName().toLowerCase(), cmd);
	}

	private static interface CompletionValues {
		List<String> getCompletions();
	}

	private static final CompletionValues NONE = () -> new ArrayList<>();


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
				StandardCharsets.UTF_8))) {
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
		BufferedWriter out;
		if(fileLocation.exists()) {
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
		out.write(contents);
		out.close();
		return true;
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
}
