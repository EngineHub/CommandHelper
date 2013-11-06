package com.laytonsmith.abstraction.enums.bukkit;

import org.bukkit.entity.Horse.Color;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCHorseColor;
import com.laytonsmith.annotations.abstractionenum;

/**
 *
 * @author Hekta
 */
@abstractionenum(
		implementation=Implementation.Type.BUKKIT,
		forAbstractEnum=MCHorseColor.class,
		forConcreteEnum=Color.class
		)
public class BukkitMCHorseColor extends EnumConvertor<MCHorseColor, Color> {

	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCHorseColor instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCHorseColor getConvertor() {
		if (instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCHorseColor();
		}
		return instance;
	}
}