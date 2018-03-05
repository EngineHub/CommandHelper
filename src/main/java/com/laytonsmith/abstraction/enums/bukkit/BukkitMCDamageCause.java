package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCDamageCause;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCDamageCause.class,
		forConcreteEnum = DamageCause.class
)
public class BukkitMCDamageCause extends EnumConvertor<MCDamageCause, DamageCause> {

	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCDamageCause instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCDamageCause getConvertor() {
		if (instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCDamageCause();
		}
		return instance;
	}
}
