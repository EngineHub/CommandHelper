package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.ClassDiscovery;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.abstraction.MCBlockCommandSender;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.enums.MCChatColor;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.events.EventUtils;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.ProgramFlowManipulationException;
import com.laytonsmith.core.functions.Economy;
import com.laytonsmith.core.functions.IncludeCache;
import com.laytonsmith.core.functions.Scheduling;
import com.laytonsmith.core.packetjumper.PacketJumper;
import com.laytonsmith.core.profiler.ProfilePoint;
import com.laytonsmith.core.profiler.Profiler;
import com.laytonsmith.persistance.MemoryDataSource;
import com.laytonsmith.persistance.PersistanceNetwork;
import com.laytonsmith.persistance.io.ConnectionMixinFactory;
import com.sk89q.util.StringUtil;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import org.bukkit.Server;

/**
 * This class contains all the handling code. It only deals with built-in Java
 * Objects, so that if the Minecraft API Hook changes, porting the code will
 * only require changing the API specific portions, not this core file.
 *
 * @author Layton
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
	private PermissionsResolver perms;
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
	public AliasCore(File aliasConfig, File auxAliases, File prefFile, File mainFile, PermissionsResolver perms, CommandHelperPlugin parent) {
		this.aliasConfig = aliasConfig;
		this.auxAliases = auxAliases;
		this.prefFile = prefFile;
		this.perms = perms;
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

		GlobalEnv gEnv = new GlobalEnv(parent.executionQueue, parent.profiler, parent.persistanceNetwork, parent.permissionsResolver, parent.chDirectory);
		CommandHelperEnvironment cEnv = new CommandHelperEnvironment();
		cEnv.SetCommandSender(player);
		Environment env = Environment.createEnvironment(gEnv, cEnv);
		
		if(player instanceof MCBlockCommandSender){
			cEnv.SetBlockCommandSender((MCBlockCommandSender)player);
		}

		if (scripts == null) {
			throw new ConfigRuntimeException("Cannot run alias commands, no config file is loaded", Target.UNKNOWN);
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
							s.run(s.getVariables(command), env, new MethodScriptComplete() {
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
							alias.stop();
						} catch (ConfigRuntimeException ex) {
							ex.setEnv(env);
							ConfigRuntimeException.React(ex, env);
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
								ac.run(ac.getVariables(command), env, new MethodScriptComplete() {
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
								alias.stop();
								match = true;
								break;
							}
						} catch (ConfigRuntimeException e) {
							//Unlike system scripts, this should just report the problem to the player
							e.getEnv().getEnv(CommandHelperEnvironment.class).SetCommandSender(player);
							Static.getAliasCore().removePlayerReference(player);
							ConfigRuntimeException.React(e, env);
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
					+ e.toString() + "\n" + TermColors.reset() + "Stack Trace:\n" + StringUtil.joinString(Arrays.asList(e.getStackTrace()), "\n", 0));
		}
		return match;
	}

	/**
	 * Loads the global alias file in from the file system. If a player is
	 * running the command, send a reference to them, and they will see compile
	 * errors, otherwise, null.
	 */
	public final void reload(MCPlayer player) {
		try {
			StaticLayer.GetConvertor().runShutdownHooks();
			CHLog.initialize(parent.chDirectory);
			//Install bukkit into the class discovery class
			ClassDiscovery.InstallDiscoveryLocation(ClassDiscovery.GetClassPackageHierachy(Server.class));
			ExtensionManager.Startup();
			CHLog.GetLogger().Log(CHLog.Tags.GENERAL, LogLevel.VERBOSE, "Scripts reloading...", Target.UNKNOWN);
			parent.profiler = new Profiler(new File(parent.chDirectory, "profiler.config"));
			ConnectionMixinFactory.ConnectionMixinOptions options = new ConnectionMixinFactory.ConnectionMixinOptions();
			options.setWorkingDirectory(parent.chDirectory);
			MemoryDataSource.ClearDatabases();
			PacketJumper.startup();
			parent.persistanceNetwork = new PersistanceNetwork(new File(parent.chDirectory, "persistance.config"),
					new URI("sqlite:/" + new File(parent.chDirectory, "persistance.db").getCanonicalFile().toURI().getRawSchemeSpecificPart().replace("\\", "/")), options);
			GlobalEnv gEnv = new GlobalEnv(parent.executionQueue, parent.profiler, parent.persistanceNetwork, parent.permissionsResolver,
					parent.chDirectory);
			CommandHelperEnvironment cEnv = new CommandHelperEnvironment();
			Environment env = Environment.createEnvironment(gEnv, cEnv);
			Globals.clear();
			Scheduling.ClearScheduledRunners();
			EventUtils.UnregisterAll();
			IncludeCache.clearCache(); //Clear the include cache, so it re-pulls files
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

			Prefs.init(prefFile);
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
			localPackages.compileMS(player, env);
			compilerMS.stop();
			ProfilePoint compilerMSA = parent.profiler.start("Compilation of MSA files in Local Packages", LogLevel.VERBOSE);
			localPackages.compileMSA(scripts, player);
			compilerMSA.stop();

		} catch (IOException ex) {
			logger.log(Level.SEVERE, "[CommandHelper]: Path to config file is not correct/accessable. Please"
					+ " check the location and try loading the plugin again.");
		} catch (Throwable t) {
			t.printStackTrace();
		}

		if (!Economy.setupEconomy()) {
			if (Prefs.DebugMode()) {
				logger.log(Level.WARNING, "[CommandHelper]: Economy could not be initialized. No further"
						+ " errors will occur, unless you try to use an Economy function.");
			}
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
								ConfigRuntimeException.React(e, "Compile error in script. Compilation will attempt to continue, however.", player);
							}
						} catch (RuntimeException ee) {
							throw new RuntimeException("While processing a script, "
									+ "(" + fi.file() + ") an unexpected exception occurred. (No further information"
									+ " is available, unfortunately.)", ee);
						}
					}
				} catch (ConfigCompileException e) {
					ConfigRuntimeException.React(e, "Could not compile file " + fi.file + " compilation will halt.", player);
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
					MethodScriptCompiler.registerAutoIncludes(env, null);
					MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(fi.contents, fi.file, true)), env, null, null);
				} catch (ConfigCompileException e) {
					exception = true;
					ConfigRuntimeException.React(e, fi.file.getAbsolutePath() + " could not be compiled, due to a compile error.", player);
				} catch (ConfigRuntimeException e) {
					exception = true;
					ConfigRuntimeException.React(e, env);
				} catch(CancelCommandException e){
					if(e.getMessage() != null && !"".equals(e.getMessage().trim())){
						logger.log(Level.INFO, e.getMessage());
					}
				} catch(ProgramFlowManipulationException e){
					exception = true;
					ConfigRuntimeException.React(new ConfigRuntimeException("Cannot break program flow in main files.", e.getTarget()), env);
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
