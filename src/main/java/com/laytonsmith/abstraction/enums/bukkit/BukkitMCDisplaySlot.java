package com.laytonsmith.abstraction.enums.bukkit;

import org.bukkit.scoreboard.DisplaySlot;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCDisplaySlot;
import com.laytonsmith.annotations.abstractionenum;

/**
 * 
 * @author jb_aero
 */
@abstractionenum(
		implementation= Implementation.Type.BUKKIT,
		forAbstractEnum=MCDisplaySlot.class,
		forConcreteEnum=DisplaySlot.class
)
public class BukkitMCDisplaySlot extends EnumConvertor<MCDisplaySlot, DisplaySlot>{

	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCDisplaySlot instance;
	
	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCDisplaySlot getConverter() {
		if (instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCDisplaySlot();
		}
		return instance;
	}
}
