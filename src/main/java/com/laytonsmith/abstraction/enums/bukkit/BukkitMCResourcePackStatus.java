package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCResourcePackStatus;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCResourcePackStatus.class,
		forConcreteEnum = PlayerResourcePackStatusEvent.Status.class
)
public class BukkitMCResourcePackStatus extends EnumConvertor<MCResourcePackStatus, PlayerResourcePackStatusEvent.Status> {

	private static BukkitMCResourcePackStatus instance;

	public static BukkitMCResourcePackStatus getConvertor() {
		if(instance == null) {
			instance = new BukkitMCResourcePackStatus();
		}
		return instance;
	}
}
