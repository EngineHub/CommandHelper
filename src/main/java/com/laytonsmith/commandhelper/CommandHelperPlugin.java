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
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Common.TimeConversionUtil;
import com.laytonsmith.PureUtilities.MapBuilder;
import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCCommand;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.bukkit.BukkitMCCommand;
import com.laytonsmith.abstraction.bukkit.BukkitMCServer;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCMaterial;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPlayer;
import com.laytonsmith.abstraction.enums.MCChatColor;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCArt;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCAttribute;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCBiomeType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCEnchantment;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCEntityType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCLegacyMaterial;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCParticle;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCPatternShape;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCPotionEffectType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCPotionType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCProfession;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCSound;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCTrimMaterial;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCTrimPattern;
import com.laytonsmith.annotations.EventIdentifier;
import com.laytonsmith.core.AliasCore;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.Installer;
import com.laytonsmith.core.MethodScriptFileLocations;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.UpgradeLog;
import com.laytonsmith.core.apps.AppsApiUtil;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.extensions.ExtensionManager;
import com.laytonsmith.core.telemetry.DefaultTelemetry;
import com.laytonsmith.core.telemetry.Telemetry;
import org.bukkit.Bukkit;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.TimedRegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import org.bukkit.entity.minecart.CommandMinecart;

public class CommandHelperPlugin extends JavaPlugin {
	//Do not rename this field, it is changed reflectively in unit tests.
	private static AliasCore ac;
	public static MCServer myServer;
	public static SimpleVersion version;
	public static CommandHelperPlugin self;
	public static ExecutorService hostnameLookupThreadPool;
	public static ConcurrentHashMap<String, String> hostnameLookupCache;
	private static int hostnameThreadPoolID = 0;
	private boolean firstLoad = true;
	private long interpreterUnlockedUntil = 0;

	/**
	 * Listener for the plugin system.
	 */
	final CommandHelperListener playerListener = new CommandHelperListener(this);
	/**
	 * Interpreter listener
	 */
	public final CommandHelperInterpreterListener interpreterListener = new CommandHelperInterpreterListener();
	/**
	 * Server Command Listener, for console commands
	 */
	final CommandHelperServerListener serverListener = new CommandHelperServerListener();

