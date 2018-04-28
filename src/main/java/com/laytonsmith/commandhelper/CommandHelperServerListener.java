package com.laytonsmith.commandhelper;

import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.bukkit.BukkitMCBlockCommandSender;
import com.laytonsmith.abstraction.bukkit.BukkitMCCommandSender;
import com.laytonsmith.abstraction.bukkit.BukkitMCConsoleCommandSender;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCCommandMinecart;
import com.laytonsmith.abstraction.bukkit.events.BukkitServerEvents;
import com.laytonsmith.abstraction.enums.MCChatColor;
import com.laytonsmith.core.InternalException;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.events.Driver;
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

public class CommandHelperServerListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onServerCommand(ServerCommandEvent event) {
		// Select the proper CommandSender wrapper.
		MCCommandSender sender;
		if(event.getSender() instanceof ConsoleCommandSender) { // Console.
			sender = new BukkitMCConsoleCommandSender((ConsoleCommandSender) event.getSender());
		} else if(event.getSender() instanceof BlockCommandSender) { // Commandblock blocks.
			sender = new BukkitMCBlockCommandSender((BlockCommandSender) event.getSender());
		} else if(event.getSender() instanceof CommandMinecart) { // Commandblock minecarts.
			sender = new BukkitMCCommandMinecart((CommandMinecart) event.getSender());
		} else { // other CommandSenders.
			sender = new BukkitMCCommandSender(event.getSender());
		}

		BukkitServerEvents.BukkitMCServerCommandEvent cce = new BukkitServerEvents.BukkitMCServerCommandEvent(event, sender);
		EventUtils.TriggerListener(Driver.SERVER_COMMAND, "server_command", cce);
		try {
			if(event.isCancelled()) {
				return;
			}
		} catch(NoSuchMethodError ex) {
			// not cancellable before 1.8.8
		}

		boolean match = false;
		try {
			match = Static.getAliasCore().alias("/" + event.getCommand(), sender);
		} catch(InternalException e) {
			Static.getLogger().log(Level.SEVERE, e.getMessage());
		} catch(ConfigRuntimeException e) {
			Static.getLogger().log(Level.WARNING, e.getMessage());
		} catch(Throwable e) {
			sender.sendMessage(MCChatColor.RED + "Command failed with following reason: " + e.getMessage());
			//Obviously the command is registered, but it somehow failed. Cancel the event.
			e.printStackTrace();
			return;
		}
		//To prevent "unknown console command" error, set the command to the meta command
		//commandhelper null, which just returns true.
		if(match) {
			event.setCommand("commandhelper null");
		}
	}

}
