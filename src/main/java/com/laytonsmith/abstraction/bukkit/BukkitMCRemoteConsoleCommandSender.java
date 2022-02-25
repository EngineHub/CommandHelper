package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCRemoteCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;

/**
 *
 */
public class BukkitMCRemoteConsoleCommandSender extends BukkitMCCommandSender implements MCRemoteCommandSender {

	RemoteConsoleCommandSender ccs;

	public BukkitMCRemoteConsoleCommandSender(RemoteConsoleCommandSender ccs) {
		super(ccs);
		this.ccs = ccs;
	}
}
