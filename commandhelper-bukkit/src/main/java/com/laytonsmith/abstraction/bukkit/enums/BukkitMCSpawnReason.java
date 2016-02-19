package com.laytonsmith.abstraction.bukkit.enums;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCSpawnReason;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

/**
 *
 * @author jb_aero
 */
@abstractionenum(
		implementation=Implementation.Type.BUKKIT,
		forAbstractEnum=MCSpawnReason.class,
		forConcreteEnum=SpawnReason.class
		)
public class BukkitMCSpawnReason extends EnumConvertor<MCSpawnReason, SpawnReason> {

	private static BukkitMCSpawnReason instance;

	public static BukkitMCSpawnReason getConvertor() {
		if (instance == null) {
			instance = new BukkitMCSpawnReason();
		}
		return instance;
	}
}