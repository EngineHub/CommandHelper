package com.laytonsmith.commandhelper;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCPlugin;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlugin;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

/**
 * This class consolidates the WE/WG functions that require bukkit
 */
public class SKHandler {
	public static WorldEditPlugin getWorldEditPlugin(Target t) {
		if (Implementation.GetServerType() != Implementation.Type.BUKKIT) {
			throw new ConfigRuntimeException("Trying to use WorldEdit on non-bukkit server.", Exceptions.ExceptionType.InvalidPluginException, t);
		}
		if (CommandHelperPlugin.wep == null) {
			MCPlugin pwep = Static.getServer().getPluginManager().getPlugin("WorldEdit");
			if (pwep != null && pwep.isEnabled() && pwep.isInstanceOf(WorldEditPlugin.class) && pwep instanceof BukkitMCPlugin) {
				CommandHelperPlugin.wep = (WorldEditPlugin) ((BukkitMCPlugin) pwep).getHandle();
			}
		}
		return CommandHelperPlugin.wep;
	}

	public static WorldGuardPlugin getWorldGuardPlugin(Target t) {
		if (Implementation.GetServerType() != Implementation.Type.BUKKIT) {
			throw new ConfigRuntimeException("Trying to use WorldGuard on non-bukkit server.", Exceptions.ExceptionType.InvalidPluginException, t);
		}
		MCPlugin pwgp = Static.getServer().getPluginManager().getPlugin("WorldGuard");
		if (pwgp != null && pwgp.isEnabled() && pwgp.isInstanceOf(WorldGuardPlugin.class) && pwgp instanceof BukkitMCPlugin) {
			return (WorldGuardPlugin) ((BukkitMCPlugin) pwgp).getHandle();
		}
		return null;
	}
}
