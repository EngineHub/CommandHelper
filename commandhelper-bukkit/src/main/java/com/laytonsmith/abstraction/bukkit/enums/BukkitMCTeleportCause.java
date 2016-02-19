package com.laytonsmith.abstraction.bukkit.enums;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCTeleportCause;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

/**
 *
 * 
 */
@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
forAbstractEnum = MCTeleportCause.class,
forConcreteEnum = TeleportCause.class)
public class BukkitMCTeleportCause extends EnumConvertor<MCTeleportCause, TeleportCause> {
	private static BukkitMCTeleportCause instance;

	public static BukkitMCTeleportCause getConvertor() {
		if (instance == null) {
			instance = new BukkitMCTeleportCause();
		}
		return instance;
	}
}
