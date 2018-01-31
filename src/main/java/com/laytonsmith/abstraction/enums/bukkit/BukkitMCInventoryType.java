package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCInventoryType;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.event.inventory.InventoryType;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCInventoryType.class,
		forConcreteEnum = InventoryType.class)
public class BukkitMCInventoryType extends EnumConvertor<MCInventoryType, InventoryType> {
	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCInventoryType instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCInventoryType getConvertor() {
		if(instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCInventoryType();
		}
		return instance;
	}
}
