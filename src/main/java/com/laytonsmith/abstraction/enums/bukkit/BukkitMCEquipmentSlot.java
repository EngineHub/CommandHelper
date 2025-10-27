package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.inventory.EquipmentSlot;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCEquipmentSlot.class,
		forConcreteEnum = EquipmentSlot.class
)
public class BukkitMCEquipmentSlot extends EnumConvertor<MCEquipmentSlot, EquipmentSlot> {

	private static BukkitMCEquipmentSlot instance;

	public static BukkitMCEquipmentSlot getConvertor() {
		if(instance == null) {
			instance = new BukkitMCEquipmentSlot();
		}
		return instance;
	}

	@Override
	protected MCEquipmentSlot getAbstractedEnumCustom(EquipmentSlot concrete) {
		switch(concrete) {
			case HAND:
				return MCEquipmentSlot.WEAPON;
			case FEET:
				return MCEquipmentSlot.BOOTS;
			case LEGS:
				return MCEquipmentSlot.LEGGINGS;
			case CHEST:
				return MCEquipmentSlot.CHESTPLATE;
			case HEAD:
				return MCEquipmentSlot.HELMET;
		}
		return super.getAbstractedEnumCustom(concrete);
	}

	@Override
	protected EquipmentSlot getConcreteEnumCustom(MCEquipmentSlot abstracted) {
		switch(abstracted) {
			case WEAPON:
				return EquipmentSlot.HAND;
			case BOOTS:
				return EquipmentSlot.FEET;
			case LEGGINGS:
				return EquipmentSlot.LEGS;
			case CHESTPLATE:
				return EquipmentSlot.CHEST;
			case HELMET:
				return EquipmentSlot.HEAD;
		}
		return super.getConcreteEnumCustom(abstracted);
	}
}
