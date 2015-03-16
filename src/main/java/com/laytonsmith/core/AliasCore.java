package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.ArgumentParser;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.abstraction.MCBlockCommandSender;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.enums.MCChatColor;
import com.laytonsmith.commandhelper.CommandHelperFileLocations;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
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
import com.laytonsmith.core.packetjumper.PacketJumper;
import com.laytonsmith.core.profiler.ProfilePoint;
import com.laytonsmith.core.profiler.Profiler;
import com.laytonsmith.core.taskmanager.TaskManager;
import com.laytonsmith.persistence.DataSourceFactory;
import com.laytonsmith.persistence.MemoryDataSource;
import com.laytonsmith.persistence.PersistenceNetwork;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * This class contains all the handling code. It only deals with built-in Java
 * Objects, so that if the Minecraft API Hook changes, porting the code will
 * only require changing the API specific portions, not this core file.
 *
 *
 */
public class AliasCore {

	private File aliasConfig;
	private File auxAliases;
	private File prefFile;
	private File mainFile;
	//AliasConfig config;
	private List<Script> scripts;
	static final Logger logger = Logger.getLogger("Minecraft");
	private Set<String> echoCommand = new HashSet<String>();
	public List<File> autoIncludes;
	public static CommandHelperPlugin parent;

	/**
	 * This constructor accepts the configuration settings for the plugin, and
	 * ensures that the manager uses these settings.
	 *
	 * @param allowCustomAliases Whether or not to allow users to add their own
	 * personal aliases
	 * @param maxCustomAliases How many aliases a player is allowed to have. -1
	 * is unlimited.
	 * @param maxCommands How many commands an alias may contain. Since aliases
	 * can be used like a macro, this can help prevent command spamming.
	 */
	public AliasCore(File aliasConfig, File auxAliases, File prefFile, File mainFile, CommandHelperPlugin parent) {
		this.aliasConfig = aliasConfig;
		this.auxAliases = auxAliases;
		this.prefFile = prefFile;
		AliasCore.parent = parent;
		this.mainFile = mainFile;
	}

	public List<Script> getScripts() {
		return new ArrayList<Script>(scripts);
	}

