// $Id$
/*
 * CommandHelper
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.laytonsmith.commandhelper;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscoveryCache;
import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.ExecutionQueue;
import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCCommand;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.bukkit.BukkitMCBlockCommandSender;
import com.laytonsmith.abstraction.bukkit.BukkitMCCommand;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.abstraction.enums.MCChatColor;
import com.laytonsmith.core.AliasCore;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.Installer;
import com.laytonsmith.core.Main;
import com.laytonsmith.core.MethodScriptExecutionQueue;
import com.laytonsmith.core.MethodScriptFileLocations;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.UpgradeLog;
import com.laytonsmith.core.UserManager;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.extensions.ExtensionManager;
import com.laytonsmith.core.profiler.Profiler;
import com.laytonsmith.persistence.DataSourceException;
import com.laytonsmith.persistence.PersistenceNetwork;
import com.laytonsmith.persistence.ReadOnlyException;
import org.bukkit.Server;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point for the plugin.
 *
 * @author sk89q
 */
public class CommandHelperPlugin extends JavaPlugin {
	//Do not rename this field, it is changed reflectively in unit tests.

	private static AliasCore ac;
	public static MCServer myServer;
	public static SimpleVersion version;
	public static CommandHelperPlugin self;
	public static ExecutorService hostnameLookupThreadPool;
	public static ConcurrentHashMap<String, String> hostnameLookupCache;
	private static int hostnameThreadPoolID = 0;
	public Profiler profiler;
	public final ExecutionQueue executionQueue = new MethodScriptExecutionQueue("CommandHelperExecutionQueue", "default");
	public PersistenceNetwork persistenceNetwork;
	public boolean firstLoad = true;
	public long interpreterUnlockedUntil = 0;
	private Thread loadingThread;
	/**
	 * Listener for the plugin system.
	 */
	final CommandHelperListener playerListener =
			new CommandHelperListener(this);
	/**
	 * Interpreter listener
	 */
	public final CommandHelperInterpreterListener interpreterListener =
			new CommandHelperInterpreterListener(this);
	/**
	 * Server Command Listener, for console commands
	 */
	final CommandHelperServerListener serverListener =
			new CommandHelperServerListener();
	final Set<MCPlayer> commandRunning = new HashSet<MCPlayer>();