	@Override
	public void onLoad() {
		AppsApiUtil.ConfigureDefaults();
		Implementation.setServerType(Implementation.Type.BUKKIT);

		myServer = BukkitMCServer.Get();
		version = new SimpleVersion(getDescription().getVersion());
		self = this;

		CommandHelperFileLocations.setDefault(new CommandHelperFileLocations());
		CommandHelperFileLocations.getDefault().getCacheDirectory().mkdirs();
		CommandHelperFileLocations.getDefault().getPreferencesDirectory().mkdirs();

		UpgradeLog upgradeLog = new UpgradeLog(CommandHelperFileLocations.getDefault().getUpgradeLogFile());
		upgradeLog.addUpgradeTask(new UpgradeLog.UpgradeTask() {

			String version = null;

			@Override
			public boolean doRun() {
				try {
					version = "versionUpgrade-" + CommandHelperPlugin.version;
					return !hasBreadcrumb(version);
				} catch (Exception ex) {
					getLogger().log(Level.SEVERE, null, ex);
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
					getLogger().log(Level.INFO, TermColors.YELLOW + "["
							+ Implementation.GetServerType().getBranding()
							+ "] Old preferences.txt file detected. Moving preferences.txt to preferences.ini."
							+ TermColors.reset());
					FileUtil.copy(oldPreferences, CommandHelperFileLocations.getDefault().getPreferencesFile(), true);
					oldPreferences.deleteOnExit();
				} catch (IOException ex) {
					getLogger().log(Level.SEVERE, null, ex);
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
					getLogger().log(Level.SEVERE, null, ex);
				}
				try {
					FileUtil.move(new File(cd, "preferences.ini"), p.getPreferencesFile());
				} catch (IOException ex) {
					getLogger().log(Level.SEVERE, null, ex);
				}
				try {
					FileUtil.move(new File(cd, "profiler.config"), p.getProfilerConfigFile());
				} catch (IOException ex) {
					getLogger().log(Level.SEVERE, null, ex);
				}
				try {
					FileUtil.move(new File(cd, "sql-profiles.xml"), p.getProfilesFile());
				} catch (IOException ex) {
					getLogger().log(Level.SEVERE, null, ex);
				}
				new File(cd, "logs/debug/loggerPreferences.txt").delete();
				leaveBreadcrumb(breadcrumb);
				getLogger().log(Level.INFO, "Preference files have been relocated to " + p.getPreferencesDirectory());
				getLogger().log(Level.INFO, "loggerPreferences.txt has been re-created, as the defaults have changed.");
			}
		});

		// Renames the sql-profiles.xml file to the new name.
		upgradeLog.addUpgradeTask(new UpgradeLog.UpgradeTask() {

			// This should never change
			private final File oldProfilesFile = new File(MethodScriptFileLocations.getDefault().getPreferencesDirectory(),
					"sql-profiles.xml");

			@Override
			public boolean doRun() {
				return oldProfilesFile.exists();
			}

			@Override
			public void run() {
				try {
					FileUtil.move(oldProfilesFile, MethodScriptFileLocations.getDefault().getProfilesFile());
					getLogger().log(Level.INFO, "sql-profiles.xml has been renamed to "
							+ MethodScriptFileLocations.getDefault().getProfilesFile().getName());
				} catch (IOException ex) {
					getLogger().log(Level.SEVERE, null, ex);
				}
			}
		});

		try {
			upgradeLog.runTasks();
		} catch (IOException ex) {
			getLogger().log(Level.SEVERE, null, ex);
		} catch (NoClassDefFoundError ex) {
			MSLog.GetLogger().e(MSLog.Tags.GENERAL, "Failed to load CommandHelper. Incorrect jar?", Target.UNKNOWN);
			return;
		}

		try {
			Prefs.init(CommandHelperFileLocations.getDefault().getPreferencesFile());
			Prefs.SetColors();
		} catch (IOException ex) {
			getLogger().log(Level.SEVERE, null, ex);
			return;
		}

		Installer.Install(CommandHelperFileLocations.getDefault().getConfigDirectory());

		ClassDiscoveryCache cdc = new ClassDiscoveryCache(CommandHelperFileLocations.getDefault().getCacheDirectory());
		cdc.setLogger(getLogger());
		ClassDiscovery.getDefaultInstance().setClassDiscoveryCache(cdc);
		ClassDiscovery.getDefaultInstance().addDiscoveryLocation(ClassDiscovery.GetClassContainer(CommandHelperPlugin.class));

		MSLog.initialize(CommandHelperFileLocations.getDefault().getConfigDirectory());

		Telemetry.GetDefault().initializeTelemetry();
		Telemetry.GetDefault().doNag();
		Telemetry.GetDefault().log(DefaultTelemetry.StartupModeMetric.class,
				MapBuilder.start("mode", Implementation.GetServerType().getBranding()), null);

		Thread loadingThread = new Thread("extensionloader") {
			@Override
			public void run() {
				ExtensionManager.AddDiscoveryLocation(CommandHelperFileLocations.getDefault().getExtensionsDirectory());
				if(OSUtils.GetOS() == OSUtils.OS.WINDOWS) {
					getLogger().log(Level.INFO, "Caching extensions in the background...");
					ExtensionManager.Cache(CommandHelperFileLocations.getDefault().getExtensionCacheDirectory());
					getLogger().log(Level.INFO, "Extension caching complete.");
				}
			}
		};
		loadingThread.start();

		// Build dynamic enums
		BukkitMCEntityType.build();
		BukkitMCBiomeType.build();
		BukkitMCSound.build();
		BukkitMCParticle.build();
		BukkitMCPotionEffectType.build();
		BukkitMCProfession.build();
		BukkitMCMaterial.build();
		BukkitMCLegacyMaterial.build();
		BukkitMCPotionType.build();
		BukkitMCEnchantment.build();
		BukkitMCPatternShape.build();
		BukkitMCAttribute.build();
		BukkitMCArt.build();
		if(myServer.getMinecraftVersion().gte(MCVersion.MC1_20)) {
			BukkitMCTrimMaterial.build();
			BukkitMCTrimPattern.build();
		}

		// Suppress warnings from the PluginClassLoader when extensions load classes from another plugin.
		// This should be done before ExtensionManager.Initialize().
		try {
			Class cls = Class.forName("org.bukkit.plugin.java.PluginClassLoader");
			Set<String> ignored = (Set<String>) ReflectionUtils.get(cls, getClassLoader(), "seenIllegalAccess");
			for(Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
				if(plugin != self) {
					ignored.add(plugin.getName());
				}
			}
		} catch (ReflectionUtils.ReflectionException | ClassNotFoundException ex) {
			getLogger().log(Level.INFO, "Failed to suppress classloader warnings.");
			// The server may still log class load warnings for unlisted dependencies.
		}

		if(loadingThread.isAlive()) {
			getLogger().log(Level.INFO, "Waiting for extension caching to complete...");
			try {
				loadingThread.join();
			} catch (InterruptedException ex) {
				getLogger().log(Level.SEVERE, null, ex);
			}
		}
		ExtensionManager.Initialize(ClassDiscovery.getDefaultInstance());
		getLogger().log(Level.INFO, "Extensions initialized.");
	}

