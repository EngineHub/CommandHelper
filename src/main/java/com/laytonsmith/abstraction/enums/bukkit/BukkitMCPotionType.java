package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCPotionType;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.potion.PotionType;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCPotionType.class,
		forConcreteEnum = PotionType.class
)
public class BukkitMCPotionType extends EnumConvertor<MCPotionType, PotionType> {

	private static BukkitMCPotionType instance;

	public static BukkitMCPotionType getConvertor() {
		if(instance == null) {
			instance = new BukkitMCPotionType();
		}
		return instance;
	}
}
