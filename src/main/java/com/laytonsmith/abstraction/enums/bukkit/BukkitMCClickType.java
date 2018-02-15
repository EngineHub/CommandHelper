package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCClickType;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.event.inventory.ClickType;

@abstractionenum(
		implementation= Implementation.Type.BUKKIT,
		forAbstractEnum=MCClickType.class,
		forConcreteEnum=ClickType.class
)
public class BukkitMCClickType extends EnumConvertor<MCClickType, ClickType>{

	private static BukkitMCClickType instance;

	public static BukkitMCClickType getConvertor() {
		if(instance == null) {
			instance = new BukkitMCClickType();
		}
		return instance;
	}
}
