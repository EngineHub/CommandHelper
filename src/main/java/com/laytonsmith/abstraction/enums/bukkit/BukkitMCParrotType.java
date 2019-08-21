package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCParrotType;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.entity.Parrot;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCParrotType.class,
		forConcreteEnum = Parrot.Variant.class
)
public class BukkitMCParrotType extends EnumConvertor<MCParrotType, Parrot.Variant> {

	private static BukkitMCParrotType instance;

	public static BukkitMCParrotType getConvertor() {
		if(instance == null) {
			instance = new BukkitMCParrotType();
		}
		return instance;
	}
}
