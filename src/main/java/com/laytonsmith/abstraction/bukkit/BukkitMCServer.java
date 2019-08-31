package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.MCBossBar;
import com.laytonsmith.abstraction.MCCommandMap;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCConsoleCommandSender;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCInventoryHolder;
import com.laytonsmith.abstraction.MCItemFactory;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCMerchant;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCPluginManager;
import com.laytonsmith.abstraction.MCRecipe;
import com.laytonsmith.abstraction.MCScoreboard;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlockData;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPlayer;
import com.laytonsmith.abstraction.bukkit.pluginmessages.BukkitMCMessenger;
import com.laytonsmith.abstraction.enums.MCBarColor;
import com.laytonsmith.abstraction.enums.MCBarStyle;
import com.laytonsmith.abstraction.enums.MCInventoryType;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.abstraction.pluginmessages.MCMessenger;
import com.laytonsmith.core.Static;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.Recipe;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BukkitMCServer implements MCServer {

	Server s;
	MCVersion version;

	public BukkitMCServer() {
		this.s = Bukkit.getServer();
	}

	public BukkitMCServer(Server server) {
		this.s = server;
	}

	@Override
	public Object getHandle() {
		return s;
	}

	public Server __Server() {
		return s;
	}

	@Override
	public String getName() {
		return s.getName();
	}

	@Override
	public Collection<MCPlayer> getOnlinePlayers() {
		Collection<? extends Player> players = s.getOnlinePlayers();
		Set<MCPlayer> mcpa = new HashSet<>();
		for(Player p : players) {
			mcpa.add(new BukkitMCPlayer(p));
		}
		return mcpa;
	}

	public static MCServer Get() {
		return new BukkitMCServer();
	}

	@Override
	public boolean dispatchCommand(MCCommandSender sender, String command) {
		CommandSender cs;
		if(sender instanceof MCPlayer) {
			cs = (Player) sender.getHandle();
		} else {
			cs = (CommandSender) sender.getHandle();
		}
		return s.dispatchCommand(cs, command);
	}

	private class CommandSenderInterceptor implements InvocationHandler {

		private final StringBuilder buffer;
		private final CommandSender sender;

		public CommandSenderInterceptor(CommandSender sender) {
			this.buffer = new StringBuilder();
			this.sender = sender;
		}

		@Override
		public Object invoke(Object o, Method method, Object[] args) throws Throwable {
			if("sendMessage".equals(method.getName())) {
				buffer.append(args[0].toString());
				return Void.TYPE;
			} else {
				return method.invoke(sender, args);
			}
		}

		public String getBuffer() {
			return buffer.toString();
		}
	}

	@Override
	public String dispatchAndCaptureCommand(MCCommandSender commandSender, String cmd) {
		// Grab the CommandSender object from the abstraction layer
		CommandSender sender = (CommandSender) commandSender.getHandle();

		// Create the interceptor
		CommandSenderInterceptor interceptor = new CommandSenderInterceptor(sender);

		// Create a new proxy and abstraction layer wrapper around the proxy
		CommandSender newCommandSender = (CommandSender) Proxy.newProxyInstance(BukkitMCServer.class.getClassLoader(), new Class[]{CommandSender.class}, interceptor);
		BukkitMCCommandSender aCommandSender = new BukkitMCCommandSender(newCommandSender);

		MCCommandSender oldSender = Static.UninjectPlayer(commandSender);
		// Inject our new wrapped object
		Static.InjectPlayer(aCommandSender);

		// Dispatch the command now
		try {
			s.dispatchCommand(newCommandSender, cmd);
		} finally {
			// Clean up
			Static.UninjectPlayer(aCommandSender);
			if(oldSender != null) {
				Static.InjectPlayer(oldSender);
			}
		}

		// Return the buffered text (if any)
		return interceptor.getBuffer();
	}

	@Override
	public MCPluginManager getPluginManager() {
		if(s.getPluginManager() == null) {
			return null;
		}
		return new BukkitMCPluginManager(s.getPluginManager());
	}

	@Override
	@SuppressWarnings("deprecation")
	public MCPlayer getPlayerExact(String name) {
		Player p = s.getPlayerExact(name);
		if(p == null) {
			return null;
		}
		return new BukkitMCPlayer(p);
	}

	@Override
	@SuppressWarnings("deprecation")
	public MCPlayer getPlayer(String name) {
		Player p = s.getPlayer(name);
		if(p == null) {
			return null;
		}
		return new BukkitMCPlayer(p);
	}

	@Override
	public MCPlayer getPlayer(UUID uuid) {
		Player p = s.getPlayer(uuid);
		if(p == null) {
			return null;
		}
		return new BukkitMCPlayer(p);
	}

	@Override
	public MCEntity getEntity(UUID uuid) {
		return BukkitConvertor.BukkitGetCorrectEntity(s.getEntity(uuid));
	}

	@Override
	public MCWorld getWorld(String name) {
		World w = s.getWorld(name);
		if(w == null) {
			return null;
		}
		return new BukkitMCWorld(w);
	}

	@Override
	public List<MCWorld> getWorlds() {
		List<MCWorld> list = new ArrayList<>();
		for(World w : s.getWorlds()) {
			list.add(new BukkitMCWorld(w));
		}
		return list;
	}

	@Override
	public void broadcastMessage(String message) {

		// Get the set of online players and include console.
		Set<CommandSender> recipients = new HashSet<>(this.s.getOnlinePlayers());
		recipients.add(this.s.getConsoleSender());

		// Perform the broadcast.
		this.bukkitBroadcastMessage(message, recipients);
	}

	@Override
	public void broadcastMessage(String message, String permission) {

		// Get the set of online players with the given permission and include console.
		Set<CommandSender> recipients = new HashSet<>();
		for(Player player : this.s.getOnlinePlayers()) {
			if(player.hasPermission(permission)) {
				recipients.add(player);
			}
		}
		recipients.add(this.s.getConsoleSender());

		// Perform the broadcast.
		this.bukkitBroadcastMessage(message, recipients);
	}

	@Override
	public void broadcastMessage(String message, Set<MCCommandSender> recipients) {

		// Convert MCCommandsSender recipients to CommandSender recipients.
		Set<CommandSender> bukkitRecipients = new HashSet<>();
		if(recipients != null) {
			for(MCCommandSender recipient : recipients) {
				bukkitRecipients.add((CommandSender) recipient.getHandle());
			}
		}

		// Perform the broadcast.
		this.bukkitBroadcastMessage(message, bukkitRecipients);
	}

	/**
	 * Broadcasts a message to a list of recipients, fireing a {@link BroadcastMessageEvent} before doing so.
	 * {@link ConsoleCommandSender Console} has to be included in this list to receive the broadcast.
	 * @param message - The message to broadcast.
	 * @param recipients - A list of {@link MCCommandSender command senders} to send the message to.
	 * @return The amount of recipients that received the message.
	 */
	@SuppressWarnings("deprecation")
	private int bukkitBroadcastMessage(String message, Set<CommandSender> recipients) {

		// Fire a BroadcastMessageEvent for this broadcast.
		BroadcastMessageEvent broadcastMessageEvent;
		if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_14)) {
			broadcastMessageEvent = new BroadcastMessageEvent(!Bukkit.isPrimaryThread(), message, recipients);
		} else {
			broadcastMessageEvent = new BroadcastMessageEvent(message, recipients);
		}
		this.s.getPluginManager().callEvent(broadcastMessageEvent);

		// Return if the event was cancelled.
		if(broadcastMessageEvent.isCancelled()) {
			return 0;
		}

		// Get the possibly modified message and recipients.
		message = broadcastMessageEvent.getMessage();
		recipients = broadcastMessageEvent.getRecipients(); // This returns the same reference, but breaks less likely.

		// Perform the actual broadcast to all remaining recipients.
		for(CommandSender recipient : recipients) {
			recipient.sendMessage(message);
		}

		// Return the amount of recipients that received the message.
		return recipients.size();
	}

	@Override
	public MCConsoleCommandSender getConsole() {
		return new BukkitMCConsoleCommandSender(s.getConsoleSender());
	}

	@Override
	public MCItemFactory getItemFactory() {
		return new BukkitMCItemFactory(s.getItemFactory());
	}

	@Override
	public MCCommandMap getCommandMap() {
		return new BukkitMCCommandMap((SimpleCommandMap) ReflectionUtils.invokeMethod(s.getClass(), s, "getCommandMap"));
	}

	@Override
	public MCOfflinePlayer getOfflinePlayer(String player) {
		return new BukkitMCOfflinePlayer(s.getOfflinePlayer(player));
	}

	@Override
	public MCOfflinePlayer getOfflinePlayer(UUID uuid) {
		return new BukkitMCOfflinePlayer(s.getOfflinePlayer(uuid));
	}

	@Override
	public MCOfflinePlayer[] getOfflinePlayers() {
		OfflinePlayer[] offp = s.getOfflinePlayers();
		MCOfflinePlayer[] mcoff = new MCOfflinePlayer[offp.length];
		for(int i = 0; i < offp.length; i++) {
			mcoff[i] = new BukkitMCOfflinePlayer(offp[i]);
		}
		return mcoff;
	}

	/* Boring information get methods -.- */
	@Override
	public String getAPIVersion() {
		return s.getBukkitVersion();
	}

	@Override
	public String getServerVersion() {
		return s.getVersion();
	}

	@Override
	public MCVersion getMinecraftVersion() {
		if(version == null) {
			int temp = s.getBukkitVersion().indexOf('-');
			version = MCVersion.match(s.getBukkitVersion().substring(0, temp).split("\\."));
		}
		return version;
	}

	@Override
	public int getPort() {
		return s.getPort();
	}

	@Override
	public String getIp() {
		return s.getIp();
	}

	@Override
	public boolean getAllowEnd() {
		return s.getAllowEnd();
	}

	@Override
	public boolean getAllowFlight() {
		return s.getAllowFlight();
	}

	@Override
	public boolean getAllowNether() {
		return s.getAllowNether();
	}

	@Override
	public boolean getOnlineMode() {
		return s.getOnlineMode();
	}

	@Override
	public int getViewDistance() {
		return s.getViewDistance();
	}

	@Override
	public String getWorldContainer() {
		return s.getWorldContainer().getPath();
	}

	@Override
	public String getServerName() {
		if(Static.getServer().getMinecraftVersion().lt(MCVersion.MC1_14)) {
			return (String) ReflectionUtils.invokeMethod(Server.class, s, "getServerName");
		}
		return "";
	}

	@Override
	public String getMotd() {
		return s.getMotd();
	}

	@Override
	public int getMaxPlayers() {
		return s.getMaxPlayers();
	}

	@Override
	public List<MCOfflinePlayer> getBannedPlayers() {
		List<MCOfflinePlayer> list = new ArrayList<>();
		for(OfflinePlayer p : s.getBannedPlayers()) {
			list.add(new BukkitMCOfflinePlayer(p));
		}
		return list;
	}

	@Override
	public List<MCOfflinePlayer> getWhitelistedPlayers() {
		List<MCOfflinePlayer> list = new ArrayList<>();
		for(OfflinePlayer p : s.getWhitelistedPlayers()) {
			list.add(new BukkitMCOfflinePlayer(p));
		}
		return list;
	}

	@Override
	public List<MCOfflinePlayer> getOperators() {
		List<MCOfflinePlayer> list = new ArrayList<>();
		for(OfflinePlayer p : s.getOperators()) {
			list.add(new BukkitMCOfflinePlayer(p));
		}
		return list;
	}

	@Override
	public void runasConsole(String cmd) {
		s.dispatchCommand(s.getConsoleSender(), cmd);
	}

	@Override
	public String toString() {
		return s.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof BukkitMCServer && s.equals(((BukkitMCServer) obj).s));
	}

	@Override
	public int hashCode() {
		return s.hashCode();
	}

	@Override
	public MCInventory createInventory(MCInventoryHolder holder, MCInventoryType type, String title) {
		InventoryHolder ih;
		if(holder == null) {
			ih = null;
		} else {
			ih = (InventoryHolder) holder.getHandle();
		}
		if(title == null) {
			return new BukkitMCInventory(Bukkit.createInventory(ih, InventoryType.valueOf(type.name())));
		}
		return new BukkitMCInventory(Bukkit.createInventory(ih, InventoryType.valueOf(type.name()), title));
	}

	@Override
	public MCInventory createInventory(MCInventoryHolder holder, int size, String title) {
		InventoryHolder ih;
		if(holder == null) {
			ih = null;
		} else {
			ih = (InventoryHolder) holder.getHandle();
		}
		if(title == null) {
			return new BukkitMCInventory(Bukkit.createInventory(ih, size));
		}
		return new BukkitMCInventory(Bukkit.createInventory(ih, size, title));
	}

	@Override
	public void banName(String name, String reason, String source) {
		s.getBanList(BanList.Type.NAME).addBan(name, reason, null, source);
	}

	@Override
	public void unbanName(String name) {
		s.getBanList(BanList.Type.NAME).pardon(name);
	}

	@Override
	public void banIP(String address) {
		s.banIP(address);
	}

	@Override
	public Set<String> getIPBans() {
		return s.getIPBans();
	}

	@Override
	public void unbanIP(String address) {
		s.unbanIP(address);
	}

	@Override
	public MCMessenger getMessenger() {
		return new BukkitMCMessenger(s.getMessenger());
	}

	@Override
	public MCScoreboard getMainScoreboard() {
		return new BukkitMCScoreboard(s.getScoreboardManager().getMainScoreboard());
	}

	@Override
	public MCScoreboard getNewScoreboard() {
		return new BukkitMCScoreboard(s.getScoreboardManager().getNewScoreboard());
	}

	@Override
	public boolean unloadWorld(MCWorld world, boolean save) {
		return s.unloadWorld(((BukkitMCWorld) world).__World(), save);
	}

	@Override
	public void savePlayers() {
		s.savePlayers();
	}

	@Override
	public void shutdown() {
		s.shutdown();
	}

	@Override
	public boolean addRecipe(MCRecipe recipe) {
		return s.addRecipe((Recipe) recipe.getHandle());
	}

	@Override
	public List<MCRecipe> getRecipesFor(MCItemStack result) {
		List<MCRecipe> ret = new ArrayList<>();
		List<Recipe> recipes = s.getRecipesFor(((BukkitMCItemStack) result).__ItemStack());
		for(Recipe recipe : recipes) {
			ret.add(BukkitConvertor.BukkitGetRecipe(recipe));
		}
		return ret;
	}

	@Override
	public List<MCRecipe> allRecipes() {
		List<MCRecipe> ret = new ArrayList<>();
		for(Iterator<Recipe> recipes = s.recipeIterator(); recipes.hasNext();) {
			Recipe recipe = recipes.next();
			ret.add(BukkitConvertor.BukkitGetRecipe(recipe));
		}
		return ret;
	}

	@Override
	public void clearRecipes() {
		s.clearRecipes();
	}

	@Override
	public void resetRecipes() {
		s.resetRecipes();
	}

	@Override
	public MCBossBar createBossBar(String title, MCBarColor color, MCBarStyle style) {
		return new BukkitMCBossBar(s.createBossBar(title, BarColor.valueOf(color.name()), BarStyle.valueOf(style.name())));
	}

	@Override
	public MCBlockData createBlockData(String data) {
		return new BukkitMCBlockData(s.createBlockData(data));
	}

	@Override
	public MCMerchant createMerchant(String title) {
		return new BukkitMCMerchant(__Server().createMerchant(title), title);
	}
}
