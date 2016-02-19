
package com.laytonsmith.abstraction.bukkit.enums;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCDragType;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.event.inventory.DragType;

/**
 *
 * @author MariuszT
 */
@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
forAbstractEnum = MCDragType.class,
forConcreteEnum = DragType.class)
public class BukkitMCDragType extends EnumConvertor<MCDragType, DragType> {

	private static BukkitMCDragType instance;

	public static BukkitMCDragType getConvertor() {
		if (instance == null) {
			instance = new BukkitMCDragType();
		}
		return instance;
	}
}
