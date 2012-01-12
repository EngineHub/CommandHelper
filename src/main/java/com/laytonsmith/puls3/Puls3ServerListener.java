/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.puls3;

import com.laytonsmith.puls3.core.InternalException;
import com.laytonsmith.puls3.core.Script;
import com.laytonsmith.puls3.core.Static;
import com.laytonsmith.puls3.core.exceptions.ConfigRuntimeException;
import java.util.ArrayList;
import java.util.logging.Level;
import com.laytonsmith.puls3.abstraction.MCChatColor;
import com.laytonsmith.puls3.abstraction.MCCommandSender;
import com.laytonsmith.puls3.abstraction.bukkit.BukkitMCCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.ServerListener;

/**
 *
 * @author Layton
 */
public class Puls3ServerListener extends ServerListener{
    
    @Override
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
