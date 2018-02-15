package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCDisplaySlot;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.scoreboard.DisplaySlot;

@abstractionenum(
		implementation= Implementation.Type.BUKKIT,
		forAbstractEnum=MCDisplaySlot.class,
		forConcreteEnum=DisplaySlot.class
)
public class BukkitMCDisplaySlot extends EnumConvertor<MCDisplaySlot, DisplaySlot>{

	private static BukkitMCDisplaySlot instance;

	public static BukkitMCDisplaySlot getConvertor() {
		if(instance == null) {
			instance = new BukkitMCDisplaySlot();
		}
		return instance;
	}
}