	/**
	 * Called on plugin enable.
	 */
	@Override
	public void onEnable() {

		//Metrics
		Metrics m = new Metrics(this, 2987);
		m.addCustomChart(new Metrics.SingleLineChart("player_count",
				() -> Static.getServer().getOnlinePlayers().size()));

		if(Prefs.UseSudoFallback()) {
			getLogger().log(Level.WARNING, "In your preferences, use-sudo-fallback is turned on."
					+ " Consider turning this off if you can.");
		}

		if(Prefs.ShowSplashScreen()) {
			StreamUtils.GetSystemOut().println(TermColors.reset());
			StreamUtils.GetSystemOut().println("\n\n" + Static.Logo());
		}
		ac = new AliasCore(CommandHelperFileLocations.getDefault());
		ac.reload(null, null, this.firstLoad);

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
		for(Player p : getServer().getOnlinePlayers()) {
			//Repopulate our cache for currently online players.
			//New players that join later will get a lookup done on them at that time.
			Static.HostnameCache(p.getName(), p.getAddress());
		}

		BukkitDirtyRegisteredListener.PlayDirty();
		registerEvents(playerListener);
		registerEvents(interpreterListener);
		registerEvents(serverListener);

		//Script events
		StaticLayer.Startup(this);

		firstLoad = false;

		getLogger().log(Level.INFO, "CommandHelper {0} enabled", getDescription().getVersion());
	}

	public boolean isFirstLoad() {
		return this.firstLoad;
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
		if(ac.getStaticRuntimeEnv() != null) {
			ac.getStaticRuntimeEnv().getExecutionQueue().stopAllNow();
		}

		ExtensionManager.Cleanup();
		ac = null;
	}

	/**
	 * Register all events in a Listener class.
	 *
	 * @param listener
	 */
	public void registerEvents(Listener listener) {
		getServer().getPluginManager().registerEvents(listener, this);
	}

