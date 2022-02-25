package com.laytonsmith.commandhelper;

import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.bukkit.events.BukkitServerEvents;
import com.laytonsmith.abstraction.enums.MCChatColor;
import com.laytonsmith.core.InternalException;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.RemoteServerCommandEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.logging.Level;

public class CommandHelperServerListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onServerCommand(ServerCommandEvent event) {
		processServerCommand(event);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onRemoteCommand(RemoteServerCommandEvent event) {
		processServerCommand(event);
	}

	private void processServerCommand(ServerCommandEvent event) {
		MCCommandSender sender = BukkitConvertor.BukkitGetCorrectSender(event.getSender());

		Environment env = CommandHelperPlugin.getCore().getLastLoadedEnv();
		BukkitServerEvents.BukkitMCServerCommandEvent cce = new BukkitServerEvents.BukkitMCServerCommandEvent(event, sender);
		EventUtils.TriggerListener(Driver.SERVER_COMMAND, "server_command", cce, env);
		if(event.isCancelled()) {
			return;
		}

		boolean match = false;
		try {
			match = Static.getAliasCore().alias("/" + event.getCommand(), sender);
		} catch (InternalException e) {
			Static.getLogger().log(Level.SEVERE, e.getMessage());
		} catch (ConfigRuntimeException e) {
			Static.getLogger().log(Level.WARNING, e.getMessage());
		} catch (Throwable e) {
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
