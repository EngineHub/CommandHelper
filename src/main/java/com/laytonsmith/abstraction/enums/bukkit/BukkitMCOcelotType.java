package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCOcelotType;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.entity.Ocelot;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCOcelotType.class,
		forConcreteEnum = Ocelot.Type.class
)
public class BukkitMCOcelotType extends EnumConvertor<MCOcelotType, Ocelot.Type> {

	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCOcelotType instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCOcelotType getConvertor() {
		if(instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCOcelotType();
		}
		return instance;
	}
}
