package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCSpawnReason;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCSpawnReason.class,
		forConcreteEnum = SpawnReason.class
)
public class BukkitMCSpawnReason extends EnumConvertor<MCSpawnReason, SpawnReason> {

	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCSpawnReason instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCSpawnReason getConvertor() {
		if(instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCSpawnReason();
		}
		return instance;
	}
}
