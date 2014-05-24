package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCSound;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.Sound;

/**
 * 
 * @author jb_aero
 */
@abstractionenum(
		implementation=Implementation.Type.BUKKIT,
		forAbstractEnum=MCSound.class,
		forConcreteEnum=Sound.class
		)
public class BukkitMCSound extends EnumConvertor<MCSound, Sound> {

	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCSound instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCSound getConvertor() {
		if (instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCSound();
		}
		return instance;
	}
}
