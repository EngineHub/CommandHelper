package com.laytonsmith.abstraction.bukkit;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import com.laytonsmith.PureUtilities.ReflectionUtils;
import com.laytonsmith.abstraction.MCCommand;
import com.laytonsmith.abstraction.MCCommandMap;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlugin;
import com.laytonsmith.commandhelper.CommandHelperPlugin;

/**
 * 
 * @author jb_aero
 */
public class BukkitMCCommand implements MCCommand {

	Command cmd;
	public BukkitMCCommand(Command command) {
		cmd = command;
	}
	
	public Object getHandle() {
		return cmd;
	}

	public List<String> getAliases() {
		return cmd.getAliases();
	}

	public String getDescription() {
		return cmd.getDescription();
	}

	public String getLabel() {
		return cmd.getLabel();
	}

	public String getName() {
		return cmd.getName();
	}

	public String getPermission() {
		return cmd.getPermission() == null ? null : cmd.getPermission();
	}

	public String getPermissionMessage() {
		return cmd.getPermissionMessage();
	}

	public String getUsage() {
		return cmd.getUsage();
	}

	public MCCommand setAliases(List<String> aliases) {
		cmd.setAliases(aliases);
		return this;
	}

	public MCCommand setDescription(String desc) {
		cmd.setDescription(desc);
		return this;
	}

	public MCCommand setLabel(String name) {
		cmd.setLabel(name);
		return this;
	}

	public MCCommand setPermission(String perm) {
		cmd.setPermission(perm);
		return this;
	}

	public MCCommand setPermissionMessage(String permmsg) {
		cmd.setPermissionMessage(permmsg);
		return this;
	}

	public MCCommand setUsage(String example) {
		cmd.setUsage(example);
		return this;
	}

	public boolean testPermission(MCCommandSender target) {
		return cmd.testPermission(((BukkitMCCommandSender) target)._CommandSender());
	}

	public boolean testPermissionSilent(MCCommandSender target) {
		return cmd.testPermissionSilent(((BukkitMCCommandSender) target)._CommandSender());
	}

	public boolean register(MCCommandMap map) {
		return cmd.register(((BukkitMCCommandMap) map).scm);
	}

	public boolean isRegistered() {
		return cmd.isRegistered();
	}

	public boolean unregister(MCCommandMap map) {
		return cmd.unregister(((BukkitMCCommandMap) map).scm);
	}
	
	public static MCCommand newCommand(String name) {
		return new BukkitMCCommand(ReflectionUtils.newInstance(PluginCommand.class,
				new Class[]{String.class, Plugin.class}, new Object[]{name, CommandHelperPlugin.self}));
	}

	public MCPlugin getPlugin() {
		if (!(cmd instanceof PluginCommand)) {
			return null;
		}
		return ((PluginCommand) cmd).getPlugin() == null ? null : new BukkitMCPlugin(((PluginCommand) cmd).getPlugin());
	}

	public MCPlugin getExecutor() {
		// TODO Not all plugins execute commands in their main class, so this cast won't always work
		if (!(cmd instanceof PluginCommand)) {
			return null;
		}
		return new BukkitMCPlugin((Plugin) ((PluginCommand) cmd).getExecutor());
	}

	public MCPlugin getTabCompleter() {
		// TODO see above
		if (!(cmd instanceof PluginCommand)) {
			return null;
		}
		return new BukkitMCPlugin((Plugin) ((PluginCommand) cmd).getTabCompleter());
	}

	public void setExecutor(MCPlugin plugin) {
		if (cmd instanceof PluginCommand) {
			((PluginCommand) cmd).setExecutor(((BukkitMCPlugin) plugin).getPlugin());
		}
	}

	public void setTabCompleter(MCPlugin plugin) {
		if (cmd instanceof PluginCommand) {
			((PluginCommand) cmd).setTabCompleter(((BukkitMCPlugin) plugin).getPlugin());
		}
	}
	
	@Override
	public int hashCode() {
		return cmd.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return cmd.equals(obj);
	}
	
	@Override
	public String toString() {
		return cmd.toString();
	}
}
