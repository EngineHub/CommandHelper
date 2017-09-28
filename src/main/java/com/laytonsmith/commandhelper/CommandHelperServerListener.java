

package com.laytonsmith.commandhelper;

import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.bukkit.BukkitMCBlockCommandSender;
import com.laytonsmith.abstraction.bukkit.BukkitMCCommandSender;
import com.laytonsmith.abstraction.bukkit.BukkitMCConsoleCommandSender;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCCommandMinecart;
import com.laytonsmith.abstraction.bukkit.events.BukkitMiscEvents;
import com.laytonsmith.abstraction.enums.MCChatColor;
import com.laytonsmith.core.InternalException;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.events.EventUtils;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import java.util.logging.Level;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;

/**
 *
 * 
 */
public class CommandHelperServerListener implements Listener{
    
    @EventHandler(priority= EventPriority.LOWEST)
    public void onServerCommand(ServerCommandEvent event){
		//Run this first, so external events can intercept it.
		BukkitMiscEvents.BukkitMCConsoleCommandEvent cce = new BukkitMiscEvents.BukkitMCConsoleCommandEvent(event);
		EventUtils.TriggerExternal(cce);
		
		// Select the proper CommandSender wrapper.
        MCCommandSender player;
        if(event.getSender() instanceof ConsoleCommandSender){ // Console.
            player = new BukkitMCConsoleCommandSender((ConsoleCommandSender)event.getSender());
        } else if(event.getSender() instanceof BlockCommandSender){ // Commandblock blocks.
            player = new BukkitMCBlockCommandSender((BlockCommandSender)event.getSender());
        } else if(event.getSender() instanceof CommandMinecart) { // Commandblock minecarts.
            player = new BukkitMCCommandMinecart((CommandMinecart) event.getSender());
        } else { // Players or unknown CommandSenders.
			player = new BukkitMCCommandSender(event.getSender());
		}
        boolean match = false;
        try {
            match = Static.getAliasCore().alias("/" + event.getCommand(), player);
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
