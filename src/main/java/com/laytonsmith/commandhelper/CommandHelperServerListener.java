/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.commandhelper;

import com.laytonsmith.abstraction.MCChatColor;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.bukkit.BukkitMCCommandSender;
import com.laytonsmith.core.InternalException;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import java.util.ArrayList;
import java.util.logging.Level;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;

/**
 *
 * @author Layton
 */
public class CommandHelperServerListener implements Listener{
    
    @EventHandler(priority= EventPriority.LOWEST)
    public void onServerCommand(ServerCommandEvent event){
        MCCommandSender player = new BukkitMCCommandSender(event.getSender());
        boolean match = false;
        try {
            match = Static.getAliasCore().alias("/" + event.getCommand(), player, new ArrayList<Script>());
        } catch (InternalException e) {
            Static.getLogger().log(Level.SEVERE, e.getMessage());
        } catch (ConfigRuntimeException e) {
            Static.getLogger().log(Level.WARNING, e.getMessage());
        } catch (Throwable e) {
            player.sendMessage(MCChatColor.RED + "Command failed with following reason: " + e.getMessage());
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
