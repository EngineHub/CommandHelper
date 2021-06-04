package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCPotionCause;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.event.entity.EntityPotionEffectEvent;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCPotionCause.class,
		forConcreteEnum = EntityPotionEffectEvent.Cause.class
)
public class BukkitMCPotionCause extends EnumConvertor<MCPotionCause, EntityPotionEffectEvent.Cause> {

	private static BukkitMCPotionCause instance;

	public static BukkitMCPotionCause getConvertor() {
		if(instance == null) {
			instance = new BukkitMCPotionCause();
		}
		return instance;
	}
}

