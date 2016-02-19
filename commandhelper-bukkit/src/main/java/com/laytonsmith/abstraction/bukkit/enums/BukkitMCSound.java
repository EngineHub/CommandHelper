package com.laytonsmith.abstraction.bukkit.enums;

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

	private static BukkitMCSound instance;

	public static BukkitMCSound getConvertor() {
		if (instance == null) {
			instance = new BukkitMCSound();
		}
		return instance;
	}
}
