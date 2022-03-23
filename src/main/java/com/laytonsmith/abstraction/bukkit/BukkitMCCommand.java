package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.abstraction.MCCommand;
import com.laytonsmith.abstraction.MCCommandMap;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlugin;
import com.laytonsmith.abstraction.bukkit.events.BukkitMiscEvents.BukkitMCCommandTabCompleteEvent;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import com.laytonsmith.core.exceptions.CRE.CREPluginInternalException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Commands;
import com.laytonsmith.core.natives.interfaces.Mixed;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class BukkitMCCommand implements MCCommand {

	Command cmd;

	public BukkitMCCommand(Command command) {
		cmd = command;
	}

	@Override
	public Object getHandle() {
		return cmd;
	}

	@Override
	public List<String> getAliases() {
		return cmd.getAliases();
	}

	@Override
	public String getDescription() {
		return cmd.getDescription();
	}

	@Override
	public String getLabel() {
		return cmd.getLabel();
	}

	@Override
	public String getName() {
		return cmd.getName();
	}

	@Override
	public String getPermission() {
		return cmd.getPermission();
	}

	@Override
	public String getPermissionMessage() {
		return cmd.getPermissionMessage();
	}

	@Override
	public String getUsage() {
		return cmd.getUsage();
	}

	@Override
	public MCCommand setAliases(List<String> aliases) {
		cmd.setAliases(aliases);
		return this;
	}

	@Override
	public MCCommand setDescription(String desc) {
		cmd.setDescription(desc);
		return this;
	}

	@Override
	public MCCommand setLabel(String name) {
		cmd.setLabel(name);
		return this;
	}

	@Override
	public MCCommand setPermission(String perm) {
		cmd.setPermission(perm);
		return this;
	}

	@Override
	public MCCommand setPermissionMessage(String permmsg) {
		cmd.setPermissionMessage(permmsg);
		return this;
	}

	@Override
	public MCCommand setUsage(String example) {
		cmd.setUsage(example);
		return this;
	}

	@Override
	public boolean testPermission(MCCommandSender target) {
		return cmd.testPermission(((BukkitMCCommandSender) target)._CommandSender());
	}

	@Override
	public boolean testPermissionSilent(MCCommandSender target) {
		return cmd.testPermissionSilent(((BukkitMCCommandSender) target)._CommandSender());
	}

	@Override
	public boolean register(MCCommandMap map) {
		return cmd.register(((BukkitMCCommandMap) map).scm);
	}

	@Override
	public boolean isRegistered() {
		return cmd.isRegistered();
	}

	@Override
	public boolean unregister(MCCommandMap map) {
		return cmd.unregister(((BukkitMCCommandMap) map).scm);
	}

	public static MCCommand newCommand(String name) {
		return new BukkitMCCommand(ReflectionUtils.newInstance(PluginCommand.class,
				new Class[]{String.class, Plugin.class}, new Object[]{name, CommandHelperPlugin.self}));
	}

	@Override
	public MCPlugin getPlugin() {
		if(!(cmd instanceof PluginIdentifiableCommand)) {
			return null;
		}
		return new BukkitMCPlugin(((PluginIdentifiableCommand) cmd).getPlugin());
	}

	@Override
	public MCPlugin getExecutor() {
		// TODO Not all plugins execute commands in their main class, so this cast won't always work
		if(!(cmd instanceof PluginCommand)) {
			return null;
		}
		return new BukkitMCPlugin((Plugin) ((PluginCommand) cmd).getExecutor());
	}

	@Override
	public MCPlugin getTabCompleter() {
		// TODO see above
		if(!(cmd instanceof PluginCommand)) {
			return null;
		}
		return new BukkitMCPlugin((Plugin) ((PluginCommand) cmd).getTabCompleter());
	}

	@Override
	public void setExecutor(MCPlugin plugin) {
		if(cmd instanceof PluginCommand) {
			((PluginCommand) cmd).setExecutor(((BukkitMCPlugin) plugin).getHandle());
		}
	}

	@Override
	public void setTabCompleter(MCPlugin plugin) {
		if(cmd instanceof PluginCommand) {
			((PluginCommand) cmd).setTabCompleter(((BukkitMCPlugin) plugin).getHandle());
		}
	}

	@Override
	public List<String> tabComplete(MCCommandSender sender, String alias, String[] args) {
		try {
			return cmd.tabComplete((CommandSender) sender.getHandle(), alias, args, null);
		} catch (CommandException ex) {
			throw new CREPluginInternalException(ex.getMessage(), Target.UNKNOWN);
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

	@Override
	public List<String> handleTabComplete(MCCommandSender sender, String alias, String[] args) {
		Environment env;
		try {
			env = Static.GenerateStandaloneEnvironment();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		if(Commands.onTabComplete.containsKey(cmd.getName().toLowerCase())) {
			Target t = Target.UNKNOWN;
			CArray cargs = new CArray(t, GenericParameters
					.addParameter(CString.TYPE, null).build(), env);
			for(String arg : args) {
				cargs.push(new CString(arg, t), t, env);
			}
			CClosure closure = Commands.onTabComplete.get(cmd.getName().toLowerCase());
			closure.getEnv().getEnv(CommandHelperEnvironment.class).SetCommandSender(sender);
			try {
				Mixed fret = closure.executeCallable(null, t, new CString(alias, t), new CString(sender.getName(), t), cargs,
						new CArray(t, null, env) // reserved for an obgen style command array
				);
				if(fret.isInstanceOf(CArray.TYPE, null, env)) {
					List<String> ret = new ArrayList<>();
					if(((CArray) fret).inAssociativeMode()) {
						for(Mixed key : ((CArray) fret).keySet(env)) {
							ret.add(((CArray) fret).get(key, Target.UNKNOWN, env).val());
						}
					} else {
						for(Mixed value : ((CArray) fret).asList(env)) {
							ret.add(value.val());
						}
					}
					return ret;
				}
			} catch (ConfigRuntimeException cre) {
				ConfigRuntimeException.HandleUncaughtException(cre, closure.getEnv());
				return new ArrayList<>();
			}
		}
		BukkitMCCommandTabCompleteEvent event = new BukkitMCCommandTabCompleteEvent(sender, cmd, alias, args);
		EventUtils.TriggerListener(Driver.TAB_COMPLETE, "tab_complete_command", event, CommandHelperPlugin.getCore().getLastLoadedEnv());
		return event.getCompletions();
	}

	@Override
	public boolean handleCustomCommand(MCCommandSender sender, String label, String[] args) {
		Environment env;
		try {
			env = Static.GenerateStandaloneEnvironment();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		if(Commands.onCommand.containsKey(cmd.getName().toLowerCase())) {
			Target t = Target.UNKNOWN;
			CArray cargs = new CArray(t, GenericParameters
					.addParameter(CString.TYPE, null).build(), env);
			for(String arg : args) {
				cargs.push(new CString(arg, t), t, env);
			}

			CClosure closure = Commands.onCommand.get(cmd.getName().toLowerCase());
			CommandHelperEnvironment cEnv = closure.getEnv().getEnv(CommandHelperEnvironment.class);
			cEnv.SetCommandSender(sender);
			cEnv.SetCommand("/" + label + StringUtils.Join(args, " "));

			try {
				Mixed fret = closure.executeCallable(null, t, new CString(label, t), new CString(sender.getName(), t), cargs,
						new CArray(t, null, env) // reserved for an obgen style command array
				);
				if(fret.isInstanceOf(CBoolean.TYPE, null, env)) {
					return ((CBoolean) fret).getBoolean();
				}
			} catch (ConfigRuntimeException cre) {
				cre.setEnv(closure.getEnv());
				ConfigRuntimeException.HandleUncaughtException(cre, closure.getEnv());
			}
			return true;
		} else {
			return false;
		}
	}
}
