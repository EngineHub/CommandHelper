package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCUnleashReason;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.event.entity.EntityUnleashEvent.UnleashReason;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCUnleashReason.class,
		forConcreteEnum = UnleashReason.class
)
public class BukkitMCUnleashReason extends EnumConvertor<MCUnleashReason, UnleashReason> {

	private static BukkitMCUnleashReason instance;

	public static BukkitMCUnleashReason getConvertor() {
		if(instance == null) {
			instance = new BukkitMCUnleashReason();
		}
		return instance;
	}
}
