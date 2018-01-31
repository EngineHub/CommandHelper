package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCTargetReason;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCTargetReason.class,
		forConcreteEnum = TargetReason.class
)
public class BukkitMCTargetReason extends EnumConvertor<MCTargetReason, TargetReason> {
	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCTargetReason instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCTargetReason getConvertor() {
		if(instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCTargetReason();
		}
		return instance;
	}
}
