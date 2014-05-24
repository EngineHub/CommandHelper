package com.laytonsmith.commandhelper;

import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.core.PermissionsResolver;
import com.sk89q.wepif.PermissionsResolverManager;

/**
 *
 * 
 */
public class CommandHelperPermissionsResolver implements PermissionsResolver {
	
	PermissionsResolverManager manager;
	
	public CommandHelperPermissionsResolver(PermissionsResolverManager manager){
		this.manager = manager;
	}

	@Override
	public boolean inGroup(String user, String group) {
		return manager.inGroup(user, group);
	}

	@Override
	public boolean hasPermission(String user, String permission, Object data) {
		String worldName = null;
		if(data instanceof MCWorld){
			worldName = ((MCWorld)data).getName();
		} else if(data instanceof String){
			worldName = (String)data;
		}
		return manager.hasPermission(worldName, user, permission);
	}

	@Override
	public boolean hasPermission(String user, String permission) {
		return manager.hasPermission(user, permission);
	}		

	@Override
	public String[] getGroups(String user) {
		return manager.getGroups(user);
	}
	
}
