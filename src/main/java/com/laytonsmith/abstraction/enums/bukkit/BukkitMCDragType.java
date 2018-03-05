package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCDragType;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.event.inventory.DragType;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCDragType.class,
		forConcreteEnum = DragType.class
)
public class BukkitMCDragType extends EnumConvertor<MCDragType, DragType> {

	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCDragType instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCDragType getConvertor() {
		if(instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCDragType();
		}
		return instance;
	}
}