	@Override
	public void onLoad() {
		Implementation.setServerType(Implementation.Type.BUKKIT);

		CommandHelperFileLocations.setDefault(new CommandHelperFileLocations());
		CommandHelperFileLocations.getDefault().getCacheDirectory().mkdirs();
		CommandHelperFileLocations.getDefault().getPreferencesDirectory().mkdirs();

		UpgradeLog upgradeLog = new UpgradeLog(CommandHelperFileLocations.getDefault().getUpgradeLogFile());
		upgradeLog.addUpgradeTask(new UpgradeLog.UpgradeTask() {

			String version = null;
			@Override
			public boolean doRun() {
				try {
					version = "versionUpgrade-" + Main.loadSelfVersion();
					return !hasBreadcrumb(version);
				} catch (Exception ex) {
					Logger.getLogger(CommandHelperPlugin.class.getName()).log(Level.SEVERE, null, ex);
					return false;
				}
			}

			@Override
			public void run() {
				leaveBreadcrumb(version);
			}
		});
		upgradeLog.addUpgradeTask(new UpgradeLog.UpgradeTask() {

			File oldPreferences = new File(CommandHelperFileLocations.getDefault().getConfigDirectory(),
					"preferences.txt");
			@Override
			public boolean doRun() {
				return oldPreferences.exists()
						&& !CommandHelperFileLocations.getDefault().getPreferencesFile().exists();
			}

			@Override
			public void run() {
				try {
					Prefs.init(oldPreferences);
					Prefs.SetColors();
					Logger.getLogger("Minecraft").log(Level.INFO,
							TermColors.YELLOW + "[" + Implementation.GetServerType().getBranding() + "] Old preferences.txt file detected. Moving preferences.txt to preferences.ini." + TermColors.reset());
					FileUtil.copy(oldPreferences, CommandHelperFileLocations.getDefault().getPreferencesFile(), true);
					oldPreferences.deleteOnExit();
				} catch (IOException ex) {
					Logger.getLogger(CommandHelperPlugin.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		});
		upgradeLog.addUpgradeTask(new UpgradeLog.UpgradeTask() {

			File cd = CommandHelperFileLocations.getDefault().getConfigDirectory();
			private final String breadcrumb = "move-preference-files-v1.0";
			@Override
			public boolean doRun() {
				return !hasBreadcrumb(breadcrumb)
						&& new File(cd, "preferences.ini").exists();
			}

			@Override
			public void run() {
				//We need to move the following files:
				//1. persistance.config to prefs/persistence.ini (note the correct spelling)
				//2. preferences.ini to prefs/preferences.ini
				//3. profiler.config to prefs/profiler.ini
				//4. sql-profiles.xml to prefs/sql-profiles.xml
				//5. We are not moving loggerPreferences.txt, instead just deleting it,
				//	because the defaults have changed. Most people aren't using this feature
				//	anyways. (The new one will write itself out upon installation.)
				//Other than the config/prefs directory, we are hardcoding all the values, so
				//we know they are correct (for old values). Any errors will be reported, but will not
				//stop the entire process.
				CommandHelperFileLocations p = CommandHelperFileLocations.getDefault();
				try {
					FileUtil.move(new File(cd, "persistance.config"), p.getPersistenceConfig());
				} catch (IOException ex) {
					Logger.getLogger(CommandHelperPlugin.class.getName()).log(Level.SEVERE, null, ex);
				}
				try {
					FileUtil.move(new File(cd, "preferences.ini"), p.getPreferencesFile());
				} catch (IOException ex) {
					Logger.getLogger(CommandHelperPlugin.class.getName()).log(Level.SEVERE, null, ex);
				}
				try {
					FileUtil.move(new File(cd, "profiler.config"), p.getProfilerConfigFile());
				} catch (IOException ex) {
					Logger.getLogger(CommandHelperPlugin.class.getName()).log(Level.SEVERE, null, ex);
				}
				try {
					FileUtil.move(new File(cd, "sql-profiles.xml"), p.getSQLProfilesFile());
				} catch (IOException ex) {
					Logger.getLogger(CommandHelperPlugin.class.getName()).log(Level.SEVERE, null, ex);
				}
				new File(cd, "logs/debug/loggerPreferences.txt").delete();
				leaveBreadcrumb(breadcrumb);
				System.out.println("CommandHelper: Your preferences files have all been relocated to " + p.getPreferencesDirectory());
				System.out.println("CommandHelper: The loggerPreferences.txt file has been deleted and re-created, as the defaults have changed.");
			}
		});

		// Renames the sql-profiles.xml file to the new name.
		upgradeLog.addUpgradeTask(new UpgradeLog.UpgradeTask() {

			// This should never change
			private final File oldProfilesFile = new File(MethodScriptFileLocations.getDefault().getPreferencesDirectory(), "sql-profiles.xml");

			@Override
			public boolean doRun() {
				return oldProfilesFile.exists();
			}

			@Override
			public void run() {
				try {
					FileUtil.move(oldProfilesFile, MethodScriptFileLocations.getDefault().getProfilesFile());
					System.out.println("CommandHelper: sql-profiles.xml has been renamed to " + MethodScriptFileLocations.getDefault().getProfilesFile().getName());
				} catch (IOException ex) {
					Logger.getLogger(CommandHelperPlugin.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		});

		try {
			upgradeLog.runTasks();
		} catch (IOException ex) {
			Logger.getLogger(CommandHelperPlugin.class.getName()).log(Level.SEVERE, null, ex);
		}

		try{
			Prefs.init(CommandHelperFileLocations.getDefault().getPreferencesFile());
		} catch (IOException ex) {
			Logger.getLogger(CommandHelperPlugin.class.getName()).log(Level.SEVERE, null, ex);
		}

		Prefs.SetColors();
		CHLog.initialize(CommandHelperFileLocations.getDefault().getConfigDirectory());
		Installer.Install(CommandHelperFileLocations.getDefault().getConfigDirectory());
		if(new SimpleVersion(System.getProperty("java.version")).lt(new SimpleVersion("1.7"))){
			CHLog.GetLogger().w(CHLog.Tags.GENERAL, "You appear to be running a version of Java older than Java 7. You should have plans"
					+ " to upgrade at some point, as " + Implementation.GetServerType().getBranding() + " may require it at some point.", Target.UNKNOWN);
		}

		self = this;

		ClassDiscoveryCache cdc = new ClassDiscoveryCache(CommandHelperFileLocations.getDefault().getCacheDirectory());
		cdc.setLogger(Logger.getLogger(CommandHelperPlugin.class.getName()));
		ClassDiscovery.getDefaultInstance().setClassDiscoveryCache(cdc);
		ClassDiscovery.getDefaultInstance().addDiscoveryLocation(ClassDiscovery.GetClassContainer(CommandHelperPlugin.class));
		ClassDiscovery.getDefaultInstance().addDiscoveryLocation(ClassDiscovery.GetClassContainer(Server.class));

		System.out.println("[CommandHelper] Running initial class discovery,"
				+ " this will probably take a few seconds...");
		myServer = StaticLayer.GetServer();

		System.out.println("[CommandHelper] Loading extensions in the background...");

		loadingThread = new Thread("extensionloader") {
			@Override
			public void run() {
				ExtensionManager.AddDiscoveryLocation(CommandHelperFileLocations.getDefault().getExtensionsDirectory());

				if (OSUtils.GetOS() == OSUtils.OS.WINDOWS) {
					// Using System.out here instead of the logger as the logger doesn't
					// immediately print to the console.
					System.out.println("[CommandHelper] Caching extensions...");
					ExtensionManager.Cache(CommandHelperFileLocations.getDefault().getExtensionCacheDirectory());
					System.out.println("[CommandHelper] Extension caching complete.");
				}

				ExtensionManager.Initialize(ClassDiscovery.getDefaultInstance());
				System.out.println("[CommandHelper] Extension loading complete.");
			}
		};

		loadingThread.start();
	}

	/**
	 * Called on plugin enable.
	 */
	@Override
	public void onEnable() {
		if(loadingThread.isAlive()){
			System.out.println("[CommandHelper] Waiting for extension loading to complete...");

			try {
				loadingThread.join();
			} catch (InterruptedException ex) {
				Logger.getLogger(CommandHelperPlugin.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		//Metrics
		try {
			org.mcstats.Metrics m = new Metrics(this);
			m.addCustomData(new Metrics.Plotter("Player count") {

				@Override
				public int getValue() {
					return Static.getServer().getOnlinePlayers().size();
				}
			});
			m.start();
		} catch (IOException e) {
			// Failed to submit the stats :-(
		}

		try {
			//This may seem redundant, but on a /reload, we want to refresh these
			//properties.
			Prefs.init(CommandHelperFileLocations.getDefault().getPreferencesFile());
		} catch (IOException ex) {
			Logger.getLogger(CommandHelperPlugin.class.getName()).log(Level.SEVERE, null, ex);
		}
		if(Prefs.UseSudoFallback()){
			Logger.getLogger(CommandHelperPlugin.class.getName()).log(Level.WARNING, "In your preferences, use-sudo-fallback is turned on. Consider turning this off if you can.");
		}
		CHLog.initialize(CommandHelperFileLocations.getDefault().getConfigDirectory());

		version = new SimpleVersion(getDescription().getVersion());

		String script_name = Prefs.ScriptName();
		String main_file = Prefs.MainFile();
		boolean showSplashScreen = Prefs.ShowSplashScreen();
		if (showSplashScreen) {
			System.out.println(TermColors.reset());
			//System.out.flush();
			System.out.println("\n\n\n" + Static.Logo());
		}
		ac = new AliasCore(new File(CommandHelperFileLocations.getDefault().getConfigDirectory(), script_name),
				CommandHelperFileLocations.getDefault().getLocalPackagesDirectory(),
				CommandHelperFileLocations.getDefault().getPreferencesFile(),
				new File(CommandHelperFileLocations.getDefault().getConfigDirectory(), main_file), this);
		ac.reload(null, null);

		//Clear out our hostname cache
		hostnameLookupCache = new ConcurrentHashMap<>();
		//Create a new thread pool, with a custom ThreadFactory,
		//so we can more clearly name our threads.
		hostnameLookupThreadPool = Executors.newFixedThreadPool(3, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "CommandHelperHostnameLookup-" + (++hostnameThreadPoolID));
			}
		});
		for (MCPlayer p : Static.getServer().getOnlinePlayers()) {
			//Repopulate our cache for currently online players.
			//New players that join later will get a lookup done
			//on them at that time.
			Static.HostnameCache(p);
		}

		BukkitDirtyRegisteredListener.PlayDirty();
		registerEvent(playerListener);

		//interpreter events
		registerEvent(interpreterListener);
		registerEvent(serverListener);

		//Script events
		StaticLayer.Startup(this);

		playerListener.loadGlobalAliases();
		interpreterListener.reload();

		Static.getLogger().log(Level.INFO, "[CommandHelper] CommandHelper {0} enabled", getDescription().getVersion());
	}

	public static AliasCore getCore() {
		return ac;
	}

	/**
	 * Disables the plugin.
	 */
	@Override
	public void onDisable() {
		//free up some memory
		StaticLayer.GetConvertor().runShutdownHooks();
		stopExecutionQueue();

		ExtensionManager.Cleanup();

		ac = null;
	}

	public void stopExecutionQueue() {
		for (String queue : executionQueue.activeQueues()) {
			executionQueue.clear(queue);
		}
	}

	/**
	 * Register an event.
	 *
	 * @param listener
	 */
	public void registerEvent(Listener listener) {
		getServer().getPluginManager().registerEvents(listener, this);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		MCCommandSender mcsender = BukkitConvertor.BukkitGetCorrectSender(sender);
		MCCommand cmd = new BukkitMCCommand(command);
		return cmd.handleTabComplete(mcsender, alias, args);
	}

	/**
	 * Called when a command registered by this plugin is received.
	 * @param sender
	 * @param cmd
	 * @param commandLabel
	 * @param args
	 * @return
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		String cmdName = cmd.getName().toLowerCase();
		if ((sender.isOp() || (sender instanceof Player && (sender.hasPermission("commandhelper.reloadaliases")
				|| sender.hasPermission("ch.reloadaliases"))))
				&& (cmdName.equals("reloadaliases") || cmdName.equals("reloadalias") || cmdName.equals("recompile"))) {
			MCPlayer player = null;
			if (sender instanceof Player) {
				player = new BukkitMCPlayer((Player) sender);
			}
			ac.reload(player, args);
//            if(ac.reload(player)){
//                if(sender instanceof Player){
//                    Static.SendMessage(player, MCChatColor.GOLD + "Command Helper scripts sucessfully recompiled.");
//                }
//                System.out.println(TermColors.YELLOW + "Command Helper scripts sucessfully recompiled." + TermColors.reset());
//            } else{
//                if(sender instanceof Player){
//                    Static.SendMessage(player, MCChatColor.RED + "An error occured when trying to compile the script. Check the console for more information.");
//                }
//                System.out.println(TermColors.RED + "An error occured when trying to compile the script. Check the console for more information." + TermColors.reset());
//            }
			return true;
		} else if (cmdName.equals("commandhelper") && args.length >= 1 && args[0].equalsIgnoreCase("null")) {
			return true;
		} else if (cmdName.equals("runalias")) {
			//Hardcoded alias rebroadcast
			if (sender instanceof Player) {
				PlayerCommandPreprocessEvent pcpe = new PlayerCommandPreprocessEvent((Player) sender, StringUtils.Join(args, " "));
				playerListener.onPlayerCommandPreprocess(pcpe);
			} else if (sender instanceof ConsoleCommandSender) {
				String cmd2 = Static.strJoin(args, " ");
				if (cmd2.startsWith("/")) {
					cmd2 = cmd2.substring(1);
				}
				ServerCommandEvent sce = new ServerCommandEvent((ConsoleCommandSender) sender, cmd2);
				serverListener.onServerCommand(sce);
			} else if(sender instanceof BlockCommandSender){
				MCCommandSender s = new BukkitMCBlockCommandSender((BlockCommandSender)sender);
				String cmd2 = StringUtils.Join(args, " ");
				Static.getAliasCore().alias(cmd2, s, new ArrayList<Script>());
			}
			return true;
		} else if(cmdName.equalsIgnoreCase("interpreter-on")){
			if(sender instanceof ConsoleCommandSender){
				int interpreterTimeout = Prefs.InterpreterTimeout();
				if(interpreterTimeout != 0){
					interpreterUnlockedUntil = (interpreterTimeout * 60 * 1000) + System.currentTimeMillis();
					sender.sendMessage("Inpterpreter mode unlocked for " + interpreterTimeout + " minute" + (interpreterTimeout==1?"":"s"));
				}
			} else {
				sender.sendMessage("This command can only be run from console.");
			}
			return true;
		} else if (sender instanceof Player && java.util.Arrays.asList(new String[]{"commandhelper", "repeat",
				"viewalias", "delalias", "interpreter", "alias"}).contains(cmdName)) {
			try {
				return runCommand(new BukkitMCPlayer((Player) sender), cmdName, args);
			} catch (DataSourceException ex) {
				Logger.getLogger(CommandHelperPlugin.class.getName()).log(Level.SEVERE, null, ex);
			} catch (ReadOnlyException ex) {
				Logger.getLogger(CommandHelperPlugin.class.getName()).log(Level.SEVERE, null, ex);
			} catch (IOException ex) {
				Logger.getLogger(CommandHelperPlugin.class.getName()).log(Level.SEVERE, null, ex);
			}
			return true;
		} else {
			MCCommandSender mcsender = BukkitConvertor.BukkitGetCorrectSender(sender);
			MCCommand mccmd = new BukkitMCCommand(cmd);
			return mccmd.handleCustomCommand(mcsender, commandLabel, args);
		}
	}

	/**
	 * Runs commands.
	 *
	 * @param player
	 * @param cmd
	 * @param args
	 * @return
	 */
	private boolean runCommand(final MCPlayer player, String cmd, String[] args) throws DataSourceException, ReadOnlyException, IOException {
		if (commandRunning.contains(player)) {
			return true;
		}

		commandRunning.add(player);
		UserManager um = UserManager.GetUserManager(player.getName());
		// Repeat command
		if (cmd.equals("repeat")) {
			if (player.isOp() || player.hasPermission("commandhelper.repeat")
					|| player.hasPermission("ch.repeat")) {
				//Go ahead and remove them, so that they can repeat aliases. They can't get stuck in
				//an infinite loop though, because the preprocessor won't try to fire off a repeat command
				commandRunning.remove(player);
				if (um.getLastCommand() != null) {
					Static.SendMessage(player, MCChatColor.GRAY + um.getLastCommand());
					execCommand(player, um.getLastCommand());
				} else {
					Static.SendMessage(player, MCChatColor.RED + "No previous command.");
				}
				return true;
			} else {
				Static.SendMessage(player, MCChatColor.RED + "You do not have permission to access the repeat command");
				commandRunning.remove(player);
				return true;
			}

			// Save alias
		} else if (cmd.equalsIgnoreCase("alias")) {
			if (!player.hasPermission("commandhelper.useralias") && !player.hasPermission("ch.useralias")) {
				Static.SendMessage(player, MCChatColor.RED + "You do not have permission to access the alias command");
				commandRunning.remove(player);
				return true;
			}

			if (args.length > 0) {
				String alias = CommandHelperPlugin.joinString(args, " ");
				try {
					int id = um.addAlias(alias, persistenceNetwork);
					if (id > -1) {
						Static.SendMessage(player, MCChatColor.YELLOW + "Alias added with id '" + id + "'");
					}
				} catch (ConfigCompileException ex) {
					Static.SendMessage(player, "Your alias could not be added due to a compile error:\n" + MCChatColor.RED + ex.getMessage());
				} catch (ConfigCompileGroupException e){
					StringBuilder b = new StringBuilder();
					b.append("Your alias could not be added due to compile errors:\n").append(MCChatColor.RED);
					for(ConfigCompileException ex : e.getList()){
						b.append(ex.getMessage());
					}
					Static.SendMessage(player, b.toString());
				}
			} else {
				//Display a help message
				Static.SendMessage(player, MCChatColor.GREEN + "Command usage: \n"
						+ MCChatColor.GREEN + "/alias <alias> - adds an alias to your user defined list\n"
						+ MCChatColor.GREEN + "/delalias <id> - deletes alias with id <id> from your user defined list\n"
						+ MCChatColor.GREEN + "/viewalias - shows you all of your aliases");
			}

			commandRunning.remove(player);
			return true;
			//View all aliases for this user
		} else if (cmd.equalsIgnoreCase("viewalias")) {
			if (!player.hasPermission("commandhelper.useralias") && !player.hasPermission("ch.useralias")) {
				Static.SendMessage(player, MCChatColor.RED + "You do not have permission to access the viewalias command");
				commandRunning.remove(player);
				return true;
			}
			int page = 0;
			try {
				page = Integer.parseInt(args[0]);
			} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
				//Meh. Index out of bounds, or number format exception. Whatever, show page 1
			}
			Static.SendMessage(player, um.getAllAliases(page, persistenceNetwork));
			commandRunning.remove(player);
			return true;
			// Delete alias
		} else if (cmd.equalsIgnoreCase("delalias")) {
			if (!player.hasPermission("commandhelper.useralias") && !player.hasPermission("ch.useralias")) {
				Static.SendMessage(player, MCChatColor.RED + "You do not have permission to access the delalias command");
				commandRunning.remove(player);
				return true;
			}
			try {
				ArrayList<String> deleted = new ArrayList<String>();
				for (String arg : args) {
					um.delAlias(Integer.parseInt(arg), persistenceNetwork);
					deleted.add("#" + arg);
				}
				if (args.length > 1) {
					String s = MCChatColor.YELLOW + "Aliases " + deleted.toString() + " were deleted";
					Static.SendMessage(player, s);

				} else {
					Static.SendMessage(player, MCChatColor.YELLOW + "Alias #" + args[0] + " was deleted");
				}
			} catch (NumberFormatException e) {
				Static.SendMessage(player, MCChatColor.RED + "The id must be a number");
			} catch (ArrayIndexOutOfBoundsException e) {
				Static.SendMessage(player, MCChatColor.RED + "Usage: /delalias <id> <id> ...");
			}
			commandRunning.remove(player);
			return true;

		} else if (cmd.equalsIgnoreCase("interpreter")) {
			if (player.hasPermission("commandhelper.interpreter")) {
				if (Prefs.EnableInterpreter()) {
					if(Prefs.InterpreterTimeout() != 0){
						if(interpreterUnlockedUntil < System.currentTimeMillis()){
							player.sendMessage(MCChatColor.RED + "Interpreter mode is currently locked. Run \"interpreter-on\" from console to unlock it."
									+ " If you want to turn this off entirely, set the interpreter-timeout option to 0 in "
									+ CommandHelperFileLocations.getDefault().getPreferencesFile().getName());
							commandRunning.remove(player);
							return true;
						}
					}
					interpreterListener.startInterpret(player.getName());
					Static.SendMessage(player, MCChatColor.YELLOW + "You are now in interpreter mode. Type a dash (-) on a line by itself to exit, and >>> to enter"
							+ " multiline mode.");
				} else {
					Static.SendMessage(player, MCChatColor.RED + "The interpreter is currently disabled. Check your preferences file.");
				}
			} else {
				Static.SendMessage(player, MCChatColor.RED + "You do not have permission to run that command");
			}
			commandRunning.remove(player);
			return true;
		}
		commandRunning.remove(player);
		return false;
	}

	/**
	 * Joins a string from an array of strings.
	 *
	 * @param str
	 * @param delimiter
	 * @return
	 */
	public static String joinString(String[] str, String delimiter) {
		if (str.length == 0) {
			return "";
		}
		StringBuilder buffer = new StringBuilder(str[0]);
		for (int i = 1; i < str.length; i++) {
			buffer.append(delimiter).append(str[i]);
		}
		return buffer.toString();
	}

	/**
	 * Execute a command.
	 *
	 * @param player
	 *
	 * @param cmd
	 */
	public static void execCommand(MCPlayer player, String cmd) {
		player.chat(cmd);
	}
}
