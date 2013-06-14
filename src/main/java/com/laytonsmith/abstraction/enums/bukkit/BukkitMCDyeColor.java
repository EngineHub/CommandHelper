
package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.DyeColor;

/**
 *
 */
@abstractionenum(
		implementation=Implementation.Type.BUKKIT,
		forAbstractEnum=MCDyeColor.class,
		forConcreteEnum=DyeColor.class
		)
public class BukkitMCDyeColor extends EnumConvertor<MCDyeColor, DyeColor> {
	
	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCDyeColor instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCDyeColor getConvertor() {
		if (instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCDyeColor();
		}
		return instance;
	}
}
