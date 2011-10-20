/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sk89q.commandhelper;

import com.laytonsmith.aliasengine.InternalException;
import com.laytonsmith.aliasengine.Script;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.exceptions.ConfigRuntimeException;
import java.util.ArrayList;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.ServerListener;

/**
 *
 * @author Layton
 */
public class CommandHelperServerListener extends ServerListener{
    
    @Override
    public void onServerCommand(ServerCommandEvent event){
        CommandSender player = event.getSender();
        boolean match = false;
        try {
            match = Static.getAliasCore().alias("/" + event.getCommand(), player, new ArrayList<Script>());
        } catch (InternalException e) {
            Static.getLogger().log(Level.SEVERE, e.getMessage());
        } catch (ConfigRuntimeException e) {
            Static.getLogger().log(Level.WARNING, e.getMessage());
        } catch (Throwable e) {
            player.sendMessage(ChatColor.RED + "Command failed with following reason: " + e.getMessage());
            //Obviously the command is registered, but it somehow failed. Cancel the event.
            e.printStackTrace();
            return;
        }
        //To prevent "unknown console command" error, set the command to the meta command
        //commandhelper null, which just returns true.
        if(match){
            event.setCommand("commandhelper null");
        }
    }
    
}
