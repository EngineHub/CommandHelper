package com.laytonsmith.abstraction.enums.bukkit;

import org.bukkit.entity.Horse.Variant;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCHorseVariant;
import com.laytonsmith.annotations.abstractionenum;

/**
 *
 * @author Hekta
 */
@abstractionenum(
		implementation=Implementation.Type.BUKKIT,
		forAbstractEnum=MCHorseVariant.class,
		forConcreteEnum=Variant.class
		)
public class BukkitMCHorseVariant extends EnumConvertor<MCHorseVariant, Variant> {

	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCHorseVariant instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCHorseVariant getConvertor() {
		if (instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCHorseVariant();
		}
		return instance;
	}
}