	/**
	 * This is the workhorse function. It takes a given command, then converts
	 * it into the actual command(s). If the command maps to a defined alias, it
	 * will run the specified alias. It will search through the global list of
	 * aliases, as well as the aliases defined for that specific player. This
	 * function doesn't handle the /alias command however.
	 *
	 * @param command
	 * @return
	 */
	public boolean alias(String command, final MCCommandSender player, List<Script> playerCommands) {

		GlobalEnv gEnv;
		try {
			gEnv = new GlobalEnv(parent.executionQueue, parent.profiler, parent.persistenceNetwork,
					MethodScriptFileLocations.getDefault().getConfigDirectory(),
					new Profiles(MethodScriptFileLocations.getDefault().getSQLProfilesFile()),
					new TaskManager());
		} catch (IOException ex) {
			Logger.getLogger(AliasCore.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		} catch (Profiles.InvalidProfileException ex) {
			throw ConfigRuntimeException.CreateUncatchableException(ex.getMessage(), Target.UNKNOWN);
		}
		CommandHelperEnvironment cEnv = new CommandHelperEnvironment();
		cEnv.SetCommandSender(player);
		Environment env = Environment.createEnvironment(gEnv, cEnv);

		if (player instanceof MCBlockCommandSender) {
			cEnv.SetBlockCommandSender((MCBlockCommandSender) player);
		}

		if (scripts == null) {
			throw ConfigRuntimeException.CreateUncatchableException("Cannot run alias commands, no config file is loaded", Target.UNKNOWN);
		}

		boolean match = false;
		try { //catch RuntimeException
			//If player is null, we are running the test harness, so don't
			//actually add the player to the array.
			if (player != null && player instanceof MCPlayer && echoCommand.contains(((MCPlayer) player).getName())) {
				//we are running one of the expanded commands, so exit with false
				return false;
			}

			//Global aliases override personal ones, so check the list first
			for (Script s : scripts) {
				try {
					if (s.match(command)) {
						this.addPlayerReference(player);
						if (Prefs.ConsoleLogCommands() && s.doLog()) {
							StringBuilder b = new StringBuilder("CH: Running original command ");
							if (player instanceof MCPlayer) {
								b.append("on player ").append(((MCPlayer) player).getName());
							} else {
								b.append("from a MCCommandSender");
							}
							b.append(" ----> ").append(command);
							Static.getLogger().log(Level.INFO, b.toString());
						}
						try {
							env.getEnv(CommandHelperEnvironment.class).SetCommand(command);
							ProfilePoint alias = env.getEnv(GlobalEnv.class).GetProfiler().start("Global Alias - \"" + command + "\"", LogLevel.ERROR);
							try {
								s.run(s.getVariables(command), env, new MethodScriptComplete() {
									@Override
									public void done(String output) {
										try {
											if (output != null) {
												if (!output.trim().isEmpty() && output.trim().startsWith("/")) {
													if (Prefs.DebugMode()) {
														if (player instanceof MCPlayer) {
															Static.getLogger().log(Level.INFO, "[CommandHelper]: Executing command on " + ((MCPlayer) player).getName() + ": " + output.trim());
														} else {
															Static.getLogger().log(Level.INFO, "[CommandHelper]: Executing command from console equivalent: " + output.trim());
														}
													}

													if (player instanceof MCPlayer) {
														((MCPlayer) player).chat(output.trim());
													} else {
														Static.getServer().dispatchCommand(player, output.trim().substring(1));
													}
												}
											}
										} catch (Throwable e) {
											System.err.println(e.getMessage());
											player.sendMessage(MCChatColor.RED + e.getMessage());
										} finally {
											Static.getAliasCore().removePlayerReference(player);
										}
									}
								});
							} finally {
								alias.stop();
							}
						} catch (ConfigRuntimeException ex) {
							ex.setEnv(env);
							ConfigRuntimeException.HandleUncaughtException(ex, env);
						} catch (Throwable e) {
							//This is not a simple user script error, this is a deeper problem, so we always handle this.
							System.err.println("An unexpected exception occured: " + e.getClass().getSimpleName());
							player.sendMessage("An unexpected exception occured: " + MCChatColor.RED + e.getClass().getSimpleName());
							e.printStackTrace();
						} finally {
							Static.getAliasCore().removePlayerReference(player);
						}
						match = true;
						break;
					}
				} catch (Exception e) {
					System.err.println("An unexpected exception occured inside the command " + s.toString());
					e.printStackTrace();
				}
			}

			if (player instanceof MCPlayer) {
				if (match == false && playerCommands != null) {
					//if we are still looking, look in the aliases for this player
					for (Script ac : playerCommands) {
						//RunnableAlias b = ac.getRunnableAliases(command, player);
						try {

							ac.compile();

							if (ac.match(command)) {
								Static.getAliasCore().addPlayerReference(player);
								ProfilePoint alias = env.getEnv(GlobalEnv.class).GetProfiler().start("User Alias (" + player.getName() + ") - \"" + command + "\"", LogLevel.ERROR);
								try {
									ac.run(ac.getVariables(command), env, new MethodScriptComplete() {
										@Override
										public void done(String output) {
											if (output != null) {
												if (!output.trim().isEmpty() && output.trim().startsWith("/")) {
													if (Prefs.DebugMode()) {
														Static.getLogger().log(Level.INFO, "[CommandHelper]: Executing command on " + ((MCPlayer) player).getName() + ": " + output.trim());
													}
													((MCPlayer) player).chat(output.trim());
												}
											}
											Static.getAliasCore().removePlayerReference(player);
										}
									});
								} finally {
									alias.stop();
								}
								match = true;
								break;
							}
						} catch (ConfigRuntimeException e) {
							//Unlike system scripts, this should just report the problem to the player
							//env.getEnv(CommandHelperEnvironment.class).SetCommandSender(player);
							Static.getAliasCore().removePlayerReference(player);
							e.setEnv(env);
							ConfigRuntimeException.HandleUncaughtException(e, env);
							match = true;
						} catch (ConfigCompileException e) {
							//Something strange happened, and a bad alias was added
							//to the database. Our best course of action is to just
							//skip it.
						}
					}

				}
			}
		} catch (Throwable e) {
			//Not only did an error happen, an error happened in our error handler
			throw new InternalException(TermColors.RED + "An unexpected error occured in the CommandHelper plugin. "
					+ "Further, this is likely an error with the error handler, so it may be caused by your script, "
					+ "however, there is no more information at this point. Check your script, but also report this "
					+ "as a bug in CommandHelper. Also, it's possible that some commands will no longer work. As a temporary "
					+ "workaround, restart the server, and avoid doing whatever it is you did to make this happen.\nThe error is as follows: "
					+ e.toString() + "\n" + TermColors.reset() + "Stack Trace:\n" + StringUtils.Join(Arrays.asList(e.getStackTrace()), "\n"));
		}
		return match;
	}

	private static final ArgumentParser reloadOptions;

	static {
		reloadOptions = ArgumentParser.GetParser().addFlag("whitelist", "Sets the list of arguments to be a whitelist, that is,"
				+ " only the specified modules get reloaded, the rest will be skipped. Without this option, the specified modules"
				+ " don't get reloaded.")
				.addFlag('g', "globals", "Specifies that globals memory (values stored with export/import) should be preserved.")
				.addFlag('t', "tasks", "Specifies that tasks registered with set_interval/set_timeout should be preserved.")
				.addFlag('e', "execution-queue", "Specifies that tasks registered in execution queues should be preserved.")
				.addFlag('r', "persistence-config", "Specifies that the persistence config file should not be reloaded.")
				.addFlag('f', "profiler", "Specifies that the profiler config should not be reloaded.")
				.addFlag('s', "scripts", "Specifies that scripts should not be reloaded.")
				.addFlag('x', "extensions", "Specifies that extensions should not be reloaded.")
				.addFlag('h', "help", "Prints this list and returns. Nothing is reloaded if this option is set.");
	}

	/**
	 * Loads the global alias file in from the file system. If a player is
	 * running the command, send a reference to them, and they will see compile
	 * errors, otherwise, null.
	 *
	 * @param player
	 * @param settings The argument list for the settings.
	 */
	public final void reload(MCPlayer player, String[] settings) {
		boolean reloadGlobals = true;
		boolean reloadTimeouts = true;
		boolean reloadExecutionQueue = true;
		boolean reloadPersistenceConfig = true;
		boolean reloadProfiler = true;
		boolean reloadScripts = true;
		boolean reloadExtensions = true;

		if (settings != null) {
			ArgumentParser.ArgumentParserResults results;
			try {
				results = reloadOptions.match(settings);
			} catch (ArgumentParser.ValidationException ex) {
				Logger.getLogger(AliasCore.class.getName()).log(Level.SEVERE, null, ex);
				return;
			}
			if (results.isFlagSet('h')) {
				if (player != null) {
					player.sendMessage(reloadOptions.getBuiltDescription());
				} else {
					System.out.println(reloadOptions.getBuiltDescription());
				}
				return;
			}
			if (results.isFlagSet("whitelist")) {
				//Invert the results
				reloadGlobals = false;
				reloadTimeouts = false;
				reloadExecutionQueue = false;
				reloadPersistenceConfig = false;
				reloadProfiler = false;
				reloadScripts = false;
				reloadExtensions = false;
			}
			if (results.isFlagSet('g')) {
				reloadGlobals = !reloadGlobals;
			}
			if (results.isFlagSet('t')) {
				reloadTimeouts = !reloadTimeouts;
			}
			if (results.isFlagSet('e')) {
				reloadExecutionQueue = !reloadExecutionQueue;
			}
			if (results.isFlagSet('r') || results.isFlagSet("persistence-config")) {
				reloadPersistenceConfig = !reloadPersistenceConfig;
			}
			if (results.isFlagSet('f')) {
				reloadProfiler = !reloadProfiler;
			}
			if (results.isFlagSet('s')) {
				reloadScripts = !reloadScripts;
			}
			if (results.isFlagSet('x')) {
				reloadExtensions = !reloadExtensions;
			}
		}
		try {
			if (Prefs.AllowDynamicShell()) {
				CHLog.GetLogger().Log(CHLog.Tags.GENERAL, LogLevel.WARNING, "allow-dynamic-shell is set to true in "
						+ CommandHelperFileLocations.getDefault().getProfilerConfigFile().getName() + " you should set this to false, except during development.", Target.UNKNOWN);
			}

			if (parent.profiler == null || reloadProfiler) {
				parent.profiler = new Profiler(MethodScriptFileLocations.getDefault().getProfilerConfigFile());
			}

			ProfilePoint extensionPreReload = parent.profiler.start("Extension PreReloadAliases call", LogLevel.VERBOSE);
			try {
				// Allow new-style extensions know we are about to reload aliases.
				ExtensionManager.PreReloadAliases(reloadGlobals, reloadTimeouts,
					reloadExecutionQueue, reloadPersistenceConfig, true, // TODO: This should be an object, not a bunch of booleans
					reloadProfiler, reloadScripts, reloadExtensions);    // and this hardcoded true should be removed then.
			} finally {
				extensionPreReload.stop();
			}

			ProfilePoint shutdownHooks = parent.profiler.start("Shutdown hooks call", LogLevel.VERBOSE);
			try {
				StaticLayer.GetConvertor().runShutdownHooks();
			} finally {
				shutdownHooks.stop();
			}
			CHLog.initialize(MethodScriptFileLocations.getDefault().getConfigDirectory());

			//Clear out the data source cache
			DataSourceFactory.DisconnectAll();

			PacketJumper.startup();

			if (reloadExtensions) {
				ProfilePoint extensionManagerStartup = parent.profiler.start("Extension manager startup", LogLevel.VERBOSE);
				try {
					ExtensionManager.Startup();
				} finally {
					extensionManagerStartup.stop();
				}
			}
			CHLog.GetLogger().Log(CHLog.Tags.GENERAL, LogLevel.VERBOSE, "Scripts reloading...", Target.UNKNOWN);
			if (parent.persistenceNetwork == null || reloadPersistenceConfig) {
				ProfilePoint persistenceConfigReload = parent.profiler.start("Reloading persistence configuration", LogLevel.VERBOSE);
				try {
					MemoryDataSource.ClearDatabases();
					ConnectionMixinFactory.ConnectionMixinOptions options = new ConnectionMixinFactory.ConnectionMixinOptions();
					options.setWorkingDirectory(MethodScriptFileLocations.getDefault().getConfigDirectory());
					parent.persistenceNetwork = new PersistenceNetwork(MethodScriptFileLocations.getDefault().getPersistenceConfig(),
							new URI("sqlite:/" + MethodScriptFileLocations.getDefault().getDefaultPersistenceDBFile()
							.getCanonicalFile().toURI().getRawSchemeSpecificPart().replace("\\", "/")), options);
				} finally {
					persistenceConfigReload.stop();
				}
			}
			GlobalEnv gEnv;
			try {
				gEnv = new GlobalEnv(parent.executionQueue, parent.profiler, parent.persistenceNetwork,
						MethodScriptFileLocations.getDefault().getConfigDirectory(),
						new Profiles(MethodScriptFileLocations.getDefault().getSQLProfilesFile()),
						new TaskManager());
				gEnv.SetLabel(Static.GLOBAL_PERMISSION);
			} catch (Profiles.InvalidProfileException ex) {
				CHLog.GetLogger().e(CHLog.Tags.GENERAL, ex.getMessage(), Target.UNKNOWN);
				return;
			}
			if (reloadExecutionQueue) {
				ProfilePoint stoppingExecutionQueue = parent.profiler.start("Stopping execution queues", LogLevel.VERBOSE);
				try {
					parent.executionQueue.stopAllNow();
				} finally {
					stoppingExecutionQueue.stop();
				}
			}
			CommandHelperEnvironment cEnv = new CommandHelperEnvironment();
			Environment env = Environment.createEnvironment(gEnv, cEnv);
			if (reloadGlobals) {
				ProfilePoint clearingGlobals = parent.profiler.start("Clearing globals", LogLevel.VERBOSE);
				try {
					Globals.clear();
				} finally {
					clearingGlobals.stop();
				}
			}
			if (reloadTimeouts) {
				ProfilePoint clearingTimeouts = parent.profiler.start("Clearing timeouts/intervals", LogLevel.VERBOSE);
				try {
					Scheduling.ClearScheduledRunners();
				} finally {
					clearingTimeouts.stop();
				}
			}
			if (!aliasConfig.exists()) {
				aliasConfig.getParentFile().mkdirs();
				aliasConfig.createNewFile();
				try {
					String samp_config = getStringResource(AliasCore.class.getResourceAsStream("/samp_config.txt"));
					//Because the sample config may have been written an a machine that isn't this type, replace all
					//line endings
					samp_config = samp_config.replaceAll("\n|\r\n", System.getProperty("line.separator"));
					file_put_contents(aliasConfig, samp_config, "o");
				} catch (Exception e) {
					logger.log(Level.WARNING, "CommandHelper: Could not write sample config file");
				}
			}

			if (!mainFile.exists()) {
				mainFile.getParentFile().mkdirs();
				mainFile.createNewFile();
				try {
					String samp_main = getStringResource(AliasCore.class.getResourceAsStream("/samp_main.txt"));
					samp_main = samp_main.replaceAll("\n|\r\n", System.getProperty("line.separator"));
					file_put_contents(mainFile, samp_main, "o");
				} catch (Exception e) {
					logger.log(Level.WARNING, "CommandHelper: Could not write sample main file");
				}
			}

			if (!Prefs.isInitialized()) {
				Prefs.init(prefFile);
			}

			if (reloadScripts) {
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
				IncludeCache.clearCache(); //Clear the include cache, so it re-pulls files
				Static.getServer().getMessenger().closeAllChannels(); // Close all channel messager channels registered by CH.

				scripts = new ArrayList<Script>();

				LocalPackage localPackages = new LocalPackage();

				//Run the main file once
				String main = file_get_contents(mainFile.getAbsolutePath());
				localPackages.appendMS(main, mainFile);

				String alias_config = file_get_contents(aliasConfig.getAbsolutePath()); //get the file again
				localPackages.appendMSA(alias_config, aliasConfig);

				//Now that we've included the default files, search the local_packages directory
				GetAuxAliases(auxAliases, localPackages);

				autoIncludes = localPackages.getAutoIncludes();

				ProfilePoint compilerMS = parent.profiler.start("Compilation of MS files in Local Packages", LogLevel.VERBOSE);
				try {
					localPackages.compileMS(player, env);
				} finally {
					compilerMS.stop();
				}
				ProfilePoint compilerMSA = parent.profiler.start("Compilation of MSA files in Local Packages", LogLevel.VERBOSE);
				try {
					localPackages.compileMSA(scripts, player);
				} finally {
					compilerMSA.stop();
				}
			}
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "[CommandHelper]: Path to config file is not correct/accessable. Please"
					+ " check the location and try loading the plugin again.");
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
	 * Returns the contents of a file as a string. Accepts the file location as
	 * a string.
	 *
	 * @param file_location
	 * @return the contents of the file as a string
	 * @throws Exception if the file cannot be found
	 */
	public static String file_get_contents(String file_location) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(file_location));
		String ret = "";
		String str;
		while ((str = in.readLine()) != null) {
			ret += str + "\n";
		}
		in.close();
		return ret;
	}

