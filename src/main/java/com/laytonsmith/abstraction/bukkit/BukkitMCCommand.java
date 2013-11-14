package com.laytonsmith.abstraction.bukkit;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.MCCommand;
import com.laytonsmith.abstraction.MCCommandMap;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlugin;
import com.laytonsmith.abstraction.bukkit.events.BukkitMiscEvents.BukkitMCCommandTabCompleteEvent;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import com.laytonsmith.core.exceptions.FunctionReturnException;
import com.laytonsmith.core.functions.Commands;

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

	// I may be able to move these to c.l.c.f.Commands.java
	@Override
	public List<String> handleTabComplete(MCCommandSender sender, String alias, String[] args) {
		if (Commands.onTabComplete.containsKey(cmd.getName().toLowerCase())) {
			Target t = Target.UNKNOWN;
			CArray cargs = new CArray(t);
			for (String arg : args) {
				cargs.push(new CString(arg, t));
			}
			try {
				Commands.onTabComplete.get(cmd.getName().toLowerCase()).execute(new Construct[]{
					new CString(alias, t), new CString(sender.getName(), t), cargs,
					new CArray(t) // reserved for an obgen style command array
				});
			} catch (FunctionReturnException e) {
				Construct fret = e.getReturn();
				if (fret instanceof CArray) {
					List<String> ret = new ArrayList<String>();
					for (Construct key : ((CArray) fret).asList()) {
						ret.add(key.val());
					}
					return ret;
				}
			}
		}
		BukkitMCCommandTabCompleteEvent event = new BukkitMCCommandTabCompleteEvent(sender, cmd, alias, args);
		EventUtils.TriggerExternal(event);
		EventUtils.TriggerListener(Driver.TAB_COMPLETE, "tab_complete_command", event);
		return event.getCompletions();
	}
	
	@Override
	public boolean handleCustomCommand(MCCommandSender sender, String label, String[] args) {
		if (Commands.onCommand.containsKey(cmd.getName().toLowerCase())) {
			Target t = Target.UNKNOWN;
			CArray cargs = new CArray(t);
			for (String arg : args) {
				cargs.push(new CString(arg, t));
			}
			try {
				Commands.onCommand.get(cmd.getName().toLowerCase()).execute(new Construct[]{
					new CString(label, t), new CString(sender.getName(), t), cargs,
					new CArray(t) // reserved for an obgen style command array
				});
			} catch (FunctionReturnException e) {
				Construct fret = e.getReturn();
				if (fret instanceof CBoolean) {
					return ((CBoolean) fret).getBoolean();
				}
			}
			return true;
		} else {
			return false;
		}
	}
}
