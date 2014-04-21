package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCInventoryType;
import com.laytonsmith.abstraction.pluginmessages.MCMessenger;
import java.util.List;
import java.util.Set;
import net.milkbowl.vault.economy.Economy;


/**
 *
 * 
 */
public interface MCServer extends AbstractionObject{
    public String getName();
    public MCPlayer[] getOnlinePlayers();
    public boolean dispatchCommand(MCCommandSender cs, String string) throws MCCommandException;
    public MCPluginManager getPluginManager();
    public MCPlayer getPlayer(String name);
    public MCWorld getWorld(String name);
    public List<MCWorld> getWorlds();
    public void broadcastMessage(String message);
	public void broadcastMessage(String message, String permission);
	public MCConsoleCommandSender getConsole();
	public MCItemFactory getItemFactory();
	public MCCommandMap getCommandMap();
	public MCInventory createInventory(MCInventoryHolder owner, MCInventoryType type);
	public MCInventory createInventory(MCInventoryHolder owner, int size, String title);
	public MCInventory createInventory(MCInventoryHolder owner, int size);

    public MCOfflinePlayer getOfflinePlayer(String player);
	public MCOfflinePlayer[] getOfflinePlayers();

    /* Boring information get methods -.- */
    public String getServerName();
    public String getModVersion();
    public String getVersion();
	public int getPort();
    public Boolean getAllowEnd();
    public Boolean getAllowFlight();
    public Boolean getAllowNether();
    public Boolean getOnlineMode();
    public String getWorldContainer();
    public int getMaxPlayers();
    public List<MCOfflinePlayer> getBannedPlayers();
    public List<MCOfflinePlayer> getWhitelistedPlayers();
    public List<MCOfflinePlayer> getOperators();

	public void banIP(String address);
	public Set<String> getIPBans();
	public void unbanIP(String address);

	public MCScoreboard getMainScoreboard();
	public MCScoreboard getNewScoreboard();

    public Economy getEconomy();

    public void runasConsole(String cmd);
	public MCMessenger getMessenger();

	public boolean unloadWorld(MCWorld world, boolean save);
	
	public boolean addRecipe(MCRecipe recipe);
	public List<MCRecipe> getRecipesFor(MCItemStack result);
	public List<MCRecipe> allRecipes();
	public void clearRecipes();
	public void resetRecipes();
	

	public void shutdown();
	
	/**
	 * Dispatches a command like {@link #dispatchCommand(com.laytonsmith.abstraction.MCCommandSender, java.lang.String) }, but
	 * attempts to capture the output of the command, and returns that. This ends up being very custom and low level code
	 * per server, so most of the implementation is left to the abstraction layer.
	 * @param commandSender The command sender
	 * @param cmd The command
	 * @return The command's captured output, if possible, otherwise an empty string, never null.
	 */
	public String dispatchAndCaptureCommand(MCCommandSender commandSender, String cmd);
}
