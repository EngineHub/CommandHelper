package com.laytonsmith.abstraction.enums.bukkit;

import org.bukkit.entity.Horse.Style;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCHorseStyle;
import com.laytonsmith.annotations.abstractionenum;

/**
 *
 * @author Hekta
 */
@abstractionenum(
		implementation=Implementation.Type.BUKKIT,
		forAbstractEnum=MCHorseStyle.class,
		forConcreteEnum=Style.class
		)
public class BukkitMCHorseStyle extends EnumConvertor<MCHorseStyle, Style> {

	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCHorseStyle instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCHorseStyle getConvertor() {
		if (instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCHorseStyle();
		}
		return instance;
	}
}