package com.laytonsmith.abstraction.bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.MCCommand;
import com.laytonsmith.abstraction.MCCommandMap;

/**
 * 
 * @author jb_aero
 */
public class BukkitMCCommandMap implements MCCommandMap {

	SimpleCommandMap scm;
	public BukkitMCCommandMap(SimpleCommandMap invokeMethod) {
		scm = invokeMethod;
	}

	public Object getHandle() {
		return scm;
	}

	public void clearCommands() {
		scm.clearCommands();
	}
	
	public boolean isCommand(String name) {
		return scm.getCommand(name) != null;
	}

	public MCCommand getCommand(String name) {
		return scm.getCommand(name) == null ? null : new BukkitMCCommand(scm.getCommand(name));
	}

	public List<MCCommand> getCommands() {
		List<MCCommand> cmds = new ArrayList<MCCommand>();
		for (Command c : scm.getCommands()) {
			cmds.add(new BukkitMCCommand(c));
		}
		return cmds;
	}

	public boolean register(String fallback, MCCommand cmd) {
		return scm.register(fallback, ((BukkitMCCommand) cmd).cmd);
	}

	public boolean register(String label, String fallback, MCCommand cmd) {
		return scm.register(label, fallback, ((BukkitMCCommand) cmd).cmd);
	}

	@SuppressWarnings("unchecked")
	public boolean unregister(MCCommand cmd) {
		if (cmd.isRegistered()) {
			((Map<String,Command>) ReflectionUtils.get(scm.getClass(), scm, "knownCommands")).remove(cmd.getName());
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
