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
    public void broadcastMessage(String message);
    public boolean dispatchCommand(MCCommandSender cs, String string) throws MCCommandException;
    public Boolean getAllowEnd();
    public Boolean getAllowFlight();
    public Boolean getAllowNether();
    public List<MCOfflinePlayer> getBannedPlayers();
    public Economy getEconomy();
    public int getMaxPlayers();

    public String getModVersion();

    public String getName();
    public MCOfflinePlayer getOfflinePlayer(String player);
    public MCPlayer[] getOnlinePlayers();
    public List<MCOfflinePlayer> getOperators();
    public MCPlayer getPlayer(String name);
    public MCPluginManager getPluginManager();
    /* Boring information get methods -.- */
    public String getServerName();
    public String getVersion();
    public List<MCOfflinePlayer> getWhitelistedPlayers();
    public MCWorld getWorld(String name);
    public String getWorldContainer();

    public List<MCWorld> getWorlds();

    public void runasConsole(String cmd);
}
