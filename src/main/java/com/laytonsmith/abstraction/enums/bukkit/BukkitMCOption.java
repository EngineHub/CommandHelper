package com.laytonsmith.abstraction.enums.bukkit;

import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCOption;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.annotations.abstractionenum;
import com.laytonsmith.core.Static;

@abstractionenum(
		implementation= Implementation.Type.BUKKIT,
		forAbstractEnum=MCOption.class,
		forConcreteEnum=Team.Option.class
)
public class BukkitMCOption extends EnumConvertor<MCOption, Option>{

	private static BukkitMCOption instance;
	
	public static BukkitMCOption getConvertor() {
		if (instance == null) {
			instance = new BukkitMCOption();
		}
		return instance;
	}

	@Override
	protected Option getConcreteEnumCustom(MCOption abstracted) {
		if(Static.getServer().getMinecraftVersion().lt(MCVersion.MC1_9)) {
			return null;
		}
		return super.getConcreteEnumCustom(abstracted);
	}
}