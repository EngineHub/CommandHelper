package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCRegainReason;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCRegainReason.class,
		forConcreteEnum = RegainReason.class
)
public class BukkitMCRegainReason extends EnumConvertor<MCRegainReason, RegainReason> {
	private static BukkitMCRegainReason INST;

	public static BukkitMCRegainReason getConvertor() {
		if(INST == null) {
			INST = new BukkitMCRegainReason();
		}
		return INST;
	}
}
