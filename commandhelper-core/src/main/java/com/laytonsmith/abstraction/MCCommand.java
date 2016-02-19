package com.laytonsmith.abstraction;

import java.util.List;

public interface MCCommand extends AbstractionObject {

	public List<String> getAliases();
	
	public String getDescription();
	
	public String getLabel();
	
	public String getName();
	
	public String getPermission();
	
	public String getPermissionMessage();
	
	public String getUsage();
	
	public MCCommand setAliases(List<String> aliases);
	
	public MCCommand setDescription(String desc);
	
	public MCCommand setLabel(String name);
	
	public MCCommand setPermission(String perm);
	
	public MCCommand setPermissionMessage(String permmsg);
	
	public MCCommand setUsage(String example);
	
	public boolean testPermission(MCCommandSender target);
	
	public boolean testPermissionSilent(MCCommandSender target);
	
	public boolean register(MCCommandMap map);
	
	public boolean isRegistered();
	
	public boolean unregister(MCCommandMap map);

	public MCPlugin getPlugin();
	
	public MCPlugin getExecutor();
	
	public MCPlugin getTabCompleter();
	
	public void setExecutor(MCPlugin plugin);
	
	public void setTabCompleter(MCPlugin plugin);
	
	public List<String> handleTabComplete(MCCommandSender sender, String alias, String[] args);
	
	public boolean handleCustomCommand(MCCommandSender sender, String label, String[] args);
}
