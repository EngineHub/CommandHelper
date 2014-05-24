package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCInventoryAction;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.event.inventory.InventoryAction;

/**
 * 
 * @author jb_aero
 */
@abstractionenum(
		implementation= Implementation.Type.BUKKIT,
		forAbstractEnum=MCInventoryAction.class,
		forConcreteEnum=InventoryAction.class
)
public class BukkitMCInventoryAction extends EnumConvertor<MCInventoryAction, InventoryAction>{

	private static BukkitMCInventoryAction instance;
	
	public static BukkitMCInventoryAction getConvertor() {
		if (instance == null) {
			instance = new BukkitMCInventoryAction();
		}
		return instance;
	}
}
