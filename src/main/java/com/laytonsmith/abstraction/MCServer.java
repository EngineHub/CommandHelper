/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

import java.util.List;
import net.milkbowl.vault.economy.Economy;


/**
 *
 * @author layton
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

    public MCOfflinePlayer getOfflinePlayer(String player);

    /* Boring information get methods -.- */
    public String getServerName();
    public String getModVersion();
    public String getVersion();
    public Boolean getAllowEnd();
    public Boolean getAllowFlight();
    public Boolean getAllowNether();
    public String getWorldContainer();
    public int getMaxPlayers();
    public List<MCOfflinePlayer> getBannedPlayers();
    public List<MCOfflinePlayer> getWhitelistedPlayers();
    public List<MCOfflinePlayer> getOperators();

    public Economy getEconomy();

    public void runasConsole(String cmd);
}
