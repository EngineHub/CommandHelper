package com.laytonsmith.abstraction;

import java.util.List;

public interface MCCommand extends AbstractionObject {

	List<String> getAliases();

	String getDescription();

	String getLabel();

	String getName();

	String getPermission();

	String getPermissionMessage();

	String getUsage();

	MCCommand setAliases(List<String> aliases);

	MCCommand setDescription(String desc);

	MCCommand setLabel(String name);

	MCCommand setPermission(String perm);

	MCCommand setPermissionMessage(String permmsg);

	MCCommand setUsage(String example);

	boolean testPermission(MCCommandSender target);

	boolean testPermissionSilent(MCCommandSender target);

	boolean register(MCCommandMap map);

	boolean isRegistered();

	boolean unregister(MCCommandMap map);

	MCPlugin getPlugin();

	MCPlugin getExecutor();

	MCPlugin getTabCompleter();

	void setExecutor(MCPlugin plugin);

	void setTabCompleter(MCPlugin plugin);

	List<String> tabComplete(MCCommandSender sender, String alias, String[] args);

	List<String> handleTabComplete(MCCommandSender sender, String alias, String[] args);

	boolean handleCustomCommand(MCCommandSender sender, String label, String[] args);
}
