package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCRabbitType;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.entity.Rabbit;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCRabbitType.class,
		forConcreteEnum = Rabbit.Type.class
)
public class BukkitMCRabbitType extends EnumConvertor<MCRabbitType, Rabbit.Type> {

	private static BukkitMCRabbitType instance;

	public static BukkitMCRabbitType getConvertor() {
		if(instance == null) {
			instance = new BukkitMCRabbitType();
		}
		return instance;
	}
}
