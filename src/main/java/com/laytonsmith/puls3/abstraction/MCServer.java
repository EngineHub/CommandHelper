/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.puls3.abstraction;

import java.util.List;


/**
 *
 * @author layton
 */
public interface MCServer {
    public String getName();
    public MCPlayer[] getOnlinePlayers();
    public boolean dispatchCommand(MCCommandSender cs, String string) throws MCCommandException;
    public MCPluginManager getPluginManager();
    public MCPlayer getPlayer(String name);
    public MCWorld getWorld(String name);
    public List<MCWorld> getWorlds();
    public void broadcastMessage(String message);

    public MCOfflinePlayer getOfflinePlayer(String player);
}
