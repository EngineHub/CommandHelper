package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.abstraction.enums.MCBarColor;
import com.laytonsmith.abstraction.enums.MCBarStyle;
import com.laytonsmith.abstraction.enums.MCInventoryType;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.abstraction.pluginmessages.MCMessenger;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface MCServer extends AbstractionObject {

	String getName();

	Collection<MCPlayer> getOnlinePlayers();

	boolean dispatchCommand(MCCommandSender cs, String string) throws MCCommandException;

	MCPluginManager getPluginManager();

	MCPlayer getPlayer(String name);

	MCPlayer getPlayer(UUID uuid);

	MCEntity getEntity(UUID uuid);

	MCWorld getWorld(String name);

	List<MCWorld> getWorlds();

	/**
	 * Broadcasts a message to all online players and console.
	 * @param message - The message to broadcast.
	 */
	void broadcastMessage(String message);

	/**
	 * Broadcasts a message to all online players with a given permission and console.
	 * @param message - The message to broadcast.
	 * @param permission - The required permission to receive the message.
	 */
	void broadcastMessage(String message, String permission);

	/**
	 * Broadcasts a message to a list of recipients.
	 * {@link MCConsoleCommandSender Console} has to be included in this list to receive the broadcast.
	 * @param message - The message to broadcast.
	 * @param recipients - A list of {@link MCCommandSender command senders} to send the message to.
	 */
	void broadcastMessage(String message, Set<MCCommandSender> recipients);

	MCConsoleCommandSender getConsole();

	MCItemFactory getItemFactory();

	MCCommandMap getCommandMap();

	MCInventory createInventory(MCInventoryHolder owner, MCInventoryType type, String title);

	MCInventory createInventory(MCInventoryHolder owner, int size, String title);

	/**
	 * Provides access to local user data associated with a name. Depending on the implementation, a web lookup with the
	 * official API may or may not be performed.
	 *
	 * @param player The name to lookup
	 * @return An object containing any info that can be accessed regardless of a connected player.
	 */
	MCOfflinePlayer getOfflinePlayer(String player);

	/**
	 * Provides access to local user data associated with a UUID. Depending on the implementation, a web lookup with the
	 * official API may or may not be performed.
	 *
	 * @param uuid The UUID to lookup
	 * @return An object containing any info that can be accessed regardless of a connected player.
	 */
	MCOfflinePlayer getOfflinePlayer(UUID uuid);

	MCOfflinePlayer[] getOfflinePlayers();

	/* Boring information get methods -.- */
	String getServerName();

	String getMotd();

	String getAPIVersion();

	String getServerVersion();

	MCVersion getMinecraftVersion();

	int getPort();

	String getIp();

	boolean getAllowEnd();

	boolean getAllowFlight();

	boolean getAllowNether();

	boolean getOnlineMode();

	int getViewDistance();

	String getWorldContainer();

	int getMaxPlayers();

	List<MCOfflinePlayer> getBannedPlayers();

	List<MCOfflinePlayer> getWhitelistedPlayers();

	List<MCOfflinePlayer> getOperators();

	void banName(String name, String reason, String source);

	void unbanName(String name);

	void banIP(String address);

	Set<String> getIPBans();

	void unbanIP(String address);

	MCScoreboard getMainScoreboard();

	MCScoreboard getNewScoreboard();

	void runasConsole(String cmd);

	MCMessenger getMessenger();

	boolean unloadWorld(MCWorld world, boolean save);

	boolean addRecipe(MCRecipe recipe);

	List<MCRecipe> getRecipesFor(MCItemStack result);

	List<MCRecipe> allRecipes();

	void clearRecipes();

	void resetRecipes();

	void savePlayers();

	void shutdown();

	/**
	 * Dispatches a command like {@link #dispatchCommand(com.laytonsmith.abstraction.MCCommandSender, java.lang.String)
	 * }, but attempts to capture the output of the command, and returns that.
	 *
	 * @param commandSender The command sender
	 * @param cmd The command
	 * @return The command's captured output, if possible, otherwise an empty string, never null.
	 */
	String dispatchAndCaptureCommand(MCCommandSender commandSender, String cmd);

	MCBossBar createBossBar(String title, MCBarColor color, MCBarStyle style);

	MCBlockData createBlockData(String data);
}
