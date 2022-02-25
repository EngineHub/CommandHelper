package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;

/**
 *
 */
public class BukkitMCRemoteConsoleCommandSender extends BukkitMCCommandSender implements MCConsoleCommandSender {

	RemoteConsoleCommandSender ccs;

	public BukkitMCRemoteConsoleCommandSender(RemoteConsoleCommandSender ccs) {
		super(ccs);
		this.ccs = ccs;
	}
}