	/**
	 * This function writes the contents of a string to a file.
	 *
	 * @param file_location the location of the file on the disk
	 * @param contents the string to be written to the file
	 * @param mode the mode in which to write the file: <br /> <ul> <li>"o" -
	 * overwrite the file if it exists, without asking</li> <li>"a" - append to
	 * the file if it exists, without asking</li> <li>"c" - cancel the operation
	 * if the file exists, without asking</li> </ul>
	 * @return true if the file was written, false if it wasn't. Throws an
	 * exception if the file could not be created, or if the mode is not valid.
	 * @throws Exception if the file could not be created
	 */
	public static boolean file_put_contents(File file_location, String contents, String mode)
			throws Exception {
		BufferedWriter out = null;
		File f = file_location;
		if (f.exists()) {
			//do different things depending on our mode
			if (mode.equalsIgnoreCase("o")) {
				out = new BufferedWriter(new FileWriter(file_location));
			} else if (mode.equalsIgnoreCase("a")) {
				out = new BufferedWriter(new FileWriter(file_location, true));
			} else if (mode.equalsIgnoreCase("c")) {
				return false;
			} else {
				throw new RuntimeException("Undefined mode in file_put_contents: " + mode);
			}
		} else {
			out = new BufferedWriter(new FileWriter(file_location));
		}
		//At this point, we are assured that the file is open, and ready to be written in
		//from this point in the file.
		if (out != null) {
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
			while ((n = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}
		} finally {
			if (is != null) {
				is.close();
			}
		}
		return writer.toString();
	}

	public void removePlayerReference(MCCommandSender p) {
		//If they're not a player, oh well.
		if (p instanceof MCPlayer) {
			echoCommand.remove(((MCPlayer) p).getName());
		}
	}

	public void addPlayerReference(MCCommandSender p) {
		if (p instanceof MCPlayer) {
			echoCommand.add(((MCPlayer) p).getName());
		}
	}

	public boolean hasPlayerReference(MCCommandSender p) {
		if (p instanceof MCPlayer) {
			return echoCommand.contains(((MCPlayer) p).getName());
		} else {
			return false;
		}
	}

	public static class LocalPackage {

		public static class FileInfo {

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
		private List<File> autoIncludes = new ArrayList<File>();
		private List<FileInfo> ms = new ArrayList<FileInfo>();
		private List<FileInfo> msa = new ArrayList<FileInfo>();

		public List<FileInfo> getMSFiles() {
			return new ArrayList<FileInfo>(ms);
		}

		public List<FileInfo> getMSAFiles() {
			return new ArrayList<FileInfo>(msa);
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

		public void compileMSA(List<Script> scripts, MCPlayer player) {

			for (FileInfo fi : msa) {
				List<Script> tempScripts;
				try {
					tempScripts = MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(fi.contents, fi.file, false));
					for (Script s : tempScripts) {
						try {
							try {
								s.compile();
								s.checkAmbiguous((ArrayList<Script>) scripts);
								scripts.add(s);
							} catch (ConfigCompileException e) {
								ConfigRuntimeException.HandleUncaughtException(e, "Compile error in script. Compilation will attempt to continue, however.", player);
							} catch(ConfigCompileGroupException ex){
								for(ConfigCompileException e : ex.getList()){
									ConfigRuntimeException.HandleUncaughtException(e, "Compile error in script. Compilation will attempt to continue, however.", player);
								}
							}
						} catch (RuntimeException ee) {
							throw new RuntimeException("While processing a script, "
									+ "(" + fi.file() + ") an unexpected exception occurred. (No further information"
									+ " is available, unfortunately.)", ee);
						}
					}
				} catch (ConfigCompileException e) {
					ConfigRuntimeException.HandleUncaughtException(e, "Could not compile file " + fi.file + " compilation will halt.", player);
					return;
				}
			}
			int errors = 0;
			for (Script s : scripts) {
				if (s.compilerError) {
					errors++;
				}
			}
			if (errors > 0) {
				System.out.println(TermColors.YELLOW + "[CommandHelper]: " + (scripts.size() - errors) + " alias(es) defined, " + TermColors.RED + "with " + errors + " aliases with compile errors." + TermColors.reset());
				if (player != null) {
					player.sendMessage(MCChatColor.YELLOW + "[CommandHelper]: " + (scripts.size() - errors) + " alias(es) defined, " + MCChatColor.RED + "with " + errors + " aliases with compile errors.");
				}
			} else {
				System.out.println(TermColors.YELLOW + "[CommandHelper]: " + scripts.size() + " alias(es) defined." + TermColors.reset());
				if (player != null) {
					player.sendMessage(MCChatColor.YELLOW + "[CommandHelper]: " + scripts.size() + " alias(es) defined.");
				}
			}
		}

		public void compileMS(MCPlayer player, Environment env) {
			for (FileInfo fi : ms) {
				boolean exception = false;
				try {
					env.getEnv(CommandHelperEnvironment.class).SetCommandSender(Static.getServer().getConsole());
					MethodScriptCompiler.registerAutoIncludes(env, null);
					MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(fi.contents, fi.file, true)), env, null, null);
				} catch (ConfigCompileGroupException e){
					exception = true;
					ConfigRuntimeException.HandleUncaughtException(e, fi.file.getAbsolutePath() + " could not be compiled, due to compile errors.", player);
				} catch (ConfigCompileException e) {
					exception = true;
					ConfigRuntimeException.HandleUncaughtException(e, fi.file.getAbsolutePath() + " could not be compiled, due to a compile error.", player);
				} catch (ConfigRuntimeException e) {
					exception = true;
					ConfigRuntimeException.HandleUncaughtException(e, env);
				} catch (CancelCommandException e) {
					if (e.getMessage() != null && !"".equals(e.getMessage().trim())) {
						logger.log(Level.INFO, e.getMessage());
					}
				} catch (ProgramFlowManipulationException e) {
					exception = true;
					ConfigRuntimeException.HandleUncaughtException(ConfigRuntimeException.CreateUncatchableException("Cannot break program flow in main files.", e.getTarget()), env);
				} finally {
					env.getEnv(CommandHelperEnvironment.class).SetCommandSender(null);
				}
				if (exception) {
					if (Prefs.HaltOnFailure()) {
						logger.log(Level.SEVERE, TermColors.RED + "[CommandHelper]: Compilation halted due to unrecoverable failure." + TermColors.reset());
						return;
					}
				}
			}
			logger.log(Level.INFO, TermColors.YELLOW + "[CommandHelper]: MethodScript files processed" + TermColors.reset());
			if (player != null) {
				player.sendMessage(MCChatColor.YELLOW + "[CommandHelper]: MethodScript files processed");
			}
		}
	}

