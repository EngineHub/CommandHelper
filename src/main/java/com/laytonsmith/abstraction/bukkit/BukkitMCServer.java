/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.*;
import java.util.ArrayList;
import java.util.List;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 *
 * @author layton
 */
public class BukkitMCServer implements MCServer{
    
    public static MCServer Get() {
        return new BukkitMCServer();
    }
    Server s;
    
    public BukkitMCServer(){
        this.s = Bukkit.getServer();
    }
    
    public BukkitMCServer(Server serv){
        this.s = serv;
    }
    
    public Server __Server(){
        return s;
    }

    public void broadcastMessage(String message) {
        s.broadcastMessage(message);
    }

    public boolean dispatchCommand(MCCommandSender sender, String command){
        return s.dispatchCommand(new BukkitMCCommandSender(sender).c, command);
    }

    public Boolean getAllowEnd() {
        return s.getAllowEnd();
    }
    
    public Boolean getAllowFlight() {
        return s.getAllowFlight();
    }

    public Boolean getAllowNether() {
        return s.getAllowNether();
    }

    public List<MCOfflinePlayer> getBannedPlayers() {
        if(s.getBannedPlayers() == null){
            return null;
        }
        List<MCOfflinePlayer> list = new ArrayList<MCOfflinePlayer>();
        for(OfflinePlayer p : s.getBannedPlayers()){
            list.add(getOfflinePlayer(p.getName()));
        }
        return list;
    }

    public Economy getEconomy() {
        try{
            RegisteredServiceProvider<Economy> economyProvider = (RegisteredServiceProvider<Economy>)
                    s.getServicesManager().getRegistration(Class.forName("net.milkbowl.vault.economy.Economy"));
            if (economyProvider != null) {
                return economyProvider.getProvider();
            }
        } catch(ClassNotFoundException e){
            //Ignored, it means they don't have Vault installed.
        }
        return null;            
    }
    
    public Object getHandle(){
        return s;
    }

    public int getMaxPlayers() {
        return s.getMaxPlayers();
    }

    /* Boring information get methods -.- */
    public String getModVersion() {
        return s.getBukkitVersion();
    }

    public String getName() {
        return s.getName();
    }

    public MCOfflinePlayer getOfflinePlayer(String player) {
        return new BukkitMCOfflinePlayer(s.getOfflinePlayer(player));
    }

    public MCPlayer[] getOnlinePlayers() {
        if(s.getOnlinePlayers() == null){
            return null;
        }
        Player[] pa = s.getOnlinePlayers();
        MCPlayer[] mcpa = new MCPlayer[pa.length];
        for(int i = 0; i < pa.length; i++){
            mcpa[i] = new BukkitMCPlayer(pa[i]);
        }
        return mcpa;
    }

    public List<MCOfflinePlayer> getOperators() {
        if(s.getOperators() == null){
            return null;
        }
        List<MCOfflinePlayer> list = new ArrayList<MCOfflinePlayer>();
        for(OfflinePlayer p : s.getOperators()){
            list.add(getOfflinePlayer(p.getName()));
        }
        return list;
    }

    public MCPlayer getPlayer(String name) {
        if(s.getPlayer(name) == null){
            return null;
        }
        return new BukkitMCPlayer(s.getPlayer(name));
    }

    public MCPluginManager getPluginManager() {
        if(s.getPluginManager() == null){
            return null;
        }
        return new BukkitMCPluginManager(s.getPluginManager());
    }

    public String getServerName() {
        return s.getServerName();
    }

    public String getVersion() {
        return s.getVersion();
    }

    public List<MCOfflinePlayer> getWhitelistedPlayers() {
        if(s.getBannedPlayers() == null){
            return null;
        }
        List<MCOfflinePlayer> list = new ArrayList<MCOfflinePlayer>();
        for(OfflinePlayer p : s.getWhitelistedPlayers()){
            list.add(getOfflinePlayer(p.getName()));
        }
        return list;
    }

    public MCWorld getWorld(String name) {
        if(s.getWorld(name) == null){
            return null;
        }
        return new BukkitMCWorld(s.getWorld(name));
    }

    public String getWorldContainer() {
        return s.getWorldContainer().getPath();
    }

    public List<MCWorld> getWorlds(){
        if(s.getWorlds() == null){
            return null;
        }
        List<MCWorld> list = new ArrayList<MCWorld>();
        for(World w : s.getWorlds()){
            list.add(new BukkitMCWorld(w));
        }
        return list;
    }

    public void runasConsole(String cmd) {
        s.dispatchCommand(s.getConsoleSender(), cmd);
    }

}
