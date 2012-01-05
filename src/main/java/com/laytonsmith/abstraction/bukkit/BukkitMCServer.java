/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCAnimalTamer;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCPluginManager;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.MCWorld;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 *
 * @author layton
 */
public class BukkitMCServer implements MCServer{
    
    Server s;
    public BukkitMCServer(){
        this.s = Bukkit.getServer();
    }
    
    public Server __Server(){
        return s;
    }

    @Override
    public String getName() {
        return s.getName();
    }

    @Override
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

    public static MCServer Get() {
        return new BukkitMCServer();
    }
    
    public boolean dispatchCommand(MCCommandSender sender, String command){
        return s.dispatchCommand(((BukkitMCCommandSender)sender).c, command);
    }

    public MCPluginManager getPluginManager() {
        if(s.getPluginManager() == null){
            return null;
        }
        return new BukkitMCPluginManager(s.getPluginManager());
    }

    public MCPlayer getPlayer(String name) {
        if(s.getPlayer(name) == null){
            return null;
        }
        return new BukkitMCPlayer(s.getPlayer(name));
    }

    public MCWorld getWorld(String name) {
        if(s.getWorld(name) == null){
            return null;
        }
        return new BukkitMCWorld(s.getWorld(name));
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

    public void broadcastMessage(String message) {
        s.broadcastMessage(message);
    }

    public MCOfflinePlayer getOfflinePlayer(String player) {
        return new BukkitMCOfflinePlayer(s.getOfflinePlayer(player));
    }

}
