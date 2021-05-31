package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCPotionAction;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.event.entity.EntityPotionEffectEvent;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCPotionAction.class,
		forConcreteEnum = EntityPotionEffectEvent.Action.class
)
public class BukkitMCPotionAction extends EnumConvertor<MCPotionAction, EntityPotionEffectEvent.Action> {

	private static BukkitMCPotionAction instance;

	public static BukkitMCPotionAction getConvertor() {
		if(instance == null) {
			instance = new BukkitMCPotionAction();
		}
		return instance;
	}
}
