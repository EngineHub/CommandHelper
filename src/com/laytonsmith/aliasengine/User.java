/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine;

import com.laytonsmith.Persistance.Persistance;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author layton
 */
public class User {
    Player player;
    Plugin plugin;
    Persistance persist;

    public User(Player player, Plugin plugin){
        this.player = player;
        this.plugin = plugin;
        persist = new Persistance(new File("plugins/CommandHelper/persistance.ser"), plugin);
    }

    public void setLastCommand(String cmd){
        try {
            persist.setValue(new String[]{player.getName(), "lastCommand"}, cmd);
            persist.save();
        } catch (Exception ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getLastCommand(){
        return persist.getValue(new String[]{player.getName(), "lastCommand"}).toString();
    }

    public int addAlias(String alias){
        try {
            ArrayList<Map.Entry> list = persist.getNamespaceValues(new String[]{player.getName(), "aliases"});
            Integer nextValue = 0;
            for (Map.Entry e : list) {
                String[] x = e.getKey().toString().split("\\.");
                Integer thisX = Integer.parseInt(x[x.length - 1]);
                nextValue = Math.max(thisX, nextValue + 1);
            }
            persist.setValue(new String[]{player.getName(), "aliases", nextValue.toString()}, alias);
            persist.save();
            return nextValue;
        } catch (Exception ex) {
            player.sendMessage("Could not add the alias. Please check the error logs for more information.");
            Logger.getLogger("Minecraft").log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    public String getAlias(int id){
        return (String) persist.getValue(new String[]{player.getName(), "aliases", Integer.toString(id)});
    }

    public String getAllAliases(){
        ArrayList<Map.Entry> al = persist.getNamespaceValues(new String[]{player.getName(), "aliases"});
        StringBuilder b = new StringBuilder();
        System.out.println(al);
        for(Map.Entry e : al){

            b.append(e.getKey().toString())
                    .append(":")
                    .append(e.getValue().toString().substring(0, 10))
                    .append("\n");
        }
        return b.toString();
    }

    public int getTotalAliases(){
        return persist.getNamespaceValues(new String[]{player.getName(), "aliases"}).size();
    }


}
