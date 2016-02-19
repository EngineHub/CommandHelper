
package com.laytonsmith.abstraction.bukkit.enums;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCDamageCause;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

/**
 *
 * 
 */
@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
forAbstractEnum = MCDamageCause.class,
forConcreteEnum = DamageCause.class)
public class BukkitMCDamageCause extends EnumConvertor<MCDamageCause, DamageCause> {
	private static BukkitMCDamageCause instance;

	public static BukkitMCDamageCause getConvertor() {
		if (instance == null) {
			instance = new BukkitMCDamageCause();
		}
		return instance;
	}
}
