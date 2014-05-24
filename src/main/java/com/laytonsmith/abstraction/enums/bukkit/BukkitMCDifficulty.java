package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCDifficulty;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.Difficulty;

/**
 *
 * @author Hekta
 */
@abstractionenum(
		implementation=Implementation.Type.BUKKIT,
		forAbstractEnum=MCDifficulty.class,
		forConcreteEnum=Difficulty.class
		)
public class BukkitMCDifficulty extends EnumConvertor<MCDifficulty, Difficulty> {

	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCDifficulty instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCDifficulty getConvertor() {
		if (instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCDifficulty();
		}
		return instance;
	}
}