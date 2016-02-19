
package com.laytonsmith.abstraction.bukkit.enums;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCTargetReason;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

/**
 *
 * 
 */
@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
forAbstractEnum = MCTargetReason.class,
forConcreteEnum = TargetReason.class)
public class BukkitMCTargetReason extends EnumConvertor<MCTargetReason, TargetReason> {
	private static BukkitMCTargetReason instance;

	public static BukkitMCTargetReason getConvertor() {
		if (instance == null) {
			instance = new BukkitMCTargetReason();
		}
		return instance;
	}
}