	public static void GetAuxAliases(File start, LocalPackage pack) {
		if (start.isDirectory() && !start.getName().endsWith(".disabled") && !start.getName().endsWith(".library")) {
			for (File f : start.listFiles()) {
				GetAuxAliases(f, pack);
			}
		} else if (start.isFile()) {
			if (start.getName().endsWith(".msa")) {
				try {
					pack.appendMSA(file_get_contents(start.getAbsolutePath()), start);
				} catch (IOException ex) {
					Logger.getLogger(AliasCore.class.getName()).log(Level.SEVERE, null, ex);
				}
			} else if (start.getName().endsWith(".ms")) {
				if (start.getName().equals("auto_include.ms")) {
					pack.addAutoInclude(start);
				} else {
					try {
						pack.appendMS(file_get_contents(start.getAbsolutePath()), start);
					} catch (IOException ex) {
						Logger.getLogger(AliasCore.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			} else if (start.getName().endsWith(".mslp")) {
				try {
					GetAuxZipAliases(new ZipFile(start), pack);
				} catch (ZipException ex) {
					Logger.getLogger(AliasCore.class.getName()).log(Level.SEVERE, null, ex);
				} catch (IOException ex) {
					Logger.getLogger(AliasCore.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}

	private static void GetAuxZipAliases(ZipFile file, LocalPackage pack) {
		ZipEntry ze;
		Enumeration<? extends ZipEntry> entries = file.entries();
		while (entries.hasMoreElements()) {
			ze = entries.nextElement();
			if (ze.getName().endsWith(".ms")) {
				if (ze.getName().equals("auto_include.ms")) {
					pack.addAutoInclude(new File(file.getName() + File.separator + ze.getName()));
				} else {
					try {
						pack.appendMS(Installer.parseISToString(file.getInputStream(ze)), new File(file.getName() + File.separator + ze.getName()));
					} catch (IOException ex) {
						Logger.getLogger(AliasCore.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			} else if (ze.getName().endsWith(".msa")) {
				try {
					pack.appendMSA(Installer.parseISToString(file.getInputStream(ze)), new File(file.getName() + File.separator + ze.getName()));
				} catch (IOException ex) {
					Logger.getLogger(AliasCore.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}
}
