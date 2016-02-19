
package com.laytonsmith.abstraction.bukkit.enums;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.DyeColor;

/**
 *
 * 
 */
@abstractionenum(
		implementation=Implementation.Type.BUKKIT,
		forAbstractEnum=MCDyeColor.class,
		forConcreteEnum=DyeColor.class
		)
public class BukkitMCDyeColor extends EnumConvertor<MCDyeColor, DyeColor> {
	
	private static BukkitMCDyeColor instance;

	public static BukkitMCDyeColor getConvertor() {
		if (instance == null) {
			instance = new BukkitMCDyeColor();
		}
		return instance;
	}
}
