package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.MCCommand;
import com.laytonsmith.abstraction.MCCommandMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.laytonsmith.commandhelper.CommandHelperPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;

public class BukkitMCCommandMap implements MCCommandMap {

	private static boolean synced = true;

	SimpleCommandMap scm;

	public BukkitMCCommandMap(SimpleCommandMap invokeMethod) {
		scm = invokeMethod;
	}

	@Override
	public Object getHandle() {
		return scm;
	}

	/**
	 * Syncs the command list with players and other command senders that use the vanilla dispatcher.
	 */
	private static void SyncCommands() {
		if(CommandHelperPlugin.self.isFirstLoad()) {
			// Craftbukkit already syncs after plugins are enabled, so we don't have to.
			return;
		}
		if(synced) {
			Bukkit.getScheduler().runTask(CommandHelperPlugin.self, () -> {
				Server s = Bukkit.getServer();
				ReflectionUtils.invokeMethod(s.getClass(), s, "syncCommands");
				synced = true;
			});
		}
		synced = false;
	}

	@Override
	public void clearCommands() {
		scm.clearCommands();
		SyncCommands();
	}

	@Override
	public boolean isCommand(String name) {
		return scm.getCommand(name) != null;
	}

	@Override
	public MCCommand getCommand(String name) {
		Command cmd = scm.getCommand(name);
		if(cmd == null) {
			return null;
		}
		return new BukkitMCCommand(cmd);
	}

	@Override
	public List<MCCommand> getCommands() {
		List<MCCommand> cmds = new ArrayList<>();
		for(Command c : scm.getCommands()) {
			cmds.add(new BukkitMCCommand(c));
		}
		return cmds;
	}

	@Override
	public boolean register(String fallback, MCCommand cmd) {
		boolean success = scm.register(fallback, ((BukkitMCCommand) cmd).cmd);
		SyncCommands();
		return success;
	}

	@Override
	public boolean register(String label, String fallback, MCCommand cmd) {
		boolean success = scm.register(label, fallback, ((BukkitMCCommand) cmd).cmd);
		SyncCommands();
		return success;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean unregister(MCCommand cmd) {
		if(cmd.isRegistered()) {
			((Map<String, Command>) ReflectionUtils.invokeMethod(scm.getClass(), scm, "getKnownCommands")).remove(cmd.getName());
			SyncCommands();
			return cmd.unregister(this);
		} else {
			return false;
		}
	}

	@Override
	public boolean equals(Object obj) {
		return scm.equals(obj);
	}

	@Override
	public int hashCode() {
		return scm.hashCode();
	}

	@Override
	public String toString() {
		return scm.toString();
	}
}
