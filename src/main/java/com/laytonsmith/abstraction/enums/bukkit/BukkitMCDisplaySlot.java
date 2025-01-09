package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.bukkit.BukkitMCServer;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCDisplaySlot;
import com.laytonsmith.annotations.abstractionenum;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import org.bukkit.scoreboard.DisplaySlot;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCDisplaySlot.class,
		forConcreteEnum = DisplaySlot.class
)
public class BukkitMCDisplaySlot extends EnumConvertor<MCDisplaySlot, DisplaySlot> {

	private static BukkitMCDisplaySlot instance;

	public static BukkitMCDisplaySlot getConvertor() {
		if(instance == null) {
			instance = new BukkitMCDisplaySlot();
		}
		return instance;
	}

	@Override
	protected MCDisplaySlot getAbstractedEnumCustom(DisplaySlot concrete) {
		if(!((BukkitMCServer) CommandHelperPlugin.myServer).isPaper()) {
			// name is different on Spigot
			return MCDisplaySlot.valueOf(concrete.name().replace("SIDEBAR_", "SIDEBAR_TEAM_"));
		}
		return super.getAbstractedEnumCustom(concrete);
	}

	@Override
	protected DisplaySlot getConcreteEnumCustom(MCDisplaySlot abstracted) {
		if(!((BukkitMCServer) CommandHelperPlugin.myServer).isPaper()) {
			return DisplaySlot.valueOf(abstracted.name().replace("TEAM_", ""));
		}
		return super.getConcreteEnumCustom(abstracted);
	}
}
