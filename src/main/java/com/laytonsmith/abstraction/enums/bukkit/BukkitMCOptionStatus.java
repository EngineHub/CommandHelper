package com.laytonsmith.abstraction.enums.bukkit;

import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.OptionStatus;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCOptionStatus;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.annotations.abstractionenum;
import com.laytonsmith.core.Static;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCOptionStatus.class,
		forConcreteEnum = Team.OptionStatus.class
)
public class BukkitMCOptionStatus extends EnumConvertor<MCOptionStatus, OptionStatus> {

	private static BukkitMCOptionStatus instance;

	public static BukkitMCOptionStatus getConvertor() {
		if(instance == null) {
			instance = new BukkitMCOptionStatus();
		}
		return instance;
	}

	@Override
	protected OptionStatus getConcreteEnumCustom(MCOptionStatus abstracted) {
		if(Static.getServer().getMinecraftVersion().lt(MCVersion.MC1_9)) {
			return null;
		}
		return super.getConcreteEnumCustom(abstracted);
	}
}
