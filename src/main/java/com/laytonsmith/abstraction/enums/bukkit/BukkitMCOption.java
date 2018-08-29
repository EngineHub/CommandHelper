package com.laytonsmith.abstraction.enums.bukkit;

import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCOption;
import com.laytonsmith.annotations.abstractionenum;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCOption.class,
		forConcreteEnum = Team.Option.class
)
public class BukkitMCOption extends EnumConvertor<MCOption, Option> {

	private static BukkitMCOption instance;

	public static BukkitMCOption getConvertor() {
		if(instance == null) {
			instance = new BukkitMCOption();
		}
		return instance;
	}
}