	/**
	 * Dynamically registers events in a Listener class.
	 * Registers all methods annotated with @EventHandler.
	 * Registers methods annotated with @EventIdentifier if the event exists in this version according to the Driver.
	 * This method is based on Bukkit's JavaPluginLoader:createRegisteredListeners.
	 * Part of this code would be run normally using the other register method.
	 *
	 * @param listener
	 */
	public void registerEventsDynamic(Listener listener) {
		for(final java.lang.reflect.Method method : listener.getClass().getMethods()) {
			EventIdentifier identifier = method.getAnnotation(EventIdentifier.class);
			EventHandler defaultHandler = method.getAnnotation(EventHandler.class);
			EventPriority priority = EventPriority.LOWEST;
			final Class<? extends Event> eventClass;
			if(defaultHandler != null) {
				priority = defaultHandler.priority();
			}
			if(identifier == null) {
				// Look for normal EventHandler
				if(defaultHandler != null && method.getParameterTypes().length == 1) {
					try {
						eventClass = (Class<? extends Event>) method.getParameterTypes()[0];
					} catch (ClassCastException e) {
						continue;
					}
				} else {
					continue;
				}
			} else {
				// Dynamically register EventIdentifier
				if(!identifier.event().existsInCurrent()) {
					continue;
				}
				try {
					eventClass = (Class<? extends Event>) Class.forName(identifier.className());
				} catch (ClassNotFoundException | ClassCastException e) {
					// Event may only exist in Paper (io.papermc.paper or com.destroytokyo.paper)
					// so when running Spigot, ignore events that are not in org.bukkit.event or org.spigotmc.event.
					if(identifier.className().startsWith("org") || ((BukkitMCServer) myServer).isPaper()) {
						MSLog.GetLogger().e(MSLog.Tags.RUNTIME, "Could not listen for " + identifier.event().name()
								+ " because the class " + identifier.className() + " could not be found."
								+ " This problem is not expected to occur, so please report it on the bug"
								+ " tracker if it does.", Target.UNKNOWN);
					}
					continue;
				}
			}
			HandlerList handler = null;
			Class handlerClass = eventClass;
			while(handlerClass != null) {
				try {
					handler = ReflectionUtils.invokeMethod(handlerClass, null, "getHandlerList");
					break;
				} catch (ReflectionUtils.ReflectionException ref) {
					handlerClass = handlerClass.getSuperclass();
				}
			}
			if(handler == null) {
				MSLog.GetLogger().e(MSLog.Tags.RUNTIME, "Could not listen for " + eventClass.getName()
						+ " because the handler could not be found. Please report this on the bug tracker.",
						Target.UNKNOWN);
				continue;
			}
			EventExecutor executor = (thisListener, event) -> {
				try {
					if(!eventClass.isAssignableFrom(event.getClass())) {
						return;
					}
					method.invoke(thisListener, event);
				} catch (InvocationTargetException ex) {
					throw new EventException(ex.getCause());
				} catch (Throwable t) {
					throw new EventException(t);
				}
			};
			if(this.getServer().getPluginManager().useTimings()) {
				handler.register(new TimedRegisteredListener(listener, executor, priority, this, false));
			} else {
				handler.register(new RegisteredListener(listener, executor, priority, this, false));
			}
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		MCCommandSender mcsender = BukkitConvertor.BukkitGetCorrectSender(sender);
		MCCommand cmd = new BukkitMCCommand(command);
		return cmd.handleTabComplete(mcsender, alias, args);
	}

	/**
	 * Called when a command registered by this plugin is received.
	 *
	 * @param sender
	 * @param cmd
	 * @param commandLabel
	 * @param args
	 * @return
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		String cmdName = cmd.getName().toLowerCase();
		if((sender.isOp() || (sender instanceof Player && (sender.hasPermission("commandhelper.reloadaliases")
				|| sender.hasPermission("ch.reloadaliases"))))
				&& (cmdName.equals("reloadaliases") || cmdName.equals("reloadalias") || cmdName.equals("recompile"))) {
			MCPlayer player = null;
			if(sender instanceof Player) {
				player = new BukkitMCPlayer((Player) sender);
			}
			ac.reload(player, args, false);
			return true;
		} else if(cmdName.equalsIgnoreCase("commandhelper")) {
			// Despite looking useless, this isn't. It's used for the server events and other
			// places. Look for "commandhelper null" in the codebase.
			return args.length >= 1 && args[0].equalsIgnoreCase("null");
		} else if(cmdName.equals("runalias")) {
			//Hardcoded alias rebroadcast
			if(args.length == 0) {
				return false;
			}
			String command = StringUtils.Join(args, " ");
			if(sender instanceof Player) {
				PlayerCommandPreprocessEvent pcpe = new PlayerCommandPreprocessEvent((Player) sender, command);
				playerListener.onPlayerCommandPreprocess(pcpe);
			} else if(sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender
					|| sender instanceof BlockCommandSender || sender instanceof CommandMinecart) {
				// Console commands and command blocks/minecarts all fire the same event, so pass them to the
				// event handler that would get them if they would not have started with "/runalias".
				if(command.startsWith("/")) {
					command = command.substring(1);
				}
				ServerCommandEvent sce = new ServerCommandEvent(sender, command);
				serverListener.onServerCommand(sce);
			}
			return true;
		} else if(cmdName.equalsIgnoreCase("interpreter-on")) {
			if(sender instanceof ConsoleCommandSender) {
				int interpreterTimeout = Prefs.InterpreterTimeout();
				if(interpreterTimeout != 0) {
					interpreterUnlockedUntil =
							TimeConversionUtil.inMilliseconds(interpreterTimeout, TimeConversionUtil.TimeUnit.MINUTE)
							+ System.currentTimeMillis();
					sender.sendMessage("Interpreter mode unlocked for " + interpreterTimeout + " minute"
							+ (interpreterTimeout == 1 ? "" : "s"));
				}
			} else {
				sender.sendMessage("This command can only be run from console.");
			}
			return true;
		} else if(sender instanceof Player && cmdName.equalsIgnoreCase("interpreter")) {
			if(!sender.hasPermission("commandhelper.interpreter")) {
				sender.sendMessage(MCChatColor.RED + "You do not have permission to run that command");
			} else if(!Prefs.EnableInterpreter()) {
				sender.sendMessage(MCChatColor.RED + "The interpreter is currently disabled."
						+ " Check your preferences file.");
			} else if(Prefs.InterpreterTimeout() != 0 && interpreterUnlockedUntil < System.currentTimeMillis()) {
				sender.sendMessage(MCChatColor.RED + "Interpreter mode is currently locked. Run \"interpreter-on\""
						+ " console to unlock it. If you want to turn this off entirely, set the interpreter-timeout"
						+ " option to 0 in " + CommandHelperFileLocations.getDefault().getPreferencesFile().getName());
			} else {
				interpreterListener.startInterpret(sender.getName());
				sender.sendMessage(MCChatColor.YELLOW + "You are now in interpreter mode. Type a dash (-) on a"
						+ " line by itself to exit, >>> to enter multiline mode, and ~ to clear the environment.");
			}
			return true;
		} else {
			MCCommandSender mcsender = BukkitConvertor.BukkitGetCorrectSender(sender);
			MCCommand mccmd = new BukkitMCCommand(cmd);
			return mccmd.handleCustomCommand(mcsender, commandLabel, args);
		}
	}
}
